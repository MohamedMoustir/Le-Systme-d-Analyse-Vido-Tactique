import cv2
import numpy as np
from utils import measure_distance

class ViewTransformer:
    def __init__(self, pixel_vertices=None):
        self.court_width = 68
        self.court_length = 23.32

        # Default pixel vertices - should be calibrated per video
        if pixel_vertices is None:
            self.pixel_vertices = np.array([
                [110, 1035], [265, 275], [910, 260], [1640, 915]
            ], dtype=np.float32)
        else:
            self.pixel_vertices = np.array(pixel_vertices, dtype=np.float32)

        self.target_vertices = np.array([
            [0, self.court_width],
            [0, 0],
            [self.court_length, 0],
            [self.court_length, self.court_width]
        ], dtype=np.float32)

        self.perspective_transformer = cv2.getPerspectiveTransform(
            self.pixel_vertices, self.target_vertices
        )

    def transform_point(self, point):
        p = (int(point[0]), int(point[1]))
        is_inside = cv2.pointPolygonTest(self.pixel_vertices, p, False) >= 0
        if not is_inside:
            return None

        reshaped_point = np.array(point, dtype=np.float32).reshape(-1, 1, 2)
        transformed = cv2.perspectiveTransform(reshaped_point, self.perspective_transformer)
        return transformed.reshape(-1, 2)


class SpeedDistanceEstimator:
    def __init__(self, frame_rate=24, frame_window=5):
        self.frame_rate = frame_rate
        self.frame_window = frame_window
        self.total_distance = {}
        self.speed_history = {}

    def calculate_speed_distance(self, track_id, position_transformed, frame_num):
        if position_transformed is None:
            return None, None

        if track_id not in self.speed_history:
            self.speed_history[track_id] = []
            self.total_distance[track_id] = 0

        self.speed_history[track_id].append({
            'frame': frame_num,
            'position': position_transformed
        })

        # Keep only last frame_window entries
        if len(self.speed_history[track_id]) > self.frame_window:
            self.speed_history[track_id] = self.speed_history[track_id][-self.frame_window:]

        if len(self.speed_history[track_id]) >= 2:
            start = self.speed_history[track_id][0]
            end = self.speed_history[track_id][-1]

            distance = measure_distance(start['position'], end['position'])
            time_elapsed = (end['frame'] - start['frame']) / self.frame_rate

            if time_elapsed > 0:
                speed_mps = distance / time_elapsed
                speed_kmh = speed_mps * 3.6
                self.total_distance[track_id] += distance / len(self.speed_history[track_id])
                return speed_kmh, self.total_distance[track_id]

        return 0, self.total_distance.get(track_id, 0)
