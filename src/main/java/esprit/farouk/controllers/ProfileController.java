package esprit.farouk.controllers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import esprit.farouk.models.User;
import esprit.farouk.services.UserService;
import esprit.farouk.utils.SessionManager;
import esprit.farouk.utils.ValidationUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

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
    private ImageView qrCodeImageView;

    @FXML
    private Button downloadQrButton;

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

        // Regenerate QR code with latest profile data
        generateAndDisplayQR();
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

    // ========== QR Code Methods ==========

    /**
     * Builds a vCard 3.0 string from the current user.
     * vCard 3.0 is required for iPhone Camera app detection.
     */
    private String buildVCard(User user) {
        String fullName = user.getName() != null ? user.getName().trim() : "";
        String[] parts = fullName.split(" ", 2);
        String firstName = parts[0];
        String lastName = parts.length > 1 ? parts[1] : "";

        StringBuilder vc = new StringBuilder();
        vc.append("BEGIN:VCARD\r\n");
        vc.append("VERSION:3.0\r\n");
        vc.append("FN:").append(fullName).append("\r\n");
        vc.append("N:").append(lastName).append(";").append(firstName).append(";;;\r\n");
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            vc.append("EMAIL;TYPE=INTERNET:").append(user.getEmail()).append("\r\n");
        }
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            vc.append("TEL;TYPE=CELL:").append(user.getPhone()).append("\r\n");
        }
        vc.append("ORG:AgriCloud\r\n");
        if (user.getRoleName() != null) {
            vc.append("TITLE:").append(user.getRoleName()).append("\r\n");
        }
        vc.append("END:VCARD\r\n");
        return vc.toString();
    }

    /**
     * Encodes the given text as a QR code and returns a JavaFX Image.
     */
    private Image encodeQR(String content, int size) throws Exception {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 2);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        BitMatrix bitMatrix = new QRCodeWriter()
                .encode(content, BarcodeFormat.QR_CODE, size, size, hints);

        BufferedImage buffered = MatrixToImageWriter.toBufferedImage(bitMatrix);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(buffered, "PNG", baos);
        return new Image(new ByteArrayInputStream(baos.toByteArray()));
    }

    /**
     * Generates the vCard QR code and sets it on the ImageView.
     */
    private void generateAndDisplayQR() {
        try {
            String vcard = buildVCard(currentUser);
            Image qrImage = encodeQR(vcard, 200);
            qrCodeImageView.setImage(qrImage);
        } catch (Exception e) {
            System.err.println("Failed to generate QR code: " + e.getMessage());
        }
    }

    /**
     * Downloads the QR code as a PNG file chosen by the user.
     */
    @FXML
    private void handleDownloadQR() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save QR Code");
        String safeName = currentUser.getName() != null
                ? currentUser.getName().replaceAll("[^a-zA-Z0-9_-]", "_")
                : "contact";
        chooser.setInitialFileName(safeName + "_contact_qr.png");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));

        File file = chooser.showSaveDialog(downloadQrButton.getScene().getWindow());
        if (file == null) return;

        try {
            String vcard = buildVCard(currentUser);
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 2);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            BitMatrix bitMatrix = new QRCodeWriter()
                    .encode(vcard, BarcodeFormat.QR_CODE, 400, 400, hints);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", file.toPath());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("QR Code Saved");
            alert.setHeaderText(null);
            alert.setContentText("QR code saved to:\n" + file.getAbsolutePath());
            alert.showAndWait();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Save Failed");
            alert.setHeaderText(null);
            alert.setContentText("Could not save QR code: " + e.getMessage());
            alert.showAndWait();
        }
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
