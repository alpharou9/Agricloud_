package org.example;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.controller.OrderController;
import org.example.controller.ProductController;
import org.example.dao.DatabaseConnection;
import org.example.dao.OrderDAO;
import org.example.dao.ProductDAO;

import java.util.ArrayList;
import java.util.List;

public class MainApp extends Application {

    private StackPane contentArea;
    private final List<Button> menuButtons = new ArrayList<>();
    private Node dashboardPage;
    private Node productsPage;
    private Node ordersPage;
    private Node settingsPage;
    private OrderController orderController;
    private ProductController productController;

    private static Scene mainScene;
    private static StackPane toastLayer;
    private static final BooleanProperty darkMode = new SimpleBooleanProperty(false);

    // Dashboard stat labels (updated live)
    private Label productCountLabel;
    private Label orderCountLabel;
    private Label revenueLabel;
    private Label dbStatusLabel;

    private final ProductDAO productDAO = new ProductDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    public void start(Stage primaryStage) {
        // --- Sidebar ---
        VBox sidebar = buildSidebar();

        // --- Content Area with toast layer ---
        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");
        HBox.setHgrow(contentArea, Priority.ALWAYS);

        toastLayer = new StackPane();
        toastLayer.setPickOnBounds(false);
        toastLayer.setAlignment(Pos.TOP_RIGHT);
        toastLayer.setPadding(new Insets(16, 16, 0, 0));

        StackPane contentWithToast = new StackPane(contentArea, toastLayer);
        HBox.setHgrow(contentWithToast, Priority.ALWAYS);

        // --- Build pages ---
        productController = new ProductController();
        orderController = new OrderController();

        dashboardPage = buildDashboardPage();
        productsPage = buildPage("Products", "Manage your farm products inventory", productController.getView());
        ordersPage = buildPage("Orders", "Track and manage customer orders", orderController.getView());
        settingsPage = buildSettingsPage();

        // --- Root layout ---
        HBox root = new HBox();
        root.getChildren().addAll(sidebar, contentWithToast);

        mainScene = new Scene(root, 1050, 650);
        mainScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // Dark mode listener
        darkMode.addListener((obs, wasD, isD) -> {
            if (isD) {
                mainScene.getRoot().getStyleClass().add("theme-dark");
            } else {
                mainScene.getRoot().getStyleClass().remove("theme-dark");
            }
        });

        primaryStage.setTitle("AgriCloud - Farm Management");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(550);
        primaryStage.setScene(mainScene);
        primaryStage.show();

        // Show dashboard by default
        switchPage(dashboardPage, 0);
        refreshDashboardStats();
    }

    // ==================== TOAST SYSTEM ====================

    public static void showToast(String message, String type) {
        if (toastLayer == null) return;

        Label toast = new Label(message);
        toast.getStyleClass().addAll("toast", "toast-" + type);
        toast.setMaxWidth(350);
        toast.setWrapText(true);

        toastLayer.getChildren().add(toast);
        StackPane.setAlignment(toast, Pos.TOP_RIGHT);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        PauseTransition hold = new PauseTransition(Duration.seconds(3));
        hold.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toast);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> toastLayer.getChildren().remove(toast));
            fadeOut.play();
        });
        hold.play();
    }

    public static boolean isDarkMode() {
        return darkMode.get();
    }

    public static BooleanProperty darkModeProperty() {
        return darkMode;
    }

    // ==================== SIDEBAR ====================

    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");

        VBox logoBox = new VBox(2);
        logoBox.getStyleClass().add("sidebar-logo");
        Label logoLabel = new Label("AgriCloud");
        logoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        Label subtitleLabel = new Label("Farm Management System");
        subtitleLabel.getStyleClass().add("subtitle");
        subtitleLabel.setStyle("-fx-text-fill: #81C784; -fx-font-size: 11px;");
        logoBox.getChildren().addAll(logoLabel, subtitleLabel);

        VBox menu = new VBox();
        menu.getStyleClass().add("sidebar-menu");

        Button dashBtn = createMenuItem("Dashboard", 0);
        Button prodBtn = createMenuItem("Products", 1);
        Button ordBtn  = createMenuItem("Orders", 2);
        Button setBtn  = createMenuItem("Settings", 3);

        menu.getChildren().addAll(dashBtn, prodBtn, ordBtn, setBtn);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox bottomBox = new VBox(4);
        bottomBox.setPadding(new Insets(16, 20, 20, 20));
        Label versionLabel = new Label("v1.0.0");
        versionLabel.setStyle("-fx-text-fill: #5A7A5D; -fx-font-size: 10px;");
        bottomBox.getChildren().add(versionLabel);

        sidebar.getChildren().addAll(logoBox, menu, spacer, bottomBox);
        return sidebar;
    }

    private Button createMenuItem(String text, int index) {
        Button btn = new Button(text);
        btn.getStyleClass().add("menu-item");
        btn.setMaxWidth(Double.MAX_VALUE);
        menuButtons.add(btn);

        btn.setOnAction(e -> {
            switch (index) {
                case 0 -> {
                    refreshDashboardStats();
                    switchPage(dashboardPage, index);
                }
                case 1 -> switchPage(productsPage, index);
                case 2 -> {
                    orderController.refreshProducts();
                    switchPage(ordersPage, index);
                }
                case 3 -> switchPage(settingsPage, index);
            }
        });

        return btn;
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

    private Node buildPage(String title, String subtitle, Node content) {
        VBox page = new VBox();
        page.setStyle("-fx-background-color: -agri-bg;");

        VBox header = new VBox(2);
        header.getStyleClass().add("page-header");
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("page-title");
        Label subLabel = new Label(subtitle);
        subLabel.getStyleClass().add("page-subtitle");
        header.getChildren().addAll(titleLabel, subLabel);

        VBox body = new VBox();
        body.getStyleClass().add("page-body");
        VBox.setVgrow(content, Priority.ALWAYS);
        VBox.setVgrow(body, Priority.ALWAYS);
        body.getChildren().add(content);

        page.getChildren().addAll(header, body);
        return page;
    }

    // ==================== DASHBOARD ====================

    private void refreshDashboardStats() {
        try {
            int productCount = productDAO.getProductCount();
            int orderCount = orderDAO.getActiveOrderCount();
            double revenue = orderDAO.getTotalRevenue();

            if (productCountLabel != null) productCountLabel.setText(String.valueOf(productCount));
            if (orderCountLabel != null) orderCountLabel.setText(String.valueOf(orderCount));
            if (revenueLabel != null) revenueLabel.setText(String.format("$%.0f", revenue));

            // Check DB connection
            if (dbStatusLabel != null) {
                try {
                    DatabaseConnection.getConnection();
                    dbStatusLabel.setText("Connected");
                    dbStatusLabel.getStyleClass().removeAll("status-disconnected");
                    if (!dbStatusLabel.getStyleClass().contains("status-connected"))
                        dbStatusLabel.getStyleClass().add("status-connected");
                } catch (Exception ex) {
                    dbStatusLabel.setText("Offline");
                    dbStatusLabel.getStyleClass().removeAll("status-connected");
                    if (!dbStatusLabel.getStyleClass().contains("status-disconnected"))
                        dbStatusLabel.getStyleClass().add("status-disconnected");
                }
            }
        } catch (Exception e) {
            System.out.println("[MainApp] Error refreshing stats: " + e.getMessage());
        }
    }

    private Node buildDashboardPage() {
        VBox page = new VBox();
        page.setStyle("-fx-background-color: -agri-bg;");

        VBox header = new VBox(2);
        header.getStyleClass().add("page-header");
        Label titleLabel = new Label("Dashboard");
        titleLabel.getStyleClass().add("page-title");
        Label subLabel = new Label("Welcome back! Here's your farm overview.");
        subLabel.getStyleClass().add("page-subtitle");
        header.getChildren().addAll(titleLabel, subLabel);

        // Stats row
        HBox statsRow = new HBox(16);
        statsRow.setPadding(new Insets(24, 28, 12, 28));

        productCountLabel = new Label("--");
        orderCountLabel = new Label("--");
        revenueLabel = new Label("--");
        dbStatusLabel = new Label("Checking...");

        statsRow.getChildren().addAll(
                buildStatCard("P", "Products", productCountLabel, "#4CAF50"),
                buildStatCard("O", "Active Orders", orderCountLabel, "#FB8C00"),
                buildStatCard("R", "Revenue", revenueLabel, "#2196F3"),
                buildStatCard("S", "DB Status", dbStatusLabel, "#8BC34A")
        );

        // Low stock warning section
        VBox alertsCard = new VBox(10);
        alertsCard.getStyleClass().add("card");
        VBox.setMargin(alertsCard, new Insets(4, 28, 8, 28));

        Label alertTitle = new Label("Low Stock Alerts");
        alertTitle.getStyleClass().add("card-title");
        alertsCard.getChildren().add(alertTitle);

        try {
            var lowStock = productDAO.getLowStockProducts(5);
            if (lowStock.isEmpty()) {
                Label ok = new Label("All products are well stocked.");
                ok.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 12px;");
                alertsCard.getChildren().add(ok);
            } else {
                for (var p : lowStock) {
                    Label warn = new Label("  [!]  " + p.getName() + " - only " + p.getQuantity() + " left");
                    warn.setStyle("-fx-text-fill: #FF9800; -fx-font-size: 12px;");
                    alertsCard.getChildren().add(warn);
                }
            }
        } catch (Exception e) {
            Label err = new Label("Could not check stock levels.");
            err.setStyle("-fx-text-fill: #9E9E9E; -fx-font-size: 12px;");
            alertsCard.getChildren().add(err);
        }

        // Quick start card
        VBox infoCard = new VBox(10);
        infoCard.getStyleClass().add("card");
        VBox.setMargin(infoCard, new Insets(4, 28, 20, 28));

        Label infoTitle = new Label("Quick Start Guide");
        infoTitle.getStyleClass().add("card-title");

        Label info1 = new Label("1. Go to Products to add your farm items (crops, dairy, livestock...)");
        Label info2 = new Label("2. Go to Orders to create and track customer orders");
        Label info3 = new Label("3. Use Settings to configure your preferences");
        info1.getStyleClass().add("guide-step");
        info2.getStyleClass().add("guide-step");
        info3.getStyleClass().add("guide-step");

        infoCard.getChildren().addAll(infoTitle, info1, info2, info3);

        page.getChildren().addAll(header, statsRow, alertsCard, infoCard);
        return page;
    }

    private VBox buildStatCard(String letter, String name, Label valueLabel, String color) {
        VBox card = new VBox(6);
        card.getStyleClass().add("stat-card");
        HBox.setHgrow(card, Priority.ALWAYS);

        // Colored circle with letter
        StackPane circle = new StackPane();
        circle.getStyleClass().add("stat-icon-circle");
        circle.setStyle("-fx-background-color: " + color + ";");
        Label letterLabel = new Label(letter);
        letterLabel.getStyleClass().add("stat-icon-letter");
        circle.getChildren().add(letterLabel);

        valueLabel.getStyleClass().add("stat-value");

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("stat-name");

        card.getChildren().addAll(circle, valueLabel, nameLabel);
        return card;
    }

    // ==================== SETTINGS ====================

    private Node buildSettingsPage() {
        VBox page = new VBox();
        page.setStyle("-fx-background-color: -agri-bg;");

        VBox header = new VBox(2);
        header.getStyleClass().add("page-header");
        Label titleLabel = new Label("Settings");
        titleLabel.getStyleClass().add("page-title");
        Label subLabel = new Label("Configure application preferences");
        subLabel.getStyleClass().add("page-subtitle");
        header.getChildren().addAll(titleLabel, subLabel);

        VBox body = new VBox(16);
        body.setPadding(new Insets(24, 28, 20, 28));

        // Theme card
        VBox themeCard = new VBox(12);
        themeCard.getStyleClass().add("card");
        Label themeTitle = new Label("Appearance");
        themeTitle.getStyleClass().add("card-title");

        HBox themeRow = new HBox(16);
        themeRow.setAlignment(Pos.CENTER_LEFT);
        Label themeLabel = new Label("Dark Mode");
        themeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: -agri-text;");

        ToggleButton themeToggle = new ToggleButton("OFF");
        themeToggle.getStyleClass().add("toggle-btn");
        themeToggle.setSelected(darkMode.get());
        themeToggle.setText(darkMode.get() ? "ON" : "OFF");

        themeToggle.selectedProperty().addListener((obs, wasS, isS) -> {
            darkMode.set(isS);
            themeToggle.setText(isS ? "ON" : "OFF");
            showToast(isS ? "Dark mode enabled" : "Light mode enabled", "info");
        });

        Label themeDesc = new Label("Switch between light and dark theme");
        themeDesc.setStyle("-fx-text-fill: -agri-text-muted; -fx-font-size: 11px;");

        themeRow.getChildren().addAll(themeLabel, themeToggle);
        themeCard.getChildren().addAll(themeTitle, themeRow, themeDesc);

        // Database card
        VBox dbCard = new VBox(10);
        dbCard.getStyleClass().add("card");
        Label dbTitle = new Label("Database Connection");
        dbTitle.getStyleClass().add("card-title");
        Label dbInfo = new Label("Host: localhost:3306  |  Database: agricloud  |  User: root");
        dbInfo.setStyle("-fx-text-fill: -agri-text-muted;");

        Label dbStatus = new Label("Checking...");
        try {
            DatabaseConnection.getConnection();
            dbStatus.setText("Connected");
            dbStatus.getStyleClass().add("status-connected");
        } catch (Exception e) {
            dbStatus.setText("Disconnected");
            dbStatus.getStyleClass().add("status-disconnected");
        }
        dbCard.getChildren().addAll(dbTitle, dbInfo, dbStatus);

        // About card
        VBox aboutCard = new VBox(10);
        aboutCard.getStyleClass().add("card");
        Label aboutTitle = new Label("About AgriCloud");
        aboutTitle.getStyleClass().add("card-title");
        Label aboutInfo = new Label("AgriCloud Farm Management System v1.0.0\nBuilt with JavaFX 17 + MySQL");
        aboutInfo.setStyle("-fx-text-fill: -agri-text-muted;");
        aboutCard.getChildren().addAll(aboutTitle, aboutInfo);

        body.getChildren().addAll(themeCard, dbCard, aboutCard);
        page.getChildren().addAll(header, body);
        return page;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
