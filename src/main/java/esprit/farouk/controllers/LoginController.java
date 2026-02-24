package esprit.farouk.controllers;

import esprit.farouk.models.Role;
import esprit.farouk.models.User;
import esprit.farouk.services.RoleService;
import esprit.farouk.services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private final UserService userService = new UserService();
    private final RoleService roleService = new RoleService();

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password.");
            return;
        }

        try {
            User user = userService.authenticate(email, password);
            if (user != null) {
                if ("blocked".equalsIgnoreCase(user.getStatus())) {
                    showError("Your account has been blocked. Please contact an administrator.");
                    return;
                }
                hideError();
                System.out.println("Login successful: " + user);

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
                Parent root = loader.load();
                DashboardController dashboardController = loader.getController();
                dashboardController.setCurrentUser(user);

                Stage stage = (Stage) emailField.getScene().getWindow();
                Scene scene = new Scene(root, 1100, 700);
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
                stage.setScene(scene);
            } else {
                showError("Invalid email or password.");
            }
        } catch (SQLException e) {
            showError("Database error. Please try again.");
            e.printStackTrace();
        } catch (Exception e) {
            showError("Failed to load dashboard.");
            e.printStackTrace();
        }
    }

    @FXML
    private void goToForgotPassword() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/forgot_password.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root, 800, 600);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToRegister() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/register.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root, 800, 600);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGuestLogin() {
        try {
            // Create unique guest user for this session
            User guestUser = userService.createUniqueGuestUser();

            if (guestUser == null) {
                showError("Failed to create guest session. Please contact administrator.");
                return;
            }

            hideError();
            System.out.println("Guest login successful: " + guestUser.getName() + " (ID: " + guestUser.getId() + ")");

            // Navigate to dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent root = loader.load();
            DashboardController dashboardController = loader.getController();
            dashboardController.setCurrentUser(guestUser);

            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root, 1100, 700);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (SQLException e) {
            showError("Database error. Please try again.");
            e.printStackTrace();
        } catch (Exception e) {
            showError("Failed to load dashboard.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFaceLogin() {
        try {
            hideError();

            // Navigate to face login screen
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/face_login.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root, 800, 650);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("AgriCloud - Face Recognition Login");

        } catch (Exception e) {
            showError("Failed to load face login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
