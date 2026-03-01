package esprit.farouk.controllers;

import esprit.farouk.models.Farm;
import esprit.farouk.models.Field;
import esprit.farouk.models.Role;
import esprit.farouk.models.User;
import esprit.farouk.services.FarmService;
import esprit.farouk.services.FieldService;
import esprit.farouk.services.RoleService;
import esprit.farouk.utils.UIUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

public class FarmController {

    private final StackPane contentArea;
    private final User currentUser;
    private final FarmService farmService = new FarmService();
    private final FieldService fieldService = new FieldService();
    private final RoleService roleService = new RoleService();

    public FarmController(StackPane contentArea, User currentUser) {
        this.contentArea = contentArea;
        this.currentUser = currentUser;
    }

    public void showFarmsView() {
        contentArea.getChildren().clear();

        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_LEFT);

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Farm Management");
        title.getStyleClass().add("content-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("action-button-add");
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("action-button-edit");
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("action-button-delete");
        Button approveBtn = new Button("Approve");
        approveBtn.getStyleClass().add("action-button-approve");
        Button rejectBtn = new Button("Reject");
        rejectBtn.getStyleClass().add("action-button-reject");
        Button fieldsBtn = new Button("Fields");
        fieldsBtn.getStyleClass().add("action-button-edit");

        header.getChildren().addAll(title, spacer, addBtn, editBtn, deleteBtn, approveBtn, rejectBtn, fieldsBtn);

        // Search & Filter bar
        HBox filterBar = new HBox(10);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search by name or location...");
        searchField.getStyleClass().add("search-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        ComboBox<String> statusFilter = new ComboBox<>(FXCollections.observableArrayList("All", "Pending", "Approved", "Rejected", "Inactive"));
        statusFilter.setValue("All");
        statusFilter.getStyleClass().add("filter-combo");

        filterBar.getChildren().addAll(searchField, statusFilter);

        // Table
        TableView<Farm> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Farm, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(60);

        TableColumn<Farm, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Farm, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));

        TableColumn<Farm, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("farmType"));

        TableColumn<Farm, Double> areaCol = new TableColumn<>("Area");
        areaCol.setCellValueFactory(new PropertyValueFactory<>("area"));
        areaCol.setMaxWidth(80);

        TableColumn<Farm, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Farm, Long> userCol = new TableColumn<>("User ID");
        userCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        userCol.setMaxWidth(80);

        table.getColumns().addAll(idCol, nameCol, locationCol, typeCol, areaCol, statusCol, userCol);

        // Load data with FilteredList + SortedList
        ObservableList<Farm> masterData = FXCollections.observableArrayList();
        try {
            masterData.addAll(farmService.getAll());
        } catch (SQLException e) {
            UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load farms: " + e.getMessage());
        }

        FilteredList<Farm> filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) ->
                filteredData.setPredicate(farm -> filterFarm(farm, newVal, statusFilter.getValue())));
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) ->
                filteredData.setPredicate(farm -> filterFarm(farm, searchField.getText(), newVal)));

        SortedList<Farm> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        // Reload helper
        Runnable reloadTable = () -> {
            masterData.clear();
            try {
                masterData.addAll(farmService.getAll());
            } catch (SQLException ex) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load farms: " + ex.getMessage());
            }
        };

        // Button actions
        addBtn.setOnAction(e -> {
            showFarmFormDialog(null, true);
            reloadTable.run();
        });

        editBtn.setOnAction(e -> {
            Farm selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a farm to edit.");
                return;
            }
            showFarmFormDialog(selected, true);
            reloadTable.run();
        });

        deleteBtn.setOnAction(e -> {
            Farm selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a farm to delete.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete farm \"" + selected.getName() + "\"?");
            confirm.setHeaderText("Confirm Deletion");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    farmService.delete(selected.getId());
                    reloadTable.run();
                } catch (SQLException ex) {
                    UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete farm: " + ex.getMessage());
                }
            }
        });

        approveBtn.setOnAction(e -> {
            Farm selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a farm to approve.");
                return;
            }
            if ("approved".equals(selected.getStatus())) {
                UIUtils.showAlert(Alert.AlertType.INFORMATION, "Already Approved", "This farm is already approved.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Approve farm \"" + selected.getName() + "\"?");
            confirm.setHeaderText("Confirm Approval");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    selected.setStatus("approved");
                    selected.setApprovedAt(LocalDateTime.now());
                    selected.setApprovedBy(currentUser.getId());
                    farmService.update(selected);
                    reloadTable.run();
                } catch (SQLException ex) {
                    UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to approve farm: " + ex.getMessage());
                }
            }
        });

        rejectBtn.setOnAction(e -> {
            Farm selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a farm to reject.");
                return;
            }
            if ("rejected".equals(selected.getStatus())) {
                UIUtils.showAlert(Alert.AlertType.INFORMATION, "Already Rejected", "This farm is already rejected.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Reject farm \"" + selected.getName() + "\"?");
            confirm.setHeaderText("Confirm Rejection");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    selected.setStatus("rejected");
                    selected.setApprovedAt(null);
                    selected.setApprovedBy(null);
                    farmService.update(selected);
                    reloadTable.run();
                } catch (SQLException ex) {
                    UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to reject farm: " + ex.getMessage());
                }
            }
        });

        fieldsBtn.setOnAction(e -> {
            Farm selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a farm to view its fields.");
                return;
            }
            showFieldsView(selected);
        });

        container.getChildren().addAll(header, filterBar, table);
        contentArea.getChildren().add(container);
    }

    private boolean filterFarm(Farm farm, String searchText, String statusValue) {
        boolean matchesSearch = true;
        if (searchText != null && !searchText.trim().isEmpty()) {
            String lower = searchText.trim().toLowerCase();
            matchesSearch = (farm.getName() != null && farm.getName().toLowerCase().contains(lower))
                    || (farm.getLocation() != null && farm.getLocation().toLowerCase().contains(lower));
        }
        boolean matchesStatus = true;
        if (statusValue != null && !"All".equals(statusValue)) {
            matchesStatus = statusValue.toLowerCase().equals(farm.getStatus());
        }
        return matchesSearch && matchesStatus;
    }

    public void showMyFarmsView() {
        contentArea.getChildren().clear();

        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_LEFT);

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("My Farms");
        title.getStyleClass().add("content-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("action-button-add");
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("action-button-edit");
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("action-button-delete");
        Button fieldsBtn = new Button("Fields");
        fieldsBtn.getStyleClass().add("action-button-edit");

        header.getChildren().addAll(title, spacer, addBtn, editBtn, deleteBtn, fieldsBtn);

        // Search & Filter bar
        HBox filterBar = new HBox(10);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search by name or location...");
        searchField.getStyleClass().add("search-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        ComboBox<String> statusFilter = new ComboBox<>(FXCollections.observableArrayList("All", "Pending", "Approved", "Rejected", "Inactive"));
        statusFilter.setValue("All");
        statusFilter.getStyleClass().add("filter-combo");

        filterBar.getChildren().addAll(searchField, statusFilter);

        // Table
        TableView<Farm> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Farm, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(60);

        TableColumn<Farm, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Farm, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));

        TableColumn<Farm, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("farmType"));

        TableColumn<Farm, Double> areaCol = new TableColumn<>("Area");
        areaCol.setCellValueFactory(new PropertyValueFactory<>("area"));
        areaCol.setMaxWidth(80);

        TableColumn<Farm, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(idCol, nameCol, locationCol, typeCol, areaCol, statusCol);

        // Load data - only current user's farms
        ObservableList<Farm> masterData = FXCollections.observableArrayList();
        try {
            masterData.addAll(farmService.getByUserId(currentUser.getId()));
        } catch (SQLException e) {
            UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load farms: " + e.getMessage());
        }

        FilteredList<Farm> filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) ->
                filteredData.setPredicate(farm -> filterFarm(farm, newVal, statusFilter.getValue())));
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) ->
                filteredData.setPredicate(farm -> filterFarm(farm, searchField.getText(), newVal)));

        SortedList<Farm> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        // Reload helper
        Runnable reloadTable = () -> {
            masterData.clear();
            try {
                masterData.addAll(farmService.getByUserId(currentUser.getId()));
            } catch (SQLException ex) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load farms: " + ex.getMessage());
            }
        };

        // Button actions
        addBtn.setOnAction(e -> {
            showFarmFormDialog(null, false);
            reloadTable.run();
        });

        editBtn.setOnAction(e -> {
            Farm selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a farm to edit.");
                return;
            }
            showFarmFormDialog(selected, false);
            reloadTable.run();
        });

        deleteBtn.setOnAction(e -> {
            Farm selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a farm to delete.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete farm \"" + selected.getName() + "\"?");
            confirm.setHeaderText("Confirm Deletion");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    farmService.delete(selected.getId());
                    reloadTable.run();
                } catch (SQLException ex) {
                    UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete farm: " + ex.getMessage());
                }
            }
        });

        fieldsBtn.setOnAction(e -> {
            Farm selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a farm to view its fields.");
                return;
            }
            showFieldsView(selected);
        });

        container.getChildren().addAll(header, filterBar, table);
        contentArea.getChildren().add(container);
    }

    private void showFarmFormDialog(Farm farm, boolean isAdmin) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(farm == null ? "Add Farm" : "Edit Farm");
        dialog.setHeaderText(farm == null ? "Create a new farm" : "Edit farm: " + farm.getName());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField(farm != null ? farm.getName() : "");
        nameField.setPromptText("Farm name");
        TextField locationField = new TextField(farm != null ? farm.getLocation() : "");
        locationField.setPromptText("Location");
        TextField latField = new TextField(farm != null && farm.getLatitude() != null ? farm.getLatitude().toString() : "");
        latField.setPromptText("Latitude (optional)");
        TextField lngField = new TextField(farm != null && farm.getLongitude() != null ? farm.getLongitude().toString() : "");
        lngField.setPromptText("Longitude (optional)");

        Button mapPickerBtn = new Button("Pick on Map");
        mapPickerBtn.getStyleClass().add("action-button-add");
        mapPickerBtn.setOnAction(ev -> showMapPickerDialog(latField, lngField));

        TextField areaField = new TextField(farm != null && farm.getArea() != null ? farm.getArea().toString() : "");
        areaField.setPromptText("Area in hectares (optional)");
        TextField typeField = new TextField(farm != null ? (farm.getFarmType() != null ? farm.getFarmType() : "") : "");
        typeField.setPromptText("Farm type (optional)");
        TextField descField = new TextField(farm != null ? (farm.getDescription() != null ? farm.getDescription() : "") : "");
        descField.setPromptText("Description (optional)");

        int row = 0;
        grid.add(new Label("Name:"), 0, row);
        grid.add(nameField, 1, row++);
        grid.add(new Label("Location:"), 0, row);
        grid.add(locationField, 1, row++);
        grid.add(new Label("Coordinates:"), 0, row);
        HBox coordBox = new HBox(8, latField, lngField, mapPickerBtn);
        coordBox.setAlignment(Pos.CENTER_LEFT);
        grid.add(coordBox, 1, row++);
        grid.add(new Label("Area:"), 0, row);
        grid.add(areaField, 1, row++);
        grid.add(new Label("Type:"), 0, row);
        grid.add(typeField, 1, row++);
        grid.add(new Label("Description:"), 0, row);
        grid.add(descField, 1, row++);

        // Admin-only fields: status and user selection
        ComboBox<String> statusCombo = null;
        TextField userIdField = null;
        if (isAdmin) {
            statusCombo = new ComboBox<>(FXCollections.observableArrayList("pending", "approved", "rejected", "inactive"));
            statusCombo.setValue(farm != null ? farm.getStatus() : "pending");
            grid.add(new Label("Status:"), 0, row);
            grid.add(statusCombo, 1, row++);

            userIdField = new TextField(farm != null ? String.valueOf(farm.getUserId()) : "");
            userIdField.setPromptText("User ID (owner)");
            grid.add(new Label("User ID:"), 0, row);
            grid.add(userIdField, 1, row);
        }

        dialog.getDialogPane().setContent(grid);

        final ComboBox<String> finalStatusCombo = statusCombo;
        final TextField finalUserIdField = userIdField;

        // Validation loop
        while (true) {
            Optional<ButtonType> result = dialog.showAndWait();
            if (!result.isPresent() || result.get() != ButtonType.OK) {
                break;
            }

            String nameVal = nameField.getText().trim();
            String locationVal = locationField.getText().trim();

            if (nameVal.length() < 2) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", "Farm name must be at least 2 characters.");
                continue;
            }
            if (locationVal.isEmpty()) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", "Location is required.");
                continue;
            }

            // Parse optional numeric fields
            Double latVal = null;
            Double lngVal = null;
            Double areaVal = null;

            if (!latField.getText().trim().isEmpty()) {
                try {
                    latVal = Double.parseDouble(latField.getText().trim());
                } catch (NumberFormatException ex) {
                    UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", "Latitude must be a valid number.");
                    continue;
                }
            }
            if (!lngField.getText().trim().isEmpty()) {
                try {
                    lngVal = Double.parseDouble(lngField.getText().trim());
                } catch (NumberFormatException ex) {
                    UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", "Longitude must be a valid number.");
                    continue;
                }
            }
            if (!areaField.getText().trim().isEmpty()) {
                try {
                    areaVal = Double.parseDouble(areaField.getText().trim());
                    if (areaVal < 0) {
                        UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", "Area must be a positive number.");
                        continue;
                    }
                } catch (NumberFormatException ex) {
                    UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", "Area must be a valid number.");
                    continue;
                }
            }

            long ownerUserId = currentUser.getId();
            if (isAdmin && finalUserIdField != null && !finalUserIdField.getText().trim().isEmpty()) {
                try {
                    ownerUserId = Long.parseLong(finalUserIdField.getText().trim());
                } catch (NumberFormatException ex) {
                    UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", "User ID must be a valid number.");
                    continue;
                }
            }

            try {
                if (farm == null) {
                    Farm newFarm = new Farm();
                    newFarm.setUserId(ownerUserId);
                    newFarm.setName(nameVal);
                    newFarm.setLocation(locationVal);
                    newFarm.setLatitude(latVal);
                    newFarm.setLongitude(lngVal);
                    newFarm.setArea(areaVal);
                    newFarm.setFarmType(typeField.getText().trim().isEmpty() ? null : typeField.getText().trim());
                    newFarm.setDescription(descField.getText().trim().isEmpty() ? null : descField.getText().trim());
                    newFarm.setStatus(isAdmin && finalStatusCombo != null ? finalStatusCombo.getValue() : "pending");
                    farmService.add(newFarm);
                } else {
                    farm.setName(nameVal);
                    farm.setLocation(locationVal);
                    farm.setLatitude(latVal);
                    farm.setLongitude(lngVal);
                    farm.setArea(areaVal);
                    farm.setFarmType(typeField.getText().trim().isEmpty() ? null : typeField.getText().trim());
                    farm.setDescription(descField.getText().trim().isEmpty() ? null : descField.getText().trim());
                    if (isAdmin) {
                        if (finalStatusCombo != null) farm.setStatus(finalStatusCombo.getValue());
                        if (finalUserIdField != null && !finalUserIdField.getText().trim().isEmpty()) {
                            farm.setUserId(ownerUserId);
                        }
                    }
                    farmService.update(farm);
                }
            } catch (SQLException e) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to save farm: " + e.getMessage());
            }
            break;
        }
    }

    private void showMapPickerDialog(TextField latField, TextField lngField) {
        Dialog<ButtonType> mapDialog = new Dialog<>();
        mapDialog.setTitle("Pick Location on Map");
        mapDialog.setHeaderText("Click on the map to select coordinates");
        mapDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        mapDialog.setResizable(true);

        WebView webView = new WebView();
        webView.setPrefSize(700, 500);
        WebEngine engine = webView.getEngine();

        // Initial coordinates: use existing values or default to Tunisia center
        String initLat = latField.getText().trim().isEmpty() ? "36.8" : latField.getText().trim();
        String initLng = lngField.getText().trim().isEmpty() ? "10.18" : lngField.getText().trim();

        String html = "<!DOCTYPE html><html><head>" +
            "<meta charset='utf-8'/>" +
            "<meta name='viewport' content='width=device-width, initial-scale=1.0'/>" +
            "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>" +
            "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
            "<style>html,body,#map{margin:0;padding:0;width:100%;height:100%;}</style>" +
            "</head><body>" +
            "<div id='map'></div>" +
            "<script>" +
            "var map = L.map('map').setView([" + initLat + "," + initLng + "], 8);" +
            "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{" +
            "  attribution:'OpenStreetMap'," +
            "  maxZoom:19" +
            "}).addTo(map);" +
            "var marker = null;" +
            "var selectedLat = " + initLat + ";" +
            "var selectedLng = " + initLng + ";" +
            // If there were existing coordinates, place initial marker
            (latField.getText().trim().isEmpty() ? "" :
                "marker = L.marker([" + initLat + "," + initLng + "]).addTo(map);") +
            "map.on('click', function(e) {" +
            "  selectedLat = e.latlng.lat;" +
            "  selectedLng = e.latlng.lng;" +
            "  if(marker) map.removeLayer(marker);" +
            "  marker = L.marker([selectedLat, selectedLng]).addTo(map);" +
            "  document.title = selectedLat.toFixed(8) + ',' + selectedLng.toFixed(8);" +
            "});" +
            "</script></body></html>";

        engine.loadContent(html);

        mapDialog.getDialogPane().setContent(webView);
        mapDialog.getDialogPane().setPrefSize(720, 550);

        Optional<ButtonType> result = mapDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String title = (String) engine.executeScript("document.title");
            if (title != null && title.contains(",")) {
                String[] parts = title.split(",");
                latField.setText(parts[0]);
                lngField.setText(parts[1]);
            }
        }
    }

    public void showFieldsView(Farm farm) {
        contentArea.getChildren().clear();

        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_LEFT);

        // Header with back button
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("action-button-block");
        backBtn.setOnAction(e -> {
            // Determine if admin or farmer to go back to the correct view
            String roleName = "";
            try {
                Role role = roleService.getById(currentUser.getRoleId());
                if (role != null) roleName = role.getName();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            if ("admin".equalsIgnoreCase(roleName)) {
                showFarmsView();
            } else {
                showMyFarmsView();
            }
        });

        Label title = new Label("Fields of \"" + farm.getName() + "\"");
        title.getStyleClass().add("content-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("action-button-add");
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("action-button-edit");
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("action-button-delete");

        header.getChildren().addAll(backBtn, title, spacer, addBtn, editBtn, deleteBtn);

        // Table
        TableView<Field> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Field, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(60);

        TableColumn<Field, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Field, Double> areaCol = new TableColumn<>("Area");
        areaCol.setCellValueFactory(new PropertyValueFactory<>("area"));
        areaCol.setMaxWidth(80);

        TableColumn<Field, String> soilCol = new TableColumn<>("Soil Type");
        soilCol.setCellValueFactory(new PropertyValueFactory<>("soilType"));

        TableColumn<Field, String> cropCol = new TableColumn<>("Crop Type");
        cropCol.setCellValueFactory(new PropertyValueFactory<>("cropType"));

        TableColumn<Field, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(idCol, nameCol, areaCol, soilCol, cropCol, statusCol);

        // Load data
        ObservableList<Field> masterData = FXCollections.observableArrayList();
        try {
            masterData.addAll(fieldService.getByFarmId(farm.getId()));
        } catch (SQLException e) {
            UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load fields: " + e.getMessage());
        }
        table.setItems(masterData);

        // Reload helper
        Runnable reloadTable = () -> {
            masterData.clear();
            try {
                masterData.addAll(fieldService.getByFarmId(farm.getId()));
            } catch (SQLException ex) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load fields: " + ex.getMessage());
            }
        };

        // Button actions
        addBtn.setOnAction(e -> {
            showFieldFormDialog(null, farm.getId());
            reloadTable.run();
        });

        editBtn.setOnAction(e -> {
            Field selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a field to edit.");
                return;
            }
            showFieldFormDialog(selected, farm.getId());
            reloadTable.run();
        });

        deleteBtn.setOnAction(e -> {
            Field selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a field to delete.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete field \"" + selected.getName() + "\"?");
            confirm.setHeaderText("Confirm Deletion");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    fieldService.delete(selected.getId());
                    reloadTable.run();
                } catch (SQLException ex) {
                    UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete field: " + ex.getMessage());
                }
            }
        });

        container.getChildren().addAll(header, table);
        contentArea.getChildren().add(container);
    }

    private void showFieldFormDialog(Field field, long farmId) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(field == null ? "Add Field" : "Edit Field");
        dialog.setHeaderText(field == null ? "Create a new field" : "Edit field: " + field.getName());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField(field != null ? field.getName() : "");
        nameField.setPromptText("Field name");
        TextField areaField = new TextField(field != null ? String.valueOf(field.getArea()) : "");
        areaField.setPromptText("Area (required, positive number)");
        TextField soilField = new TextField(field != null ? (field.getSoilType() != null ? field.getSoilType() : "") : "");
        soilField.setPromptText("Soil type (optional)");
        TextField cropField = new TextField(field != null ? (field.getCropType() != null ? field.getCropType() : "") : "");
        cropField.setPromptText("Crop type (optional)");

        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList("active", "inactive", "fallow"));
        statusCombo.setValue(field != null ? field.getStatus() : "active");

        int row = 0;
        grid.add(new Label("Name:"), 0, row);
        grid.add(nameField, 1, row++);
        grid.add(new Label("Area:"), 0, row);
        grid.add(areaField, 1, row++);
        grid.add(new Label("Soil Type:"), 0, row);
        grid.add(soilField, 1, row++);
        grid.add(new Label("Crop Type:"), 0, row);
        grid.add(cropField, 1, row++);
        grid.add(new Label("Status:"), 0, row);
        grid.add(statusCombo, 1, row);

        dialog.getDialogPane().setContent(grid);

        // Validation loop
        while (true) {
            Optional<ButtonType> result = dialog.showAndWait();
            if (!result.isPresent() || result.get() != ButtonType.OK) {
                break;
            }

            String nameVal = nameField.getText().trim();
            if (nameVal.length() < 2) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", "Field name must be at least 2 characters.");
                continue;
            }

            double areaVal;
            try {
                areaVal = Double.parseDouble(areaField.getText().trim());
                if (areaVal <= 0) {
                    UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", "Area must be a positive number.");
                    continue;
                }
            } catch (NumberFormatException ex) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", "Area must be a valid positive number.");
                continue;
            }

            try {
                if (field == null) {
                    Field newField = new Field();
                    newField.setFarmId(farmId);
                    newField.setName(nameVal);
                    newField.setArea(areaVal);
                    newField.setSoilType(soilField.getText().trim().isEmpty() ? null : soilField.getText().trim());
                    newField.setCropType(cropField.getText().trim().isEmpty() ? null : cropField.getText().trim());
                    newField.setStatus(statusCombo.getValue());
                    fieldService.add(newField);
                } else {
                    field.setName(nameVal);
                    field.setArea(areaVal);
                    field.setSoilType(soilField.getText().trim().isEmpty() ? null : soilField.getText().trim());
                    field.setCropType(cropField.getText().trim().isEmpty() ? null : cropField.getText().trim());
                    field.setStatus(statusCombo.getValue());
                    fieldService.update(field);
                }
            } catch (SQLException e) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to save field: " + e.getMessage());
            }
            break;
        }
    }
}
