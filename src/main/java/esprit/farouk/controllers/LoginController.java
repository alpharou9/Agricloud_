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
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink registerLink;

    @FXML
    private Hyperlink forgotPasswordLink;

    @FXML
    private Button guestButton;

    private UserService userService;

    @FXML
    public void initialize() {
        userService = new UserService();
        errorLabel.setVisible(false);

        // Add Enter key handler for password field
        passwordField.setOnAction(event -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        // Clear previous error
        errorLabel.setVisible(false);

        // Get input values
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validate inputs
        if (ValidationUtils.isEmpty(email)) {
            showError("Please enter your email");
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            showError("Please enter a valid email address");
            return;
        }

        if (ValidationUtils.isEmpty(password)) {
            showError("Please enter your password");
            return;
        }

        // Authenticate user
        User user = userService.authenticate(email, password);

        if (user == null) {
            showError("Invalid email or password");
            return;
        }

        // Check if user is blocked
        if ("blocked".equals(user.getStatus())) {
            showError("Your account has been blocked. Please contact support.");
            return;
        }

        // Login successful - set session
        SessionManager.setCurrentUser(user);

        // Navigate to dashboard
        navigateToDashboard();
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) registerLink.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("AgriCloud - Register");
        } catch (Exception e) {
            showError("Failed to load registration page");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleForgotPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/forgot_password.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) forgotPasswordLink.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("AgriCloud - Forgot Password");
        } catch (Exception e) {
            showError("Failed to load forgot password page");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGoogleLogin() {
        try {
            // Show Google OAuth login dialog
            OAuthLoginDialog oauthDialog = new OAuthLoginDialog();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            User user = oauthDialog.showGoogleLogin(stage);

            if (user != null) {
                // Check if user is blocked
                if ("blocked".equals(user.getStatus())) {
                    showError("Your account has been blocked. Please contact support.");
                    return;
                }

                // Login successful - set session
                SessionManager.setCurrentUser(user);

                // Navigate to dashboard
                navigateToDashboard();
            }
        } catch (Exception e) {
            showError("Google sign-in failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFaceLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/face_login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root, 900, 700);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("AgriCloud - Face Login");
        } catch (Exception e) {
            showError("Failed to load face login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGuestLogin() {
        // TODO: Implement guest login
        showError("Guest login coming soon!");
    }

    private void navigateToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("AgriCloud - Dashboard");

            System.out.println("✓ Navigated to dashboard for: " + SessionManager.getCurrentUser().getName());
        } catch (Exception e) {
            showError("Failed to load dashboard");
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
