package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import org.example.MainApp;
import org.example.dao.DatabaseConnection;

public class SettingsController {

    @FXML private ToggleButton themeToggle;
    @FXML private Label dbStatusLabel;

    @FXML
    public void initialize() {
        // Sync toggle with current dark mode state
        themeToggle.setSelected(MainApp.isDarkMode());
        themeToggle.setText(MainApp.isDarkMode() ? "ON" : "OFF");

        themeToggle.selectedProperty().addListener((obs, wasS, isS) -> {
            MainApp.darkModeProperty().set(isS);
            themeToggle.setText(isS ? "ON" : "OFF");
            MainApp.showToast(isS ? "Dark mode enabled" : "Light mode enabled", "info");
        });

        // Check DB status
        try {
            DatabaseConnection.getConnection();
            dbStatusLabel.setText("Connected");
            dbStatusLabel.getStyleClass().add("status-connected");
        } catch (Exception e) {
            dbStatusLabel.setText("Disconnected");
            dbStatusLabel.getStyleClass().add("status-disconnected");
        }
    }
}
