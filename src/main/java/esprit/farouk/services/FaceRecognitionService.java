package esprit.farouk.services;

import esprit.farouk.models.FaceEmbedding;
import esprit.farouk.models.User;
import esprit.farouk.utils.FaceUtils;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_objdetect.FaceDetectorYN;
import org.bytedeco.opencv.opencv_objdetect.FaceRecognizerSF;

import java.util.List;

/**
 * Face recognition service using OpenCV DNN models.
 * Handles face detection, feature extraction, and authentication.
 */
public class FaceRecognitionService {
    private static final String DETECTION_MODEL_PATH = "models/face_detection_yunet_2023mar.onnx";
    private static final String RECOGNITION_MODEL_PATH = "models/face_recognition_sface_2021dec.onnx";
    private static final double RECOGNITION_THRESHOLD = 7.0; // Euclidean distance threshold (higher = more lenient)

    private FaceDetectorYN faceDetector;
    private FaceRecognizerSF faceRecognizer;
    private OpenCVFrameConverter.ToMat converter;

    /**
     * Initializes face detection and recognition models.
     *
     * @throws Exception if models cannot be loaded
     */
    public void initialize() throws Exception {
        System.out.println("Loading face recognition models...");

        // Initialize face detector (YuNet)
        faceDetector = FaceDetectorYN.create(
                DETECTION_MODEL_PATH,
                "",
                new Size(320, 320)
        );
        faceDetector.setScoreThreshold(0.6f);
        faceDetector.setNMSThreshold(0.3f);

        // Initialize face recognizer (SFace)
        faceRecognizer = FaceRecognizerSF.create(RECOGNITION_MODEL_PATH, "");

        converter = new OpenCVFrameConverter.ToMat();

        System.out.println("Face recognition models loaded successfully");
    }

    /**
     * Detects face in the given frame.
     *
     * @param frame Input image as Mat
     * @return Mat containing face coordinates, or null if no face detected
     */
    public Mat detectFace(Mat frame) {
        if (faceDetector == null) {
            throw new IllegalStateException("Face detector not initialized");
        }

        faceDetector.setInputSize(new Size(frame.cols(), frame.rows()));
        Mat faces = new Mat();
        faceDetector.detect(frame, faces);

        if (faces.rows() > 0) {
            return faces;
        }

        return null;
    }

    /**
     * Generates 128D face embedding from detected face.
     *
     * @param frame Input frame
     * @param faceBox Detected face coordinates
     * @return 128D float array embedding
     */
    public float[] generateEmbedding(Mat frame, Mat faceBox) {
        if (faceRecognizer == null) {
            throw new IllegalStateException("Face recognizer not initialized");
        }

        // Extract face coordinates from first row
        org.bytedeco.javacpp.FloatPointer facePtr = new org.bytedeco.javacpp.FloatPointer(faceBox.data());
        int x = (int) facePtr.get(0);
        int y = (int) facePtr.get(1);
        int w = (int) facePtr.get(2);
        int h = (int) facePtr.get(3);

        System.out.println("Face detected at: x=" + x + ", y=" + y + ", w=" + w + ", h=" + h);

        // Align face
        Mat alignedFace = new Mat();
        faceRecognizer.alignCrop(frame, faceBox, alignedFace);

        // Extract features (128D embedding)
        Mat feature = new Mat();
        faceRecognizer.feature(alignedFace, feature);

        // Convert Mat to float array
        float[] embedding = new float[128];
        org.bytedeco.javacpp.FloatPointer embPtr = new org.bytedeco.javacpp.FloatPointer(feature.data());
        embPtr.get(embedding);

        return embedding;
    }

    /**
     * Compares two face embeddings and returns similarity score.
     *
     * @param embedding1 First embedding
     * @param embedding2 Second embedding
     * @return Euclidean distance (lower = more similar)
     */
    public double compareEmbeddings(float[] embedding1, float[] embedding2) {
        if (embedding1.length != embedding2.length) {
            throw new IllegalArgumentException("Embeddings must have same dimension");
        }

        double sum = 0.0;
        for (int i = 0; i < embedding1.length; i++) {
            double diff = embedding1[i] - embedding2[i];
            sum += diff * diff;
        }

        return Math.sqrt(sum);
    }

    /**
     * Authenticates user by comparing face embedding against enrolled users.
     *
     * @param capturedEmbedding Embedding from camera
     * @param enrolledUsers List of users with enrolled faces
     * @return Matched User or null if no match found
     */
    public User authenticateByFace(float[] capturedEmbedding, List<User> enrolledUsers) {
        if (capturedEmbedding == null || enrolledUsers == null || enrolledUsers.isEmpty()) {
            return null;
        }

        User bestMatch = null;
        double bestDistance = Double.MAX_VALUE;

        for (User user : enrolledUsers) {
            if ("blocked".equals(user.getStatus())) {
                continue; // Skip blocked users
            }

            List<FaceEmbedding> userEmbeddings = FaceUtils.embeddingsFromJson(user.getFaceEmbeddings());

            for (FaceEmbedding storedEmbedding : userEmbeddings) {
                double distance = compareEmbeddings(capturedEmbedding, storedEmbedding.getEmbedding());

                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestMatch = user;
                }
            }
        }

        // Check if best match is below threshold
        if (bestDistance < RECOGNITION_THRESHOLD) {
            System.out.println("Face recognized! Distance: " + bestDistance + " (threshold: " + RECOGNITION_THRESHOLD + ")");
            return bestMatch;
        } else {
            System.out.println("Face not recognized. Best distance: " + bestDistance + " (threshold: " + RECOGNITION_THRESHOLD + ")");
            return null;
        }
    }

    /**
     * Captures single frame from camera.
     *
     * @param grabber Camera frame grabber
     * @return Frame object
     * @throws FrameGrabber.Exception if capture fails
     */
    public Frame captureFrame(FrameGrabber grabber) throws FrameGrabber.Exception {
        return grabber.grab();
    }

    /**
     * Converts Frame to Mat for processing.
     *
     * @param frame JavaCV Frame
     * @return OpenCV Mat
     */
    public Mat frameToMat(Frame frame) {
        return converter.convert(frame);
    }

    /**
     * Cleanup resources.
     */
    public void dispose() {
        if (faceDetector != null) {
            faceDetector.close();
        }
        if (faceRecognizer != null) {
            faceRecognizer.close();
        }
    }
}
