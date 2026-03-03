package esprit.shahed.controller;

import esprit.shahed.models.Farm;
import esprit.shahed.models.Field;
import esprit.shahed.services.FieldService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

public class FieldController {
    @FXML private Label headerLabel;
    @FXML private TableView<Field> fieldTable;
    @FXML private TableColumn<Field, Integer> colId;
    @FXML private TableColumn<Field, String> colName, colSoil, colCrop, colCoords, colStatus;
    @FXML private TableColumn<Field, Double> colArea;
    @FXML private TableColumn<Field, Void> colActions;

    @FXML private VBox fieldFormContainer;
    @FXML private Button addMainBtn;
    @FXML private TextField nameField, areaField, coordsField;
    @FXML private ComboBox<String> statusCombo, soilCombo, cropCombo;

    private final FieldService fieldService = new FieldService();
    private Farm currentFarm;
    private Field selectedField = null;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colArea.setCellValueFactory(new PropertyValueFactory<>("area"));
        colSoil.setCellValueFactory(new PropertyValueFactory<>("soilType"));
        colCrop.setCellValueFactory(new PropertyValueFactory<>("cropType"));
        colCoords.setCellValueFactory(new PropertyValueFactory<>("coordinates"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        statusCombo.setItems(FXCollections.observableArrayList("Active", "Fallow"));
        soilCombo.setItems(FXCollections.observableArrayList("Clay", "Sandy", "Loam"));
        cropCombo.setItems(FXCollections.observableArrayList("Wheat", "Corn", "Grapes"));

        setupActionButtons();
    }

    public void setFarmData(Farm farm) {
        this.currentFarm = farm;
        headerLabel.setText("Fields for: " + farm.getName());
        refreshTable();
    }

    private void setupActionButtons() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button delBtn = new Button("Delete"), updateBtn = new Button("Update");
            private final HBox box = new HBox(10, updateBtn, delBtn);
            {
                delBtn.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white;");
                updateBtn.setStyle("-fx-background-color: #5bc0de; -fx-text-fill: white;");

                delBtn.setOnAction(e -> {
                    Field field = getTableView().getItems().get(getIndex());
                    fieldService.deleteField(field.getId());
                    refreshTable();
                });
                updateBtn.setOnAction(e -> preFillForm(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    @FXML void handleSaveField() {
        try {
            double area = Double.parseDouble(areaField.getText());
            if (selectedField == null) {
                Field f = new Field(currentFarm.getId(), nameField.getText(), area, soilCombo.getValue(), cropCombo.getValue(), coordsField.getText(), statusCombo.getValue());
                fieldService.addField(f);
            } else {
                selectedField.setName(nameField.getText());
                selectedField.setArea(area);
                selectedField.setSoilType(soilCombo.getValue());
                selectedField.setCropType(cropCombo.getValue());
                selectedField.setCoordinates(coordsField.getText());
                selectedField.setStatus(statusCombo.getValue());
                fieldService.updateField(selectedField);
            }
            handleCancelAction();
            refreshTable();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error saving field.").show();
        }
    }

    private void refreshTable() {
        if (currentFarm != null) {
            fieldTable.setItems(FXCollections.observableArrayList(fieldService.getFieldsByFarm(currentFarm.getId())));
        }
    }

    private void preFillForm(Field field) {
        selectedField = field;
        nameField.setText(field.getName());
        areaField.setText(String.valueOf(field.getArea()));
        coordsField.setText(field.getCoordinates());
        statusCombo.setValue(field.getStatus());
        soilCombo.setValue(field.getSoilType());
        cropCombo.setValue(field.getCropType());
        toggleUI(true);
    }

    @FXML void handleBack() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/FarmView.fxml"));
        ((Stage) fieldTable.getScene().getWindow()).getScene().setRoot(root);
    }

    private void toggleUI(boolean show) {
        fieldFormContainer.setVisible(show);
        fieldFormContainer.setManaged(show);
        addMainBtn.setVisible(!show);
    }

    @FXML void showAddFieldForm() {
        selectedField = null;

        // Clear previous entries
        nameField.clear();
        areaField.clear();

        // AUTO-POPULATE COORDINATES FROM THE FARM DATA
        if (currentFarm != null) {
            String autoCoords = currentFarm.getLatitude() + ", " + currentFarm.getLongitude();
            coordsField.setText(autoCoords);
        }

        toggleUI(true);
    }

    @FXML void handleCancelAction() { toggleUI(false); }
    @FXML void handleSortByName() { /* Sort logic */ }
}