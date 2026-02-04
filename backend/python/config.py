# config.py
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