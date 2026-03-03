package org.example.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import org.example.MainApp;
import org.example.model.Notification;
import org.example.service.NotificationService;

import java.io.IOException;
import java.util.List;

/**
 * Root controller – manages sidebar navigation, toast layer, notification bell,
 * and the content area.
 */
public class MainController {

    @FXML private StackPane contentArea;
    @FXML private StackPane toastLayer;
    @FXML private StackPane notifLayer;

    @FXML private Button dashBtn;
    @FXML private Button prodBtn;
    @FXML private Button ordBtn;
    @FXML private Button setBtn;
    @FXML private Button analyticsBtn;

    @FXML private StackPane bellPane;
    @FXML private Button    bellBtn;
    @FXML private Label     bellBadge;

    // Sub-controllers
    private DashboardController  dashboardController;
    private ProductController    productController;
    private OrderController      orderController;
    private SettingsController   settingsController;
    private AnalyticsController  analyticsController;

    private Node dashPage;
    private Node prodPage;
    private Node ordPage;
    private Node setPage;
    private Node analyticsPage;

    private final NotificationService notifService = new NotificationService();

    /** The currently-visible notification panel VBox (null when hidden). */
    private VBox notifPanel;

    // =========================================================================
    // Init
    // =========================================================================

    @FXML
    public void initialize() {
        MainApp.getInstance().setToastLayer(toastLayer);

        try {
            FXMLLoader dl = new FXMLLoader(getClass().getResource("/org/example/dashboard-view.fxml"));
            dashPage = dl.load();
            dashboardController = dl.getController();

            FXMLLoader pl = new FXMLLoader(getClass().getResource("/org/example/products-view.fxml"));
            prodPage = pl.load();
            productController = pl.getController();
            productController.setMainController(this);

            FXMLLoader ol = new FXMLLoader(getClass().getResource("/org/example/orders-view.fxml"));
            ordPage = ol.load();
            orderController = ol.getController();
            orderController.setMainController(this);

            FXMLLoader sl = new FXMLLoader(getClass().getResource("/org/example/settings-view.fxml"));
            setPage = sl.load();
            settingsController = sl.getController();
            settingsController.setMainController(this);

            FXMLLoader al = new FXMLLoader(getClass().getResource("/org/example/analytics-view.fxml"));
            analyticsPage = al.load();
            analyticsController = al.getController();

        } catch (IOException e) {
            System.err.println("[Main] Failed to load FXML pages: " + e.getMessage());
            e.printStackTrace();
        }

        switchPage(dashPage, 0);
        refreshBadge();
    }

    // =========================================================================
    // Navigation
    // =========================================================================

    @FXML void onDashboard() {
        closeNotifPanel();
        dashboardController.refreshStats();
        switchPage(dashPage, 0);
        refreshBadge();
    }

    @FXML void onProducts() {
        closeNotifPanel();
        productController.refreshGrid();
        switchPage(prodPage, 1);
        refreshBadge();
    }

    @FXML void onOrders() {
        closeNotifPanel();
        orderController.refreshProducts();
        orderController.refreshOrders();
        switchPage(ordPage, 2);
        refreshBadge();
    }

    @FXML void onSettings() {
        closeNotifPanel();
        settingsController.refresh();
        switchPage(setPage, 3);
        refreshBadge();
    }

    @FXML void onAnalytics() {
        closeNotifPanel();
        analyticsController.refreshAnalytics();
        switchPage(analyticsPage, 4);
        refreshBadge();
    }

    // =========================================================================
    // Cross-page refresh helpers
    // =========================================================================

    public void notifyProductsChanged() {
        orderController.refreshProducts();
    }

    public void notifyRoleChanged() {
        productController.onRoleChanged();
        orderController.onRoleChanged();
        dashboardController.refreshStats();
        refreshBadge();
    }

    // =========================================================================
    // Bell / Notification panel
    // =========================================================================

    @FXML
    void onBell() {
        if (notifPanel != null) {
            closeNotifPanel();
        } else {
            openNotifPanel();
        }
    }

    public void refreshBadge() {
        Thread t = new Thread(() -> {
            int count = notifService.getUnreadCount();
            Platform.runLater(() -> {
                if (count > 0) {
                    bellBadge.setText(count > 99 ? "99+" : String.valueOf(count));
                    bellBadge.setVisible(true);
                } else {
                    bellBadge.setVisible(false);
                }
            });
        }, "notif-badge");
        t.setDaemon(true);
        t.start();
    }

    private void openNotifPanel() {
        List<Notification> notifs = notifService.getAllNotifications();

        // ── Panel container ──────────────────────────────────────────────────
        notifPanel = new VBox(0);
        notifPanel.setMaxWidth(360);
        notifPanel.setPrefWidth(360);
        notifPanel.setMaxHeight(480);
        notifPanel.setStyle(
            "-fx-background-color: -agri-card;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: -agri-border;" +
            "-fx-border-radius: 10;" +
            "-fx-border-width: 1;" +
            "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.25),16,0,2,-2);");

        // ── Header ───────────────────────────────────────────────────────────
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 14, 12, 14));
        header.setStyle(
            "-fx-background-color: -agri-green;" +
            "-fx-background-radius: 10 10 0 0;");

        Label title = new Label("Notifications");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button markAll = new Button("✓ All");
        markAll.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 3 8; -fx-background-radius: 4;");
        markAll.setOnAction(e -> { notifService.markAllRead(); closeNotifPanel(); refreshBadge(); });

        Button clearBtn = new Button("Clear");
        clearBtn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 3 8; -fx-background-radius: 4;");
        clearBtn.setOnAction(e -> { notifService.clearRead(); closeNotifPanel(); refreshBadge(); });

        Button closeX = new Button("✕");
        closeX.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 2 6;");
        closeX.setOnAction(e -> closeNotifPanel());

        header.getChildren().addAll(title, spacer, markAll, clearBtn, closeX);

        // ── Notification list ────────────────────────────────────────────────
        VBox listBox = new VBox(0);

        if (notifs.isEmpty()) {
            Label empty = new Label("No notifications.");
            empty.setStyle("-fx-text-fill: -agri-text-muted; -fx-font-size: 12px; -fx-padding: 20 14;");
            listBox.getChildren().add(empty);
        } else {
            for (Notification n : notifs) {
                listBox.getChildren().add(buildNotifRow(n));
            }
        }

        ScrollPane scroll = new ScrollPane(listBox);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setPrefHeight(400);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        notifPanel.getChildren().addAll(header, scroll);

        // Position: bottom-left corner of content area (above sidebar)
        StackPane.setAlignment(notifPanel, Pos.BOTTOM_LEFT);
        StackPane.setMargin(notifPanel, new Insets(0, 0, 12, 12));
        notifLayer.getChildren().add(notifPanel);
    }

    private Node buildNotifRow(Notification n) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 12, 10, 12));
        row.setStyle(
            "-fx-background-color: " + (n.isRead() ? "transparent" : "-agri-green-bg") + ";" +
            "-fx-border-color: transparent transparent -agri-border transparent;" +
            "-fx-border-width: 0 0 1 0;");

        // Type icon
        Label icon = new Label(n.getIcon());
        icon.setStyle("-fx-font-size: 18px; -fx-min-width: 26;");

        // Text
        VBox text = new VBox(2);
        HBox.setHgrow(text, Priority.ALWAYS);
        Label titleLbl = new Label(n.getTitle());
        titleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: -agri-text;");
        titleLbl.setWrapText(true);
        titleLbl.setMaxWidth(220);
        String msgText = n.getMessage() != null ? n.getMessage() : "";
        if (msgText.length() > 70) msgText = msgText.substring(0, 67) + "...";
        Label msgLbl = new Label(msgText);
        msgLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: -agri-text-muted;");
        msgLbl.setWrapText(true);
        msgLbl.setMaxWidth(220);
        Label timeLbl = new Label(n.getTimeAgo());
        timeLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: -agri-text-muted;");
        text.getChildren().addAll(titleLbl, msgLbl, timeLbl);

        // Read button (only for persisted unread)
        if (!n.isRead() && n.isPersisted()) {
            Button readBtn = new Button("✓");
            readBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: -agri-green; -fx-font-size: 13px; -fx-padding: 0 4;");
            readBtn.setOnAction(e -> {
                notifService.markRead(n.getId());
                closeNotifPanel();
                openNotifPanel();
                refreshBadge();
            });
            row.getChildren().addAll(icon, text, readBtn);
        } else {
            row.getChildren().addAll(icon, text);
        }

        // Click to navigate to related page
        if (n.getRelatedType() != null) {
            row.setStyle(row.getStyle() + "-fx-cursor: hand;");
            row.setOnMouseClicked(e -> {
                closeNotifPanel();
                if ("ORDER".equals(n.getRelatedType())) {
                    onOrders();
                } else if ("PRODUCT".equals(n.getRelatedType())) {
                    onProducts();
                }
            });
        }

        return row;
    }

    private void closeNotifPanel() {
        if (notifPanel != null) {
            notifLayer.getChildren().remove(notifPanel);
            notifPanel = null;
        }
    }

    // =========================================================================
    // Internals
    // =========================================================================

    private void switchPage(Node page, int activeIndex) {
        contentArea.getChildren().setAll(page);
        Button[] btns = {dashBtn, prodBtn, ordBtn, setBtn, analyticsBtn};
        for (int i = 0; i < btns.length; i++) {
            if (btns[i] == null) continue;
            btns[i].getStyleClass().removeAll("menu-item-active");
            if (i == activeIndex) btns[i].getStyleClass().add("menu-item-active");
        }
    }
}
