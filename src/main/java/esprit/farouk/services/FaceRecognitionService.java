package esprit.farouk.services;

import esprit.farouk.models.FaceEmbedding;
import esprit.farouk.models.User;
import esprit.farouk.utils.FaceUtils;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_dnn.Net;
import org.bytedeco.opencv.opencv_objdetect.FaceDetectorYN;
import org.bytedeco.opencv.opencv_objdetect.FaceRecognizerSF;

import java.io.File;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_dnn.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_objdetect.*;

/**
 * Service for face detection and recognition using OpenCV DNN models.
 * Uses YuNet for face detection and SFace for feature extraction.
 */
public class FaceRecognitionService {

    private FaceDetectorYN faceDetector;
    private FaceRecognizerSF faceRecognizer;
    private boolean initialized = false;

    // Recognition threshold (Euclidean distance)
    private static final double RECOGNITION_THRESHOLD = 0.6;

    // Model paths
    private static final String DETECTION_MODEL_PATH = "models/face_detection_yunet_2023mar.onnx";
    private static final String RECOGNITION_MODEL_PATH = "models/face_recognition_sface_2021dec.onnx";

    /**
     * Initializes the face recognition service by loading ONNX models.
     * @throws Exception If models cannot be loaded
     */
    public void initialize() throws Exception {
        if (initialized) {
            return;
        }

        try {
            // Verify model files exist
            File detectionModel = new File(DETECTION_MODEL_PATH);
            File recognitionModel = new File(RECOGNITION_MODEL_PATH);

            if (!detectionModel.exists()) {
                throw new Exception("Face detection model not found: " + DETECTION_MODEL_PATH);
            }
            if (!recognitionModel.exists()) {
                throw new Exception("Face recognition model not found: " + RECOGNITION_MODEL_PATH);
            }

            // Load YuNet face detector (input size 320x320)
            faceDetector = FaceDetectorYN.create(
                    DETECTION_MODEL_PATH,
                    "",
                    new Size(320, 320)
            );

            // Set detection thresholds for better accuracy
            faceDetector.setScoreThreshold(0.6f);  // Lower threshold for easier detection
            faceDetector.setNMSThreshold(0.3f);
            faceDetector.setTopK(5000);

            // Load SFace recognizer
            faceRecognizer = FaceRecognizerSF.create(
                    RECOGNITION_MODEL_PATH,
                    ""
            );

            initialized = true;
            System.out.println("Face recognition models loaded successfully");

        } catch (Exception e) {
            System.err.println("Failed to initialize face recognition: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Detects face in an image and returns the bounding box.
     * @param image Input image (BGR format)
     * @return Mat containing detected face, or null if no face found
     */
    public Mat detectFace(Mat image) {
        if (!initialized || image == null || image.empty()) {
            return null;
        }

        try {
            // Set input size for detector
            faceDetector.setInputSize(new Size(image.cols(), image.rows()));

            // Set thresholds again (ensure they're applied)
            faceDetector.setScoreThreshold(0.6f);
            faceDetector.setNMSThreshold(0.3f);

            // Detect faces
            Mat faces = new Mat();
            int result = faceDetector.detect(image, faces);

            System.out.println("Detection result: " + result + ", Faces found: " + faces.rows());

            if (faces.rows() == 0 || faces.empty()) {
                System.out.println("No faces detected in image");
                return null; // No face detected
            }

            // Get first face (most prominent) - YuNet returns: x, y, w, h, ...
            Mat face = faces.row(0);

            // Extract face coordinates using FloatPointer directly
            org.bytedeco.javacpp.FloatPointer facePtr = new org.bytedeco.javacpp.FloatPointer(face.data());

            // Extract face region coordinates
            int x = (int) facePtr.get(0);  // x
            int y = (int) facePtr.get(1);  // y
            int w = (int) facePtr.get(2);  // width
            int h = (int) facePtr.get(3);  // height

            System.out.println("Face detected at: x=" + x + ", y=" + y + ", w=" + w + ", h=" + h);

            // Ensure coordinates are within bounds
            x = Math.max(0, Math.min(x, image.cols() - 1));
            y = Math.max(0, Math.min(y, image.rows() - 1));
            w = Math.min(w, image.cols() - x);
            h = Math.min(h, image.rows() - y);

            if (w <= 0 || h <= 0) {
                return null;
            }

            // Extract face ROI
            Rect faceRect = new Rect(x, y, w, h);
            return new Mat(image, faceRect);

        } catch (Exception e) {
            System.err.println("Face detection error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Generates 128D face embedding from detected face image.
     * @param faceImage Face image (output from detectFace)
     * @return 128D embedding vector as float array
     */
    public float[] generateEmbedding(Mat faceImage) {
        if (!initialized || faceImage == null || faceImage.empty()) {
            return null;
        }

        try {
            // Align face (SFace expects aligned face)
            Mat alignedFace = new Mat();
            faceRecognizer.alignCrop(faceImage, faceImage, alignedFace);

            // Extract features
            Mat feature = new Mat();
            faceRecognizer.feature(alignedFace, feature);

            // Convert to float array (128D) using FloatPointer
            float[] embedding = new float[128];
            org.bytedeco.javacpp.FloatPointer embPtr = new org.bytedeco.javacpp.FloatPointer(feature.data());
            embPtr.get(embedding);

            return embedding;

        } catch (Exception e) {
            System.err.println("Embedding generation error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Compares two face embeddings and returns similarity score.
     * @param embedding1 First embedding
     * @param embedding2 Second embedding
     * @return Euclidean distance (lower = more similar)
     */
    public double compareEmbeddings(float[] embedding1, float[] embedding2) {
        if (embedding1 == null || embedding2 == null ||
            embedding1.length != embedding2.length) {
            return Double.MAX_VALUE;
        }

        double sum = 0.0;
        for (int i = 0; i < embedding1.length; i++) {
            double diff = embedding1[i] - embedding2[i];
            sum += diff * diff;
        }

        return Math.sqrt(sum);
    }

    /**
     * Authenticates a user by comparing captured face embedding with enrolled embeddings.
     * @param capturedEmbedding Embedding from live camera capture
     * @param enrolledUsers List of users with face enrollment
     * @return Matched User object, or null if no match found
     */
    public User authenticateByFace(float[] capturedEmbedding, List<User> enrolledUsers) {
        if (capturedEmbedding == null || enrolledUsers == null || enrolledUsers.isEmpty()) {
            return null;
        }

        User bestMatch = null;
        double bestDistance = Double.MAX_VALUE;

        for (User user : enrolledUsers) {
            String embeddingsJson = user.getFaceEmbeddings();
            if (embeddingsJson == null) {
                continue;
            }

            List<FaceEmbedding> userEmbeddings = FaceUtils.embeddingsFromJson(embeddingsJson);

            for (FaceEmbedding userEmb : userEmbeddings) {
                double distance = compareEmbeddings(capturedEmbedding, userEmb.getEmbedding());

                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestMatch = user;
                }
            }
        }

        // Check if best match is below threshold
        if (bestDistance <= RECOGNITION_THRESHOLD) {
            System.out.println("Face match found: " + bestMatch.getName() + " (distance: " + bestDistance + ")");
            return bestMatch;
        }

        System.out.println("No face match found (best distance: " + bestDistance + ")");
        return null;
    }

    /**
     * Captures a frame from the camera grabber.
     * @param grabber Active FrameGrabber
     * @return Captured frame as Mat
     */
    public Mat captureFrame(FrameGrabber grabber) {
        try {
            Frame frame = grabber.grab();
            if (frame == null) {
                return null;
            }

            // Convert Frame to Mat
            org.bytedeco.javacv.OpenCVFrameConverter.ToMat converter =
                    new org.bytedeco.javacv.OpenCVFrameConverter.ToMat();
            return converter.convert(frame);

        } catch (Exception e) {
            System.err.println("Frame capture error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Releases resources and cleans up models.
     */
    public void dispose() {
        if (faceDetector != null) {
            faceDetector.close();
            faceDetector = null;
        }
        if (faceRecognizer != null) {
            faceRecognizer.close();
            faceRecognizer = null;
        }
        initialized = false;
        System.out.println("Face recognition service disposed");
    }

    public boolean isInitialized() {
        return initialized;
    }

    public double getRecognitionThreshold() {
        return RECOGNITION_THRESHOLD;
    }
}
