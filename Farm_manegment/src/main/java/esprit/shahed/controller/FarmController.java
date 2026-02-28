package esprit.shahed.controller;

import esprit.shahed.models.Farm;
import esprit.shahed.services.FarmService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Comparator;
import java.util.Optional;

public class FarmController {
    @FXML private TableView<Farm> farmTable;
    @FXML private TableColumn<Farm, Integer> colId;
    @FXML private TableColumn<Farm, String> colName, colLocation, colType, colStatus, colDesc;
    @FXML private TableColumn<Farm, Double> colArea, colLat, colLong;
    @FXML private TableColumn<Farm, Void> colActions;

    @FXML private VBox formContainer;
    @FXML private Button addMainBtn;
    @FXML private TextField nameField, locationField, latField, longField, areaField;
    @FXML private ComboBox<String> typeCombo; // Form ComboBox
    @FXML private ComboBox<String> searchTypeCombo; // Search/Filter ComboBox
    @FXML private TextArea descriptionField;

    private final FarmService farmService = new FarmService();
    private Farm selectedFarm = null;

    // List that holds all data from DB
    private ObservableList<Farm> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colLat.setCellValueFactory(new PropertyValueFactory<>("latitude"));
        colLong.setCellValueFactory(new PropertyValueFactory<>("longitude"));
        colArea.setCellValueFactory(new PropertyValueFactory<>("area"));
        colType.setCellValueFactory(new PropertyValueFactory<>("farmType"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Setup the ComboBoxes
        ObservableList<String> types = FXCollections.observableArrayList("Crop Farm", "Livestock Farm", "Mixed", "Vineyard");
        typeCombo.setItems(types);

        // Setup Search Filter ComboBox
        ObservableList<String> filterTypes = FXCollections.observableArrayList("All Types");
        filterTypes.addAll(types);
        searchTypeCombo.setItems(filterTypes);
        searchTypeCombo.setValue("All Types");

        // Listener for search filter
        searchTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter());

        setupActionButtons();
        refreshTable();
    }

    private void applyFilter() {
        String filter = searchTypeCombo.getValue();
        if (filter == null || filter.equals("All Types")) {
            farmTable.setItems(masterData);
        } else {
            FilteredList<Farm> filteredList = new FilteredList<>(masterData, farm ->
                    farm.getFarmType() != null && farm.getFarmType().equals(filter)
            );
            farmTable.setItems(filteredList);
        }
    }

    @FXML
    void handleSortByName() {
        // Sort the master data so the order persists
        masterData.sort(Comparator.comparing(Farm::getName, String.CASE_INSENSITIVE_ORDER));
        applyFilter(); // Keep filter applied after sort
    }

    private void setupActionButtons() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button delBtn = new Button("Delete");
            private final Button updateBtn = new Button("Update");
            private final Button fieldsBtn = new Button("Fields");
            private final HBox box = new HBox(10, delBtn, updateBtn, fieldsBtn);
            {
                delBtn.getStyleClass().add("button-delete");
                updateBtn.getStyleClass().add("button-update");
                fieldsBtn.getStyleClass().add("button-fields");
                delBtn.setOnAction(e -> {
                    Farm farm = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete Farm: " + farm.getName() + "?", ButtonType.YES, ButtonType.NO);
                    if (alert.showAndWait().get() == ButtonType.YES) {
                        farmService.deleteFarm(farm.getId());
                        refreshTable();
                    }
                });
                updateBtn.setOnAction(e -> preFillUpdateForm(getTableView().getItems().get(getIndex())));
                fieldsBtn.setOnAction(e -> navigateToFields(getTableView().getItems().get(getIndex())));
                box.setStyle("-fx-alignment: CENTER;");
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    @FXML void handleSaveAction() {
        if (nameField.getText().trim().isEmpty()) { showError("Error", "Name required"); return; }
        try {
            double lat = Double.parseDouble(latField.getText());
            double lon = Double.parseDouble(longField.getText());
            double area = Double.parseDouble(areaField.getText());

            if (selectedFarm == null) {
                farmService.addFarm(new Farm(nameField.getText(), locationField.getText(), lat, lon, area, typeCombo.getValue(), "pending", descriptionField.getText()));
            } else {
                selectedFarm.setName(nameField.getText());
                selectedFarm.setLocation(locationField.getText());
                selectedFarm.setLatitude(lat);
                selectedFarm.setLongitude(lon);
                selectedFarm.setArea(area);
                selectedFarm.setFarmType(typeCombo.getValue());
                selectedFarm.setDescription(descriptionField.getText());
                farmService.updateFarm(selectedFarm);
            }
            handleCancelAction();
            refreshTable();
        } catch (Exception e) { showError("Input Error", "Check numeric fields."); }
    }

    private void refreshTable() {
        masterData.setAll(farmService.getAllFarms());
        applyFilter();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title); alert.setContentText(content); alert.showAndWait();
    }

    @FXML void showAddForm() { selectedFarm = null; clearFields(); toggleUI(true); }
    @FXML void handleCancelAction() { selectedFarm = null; clearFields(); toggleUI(false); }
    private void clearFields() { nameField.clear(); locationField.clear(); latField.clear(); longField.clear(); areaField.clear(); typeCombo.setValue(null); descriptionField.clear(); }
    private void toggleUI(boolean show) { formContainer.setVisible(show); formContainer.setManaged(show); addMainBtn.setVisible(!show); addMainBtn.setManaged(!show); }

    private void preFillUpdateForm(Farm farm) {
        selectedFarm = farm;
        nameField.setText(farm.getName()); locationField.setText(farm.getLocation());
        latField.setText(String.valueOf(farm.getLatitude())); longField.setText(String.valueOf(farm.getLongitude()));
        areaField.setText(String.valueOf(farm.getArea())); typeCombo.setValue(farm.getFarmType());
        descriptionField.setText(farm.getDescription()); toggleUI(true);
    }

    private void navigateToFields(Farm farm) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FieldView.fxml"));
            Parent root = loader.load();
            ((FieldController)loader.getController()).setFarmData(farm);
            ((Stage) farmTable.getScene().getWindow()).getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }
}