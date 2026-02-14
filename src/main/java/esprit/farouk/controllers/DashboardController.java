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
    private javafx.scene.Node dashboardContent;

    @FXML
    public void initialize() {
        userService = new UserService();
        roleService = new RoleService();

        // Store the original dashboard content
        if (!contentPane.getChildren().isEmpty()) {
            dashboardContent = contentPane.getChildren().get(0);
        }

        // Load current user info
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            userNameLabel.setText("Welcome, " + currentUser.getName().toLowerCase());

            // Update date/time with role
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
            dateTimeLabel.setText(LocalDateTime.now().format(formatter) + " | " + currentUser.getRoleName());
        }

        // Load dashboard stats
        loadDashboardStats();

        // Hide admin-only menu items for non-admin users
        if (!SessionManager.isAdmin()) {
            usersBtn.setVisible(false);
            rolesBtn.setVisible(false);
        }

        // Add hover effects to sidebar buttons
        addHoverEffect(dashboardBtn);
        addHoverEffect(usersBtn);
        addHoverEffect(rolesBtn);
        addHoverEffect(profileBtn);
    }

    private void addHoverEffect(Button button) {
        button.setOnMouseEntered(e -> {
            if (!button.getStyle().contains("border-color")) { // Not the active button
                button.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-alignment: center-left; -fx-padding: 14px 20px; -fx-background-radius: 0; -fx-background-insets: 0;");
            }
        });

        button.setOnMouseExited(e -> {
            if (!button.getStyle().contains("border-color")) { // Not the active button
                button.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: center-left; -fx-padding: 14px 20px; -fx-background-radius: 0; -fx-background-insets: 0;");
            }
        });
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
        setActiveButton(dashboardBtn);

        // Restore the original dashboard content
        if (dashboardContent != null) {
            contentPane.getChildren().clear();
            contentPane.getChildren().add(dashboardContent);
        }

        // Reload stats
        loadDashboardStats();
    }

    @FXML
    private void showUsers() {
        setActiveButton(usersBtn);
        loadContentPane("/fxml/users.fxml");
    }

    @FXML
    private void showRoles() {
        setActiveButton(rolesBtn);
        loadContentPane("/fxml/roles.fxml");
    }

    @FXML
    private void showProfile() {
        setActiveButton(profileBtn);
        loadContentPane("/fxml/profile.fxml");
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
        // Reset all buttons to inactive style
        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: center-left; -fx-padding: 14px 20px; -fx-background-radius: 0; -fx-background-insets: 0;";
        String activeStyle = "-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-alignment: center-left; -fx-padding: 14px 20px; -fx-border-width: 0 0 0 4px; -fx-border-color: #66bb6a; -fx-background-radius: 0; -fx-background-insets: 0;";

        dashboardBtn.setStyle(inactiveStyle);
        usersBtn.setStyle(inactiveStyle);
        rolesBtn.setStyle(inactiveStyle);
        profileBtn.setStyle(inactiveStyle);

        // Set active button style
        activeButton.setStyle(activeStyle);
    }

    private void loadContentPane(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Node content = loader.load();

            // Apply stylesheet to dynamically loaded content
            if (content instanceof javafx.scene.Parent) {
                javafx.scene.Parent parent = (javafx.scene.Parent) content;
                parent.getStylesheets().clear();
                parent.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            }

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
