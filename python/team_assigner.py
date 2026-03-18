


import sys
from utils import parse_color_string, get_center_of_bbox, output_json
import json
import numpy as np
import sys
import cv2
import os
from sklearn.cluster import KMeans


class TeamAssigner:
    def __init__(self, teams_config=None):
        self.team_colors = {}
        self.player_team_dict = {}
        self.kmeans = None
        self.use_configured_colors = False
        self.configured_colors = {}
        self.display_colors = {}
        self.color_tolerance = 80
        self.ball_roi = None  
        self.roi_margin = 150
        # Load configured colors if available
        if teams_config:
            self._load_configured_colors(teams_config)

    def _load_configured_colors(self, config):
        """Load team colors from configuration"""
        if config.get('use_configured_colors', False):
            self.use_configured_colors = True
            self.color_tolerance = config.get('color_tolerance', 80)

            # Parse team 1 color
            if 'team_1' in config and 'color' in config['team_1']:
                color_str = config['team_1']['color']
                rgb = parse_color_string(color_str)
                # BGR format for OpenCV (reverse RGB)
                self.configured_colors[1] = np.array([rgb[2], rgb[1], rgb[0]])
                self.team_colors[1] = [int(rgb[2]), int(rgb[1]), int(rgb[0])]
                # Generate display color (slightly different for visibility)
                self.display_colors[1] = [0, 0, 255]  # Red for team 1
                sys.stderr.write(f"[TeamAssigner] Team 1 color '{color_str}' -> RGB{list(rgb)} -> BGR{self.team_colors[1]}\n")

            # Parse team 2 color
            if 'team_2' in config and 'color' in config['team_2']:
                color_str = config['team_2']['color']
                rgb = parse_color_string(color_str)
                self.configured_colors[2] = np.array([rgb[2], rgb[1], rgb[0]])
                self.team_colors[2] = [int(rgb[2]), int(rgb[1]), int(rgb[0])]
                self.display_colors[2] = [255, 0, 0]  # Blue for team 2
                sys.stderr.write(f"[TeamAssigner] Team 2 color '{color_str}' -> RGB{list(rgb)} -> BGR{self.team_colors[2]}\n")

            if self.configured_colors:
                sys.stderr.write(f"[TeamAssigner] Using configured colors - Team1: {self.team_colors.get(1)}, Team2: {self.team_colors.get(2)}\n")
                sys.stderr.flush()

    def get_clustering_model(self, image):
        image_2D = image.reshape(-1, 3)
        kmeans = KMeans(n_clusters=2, init="k-means++", n_init=1, random_state=42)
        kmeans.fit(image_2D)
        return kmeans

    def get_player_color(self, frame, bbox):
        x1, y1, x2, y2 = int(bbox[0]), int(bbox[1]), int(bbox[2]), int(bbox[3])
        image = frame[y1:y2, x1:x2]
        if image.size == 0:
            return np.array([0, 0, 0])

        top_half_image = image[0:int(image.shape[0] / 2), :]
        if top_half_image.size == 0:
            return np.array([0, 0, 0])
    
        top_half_image = image[0:int(image.shape[0] / 2), :]
        if top_half_image.size == 0:
            return np.array([0, 0, 0])
        kmeans = self.get_clustering_model(top_half_image)
        labels = kmeans.labels_
        clustered_image = labels.reshape(top_half_image.shape[0], top_half_image.shape[1])

        corner_clusters = [clustered_image[0, 0], clustered_image[0, -1],
                          clustered_image[-1, 0], clustered_image[-1, -1]]
        non_player_cluster = max(set(corner_clusters), key=corner_clusters.count)
        player_cluster = 1 - non_player_cluster

        player_color = kmeans.cluster_centers_[player_cluster]
        return player_color

    def assign_team_color(self, frame, player_detections):
        # Skip auto-detection if using configured colors
        if self.use_configured_colors and len(self.configured_colors) == 2:
            return

        player_colors = []
        for bbox in player_detections:
            player_color = self.get_player_color(frame, bbox)
            player_colors.append(player_color)

        if len(player_colors) >= 2:
            kmeans = KMeans(n_clusters=2, init="k-means++", n_init=10, random_state=42)
            kmeans.fit(player_colors)
            self.kmeans = kmeans
            self.team_colors[1] = kmeans.cluster_centers_[0].tolist()
            self.team_colors[2] = kmeans.cluster_centers_[1].tolist()

    def get_player_team(self, frame, player_bbox, player_id):
        if player_id in self.player_team_dict:
            return self.player_team_dict[player_id]

        player_color = self.get_player_color(frame, player_bbox)

        # Use configured colors if available
        if self.use_configured_colors and len(self.configured_colors) == 2:
            dist1 = np.linalg.norm(player_color - self.configured_colors[1])
            dist2 = np.linalg.norm(player_color - self.configured_colors[2])
            team_id = 1 if dist1 < dist2 else 2
            self.player_team_dict[player_id] = team_id
            return team_id

        # Fallback to kmeans
        if self.kmeans is None:
            return 1

        team_id = self.kmeans.predict(player_color.reshape(1, -1))[0] + 1
        self.player_team_dict[player_id] = team_id
        return team_id

    def get_display_color(self, team_id):
        """Get the display color for a team (for drawing on frame)"""
        if team_id in self.display_colors:
            return tuple(self.display_colors[team_id])
        # Default colors
        return (255, 100, 100) if team_id == 1 else (100, 100, 255)
