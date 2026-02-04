import argparse
import os
import sys
import json
import cv2
import numpy as np
from config import EASYOCR_AVAILABLE, PADDLEOCR_AVAILABLE
from analyzer import FootballAnalyzer
from utils import output_json, parse_color_string
os.environ['DISABLE_MODEL_SOURCE_CHECK'] = 'True'



try:
    from paddleocr import PaddleOCR
    PADDLEOCR_AVAILABLE = True
    sys.stderr.write("[OCR] PaddleOCR import successful\n")
except ImportError:
    try:
        import easyocr
        EASYOCR_AVAILABLE = True
        sys.stderr.write("[OCR] EasyOCR import successful (fallback)\n")
    except ImportError:
        sys.stderr.write("[OCR] No OCR library found\n")


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
    # Hna ghadi n-passiw availability dial OCR l-dakhel
    enable_ocr = not args.no_ocr and (PADDLEOCR_AVAILABLE or EASYOCR_AVAILABLE)
    
    # Streaming mode logic
    if args.stream_mjpeg:
        sys.stderr.write("[Streaming] Loading AI models (OCR disabled for speed)...\n")
        analyzer = FootballAnalyzer(
            args.model, args.device, teams_config_path=args.teams,
            enable_ocr=False, enable_annotation=True
        )
        analyzer.stream_mjpeg(args.input)
    else:
        analyzer = FootballAnalyzer(
            args.model, args.device, teams_config_path=args.teams,
            enable_ocr=enable_ocr, enable_annotation=not args.no_annotate
        )
        analyzer.analyze_video(args.input, args.output)

if __name__ == "__main__":
    main()