
import cv2
class VideoAnnotator:
    """Draws bounding boxes, player info, and stats on video frames"""

    DEFAULT_TEAM_COLORS = {
        1: (0, 0, 255),      # Red for team 1
        2: (255, 0, 0),      # Blue for team 2
        0: (128, 128, 128)   # Gray for unknown
    }

    BALL_COLOR = (0, 255, 255)       # Yellow for ball
    BALL_HOLDER_COLOR = (0, 255, 0)  # Green for ball holder

    def __init__(self, team_colors=None):
        self.font = cv2.FONT_HERSHEY_SIMPLEX
        self.font_scale = 0.5
        self.thickness = 2
        # Use custom team colors if provided
        if team_colors:
            self.TEAM_COLORS = {
                1: tuple(team_colors.get(1, self.DEFAULT_TEAM_COLORS[1])),
                2: tuple(team_colors.get(2, self.DEFAULT_TEAM_COLORS[2])),
                0: self.DEFAULT_TEAM_COLORS[0]
            }
        else:
            self.TEAM_COLORS = self.DEFAULT_TEAM_COLORS.copy()

    def draw_player_box(self, frame, bbox, player_data, is_ball_holder=False):
        """Draw bounding box and info for a player"""
        x1, y1, x2, y2 = int(bbox[0]), int(bbox[1]), int(bbox[2]), int(bbox[3])

        team_id = player_data.get("team", 0)
        player_id = player_data.get("id", 0)
        jersey_number = player_data.get("jersey_number")
        player_name = player_data.get("player_name")
        speed = player_data.get("speed_kmh", 0)

        # Choose color based on team and ball possession
        if is_ball_holder:
            color = self.BALL_HOLDER_COLOR
            box_thickness = 3
        else:
            color = self.TEAM_COLORS.get(team_id, self.TEAM_COLORS[0])
            box_thickness = 2

        # Draw bounding box
        cv2.rectangle(frame, (x1, y1), (x2, y2), color, box_thickness)

        # Build label text
        if player_name and jersey_number:
            label = f"#{jersey_number} {player_name}"
        elif jersey_number:
            label = f"#{jersey_number}"
        else:
            label = f"ID:{player_id}"

        # Add speed if available
        if speed > 0:
            label += f" | {speed:.1f}km/h"

        # Draw label background
        (text_width, text_height), baseline = cv2.getTextSize(label, self.font, self.font_scale, 1)

        # Position label above the box
        label_y = y1 - 10 if y1 > 30 else y2 + 20
        label_x = x1

        # Background rectangle for text
        cv2.rectangle(frame,
                      (label_x, label_y - text_height - 5),
                      (label_x + text_width + 10, label_y + 5),
                      color, -1)

        # Draw text (white on colored background)
        cv2.putText(frame, label, (label_x + 5, label_y - 2),
                    self.font, self.font_scale, (255, 255, 255), 1, cv2.LINE_AA)

        # Draw ball holder indicator
        if is_ball_holder:
            # Draw a small ball icon next to player
            ball_icon_x = x2 + 5
            ball_icon_y = (y1 + y2) // 2
            cv2.circle(frame, (ball_icon_x, ball_icon_y), 8, self.BALL_COLOR, -1)
            cv2.circle(frame, (ball_icon_x, ball_icon_y), 8, (0, 0, 0), 1)

        return frame

    def draw_ball(self, frame, ball_data):
        """Draw circle around the ball"""
        if not ball_data:
            return frame

        bbox = ball_data.get("bbox")
        if not bbox:
            return frame

        x1, y1, x2, y2 = int(bbox[0]), int(bbox[1]), int(bbox[2]), int(bbox[3])
        center_x = (x1 + x2) // 2
        center_y = (y1 + y2) // 2
        radius = max((x2 - x1) // 2, (y2 - y1) // 2) + 5

        # Draw circle around ball
        cv2.circle(frame, (center_x, center_y), radius, self.BALL_COLOR, 2)
        cv2.circle(frame, (center_x, center_y), 3, self.BALL_COLOR, -1)

        return frame

    def draw_stats_overlay(self, frame, analysis_data):
        """Draw stats overlay in corner of frame"""
        height, width = frame.shape[:2]

        # Stats panel dimensions
        panel_width = 300
        panel_height = 120
        margin = 10

        # Create semi-transparent overlay
        overlay = frame.copy()
        cv2.rectangle(overlay,
                      (margin, margin),
                      (margin + panel_width, margin + panel_height),
                      (0, 0, 0), -1)
        cv2.addWeighted(overlay, 0.7, frame, 0.3, 0, frame)

        # Draw stats text
        y_offset = margin + 25
        line_height = 22

        # Frame number
        frame_num = analysis_data.get("frame_num", 0)
        cv2.putText(frame, f"Frame: {frame_num}", (margin + 10, y_offset),
                    self.font, 0.6, (255, 255, 255), 1, cv2.LINE_AA)
        y_offset += line_height

        # Player counts
        team_1_count = analysis_data.get("team_1_count", 0)
        team_2_count = analysis_data.get("team_2_count", 0)
        team_1_name = analysis_data.get("team_1_name", "Equipe 1")
        team_2_name = analysis_data.get("team_2_name", "Equipe 2")

        cv2.putText(frame, f"{team_1_name}: {team_1_count}", (margin + 10, y_offset),
                    self.font, 0.5, self.TEAM_COLORS[1], 1, cv2.LINE_AA)
        cv2.putText(frame, f"{team_2_name}: {team_2_count}", (margin + 150, y_offset),
                    self.font, 0.5, self.TEAM_COLORS[2], 1, cv2.LINE_AA)
        y_offset += line_height

        # Possession
        possession = analysis_data.get("possession", {})
        poss_1 = possession.get(1, possession.get("1", 50))
        poss_2 = possession.get(2, possession.get("2", 50))

        cv2.putText(frame, f"Possession:", (margin + 10, y_offset),
                    self.font, 0.5, (255, 255, 255), 1, cv2.LINE_AA)
        y_offset += line_height

        # Possession bar
        bar_x = margin + 10
        bar_width = panel_width - 20
        bar_height = 15

        team1_width = int(bar_width * poss_1 / 100)

        cv2.rectangle(frame, (bar_x, y_offset - 10),
                      (bar_x + team1_width, y_offset + 5), self.TEAM_COLORS[1], -1)
        cv2.rectangle(frame, (bar_x + team1_width, y_offset - 10),
                      (bar_x + bar_width, y_offset + 5), self.TEAM_COLORS[2], -1)

        # Possession percentages on bar
        cv2.putText(frame, f"{poss_1:.0f}%", (bar_x + 5, y_offset + 2),
                    self.font, 0.4, (255, 255, 255), 1, cv2.LINE_AA)
        cv2.putText(frame, f"{poss_2:.0f}%", (bar_x + bar_width - 35, y_offset + 2),
                    self.font, 0.4, (255, 255, 255), 1, cv2.LINE_AA)

        return frame

    def draw_ball_holder_info(self, frame, ball_holder_data):
        """Draw info about current ball holder at bottom of screen"""
        if not ball_holder_data:
            return frame

        height, width = frame.shape[:2]

        # Panel at bottom center
        panel_width = 350
        panel_height = 50
        panel_x = (width - panel_width) // 2
        panel_y = height - panel_height - 10

        # Semi-transparent background
        overlay = frame.copy()
        cv2.rectangle(overlay, (panel_x, panel_y),
                      (panel_x + panel_width, panel_y + panel_height),
                      self.BALL_HOLDER_COLOR, -1)
        cv2.addWeighted(overlay, 0.8, frame, 0.2, 0, frame)

        # Ball holder info
        jersey = ball_holder_data.get("jersey_number", "")
        name = ball_holder_data.get("player_name", "")
        team = ball_holder_data.get("team", 0)
        speed = ball_holder_data.get("speed_kmh", 0)

        if name:
            holder_text = f"BALLON: #{jersey} {name}"
        elif jersey:
            holder_text = f"BALLON: #{jersey}"
        else:
            holder_text = f"BALLON: Joueur ID {ball_holder_data.get('id', '?')}"

        speed_text = f"{speed:.1f} km/h"

        cv2.putText(frame, holder_text, (panel_x + 10, panel_y + 22),
                    self.font, 0.6, (0, 0, 0), 2, cv2.LINE_AA)
        cv2.putText(frame, speed_text, (panel_x + 10, panel_y + 42),
                    self.font, 0.5, (0, 0, 0), 1, cv2.LINE_AA)

        # Ball icon
        cv2.circle(frame, (panel_x + panel_width - 30, panel_y + 25), 15, self.BALL_COLOR, -1)
        cv2.circle(frame, (panel_x + panel_width - 30, panel_y + 25), 15, (0, 0, 0), 2)

        return frame

    def annotate_frame(self, frame, analysis_data):
        """Main method to annotate a frame with all visual elements"""
        annotated = frame.copy()

        # Get ball holder ID
        ball_holder_id = analysis_data.get("ball_holder_id")
        ball_holder_data = None

        # Draw all players
        players = analysis_data.get("players", [])
        for player in players:
            is_holder = player.get("id") == ball_holder_id
            if is_holder:
                ball_holder_data = player
            bbox = player.get("bbox", [])
            if bbox:
                annotated = self.draw_player_box(annotated, bbox, player, is_holder)

        # Draw ball
        ball_data = analysis_data.get("ball")
        annotated = self.draw_ball(annotated, ball_data)

        # Draw stats overlay
        annotated = self.draw_stats_overlay(annotated, analysis_data)

        # Draw ball holder info
        if ball_holder_data:
            annotated = self.draw_ball_holder_info(annotated, ball_holder_data)

        return annotated
        