package esprit.farouk.controllers;

import esprit.farouk.models.User;
import esprit.farouk.services.UserService;
import esprit.farouk.utils.SessionManager;
import esprit.farouk.utils.ValidationUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.time.format.DateTimeFormatter;

public class ProfileController {

    @FXML
    private Label statusBadge;

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField roleField;

    @FXML
    private Label profileErrorLabel;

    @FXML
    private Label profileSuccessLabel;

    @FXML
    private Button saveProfileButton;

    @FXML
    private PasswordField currentPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label passwordStrengthLabel;

    @FXML
    private Label passwordErrorLabel;

    @FXML
    private Label passwordSuccessLabel;

    @FXML
    private Button changePasswordButton;

    @FXML
    private Label createdAtLabel;

    @FXML
    private Label updatedAtLabel;

    @FXML
    private Label userIdLabel;

    @FXML
    private VBox faceRecognitionSection;

    @FXML
    private Label faceStatusLabel;

    @FXML
    private Label faceErrorLabel;

    @FXML
    private Label faceSuccessLabel;

    @FXML
    private Button setupFaceButton;

    @FXML
    private Button removeFaceButton;

    private UserService userService;
    private User currentUser;

    @FXML
    public void initialize() {
        userService = new UserService();
        currentUser = SessionManager.getCurrentUser();

        if (currentUser != null) {
            loadUserData();
            initializeFaceRecognition();
        }

        // Add password strength indicator
        newPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            updatePasswordStrength(newValue);
        });

        // Hide all messages initially
        hideAllMessages();
    }

    private void loadUserData() {
        // Refresh user data from database
        currentUser = userService.getById(currentUser.getId());
        if (currentUser == null) {
            showProfileError("Failed to load user data");
            return;
        }

        // Populate fields
        nameField.setText(currentUser.getName());
        emailField.setText(currentUser.getEmail());
        phoneField.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "");
        roleField.setText(currentUser.getRoleName());

        // Update status badge
        String status = currentUser.getStatus();
        statusBadge.setText(status.substring(0, 1).toUpperCase() + status.substring(1));
        if ("active".equals(status)) {
            statusBadge.setStyle("-fx-padding: 5 15; -fx-background-color: #e8f5e9; -fx-background-radius: 15; -fx-text-fill: #2e7d32;");
        } else if ("blocked".equals(status)) {
            statusBadge.setStyle("-fx-padding: 5 15; -fx-background-color: #ffebee; -fx-background-radius: 15; -fx-text-fill: #c62828;");
        }

        // Format and display dates
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        if (currentUser.getCreatedAt() != null) {
            createdAtLabel.setText(currentUser.getCreatedAt().format(formatter));
        }
        if (currentUser.getUpdatedAt() != null) {
            updatedAtLabel.setText(currentUser.getUpdatedAt().format(formatter));
        }
        userIdLabel.setText("#" + currentUser.getId());
    }

    @FXML
    private void handleSaveProfile() {
        hideAllMessages();

        // Get input values
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        // Validate inputs
        if (!ValidationUtils.isValidName(name)) {
            showProfileError("Name must be at least 2 characters long");
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            showProfileError("Please enter a valid email address");
            return;
        }

        if (!ValidationUtils.isValidPhone(phone)) {
            showProfileError("Please enter a valid phone number");
            return;
        }

        // Check if email changed and if new email already exists
        if (!email.equals(currentUser.getEmail())) {
            if (userService.emailExists(email)) {
                showProfileError("This email is already taken by another user");
                return;
            }
        }

        // Update user object
        currentUser.setName(name);
        currentUser.setEmail(email);
        currentUser.setPhone(phone.isEmpty() ? null : phone);

        // Save to database
        boolean updated = userService.update(currentUser);

        if (updated) {
            // Update session
            SessionManager.setCurrentUser(currentUser);

            showProfileSuccess("Profile updated successfully!");

            // Reload data to get updated timestamp
            loadUserData();
        } else {
            showProfileError("Failed to update profile. Please try again.");
        }
    }

    @FXML
    private void handleChangePassword() {
        hideAllMessages();

        // Get password values
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate inputs
        if (ValidationUtils.isEmpty(currentPassword)) {
            showPasswordError("Please enter your current password");
            return;
        }

        if (!ValidationUtils.isValidPassword(newPassword)) {
            showPasswordError("New password must be at least 6 characters long");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showPasswordError("New passwords do not match");
            return;
        }

        if (currentPassword.equals(newPassword)) {
            showPasswordError("New password must be different from current password");
            return;
        }

        // Verify current password
        if (!BCrypt.checkpw(currentPassword, currentUser.getPassword())) {
            showPasswordError("Current password is incorrect");
            return;
        }

        // Update password
        boolean updated = userService.updatePassword(currentUser.getId(), newPassword);

        if (updated) {
            showPasswordSuccess("Password changed successfully!");

            // Clear password fields
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
            passwordStrengthLabel.setVisible(false);

            // Reload user data
            currentUser = userService.getById(currentUser.getId());
            SessionManager.setCurrentUser(currentUser);
        } else {
            showPasswordError("Failed to change password. Please try again.");
        }
    }

    private void updatePasswordStrength(String password) {
        if (password.isEmpty()) {
            passwordStrengthLabel.setVisible(false);
            return;
        }

        passwordStrengthLabel.setVisible(true);

        if (password.length() < 6) {
            passwordStrengthLabel.setText("⚠ Weak (too short)");
            passwordStrengthLabel.setStyle("-fx-text-fill: #f44336;");
        } else if (password.length() < 8) {
            passwordStrengthLabel.setText("✓ Fair");
            passwordStrengthLabel.setStyle("-fx-text-fill: #ff9800;");
        } else if (password.matches(".*[0-9].*") && password.matches(".*[a-zA-Z].*")) {
            passwordStrengthLabel.setText("✓✓ Strong");
            passwordStrengthLabel.setStyle("-fx-text-fill: #4caf50;");
        } else {
            passwordStrengthLabel.setText("✓ Good");
            passwordStrengthLabel.setStyle("-fx-text-fill: #2196f3;");
        }
    }

    private void hideAllMessages() {
        profileErrorLabel.setVisible(false);
        profileSuccessLabel.setVisible(false);
        passwordErrorLabel.setVisible(false);
        passwordSuccessLabel.setVisible(false);
    }

    private void showProfileError(String message) {
        profileErrorLabel.setText(message);
        profileErrorLabel.setVisible(true);
        profileSuccessLabel.setVisible(false);
    }

    private void showProfileSuccess(String message) {
        profileSuccessLabel.setText(message);
        profileSuccessLabel.setVisible(true);
        profileErrorLabel.setVisible(false);
    }

    private void showPasswordError(String message) {
        passwordErrorLabel.setText(message);
        passwordErrorLabel.setVisible(true);
        passwordSuccessLabel.setVisible(false);
    }

    private void showPasswordSuccess(String message) {
        passwordSuccessLabel.setText(message);
        passwordSuccessLabel.setVisible(true);
        passwordErrorLabel.setVisible(false);
    }

    // ========== Face Recognition Methods ==========

    /**
     * Initializes face recognition section (Admin only)
     */
    private void initializeFaceRecognition() {
        // Only show for Admin users
        if ("Admin".equalsIgnoreCase(currentUser.getRoleName())) {
            faceRecognitionSection.setVisible(true);
            faceRecognitionSection.setManaged(true);
            updateFaceStatus();
        }
    }

    /**
     * Updates face recognition status display
     */
    private void updateFaceStatus() {
        boolean hasFace = userService.hasFaceEnrollment(currentUser.getId());

        if (hasFace) {
            faceStatusLabel.setText("✓ Face recognition enabled");
            faceStatusLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
            setupFaceButton.setVisible(false);
            setupFaceButton.setManaged(false);
            removeFaceButton.setVisible(true);
            removeFaceButton.setManaged(true);
        } else {
            faceStatusLabel.setText("Face recognition not enrolled");
            faceStatusLabel.setStyle("-fx-text-fill: #666;");
            setupFaceButton.setVisible(true);
            setupFaceButton.setManaged(true);
            removeFaceButton.setVisible(false);
            removeFaceButton.setManaged(false);
        }
    }

    /**
     * Opens face enrollment dialog
     */
    @FXML
    private void handleSetupFace() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/face_enrollment.fxml"));
            Parent root = loader.load();

            FaceEnrollmentController controller = loader.getController();
            controller.initialize(currentUser.getId());

            Stage stage = new Stage();
            stage.setTitle("Face Recognition Enrollment");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getScene().getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            stage.setOnHidden(e -> updateFaceStatus());
            stage.showAndWait();

        } catch (Exception e) {
            showFaceError("Failed to open enrollment dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Removes face enrollment data
     */
    @FXML
    private void handleRemoveFace() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remove Face Data");
        alert.setHeaderText("Are you sure you want to remove your face data?");
        alert.setContentText("You will need to re-enroll to use face login again.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            boolean success = userService.removeFaceEnrollment(currentUser.getId());

            if (success) {
                showFaceSuccess("Face data removed successfully");
                updateFaceStatus();
            } else {
                showFaceError("Failed to remove face data");
            }
        }
    }

    private void showFaceError(String message) {
        faceErrorLabel.setText(message);
        faceErrorLabel.setVisible(true);
        faceSuccessLabel.setVisible(false);
    }

    private void showFaceSuccess(String message) {
        faceSuccessLabel.setText(message);
        faceSuccessLabel.setVisible(true);
        faceErrorLabel.setVisible(false);
    }
}
