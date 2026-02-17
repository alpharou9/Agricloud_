package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import org.example.MainApp;

import java.io.IOException;
import java.util.List;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private StackPane toastLayer;
    @FXML private Button dashBtn;
    @FXML private Button prodBtn;
    @FXML private Button ordBtn;
    @FXML private Button setBtn;

    private Node dashboardPage;
    private Node productsPage;
    private Node ordersPage;
    private Node settingsPage;

    private DashboardController dashboardController;
    private OrderController orderController;

    private List<Button> menuButtons;

    @FXML
    public void initialize() {
        MainApp.setToastLayer(toastLayer);
        menuButtons = List.of(dashBtn, prodBtn, ordBtn, setBtn);

        try {
            FXMLLoader dashLoader = new FXMLLoader(getClass().getResource("/org/example/dashboard-view.fxml"));
            dashboardPage = dashLoader.load();
            dashboardController = dashLoader.getController();

            FXMLLoader prodLoader = new FXMLLoader(getClass().getResource("/org/example/products-view.fxml"));
            productsPage = prodLoader.load();

            FXMLLoader ordLoader = new FXMLLoader(getClass().getResource("/org/example/orders-view.fxml"));
            ordersPage = ordLoader.load();
            orderController = ordLoader.getController();

            FXMLLoader setLoader = new FXMLLoader(getClass().getResource("/org/example/settings-view.fxml"));
            settingsPage = setLoader.load();
        } catch (IOException e) {
            System.out.println("[MainController] Error loading sub-views: " + e.getMessage());
            e.printStackTrace();
        }

        // Show dashboard by default
        switchPage(dashboardPage, 0);
        dashboardController.refreshStats();
    }

    @FXML
    private void onDashboard() {
        dashboardController.refreshStats();
        switchPage(dashboardPage, 0);
    }

    @FXML
    private void onProducts() {
        switchPage(productsPage, 1);
    }

    @FXML
    private void onOrders() {
        orderController.refreshProducts();
        switchPage(ordersPage, 2);
    }

    @FXML
    private void onSettings() {
        switchPage(settingsPage, 3);
    }

    private void switchPage(Node page, int activeIndex) {
        contentArea.getChildren().setAll(page);
        for (int i = 0; i < menuButtons.size(); i++) {
            Button btn = menuButtons.get(i);
            btn.getStyleClass().removeAll("menu-item-active");
            if (i == activeIndex) {
                btn.getStyleClass().add("menu-item-active");
            }
        }
    }
}
