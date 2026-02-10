package esprit.farouk.controllers;

import esprit.farouk.models.User;
import esprit.farouk.services.RoleService;
import esprit.farouk.services.UserService;
import esprit.farouk.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {

    @FXML
    private Label userNameLabel;

    @FXML
    private Label userRoleLabel;

    @FXML
    private Label pageTitle;

    @FXML
    private Label dateTimeLabel;

    @FXML
    private StackPane contentPane;

    @FXML
    private Label totalUsersLabel;

    @FXML
    private Label activeUsersLabel;

    @FXML
    private Label totalRolesLabel;

    @FXML
    private Button dashboardBtn;

    @FXML
    private Button usersBtn;

    @FXML
    private Button rolesBtn;

    @FXML
    private Button profileBtn;

    @FXML
    private Button logoutBtn;

    private UserService userService;
    private RoleService roleService;

    @FXML
    public void initialize() {
        userService = new UserService();
        roleService = new RoleService();

        // Load current user info
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            userNameLabel.setText("Welcome, " + currentUser.getName());
            userRoleLabel.setText("Role: " + currentUser.getRoleName());
        }

        // Update date/time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        dateTimeLabel.setText(LocalDateTime.now().format(formatter));

        // Load dashboard stats
        loadDashboardStats();

        // Hide admin-only menu items for non-admin users
        if (!SessionManager.isAdmin()) {
            usersBtn.setVisible(false);
            rolesBtn.setVisible(false);
        }
    }

    private void loadDashboardStats() {
        try {
            // Get all users
            List<esprit.farouk.models.User> allUsers = userService.getAll();
            totalUsersLabel.setText(String.valueOf(allUsers.size()));

            // Count active users
            long activeCount = allUsers.stream()
                    .filter(u -> "active".equals(u.getStatus()))
                    .count();
            activeUsersLabel.setText(String.valueOf(activeCount));

            // Get all roles
            var allRoles = roleService.getAll();
            totalRolesLabel.setText(String.valueOf(allRoles.size()));

        } catch (Exception e) {
            System.err.println("Failed to load dashboard stats: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void showDashboard() {
        pageTitle.setText("Dashboard");
        setActiveButton(dashboardBtn);
        // Reload stats
        loadDashboardStats();
    }

    @FXML
    private void showUsers() {
        pageTitle.setText("Users Management");
        setActiveButton(usersBtn);
        loadContentPane("/fxml/users.fxml");
    }

    @FXML
    private void showRoles() {
        pageTitle.setText("Roles Management");
        setActiveButton(rolesBtn);
        showAlert("Info", "Roles management coming soon!", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void showProfile() {
        pageTitle.setText("My Profile");
        setActiveButton(profileBtn);
        showAlert("Info", "Profile management coming soon!", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleLogout() {
        // Confirm logout
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Logout");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("You will be returned to the login screen.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            // Logout user
            SessionManager.logout();

            // Navigate back to login
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) logoutBtn.getScene().getWindow();
                Scene scene = new Scene(root, 900, 600);
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
                stage.setScene(scene);
                stage.setTitle("AgriCloud - Login");

                System.out.println("✓ User logged out successfully");
            } catch (Exception e) {
                System.err.println("Failed to return to login screen");
                e.printStackTrace();
            }
        }
    }

    private void setActiveButton(Button activeButton) {
        // Remove active class from all buttons
        dashboardBtn.getStyleClass().remove("sidebar-button-active");
        usersBtn.getStyleClass().remove("sidebar-button-active");
        rolesBtn.getStyleClass().remove("sidebar-button-active");
        profileBtn.getStyleClass().remove("sidebar-button-active");

        // Add active class to clicked button
        if (!activeButton.getStyleClass().contains("sidebar-button-active")) {
            activeButton.getStyleClass().add("sidebar-button-active");
        }
    }

    private void loadContentPane(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Node content = loader.load();
            contentPane.getChildren().clear();
            contentPane.getChildren().add(content);
        } catch (Exception e) {
            System.err.println("Failed to load content: " + fxmlPath);
            e.printStackTrace();
            showAlert("Error", "Failed to load content", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
