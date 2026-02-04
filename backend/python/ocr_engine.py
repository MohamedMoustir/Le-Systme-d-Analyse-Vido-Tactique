import cv2
import numpy as np
import re
import sys
import os
import json
from config import EASYOCR_AVAILABLE, PADDLEOCR_AVAILABLE
from utils import get_center_of_bbox
class JerseyNumberRecognizer:
    def __init__(self, teams_config_path=None):
        self.ocr_reader = None
        self.paddle_ocr = None
        self.use_paddle = False
        self.teams_config = {}
        self.player_jersey_cache = {}  # track_id -> jersey_number
        self.jersey_confidence = {}    # track_id -> confidence count

        # Initialize PaddleOCR (preferred) or EasyOCR (fallback)
        if PADDLEOCR_AVAILABLE:
            try:
                sys.stderr.write("[JerseyRecognizer] Initializing PaddleOCR v3...\n")
                sys.stderr.flush()
                # PaddleOCR 3.x with optimized settings for jersey numbers
                # Note: API changed in v3.x - many parameters are now deprecated
                self.paddle_ocr = PaddleOCR(
                    lang='en',
                    use_doc_orientation_classify=False,  # Skip document orientation
                    use_doc_unwarping=False,  # Skip document unwarping
                    use_textline_orientation=False,  # Skip textline orientation for speed
                    text_det_thresh=0.2,  # Lower threshold for detecting small numbers
                    text_det_box_thresh=0.3,
                    text_rec_score_thresh=0.1,  # Lower threshold to catch more numbers
                )
                self.use_paddle = True
                sys.stderr.write("[JerseyRecognizer] PaddleOCR v3 initialized successfully (more accurate)\n")
                sys.stderr.flush()
            except Exception as e:
                self.paddle_ocr = None
                sys.stderr.write(f"[JerseyRecognizer] Failed to initialize PaddleOCR: {type(e).__name__}: {e}\n")
                sys.stderr.flush()
                import traceback
                traceback.print_exc(file=sys.stderr)
                sys.stderr.flush()

        # Fallback to EasyOCR if PaddleOCR not available
        if not self.use_paddle and EASYOCR_AVAILABLE:
            try:
                self.ocr_reader = easyocr.Reader(['en'], gpu=False, verbose=False)
                sys.stderr.write("[JerseyRecognizer] EasyOCR initialized as fallback\n")
            except Exception as e:
                self.ocr_reader = None
                sys.stderr.write(f"[JerseyRecognizer] Failed to initialize EasyOCR: {e}\n")

        if not self.use_paddle and not self.ocr_reader:
            sys.stderr.write("[JerseyRecognizer] No OCR available - jersey recognition disabled\n")

        # Load teams configuration
        if teams_config_path and os.path.exists(teams_config_path):
            try:
                with open(teams_config_path, 'r', encoding='utf-8') as f:
                    self.teams_config = json.load(f)
                team1_name = self.teams_config.get('team_1', {}).get('name', 'Team 1')
                team2_name = self.teams_config.get('team_2', {}).get('name', 'Team 2')
                team1_players = len(self.teams_config.get('team_1', {}).get('players', {}))
                team2_players = len(self.teams_config.get('team_2', {}).get('players', {}))
                sys.stderr.write(f"[JerseyRecognizer] Loaded teams config: {team1_name} ({team1_players} players) vs {team2_name} ({team2_players} players)\n")
            except Exception as e:
                sys.stderr.write(f"[JerseyRecognizer] Failed to load teams config: {e}\n")

    def extract_jersey_regions(self, frame, bbox):
        """Extract multiple regions where jersey number might be visible"""
        x1, y1, x2, y2 = int(bbox[0]), int(bbox[1]), int(bbox[2]), int(bbox[3])

        # Get player region
        player_img = frame[y1:y2, x1:x2]
        if player_img.size == 0:
            return []

        h, w = player_img.shape[:2]
        if h < 30 or w < 20:
            return []

        regions = []

        # Region 1: Upper body center (back number)
        margin_x = int(w * 0.15)
        jersey_region = player_img[int(h*0.1):int(h*0.55), margin_x:w-margin_x]
        if jersey_region.size > 0 and jersey_region.shape[0] >= 15 and jersey_region.shape[1] >= 10:
            regions.append(jersey_region)

        # Region 2: Full upper body (for larger numbers)
        jersey_region2 = player_img[int(h*0.05):int(h*0.6), int(w*0.1):int(w*0.9)]
        if jersey_region2.size > 0 and jersey_region2.shape[0] >= 15 and jersey_region2.shape[1] >= 10:
            regions.append(jersey_region2)

        # Region 3: Chest area (front number, smaller)
        jersey_region3 = player_img[int(h*0.15):int(h*0.45), int(w*0.2):int(w*0.8)]
        if jersey_region3.size > 0 and jersey_region3.shape[0] >= 10 and jersey_region3.shape[1] >= 8:
            regions.append(jersey_region3)

        shorts_region = player_img[int(h*0.6):int(h*0.85), int(w*0.1):int(w*0.9)]
        if shorts_region.size > 0 and shorts_region.shape[0] >= 12:
            regions.append(shorts_region)
            
        return regions

    def extract_jersey_region(self, frame, bbox):
        """Extract the back/chest region where jersey number is likely visible (legacy)"""
        regions = self.extract_jersey_regions(frame, bbox)
        return regions[0] if regions else None

    def preprocess_for_ocr(self, image):
       
        if image is None or image.size == 0:
            return image

        h, w = image.shape[:2]
        image = cv2.resize(image, (w*3, h*3), interpolation=cv2.INTER_CUBIC)

    
        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

        # 3. CLAHE (Contrast Enhancement)
        clahe = cv2.createCLAHE(clipLimit=3.0, tileGridSize=(8, 8))
        enhanced = clahe.apply(gray)

        # 4. Denoising (Bach n-7iydo l-mouchwichat sghar)
        denoised = cv2.fastNlMeansDenoising(enhanced, None, 10, 7, 21)

        # 5. Adaptive Thresholding (K-y-rdd l-image B&W 3la hsab l-lighting)
        binary = cv2.adaptiveThreshold(
            denoised, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, 
            cv2.THRESH_BINARY, 11, 2
        )

        return binary

    def _run_paddleocr(self, image):
        """Run PaddleOCR on an image and return detected numbers with confidence"""
        results = []
        try:
            # PaddleOCR 3.x uses predict() method
            ocr_result = self.paddle_ocr.predict(image)
            if ocr_result and len(ocr_result) > 0:
                result = ocr_result[0]  # First result
                texts = result.get('rec_texts', [])
                scores = result.get('rec_scores', [])

                for text, confidence in zip(texts, scores):
                    # Clean the text - extract only digits
                    number_str = re.sub(r'[^0-9]', '', str(text))
                    if number_str:
                        results.append((number_str, float(confidence)))
        except Exception as e:
            pass  # PaddleOCR 3.x should use predict(), no fallback needed
        return results

    def _run_easyocr(self, image):
        """Run EasyOCR on an image and return detected numbers with confidence"""
        results = []
        try:
            ocr_result = self.ocr_reader.readtext(image, allowlist='0123456789')
            for (_, text, confidence) in ocr_result:
                number_str = re.sub(r'[^0-9]', '', text)
                if number_str:
                    results.append((number_str, confidence))
        except Exception:
            pass
        return results

    def recognize_number(self, frame, bbox, track_id):
        """
        Recognize jersey number from player bounding box (Jersey + Shorts).
        Optimized to check multiple regions with early exit on high confidence.
        """
        # 1. Check Cache: Ila 3ndna raqm thabet (Conf >= 3), bla ma n-3ayto l-OCR bach n-rebho l-waqt
        if track_id in self.player_jersey_cache:
            if self.jersey_confidence.get(track_id, 0) >= 3:
                return self.player_jersey_cache[track_id]

        if not self.use_paddle and self.ocr_reader is None:
            return None

        # 2. Extract All Regions (Jersey Back, Chest, and Shorts)
        jersey_regions = self.extract_jersey_regions(frame, bbox)
        if not jersey_regions:
            return self.player_jersey_cache.get(track_id)

        best_number = None
        best_confidence = 0

        # 3. Loop over each region (Scanning for number)
        for region in jersey_regions:
            try:
                # Try OCR on raw region first
                if self.use_paddle:
                    ocr_results = self._run_paddleocr(region)
                else:
                    ocr_results = self._run_easyocr(region)

                # Process results
                for (number_str, confidence) in ocr_results:
                    if number_str.isdigit():
                        num = int(number_str)
                        if 1 <= num <= 99 and confidence > 0.4:
                            best_number = num
                            best_confidence = confidence
                            break # Found a valid number in this region

                # If no number found, try Pre-processing (Binary/CLAHE)
                if best_number is None:
                    processed = self.preprocess_for_ocr(region)
                    ocr_results = self._run_paddleocr(processed) if self.use_paddle else self._run_easyocr(processed)
                    
                    for (number_str, confidence) in ocr_results:
                        if number_str.isdigit():
                            num = int(number_str)
                            if 1 <= num <= 99 and confidence > 0.4:
                                best_number = num
                                best_confidence = confidence
                                break

                # Early Exit: Ila lqina numra b confidence tal3a, bla ma n-kemlo l-ga3 l-manatiq l-okhrin
                if best_number is not None and best_confidence > 0.6:
                    break

            except Exception as e:
                continue

        # 4. Update Cache and Voting Logic
        if best_number is not None:
            ocr_type = "PaddleOCR" if self.use_paddle else "EasyOCR"
            
            if track_id not in self.player_jersey_cache:
                self.player_jersey_cache[track_id] = best_number
                self.jersey_confidence[track_id] = 1
            elif self.player_jersey_cache[track_id] == best_number:
                # Same number: Increase confidence
                self.jersey_confidence[track_id] += 1
            else:
                # Different number: Decrease confidence before switching
                self.jersey_confidence[track_id] -= 1
                if self.jersey_confidence[track_id] <= 0:
                    self.player_jersey_cache[track_id] = best_number
                    self.jersey_confidence[track_id] = 1
            
            # Log discovery
            if self.jersey_confidence[track_id] == 1:
                sys.stderr.write(f"[{ocr_type}] Player {track_id} assigned Jersey #{best_number}\n")
            
            return best_number

        return self.player_jersey_cache.get(track_id)
    
    def get_player_name(self, team_id, jersey_number):
        """Get player name from teams configuration"""
        if not jersey_number or not self.teams_config:
            return None

        team_key = f"team_{team_id}"
        if team_key in self.teams_config:
            players = self.teams_config[team_key].get("players", {})
            return players.get(str(jersey_number))

        return None

    def get_team_name(self, team_id):
        """Get team name from configuration"""
        team_key = f"team_{team_id}"
        if team_key in self.teams_config:
            return self.teams_config[team_key].get("name", f"Equipe {team_id}")
        return f"Equipe {team_id}"

