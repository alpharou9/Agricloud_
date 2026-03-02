package org.example.controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.MainApp;
import org.example.model.Order;
import org.example.model.Product;
import org.example.model.User;
import org.example.service.OrderService;
import org.example.service.ProductService;
import org.example.session.UserSession;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * UI controller for the Orders page.
 *
 * Role behaviour:
 *  FARMER - sees only their own orders; can create, edit (if pending), cancel.
 *           The product dropdown shows only APPROVED products.
 *  ADMIN  - sees all orders; can update status on any order; cannot create orders.
 *           "New Order" button is hidden for admins.
 *
 * This controller reads the product catalog via ProductService (one-way dependency).
 * It never touches ProductDAO directly.
 */
public class OrderController {

    // -------------------------------------------------------------------------
    // FXML bindings
    // -------------------------------------------------------------------------
    @FXML private StackPane rootStack;
    @FXML private VBox      orderListBox;
    @FXML private Button    addOrderBtn;

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------
    private static final Map<String, String> STATUS_CLASSES = Map.of(
            "pending",    "status-pending",
            "confirmed",  "status-confirmed",
            "processing", "status-processing",
            "shipped",    "status-shipped",
            "delivered",  "status-delivered",
            "cancelled",  "status-cancelled");

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy");

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------
    private final OrderService   orderService   = new OrderService();
    private final ProductService productService = new ProductService();

    private List<Product> approvedProducts;
    private MainController mainController;

    // Drawer state
    private StackPane drawerOverlay;
    private Order     editingOrder;
    private Order     editingOrderSnapshot;

    // Drawer form fields
    private ComboBox<Product> productCombo;
    private TextField     qtyField;
    private ComboBox<String> statusCombo;
    private TextField     addressField;
    private TextField     cityField;
    private TextField     postalField;
    private TextArea      notesField;
    private DatePicker    deliveryPicker;
    private Label         unitPriceLabel;
    private Label         totalPriceLabel;
    private Label         stockWarningLabel;
    private Label         formError;

    // -------------------------------------------------------------------------
    // Init
    // -------------------------------------------------------------------------

    @FXML
    public void initialize() {
        refreshProducts();
        refreshOrders();
        applyRoleVisibility();
    }

    public void setMainController(MainController mc) {
        this.mainController = mc;
    }

    public void onRoleChanged() {
        applyRoleVisibility();
        refreshOrders();
    }

    /** Reload the approved product list (called when products change). */
    public void refreshProducts() {
        try {
            approvedProducts = productService.getApprovedProducts();
        } catch (Exception e) {
            approvedProducts = List.of();
        }
    }

    public void refreshOrders() {
        orderListBox.getChildren().clear();
        try {
            List<Order> orders;
            User user = UserSession.getInstance().getCurrentUser();
            if (user.getRole() == User.Role.ADMIN) {
                orders = orderService.getAllOrders();
            } else {
                orders = orderService.getFarmerOrders(user.getId());
            }
            if (orders.isEmpty()) {
                Label empty = new Label("No orders yet. Click \"New Order\" to create one.");
                empty.setStyle("-fx-text-fill: #757575; -fx-font-size: 13px;");
                orderListBox.getChildren().add(empty);
            } else {
                for (Order o : orders) {
                    orderListBox.getChildren().add(createOrderCard(o));
                }
            }
        } catch (Exception e) {
            MainApp.getInstance().showToast("Failed to load orders: " + e.getMessage(), "error");
        }
    }

    private void applyRoleVisibility() {
        boolean isFarmer = UserSession.getInstance().isFarmer();
        if (addOrderBtn != null) addOrderBtn.setVisible(isFarmer);
    }

    // -------------------------------------------------------------------------
    // FXML actions
    // -------------------------------------------------------------------------

    @FXML
    void onAddOrder() {
        if (!UserSession.getInstance().isFarmer()) {
            MainApp.getInstance().showToast("Only farmers can place orders.", "error");
            return;
        }
        openDrawer(null);
    }

    // -------------------------------------------------------------------------
    // Order card
    // -------------------------------------------------------------------------

    private Node createOrderCard(Order o) {
        VBox card = new VBox(6);
        card.getStyleClass().add("order-card");
        card.setPadding(new Insets(14));

        // Header row: ID + status
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label idLabel = new Label("#" + o.getId() + " - " + o.getProductName());
        idLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        HBox.setHgrow(idLabel, Priority.ALWAYS);

        Label statusBadge = new Label(o.getStatus());
        String sc = STATUS_CLASSES.getOrDefault(o.getStatus(), "");
        statusBadge.getStyleClass().addAll("status-badge", sc);

        header.getChildren().addAll(idLabel, statusBadge);
        card.getChildren().add(header);

        // Shipping address
        String addr = o.getShippingAddress() != null ? o.getShippingAddress() : "";
        if (o.getShippingCity() != null && !o.getShippingCity().isBlank())
            addr += ", " + o.getShippingCity();
        Label addrLabel = new Label("Location: " + addr);
        addrLabel.setStyle("-fx-text-fill: #757575; -fx-font-size: 11px;");
        card.getChildren().add(addrLabel);

        // Pricing
        Label priceLabel = new Label(
                String.format("Qty: %d  x  $%.2f  =  $%.2f",
                              o.getQuantity(), o.getUnitPrice(), o.getTotalPrice()));
        priceLabel.setStyle("-fx-font-size: 12px;");
        card.getChildren().add(priceLabel);

        // Order date
        if (o.getOrderDate() != null) {
            Label dateLabel = new Label("Ordered: " + o.getOrderDate().format(DATE_FMT));
            dateLabel.setStyle("-fx-text-fill: #757575; -fx-font-size: 11px;");
            card.getChildren().add(dateLabel);
        }

        // Action buttons
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        User user = UserSession.getInstance().getCurrentUser();
        boolean canEdit = user.getRole() == User.Role.ADMIN ||
                (user.getRole() == User.Role.FARMER &&
                 o.getCustomerId() == user.getId() &&
                 "pending".equals(o.getStatus()));

        if (canEdit) {
            Button edit = new Button("Edit");
            edit.getStyleClass().add("btn-secondary");
            edit.setOnAction(e -> openDrawer(o));
            actions.getChildren().add(edit);
        }

        boolean canCancel = !("cancelled".equals(o.getStatus()) ||
                              "delivered".equals(o.getStatus())) &&
                            (user.getRole() == User.Role.ADMIN ||
                             o.getCustomerId() == user.getId());
        if (canCancel) {
            Button cancel = new Button("Cancel");
            cancel.getStyleClass().add("btn-danger");
            cancel.setOnAction(e -> cancelOrder(o));
            actions.getChildren().add(cancel);
        }

        // Admin: status update dropdown - uses updateOrderStatus() to avoid stock-diff issues
        if (user.getRole() == User.Role.ADMIN) {
            ComboBox<String> statusUpdate = new ComboBox<>();
            statusUpdate.getItems().addAll(
                    "pending","confirmed","processing","shipped","delivered","cancelled");
            statusUpdate.setValue(o.getStatus());
            final String[] currentStatus = {o.getStatus()};
            statusUpdate.setOnAction(e -> {
                String newStatus = statusUpdate.getValue();
                if (!newStatus.equals(currentStatus[0])) {
                    try {
                        orderService.updateOrderStatus(o.getId(), newStatus, user);
                        currentStatus[0] = newStatus;
                        refreshOrders();
                        MainApp.getInstance().showToast("Order status updated.", "success");
                        if (mainController != null) mainController.notifyProductsChanged();
                    } catch (Exception ex) {
                        MainApp.getInstance().showToast(ex.getMessage(), "error");
                        statusUpdate.setValue(currentStatus[0]);
                    }
                }
            });
            actions.getChildren().add(statusUpdate);
        }

        if (!actions.getChildren().isEmpty()) card.getChildren().add(actions);
        return card;
    }

    // -------------------------------------------------------------------------
    // Cancel order
    // -------------------------------------------------------------------------

    private void cancelOrder(Order o) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel Order");
        alert.setHeaderText("Cancel order #" + o.getId() + "?");
        alert.setContentText("Stock will be restored to the product.");
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    orderService.cancelOrder(o.getId(),
                            UserSession.getInstance().getCurrentUser());
                    MainApp.getInstance().showToast("Order cancelled. Stock restored.", "info");
                    refreshOrders();
                    if (mainController != null) mainController.notifyProductsChanged();
                } catch (Exception e) {
                    MainApp.getInstance().showToast(e.getMessage(), "error");
                }
            }
        });
    }

    // -------------------------------------------------------------------------
    // Drawer - Add / Edit form
    // -------------------------------------------------------------------------

    private void openDrawer(Order existing) {
        editingOrder         = existing;
        editingOrderSnapshot = copyOrder(existing);

        drawerOverlay = new StackPane();
        drawerOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.4);");
        drawerOverlay.setAlignment(Pos.CENTER_RIGHT);
        drawerOverlay.setOnMouseClicked(e -> {
            if (e.getTarget() == drawerOverlay) closeDrawer();
        });

        VBox panel = new VBox(12);
        panel.setMaxWidth(400);
        panel.setPrefWidth(400);
        // Inline style to avoid CSS variable resolution issues in setStyle()
        panel.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 24;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 24, 0, -4, 0);");

        Label title = new Label(existing == null ? "New Order" : "Edit Order");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Product dropdown (reads from ProductService - one-way dependency)
        productCombo = new ComboBox<>();
        productCombo.getItems().addAll(approvedProducts);
        productCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Product p) {
                if (p == null) return "";
                return p.getName() + " ($" + p.getPrice() + "/" + p.getUnit() +
                       " - stock: " + p.getQuantity() + ")";
            }
            @Override public Product fromString(String s) { return null; }
        });
        productCombo.setMaxWidth(Double.MAX_VALUE);

        stockWarningLabel = new Label("");
        stockWarningLabel.setStyle("-fx-text-fill: #FB8C00; -fx-font-size: 11px;");
        unitPriceLabel  = new Label("Unit Price: -");
        totalPriceLabel = new Label("Total: -");
        totalPriceLabel.setStyle("-fx-font-weight: bold;");

        if (existing != null && approvedProducts != null) {
            approvedProducts.stream()
                    .filter(pp -> pp.getId() == existing.getProductId())
                    .findFirst()
                    .ifPresent(productCombo::setValue);
        }

        qtyField = new TextField(existing != null ? String.valueOf(existing.getQuantity()) : "1");
        qtyField.setPromptText("Quantity *");

        productCombo.valueProperty().addListener((obs, o, n) -> updatePriceCalc());
        qtyField.textProperty().addListener((obs, o, n) -> updatePriceCalc());
        updatePriceCalc();

        statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(
                "pending","confirmed","processing","shipped","delivered","cancelled");
        statusCombo.setValue(existing != null ? existing.getStatus() : "pending");
        statusCombo.setMaxWidth(Double.MAX_VALUE);
        statusCombo.setDisable(existing == null);

        addressField = new TextField(existing != null ? nvl(existing.getShippingAddress()) : "");
        addressField.setPromptText("Shipping address *");

        cityField = new TextField(existing != null ? nvl(existing.getShippingCity()) : "");
        cityField.setPromptText("City");

        postalField = new TextField(existing != null ? nvl(existing.getShippingPostal()) : "");
        postalField.setPromptText("Postal code");

        notesField = new TextArea(existing != null ? nvl(existing.getNotes()) : "");
        notesField.setPromptText("Notes / special instructions");
        notesField.setPrefRowCount(2);
        notesField.setWrapText(true);

        deliveryPicker = new DatePicker(existing != null ? existing.getDeliveryDate() : null);
        deliveryPicker.setMaxWidth(Double.MAX_VALUE);

        formError = new Label("");
        formError.setStyle("-fx-text-fill: #F44336; -fx-font-size: 11px;");
        formError.setWrapText(true);

        Button save   = new Button(existing == null ? "Place Order" : "Save Changes");
        Button cancel = new Button("Cancel");
        save.getStyleClass().add("btn-primary");
        cancel.getStyleClass().add("btn-secondary");
        save.setOnAction(e -> submitForm());
        cancel.setOnAction(e -> closeDrawer());
        save.setMaxWidth(Double.MAX_VALUE);
        cancel.setMaxWidth(Double.MAX_VALUE);

        panel.getChildren().addAll(
                title,
                labeled("Product *", productCombo),
                stockWarningLabel,
                unitPriceLabel,
                labeled("Quantity *", qtyField),
                totalPriceLabel,
                labeled("Status", statusCombo),
                labeled("Shipping Address *", addressField),
                labeled("City", cityField),
                labeled("Postal Code", postalField),
                labeled("Notes", notesField),
                labeled("Delivery Date", deliveryPicker),
                formError,
                save, cancel);

        drawerOverlay.getChildren().add(panel);
        rootStack.getChildren().add(drawerOverlay);
    }

    private void closeDrawer() {
        rootStack.getChildren().remove(drawerOverlay);
        drawerOverlay = null;
    }

    private void updatePriceCalc() {
        Product selected = productCombo.getValue();
        if (selected == null) {
            unitPriceLabel.setText("Unit Price: -");
            totalPriceLabel.setText("Total: -");
            stockWarningLabel.setText("");
            return;
        }
        unitPriceLabel.setText(String.format("Unit Price: $%.2f / %s",
                                              selected.getPrice(), selected.getUnit()));
        try {
            int qty = Integer.parseInt(qtyField.getText().trim());
            totalPriceLabel.setText(String.format("Total: $%.2f", selected.getPrice() * qty));
            if (qty > selected.getQuantity()) {
                stockWarningLabel.setText(
                        "Only " + selected.getQuantity() + " " + selected.getUnit() + " in stock.");
            } else {
                stockWarningLabel.setText("");
            }
        } catch (NumberFormatException e) {
            totalPriceLabel.setText("Total: -");
        }
    }

    private void submitForm() {
        formError.setText("");
        Product selectedProduct = productCombo.getValue();
        if (selectedProduct == null) {
            formError.setText("Please select a product.");
            return;
        }
        int qty;
        try {
            qty = Integer.parseInt(qtyField.getText().trim());
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            formError.setText("Quantity must be a positive integer.");
            return;
        }
        String address = addressField.getText().trim();
        if (address.isBlank()) {
            formError.setText("Shipping address is required.");
            return;
        }

        Order o = editingOrder != null ? editingOrder : new Order();
        o.setProductId(selectedProduct.getId());
        o.setQuantity(qty);
        o.setUnitPrice(selectedProduct.getPrice());
        o.setTotalPrice(selectedProduct.getPrice() * qty);
        o.setStatus(statusCombo.getValue());
        o.setShippingAddress(address);
        o.setShippingCity(cityField.getText().trim());
        o.setShippingPostal(postalField.getText().trim());
        o.setNotes(notesField.getText().trim());
        o.setDeliveryDate(deliveryPicker.getValue());

        try {
            User user = UserSession.getInstance().getCurrentUser();
            if (editingOrder == null) {
                orderService.createOrder(o, user);
                MainApp.getInstance().showToast("Order placed successfully.", "success");
            } else {
                orderService.updateOrder(o, editingOrderSnapshot, user);
                MainApp.getInstance().showToast("Order updated.", "success");
            }
            closeDrawer();
            refreshOrders();
            refreshProducts();
            if (mainController != null) mainController.notifyProductsChanged();
        } catch (Exception e) {
            formError.setText(e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Node labeled(String text, Node control) {
        VBox box = new VBox(4);
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #757575;");
        box.getChildren().addAll(lbl, control);
        return box;
    }

    private String nvl(String s) { return s != null ? s : ""; }

    /** Shallow snapshot of an Order for stock-diff calculation in updateOrder(). */
    private Order copyOrder(Order src) {
        if (src == null) return null;
        Order copy = new Order();
        copy.setId(src.getId());
        copy.setCustomerId(src.getCustomerId());
        copy.setProductId(src.getProductId());
        copy.setSellerId(src.getSellerId());
        copy.setQuantity(src.getQuantity());
        copy.setUnitPrice(src.getUnitPrice());
        copy.setTotalPrice(src.getTotalPrice());
        copy.setStatus(src.getStatus());
        return copy;
    }
}
