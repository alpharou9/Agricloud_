package esprit.farouk.controllers;

import esprit.farouk.models.User;
import esprit.farouk.services.FaceRecognitionService;
import esprit.farouk.services.UserService;
import esprit.farouk.utils.CameraUtils;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.opencv.opencv_core.Mat;

import java.util.List;

/**
 * Controller for face login screen.
 * Handles face scanning and authentication.
 */
public class FaceLoginController {

    @FXML private ImageView cameraPreview;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Button scanButton;

    private UserService userService;
    private FaceRecognitionService faceService;
    private FrameGrabber grabber;
    private AnimationTimer cameraTimer;
    private boolean isProcessing = false;

    public void initialize() {
        userService = new UserService();
        faceService = new FaceRecognitionService();

        // Initialize face recognition service
        try {
            faceService.initialize();
            statusLabel.setText("Camera initializing...");
            startCamera();
        } catch (Exception e) {
            showError("Failed to initialize face recognition: " + e.getMessage() +
                    "\n\nPlease ensure:\n" +
                    "1. ONNX models are downloaded in the 'models' directory\n" +
                    "2. Your webcam is connected and accessible");
            Platform.runLater(() -> handleUsePassword());
        }
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

            statusLabel.setText("Ready to scan. Position your face in the camera.");

        } catch (Exception e) {
            showError("Failed to start camera: " + e.getMessage() +
                    "\n\nPlease check:\n" +
                    "1. Camera is connected\n" +
                    "2. Camera is not being used by another application\n" +
                    "3. Camera permissions are granted");
            Platform.runLater(() -> handleUsePassword());
        }
    }

    private void updateCameraPreview() {
        if (isProcessing) {
            return; // Skip preview updates during face recognition
        }

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
    private void handleScanFace() {
        if (isProcessing) {
            return;
        }

        isProcessing = true;
        scanButton.setDisable(true);
        statusLabel.setText("Scanning face...");
        progressIndicator.setVisible(true);
        progressIndicator.setManaged(true);

        // Perform face recognition in background thread
        new Thread(() -> {
            try {
                // Capture frame
                Mat frame = faceService.captureFrame(grabber);
                if (frame == null || frame.empty()) {
                    Platform.runLater(() -> {
                        statusLabel.setText("Failed to capture frame. Please try again.");
                        resetScanButton();
                    });
                    return;
                }

                // Detect face
                Platform.runLater(() -> statusLabel.setText("Detecting face..."));
                Mat faceImage = faceService.detectFace(frame);
                if (faceImage == null) {
                    Platform.runLater(() -> {
                        statusLabel.setText("No face detected. Please position your face clearly.");
                        resetScanButton();
                    });
                    return;
                }

                // Generate embedding
                Platform.runLater(() -> statusLabel.setText("Analyzing face features..."));
                float[] embedding = faceService.generateEmbedding(faceImage);
                if (embedding == null) {
                    Platform.runLater(() -> {
                        statusLabel.setText("Failed to analyze face. Please try again.");
                        resetScanButton();
                    });
                    return;
                }

                // Get all users with face enrollment
                Platform.runLater(() -> statusLabel.setText("Matching face..."));
                List<User> enrolledUsers = userService.getAllFaceEnabledUsers();

                if (enrolledUsers.isEmpty()) {
                    Platform.runLater(() -> {
                        statusLabel.setText("No users with face enrollment found.");
                        showInfo("No enrolled faces found in the system.\n\n" +
                                "Please enroll your face in Profile Settings first,\n" +
                                "or use password login.");
                        resetScanButton();
                    });
                    return;
                }

                // Authenticate
                User matchedUser = faceService.authenticateByFace(embedding, enrolledUsers);

                if (matchedUser != null) {
                    // Check if user is blocked
                    if ("blocked".equalsIgnoreCase(matchedUser.getStatus())) {
                        Platform.runLater(() -> {
                            statusLabel.setText("Access denied.");
                            showError("Your account has been blocked.\nPlease contact the administrator.");
                            resetScanButton();
                        });
                        return;
                    }

                    // Successful login
                    Platform.runLater(() -> {
                        statusLabel.setText("Face recognized! Welcome, " + matchedUser.getName());
                        navigateToDashboard(matchedUser);
                    });

                } else {
                    Platform.runLater(() -> {
                        statusLabel.setText("Face not recognized. Please try again or use password.");
                        showWarning("Face not recognized",
                                "Your face could not be matched with any enrolled user.\n\n" +
                                "Please try again or use password login.");
                        resetScanButton();
                    });
                }

            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Recognition error: " + e.getMessage());
                    showError("Face recognition error: " + e.getMessage());
                    resetScanButton();
                });
            }
        }).start();
    }

    @FXML
    private void handleUsePassword() {
        cleanup();
        navigateToLogin();
    }

    private void resetScanButton() {
        isProcessing = false;
        scanButton.setDisable(false);
        progressIndicator.setVisible(false);
        progressIndicator.setManaged(false);
    }

    private void navigateToDashboard(User user) {
        try {
            cleanup();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent root = loader.load();

            DashboardController controller = loader.getController();
            controller.setCurrentUser(user);

            Stage stage = (Stage) cameraPreview.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 700);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("AgriCloud - Dashboard");

        } catch (Exception e) {
            showError("Failed to load dashboard: " + e.getMessage());
        }
    }

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) cameraPreview.getScene().getWindow();
            Scene scene = new Scene(root, 800, 650);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("AgriCloud - Login");

        } catch (Exception e) {
            showError("Failed to load login screen: " + e.getMessage());
        }
    }

    private void cleanup() {
        // Stop camera timer
        if (cameraTimer != null) {
            cameraTimer.stop();
        }

        // Release camera
        CameraUtils.releaseGrabber(grabber);

        // Dispose face service
        if (faceService != null) {
            faceService.dispose();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Face Login Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText("Face Recognition");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
