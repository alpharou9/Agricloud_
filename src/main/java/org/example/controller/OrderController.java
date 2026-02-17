package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import org.example.MainApp;
import org.example.dao.OrderDAO;
import org.example.dao.ProductDAO;
import org.example.model.Order;
import org.example.model.Product;

import java.time.format.DateTimeFormatter;
import java.util.Map;

public class OrderController {

    private static final long DEFAULT_CUSTOMER_ID = 1;
    private static final Map<String, String> STATUS_CLASSES = Map.of(
            "pending", "status-pending",
            "confirmed", "status-confirmed",
            "processing", "status-processing",
            "shipped", "status-shipped",
            "delivered", "status-delivered",
            "cancelled", "status-cancelled"
    );

    private final OrderDAO orderDAO = new OrderDAO();
    private final ProductDAO productDAO = new ProductDAO();

    @FXML private StackPane rootStack;
    @FXML private VBox orderListBox;

    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private HBox drawerOverlay;

    @FXML
    public void initialize() {
        refreshProducts();
        refreshOrders();
    }

    @FXML
    private void onAddOrder() {
        openDrawer(null);
    }

    // ==================== DRAWER ====================

    private void openDrawer(Order existing) {
        closeDrawer();

        ComboBox<Product> productCombo = new ComboBox<>();
        productCombo.setItems(productList);
        productCombo.setMaxWidth(Double.MAX_VALUE);
        productCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Product p) {
                if (p == null) return "";
                return p.getName() + "  $" + String.format("%.2f", p.getPrice()) + "/" + p.getUnit()
                        + "  (Stock: " + p.getQuantity() + ")";
            }
            @Override
            public Product fromString(String s) { return null; }
        });
        productCombo.setPromptText("Select product");

        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");

        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList(
                "pending", "confirmed", "processing", "shipped", "delivered", "cancelled"));
        statusCombo.setValue("pending");
        statusCombo.setMaxWidth(Double.MAX_VALUE);

        TextField addressField = new TextField();
        addressField.setPromptText("Street address");
        TextField cityField = new TextField();
        cityField.setPromptText("City");
        TextField postalField = new TextField();
        postalField.setPromptText("Postal code");
        TextArea notesArea = new TextArea();
        notesArea.setPrefRowCount(2);
        notesArea.setPromptText("Notes");
        DatePicker deliveryPicker = new DatePicker();
        deliveryPicker.setMaxWidth(Double.MAX_VALUE);

        Label unitPriceLabel = new Label("$0.00");
        unitPriceLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: -agri-text-muted;");
        Label totalPriceLabel = new Label("$0.00");
        totalPriceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: -agri-green;");

        Label stockWarning = new Label();
        stockWarning.getStyleClass().add("error-message");
        stockWarning.setVisible(false); stockWarning.setManaged(false);

        // Price calculation
        Runnable updatePrice = () -> {
            Product p = productCombo.getValue();
            if (p != null) {
                unitPriceLabel.setText(String.format("$%.2f / %s", p.getPrice(), p.getUnit()));
                String qt = quantityField.getText().trim();
                if (!qt.isEmpty()) {
                    try {
                        int qty = Integer.parseInt(qt);
                        totalPriceLabel.setText(String.format("$%.2f", p.getPrice() * qty));
                        if (qty > p.getQuantity()) {
                            stockWarning.setText("Only " + p.getQuantity() + " in stock!");
                            stockWarning.setVisible(true); stockWarning.setManaged(true);
                        } else {
                            stockWarning.setVisible(false); stockWarning.setManaged(false);
                        }
                    } catch (NumberFormatException ignored) { totalPriceLabel.setText("$0.00"); }
                }
            } else {
                unitPriceLabel.setText("$0.00");
                totalPriceLabel.setText("$0.00");
            }
        };
        productCombo.setOnAction(e -> updatePrice.run());
        quantityField.textProperty().addListener((obs, o, n) -> updatePrice.run());

        // Pre-fill
        if (existing != null) {
            for (Product p : productList) {
                if (p.getId() == existing.getProductId()) { productCombo.setValue(p); break; }
            }
            quantityField.setText(String.valueOf(existing.getQuantity()));
            statusCombo.setValue(existing.getStatus());
            addressField.setText(existing.getShippingAddress());
            cityField.setText(existing.getShippingCity());
            postalField.setText(existing.getShippingPostal());
            notesArea.setText(existing.getNotes());
            deliveryPicker.setValue(existing.getDeliveryDate());
            unitPriceLabel.setText(String.format("$%.2f", existing.getUnitPrice()));
            totalPriceLabel.setText(String.format("$%.2f", existing.getTotalPrice()));
        }

        // --- Drawer body ---
        VBox body = new VBox(10);
        body.getStyleClass().add("drawer-body");

        VBox prodCol = fieldCol("Product *", productCombo);
        VBox qtyCol = fieldCol("Quantity *", quantityField);

        VBox priceSection = new VBox(4);
        Label priceSectLbl = new Label("PRICING");
        priceSectLbl.getStyleClass().add("drawer-section-label");
        HBox priceRow = new HBox(16);
        priceRow.setAlignment(Pos.CENTER_LEFT);
        VBox upCol = new VBox(2, new Label("Unit Price") {{ getStyleClass().add("form-label"); }}, unitPriceLabel);
        VBox tpCol = new VBox(2, new Label("Total") {{ getStyleClass().add("form-label"); }}, totalPriceLabel);
        priceRow.getChildren().addAll(upCol, tpCol);
        priceSection.getChildren().addAll(priceSectLbl, priceRow, stockWarning);

        HBox statusDelRow = new HBox(10);
        statusDelRow.getStyleClass().add("drawer-field-row");
        VBox statCol = fieldCol("Status", statusCombo);
        VBox delCol = fieldCol("Delivery Date", deliveryPicker);
        HBox.setHgrow(statCol, Priority.ALWAYS);
        HBox.setHgrow(delCol, Priority.ALWAYS);
        statusDelRow.getChildren().addAll(statCol, delCol);

        Label shipLbl = new Label("SHIPPING");
        shipLbl.getStyleClass().add("drawer-section-label");
        VBox addrCol = fieldCol("Address *", addressField);
        HBox cityPostRow = new HBox(10);
        cityPostRow.getStyleClass().add("drawer-field-row");
        VBox cCol = fieldCol("City", cityField);
        VBox pCol = fieldCol("Postal", postalField);
        HBox.setHgrow(cCol, Priority.ALWAYS);
        HBox.setHgrow(pCol, Priority.ALWAYS);
        cityPostRow.getChildren().addAll(cCol, pCol);

        VBox notesCol = fieldCol("Notes", notesArea);

        body.getChildren().addAll(prodCol, qtyCol, priceSection, statusDelRow, shipLbl, addrCol, cityPostRow, notesCol);

        ScrollPane bodyScroll = new ScrollPane(body);
        bodyScroll.setFitToWidth(true);
        bodyScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        bodyScroll.getStyleClass().add("products-scroll");
        VBox.setVgrow(bodyScroll, Priority.ALWAYS);

        // --- Header ---
        HBox header = new HBox();
        header.getStyleClass().add("drawer-header");
        header.setAlignment(Pos.CENTER_LEFT);
        Label titleLbl = new Label(existing == null ? "New Order" : "Edit Order #" + existing.getId());
        titleLbl.getStyleClass().add("drawer-title");
        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);
        Button closeBtn = new Button("\u2715");
        closeBtn.getStyleClass().add("drawer-close");
        closeBtn.setOnAction(e -> closeDrawer());
        header.getChildren().addAll(titleLbl, hSpacer, closeBtn);

        // --- Actions ---
        HBox actions = new HBox(8);
        actions.getStyleClass().add("drawer-actions");

        Button saveBtn = new Button(existing == null ? "Save" : "Update");
        saveBtn.getStyleClass().add("btn-primary");
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("btn-secondary");
        cancelBtn.setOnAction(e -> closeDrawer());

        Region aSpacer = new Region();
        HBox.setHgrow(aSpacer, Priority.ALWAYS);

        if (existing != null) {
            Button deleteBtn = new Button("Delete");
            deleteBtn.getStyleClass().add("btn-link-danger");
            deleteBtn.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Delete order #" + existing.getId() + "?", ButtonType.YES, ButtonType.NO);
                confirm.showAndWait().ifPresent(r -> {
                    if (r == ButtonType.YES) {
                        orderDAO.delete(existing.getId());
                        MainApp.showToast("Order deleted", "success");
                        closeDrawer();
                        refreshOrders();
                    }
                });
            });
            actions.getChildren().addAll(deleteBtn, aSpacer, cancelBtn, saveBtn);
        } else {
            actions.getChildren().addAll(aSpacer, cancelBtn, saveBtn);
        }

        saveBtn.setOnAction(e -> {
            if (productCombo.getValue() == null) {
                MainApp.showToast("Select a product", "warning"); return;
            }
            int qty;
            try {
                qty = Integer.parseInt(quantityField.getText().trim());
                if (qty <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                MainApp.showToast("Quantity must be a positive integer", "warning"); return;
            }
            if (addressField.getText().trim().isEmpty()) {
                MainApp.showToast("Address is required", "warning"); return;
            }

            Product p = productCombo.getValue();
            if (qty > p.getQuantity()) {
                MainApp.showToast("Only " + p.getQuantity() + " units in stock!", "warning");
            }

            if (existing == null) {
                Order order = new Order(DEFAULT_CUSTOMER_ID, p.getId(), p.getUserId(), qty,
                        p.getPrice(), p.getPrice() * qty, statusCombo.getValue(),
                        addressField.getText().trim(), cityField.getText().trim(),
                        postalField.getText().trim(),
                        notesArea.getText() != null ? notesArea.getText().trim() : "");
                order.setDeliveryDate(deliveryPicker.getValue());
                orderDAO.insert(order);
                MainApp.showToast("Order created!", "success");
            } else {
                existing.setProductId(p.getId());
                existing.setQuantity(qty);
                existing.setUnitPrice(p.getPrice());
                existing.setTotalPrice(p.getPrice() * qty);
                existing.setStatus(statusCombo.getValue());
                existing.setShippingAddress(addressField.getText().trim());
                existing.setShippingCity(cityField.getText().trim());
                existing.setShippingPostal(postalField.getText().trim());
                existing.setNotes(notesArea.getText() != null ? notesArea.getText().trim() : "");
                existing.setDeliveryDate(deliveryPicker.getValue());
                orderDAO.update(existing);
                MainApp.showToast("Order updated!", "success");
            }
            closeDrawer();
            refreshOrders();
        });

        // --- Assemble ---
        VBox drawerPanel = new VBox();
        drawerPanel.getStyleClass().add("drawer-panel");
        drawerPanel.getChildren().addAll(header, bodyScroll, actions);

        Region backdrop = new Region();
        backdrop.getStyleClass().add("drawer-backdrop");
        backdrop.setOnMouseClicked(e -> closeDrawer());
        HBox.setHgrow(backdrop, Priority.ALWAYS);

        drawerOverlay = new HBox();
        drawerOverlay.getChildren().addAll(backdrop, drawerPanel);

        rootStack.getChildren().add(drawerOverlay);
    }

    private void closeDrawer() {
        if (drawerOverlay != null) {
            rootStack.getChildren().remove(drawerOverlay);
            drawerOverlay = null;
        }
    }

    // ==================== ORDER CARD ====================

    private HBox createOrderCard(Order order) {
        HBox card = new HBox(16);
        card.getStyleClass().add("order-card");
        card.setAlignment(Pos.CENTER_LEFT);

        Label idLabel = new Label("#" + order.getId());
        idLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: -agri-green; -fx-min-width: 50;");

        VBox mainInfo = new VBox(2);
        HBox.setHgrow(mainInfo, Priority.ALWAYS);
        Label productLabel = new Label(order.getProductName() != null ? order.getProductName() : "Unknown");
        productLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: -agri-text;");
        String details = "Qty: " + order.getQuantity();
        if (order.getShippingAddress() != null && !order.getShippingAddress().isEmpty())
            details += "  |  " + order.getShippingAddress();
        if (order.getShippingCity() != null && !order.getShippingCity().isEmpty())
            details += ", " + order.getShippingCity();
        Label detailLabel = new Label(details);
        detailLabel.setStyle("-fx-font-size: 11.5px; -fx-text-fill: -agri-text-muted;");
        mainInfo.getChildren().addAll(productLabel, detailLabel);

        VBox priceBox = new VBox(2);
        priceBox.setAlignment(Pos.CENTER_RIGHT);
        Label totalLabel = new Label(String.format("$%.2f", order.getTotalPrice()));
        totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: -agri-green;");
        Label unitLabel = new Label(String.format("$%.2f x %d", order.getUnitPrice(), order.getQuantity()));
        unitLabel.setStyle("-fx-font-size: 10.5px; -fx-text-fill: -agri-text-muted;");
        priceBox.getChildren().addAll(totalLabel, unitLabel);

        VBox dateBox = new VBox(4);
        dateBox.setAlignment(Pos.CENTER_RIGHT);
        dateBox.setMinWidth(90);
        Label statusLabel = new Label(order.getStatus() != null ? order.getStatus() : "pending");
        statusLabel.getStyleClass().add("status-badge");
        statusLabel.getStyleClass().add(STATUS_CLASSES.getOrDefault(
                order.getStatus() != null ? order.getStatus() : "pending", "status-pending"));
        if (order.getOrderDate() != null) {
            Label dateLabel = new Label(order.getOrderDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            dateLabel.setStyle("-fx-font-size: 10.5px; -fx-text-fill: -agri-text-muted;");
            dateBox.getChildren().add(dateLabel);
        }
        dateBox.getChildren().add(statusLabel);

        VBox actionBox = new VBox(4);
        actionBox.setAlignment(Pos.CENTER);
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("btn-icon");
        editBtn.setOnAction(e -> openDrawer(order));
        Button deleteBtn = new Button("Del");
        deleteBtn.getStyleClass().addAll("btn-icon", "btn-icon-danger");
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete order #" + order.getId() + "?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.YES) {
                    orderDAO.delete(order.getId());
                    MainApp.showToast("Order deleted", "success");
                    refreshOrders();
                }
            });
        });
        actionBox.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(idLabel, mainInfo, priceBox, dateBox, actionBox);
        card.setOnMouseClicked(e -> {
            if (e.getTarget() instanceof Button) return;
            openDrawer(order);
        });
        return card;
    }

    // ==================== HELPERS ====================

    private VBox fieldCol(String labelText, Control field) {
        VBox col = new VBox(3);
        col.getStyleClass().add("drawer-field-col");
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("form-label");
        col.getChildren().addAll(lbl, field);
        if (field instanceof TextField) ((TextField) field).setMaxWidth(Double.MAX_VALUE);
        if (field instanceof TextArea) ((TextArea) field).setMaxWidth(Double.MAX_VALUE);
        return col;
    }

    public void refreshProducts() {
        productList.setAll(productDAO.getAll());
    }

    private void refreshOrders() {
        orderListBox.getChildren().clear();
        ObservableList<Order> orders = FXCollections.observableArrayList(orderDAO.getAll());

        if (orders.isEmpty()) {
            VBox emptyState = new VBox(12);
            emptyState.getStyleClass().add("empty-state");
            emptyState.setAlignment(Pos.CENTER);
            Label icon = new Label("?");
            icon.getStyleClass().add("empty-state-icon");
            Label titleLbl = new Label("No orders yet");
            titleLbl.getStyleClass().add("empty-state-title");
            Button addCta = new Button("+ Create First Order");
            addCta.getStyleClass().add("btn-primary");
            addCta.setOnAction(e -> openDrawer(null));
            emptyState.getChildren().addAll(icon, titleLbl, addCta);
            orderListBox.getChildren().add(emptyState);
        } else {
            for (Order o : orders) orderListBox.getChildren().add(createOrderCard(o));
        }
    }
}
