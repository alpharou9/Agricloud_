package esprit.shahed.controller;

import esprit.shahed.models.Farm;
import esprit.shahed.models.Field;
import esprit.shahed.services.FarmService;
import esprit.shahed.services.FieldService;
import esprit.shahed.services.WeatherService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfWriter;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class FarmController {
    // --- TABLE & UI FIELDS ---
    @FXML private TableView<Farm> farmTable;
    @FXML private TableColumn<Farm, Integer> colId;
    @FXML private TableColumn<Farm, String> colName, colLocation, colType, colStatus, colDescription;
    @FXML private TableColumn<Farm, Double> colArea, colLat, colLong;
    @FXML private TableColumn<Farm, Void> colActions;

    @FXML private VBox formContainer;
    @FXML private Button addMainBtn;
    @FXML private TextField nameField, locationField, latField, longField, areaField;
    @FXML private ComboBox<String> typeCombo, searchTypeCombo;
    @FXML private TextArea descriptionField;

    @FXML private Label weatherLabel;
    @FXML private ImageView weatherIcon;

    // --- SIDEBAR BUTTONS ---
    @FXML private Button homeBtn;
    @FXML private Button dashBtn;

    private final FarmService farmService = new FarmService();
    private final FieldService fieldService = new FieldService();
    private ObservableList<Farm> masterData = FXCollections.observableArrayList();
    private FilteredList<Farm> filteredData;
    private Farm selectedFarm = null;
    private boolean isAscending = false;

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
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        ObservableList<String> types = FXCollections.observableArrayList("Crop Farm", "Livestock Farm", "Mixed", "Orchard");
        typeCombo.setItems(types);

        ObservableList<String> searchChoices = FXCollections.observableArrayList("All Types");
        searchChoices.addAll(types);
        searchTypeCombo.setItems(searchChoices);
        searchTypeCombo.setValue("All Types");

        filteredData = new FilteredList<>(masterData, p -> true);
        searchTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(farm -> newVal.equals("All Types") || farm.getFarmType().equals(newVal));
        });
        farmTable.setItems(filteredData);

        setupActionButtons();
        refreshTable();
    }

    /**
     * Public method to allow UserHomeController to trigger an edit remotely.
     */
    public void handleExternalEdit(Farm farm) {
        preFillForm(farm);
    }

    // --- NEW SIDEBAR NAVIGATION ---
    @FXML
    private void handleHomeNavigation(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/UserHomeView.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Could not load Home page.").show();
        }
    }

    @FXML
    private void handleDashboardNavigation() {
        refreshTable();
        toggleUI(false); // Hide form if it was open
    }

    // --- VALIDATION & REFRESH ---
    private boolean isInputValid() {
        StringBuilder errorMsg = new StringBuilder();
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) errorMsg.append("- Farm Name is required.\n");
        if (locationField.getText() == null || locationField.getText().trim().isEmpty()) errorMsg.append("- Location is required.\n");
        if (typeCombo.getValue() == null) errorMsg.append("- Please select a Farm Type.\n");

        try {
            double lat = Double.parseDouble(latField.getText());
            if (lat < -90 || lat > 90) errorMsg.append("- Latitude must be between -90 and 90.\n");
        } catch (NumberFormatException e) { errorMsg.append("- Latitude must be a valid number.\n"); }

        try {
            double lon = Double.parseDouble(longField.getText());
            if (lon < -180 || lon > 180) errorMsg.append("- Longitude must be between -180 and 180.\n");
        } catch (NumberFormatException e) { errorMsg.append("- Longitude must be a valid number.\n"); }

        try {
            double area = Double.parseDouble(areaField.getText());
            if (area <= 0) errorMsg.append("- Area must be a positive number.\n");
        } catch (NumberFormatException e) { errorMsg.append("- Area must be a valid number.\n"); }

        if (errorMsg.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING, errorMsg.toString());
            alert.setTitle("Validation Error");
            alert.showAndWait();
            return false;
        }
        return true;
    }

    @FXML
    public void handleSaveAction() {
        if (!isInputValid()) return;
        try {
            double lat = Double.parseDouble(latField.getText());
            double lon = Double.parseDouble(longField.getText());
            double area = Double.parseDouble(areaField.getText());

            if (selectedFarm == null) {
                Farm f = new Farm(nameField.getText(), locationField.getText(), lat, lon, area, typeCombo.getValue(), "pending", descriptionField.getText());
                farmService.addFarm(f);
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
            toggleUI(false);
            refreshTable();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "An error occurred: " + e.getMessage()).show();
        }
    }

    // --- SETUP TABLE ACTIONS ---
    private void setupActionButtons() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button del = new Button("Delete"), upd = new Button("Update"), flds = new Button("Fields");
            private final HBox box = new HBox(8, upd, del, flds);
            {
                upd.setStyle("-fx-background-color: #5bc0de; -fx-text-fill: white; -fx-cursor: hand;");
                del.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white; -fx-cursor: hand;");
                flds.setStyle("-fx-background-color: #3e4a2e; -fx-text-fill: white; -fx-cursor: hand;");

                upd.setOnAction(e -> preFillForm(getTableView().getItems().get(getIndex())));
                del.setOnAction(e -> {
                    farmService.deleteFarm(getTableView().getItems().get(getIndex()).getId());
                    refreshTable();
                });
                flds.setOnAction(e -> navigateToFields(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void preFillForm(Farm f) {
        selectedFarm = f;
        nameField.setText(f.getName());
        locationField.setText(f.getLocation());
        latField.setText(String.valueOf(f.getLatitude()));
        longField.setText(String.valueOf(f.getLongitude()));
        areaField.setText(String.valueOf(f.getArea()));
        typeCombo.setValue(f.getFarmType());
        descriptionField.setText(f.getDescription());
        fetchAndDisplayWeather(f.getLatitude(), f.getLongitude());
        toggleUI(true);
    }

    private void fetchAndDisplayWeather(double lat, double lon) {
        JSONObject data = WeatherService.getWeather(lat, lon);
        if (data != null) {
            double temp = data.getJSONObject("main").getDouble("temp");
            String desc = data.getJSONArray("weather").getJSONObject(0).getString("description");
            String iconCode = data.getJSONArray("weather").getJSONObject(0).getString("icon");
            weatherLabel.setText(String.format("%.1f°C - %s", temp, desc.toUpperCase()));
            weatherIcon.setImage(new Image("https://openweathermap.org/img/wn/" + iconCode + "@2x.png"));
        } else {
            weatherLabel.setText("Weather unavailable");
            weatherIcon.setImage(null);
        }
    }

    private void navigateToFields(Farm farm) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FieldView.fxml"));
            Parent root = loader.load();
            FieldController controller = loader.getController();
            controller.setFarmData(farm);
            Stage stage = (Stage) farmTable.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void openMapPopup() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MapPopup.fxml"));
        Parent root = loader.load();
        MapPopupController popup = loader.getController();
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.showAndWait();
        if (popup.isConfirmed()) {
            latField.setText(String.valueOf(popup.getLat()));
            longField.setText(String.valueOf(popup.getLng()));
            locationField.setText(popup.getName());
            fetchAndDisplayWeather(popup.getLat(), popup.getLng());
        }
    }

    @FXML public void showStatistics() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StatsView.fxml"));
            Parent root = loader.load();
            StatsController statsController = loader.getController();
            statsController.setData(filteredData);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML public void handleExportPDF() {
        // PDF Export Logic remains same...
    }

    public void refreshTable() { masterData.setAll(farmService.getAllFarms()); }

    @FXML void showAddForm() {
        selectedFarm = null;
        nameField.clear(); locationField.clear(); latField.clear();
        longField.clear(); areaField.clear(); descriptionField.clear();
        weatherLabel.setText("Select location to load weather");
        weatherIcon.setImage(null);
        toggleUI(true);
    }

    @FXML void handleCancelAction() { toggleUI(false); }
    @FXML void handleSortByName() { masterData.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName())); }
    @FXML void handleSortByDate() {
        if (isAscending) masterData.sort(Comparator.comparingInt(Farm::getId));
        else masterData.sort((f1, f2) -> Integer.compare(f2.getId(), f1.getId()));
        isAscending = !isAscending;
    }

    private void toggleUI(boolean show) {
        formContainer.setVisible(show);
        formContainer.setManaged(show);
        addMainBtn.setVisible(!show);
    }
}