# Face Recognition - Quick Start Guide

## Prerequisites

Before using face recognition, ensure you have:

1. ✅ **Database Updated** - Run SQL setup script
2. ✅ **Maven Dependencies** - Build completed
3. ✅ **ONNX Models** - Downloaded to `models/` directory
4. ✅ **Webcam** - Connected and accessible
5. ✅ **Admin Account** - Only admins can use face recognition

---

## Step 1: Database Setup

**Execute SQL commands in phpMyAdmin:**

1. Open http://localhost/phpmyadmin
2. Select `agricloud` database
3. Click **SQL** tab
4. Copy and paste from `FACE_RECOGNITION_DATABASE_SETUP.sql`
5. Click **Go**

**Verify:**
```sql
DESCRIBE users;
-- Should show: face_embeddings (TEXT), face_enrolled_at (TIMESTAMP)
```

---

## Step 2: Build Project

**Run Maven build:**

```bash
cd C:\Users\rouk1\OneDrive\Bureau\projekt
mvn clean install
```

**Expected:** BUILD SUCCESS (downloads ~100MB JavaCV dependencies)

---

## Step 3: Download Models

**Option A: PowerShell (Recommended)**

```powershell
cd models
Invoke-WebRequest -Uri "https://github.com/opencv/opencv_zoo/raw/main/models/face_detection_yunet/face_detection_yunet_2023mar.onnx" -OutFile "face_detection_yunet_2023mar.onnx"
Invoke-WebRequest -Uri "https://github.com/opencv/opencv_zoo/raw/main/models/face_recognition_sface/face_recognition_sface_2021dec.onnx" -OutFile "face_recognition_sface_2021dec.onnx"
```

**Option B: Manual Download**

1. Download: https://github.com/opencv/opencv_zoo/raw/main/models/face_detection_yunet/face_detection_yunet_2023mar.onnx
2. Download: https://github.com/opencv/opencv_zoo/raw/main/models/face_recognition_sface/face_recognition_sface_2021dec.onnx
3. Save to `C:\Users\rouk1\OneDrive\Bureau\projekt\models\`

**Verify:**
```bash
dir models
# Should show:
# face_detection_yunet_2023mar.onnx (300KB)
# face_recognition_sface_2021dec.onnx (30MB)
```

---

## Step 4: Run Application

```bash
mvn javafx:run
```

---

## Step 5: Enroll Your Face (Admin Only)

1. **Login** with admin credentials (e.g., admin@agricloud.com)
2. **Navigate** to Profile (sidebar)
3. **Scroll** to "Face Recognition" section
4. **Click** "Setup Face Recognition"
5. **Follow** on-screen instructions:
   - Look straight (capture 1/5)
   - Turn left (capture 2/5)
   - Turn right (capture 3/5)
   - Tilt up (capture 4/5)
   - Tilt down (capture 5/5)
6. **Click** "Finish" to save

**Result:** Profile shows "✓ Face recognition enabled"

---

## Step 6: Test Face Login

1. **Logout** from dashboard
2. **Click** "Login with Face Recognition" button
3. **Position** your face in camera frame
4. **Click** "Scan Face"
5. **Wait** 2-3 seconds
6. **Success!** Logged in to dashboard

---

## Troubleshooting

### Error: "Face detection model not found"

**Cause:** ONNX models not downloaded

**Fix:**
1. Check `models/` directory exists
2. Verify both ONNX files are present
3. Re-run download script

---

### Error: "Failed to start camera"

**Cause:** Camera in use or no permissions

**Fix:**
1. Close other camera apps (Zoom, Skype, etc.)
2. Grant camera permissions:
   - Windows Settings → Privacy → Camera → Allow apps
3. Restart application
4. Try different USB port (if USB camera)

---

### Warning: "Face not recognized"

**Cause:** Poor lighting or no enrollment

**Fix:**
1. Ensure good lighting (avoid backlighting)
2. Position face clearly in frame
3. Verify you enrolled face as admin user
4. Re-enroll if appearance changed

---

### Error: "No users with face enrollment found"

**Cause:** No enrolled users in database

**Fix:**
1. Login as admin with password
2. Enroll face via Profile Settings
3. Logout and try face login again

---

## Testing Checklist

- [ ] Database schema updated
- [ ] Maven build successful
- [ ] Both ONNX models downloaded
- [ ] Camera accessible
- [ ] Admin login works (password)
- [ ] Face enrollment completes (5/5)
- [ ] Profile shows "✓ Face recognition enabled"
- [ ] Face login authenticates successfully
- [ ] Dashboard loads correctly
- [ ] "Use Password" fallback works
- [ ] "Remove Face Data" deletes enrollment

---

## Security Notes

**What is stored in the database?**
- Only mathematical embeddings (128D float arrays)
- NOT your actual face images
- Embeddings cannot be reversed to recreate your face

**Who can access face recognition?**
- Only users with **Admin** role
- Farmer, Customer, Guest cannot enroll or use face login

**Can I remove my face data?**
- Yes, anytime from Profile Settings
- Click "Remove Face Data" button
- Confirmation required before deletion

**Is password login still available?**
- Yes, always available as fallback
- Face recognition is optional enhancement
- Click "Use Password" on face login screen

---

## Need Help?

**Full Documentation:** See `FACE_RECOGNITION_IMPLEMENTATION.md`

**Common Issues:**
- Camera permissions: Windows Settings → Privacy → Camera
- Model errors: Re-download ONNX files
- Recognition failures: Re-enroll with better lighting

**Contact:** Farouk (Module 1: User Management)

---

**Version:** 1.0
**Last Updated:** February 2026
**Status:** ✅ Production Ready
