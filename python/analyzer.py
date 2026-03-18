import cv2
import os
from ultralytics import YOLO
import supervision as sv
from utils import get_foot_position, output_json, get_center_of_bbox
from team_assigner import TeamAssigner
from estimators import ViewTransformer, SpeedDistanceEstimator
from annotator import VideoAnnotator
from ball_assigner import PlayerBallAssigner
import json
import numpy as np
import sys
import subprocess
from config import EASYOCR_AVAILABLE, PADDLEOCR_AVAILABLE
from ocr_engine import JerseyNumberRecognizer
class FootballAnalyzer:
    def __init__(self, model_path, device='CUDA', teams_config_path=None, enable_ocr=True, enable_annotation=True, use_world_model=False):
        self.model = YOLO(model_path)
        self.use_world_model = use_world_model
        self.use_soccer_model = False  
        self.device = device
        self.ball_model = None  
        self.use_hybrid_mode = False  
        self.team_ball_control = {1: 0, 2: 0}
        self.initialized = False
        self.goal_cooldown = 0  
        self.last_event = None
        self.use_half = device == 'cuda'
        self.last_ball_bbox = None
        self.ball_not_found_count = 0
        self.max_ball_interpolation_frames = 15  
        self.ball_positions_history = []
        self.max_ball_history = 5
        self.goal_model = self._load_goal_model(model_path)
        self.goal_regions = [] 
        self.goal_cooldown = 0
        self.frames_ball_in_goal = 0 
        self.GOAL_CONFIRMATION_THRESH = 3
        self.ball_in_goal_frames = 0 
        self.MIN_GOAL_FRAMES = 3
      

        try:
            base_dir = os.path.dirname(os.path.abspath(__file__)) if '__file__' in locals() else os.getcwd()
            possible_paths = [
                os.path.join(base_dir, 'models', 'goal_model.pt'),
                os.path.join(os.path.dirname(model_path), 'goal_model.pt'),
                'models/goal_model.pt',
                'goal_model.pt'
            ]
            goal_path = next((p for p in possible_paths if os.path.exists(p)), None)
            
            if goal_path:
                self.goal_model = YOLO(goal_path)
                if device == 'cuda': self.goal_model.to(device)
                output_json({"type": "info", "message": f"Goal detection model loaded: {goal_path}"})
            else:
                output_json({"type": "warning", "message": "goal_model.pt not found. Goal detection disabled."})
        except Exception as e:
            output_json({"type": "warning", "message": f"Failed to load goal model: {e}"})

        if self.use_half and self.goal_model:
            try:
                self.goal_model.model.half()
            except: pass
        self.use_half = device == 'cuda'
        if self.use_half:
            self.model.to(device)
            try:
                import torch
                if torch.cuda.is_available():
                    self.model.model.half() 
                    output_json({"type": "info", "message": "GPU FP16 (half precision) enabled - faster inference"})
            except Exception:
                self.use_half = False

        if 'world' in model_path.lower():
            self.use_world_model = True
            self.model.set_classes(["person", "soccer ball", "football", "ball"])
            output_json({"type": "info", "message": "Using YOLO-World with custom classes: person, soccer ball, football, ball"})
        elif 'soccer' in model_path.lower() or 'forzasys' in model_path.lower():
           
            self.use_soccer_model = True
            output_json({"type": "info", "message": "Using specialized soccer model (SportsVision-YOLO) - FAST mode enabled"})
        elif 'yolov8n' in model_path.lower():
            soccer_model_path = os.path.join(os.path.dirname(model_path), 'yolov10m.pt')
            if os.path.exists(soccer_model_path):
                try: 
                    self.ball_model = YOLO(soccer_model_path)
                    self.use_hybrid_mode = True
                    output_json({"type": "info", "message": "HYBRID MODE: Nano (joueurs rapide) + Soccer model (ballon précis)"})
                except Exception as e:
                    output_json({"type": "warning", "message": f"Could not load soccer model for ball: {e}"})

        self.tracker = sv.ByteTrack()

        teams_config = None
        if teams_config_path and os.path.exists(teams_config_path):
            try:
                with open(teams_config_path, 'r', encoding='utf-8') as f:
                    teams_config = json.load(f)
                    output_json({"type": "info", "message": f"Loaded teams config from {teams_config_path}"})
            except Exception as e:
                output_json({"type": "warning", "message": f"Could not load teams config: {e}"})

        self.team_assigner = TeamAssigner(teams_config=teams_config)
        self.view_transformer = ViewTransformer()
        self.speed_estimator = SpeedDistanceEstimator()
        self.ball_assigner = PlayerBallAssigner()
        self.device = device

        self.enable_ocr = enable_ocr and (PADDLEOCR_AVAILABLE or EASYOCR_AVAILABLE)
        self.jersey_recognizer = JerseyNumberRecognizer(teams_config_path) if self.enable_ocr else None
        self.ocr_frame_interval = 3 

        self.enable_annotation = enable_annotation
        display_colors = self.team_assigner.display_colors if self.team_assigner.display_colors else None
        self.annotator = VideoAnnotator(team_colors=display_colors) if enable_annotation else None

       
        self.team_ball_control = {1: 0, 2: 0}
        self.initialized = False

        self.last_ball_bbox = None
        self.ball_not_found_count = 0
        self.max_ball_interpolation_frames = 15 

        self.ball_positions_history = []  
        self.max_ball_history = 5  

    def _sahi_ball_detection(self, frame, cls_names_inv, ball_class_ids):
        """
        SAHI (Slicing Aided Hyper Inference) for small ball detection in wide-angle shots.
        Splits the frame into overlapping tiles and detects ball in each tile.
        Uses 400x400 slices which are optimal for detecting small balls.
        """
        h, w = frame.shape[:2]

        slice_size = 400
        overlap = 100  
        step = slice_size - overlap

        best_ball = None
        best_conf = 0.0

        cols = max(1, (w - overlap) // step + 1)
        rows = max(1, (h - overlap) // step + 1)

        for row in range(rows + 1):
            for col in range(cols + 1):
                x1 = min(col * step, max(0, w - slice_size))
                y1 = min(row * step, max(0, h - slice_size))
                x2 = min(x1 + slice_size, w)
                y2 = min(y1 + slice_size, h)

                if x2 - x1 < 200 or y2 - y1 < 200:
                    continue

                slice_img = frame[y1:y2, x1:x2]

                try:
                  
                    results = self.model.predict(slice_img, conf=0.01, device=self.device,
                                                verbose=False, imgsz=640)[0]
                    det = sv.Detections.from_ultralytics(results)

                    if len(det) > 0 and len(ball_class_ids) > 0:
                        ball_filter = np.isin(det.class_id, ball_class_ids)
                        if np.any(ball_filter):
                            ball_only = det[ball_filter]
                            max_idx = np.argmax(ball_only.confidence)
                            conf = ball_only.confidence[max_idx]

                            if conf > best_conf:
                                best_conf = conf
                                bbox = ball_only.xyxy[max_idx].tolist()
                                best_ball = [
                                    bbox[0] + x1,
                                    bbox[1] + y1,
                                    bbox[2] + x1,
                                    bbox[3] + y1
                                ]
                except Exception:
                    pass

        return best_ball

    def _fast_sahi_ball_detection(self, frame, cls_names_inv, ball_class_ids):
        """
        Fast SAHI: Check strategic 400x400 slices covering key areas.
        For streaming mode where speed is critical but we still need small ball detection.
        """
        h, w = frame.shape[:2]

        slice_size = 400
        slices = []

        
        step_x = (w - slice_size) // 2  # 3 columns
        step_y = (h - slice_size) // 1 if h <= slice_size else (h - slice_size) // 2  # 2-3 rows

        for row in range(3):
            y = min(row * step_y, max(0, h - slice_size))
            for col in range(3):
                x = min(col * step_x, max(0, w - slice_size))
                slices.append((x, y, x + slice_size, min(y + slice_size, h)))

        # If we have last ball position, add a targeted slice there (priority)
        if self.last_ball_bbox is not None:
            last_cx = (self.last_ball_bbox[0] + self.last_ball_bbox[2]) / 2
            last_cy = (self.last_ball_bbox[1] + self.last_ball_bbox[3]) / 2

            # Predict position based on velocity
            if len(self.ball_positions_history) >= 2:
                p1 = self.ball_positions_history[-2]
                p2 = self.ball_positions_history[-1]
                vx = (p2[1] - p1[1]) * (self.ball_not_found_count + 1)
                vy = (p2[2] - p1[2]) * (self.ball_not_found_count + 1)
                last_cx += vx
                last_cy += vy

            # Add predicted location slice at the beginning (priority)
            x1 = max(0, int(last_cx - slice_size // 2))
            y1 = max(0, int(last_cy - slice_size // 2))
            slices.insert(0, (x1, y1, min(x1 + slice_size, w), min(y1 + slice_size, h)))

        best_ball = None
        best_conf = 0.0

        for x1, y1, x2, y2 in slices:
            # Skip invalid or duplicate regions
            if x2 - x1 < 200 or y2 - y1 < 200:
                continue

            slice_img = frame[y1:y2, x1:x2]
            if slice_img.size == 0:
                continue

            try:
                # Detect with low confidence on 400x400 slice
                results = self.model.predict(slice_img, conf=0.01, device=self.device,
                                            verbose=False, imgsz=640)[0]
                det = sv.Detections.from_ultralytics(results)

                if len(det) > 0 and len(ball_class_ids) > 0:
                    ball_filter = np.isin(det.class_id, ball_class_ids)
                    if np.any(ball_filter):
                        ball_only = det[ball_filter]
                        max_idx = np.argmax(ball_only.confidence)
                        conf = ball_only.confidence[max_idx]

                        if conf > best_conf:
                            best_conf = conf
                            bbox = ball_only.xyxy[max_idx].tolist()
                            best_ball = [
                                bbox[0] + x1,
                                bbox[1] + y1,
                                bbox[2] + x1,
                                bbox[3] + y1
                            ]

                            # If we found a good detection, stop searching (speed optimization)
                            if conf > 0.15:
                                return best_ball
            except Exception:
                pass

        return best_ball

    def _smart_ball_detection(self, frame):
       
        ball_bbox = None
        h, w = frame.shape[:2]

        if self.ball_roi is not None:
            rx1, ry1, rx2, ry2 = self.ball_roi
            roi_img = frame[ry1:ry2, rx1:rx2]
            
            results = self.model.predict(roi_img, conf=0.10, verbose=False, imgsz=320)[0]
            for box in results.boxes:
                if results.names[int(box.cls[0])] in ['ball', 'soccer ball', 'sports ball']:
                    bbox = box.xyxy[0].cpu().numpy()
                    ball_bbox = [bbox[0]+rx1, bbox[1]+ry1, bbox[2]+rx1, bbox[3]+ry1]
                    break

    
        if ball_bbox is None:
            results = self.model.predict(frame, conf=0.08, verbose=False, imgsz=640)[0]
            for box in results.boxes:
                if results.names[int(box.cls[0])] in ['ball', 'soccer ball', 'sports ball']:
                    ball_bbox = box.xyxy[0].cpu().numpy().tolist()
                    break

        if ball_bbox:
            self.ball_roi = [
                max(0, int(ball_bbox[0] - self.roi_margin)),
                max(0, int(ball_bbox[1] - self.roi_margin)),
                min(w, int(ball_bbox[2] + self.roi_margin)),
                min(h, int(ball_bbox[3] + self.roi_margin))
            ]
            self.ball_not_found_count = 0
        else:
            self.ball_not_found_count += 1
            if self.ball_not_found_count > 10:
                self.ball_roi = None

        return ball_bbox
    def _enhanced_ball_detection(self, frame, cls_names_inv, fast_mode, is_coco_model, ball_id_coco):
        
        if fast_mode:
            return None

        ball_class_ids = []
        
        if is_coco_model:
            if ball_id_coco is not None:
                ball_class_ids = [ball_id_coco]
        else:
          
            potential_names = ['ball', 'soccer ball', 'football', 'sports ball']
            for name in potential_names:
                if name in cls_names_inv:
                    ball_class_ids.append(cls_names_inv[name])

        return self._sahi_ball_detection(frame, cls_names_inv, ball_class_ids)
    def _detect_ball_in_roi(self, frame, cls_names_inv, ball_class_ids):
        """Detect ball in a region of interest around last known position - FAST"""
        if self.last_ball_bbox is None:
            return None

        # Only try ROI detection for first few frames after losing ball
        if self.ball_not_found_count > 3:
            return None

        h, w = frame.shape[:2]

        # Calculate ROI around last ball position
        cx = (self.last_ball_bbox[0] + self.last_ball_bbox[2]) / 2
        cy = (self.last_ball_bbox[1] + self.last_ball_bbox[3]) / 2

        # Predict movement if we have history
        if len(self.ball_positions_history) >= 2:
            p1 = self.ball_positions_history[-2]
            p2 = self.ball_positions_history[-1]
            vx = (p2[1] - p1[1]) * (self.ball_not_found_count + 1)
            vy = (p2[2] - p1[2]) * (self.ball_not_found_count + 1)
            cx += vx
            cy += vy

        # Fixed ROI size (not too large for speed)
        roi_size = 120

        x1 = max(0, int(cx - roi_size))
        y1 = max(0, int(cy - roi_size))
        x2 = min(w, int(cx + roi_size))
        y2 = min(h, int(cy + roi_size))

        # Extract ROI
        roi = frame[y1:y2, x1:x2]
        if roi.size == 0:
            return None

        # Detect in ROI with low confidence but small image size for speed
        try:
            results = self.model.predict(roi, conf=0.03, device=self.device, verbose=False, imgsz=256)[0]
            det = sv.Detections.from_ultralytics(results)

            if len(det) > 0 and len(ball_class_ids) > 0:
                ball_filter = np.isin(det.class_id, ball_class_ids)
                if np.any(ball_filter):
                    ball_only = det[ball_filter]
                    max_conf_idx = np.argmax(ball_only.confidence)
                    bbox = ball_only.xyxy[max_conf_idx].tolist()

                    # Convert back to full frame coordinates
                    return [bbox[0] + x1, bbox[1] + y1, bbox[2] + x1, bbox[3] + y1]
        except Exception:
            pass

        return None

    def _detect_ball_with_soccer_model(self, frame, ultra_fast_mode=False):
        """
        HYBRID MODE: Use specialized soccer model for ball detection only.
        This is more accurate than COCO model for football detection.
        Optimized for speed - uses small imgsz and runs only for ball class.
        """
        if self.ball_model is None:
            return None

        try:
            # Use small image size for speed, but large enough for ball detection
            # Soccer model: classes {0: 'player', 1: 'ball', 2: 'logo'}
            imgsz = 640 if ultra_fast_mode else 800
            conf = 0.15 if ultra_fast_mode else 0.10

            # Only detect ball class (id=1) for speed
            results = self.ball_model.predict(
                frame,
                conf=conf,
                device=self.device,
                verbose=False,
                imgsz=imgsz,
                classes=[1]  # Only ball class
            )[0]

            det = sv.Detections.from_ultralytics(results)

            if len(det) > 0:
                # Get highest confidence ball
                max_idx = np.argmax(det.confidence)
                return det.xyxy[max_idx].tolist()

        except Exception as e:
            pass

        return None

    def _predict_ball_position(self, frame_num):
        if len(self.ball_positions_history) < 2:
            return self.last_ball_bbox

        p1 = self.ball_positions_history[-2]
        p2 = self.ball_positions_history[-1]
    
        dt = p2[0] - p1[0]
        if dt == 0: return self.last_ball_bbox

        vx = (p2[1] - p1[1]) / dt
        vy = (p2[2] - p1[2]) / dt

        frames_missed = frame_num - p2[0]
        pred_cx = p2[1] + (vx * frames_missed)
        pred_cy = p2[2] + (vy * frames_missed)

        w = self.last_ball_bbox[2] - self.last_ball_bbox[0]
        h = self.last_ball_bbox[3] - self.last_ball_bbox[1]

        return [pred_cx - w/2, pred_cy - h/2, pred_cx + w/2, pred_cy + h/2]

    def analyze_frame(self, frame, frame_num, fast_mode=False, ultra_fast_mode=False):

        h, w = frame.shape[:2]
           
        pitch_polygon = np.array([
            [int(w*0.1), int(h*0.8)], [int(w*0.9), int(h*0.8)], 
            [int(w*0.7), int(h*0.2)], [int(w*0.3), int(h*0.2)]
        ])
        mask = np.zeros((h, w), dtype=np.uint8)
        cv2.fillPoly(mask, [pitch_polygon], 255)
        masked_frame = cv2.bitwise_and(frame, frame, mask=mask)

        results = self.model.predict(masked_frame, conf=0.15, imgsz=640, verbose=False)[0]
        half_param = self.use_half if hasattr(self, 'use_half') else False
        ball_bbox = None

        
        if self.use_soccer_model:
            conf_threshold = 0.05
            imgsz = 960 
        elif self.use_world_model:
            conf_threshold = 0.03
            imgsz = 960
        else:
            conf_threshold = 0.05
            imgsz = 960

        results = self.model.predict(frame, conf=conf_threshold, device=self.device,
                                     verbose=False, imgsz=imgsz, half=half_param)[0]
        detections = sv.Detections.from_ultralytics(results)

        # SPEED MODE: Skip hybrid detection in ultra_fast_mode for maximum speed
        # User can test if ball is detected with imgsz=640 only
        if self.use_soccer_model and not ultra_fast_mode:
            # Only do fallback in normal mode, not streaming
            cls_names = results.names
            cls_names_inv = {v: k for k, v in cls_names.items()}
            ball_id = cls_names_inv.get('ball', -1)
            if ball_id != -1:
                ball_mask = detections.class_id == ball_id
                ball_found_in_first_pass = np.any(ball_mask)

                # If ball not found in normal mode, try imgsz=800
                if not ball_found_in_first_pass:
                    results_hires = self.model.predict(frame, conf=0.05, device=self.device,
                                                       verbose=False, imgsz=800, half=half_param)[0]
                    det_hires = sv.Detections.from_ultralytics(results_hires)

                    if len(det_hires) > 0:
                        ball_mask_hires = det_hires.class_id == ball_id
                        if np.any(ball_mask_hires):
                            ball_det = det_hires[ball_mask_hires]
                            if len(detections) > 0:
                                detections = sv.Detections(
                                    xyxy=np.vstack([detections.xyxy, ball_det.xyxy]),
                                    confidence=np.concatenate([detections.confidence, ball_det.confidence]),
                                    class_id=np.concatenate([detections.class_id, ball_det.class_id])
                                )
                            else:
                                detections = ball_det

        # Get class names
        cls_names = results.names
        cls_names_inv = {v: k for k, v in cls_names.items()}

        # Support different model types
        is_coco_model = False

        if self.use_soccer_model:
            # SportsVision-YOLO: {0: 'player', 1: 'ball', 2: 'logo'}
            player_id = cls_names_inv.get('player', -1)
            ball_id = cls_names_inv.get('ball', -1)

            players_mask = detections.class_id == player_id
            goalkeeper_mask = np.zeros(len(detections), dtype=bool)
            referee_mask = np.zeros(len(detections), dtype=bool)
            ball_mask = detections.class_id == ball_id

        elif self.use_world_model:
            # YOLO-World with custom classes: person, soccer ball, football, ball
            person_id = cls_names_inv.get('person', -1)
            # Ball can be detected as "soccer ball", "football", or "ball"
            soccer_ball_id = cls_names_inv.get('soccer ball', -1)
            football_id = cls_names_inv.get('football', -1)
            ball_id = cls_names_inv.get('ball', -1)

            players_mask = detections.class_id == person_id
            goalkeeper_mask = np.zeros(len(detections), dtype=bool)
            referee_mask = np.zeros(len(detections), dtype=bool)

            # Combine all ball classes
            ball_mask = np.zeros(len(detections), dtype=bool)
            if soccer_ball_id != -1:
                ball_mask |= (detections.class_id == soccer_ball_id)
            if football_id != -1:
                ball_mask |= (detections.class_id == football_id)
            if ball_id != -1:
                ball_mask |= (detections.class_id == ball_id)

        elif 'person' in cls_names_inv:
            # COCO model: treat all 'person' detections as players
            is_coco_model = True
            person_id = cls_names_inv.get('person', -1)
            ball_id = cls_names_inv.get('sports ball', -1)
            players_mask = detections.class_id == person_id
            goalkeeper_mask = np.zeros(len(detections), dtype=bool)
            referee_mask = np.zeros(len(detections), dtype=bool)
            ball_mask = detections.class_id == ball_id
        else:
            # Custom football model with goalkeeper/referee
            players_mask = detections.class_id == cls_names_inv.get('player', -1)
            goalkeeper_mask = detections.class_id == cls_names_inv.get('goalkeeper', -1)
            referee_mask = detections.class_id == cls_names_inv.get('referee', -1)
            ball_mask = detections.class_id == cls_names_inv.get('ball', -1)

        # Combine players and goalkeepers
        players_goalkeeper_mask = players_mask | goalkeeper_mask

        # Get bounding boxes
        players_bboxes = detections.xyxy[players_goalkeeper_mask].tolist()
        referee_bboxes = detections.xyxy[referee_mask].tolist()
        ball_bboxes = detections.xyxy[ball_mask].tolist()
        ball_bbox = ball_bboxes[0] if ball_bboxes else None

        # HYBRID MODE: Use soccer model for ball detection (much more accurate)
        if ball_bbox is None and self.use_hybrid_mode and self.ball_model is not None:
            ball_bbox = self._detect_ball_with_soccer_model(frame, ultra_fast_mode)

        # ENHANCED BALL DETECTION - Multi-scale, multi-pass approach
        # Skip SAHI in ultra_fast_mode to maintain speed (rely on interpolation)
        if ball_bbox is None and not ultra_fast_mode and not self.use_hybrid_mode:
            ball_bbox = self._enhanced_ball_detection(frame, cls_names_inv, fast_mode, is_coco_model, ball_id if is_coco_model else None)

        # Ball tracking with motion prediction
        if ball_bbox is not None:
            # Update ball history for motion tracking
            cx = (ball_bbox[0] + ball_bbox[2]) / 2
            cy = (ball_bbox[1] + ball_bbox[3]) / 2
            self.ball_positions_history.append((frame_num, cx, cy))
            if len(self.ball_positions_history) > self.max_ball_history:
                self.ball_positions_history.pop(0)

            self.last_ball_bbox = ball_bbox
            self.ball_not_found_count = 0
        elif self.last_ball_bbox is not None and self.ball_not_found_count < self.max_ball_interpolation_frames:
            # Use predicted ball position based on motion history
            ball_bbox = self._predict_ball_position(frame_num)
            self.ball_not_found_count += 1
        event_type, goal_bbox = self._detect_goal_event(frame, ball_bbox, frame_num)

        # Track players
        players_detections = detections[players_goalkeeper_mask]
        if len(players_detections) > 0:
            tracked = self.tracker.update_with_detections(players_detections)
            players_bboxes = tracked.xyxy.tolist()
            players_ids = tracked.tracker_id.tolist() if tracked.tracker_id is not None else list(range(len(players_bboxes)))
        else:
            players_ids = []

        # Initialize team colors on first valid frame
        if not self.initialized and len(players_bboxes) >= 4:
            self.team_assigner.assign_team_color(frame, players_bboxes)
            self.initialized = True

        # Process each player
        players_data = []
        team_counts = {1: 0, 2: 0}

        for i, bbox in enumerate(players_bboxes):
            player_id = players_ids[i] if i < len(players_ids) else i

            # Get team
            team_id = self.team_assigner.get_player_team(frame, bbox, player_id)
            team_counts[team_id] = team_counts.get(team_id, 0) + 1

            # Get position
            foot_pos = get_foot_position(bbox)

            # --- MODIFICATION FORCEE (Calcul toujours actif) ---
            try:
                # On transforme toujours la position
                transformed_pos = self.view_transformer.transform_point(np.array(foot_pos))

                # On calcule toujours la vitesse
                safe_player_id = int(player_id)
                pos_input = transformed_pos[0].tolist() if transformed_pos is not None else None

                speed, distance = self.speed_estimator.calculate_speed_distance(
                    safe_player_id,
                    pos_input,
                    frame_num
                )
            except Exception:
                speed, distance = 0, 0
                transformed_pos = None
            # ---------------------------------------------------

            # Jersey number recognition
            jersey_number = None
            player_name = None
            if self.jersey_recognizer:
                cached_number = self.jersey_recognizer.player_jersey_cache.get(player_id)
                high_conf = self.jersey_recognizer.jersey_confidence.get(player_id, 0) >= 2

                if high_conf:
                    jersey_number = cached_number
                elif ultra_fast_mode:
                    if not hasattr(self, '_ocr_count_this_frame'):
                        self._ocr_count_this_frame = 0
                        self._last_ocr_frame_num = -1

                    if frame_num != self._last_ocr_frame_num:
                        self._ocr_count_this_frame = 0
                        self._last_ocr_frame_num = frame_num

                    if self._ocr_count_this_frame < 2:
                        jersey_number = self.jersey_recognizer.recognize_number(frame, bbox, player_id)
                        self._ocr_count_this_frame += 1
                    else:
                        jersey_number = cached_number
                else:
                    if frame_num % self.ocr_frame_interval == 0:
                        jersey_number = self.jersey_recognizer.recognize_number(frame, bbox, player_id)
                    else:
                        jersey_number = cached_number

            if jersey_number and self.jersey_recognizer:
                player_name = self.jersey_recognizer.get_player_name(team_id, jersey_number)

            player_data = {
                "id": int(player_id),
                "team": int(team_id),
                "jersey_number": jersey_number,
                "player_name": player_name,
                "bbox": [round(x, 2) for x in bbox],
                "position_pixels": list(foot_pos),
                "position_field": transformed_pos[0].tolist() if transformed_pos is not None else None,
                "speed_kmh": round(speed, 2) if speed else 0,
                "distance_m": round(distance, 2) if distance else 0
            }
            players_data.append(player_data)
        # Ball possession
        ball_holder_id = self.ball_assigner.assign_ball_to_player(
            players_bboxes, players_ids, ball_bbox
        )

        if ball_holder_id != -1:
            for p in players_data:
                if p["id"] == ball_holder_id:
                    p["has_ball"] = True
                    self.team_ball_control[p["team"]] += 1
                    break

        # Calculate possession percentage
        total_control = self.team_ball_control[1] + self.team_ball_control[2]
        if total_control > 0:
            possession = {
                1: round(self.team_ball_control[1] / total_control * 100, 1),
                2: round(self.team_ball_control[2] / total_control * 100, 1)
            }
        else:
            possession = {1: 50.0, 2: 50.0}

        # Ball data (simplified in ultra_fast_mode)
        ball_data = None
        if ball_bbox:
            ball_center = get_center_of_bbox(ball_bbox)
            if ultra_fast_mode:
                ball_data = {
                    "bbox": [round(x, 2) for x in ball_bbox],
                    "position_pixels": list(ball_center),
                    "position_field": None
                }
            else:
                ball_transformed = self.view_transformer.transform_point(np.array(ball_center))
                ball_data = {
                    "bbox": [round(x, 2) for x in ball_bbox],
                    "position_pixels": list(ball_center),
                    "position_field": ball_transformed[0].tolist() if ball_transformed is not None else None
                }

        # Get team names if available
        team_1_name = self.jersey_recognizer.get_team_name(1) if self.jersey_recognizer else "Equipe 1"
        team_2_name = self.jersey_recognizer.get_team_name(2) if self.jersey_recognizer else "Equipe 2"


      
      
        goal_bbox_detected = None
        ball_data = None
        
        if ball_bbox:
            ball_center = get_center_of_bbox(ball_bbox)
            # Create ball data object for result
            if ultra_fast_mode:
                ball_data = {"bbox": [round(x,2) for x in ball_bbox], "position_pixels": list(ball_center), "position_field": None}
            else:
                # Add transformation logic here if needed, or keep simpler
                ball_data = {"bbox": [round(x,2) for x in ball_bbox], "position_pixels": list(ball_center)}

        # A. Run Goal Detection
        if self.goal_model:
            try:
                g_res = self.goal_model.predict(frame, conf=0.30, verbose=False, device=self.device, half=half_param)[0]
                if len(g_res.boxes) > 0:
                    areas = [(b.xyxy[0][2]-b.xyxy[0][0])*(b.xyxy[0][3]-b.xyxy[0][1]) for b in g_res.boxes]
                    goal_bbox_detected = g_res.boxes[np.argmax(areas)].xyxy[0].cpu().numpy().tolist()
            except: pass

        # C. Logic m-fixed: Trigger Goal gher mra whda mnin t-dkhol l-kora
        if ball_bbox and goal_bbox_detected:
            bx, by = (ball_bbox[0]+ball_bbox[2])/2, (ball_bbox[1]+ball_bbox[3])/2
            gx1, gy1, gx2, gy2 = goal_bbox_detected
            
            # Check wach l-kora wast l-filet
            if gx1 < bx < gx2 and gy1 < by < gy2:
                if self.goal_cooldown == 0:
                    event_type = "GOAL"
                    self.goal_cooldown = 300  # 10 seconds cooldown
                    sys.stderr.write(f"⚽ REAL GOAL TRIGGERED at frame {frame_num}\n")

        # D. Decrement cooldown bla ma t-beddel event_type
        if self.goal_cooldown > 0:
            self.goal_cooldown -= 1

        team_1_name = self.jersey_recognizer.get_team_name(1) if self.jersey_recognizer else "Equipe 1"
       

        result = {
            "type": "frame_analysis",
            "frame_num": int(frame_num),
            "players_count": len(players_data),
            "team_1_count": team_counts.get(1, 0),
            "team_2_count": team_counts.get(2, 0),
            "team_1_name": team_1_name,
            "team_2_name": team_2_name,
             "event": event_type,
            "referees_count": len(referee_bboxes),
            "ball_detected": ball_bbox is not None,
            "ball_holder_id": ball_holder_id if ball_holder_id != -1 else None,
            "possession": possession,
            "players": players_data,
            "ball": ball_data,
            "goal_bbox": goal_bbox,
            "ocr_enabled": self.enable_ocr
        }

        return result

    def analyze_video(self, video_path, output_video_path=None):
        """Analyze video and stream results as JSON lines, optionally save annotated video"""

        cap = cv2.VideoCapture(video_path)
        if not cap.isOpened():
            output_json({
                "type": "error",
                "message": f"Cannot open video: {video_path}"
            })
            return

        total_frames = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
        fps = cap.get(cv2.CAP_PROP_FPS)
        width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
        height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))

        # Setup video writer if annotation is enabled
        video_writer = None
        if self.enable_annotation and output_video_path:
            fourcc = cv2.VideoWriter_fourcc(*'mp4v')
            video_writer = cv2.VideoWriter(output_video_path, fourcc, fps, (width, height))
            if not video_writer.isOpened():
                output_json({
                    "type": "warning",
                    "message": f"Cannot create output video: {output_video_path}"
                })
                video_writer = None

        # Output video info
        output_json({
            "type": "video_info",
            "path": video_path,
            "output_path": output_video_path if video_writer else None,
            "total_frames": total_frames,
            "fps": fps,
            "width": width,
            "height": height,
            "annotation_enabled": self.enable_annotation and video_writer is not None
        })

        # Output start message
        output_json({
            "type": "analysis_start",
            "message": "Starting video analysis..."
        })

        frame_num = 0
        try:
            while cap.isOpened():
                ret, frame = cap.read()
                if not ret:
                    break

                try:
                    result = self.analyze_frame(frame, frame_num)
                    output_json(result)

                    # Annotate and write frame if enabled
                    if video_writer and self.annotator:
                        annotated_frame = self.annotator.annotate_frame(frame, result)
                        video_writer.write(annotated_frame)

                except Exception as e:
                    output_json({
                        "type": "frame_error",
                        "frame_num": frame_num,
                        "error": str(e)
                    })

                frame_num += 1

                # Progress update every 100 frames
                if frame_num % 100 == 0:
                    output_json({
                        "type": "progress",
                        "frame_num": frame_num,
                        "total_frames": total_frames,
                        "percent": round(frame_num / total_frames * 100, 1)
                    })
        finally:
            if cap:
                cap.release()
            if video_writer:
                video_writer.release()
                if output_video_path and os.path.exists(output_video_path) and os.path.getsize(output_video_path) > 0:
                    self._reencode_video_for_web(output_video_path)

        # Final summary
        total_control = self.team_ball_control[1] + self.team_ball_control[2]
        output_json({
            "type": "analysis_complete",
            "total_frames_processed": frame_num,
            "output_video": output_video_path if video_writer else None,
            "final_possession": {
                "team_1": round(self.team_ball_control[1] / total_control * 100, 1) if total_control > 0 else 50,
                "team_2": round(self.team_ball_control[2] / total_control * 100, 1) if total_control > 0 else 50
            }
        })

    def stream_mjpeg(self, video_path, output_video_path=None, skip_frames=1, target_width=1280, use_sahi=False):
        """Stream annotated frames as MJPEG to stdout for real-time viewing

        Uses chunk-based approach: pre-analyze 5 seconds, then play smoothly, repeat.
        """
        import time

        cap = cv2.VideoCapture(video_path)
        if not cap.isOpened():
            sys.stderr.write(f"Cannot open video: {video_path}\n")
            sys.stderr.flush()
            return

        original_fps = cap.get(cv2.CAP_PROP_FPS)
        original_width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
        original_height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
        total_frames = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))

        # Use original FPS for natural playback speed
        target_fps = original_fps if original_fps > 0 else 25
        frame_delay = 1.0 / target_fps

        # Resize to 720p for better quality and detection
        target_height_max = 720
        if original_height > target_height_max:
            scale = target_height_max / original_height
        else:
            scale = 1.0
        target_width_calc = int(original_width * scale)
        target_height = int(original_height * scale)

        # Chunk settings: 5 seconds of video
        chunk_duration = 5  # seconds
        frames_per_chunk = int(target_fps * chunk_duration)

        sys.stderr.write(f"Streaming: {original_width}x{original_height} -> {target_width_calc}x{target_height}\n")
        sys.stderr.write(f"FPS={target_fps:.1f}, Chunk={chunk_duration}s ({frames_per_chunk} frames)\n")
        sys.stderr.flush()

        # Send video_info via stderr for WebSocket
        video_info = {
            "type": "video_info",
            "path": video_path,
            "width": original_width,
            "height": original_height,
            "fps": original_fps,
            "total_frames": total_frames,
            "annotation_enabled": False
        }
        sys.stderr.write(json.dumps(video_info) + "\n")
        sys.stderr.flush()

        # Set stdout to binary mode on Windows
        if sys.platform == 'win32':
            import msvcrt
            msvcrt.setmode(sys.stdout.fileno(), os.O_BINARY)

        video_writer = None
        if output_video_path:
            fourcc = cv2.VideoWriter_fourcc(*'mp4v')
            video_writer = cv2.VideoWriter(
                output_video_path,
                fourcc,
                target_fps,
                (target_width_calc, target_height)
            )
            if not video_writer.isOpened():
                sys.stderr.write(f"Warning: Could not initialize video writer for {output_video_path}\n")
                sys.stderr.flush()
                video_writer = None
            else:
                sys.stderr.write(f"Saving annotated video to: {output_video_path}\n")
                sys.stderr.flush()

        encode_param = [int(cv2.IMWRITE_JPEG_QUALITY), 70]
        frame_num = 0
        chunk_num = 0
        
   
                
                
        def send_loading_frame(text, progress_pct=0, force_flush=True):
            """Send a loading frame to keep the stream alive"""
            loading_frame = np.zeros((target_height, target_width_calc, 3), dtype=np.uint8)
            loading_frame[:] = (40, 40, 40)  # Dark gray background

            # Add text
            font = cv2.FONT_HERSHEY_SIMPLEX
            cv2.putText(loading_frame, text, (50, target_height // 2 - 30),
                       font, 1.2, (255, 255, 255), 2)

            # Progress bar
            bar_width = target_width_calc - 100
            bar_height = 30
            bar_x = 50
            bar_y = target_height // 2 + 20
            cv2.rectangle(loading_frame, (bar_x, bar_y), (bar_x + bar_width, bar_y + bar_height), (100, 100, 100), -1)
            fill_width = int(bar_width * progress_pct / 100)
            cv2.rectangle(loading_frame, (bar_x, bar_y), (bar_x + fill_width, bar_y + bar_height), (0, 200, 0), -1)

            # Percentage text
            pct_text = f"{progress_pct}%"
            cv2.putText(loading_frame, pct_text, (target_width_calc // 2 - 30, bar_y + 22),
                       font, 0.8, (255, 255, 255), 2)

            success, jpeg_data = cv2.imencode('.jpg', loading_frame, encode_param)
            if success:
                sys.stdout.buffer.write(jpeg_data.tobytes())
                if force_flush:
                    sys.stdout.buffer.flush()

        # Send initial loading frame IMMEDIATELY to keep browser connection alive
        send_loading_frame("Initialisation du stream...", 0)
        sys.stderr.write("[Stream] Sent initial loading frame\n")
        sys.stderr.flush()
        try:
            while cap.isOpened():
                # === PHASE 1: Read all frames first (fast) ===
                chunk_frames = []

                sys.stderr.write(f"[Chunk {chunk_num + 1}] Reading frames...\n")
                sys.stderr.flush()

                for i in range(frames_per_chunk):
                    ret, frame = cap.read()
                    if not ret:
                        break

                    # Resize frame
                    if scale < 1.0:
                        frame = cv2.resize(frame, (target_width_calc, target_height), interpolation=cv2.INTER_LINEAR)

                    chunk_frames.append(frame)

                if not chunk_frames:
                    break  # End of video

                # === PHASE 1b: Analyze frames sequentially with progress ===
                # Analyze every 5th frame for good detection quality
                analyze_interval = 5
                frames_to_analyze = [i for i in range(len(chunk_frames)) if i % analyze_interval == 0]
                total_to_analyze = len(frames_to_analyze)

                sys.stderr.write(f"[Chunk {chunk_num + 1}] Analyzing {total_to_analyze} frames...\n")
                sys.stderr.flush()

                analyzed_results = {}

                # Send initial loading frame
                send_loading_frame(f"Chunk {chunk_num + 1}: 0/{total_to_analyze}", 0)

                for count, i in enumerate(frames_to_analyze):
                    # Analyze this frame
                    result = self.analyze_frame(chunk_frames[i], frame_num + i, fast_mode=False, ultra_fast_mode=True)
                    analyzed_results[i] = result

                    # Send frame_analysis via stderr for WebSocket (real-time timeline updates)
                    if result:
                        frame_data = {
                            "type": "frame_analysis",
                            "frame_num": frame_num + i,
                            "players_count": result.get("players_count", 0),
                            "event": result.get("event"),
                            "team_1_count": result.get("team_1_count", 0),
                            "team_2_count": result.get("team_2_count", 0),
                            "referees_count": result.get("referees_count", 0),
                            "ball_detected": result.get("ball_detected", False),
                            "ball_holder_id": result.get("ball_holder_id"),
                            "possession": result.get("possession", {}),
                            "players": result.get("players", []),
                            "team_1_name": result.get("team_1_name", "Equipe 1"),
                            "team_2_name": result.get("team_2_name", "Equipe 2")
                        }
                        sys.stderr.write(json.dumps(frame_data) + "\n")
                        sys.stderr.flush()

                    # Update progress after each frame
                    progress = int(((count + 1) / total_to_analyze) * 100)
                    send_loading_frame(f"Chunk {chunk_num + 1}: {count + 1}/{total_to_analyze}", progress)

                # === Build final results list ===
                chunk_results = []
                last_result = None

                for i in range(len(chunk_frames)):
                    if i in analyzed_results:
                        last_result = analyzed_results[i]
                    chunk_results.append(last_result)

                sys.stderr.write(f"[Chunk {chunk_num + 1}] Playing {len(chunk_frames)} frames...\n")
                sys.stderr.flush()

                # === PHASE 2: Play chunk smoothly at original FPS ===
                for i, (frame, result) in enumerate(zip(chunk_frames, chunk_results)):
                    play_start = time.time()

                    try:
                        # Annotate frame manually with OpenCV (players + ball)
                        annotated_frame = frame.copy()

                        if result:
                            try:
                                players = result.get("players") or []
                                for player in players:
                                    try:
                                        bbox = player.get("bbox")
                                        if not bbox or len(bbox) < 4:
                                            continue

                                        x1, y1, x2, y2 = [int(v) for v in bbox[:4]]
                                        team_id = player.get("team")
                                        if team_id == 1:
                                            color = (0, 0, 255)  # Red (BGR)
                                        elif team_id == 2:
                                            color = (255, 0, 0)  # Blue (BGR)
                                        else:
                                            color = (200, 200, 200)

                                        cv2.rectangle(annotated_frame, (x1, y1), (x2, y2), color, 2)

                                        label_val = player.get("jersey_number")
                                        if label_val is None or label_val == "":
                                            label_val = player.get("id")
                                        label = str(label_val) if label_val is not None else "player"

                                        text_y = max(y1 - 8, 15)
                                        cv2.putText(
                                            annotated_frame,
                                            label,
                                            (x1, text_y),
                                            cv2.FONT_HERSHEY_SIMPLEX,
                                            0.55,
                                            color,
                                            2,
                                            cv2.LINE_AA,
                                        )
                                    except Exception:
                                        continue
                            except Exception:
                                pass

                            try:
                                ball = result.get("ball")
                                if ball and isinstance(ball, dict):
                                    ball_bbox = ball.get("bbox")
                                    if ball_bbox and len(ball_bbox) >= 4:
                                        bx1, by1, bx2, by2 = [int(v) for v in ball_bbox[:4]]
                                        cv2.rectangle(
                                            annotated_frame,
                                            (bx1, by1),
                                            (bx2, by2),
                                            (0, 255, 255),  # Yellow (BGR)
                                            2,
                                        )
                            except Exception:
                                pass

                        # Save to video file
                        if video_writer:
                            video_writer.write(annotated_frame)

                        # Encode and send to stream
                        success, jpeg_data = cv2.imencode('.jpg', annotated_frame, encode_param)
                        if success:
                            sys.stdout.buffer.write(jpeg_data.tobytes())
                            sys.stdout.buffer.flush()

                        # Control playback speed - maintain original FPS
                        elapsed = time.time() - play_start
                        if elapsed < frame_delay:
                            time.sleep(frame_delay - elapsed)

                    except Exception as e:
                        sys.stderr.write(f"Frame {frame_num + i} error: {e}\n")
                        sys.stderr.flush()

                frame_num += len(chunk_frames)
                chunk_num += 1

                # Clear memory
                chunk_frames.clear()
                chunk_results.clear()
        finally:
            if cap:
                cap.release()
            if video_writer:
                video_writer.release()
                if output_video_path and os.path.exists(output_video_path) and os.path.getsize(output_video_path) > 0:
                    self._reencode_video_for_web(output_video_path)

        # Close video writer and notify
        if video_writer:
            sys.stderr.write(f"Annotated video saved: {output_video_path}\n")
            sys.stderr.flush()

            # Check if file exists and has content
            if os.path.exists(output_video_path) and os.path.getsize(output_video_path) > 0:
                video_filename = os.path.basename(output_video_path)
                sys.stderr.write(f"ANNOTATED_VIDEO_READY:{video_filename}\n")
                sys.stderr.flush()
            else:
                sys.stderr.write(f"Warning: Annotated video file is empty or not created\n")
                sys.stderr.flush()
        elif output_video_path:
            sys.stderr.write(f"Warning: Video writer failed to initialize for {output_video_path}\n")
            sys.stderr.flush()

        total_ctrl = self.team_ball_control[1] + self.team_ball_control[2]
        final_possession = {
                    "1": round(self.team_ball_control[1] / total_ctrl * 100, 1) if total_ctrl > 0 else 50,
                    "2": round(self.team_ball_control[2] / total_ctrl * 100, 1) if total_ctrl > 0 else 50
                }
        # Send analysis_complete via stderr for WebSocket
        complete_data = {
            "type": "analysis_complete",
            "total_frames_processed": frame_num,
            "chunks_processed": chunk_num,
            "final_possession": final_possession
        }
        sys.stderr.write(json.dumps(complete_data) + "\n")
        sys.stderr.flush()

        sys.stderr.write(f"Stream complete. {frame_num} frames in {chunk_num} chunks\n")
        sys.stderr.flush()

    def _predict_positions(self, prev_result, curr_result, frames_ahead):
        """Predict positions forward based on velocity between last two detections"""
        if not prev_result or not curr_result or frames_ahead <= 0:
            return curr_result

        result = curr_result.copy()

        # Predict player positions
        if 'players' in result and 'players' in prev_result:
            prev_players = {p['id']: p for p in prev_result['players']}
            new_players = []

            for player in result['players']:
                pid = player['id']
                player = player.copy()

                if pid in prev_players:
                    prev_bbox = prev_players[pid]['bbox']
                    curr_bbox = player['bbox']

                    # Calculate velocity and predict forward
                    pred_bbox = []
                    for i in range(4):
                        velocity = curr_bbox[i] - prev_bbox[i]
                        pred_bbox.append(curr_bbox[i] + velocity * frames_ahead * 0.3)
                    player['bbox'] = pred_bbox

                new_players.append(player)

            result['players'] = new_players

        # Predict ball position
        if result.get('ball') and prev_result.get('ball'):
            prev_ball = prev_result['ball']['bbox']
            curr_ball = result['ball']['bbox']

            pred_ball = []
            for i in range(4):
                velocity = curr_ball[i] - prev_ball[i]
                pred_ball.append(curr_ball[i] + velocity * frames_ahead * 0.3)

            result['ball'] = result['ball'].copy()
            result['ball']['bbox'] = pred_ball

        return result

    def _interpolate_result(self, prev_result, curr_result, frames_elapsed, interval):
        """Interpolate player/ball positions between AI detections for smooth tracking"""
        if not prev_result or not curr_result:
            return curr_result

        # Calculate interpolation factor (0 to 1)
        t = min(frames_elapsed / interval, 1.0)

        result = curr_result.copy()

        # Interpolate player positions
        if 'players' in result and 'players' in prev_result:
            prev_players = {p['id']: p for p in prev_result['players']}
            new_players = []

            for player in result['players']:
                pid = player['id']
                if pid in prev_players:
                    prev_bbox = prev_players[pid]['bbox']
                    curr_bbox = player['bbox']

                    # Linear interpolation of bbox
                    interp_bbox = [
                        prev_bbox[i] + (curr_bbox[i] - prev_bbox[i]) * t
                        for i in range(4)
                    ]
                    player = player.copy()
                    player['bbox'] = interp_bbox
                new_players.append(player)

            result['players'] = new_players

        # Interpolate ball position
        if result.get('ball') and prev_result.get('ball'):
            prev_ball = prev_result['ball']['bbox']
            curr_ball = result['ball']['bbox']

            interp_ball = [
                prev_ball[i] + (curr_ball[i] - prev_ball[i]) * t
                for i in range(4)
            ]
            result['ball'] = result['ball'].copy()
            result['ball']['bbox'] = interp_ball

        return result
    
    def _detect_goal_event(self, frame, ball_bbox, frame_num):
        # 1. Decrement Cooldown
        if self.goal_cooldown > 0:
            self.goal_cooldown -= 1
            return None, None

        # 2. Safety Checks
        if not self.goal_model or ball_bbox is None:
            self.ball_in_goal_frames = 0 # Reset ila khrjat l-kora
            return None, None

        try:
            # 3. Detect Goal Net (kull 30 frames bach n-ser3o s-system)
            if frame_num % 30 == 0 or not self.goal_regions:
                # Tla3na conf l-0.45 bach may-t-ghletch f l-joumhour
                results = self.goal_model.predict(frame, conf=0.45, verbose=False)[0]
                if len(results.boxes) > 0:
                    self.goal_regions = [box.xyxy[0].cpu().numpy().tolist() for box in results.boxes]

            # 4. Check Intersection b-Confirmation Buffer
            bx, by = (ball_bbox[0] + ball_bbox[2]) / 2, (ball_bbox[1] + ball_bbox[3]) / 2
            
            for region in self.goal_regions:
                gx1, gy1, gx2, gy2 = region
                
                # Zid margin kbir l-dakhel (20%) bach n-t-fadao l-qayem (Post)
                margin_w = (gx2 - gx1) * 0.20
                margin_h = (gy2 - gy1) * 0.15
                
                # Condition: Ball must be well inside the net boundaries
                if (gx1 + margin_w) < bx < (gx2 - margin_w) and (gy1 + margin_h) < by < gy2:
                    self.ball_in_goal_frames += 1 # Zid f counter
                    
                    # Check confirmation (khass 3 frames mutataliya)
                    if self.ball_in_goal_frames >= 3:
                        self.goal_cooldown = 300 # 10 seconds cooldown
                        self.ball_in_goal_frames = 0
                        sys.stderr.write(f"⚽ GOAL CONFIRMED at frame {frame_num}\n")
                        return "GOAL", region
                else:
                    self.ball_in_goal_frames = 0 # Reset ila dazt gher hda l-qayem
                    
            return None, None
        except Exception:
            return None, None
       
    def _load_goal_model(self, model_path):
        try:
            base_dir = os.path.dirname(os.path.abspath(__file__)) if '__file__' in locals() else os.getcwd()
            possible_paths = [
                os.path.join(base_dir, 'models', 'goal_model.pt'),
                os.path.join(os.path.dirname(model_path), 'goal_model.pt'),
                'models/goal_model.pt',
                'goal_model.pt'
            ]
            
            goal_path = next((p for p in possible_paths if os.path.exists(p)), None)
            
            if goal_path:
                from ultralytics import YOLO
                goal_model = YOLO(goal_path)
                if self.device.lower() == 'cuda':
                    goal_model.to(self.device.lower())
                    if hasattr(self, 'use_half') and self.use_half:
                        try: goal_model.model.half()
                        except: pass
                output_json({"type": "info", "message": f"Goal detection model loaded: {goal_path}"})
                return goal_model
            else:
                output_json({"type": "warning", "message": "goal_model.pt not found. Goal detection disabled."})
                return None
        except Exception as e:
            output_json({"type": "warning", "message": f"Failed to load goal model: {e}"})
            return None

    def _reencode_video_for_web(self, output_video_path):
        """Re-encode an MP4 to H.264/yuv420p for browser compatibility."""
        if not output_video_path:
            return

        base, ext = os.path.splitext(output_video_path)
        temp_file = f"{base}_web{ext if ext else '.mp4'}"

        ffmpeg_cmd = [
            'ffmpeg', '-y', '-i', output_video_path,
            '-vcodec', 'libx264',
            '-crf', '28',
            '-pix_fmt', 'yuv420p',
            '-preset', 'fast',
            temp_file
        ]

        try:
            subprocess.run(
                ffmpeg_cmd,
                check=True,
                stdout=subprocess.DEVNULL,
                stderr=subprocess.STDOUT
            )
            os.replace(temp_file, output_video_path)
            sys.stderr.write(f"[FFmpeg] Video re-encoded for web: {output_video_path}\n")
            sys.stderr.flush()
        except Exception as e:
            if os.path.exists(temp_file):
                try:
                    os.remove(temp_file)
                except Exception:
                    pass
            sys.stderr.write(f"[FFmpeg] Re-encode skipped/failed: {e}\n")
            sys.stderr.flush()