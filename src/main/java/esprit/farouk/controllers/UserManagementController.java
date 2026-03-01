package esprit.farouk.controllers;

import esprit.farouk.models.Role;
import esprit.farouk.models.User;
import esprit.farouk.services.RoleService;
import esprit.farouk.services.UserService;
import esprit.farouk.utils.UIUtils;
import esprit.farouk.utils.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserManagementController {

    private static final String UPLOADS_DIR = "uploads/profile_pictures/";

    private final StackPane contentArea;
    private final User currentUser;
    private final Label userNameLabel;
    private final UserService userService = new UserService();
    private final RoleService roleService = new RoleService();

    public UserManagementController(StackPane contentArea, User currentUser, Label userNameLabel) {
        this.contentArea = contentArea;
        this.currentUser = currentUser;
        this.userNameLabel = userNameLabel;
    }

    public void showUsersView() {
        contentArea.getChildren().clear();

        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_LEFT);

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("User Management");
        title.getStyleClass().add("content-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("action-button-add");
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("action-button-edit");
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("action-button-delete");
        Button blockBtn = new Button("Block/Unblock");
        blockBtn.getStyleClass().add("action-button-block");

        header.getChildren().addAll(title, spacer, addBtn, editBtn, deleteBtn, blockBtn);

        // Search & Filter bar
        HBox filterBar = new HBox(10);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search by name or email...");
        searchField.getStyleClass().add("search-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        ComboBox<String> statusFilter = new ComboBox<>(FXCollections.observableArrayList("All", "Active", "Inactive", "Blocked"));
        statusFilter.setValue("All");
        statusFilter.getStyleClass().add("filter-combo");

        filterBar.getChildren().addAll(searchField, statusFilter);

        // Table
        TableView<User> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<User, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(60);

        TableColumn<User, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<User, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));

        TableColumn<User, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<User, Long> roleCol = new TableColumn<>("Role ID");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("roleId"));
        roleCol.setMaxWidth(80);

        table.getColumns().addAll(idCol, nameCol, emailCol, phoneCol, statusCol, roleCol);

        // Load data with FilteredList + SortedList
        ObservableList<User> masterData = FXCollections.observableArrayList();
        try {
            masterData.addAll(userService.getAll());
        } catch (SQLException e) {
            UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load users: " + e.getMessage());
        }

        FilteredList<User> filteredData = new FilteredList<>(masterData, p -> true);

        // Update predicate when search text or status filter changes
        searchField.textProperty().addListener((obs, oldVal, newVal) ->
                filteredData.setPredicate(user -> filterUser(user, newVal, statusFilter.getValue())));
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) ->
                filteredData.setPredicate(user -> filterUser(user, searchField.getText(), newVal)));

        SortedList<User> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        // Reload helper
        Runnable reloadTable = () -> {
            masterData.clear();
            try {
                masterData.addAll(userService.getAll());
            } catch (SQLException ex) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load users: " + ex.getMessage());
            }
        };

        // Button actions
        addBtn.setOnAction(e -> {
            showUserFormDialog(null);
            reloadTable.run();
        });

        editBtn.setOnAction(e -> {
            User selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to edit.");
                return;
            }
            showUserFormDialog(selected);
            reloadTable.run();
        });

        deleteBtn.setOnAction(e -> {
            User selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to delete.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete user \"" + selected.getName() + "\"?");
            confirm.setHeaderText("Confirm Deletion");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    userService.delete(selected.getId());
                    reloadTable.run();
                } catch (SQLException ex) {
                    UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete user: " + ex.getMessage());
                }
            }
        });

        blockBtn.setOnAction(e -> {
            User selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to block/unblock.");
                return;
            }
            if (currentUser != null && selected.getId() == currentUser.getId()) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "Action Denied", "You cannot block your own account.");
                return;
            }
            String newStatus = "blocked".equals(selected.getStatus()) ? "active" : "blocked";
            String action = "blocked".equals(newStatus) ? "block" : "unblock";
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to " + action + " \"" + selected.getName() + "\"?");
            confirm.setHeaderText("Confirm " + action.substring(0, 1).toUpperCase() + action.substring(1));
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    selected.setStatus(newStatus);
                    userService.update(selected);
                    reloadTable.run();
                } catch (SQLException ex) {
                    UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to " + action + " user: " + ex.getMessage());
                }
            }
        });

        container.getChildren().addAll(header, filterBar, table);
        contentArea.getChildren().add(container);
    }

    private boolean filterUser(User user, String searchText, String statusValue) {
        boolean matchesSearch = true;
        if (searchText != null && !searchText.trim().isEmpty()) {
            String lower = searchText.trim().toLowerCase();
            matchesSearch = (user.getName() != null && user.getName().toLowerCase().contains(lower))
                    || (user.getEmail() != null && user.getEmail().toLowerCase().contains(lower));
        }
        boolean matchesStatus = true;
        if (statusValue != null && !"All".equals(statusValue)) {
            matchesStatus = statusValue.toLowerCase().equals(user.getStatus());
        }
        return matchesSearch && matchesStatus;
    }

    public void showRolesView() {
        contentArea.getChildren().clear();

        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_LEFT);

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Role Management");
        title.getStyleClass().add("content-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("action-button-add");
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("action-button-edit");
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("action-button-delete");

        header.getChildren().addAll(title, spacer, addBtn, editBtn, deleteBtn);

        // Table
        TableView<Role> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Role, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(60);

        TableColumn<Role, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Role, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        table.getColumns().addAll(idCol, nameCol, descCol);

        loadRoles(table);

        // Button actions
        addBtn.setOnAction(e -> {
            showRoleFormDialog(null);
            loadRoles(table);
        });

        editBtn.setOnAction(e -> {
            Role selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a role to edit.");
                return;
            }
            showRoleFormDialog(selected);
            loadRoles(table);
        });

        deleteBtn.setOnAction(e -> {
            Role selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a role to delete.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete role \"" + selected.getName() + "\"?");
            confirm.setHeaderText("Confirm Deletion");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    roleService.delete(selected.getId());
                    loadRoles(table);
                } catch (SQLException ex) {
                    UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete role: " + ex.getMessage());
                }
            }
        });

        container.getChildren().addAll(header, table);
        contentArea.getChildren().add(container);
    }

    private void showUserFormDialog(User user) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(user == null ? "Add User" : "Edit User");
        dialog.setHeaderText(user == null ? "Create a new user" : "Edit user: " + user.getName());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField(user != null ? user.getName() : "");
        nameField.setPromptText("Name");
        TextField emailField = new TextField(user != null ? user.getEmail() : "");
        emailField.setPromptText("Email");
        TextField phoneField = new TextField(user != null ? user.getPhone() : "");
        phoneField.setPromptText("Phone");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList("active", "inactive", "blocked"));
        statusCombo.setValue(user != null ? user.getStatus() : "active");

        ComboBox<Role> roleCombo = new ComboBox<>();
        try {
            List<Role> roles = roleService.getAll();
            roleCombo.setItems(FXCollections.observableArrayList(roles));
            roleCombo.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Role item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getName());
                }
            });
            roleCombo.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Role item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getName());
                }
            });
            if (user != null) {
                for (Role r : roles) {
                    if (r.getId() == user.getRoleId()) {
                        roleCombo.setValue(r);
                        break;
                    }
                }
            } else if (!roles.isEmpty()) {
                roleCombo.setValue(roles.get(0));
            }
        } catch (SQLException e) {
            UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load roles: " + e.getMessage());
        }

        int row = 0;
        grid.add(new Label("Name:"), 0, row);
        grid.add(nameField, 1, row++);
        grid.add(new Label("Email:"), 0, row);
        grid.add(emailField, 1, row++);
        grid.add(new Label("Phone:"), 0, row);
        grid.add(phoneField, 1, row++);
        if (user == null) {
            grid.add(new Label("Password:"), 0, row);
            grid.add(passwordField, 1, row++);
        }
        grid.add(new Label("Status:"), 0, row);
        grid.add(statusCombo, 1, row++);
        grid.add(new Label("Role:"), 0, row);
        grid.add(roleCombo, 1, row);

        dialog.getDialogPane().setContent(grid);

        // Validation loop: re-show dialog if validation fails
        while (true) {
            Optional<ButtonType> result = dialog.showAndWait();
            if (!result.isPresent() || result.get() != ButtonType.OK) {
                break;
            }

            String nameVal = nameField.getText().trim();
            String emailVal = emailField.getText().trim();
            String phoneVal = phoneField.getText().trim();

            if (!ValidationUtils.isValidName(nameVal)) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", "Name must be at least 2 characters and contain only letters.");
                continue;
            }
            if (!ValidationUtils.isValidEmail(emailVal)) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid email address.");
                continue;
            }
            if (!ValidationUtils.isValidPhone(phoneVal)) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", "Phone must be 8-15 digits, optionally starting with +.");
                continue;
            }
            if (user == null && passwordField.getText().length() < 6) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", "Password must be at least 6 characters.");
                continue;
            }

            try {
                if (user == null) {
                    User newUser = new User();
                    newUser.setName(nameVal);
                    newUser.setEmail(emailVal);
                    newUser.setPhone(phoneVal);
                    newUser.setPassword(passwordField.getText());
                    newUser.setStatus(statusCombo.getValue());
                    newUser.setRoleId(roleCombo.getValue() != null ? roleCombo.getValue().getId() : 1);
                    userService.add(newUser);
                } else {
                    user.setName(nameVal);
                    user.setEmail(emailVal);
                    user.setPhone(phoneVal);
                    user.setStatus(statusCombo.getValue());
                    user.setRoleId(roleCombo.getValue() != null ? roleCombo.getValue().getId() : user.getRoleId());
                    userService.update(user);
                }
            } catch (SQLException e) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to save user: " + e.getMessage());
            }
            break;
        }
    }

    private void showRoleFormDialog(Role role) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(role == null ? "Add Role" : "Edit Role");
        dialog.setHeaderText(role == null ? "Create a new role" : "Edit role: " + role.getName());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField(role != null ? role.getName() : "");
        nameField.setPromptText("Role name");
        TextField descField = new TextField(role != null ? role.getDescription() : "");
        descField.setPromptText("Description");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (role == null) {
                    Role newRole = new Role();
                    newRole.setName(nameField.getText().trim());
                    newRole.setDescription(descField.getText().trim());
                    roleService.add(newRole);
                } else {
                    role.setName(nameField.getText().trim());
                    role.setDescription(descField.getText().trim());
                    roleService.update(role);
                }
            } catch (SQLException e) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to save role: " + e.getMessage());
            }
        }
    }

    public void showProfileView() {
        contentArea.getChildren().clear();

        VBox container = new VBox(20);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(30));

        Label title = new Label("My Profile");
        title.getStyleClass().add("content-title");

        // Profile picture
        ImageView profileImage = new ImageView();
        profileImage.setFitWidth(120);
        profileImage.setFitHeight(120);
        profileImage.setPreserveRatio(false);
        profileImage.setSmooth(true);
        Circle clip = new Circle(60, 60, 60);
        profileImage.setClip(clip);
        loadProfileImage(profileImage, currentUser.getProfilePicture());

        Button changePicBtn = new Button("Change Picture");
        changePicBtn.getStyleClass().add("profile-pic-button");
        final String[] selectedPicPath = {null};
        changePicBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose Profile Picture");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            File file = fileChooser.showOpenDialog(contentArea.getScene().getWindow());
            if (file != null) {
                selectedPicPath[0] = file.getAbsolutePath();
                profileImage.setImage(new Image(file.toURI().toString(), 120, 120, false, true));
            }
        });

        Button deletePicBtn = new Button("Remove Picture");
        deletePicBtn.getStyleClass().add("profile-pic-delete-button");
        final boolean[] deletePic = {false};
        deletePicBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to remove your profile picture?");
            confirm.setHeaderText("Remove Profile Picture");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                selectedPicPath[0] = null;
                deletePic[0] = true;
                profileImage.setImage(null);
                profileImage.setStyle("-fx-background-color: #e0e0e0;");
            }
        });

        HBox picButtons = new HBox(10, changePicBtn, deletePicBtn);
        picButtons.setAlignment(Pos.CENTER);

        VBox picBox = new VBox(10, profileImage, picButtons);
        picBox.setAlignment(Pos.CENTER);

        GridPane form = new GridPane();
        form.getStyleClass().add("profile-form");
        form.setHgap(15);
        form.setVgap(15);
        form.setPadding(new Insets(25));
        form.setMaxWidth(450);

        TextField nameField = new TextField(currentUser.getName());
        nameField.setPromptText("Full Name");
        nameField.getStyleClass().add("text-input");

        TextField emailField = new TextField(currentUser.getEmail());
        emailField.setPromptText("Email");
        emailField.getStyleClass().add("text-input");

        TextField phoneField = new TextField(currentUser.getPhone() != null ? currentUser.getPhone() : "");
        phoneField.setPromptText("Phone");
        phoneField.getStyleClass().add("text-input");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("New password (leave empty to keep current)");
        passwordField.getStyleClass().add("text-input");

        Label feedbackLabel = new Label();
        feedbackLabel.setVisible(false);
        feedbackLabel.setManaged(false);

        int row = 0;
        form.add(new Label("Name:"), 0, row);
        form.add(nameField, 1, row++);
        form.add(new Label("Email:"), 0, row);
        form.add(emailField, 1, row++);
        form.add(new Label("Phone:"), 0, row);
        form.add(phoneField, 1, row++);
        form.add(new Label("Password:"), 0, row);
        form.add(passwordField, 1, row++);

        Button saveBtn = new Button("Save");
        saveBtn.getStyleClass().add("profile-save-button");
        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            if (name.isEmpty() || email.isEmpty()) {
                feedbackLabel.setText("Name and email are required.");
                feedbackLabel.getStyleClass().setAll("error-label");
                feedbackLabel.setVisible(true);
                feedbackLabel.setManaged(true);
                return;
            }
            if (!ValidationUtils.isValidName(name)) {
                feedbackLabel.setText("Name must be at least 2 characters and contain only letters.");
                feedbackLabel.getStyleClass().setAll("error-label");
                feedbackLabel.setVisible(true);
                feedbackLabel.setManaged(true);
                return;
            }
            if (!ValidationUtils.isValidEmail(email)) {
                feedbackLabel.setText("Please enter a valid email address.");
                feedbackLabel.getStyleClass().setAll("error-label");
                feedbackLabel.setVisible(true);
                feedbackLabel.setManaged(true);
                return;
            }
            if (!ValidationUtils.isValidPhone(phone)) {
                feedbackLabel.setText("Phone must be 8-15 digits, optionally starting with +.");
                feedbackLabel.getStyleClass().setAll("error-label");
                feedbackLabel.setVisible(true);
                feedbackLabel.setManaged(true);
                return;
            }
            try {
                // Handle profile picture
                if (deletePic[0]) {
                    String oldPic = currentUser.getProfilePicture();
                    if (oldPic != null && !oldPic.isEmpty()) {
                        File oldFile = new File(oldPic);
                        oldFile.delete();
                    }
                    currentUser.setProfilePicture(null);
                    deletePic[0] = false;
                } else if (selectedPicPath[0] != null) {
                    String savedPath = saveProfilePicture(selectedPicPath[0], currentUser.getId());
                    currentUser.setProfilePicture(savedPath);
                }

                currentUser.setName(name);
                currentUser.setEmail(email);
                currentUser.setPhone(phone);
                userService.update(currentUser);

                String newPassword = passwordField.getText();
                if (!newPassword.isEmpty()) {
                    if (newPassword.length() < 6) {
                        feedbackLabel.setText("Password must be at least 6 characters.");
                        feedbackLabel.getStyleClass().setAll("error-label");
                        feedbackLabel.setVisible(true);
                        feedbackLabel.setManaged(true);
                        return;
                    }
                    userService.updatePassword(currentUser.getId(), newPassword);
                }

                userNameLabel.setText("Welcome, " + currentUser.getName());
                feedbackLabel.setText("Profile updated successfully!");
                feedbackLabel.getStyleClass().setAll("success-label");
                feedbackLabel.setVisible(true);
                feedbackLabel.setManaged(true);
                passwordField.clear();
                selectedPicPath[0] = null;
            } catch (SQLException ex) {
                feedbackLabel.setText("Failed to update profile: " + ex.getMessage());
                feedbackLabel.getStyleClass().setAll("error-label");
                feedbackLabel.setVisible(true);
                feedbackLabel.setManaged(true);
            } catch (IOException ex) {
                feedbackLabel.setText("Failed to save profile picture.");
                feedbackLabel.getStyleClass().setAll("error-label");
                feedbackLabel.setVisible(true);
                feedbackLabel.setManaged(true);
            }
        });

        form.add(saveBtn, 1, row);

        // Face Recognition section (All roles except Guest)
        VBox faceRecognitionBox = null;
        try {
            Role role = roleService.getById(currentUser.getRoleId());
            if (role != null && !"guest".equalsIgnoreCase(role.getName())) {
                faceRecognitionBox = createFaceRecognitionSection();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        if (faceRecognitionBox != null) {
            container.getChildren().addAll(title, picBox, form, feedbackLabel, faceRecognitionBox);
        } else {
            container.getChildren().addAll(title, picBox, form, feedbackLabel);
        }
        contentArea.getChildren().add(container);
    }

    private VBox createFaceRecognitionSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setMaxWidth(450);
        section.setStyle("-fx-background-color: -color-surface; -fx-background-radius: 12; " +
                         "-fx-border-color: -color-border; -fx-border-radius: 12; -fx-border-width: 1;");

        Label sectionTitle = new Label("Face Recognition");
        sectionTitle.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: -color-brand;");

        HBox statusRow = new HBox(10);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        Label statusLabel = new Label();
        Button actionButton = new Button();

        try {
            boolean hasEnrollment = userService.hasFaceEnrollment(currentUser.getId());

            if (hasEnrollment) {
                statusLabel.setText("✓ Face recognition enabled");
                statusLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");

                actionButton.setText("Remove Face Data");
                actionButton.getStyleClass().add("profile-pic-delete-button");
                actionButton.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Are you sure you want to remove your face recognition data?");
                    confirm.setHeaderText("Remove Face Data");
                    if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                        try {
                            userService.removeFaceEnrollment(currentUser.getId());
                            UIUtils.showAlert(Alert.AlertType.INFORMATION, "Success", "Face recognition data removed.");
                            showProfileView(); // Refresh view
                        } catch (SQLException ex) {
                            UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to remove face data: " + ex.getMessage());
                        }
                    }
                });
            } else {
                statusLabel.setText("Face recognition not enabled");
                statusLabel.setStyle("-fx-text-fill: #6c757d;");

                actionButton.setText("Setup Face Recognition");
                actionButton.getStyleClass().add("profile-pic-button");
                actionButton.setOnAction(e -> showFaceEnrollmentDialog());
            }

        } catch (SQLException ex) {
            statusLabel.setText("Error checking face enrollment status");
            statusLabel.setStyle("-fx-text-fill: #dc3545;");
            actionButton.setDisable(true);
        }

        statusRow.getChildren().addAll(statusLabel, actionButton);

        Label description = new Label(
                "Face recognition allows you to login quickly and securely by scanning your face.");
        description.setWrapText(true);
        description.setStyle("-fx-text-fill: -color-text-muted; -fx-font-size: 12;");

        section.getChildren().addAll(sectionTitle, statusRow, description);
        return section;
    }

    private void showFaceEnrollmentDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/face_enrollment.fxml"));
            javafx.scene.layout.VBox dialogContent = loader.load();

            FaceEnrollmentController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Stage dialog = new Stage();
            dialog.setTitle("Face Enrollment");
            dialog.setScene(new Scene(dialogContent));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(contentArea.getScene().getWindow());

            dialog.setOnHidden(e -> showProfileView()); // Refresh profile view when dialog closes

            dialog.showAndWait();

        } catch (IOException ex) {
            UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to open face enrollment: " + ex.getMessage());
        }
    }

    private String saveProfilePicture(String sourcePath, long userId) throws IOException {
        Path uploadsPath = Paths.get(UPLOADS_DIR);
        if (!Files.exists(uploadsPath)) {
            Files.createDirectories(uploadsPath);
        }
        String extension = sourcePath.substring(sourcePath.lastIndexOf('.'));
        String fileName = "user_" + userId + extension;
        Path destination = uploadsPath.resolve(fileName);
        Files.copy(Paths.get(sourcePath), destination, StandardCopyOption.REPLACE_EXISTING);
        return destination.toString();
    }

    private void loadProfileImage(ImageView imageView, String picturePath) {
        if (picturePath != null && !picturePath.isEmpty()) {
            File file = new File(picturePath);
            if (file.exists()) {
                imageView.setImage(new Image(file.toURI().toString(), 120, 120, false, true));
                return;
            }
        }
        // Gray placeholder when no picture
        imageView.setStyle("-fx-background-color: #e0e0e0;");
    }

    public void showStatisticsView() {
        contentArea.getChildren().clear();

        VBox container = new VBox(20);
        container.setAlignment(Pos.TOP_LEFT);
        container.setPadding(new Insets(10));

        Label title = new Label("User Statistics");
        title.getStyleClass().add("content-title");

        List<User> users;
        List<Role> roles;
        try {
            users = userService.getAll();
            roles = roleService.getAll();
        } catch (SQLException e) {
            UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load data: " + e.getMessage());
            return;
        }

        int total = users.size();
        long active = users.stream().filter(u -> "active".equals(u.getStatus())).count();
        long inactive = users.stream().filter(u -> "inactive".equals(u.getStatus())).count();
        long blocked = users.stream().filter(u -> "blocked".equals(u.getStatus())).count();

        // Stat cards
        HBox cards = new HBox(15);
        cards.setAlignment(Pos.CENTER_LEFT);
        cards.getChildren().addAll(
                UIUtils.createStatCard("Total Users", String.valueOf(total), "stat-card-total"),
                UIUtils.createStatCard("Active", String.valueOf(active), "stat-card-active"),
                UIUtils.createStatCard("Inactive", String.valueOf(inactive), "stat-card-inactive"),
                UIUtils.createStatCard("Blocked", String.valueOf(blocked), "stat-card-blocked")
        );

        // Charts row
        HBox chartsRow = new HBox(20);
        VBox.setVgrow(chartsRow, Priority.ALWAYS);

        // Pie chart - Users per role
        Map<Long, String> roleNames = new LinkedHashMap<>();
        for (Role r : roles) roleNames.put(r.getId(), r.getName());

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        Map<Long, Long> roleCounts = new LinkedHashMap<>();
        for (User u : users) {
            roleCounts.merge(u.getRoleId(), 1L, Long::sum);
        }
        for (Map.Entry<Long, Long> entry : roleCounts.entrySet()) {
            String name = roleNames.getOrDefault(entry.getKey(), "Role " + entry.getKey());
            pieData.add(new PieChart.Data(name + " (" + entry.getValue() + ")", entry.getValue()));
        }
        PieChart pieChart = new PieChart(pieData);
        pieChart.setTitle("Users per Role");
        pieChart.setLabelsVisible(true);
        pieChart.setMaxHeight(350);
        HBox.setHgrow(pieChart, Priority.ALWAYS);

        // Bar chart - Registrations over last 7 days
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Registrations");
        yAxis.setTickUnit(1);
        yAxis.setMinorTickVisible(false);

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Registrations (Last 7 Days)");
        barChart.setLegendVisible(false);
        barChart.setMaxHeight(350);
        HBox.setHgrow(barChart, Priority.ALWAYS);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd");
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            long count = users.stream()
                    .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().toLocalDate().equals(day))
                    .count();
            series.getData().add(new XYChart.Data<>(day.format(fmt), count));
        }
        barChart.getData().add(series);

        chartsRow.getChildren().addAll(pieChart, barChart);

        container.getChildren().addAll(title, cards, chartsRow);
        contentArea.getChildren().add(container);
    }

    private void loadRoles(TableView<Role> table) {
        try {
            table.setItems(FXCollections.observableArrayList(roleService.getAll()));
        } catch (SQLException e) {
            UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load roles: " + e.getMessage());
        }
    }
}
