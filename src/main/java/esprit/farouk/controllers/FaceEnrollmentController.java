package esprit.farouk.controllers;

import esprit.farouk.models.FaceEmbedding;
import esprit.farouk.models.User;
import esprit.farouk.services.FaceRecognitionService;
import esprit.farouk.services.UserService;
import esprit.farouk.utils.CameraUtils;
import esprit.farouk.utils.FaceUtils;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.opencv.opencv_core.Mat;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for face enrollment dialog.
 * Captures 5 face images from different angles and saves embeddings.
 */
public class FaceEnrollmentController {

    @FXML private ImageView cameraPreview;
    @FXML private Label instructionLabel;
    @FXML private Label progressLabel;
    @FXML private Label statusLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Button captureButton;
    @FXML private Button finishButton;

    private User currentUser;
    private UserService userService;
    private FaceRecognitionService faceService;
    private FrameGrabber grabber;
    private AnimationTimer cameraTimer;
    private List<FaceEmbedding> capturedEmbeddings;
    private int captureCount = 0;

    private static final String[] INSTRUCTIONS = {
            "Look straight at the camera",
            "Turn your head slightly to the left",
            "Turn your head slightly to the right",
            "Tilt your head slightly up",
            "Tilt your head slightly down"
    };

    public void initialize() {
        capturedEmbeddings = new ArrayList<>();
        userService = new UserService();
        faceService = new FaceRecognitionService();

        // Initialize face recognition service
        try {
            faceService.initialize();
        } catch (Exception e) {
            showError("Failed to initialize face recognition: " + e.getMessage());
            Platform.runLater(() -> ((Stage) cameraPreview.getScene().getWindow()).close());
            return;
        }

        // Start camera
        startCamera();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    private void startCamera() {
        try {
            grabber = CameraUtils.createCameraGrabber(0);

            // Start camera preview timer
            cameraTimer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    updateCameraPreview();
                }
            };
            cameraTimer.start();

            statusLabel.setText("Camera started. Ready to capture.");

        } catch (Exception e) {
            showError("Failed to start camera: " + e.getMessage());
            Platform.runLater(() -> ((Stage) cameraPreview.getScene().getWindow()).close());
        }
    }

    private void updateCameraPreview() {
        try {
            Mat frame = faceService.captureFrame(grabber);
            if (frame != null && !frame.empty()) {
                javafx.scene.image.Image image = CameraUtils.matToImage(frame);
                if (image != null) {
                    Platform.runLater(() -> cameraPreview.setImage(image));
                }
            }
        } catch (Exception e) {
            System.err.println("Preview update error: " + e.getMessage());
        }
    }

    @FXML
    private void handleCapture() {
        if (captureCount >= 5) {
            return;
        }

        captureButton.setDisable(true);
        statusLabel.setText("Capturing...");

        // Capture in background thread
        new Thread(() -> {
            try {
                // Capture frame
                Mat frame = faceService.captureFrame(grabber);
                if (frame == null || frame.empty()) {
                    Platform.runLater(() -> {
                        statusLabel.setText("Failed to capture frame. Try again.");
                        captureButton.setDisable(false);
                    });
                    return;
                }

                // Detect face
                Mat faceImage = faceService.detectFace(frame);
                if (faceImage == null) {
                    Platform.runLater(() -> {
                        statusLabel.setText("No face detected. Please position your face clearly.");
                        captureButton.setDisable(false);
                    });
                    return;
                }

                // Generate embedding
                float[] embedding = faceService.generateEmbedding(faceImage);
                if (embedding == null) {
                    Platform.runLater(() -> {
                        statusLabel.setText("Failed to generate face embedding. Try again.");
                        captureButton.setDisable(false);
                    });
                    return;
                }

                // Store embedding
                capturedEmbeddings.add(new FaceEmbedding(embedding));
                captureCount++;

                Platform.runLater(() -> {
                    updateProgress();
                    statusLabel.setText("Captured successfully! (" + captureCount + "/5)");

                    if (captureCount < 5) {
                        instructionLabel.setText(INSTRUCTIONS[captureCount]);
                        captureButton.setDisable(false);
                    } else {
                        instructionLabel.setText("All captures complete!");
                        captureButton.setDisable(true);
                        finishButton.setDisable(false);
                        statusLabel.setText("Click Finish to save your face data.");
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Capture error: " + e.getMessage());
                    captureButton.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    private void handleFinish() {
        if (capturedEmbeddings.isEmpty()) {
            showError("No face data captured");
            return;
        }

        finishButton.setDisable(true);
        statusLabel.setText("Saving face data...");

        new Thread(() -> {
            try {
                // Convert embeddings to JSON
                String embeddingsJson = FaceUtils.embeddingsToJson(capturedEmbeddings);

                // Save to database
                userService.enrollFaceEmbeddings(currentUser.getId(), embeddingsJson);

                Platform.runLater(() -> {
                    showInfo("Face enrollment completed successfully!");
                    cleanup();
                    ((Stage) cameraPreview.getScene().getWindow()).close();
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Failed to save face data: " + e.getMessage());
                    finishButton.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    private void handleCancel() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Enrollment");
        confirm.setHeaderText("Are you sure you want to cancel?");
        confirm.setContentText("All captured face data will be discarded.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            cleanup();
            ((Stage) cameraPreview.getScene().getWindow()).close();
        }
    }

    private void updateProgress() {
        double progress = captureCount / 5.0;
        progressBar.setProgress(progress);
        progressLabel.setText(captureCount + "/5 captured");
    }

    private void cleanup() {
        // Stop camera timer
        if (cameraTimer != null) {
            cameraTimer.stop();
        }

        // Release camera
        CameraUtils.releaseGrabber(grabber);

        // Dispose face service
        faceService.dispose();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Face Enrollment Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("Face Enrollment");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
