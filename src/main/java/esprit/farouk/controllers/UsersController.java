package esprit.farouk.controllers;

import esprit.farouk.models.Role;
import esprit.farouk.models.User;
import esprit.farouk.services.RoleService;
import esprit.farouk.services.UserService;
import esprit.farouk.utils.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.Optional;

public class UsersController {

    @FXML
    private TextField searchField;

    @FXML
    private Button addButton;

    @FXML
    private Button refreshButton;

    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, Long> idColumn;

    @FXML
    private TableColumn<User, String> nameColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> phoneColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TableColumn<User, String> statusColumn;

    @FXML
    private TableColumn<User, Void> actionsColumn;

    @FXML
    private Label statsLabel;

    @FXML
    private Label activeStatsLabel;

    @FXML
    private Label blockedStatsLabel;

    private UserService userService;
    private RoleService roleService;
    private ObservableList<User> usersData;

    @FXML
    public void initialize() {
        userService = new UserService();
        roleService = new RoleService();
        usersData = FXCollections.observableArrayList();

        // Set up actions column with buttons
        setupActionsColumn();

        // Load users
        loadUsers();

        // Add search listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterUsers(newValue));
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("✏ Edit");
            private final Button deleteBtn = new Button("🗑 Delete");
            private final Button blockBtn = new Button("🚫 Block");
            private final Button unblockBtn = new Button("✓ Unblock");
            private final HBox actionBox = new HBox(5);

            {
                editBtn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand;");
                blockBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-cursor: hand;");
                unblockBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-cursor: hand;");

                editBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleEditUser(user);
                });

                deleteBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleDeleteUser(user);
                });

                blockBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleBlockUser(user);
                });

                unblockBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleUnblockUser(user);
                });

                actionBox.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());

                    actionBox.getChildren().clear();
                    actionBox.getChildren().addAll(editBtn, deleteBtn);

                    // Add block/unblock button based on status
                    if ("blocked".equals(user.getStatus())) {
                        actionBox.getChildren().add(unblockBtn);
                    } else {
                        actionBox.getChildren().add(blockBtn);
                    }

                    setGraphic(actionBox);
                }
            }
        });
    }

    private void loadUsers() {
        List<User> users = userService.getAll();
        usersData.clear();
        usersData.addAll(users);
        usersTable.setItems(usersData);
        updateStats();
    }

    private void filterUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            loadUsers();
            return;
        }

        List<User> filtered = userService.search(keyword);
        usersData.clear();
        usersData.addAll(filtered);
        usersTable.setItems(usersData);
        updateStats();
    }

    private void updateStats() {
        int total = usersData.size();
        long active = usersData.stream().filter(u -> "active".equals(u.getStatus())).count();
        long blocked = usersData.stream().filter(u -> "blocked".equals(u.getStatus())).count();

        statsLabel.setText("Total Users: " + total);
        activeStatsLabel.setText("Active: " + active);
        blockedStatsLabel.setText("Blocked: " + blocked);
    }

    @FXML
    private void handleAddUser() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Add New User");
        dialog.setHeaderText("Create a new user account");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Create form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone (optional)");
        ComboBox<Role> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll(roleService.getAll());
        roleCombo.setPromptText("Select Role");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("Role:"), 0, 3);
        grid.add(roleCombo, 1, 3);
        grid.add(new Label("Password:"), 0, 4);
        grid.add(passwordField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();
                Role role = roleCombo.getValue();
                String password = passwordField.getText();

                // Validate
                if (!ValidationUtils.isValidName(name) || !ValidationUtils.isValidEmail(email) ||
                    role == null || !ValidationUtils.isValidPassword(password)) {
                    showAlert("Validation Error", "Please fill all required fields correctly", Alert.AlertType.ERROR);
                    return null;
                }

                if (userService.emailExists(email)) {
                    showAlert("Error", "Email already exists!", Alert.AlertType.ERROR);
                    return null;
                }

                User user = new User(role.getId(), name, email, password);
                if (!phone.isEmpty()) user.setPhone(phone);
                user.setStatus("active");
                return user;
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();
        result.ifPresent(user -> {
            if (userService.create(user)) {
                showAlert("Success", "User created successfully!", Alert.AlertType.INFORMATION);
                loadUsers();
            } else {
                showAlert("Error", "Failed to create user", Alert.AlertType.ERROR);
            }
        });
    }

    private void handleEditUser(User user) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Update user information");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField(user.getName());
        TextField emailField = new TextField(user.getEmail());
        TextField phoneField = new TextField(user.getPhone() != null ? user.getPhone() : "");
        ComboBox<Role> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll(roleService.getAll());
        roleCombo.setValue(roleService.getById(user.getRoleId()));

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("Role:"), 0, 3);
        grid.add(roleCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                user.setName(nameField.getText().trim());
                user.setEmail(emailField.getText().trim());
                user.setPhone(phoneField.getText().trim());
                if (roleCombo.getValue() != null) {
                    user.setRoleId(roleCombo.getValue().getId());
                }
                return user;
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();
        result.ifPresent(updatedUser -> {
            if (userService.update(updatedUser)) {
                showAlert("Success", "User updated successfully!", Alert.AlertType.INFORMATION);
                loadUsers();
            } else {
                showAlert("Error", "Failed to update user", Alert.AlertType.ERROR);
            }
        });
    }

    private void handleDeleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete User");
        alert.setHeaderText("Are you sure you want to delete this user?");
        alert.setContentText("User: " + user.getName() + " (" + user.getEmail() + ")");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (userService.delete(user.getId())) {
                showAlert("Success", "User deleted successfully!", Alert.AlertType.INFORMATION);
                loadUsers();
            } else {
                showAlert("Error", "Failed to delete user", Alert.AlertType.ERROR);
            }
        }
    }

    private void handleBlockUser(User user) {
        if (userService.blockUser(user.getId())) {
            showAlert("Success", "User blocked successfully!", Alert.AlertType.INFORMATION);
            loadUsers();
        } else {
            showAlert("Error", "Failed to block user", Alert.AlertType.ERROR);
        }
    }

    private void handleUnblockUser(User user) {
        if (userService.unblockUser(user.getId())) {
            showAlert("Success", "User unblocked successfully!", Alert.AlertType.INFORMATION);
            loadUsers();
        } else {
            showAlert("Error", "Failed to unblock user", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadUsers();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
