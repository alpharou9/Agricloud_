package esprit.farouk.controllers;

import esprit.farouk.models.User;
import esprit.farouk.services.FaceRecognitionService;
import esprit.farouk.services.UserService;
import esprit.farouk.utils.CameraUtils;
import esprit.farouk.utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.opencv.opencv_core.Mat;

import java.util.List;

/**
 * Controller for face login screen.
 * Handles face scanning and authentication.
 */
public class FaceLoginController {

    @FXML
    private ImageView cameraView;

    @FXML
    private Label statusLabel;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private Button scanButton;

    @FXML
    private Button usePasswordButton;

    private FrameGrabber camera;
    private FaceRecognitionService faceService;
    private UserService userService;
    private Thread cameraThread;
    private volatile boolean running = false;

    @FXML
    public void initialize() {
        this.faceService = new FaceRecognitionService();
        this.userService = new UserService();
        progressIndicator.setVisible(false);

        try {
            faceService.initialize();
            startCamera();
            statusLabel.setText("Position your face in the frame and click Scan Face");
        } catch (Exception e) {
            showError("Failed to initialize: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Starts camera preview
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
                    Platform.runLater(() -> showError("Camera error: " + e.getMessage()));
                }
            });
            cameraThread.setDaemon(true);
            cameraThread.start();

        } catch (Exception e) {
            showError("Failed to start camera: " + e.getMessage());
            scanButton.setDisable(true);
        }
    }

    /**
     * Scans face and attempts authentication
     */
    @FXML
    private void handleScanFace() {
        scanButton.setDisable(true);
        progressIndicator.setVisible(true);
        statusLabel.setText("Scanning face...");

        // Run face recognition in background thread
        new Thread(() -> {
            try {
                // Get all enrolled users
                List<User> enrolledUsers = userService.getAllFaceEnabledUsers();

                if (enrolledUsers.isEmpty()) {
                    Platform.runLater(() -> {
                        showError("No enrolled faces found in the system.\nPlease enroll your face in Profile Settings first.");
                        scanButton.setDisable(false);
                        progressIndicator.setVisible(false);
                    });
                    return;
                }

                // Capture frame
                Frame frame = camera.grab();
                Mat mat = CameraUtils.frameToMat(frame);

                // Detect face
                Mat faces = faceService.detectFace(mat);
                if (faces == null || faces.rows() == 0) {
                    Platform.runLater(() -> {
                        showError("No face detected. Please ensure your face is clearly visible.");
                        scanButton.setDisable(false);
                        progressIndicator.setVisible(false);
                    });
                    return;
                }

                System.out.println("Detection result: " + faces.rows() + ", Faces found: " + faces.rows());

                // Generate embedding
                float[] capturedEmbedding = faceService.generateEmbedding(mat, faces);

                // Authenticate
                User matchedUser = faceService.authenticateByFace(capturedEmbedding, enrolledUsers);

                Platform.runLater(() -> {
                    if (matchedUser != null) {
                        // Check if user is blocked
                        if ("blocked".equals(matchedUser.getStatus())) {
                            showError("Your account has been blocked. Please contact support.");
                            scanButton.setDisable(false);
                            progressIndicator.setVisible(false);
                            return;
                        }

                        // Login successful
                        SessionManager.setCurrentUser(matchedUser);
                        showSuccess("Face recognized! Logging in...");

                        // Navigate to dashboard after brief delay
                        new Thread(() -> {
                            try {
                                Thread.sleep(1000);
                                Platform.runLater(this::navigateToDashboard);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();

                    } else {
                        showError("Face not recognized. Please try again or use password login.");
                        scanButton.setDisable(false);
                        progressIndicator.setVisible(false);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Authentication failed: " + e.getMessage());
                    scanButton.setDisable(false);
                    progressIndicator.setVisible(false);
                });
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Returns to password login screen
     */
    @FXML
    private void handleUsePassword() {
        cleanup();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) usePasswordButton.getScene().getWindow();
            Scene scene = new Scene(root, 800, 600);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Navigates to dashboard
     */
    private void navigateToDashboard() {
        cleanup();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) scanButton.getScene().getWindow();
            Scene scene = new Scene(root, 1100, 700);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("AgriCloud - Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows error message
     */
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 14px;");
    }

    /**
     * Shows success message
     */
    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 14px; -fx-font-weight: bold;");
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
}
