package esprit.shahed.controller;

import esprit.shahed.models.Farm;
import esprit.shahed.services.FarmService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class UserHomeController {
    @FXML private FlowPane farmContainer;

    // Filter and Sort Controls
    @FXML private ComboBox<String> filterTypeCombo;
    private boolean sortAscending = false;

    private final FarmService farmService = new FarmService();

    @FXML
    public void initialize() {
        // Populate the filter dropdown with choices
        filterTypeCombo.getItems().addAll("All Types", "Crop Farm", "Livestock Farm", "Mixed", "Orchard");
        filterTypeCombo.setValue("All Types");

        loadFarmCards();
    }

    /**
     * Loads and displays farms based on current filter and sort settings
     */
    private void loadFarmCards() {
        List<Farm> farms = farmService.getAllFarms();
        applyFilterAndSort(farms);
    }

    /**
     * Handles the logic for the search dropdown and date sorting
     */
    @FXML
    private void handleFilterAndSort() {
        List<Farm> farms = farmService.getAllFarms();
        applyFilterAndSort(farms);
    }

    private void applyFilterAndSort(List<Farm> farms) {
        String selectedType = filterTypeCombo.getValue();

        // 1. Filter by Farm Type
        if (selectedType != null && !selectedType.equals("All Types")) {
            farms = farms.stream()
                    .filter(f -> f.getFarmType().equalsIgnoreCase(selectedType))
                    .collect(Collectors.toList());
        }

        // 2. Sort by Date (Assumes getCreatedAt() or getId() as a proxy for date)
        if (sortAscending) {
            farms.sort(Comparator.comparingInt(Farm::getId));
        } else {
            farms.sort(Comparator.comparingInt(Farm::getId).reversed());
        }

        displayFarms(farms);
    }

    @FXML
    private void handleSortDate() {
        sortAscending = !sortAscending; // Toggle direction
        handleFilterAndSort();
    }

    private void displayFarms(List<Farm> farms) {
        farmContainer.getChildren().clear();
        for (Farm farm : farms) {
            farmContainer.getChildren().add(createFarmCard(farm));
        }
    }

    /**
     * Creates the farm box with the "View Fields" button
     */
    private VBox createFarmCard(Farm farm) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 15; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        card.setPrefWidth(280);

        Label name = new Label(farm.getName().toUpperCase());
        name.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2D5A27;");

        Label details = new Label(String.format("%s\nSize: %.1f ha\nLocation: %s",
                farm.getFarmType(), farm.getArea(), farm.getLocation()));
        details.setWrapText(true);
        details.setStyle("-fx-text-fill: #555555;");

        Button btnFields = new Button("View Fields");
        btnFields.setMaxWidth(Double.MAX_VALUE);
        btnFields.setStyle("-fx-background-color: #3e4a2e; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        btnFields.setOnAction(e -> navigateToFields(farm));

        card.getChildren().addAll(name, new Separator(), details, btnFields);
        return card;
    }

    private void navigateToFields(Farm farm) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FieldView.fxml"));
            Parent root = loader.load();

            // Assuming your FieldController has this method
            // FieldController controller = loader.getController();
            // controller.setFarmData(farm);

            Stage stage = (Stage) farmContainer.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void switchToDashboard(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/FarmView.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Could not load Dashboard page.").show();
        }
    }
}