package esprit.farouk.controllers;

import esprit.farouk.models.Role;
import esprit.farouk.models.User;
import esprit.farouk.services.RoleService;
import esprit.farouk.services.UserService;
import esprit.farouk.utils.ValidationUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;

public class RegisterController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private ComboBox<Role> roleComboBox;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    @FXML
    private Button registerButton;

    @FXML
    private Hyperlink loginLink;

    private UserService userService;
    private RoleService roleService;

    @FXML
    public void initialize() {
        userService = new UserService();
        roleService = new RoleService();

        // Load registration roles (Farmer and Customer only)
        loadRoles();

        // Hide error/success labels initially
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
    }

    private void loadRoles() {
        List<Role> registrationRoles = roleService.getRegistrationRoles();
        roleComboBox.getItems().addAll(registrationRoles);
    }

    @FXML
    private void handleRegister() {
        // Clear previous messages
        errorLabel.setVisible(false);
        successLabel.setVisible(false);

        // Get input values
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        Role selectedRole = roleComboBox.getValue();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate inputs
        if (!validateInputs(name, email, phone, selectedRole, password, confirmPassword)) {
            return;
        }

        // Check if email already exists
        if (userService.emailExists(email)) {
            showError("This email is already registered. Please use a different email or login.");
            return;
        }

        // Create new user
        User newUser = new User(selectedRole.getId(), name, email, password);
        if (!phone.isEmpty()) {
            newUser.setPhone(phone);
        }
        newUser.setStatus("active");

        // Save user to database
        boolean created = userService.create(newUser);

        if (created) {
            showSuccess("Account created successfully! You can now login.");

            // Clear form fields
            clearForm();

            // Navigate to login after 2 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(this::handleBackToLogin);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            showError("Failed to create account. Please try again.");
        }
    }

    private boolean validateInputs(String name, String email, String phone, Role role,
                                     String password, String confirmPassword) {
        // Validate name
        if (!ValidationUtils.isValidName(name)) {
            showError("Name must be at least 2 characters long");
            return false;
        }

        // Validate email
        if (ValidationUtils.isEmpty(email)) {
            showError("Email is required");
            return false;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            showError("Please enter a valid email address");
            return false;
        }

        // Validate phone (optional but must be valid if provided)
        if (!ValidationUtils.isValidPhone(phone)) {
            showError("Please enter a valid phone number");
            return false;
        }

        // Validate role
        if (role == null) {
            showError("Please select your role (Farmer or Customer)");
            return false;
        }

        // Validate password
        if (!ValidationUtils.isValidPassword(password)) {
            showError("Password must be at least 6 characters long");
            return false;
        }

        // Validate password confirmation
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return false;
        }

        return true;
    }

    @FXML
    private void handleBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) loginLink.getScene().getWindow();
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("AgriCloud - Login");
        } catch (Exception e) {
            showError("Failed to load login page");
            e.printStackTrace();
        }
    }

    private void clearForm() {
        nameField.clear();
        emailField.clear();
        phoneField.clear();
        roleComboBox.setValue(null);
        passwordField.clear();
        confirmPasswordField.clear();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        successLabel.setVisible(false);
    }

    private void showSuccess(String message) {
        successLabel.setText(message);
        successLabel.setVisible(true);
        errorLabel.setVisible(false);
    }
}
