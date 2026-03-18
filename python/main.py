import argparse
import os
import sys
import json
import cv2
import numpy as np
# from config import EASYOCR_AVAILABLE, PADDLEOCR_AVAILABLE # لا نحتاج استيرادهم لأننا سنفحصهم هنا
from analyzer import FootballAnalyzer
from utils import output_json, parse_color_string, set_streaming_mode
os.environ['DISABLE_MODEL_SOURCE_CHECK'] = 'True'

PADDLEOCR_AVAILABLE = False
EASYOCR_AVAILABLE = False

try:
    from paddleocr import PaddleOCR
    PADDLEOCR_AVAILABLE = True
except ImportError:
    try:
        import easyocr
        EASYOCR_AVAILABLE = True
    except ImportError:
        pass

# ============== MAIN FUNCTION ==============

def main():
    parser = argparse.ArgumentParser(description='Football Video Analyzer - Streaming JSON Output')
    parser.add_argument('-i', '--input', required=True, help='Path to input video')
    parser.add_argument('-o', '--output', help='Path for annotated output video')
    parser.add_argument('-m', '--model', default='models/yolov10n.pt', help='Path to YOLO model')
    parser.add_argument('-d', '--device', default='cpu', choices=['cpu', 'cuda'], help='Device to use')
    parser.add_argument('-t', '--teams', default='config/teams.json', help='Path to teams configuration JSON')
    parser.add_argument('--no-ocr', action='store_true', help='Disable jersey number recognition')
    parser.add_argument('--no-annotate', action='store_true', help='Disable video annotation')
    parser.add_argument('--stream-mjpeg', action='store_true', help='Stream annotated frames as MJPEG')

    args = parser.parse_args()

    # Verify Paths
    if not os.path.exists(args.model):
        output_json({"type": "error", "message": f"Model not found: {args.model}"})
        sys.exit(1)

    if not os.path.exists(args.input):
        output_json({"type": "error", "message": f"Video not found: {args.input}"})
        sys.exit(1)

    # Initialize Analyzer
    enable_ocr = not args.no_ocr and (PADDLEOCR_AVAILABLE or EASYOCR_AVAILABLE)
    
    # Streaming mode logic
    if args.stream_mjpeg:
        # Enable streaming mode BEFORE creating the analyzer so that all
        # output_json calls inside __init__ go to stderr, not stdout.
        set_streaming_mode(True)

        # Set stdout to binary mode on Windows immediately, before any write.
        if sys.platform == 'win32':
            import msvcrt
            msvcrt.setmode(sys.stdout.fileno(), os.O_BINARY)

        sys.stderr.write(json.dumps({"type": "info", "message": "Streaming Mode: OCR disabled for speed"}) + "\n")
        sys.stderr.flush()

        analyzer = FootballAnalyzer(
            args.model, args.device, teams_config_path=args.teams,
            enable_ocr=False, enable_annotation=True
        )
        analyzer.stream_mjpeg(args.input, output_video_path=args.output)
    else:
        analyzer = FootballAnalyzer(
            args.model, args.device, teams_config_path=args.teams,
            enable_ocr=enable_ocr, enable_annotation=not args.no_annotate
        )
        analyzer.analyze_video(args.input, args.output)

if __name__ == "__main__":
    main()