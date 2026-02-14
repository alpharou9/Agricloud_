# Face Recognition Models

This directory contains pre-trained ONNX models for face detection and recognition.

## Required Models

### 1. YuNet Face Detection Model
- **File:** `face_detection_yunet_2023mar.onnx` (300KB)
- **URL:** https://github.com/opencv/opencv_zoo/raw/main/models/face_detection_yunet/face_detection_yunet_2023mar.onnx
- **Purpose:** Detects faces in camera frames

### 2. SFace Recognition Model
- **File:** `face_recognition_sface_2021dec.onnx` (30MB)
- **URL:** https://github.com/opencv/opencv_zoo/raw/main/models/face_recognition_sface/face_recognition_sface_2021dec.onnx
- **Purpose:** Extracts 128D face embeddings

## Download Instructions

### PowerShell (Windows):
```powershell
cd models
Invoke-WebRequest -Uri "https://github.com/opencv/opencv_zoo/raw/main/models/face_detection_yunet/face_detection_yunet_2023mar.onnx" -OutFile "face_detection_yunet_2023mar.onnx"
Invoke-WebRequest -Uri "https://github.com/opencv/opencv_zoo/raw/main/models/face_recognition_sface/face_recognition_sface_2021dec.onnx" -OutFile "face_recognition_sface_2021dec.onnx"
```

### Manual Download:
1. Download both files from the URLs above
2. Save them to this directory

## Verification
After download, verify files exist:
```bash
ls models/
# Should show:
# face_detection_yunet_2023mar.onnx (300KB)
# face_recognition_sface_2021dec.onnx (30MB)
```

---

**Note:** These models are excluded from version control via `.gitignore` due to file size.
