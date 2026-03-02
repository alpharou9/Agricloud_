package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import org.example.MainApp;

import java.io.IOException;

/**
 * Root controller that manages the sidebar navigation and content area.
 * All page loading happens here; sub-controllers are wired up so that
 * OrderController can refresh products list when the user switches pages.
 */
public class MainController {

    @FXML private StackPane contentArea;
    @FXML private StackPane toastLayer;

    @FXML private Button dashBtn;
    @FXML private Button prodBtn;
    @FXML private Button ordBtn;
    @FXML private Button setBtn;

    // Sub-controllers kept for cross-page coordination
    private DashboardController dashboardController;
    private ProductController   productController;
    private OrderController     orderController;
    private SettingsController  settingsController;

    private Node dashPage;
    private Node prodPage;
    private Node ordPage;
    private Node setPage;

    @FXML
    public void initialize() {
        // Register toast layer so MainApp can push toasts from anywhere
        MainApp.getInstance().setToastLayer(toastLayer);

        try {
            // Dashboard
            FXMLLoader dl = new FXMLLoader(getClass().getResource("/org/example/dashboard-view.fxml"));
            dashPage = dl.load();
            dashboardController = dl.getController();

            // Products
            FXMLLoader pl = new FXMLLoader(getClass().getResource("/org/example/products-view.fxml"));
            prodPage = pl.load();
            productController = pl.getController();
            productController.setMainController(this);

            // Orders
            FXMLLoader ol = new FXMLLoader(getClass().getResource("/org/example/orders-view.fxml"));
            ordPage = ol.load();
            orderController = ol.getController();
            orderController.setMainController(this);

            // Settings
            FXMLLoader sl = new FXMLLoader(getClass().getResource("/org/example/settings-view.fxml"));
            setPage = sl.load();
            settingsController = sl.getController();
            settingsController.setMainController(this);

        } catch (IOException e) {
            System.err.println("[Main] Failed to load FXML pages: " + e.getMessage());
            e.printStackTrace();
        }

        // Start on Dashboard
        switchPage(dashPage, 0);
    }

    // -------------------------------------------------------------------------
    // Navigation handlers (FXML onAction)
    // -------------------------------------------------------------------------

    @FXML void onDashboard() {
        dashboardController.refreshStats();
        switchPage(dashPage, 0);
    }

    @FXML void onProducts() {
        productController.refreshGrid();
        switchPage(prodPage, 1);
    }

    @FXML void onOrders() {
        // Always reload product list in case new products were approved/added
        orderController.refreshProducts();
        orderController.refreshOrders();
        switchPage(ordPage, 2);
    }

    @FXML void onSettings() {
        settingsController.refresh();
        switchPage(setPage, 3);
    }

    // -------------------------------------------------------------------------
    // Cross-page refresh helpers called by sub-controllers
    // -------------------------------------------------------------------------

    /** Called by ProductController after stock-changing operations. */
    public void notifyProductsChanged() {
        orderController.refreshProducts();
    }

    /** Called by SettingsController after the role is switched. */
    public void notifyRoleChanged() {
        productController.onRoleChanged();
        orderController.onRoleChanged();
        dashboardController.refreshStats();
    }

    // -------------------------------------------------------------------------
    // Internals
    // -------------------------------------------------------------------------

    private void switchPage(Node page, int activeIndex) {
        contentArea.getChildren().setAll(page);
        Button[] btns = {dashBtn, prodBtn, ordBtn, setBtn};
        for (int i = 0; i < btns.length; i++) {
            btns[i].getStyleClass().removeAll("menu-item-active");
            if (i == activeIndex) btns[i].getStyleClass().add("menu-item-active");
        }
    }
}
