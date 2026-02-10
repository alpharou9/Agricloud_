package esprit.farouk.controllers;

import esprit.farouk.models.Role;
import esprit.farouk.services.RoleService;
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

public class RolesController {

    @FXML
    private Button addButton;

    @FXML
    private Button refreshButton;

    @FXML
    private TableView<Role> rolesTable;

    @FXML
    private TableColumn<Role, Long> idColumn;

    @FXML
    private TableColumn<Role, String> nameColumn;

    @FXML
    private TableColumn<Role, String> descriptionColumn;

    @FXML
    private TableColumn<Role, Void> actionsColumn;

    @FXML
    private Label statsLabel;

    private RoleService roleService;
    private ObservableList<Role> rolesData;

    // System roles that cannot be deleted
    private final List<String> PROTECTED_ROLES = List.of("Admin", "Farmer", "Customer", "Guest");

    @FXML
    public void initialize() {
        roleService = new RoleService();
        rolesData = FXCollections.observableArrayList();

        // Set up actions column
        setupActionsColumn();

        // Load roles
        loadRoles();
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("✏ Edit");
            private final Button deleteBtn = new Button("🗑 Delete");
            private final Button viewUsersBtn = new Button("👥 View Users");
            private final HBox actionBox = new HBox(5);

            {
                // Modern button styling with gradients and shadows
                editBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #42a5f5, #2196f3); -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8px; -fx-padding: 8px 16px; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");
                deleteBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #ef5350, #f44336); -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8px; -fx-padding: 8px 16px; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");
                viewUsersBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #66bb6a, #4caf50); -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8px; -fx-padding: 6px 12px; -fx-font-weight: bold; -fx-font-size: 11px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");

                editBtn.setOnAction(event -> {
                    Role role = getTableView().getItems().get(getIndex());
                    handleEditRole(role);
                });

                deleteBtn.setOnAction(event -> {
                    Role role = getTableView().getItems().get(getIndex());
                    handleDeleteRole(role);
                });

                viewUsersBtn.setOnAction(event -> {
                    Role role = getTableView().getItems().get(getIndex());
                    handleViewUsers(role);
                });

                actionBox.setAlignment(Pos.CENTER);
                actionBox.getChildren().addAll(editBtn, viewUsersBtn, deleteBtn);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Role role = getTableView().getItems().get(getIndex());

                    // Disable delete for protected roles
                    if (PROTECTED_ROLES.contains(role.getName())) {
                        deleteBtn.setDisable(true);
                        deleteBtn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #999; -fx-opacity: 0.6; -fx-background-radius: 8px; -fx-padding: 8px 16px;");
                    } else {
                        deleteBtn.setDisable(false);
                        deleteBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #ef5350, #f44336); -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8px; -fx-padding: 8px 16px; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");
                    }

                    // Add hover effects
                    editBtn.setOnMouseEntered(e -> editBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #1e88e5, #1976d2); -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8px; -fx-padding: 8px 16px; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 3); -fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
                    editBtn.setOnMouseExited(e -> editBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #42a5f5, #2196f3); -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8px; -fx-padding: 8px 16px; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"));

                    if (!PROTECTED_ROLES.contains(role.getName())) {
                        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #e53935, #d32f2f); -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8px; -fx-padding: 8px 16px; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 3); -fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
                        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #ef5350, #f44336); -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8px; -fx-padding: 8px 16px; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"));
                    }

                    viewUsersBtn.setOnMouseEntered(e -> viewUsersBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #5cb860, #45a049); -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8px; -fx-padding: 6px 12px; -fx-font-weight: bold; -fx-font-size: 11px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 3); -fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
                    viewUsersBtn.setOnMouseExited(e -> viewUsersBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #66bb6a, #4caf50); -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8px; -fx-padding: 6px 12px; -fx-font-weight: bold; -fx-font-size: 11px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"));

                    setGraphic(actionBox);
                }
            }
        });
    }

    private void loadRoles() {
        List<Role> roles = roleService.getAll();
        rolesData.clear();
        rolesData.addAll(roles);
        rolesTable.setItems(rolesData);
        updateStats();
    }

    private void updateStats() {
        statsLabel.setText("Total Roles: " + rolesData.size());
    }

    @FXML
    private void handleAddRole() {
        Dialog<Role> dialog = new Dialog<>();
        dialog.setTitle("Add New Role");
        dialog.setHeaderText("Create a new role");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Create form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField();
        nameField.setPromptText("Role Name");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Role Description");
        descriptionArea.setPrefRowCount(3);
        TextField permissionsField = new TextField();
        permissionsField.setPromptText("[\"permission1\", \"permission2\"]");
        permissionsField.setText("[]");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionArea, 1, 1);
        grid.add(new Label("Permissions:"), 0, 2);
        grid.add(permissionsField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                String name = nameField.getText().trim();
                String description = descriptionArea.getText().trim();
                String permissions = permissionsField.getText().trim();

                // Validate
                if (ValidationUtils.isEmpty(name) || ValidationUtils.isEmpty(description)) {
                    showAlert("Validation Error", "Name and description are required", Alert.AlertType.ERROR);
                    return null;
                }

                if (roleService.nameExists(name)) {
                    showAlert("Error", "Role name already exists!", Alert.AlertType.ERROR);
                    return null;
                }

                Role role = new Role(name, description);
                role.setPermissions(permissions);
                return role;
            }
            return null;
        });

        Optional<Role> result = dialog.showAndWait();
        result.ifPresent(role -> {
            if (roleService.create(role)) {
                showAlert("Success", "Role created successfully!", Alert.AlertType.INFORMATION);
                loadRoles();
            } else {
                showAlert("Error", "Failed to create role", Alert.AlertType.ERROR);
            }
        });
    }

    private void handleEditRole(Role role) {
        Dialog<Role> dialog = new Dialog<>();
        dialog.setTitle("Edit Role");
        dialog.setHeaderText("Update role information");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField(role.getName());
        TextArea descriptionArea = new TextArea(role.getDescription());
        descriptionArea.setPrefRowCount(3);
        TextField permissionsField = new TextField(role.getPermissions() != null ? role.getPermissions() : "[]");

        // Disable name editing for protected roles
        if (PROTECTED_ROLES.contains(role.getName())) {
            nameField.setEditable(false);
            nameField.setStyle("-fx-background-color: #f5f5f5; -fx-opacity: 0.7;");
        }

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionArea, 1, 1);
        grid.add(new Label("Permissions:"), 0, 2);
        grid.add(permissionsField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                role.setName(nameField.getText().trim());
                role.setDescription(descriptionArea.getText().trim());
                role.setPermissions(permissionsField.getText().trim());
                return role;
            }
            return null;
        });

        Optional<Role> result = dialog.showAndWait();
        result.ifPresent(updatedRole -> {
            if (roleService.update(updatedRole)) {
                showAlert("Success", "Role updated successfully!", Alert.AlertType.INFORMATION);
                loadRoles();
            } else {
                showAlert("Error", "Failed to update role", Alert.AlertType.ERROR);
            }
        });
    }

    private void handleDeleteRole(Role role) {
        // Check if role is protected
        if (PROTECTED_ROLES.contains(role.getName())) {
            showAlert("Cannot Delete", "System roles cannot be deleted", Alert.AlertType.WARNING);
            return;
        }

        // Check if role can be deleted (no users assigned)
        if (!roleService.canDelete(role.getId())) {
            showAlert("Cannot Delete", "Cannot delete role because users are assigned to it. " +
                    "Please reassign or remove those users first.", Alert.AlertType.WARNING);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Role");
        alert.setHeaderText("Are you sure you want to delete this role?");
        alert.setContentText("Role: " + role.getName());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (roleService.delete(role.getId())) {
                showAlert("Success", "Role deleted successfully!", Alert.AlertType.INFORMATION);
                loadRoles();
            } else {
                showAlert("Error", "Failed to delete role", Alert.AlertType.ERROR);
            }
        }
    }

    private void handleViewUsers(Role role) {
        // Count users with this role
        int userCount = 0;
        try {
            userCount = new esprit.farouk.services.UserService().countByRole(role.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Role Users");
        alert.setHeaderText("Users with role: " + role.getName());
        alert.setContentText("Total users: " + userCount + "\n\n" +
                "Description: " + role.getDescription() + "\n\n" +
                "To manage users, go to Users Management page.");
        alert.showAndWait();
    }

    @FXML
    private void handleRefresh() {
        loadRoles();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
