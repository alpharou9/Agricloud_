package esprit.shahed.controller;

import esprit.shahed.models.Farm;
import esprit.shahed.models.Field;
import esprit.shahed.services.FieldService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

    @FXML
    void handleSortByName() {
        ObservableList<Field> currentItems = fieldTable.getItems();
        if (currentItems != null && !currentItems.isEmpty()) {
            FXCollections.sort(currentItems, Comparator.comparing(Field::getName, String.CASE_INSENSITIVE_ORDER));
        }
    }

    private void setupActionButtons() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button delBtn = new Button("Delete");
            private final Button updateBtn = new Button("Update");
            private final HBox box = new HBox(10, delBtn, updateBtn);
            {
                delBtn.getStyleClass().add("button-delete");
                updateBtn.getStyleClass().add("button-update");

                delBtn.setOnAction(e -> {
                    Field field = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Delete Confirmation");
                    alert.setHeaderText("Delete Field: " + field.getName() + "?");
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        fieldService.deleteField(field.getId());
                        refreshTable();
                    }
                });
                updateBtn.setOnAction(e -> preFillForm(getTableView().getItems().get(getIndex())));
                box.setStyle("-fx-alignment: CENTER;");
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    @FXML void handleSaveField() {
        if (nameField.getText().trim().isEmpty()) {
            showError("Validation Error", "Field Name is required.");
            return;
        }
        try {
            double area = Double.parseDouble(areaField.getText());
            if (selectedField == null) {
                Field f = new Field(currentFarm.getId(), nameField.getText(), area,
                        soilCombo.getValue(), cropCombo.getValue(), coordsField.getText(), statusCombo.getValue());
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
        } catch (NumberFormatException e) {
            showError("Input Error", "Please enter a valid numeric value for Area.");
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML void showAddFieldForm() { selectedField = null; clearFields(); toggleUI(true); }
    @FXML void handleCancelAction() { selectedField = null; clearFields(); toggleUI(false); }
    private void clearFields() {
        nameField.clear(); areaField.clear(); coordsField.clear();
        statusCombo.setValue(null); soilCombo.setValue(null); cropCombo.setValue(null);
    }
    private void toggleUI(boolean showForm) {
        fieldFormContainer.setVisible(showForm); fieldFormContainer.setManaged(showForm);
        addMainBtn.setVisible(!showForm); addMainBtn.setManaged(!showForm);
    }
    private void refreshTable() {
        if (currentFarm != null) fieldTable.setItems(FXCollections.observableArrayList(fieldService.getFieldsByFarm(currentFarm.getId())));
    }
    private void preFillForm(Field field) {
        selectedField = field;
        nameField.setText(field.getName()); areaField.setText(String.valueOf(field.getArea()));
        coordsField.setText(field.getCoordinates()); statusCombo.setValue(field.getStatus());
        soilCombo.setValue(field.getSoilType()); cropCombo.setValue(field.getCropType()); toggleUI(true);
    }
    @FXML void handleBack() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/FarmView.fxml"));
        ((Stage) fieldTable.getScene().getWindow()).getScene().setRoot(root);
    }
}