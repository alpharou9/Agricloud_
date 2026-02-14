package esprit.farouk.controllers;

import esprit.farouk.models.FaceEmbedding;
import esprit.farouk.services.FaceRecognitionService;
import esprit.farouk.services.UserService;
import esprit.farouk.utils.CameraUtils;
import esprit.farouk.utils.FaceUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.opencv.opencv_core.Mat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for face enrollment dialog.
 * Guides user through 5-capture process for robust face recognition.
 */
public class FaceEnrollmentController {

    @FXML
    private ImageView cameraView;

    @FXML
    private Label instructionLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Button captureButton;

    @FXML
    private Button finishButton;

    @FXML
    private Button cancelButton;

    private FrameGrabber camera;
    private FaceRecognitionService faceService;
    private UserService userService;
    private long currentUserId;

    private List<FaceEmbedding> capturedEmbeddings = new ArrayList<>();
    private int captureCount = 0;
    private final int REQUIRED_CAPTURES = 5;

    private String[] instructions = {
        "Look straight at the camera",
        "Turn your head slightly left",
        "Turn your head slightly right",
        "Tilt your head slightly up",
        "Tilt your head slightly down"
    };

    private Thread cameraThread;
    private volatile boolean running = false;

    /**
     * Initializes enrollment dialog for specific user
     */
    public void initialize(long userId) {
        this.currentUserId = userId;
        this.faceService = new FaceRecognitionService();
        this.userService = new UserService();

        try {
            faceService.initialize();
            startCamera();
            updateUI();
        } catch (Exception e) {
            showError("Failed to initialize camera: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Starts camera and preview stream
     */
    private void startCamera() {
        try {
            camera = CameraUtils.createCameraGrabber(0);
            running = true;

            cameraThread = new Thread(() -> {
                try {
                    while (running) {
                        Frame frame = camera.grab();
                        if (frame != null && frame.image != null) {
                            Mat mat = CameraUtils.frameToMat(frame);
                            Platform.runLater(() -> {
                                cameraView.setImage(CameraUtils.matToImage(mat));
                            });
                        }
                        Thread.sleep(33); // ~30 FPS
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            cameraThread.setDaemon(true);
            cameraThread.start();

        } catch (Exception e) {
            showError("Failed to start camera: " + e.getMessage());
        }
    }

    /**
     * Captures current frame and extracts face embedding
     */
    @FXML
    private void handleCapture() {
        if (captureCount >= REQUIRED_CAPTURES) {
            return;
        }

        try {
            Frame frame = camera.grab();
            Mat mat = CameraUtils.frameToMat(frame);

            // Detect face
            Mat faces = faceService.detectFace(mat);
            if (faces == null || faces.rows() == 0) {
                showError("No face detected. Please position your face clearly.");
                return;
            }

            // Generate embedding
            float[] embedding = faceService.generateEmbedding(mat, faces);
            FaceEmbedding faceEmbedding = new FaceEmbedding(embedding, LocalDateTime.now());
            capturedEmbeddings.add(faceEmbedding);

            captureCount++;
            updateUI();

            if (captureCount >= REQUIRED_CAPTURES) {
                captureButton.setDisable(true);
                finishButton.setDisable(false);
                statusLabel.setText("All captures complete! Click Finish to save.");
                statusLabel.setStyle("-fx-text-fill: #16a34a;");
            }

        } catch (Exception e) {
            showError("Capture failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Saves all captured embeddings to database
     */
    @FXML
    private void handleFinish() {
        if (captureCount < REQUIRED_CAPTURES) {
            showError("Please complete all " + REQUIRED_CAPTURES + " captures first.");
            return;
        }

        try {
            String embeddingsJson = FaceUtils.embeddingsToJson(capturedEmbeddings);
            boolean success = userService.enrollFaceEmbeddings(currentUserId, embeddingsJson);

            if (success) {
                showSuccess("Face recognition enrolled successfully!");
                cleanup();
                closeDialog();
            } else {
                showError("Failed to save face data to database.");
            }

        } catch (Exception e) {
            showError("Failed to enroll face: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cancels enrollment and closes dialog
     */
    @FXML
    private void handleCancel() {
        cleanup();
        closeDialog();
    }

    /**
     * Updates UI based on current capture progress
     */
    private void updateUI() {
        progressBar.setProgress((double) captureCount / REQUIRED_CAPTURES);
        statusLabel.setText("Captures: " + captureCount + "/" + REQUIRED_CAPTURES);

        if (captureCount < REQUIRED_CAPTURES) {
            instructionLabel.setText(instructions[captureCount]);
        } else {
            instructionLabel.setText("Enrollment Complete!");
        }
    }

    /**
     * Shows error message
     */
    private void showError(String message) {
        statusLabel.setText("Error: " + message);
        statusLabel.setStyle("-fx-text-fill: #dc2626;");
    }

    /**
     * Shows success message
     */
    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #16a34a;");
    }

    /**
     * Cleanup camera resources
     */
    private void cleanup() {
        running = false;
        if (cameraThread != null) {
            try {
                cameraThread.join(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        CameraUtils.releaseGrabber(camera);
        if (faceService != null) {
            faceService.dispose();
        }
    }

    /**
     * Closes the dialog
     */
    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
