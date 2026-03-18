import numpy as np
import json
import sys
# ============== UTILITY FUNCTIONS ==============

# When True, output_json writes to stderr so stdout stays binary-clean for MJPEG
_streaming_mode = False

def set_streaming_mode(enabled: bool):
    global _streaming_mode
    _streaming_mode = enabled

NAMED_COLORS = {
    'white': [255, 255, 255],
    'black': [0, 0, 0],
    'red': [255, 0, 0],
    'green': [0, 255, 0],
    'blue': [0, 0, 255],
    'yellow': [255, 255, 0],
    'cyan': [0, 255, 255],
    'magenta': [255, 0, 255],
    'orange': [255, 165, 0],
    'pink': [255, 192, 203],
    'purple': [128, 0, 128],
    'brown': [139, 69, 19],
    'gray': [128, 128, 128],
    'grey': [128, 128, 128],
    'navy': [0, 0, 128],
    'maroon': [128, 0, 0],
    'lime': [0, 255, 0],
    'olive': [128, 128, 0],
    'teal': [0, 128, 128],
    'gold': [255, 215, 0],
    'silver': [192, 192, 192],
    'beige': [245, 245, 220],
    'turquoise': [64, 224, 208],
    'violet': [238, 130, 238],
    'indigo': [75, 0, 130],
    'coral': [255, 127, 80],
    'salmon': [250, 128, 114],
    'khaki': [240, 230, 140],
    'crimson': [220, 20, 60],
    'darkblue': [0, 0, 139],
    'darkgreen': [0, 100, 0],
    'darkred': [139, 0, 0],
    'lightblue': [173, 216, 230],
    'lightgreen': [144, 238, 144],
    'skyblue': [135, 206, 235],
}

def get_center_of_bbox(bbox):
    x1, y1, x2, y2 = bbox
    return int((x1 + x2) / 2), int((y1 + y2) / 2)

def get_bbox_width(bbox):
    return bbox[2] - bbox[0]

def measure_distance(p1, p2):
    return ((p1[0] - p2[0])**2 + (p1[1] - p2[1])**2)**0.5

def measure_xy_distance(p1, p2):
    return p1[0] - p2[0], p1[1] - p2[1]

def get_foot_position(bbox):
    x1, y1, x2, y2 = bbox
    return int((x1 + x2) / 2), int(y2)

def output_json(data):
    """Output JSON line and flush immediately for real-time streaming.

    In MJPEG streaming mode writes to stderr to keep stdout binary-clean.
    In normal analysis mode writes to stdout so Java can read structured data.
    """
    line = json.dumps(data, ensure_ascii=False) + "\n"
    if _streaming_mode:
        sys.stderr.write(line)
        sys.stderr.flush()
    else:
        sys.stdout.write(line)
        sys.stdout.flush()

def parse_color_string(color_str):
    color_str = color_str.lower().strip()
    if color_str in NAMED_COLORS:
        return np.array(NAMED_COLORS[color_str])
    
    import re
    pattern = r'(\d+)%\s*(\w+)'
    matches = re.findall(pattern, color_str)
    if not matches:
        return np.array([255, 255, 255])

    final_color = np.array([0.0, 0.0, 0.0])
    total_percent = 0
    for percent_str, color_name in matches:
        percent = float(percent_str) / 100.0
        total_percent += percent
        if color_name in NAMED_COLORS:
            final_color += np.array(NAMED_COLORS[color_name]) * percent
    
    return np.clip(final_color, 0, 255).astype(int)
