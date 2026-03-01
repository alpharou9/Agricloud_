package esprit.farouk.controllers;

import esprit.farouk.models.User;
import esprit.farouk.services.UserService;
import esprit.farouk.utils.EmailUtils;
import esprit.farouk.utils.ValidationUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.Random;

public class ForgotPasswordController {

    @FXML
    private VBox emailStep;

    @FXML
    private VBox codeStep;

    @FXML
    private TextField emailField;

    @FXML
    private TextField codeField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    @FXML
    private Button sendCodeButton;

    @FXML
    private Button resetButton;

    private UserService userService;
    private String generatedCode;
    private String userEmail;
    private LocalDateTime codeExpiryTime;

    @FXML
    public void initialize() {
        userService = new UserService();
        messageLabel.setVisible(false);
    }

    @FXML
    private void handleSendCode() {
        // Clear previous messages
        messageLabel.setVisible(false);

        // Get email
        String email = emailField.getText().trim();

        // Validate email
        if (ValidationUtils.isEmpty(email)) {
            showError("Please enter your email address");
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            showError("Please enter a valid email address");
            return;
        }

        // Disable button to prevent multiple clicks
        sendCodeButton.setDisable(true);
        sendCodeButton.setText("Sending...");

        // Check if user exists in background thread
        new Thread(() -> {
            User user = userService.getByEmail(email);

            Platform.runLater(() -> {
                if (user == null) {
                    showError("No account found with this email address");
                    sendCodeButton.setDisable(false);
                    sendCodeButton.setText("Send Reset Code");
                    return;
                }

                // Generate 6-digit code
                generatedCode = String.format("%06d", new Random().nextInt(1000000));
                userEmail = email;
                codeExpiryTime = LocalDateTime.now().plusMinutes(15);

                // Send email using sendResetCode
                try {
                    EmailUtils.sendResetCode(email, generatedCode);

                    // Switch to code step
                    emailStep.setVisible(false);
                    emailStep.setManaged(false);
                    codeStep.setVisible(true);
                    codeStep.setManaged(true);

                    showSuccess("Reset code sent to " + email + "\nCode expires in 15 minutes.");

                    // For testing purposes, also print to console
                    System.out.println("✓ Reset code sent: " + generatedCode);

                } catch (Exception e) {
                    showError("Failed to send email. Please check your email configuration.");
                    e.printStackTrace();
                }

                sendCodeButton.setDisable(false);
                sendCodeButton.setText("Send Reset Code");
            });
        }).start();
    }

    @FXML
    private void handleResetPassword() {
        // Clear previous messages
        messageLabel.setVisible(false);

        // Get input values
        String code = codeField.getText().trim();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate inputs
        if (ValidationUtils.isEmpty(code)) {
            showError("Please enter the reset code");
            return;
        }

        if (code.length() != 6) {
            showError("Reset code must be 6 digits");
            return;
        }

        if (ValidationUtils.isEmpty(newPassword)) {
            showError("Please enter a new password");
            return;
        }

        if (!ValidationUtils.isValidPassword(newPassword)) {
            showError("Password must be at least 6 characters long");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        // Check if code is expired
        if (LocalDateTime.now().isAfter(codeExpiryTime)) {
            showError("Reset code has expired. Please request a new one.");
            return;
        }

        // Verify code
        if (!code.equals(generatedCode)) {
            showError("Invalid reset code");
            return;
        }

        // Disable button to prevent multiple clicks
        resetButton.setDisable(true);
        resetButton.setText("Resetting...");

        // Reset password in background thread
        new Thread(() -> {
            User user = userService.getByEmail(userEmail);

            if (user != null) {
                // Update password (UserService will hash it)
                boolean success = userService.updatePassword(user.getId(), newPassword);

                Platform.runLater(() -> {
                    if (success) {
                        showSuccess("Password reset successful! Redirecting to login...");

                        // Navigate to login after 2 seconds
                        new Thread(() -> {
                            try {
                                Thread.sleep(2000);
                                Platform.runLater(this::handleBackToLogin);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    } else {
                        showError("Failed to reset password. Please try again.");
                        resetButton.setDisable(false);
                        resetButton.setText("Reset Password");
                    }
                });

            } else {
                Platform.runLater(() -> {
                    showError("User not found. Please try again.");
                    resetButton.setDisable(false);
                    resetButton.setText("Reset Password");
                });
            }
        }).start();
    }

    @FXML
    private void handleResendCode() {
        // Go back to email step
        codeStep.setVisible(false);
        codeStep.setManaged(false);
        emailStep.setVisible(true);
        emailStep.setManaged(true);

        // Clear fields
        codeField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();

        showInfo("Please enter your email to receive a new reset code");
    }

    @FXML
    private void handleBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("AgriCloud - Login");
        } catch (Exception e) {
            showError("Failed to load login page");
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
        messageLabel.setVisible(true);
    }

    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
        messageLabel.setVisible(true);
    }

    private void showInfo(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");
        messageLabel.setVisible(true);
    }
}
