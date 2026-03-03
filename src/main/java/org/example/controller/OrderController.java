package org.example.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.example.MainApp;
import org.example.model.Order;
import org.example.model.OrderDetail;
import org.example.model.Product;
import org.example.model.User;
import org.example.service.OrderService;
import org.example.service.ProductService;
import org.example.session.UserSession;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for orders-view.fxml.
 *
 * Tab 1 "New Order"  – product catalog (paginated) + cart + place order.
 * Tab 2 "Manage Orders" – orders table (paginated) + detail rows + edit/delete.
 */
public class OrderController {

    // =========================================================================
    // FXML bindings
    // =========================================================================

    @FXML private StackPane rootStack;
    @FXML private TabPane   tabPane;
    @FXML private Tab       newOrderTab;

    // ── Tab 1: catalog ──
    @FXML private FlowPane  productGrid;
    @FXML private TextField searchField;
    @FXML private Button    prevProductBtn;
    @FXML private Button    nextProductBtn;
    @FXML private Label     productPageLabel;

    // ── Tab 1: cart ──
    @FXML private TableView<CartItem> cartTable;
    @FXML private Label     totalLabel;
    @FXML private TextField shippingField;
    @FXML private TextField cityField;
    @FXML private TextField postalField;
    @FXML private TextArea  notesField;
    @FXML private Label     orderFormError;

    // ── Tab 2: dashboard ──
    @FXML private TableView<Order>       ordersTable;
    @FXML private Button                 prevOrderBtn;
    @FXML private Button                 nextOrderBtn;
    @FXML private Label                  orderPageLabel;
    @FXML private TableView<OrderDetail> detailsTable;

    // =========================================================================
    // Constants
    // =========================================================================

    private static final int    PRODUCT_PAGE_SIZE = 9;
    private static final int    ORDER_PAGE_SIZE   = 10;
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy");

    // =========================================================================
    // State
    // =========================================================================

    private final ObservableList<CartItem> cartItems = FXCollections.observableArrayList();

    private int productPage       = 0;
    private int productTotalPages = 1;
    private int orderPage         = 0;
    private int orderTotalPages   = 1;

    private MainController mainController;

    // Drawer (edit order)
    private StackPane drawerOverlay;

    private final OrderService   orderService   = new OrderService();
    private final ProductService productService = new ProductService();

    // =========================================================================
    // Initialization
    // =========================================================================

    @FXML
    public void initialize() {
        setupCartTable();
        setupOrdersTable();
        setupDetailsTable();
        applyRoleVisibility();
        loadProductPage();
        loadOrderPage();
    }

    public void setMainController(MainController mc) { this.mainController = mc; }

    /** Called by MainController after a role switch. */
    public void onRoleChanged() {
        applyRoleVisibility();
        productPage = 0;
        orderPage   = 0;
        loadProductPage();
        loadOrderPage();
    }

    /** Called by MainController when products change (stock update). */
    public void refreshProducts() {
        productPage = 0;
        loadProductPage();
    }

    /** Called by MainController when navigating to the Orders page. */
    public void refreshOrders() {
        orderPage = 0;
        loadOrderPage();
    }

    // =========================================================================
    // Role visibility
    // =========================================================================

    private void applyRoleVisibility() {
        boolean isFarmer = UserSession.getInstance().isFarmer();
        // New Order tab is only meaningful for farmers
        newOrderTab.setDisable(!isFarmer);
        if (!isFarmer && tabPane != null) {
            tabPane.getSelectionModel().select(1);
        }
    }

    // =========================================================================
    // ── TAB 1: PRODUCT CATALOG ────────────────────────────────────────────────
    // =========================================================================

    private void loadProductPage() {
        String search = searchField == null ? "" : searchField.getText().trim();
        try {
            int total = productService.countApprovedProducts(search);
            productTotalPages = Math.max(1, (int) Math.ceil((double) total / PRODUCT_PAGE_SIZE));
            if (productPage >= productTotalPages) productPage = productTotalPages - 1;

            List<Product> products = productService.getApprovedProductsPage(
                    search, PRODUCT_PAGE_SIZE, productPage * PRODUCT_PAGE_SIZE);

            productGrid.getChildren().clear();
            if (products.isEmpty()) {
                Label empty = new Label("No products available.");
                empty.setStyle("-fx-text-fill: #757575; -fx-font-size: 13px;");
                productGrid.getChildren().add(empty);
            } else {
                for (Product p : products) {
                    productGrid.getChildren().add(createProductCard(p));
                }
            }
            productPageLabel.setText("Page " + (productPage + 1) + " / " + productTotalPages);
            prevProductBtn.setDisable(productPage == 0);
            nextProductBtn.setDisable(productPage >= productTotalPages - 1);

        } catch (Exception e) {
            MainApp.getInstance().showToast("Failed to load products: " + e.getMessage(), "error");
        }
    }

    @FXML void onSearchProducts()    { productPage = 0; loadProductPage(); }
    @FXML void onPrevProductPage()   { if (productPage > 0) { productPage--; loadProductPage(); } }
    @FXML void onNextProductPage()   { if (productPage < productTotalPages - 1) { productPage++; loadProductPage(); } }

    // ── Product card ─────────────────────────────────────────────────────────

    private static final String CARD_STYLE_BASE =
            "-fx-background-color: white;" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: #E0E0E0;" +
            "-fx-border-radius: 8;" +
            "-fx-padding: 10;";
    private static final String CARD_STYLE_HOVER =
            "-fx-background-color: white;" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: #4CAF50;" +
            "-fx-border-radius: 8;" +
            "-fx-padding: 10;";

    private Node createProductCard(Product p) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(155);
        card.setStyle(CARD_STYLE_BASE);

        // Clicking anywhere on the card adds the product to the cart
        if (p.getQuantity() > 0) {
            card.setCursor(javafx.scene.Cursor.HAND);
            card.setOnMouseClicked(e -> onAddToCart(p));
            card.setOnMouseEntered(e -> card.setStyle(CARD_STYLE_HOVER));
            card.setOnMouseExited(e -> card.setStyle(CARD_STYLE_BASE));
        }

        // Thumbnail
        StackPane imgBox = new StackPane();
        imgBox.setPrefSize(135, 85);
        imgBox.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 6;");
        if (p.getImage() != null && !p.getImage().isBlank()) {
            File f = new File(p.getImage());
            if (f.exists()) {
                ImageView iv = new ImageView(
                        new Image(f.toURI().toString(), 135, 85, true, true));
                iv.setFitWidth(135);
                iv.setFitHeight(85);
                iv.setPreserveRatio(true);
                imgBox.getChildren().add(iv);
            } else {
                imgBox.getChildren().add(placeholder(p.getCategory()));
            }
        } else {
            imgBox.getChildren().add(placeholder(p.getCategory()));
        }
        card.getChildren().add(imgBox);

        // Name
        Label name = new Label(p.getName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        name.setWrapText(true);
        name.setMaxWidth(135);

        // Price
        Label price = new Label(String.format("$%.2f / %s", p.getPrice(), p.getUnit()));
        price.setStyle("-fx-text-fill: #2E7D32; -fx-font-size: 11px;");

        // Stock
        Label stock = new Label("Stock: " + p.getQuantity());
        stock.setStyle("-fx-text-fill: #757575; -fx-font-size: 10px;");

        // Add button
        Button add = new Button(p.getQuantity() > 0 ? "Add to Order" : "Out of Stock");
        add.getStyleClass().add("btn-primary");
        add.setMaxWidth(Double.MAX_VALUE);
        add.setDisable(p.getQuantity() == 0);
        add.setOnAction(e -> onAddToCart(p));

        card.getChildren().addAll(name, price, stock, add);
        return card;
    }

    private Label placeholder(String category) {
        String text = category != null ? category.substring(0, Math.min(3, category.length())) : "?";
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: #9E9E9E; -fx-font-size: 13px;");
        return lbl;
    }

    // =========================================================================
    // ── TAB 1: CART ──────────────────────────────────────────────────────────
    // =========================================================================

    @SuppressWarnings("unchecked")
    private void setupCartTable() {
        // Image
        TableColumn<CartItem, CartItem> imgCol = new TableColumn<>("");
        imgCol.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue()));
        imgCol.setCellFactory(col -> new TableCell<>() {
            private final ImageView iv = new ImageView();
            { iv.setFitWidth(40); iv.setFitHeight(40); iv.setPreserveRatio(true); }
            @Override protected void updateItem(CartItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                String img = item.getProduct().getImage();
                if (img != null && !img.isBlank()) {
                    File f = new File(img);
                    if (f.exists()) {
                        iv.setImage(new Image(f.toURI().toString(), 40, 40, true, true));
                        setGraphic(iv);
                        return;
                    }
                }
                Label lbl = new Label("—");
                lbl.setStyle("-fx-text-fill: #9E9E9E;");
                setGraphic(lbl);
            }
        });
        imgCol.setPrefWidth(55);
        imgCol.setSortable(false);

        // Name
        TableColumn<CartItem, String> nameCol = new TableColumn<>("Product");
        nameCol.setCellValueFactory(p ->
                new SimpleStringProperty(p.getValue().getProduct().getName()));
        nameCol.setPrefWidth(120);

        // Unit price
        TableColumn<CartItem, String> priceCol = new TableColumn<>("Unit Price");
        priceCol.setCellValueFactory(p -> new SimpleStringProperty(
                String.format("$%.2f", p.getValue().getProduct().getPrice())));
        priceCol.setPrefWidth(75);

        // Qty  (– label +  buttons)
        TableColumn<CartItem, CartItem> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue()));
        qtyCol.setCellFactory(col -> new TableCell<>() {
            private final Button minus = new Button("−");
            private final Label  num   = new Label();
            private final Button plus  = new Button("+");
            private final HBox   box   = new HBox(4, minus, num, plus);
            {
                box.setAlignment(Pos.CENTER);
                minus.setStyle("-fx-min-width: 24; -fx-min-height: 24; -fx-padding: 0;");
                plus.setStyle ("-fx-min-width: 24; -fx-min-height: 24; -fx-padding: 0;");
                minus.setOnAction(e -> {
                    CartItem ci = getTableView().getItems().get(getIndex());
                    if (ci.getQuantity() > 1) {
                        ci.setQuantity(ci.getQuantity() - 1);
                    } else {
                        cartItems.remove(ci);
                    }
                    getTableView().refresh();
                    updateTotals();
                });
                plus.setOnAction(e -> {
                    CartItem ci = getTableView().getItems().get(getIndex());
                    ci.setQuantity(ci.getQuantity() + 1);
                    getTableView().refresh();
                    updateTotals();
                });
            }
            @Override protected void updateItem(CartItem ci, boolean empty) {
                super.updateItem(ci, empty);
                if (empty || ci == null) { setGraphic(null); return; }
                num.setText(String.valueOf(ci.getQuantity()));
                setGraphic(box);
            }
        });
        qtyCol.setPrefWidth(90);
        qtyCol.setSortable(false);

        // Subtotal
        TableColumn<CartItem, String> subCol = new TableColumn<>("Subtotal");
        subCol.setCellValueFactory(p -> new SimpleStringProperty(
                String.format("$%.2f", p.getValue().getSubtotal())));
        subCol.setPrefWidth(75);

        // Remove
        TableColumn<CartItem, CartItem> removeCol = new TableColumn<>("");
        removeCol.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue()));
        removeCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("✕");
            { btn.getStyleClass().add("btn-danger");
              btn.setStyle("-fx-min-width: 28; -fx-min-height: 24; -fx-padding: 0 6;");
              btn.setOnAction(e -> {
                  cartItems.remove(getTableView().getItems().get(getIndex()));
                  updateTotals();
              }); }
            @Override protected void updateItem(CartItem ci, boolean empty) {
                super.updateItem(ci, empty);
                setGraphic(empty || ci == null ? null : btn);
            }
        });
        removeCol.setPrefWidth(45);
        removeCol.setSortable(false);

        cartTable.getColumns().addAll(imgCol, nameCol, priceCol, qtyCol, subCol, removeCol);
        cartTable.setItems(cartItems);
        cartTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        cartTable.setFixedCellSize(50);
        cartTable.setPlaceholder(new Label("No items. Click \"Add to Order\" on a product."));
    }

    private void onAddToCart(Product p) {
        for (CartItem ci : cartItems) {
            if (ci.getProduct().getId() == p.getId()) {
                ci.setQuantity(ci.getQuantity() + 1);
                cartTable.refresh();
                updateTotals();
                return;
            }
        }
        cartItems.add(new CartItem(p, 1));
        updateTotals();
    }

    private void updateTotals() {
        double total = cartItems.stream().mapToDouble(CartItem::getSubtotal).sum();
        totalLabel.setText(String.format("$%.2f", total));
    }

    @FXML void onClearCart() {
        cartItems.clear();
        updateTotals();
    }

    @FXML
    void onPlaceOrder() {
        orderFormError.setText("");
        if (cartItems.isEmpty()) {
            orderFormError.setText("Add at least one product to the order.");
            return;
        }
        String address = shippingField.getText().trim();
        if (address.isBlank()) {
            orderFormError.setText("Shipping address is required.");
            return;
        }

        Order order = new Order();
        order.setShippingAddress(address);
        order.setShippingCity(cityField.getText().trim());
        order.setShippingPostal(postalField.getText().trim());
        order.setNotes(notesField.getText().trim());

        List<OrderDetail> details = new ArrayList<>();
        for (CartItem ci : cartItems) {
            OrderDetail d = new OrderDetail();
            d.setProductId(ci.getProduct().getId());
            d.setQuantity(ci.getQuantity());
            details.add(d);
        }

        try {
            orderService.createOrder(order, details, UserSession.getInstance().getCurrentUser());
            MainApp.getInstance().showToast("Order placed successfully!", "success");
            cartItems.clear();
            updateTotals();
            shippingField.clear();
            cityField.clear();
            postalField.clear();
            notesField.clear();
            loadProductPage();    // refresh stock numbers
            loadOrderPage();
            if (mainController != null) mainController.notifyProductsChanged();
        } catch (Exception e) {
            orderFormError.setText(e.getMessage());
        }
    }

    // =========================================================================
    // ── TAB 2: ORDERS DASHBOARD ───────────────────────────────────────────────
    // =========================================================================

    private void loadOrderPage() {
        User user = UserSession.getInstance().getCurrentUser();
        try {
            int total = user.getRole() == User.Role.ADMIN
                    ? orderService.countAllOrders()
                    : orderService.countFarmerOrders(user.getId());

            orderTotalPages = Math.max(1, (int) Math.ceil((double) total / ORDER_PAGE_SIZE));
            if (orderPage >= orderTotalPages) orderPage = orderTotalPages - 1;

            List<Order> orders = user.getRole() == User.Role.ADMIN
                    ? orderService.getAllOrdersPage(ORDER_PAGE_SIZE, orderPage * ORDER_PAGE_SIZE)
                    : orderService.getFarmerOrdersPage(user.getId(), ORDER_PAGE_SIZE, orderPage * ORDER_PAGE_SIZE);

            ordersTable.setItems(FXCollections.observableArrayList(orders));
            detailsTable.setItems(FXCollections.observableArrayList());

            orderPageLabel.setText("Page " + (orderPage + 1) + " / " + orderTotalPages);
            prevOrderBtn.setDisable(orderPage == 0);
            nextOrderBtn.setDisable(orderPage >= orderTotalPages - 1);

        } catch (Exception e) {
            MainApp.getInstance().showToast("Failed to load orders: " + e.getMessage(), "error");
        }
    }

    @FXML void onPrevOrderPage() { if (orderPage > 0) { orderPage--; loadOrderPage(); } }
    @FXML void onNextOrderPage() { if (orderPage < orderTotalPages - 1) { orderPage++; loadOrderPage(); } }

    // ── Orders table ──────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private void setupOrdersTable() {
        TableColumn<Order, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(p -> new SimpleStringProperty("#" + p.getValue().getId()));
        idCol.setPrefWidth(55);

        TableColumn<Order, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(p ->
                new SimpleStringProperty(p.getValue().getFormattedDate()));
        dateCol.setPrefWidth(110);

        TableColumn<Order, String> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(p -> new SimpleStringProperty(
                String.format("$%.2f", p.getValue().getTotalPrice())));
        totalCol.setPrefWidth(80);

        TableColumn<Order, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(p ->
                new SimpleStringProperty(p.getValue().getStatus()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); return; }
                Label badge = new Label(s);
                badge.getStyleClass().addAll("status-badge", "status-" + s);
                setGraphic(badge);
                setText(null);
            }
        });
        statusCol.setPrefWidth(90);

        TableColumn<Order, Order> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue()));
        actionsCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Order o, boolean empty) {
                super.updateItem(o, empty);
                if (empty || o == null) { setGraphic(null); return; }
                HBox box = new HBox(6);
                box.setAlignment(Pos.CENTER_LEFT);
                User user = UserSession.getInstance().getCurrentUser();

                Button edit = new Button("Edit");
                edit.getStyleClass().add("btn-secondary");
                edit.setOnAction(e -> openEditDrawer(o));
                box.getChildren().add(edit);

                if (user.getRole() == User.Role.ADMIN) {
                    Button delete = new Button("Delete");
                    delete.getStyleClass().add("btn-danger");
                    delete.setOnAction(e -> confirmDelete(o));
                    box.getChildren().add(delete);
                } else if (!("cancelled".equals(o.getStatus()) ||
                             "delivered".equals(o.getStatus()))) {
                    Button cancel = new Button("Cancel");
                    cancel.getStyleClass().add("btn-danger");
                    cancel.setOnAction(e -> confirmCancel(o));
                    box.getChildren().add(cancel);
                }
                setGraphic(box);
            }
        });
        actionsCol.setPrefWidth(160);
        actionsCol.setSortable(false);

        ordersTable.getColumns().addAll(idCol, dateCol, totalCol, statusCol, actionsCol);
        ordersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        ordersTable.setPlaceholder(new Label("No orders yet."));

        // Load detail rows when an order row is selected
        ordersTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) loadDetailsFor(selected);
            else detailsTable.setItems(FXCollections.observableArrayList());
        });
    }

    // ── Detail rows ───────────────────────────────────────────────────────────

    private void loadDetailsFor(Order order) {
        try {
            List<OrderDetail> details = orderService.getOrderDetails(order.getId());
            detailsTable.setItems(FXCollections.observableArrayList(details));
        } catch (Exception e) {
            MainApp.getInstance().showToast("Could not load order details: " + e.getMessage(), "error");
        }
    }

    @SuppressWarnings("unchecked")
    private void setupDetailsTable() {
        TableColumn<OrderDetail, OrderDetail> imgCol = new TableColumn<>("");
        imgCol.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue()));
        imgCol.setCellFactory(col -> new TableCell<>() {
            private final ImageView iv = new ImageView();
            { iv.setFitWidth(38); iv.setFitHeight(38); iv.setPreserveRatio(true); }
            @Override protected void updateItem(OrderDetail d, boolean empty) {
                super.updateItem(d, empty);
                if (empty || d == null) { setGraphic(null); return; }
                String img = d.getProductImage();
                if (img != null && !img.isBlank()) {
                    File f = new File(img);
                    if (f.exists()) {
                        iv.setImage(new Image(f.toURI().toString(), 38, 38, true, true));
                        setGraphic(iv);
                        return;
                    }
                }
                Label lbl = new Label("—");
                lbl.setStyle("-fx-text-fill: #9E9E9E;");
                setGraphic(lbl);
            }
        });
        imgCol.setPrefWidth(50);
        imgCol.setSortable(false);

        TableColumn<OrderDetail, String> nameCol = new TableColumn<>("Product");
        nameCol.setCellValueFactory(p ->
                new SimpleStringProperty(p.getValue().getProductName()));
        nameCol.setPrefWidth(150);

        TableColumn<OrderDetail, String> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(p -> new SimpleStringProperty(
                p.getValue().getQuantity() + " " + nvl(p.getValue().getProductUnit())));
        qtyCol.setPrefWidth(70);

        TableColumn<OrderDetail, String> unitPriceCol = new TableColumn<>("Unit Price");
        unitPriceCol.setCellValueFactory(p -> new SimpleStringProperty(
                String.format("$%.2f", p.getValue().getUnitPrice())));
        unitPriceCol.setPrefWidth(80);

        TableColumn<OrderDetail, String> subtotalCol = new TableColumn<>("Subtotal");
        subtotalCol.setCellValueFactory(p -> new SimpleStringProperty(
                String.format("$%.2f", p.getValue().getSubtotal())));
        subtotalCol.setPrefWidth(80);

        detailsTable.getColumns().addAll(imgCol, nameCol, qtyCol, unitPriceCol, subtotalCol);
        detailsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        detailsTable.setFixedCellSize(48);
        detailsTable.setPlaceholder(new Label("Select an order above to see its items."));
    }

    // =========================================================================
    // ── EDIT ORDER DRAWER ────────────────────────────────────────────────────
    // =========================================================================

    private void openEditDrawer(Order order) {
        List<OrderDetail> existingDetails;
        try {
            existingDetails = orderService.getOrderDetails(order.getId());
        } catch (Exception e) {
            MainApp.getInstance().showToast("Failed to load order: " + e.getMessage(), "error");
            return;
        }

        // Cart items for the drawer
        ObservableList<CartItem> editCart = FXCollections.observableArrayList();
        for (OrderDetail d : existingDetails) {
            // Re-use CartItem with a minimal Product shell for display
            Product shell = new Product();
            shell.setId(d.getProductId());
            shell.setName(d.getProductName() != null ? d.getProductName() : "Unknown");
            shell.setImage(d.getProductImage());
            shell.setUnit(d.getProductUnit());
            shell.setPrice(d.getUnitPrice());
            shell.setQuantity(Integer.MAX_VALUE); // no stock cap in edit mode
            editCart.add(new CartItem(shell, d.getQuantity()));
        }

        drawerOverlay = new StackPane();
        drawerOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.45);");
        drawerOverlay.setAlignment(Pos.CENTER_RIGHT);
        drawerOverlay.setOnMouseClicked(e -> {
            if (e.getTarget() == drawerOverlay) closeDrawer();
        });

        VBox panel = new VBox(12);
        panel.setMaxWidth(420);
        panel.setPrefWidth(420);
        panel.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 24;" +
            "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.35),24,0,-4,0);");

        Label title = new Label("Edit Order #" + order.getId());
        title.setStyle("-fx-font-size: 17px; -fx-font-weight: bold;");

        // Status
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(
                "pending","confirmed","processing","shipped","delivered","cancelled");
        statusCombo.setValue(order.getStatus());
        statusCombo.setMaxWidth(Double.MAX_VALUE);

        // Shipping
        TextField addrField   = new TextField(nvl(order.getShippingAddress()));
        TextField cityFld     = new TextField(nvl(order.getShippingCity()));
        TextField postalFld   = new TextField(nvl(order.getShippingPostal()));
        TextArea  notesFld    = new TextArea(nvl(order.getNotes()));
        notesFld.setPrefRowCount(2);
        notesFld.setWrapText(true);

        // Editable items table inside the drawer
        TableView<CartItem> editTable = new TableView<>(editCart);
        editTable.setPrefHeight(180);
        editTable.setFixedCellSize(46);

        TableColumn<CartItem, String> eName = new TableColumn<>("Product");
        eName.setCellValueFactory(p ->
                new SimpleStringProperty(p.getValue().getProduct().getName()));
        eName.setPrefWidth(130);

        TableColumn<CartItem, CartItem> eQty = buildEditQtyColumn(editCart, editTable);
        eQty.setPrefWidth(95);

        TableColumn<CartItem, String> eSub = new TableColumn<>("Subtotal");
        eSub.setCellValueFactory(p -> new SimpleStringProperty(
                String.format("$%.2f", p.getValue().getSubtotal())));
        eSub.setPrefWidth(75);

        TableColumn<CartItem, CartItem> eRm = new TableColumn<>("");
        eRm.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue()));
        eRm.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("✕");
            { btn.getStyleClass().add("btn-danger");
              btn.setStyle("-fx-min-width:26;-fx-min-height:22;-fx-padding:0 5;");
              btn.setOnAction(e -> { editCart.remove(getTableView().getItems().get(getIndex())); }); }
            @Override protected void updateItem(CartItem ci, boolean empty) {
                super.updateItem(ci, empty);
                setGraphic(empty || ci == null ? null : btn);
            }
        });
        eRm.setPrefWidth(40);
        eRm.setSortable(false);

        //noinspection unchecked
        editTable.getColumns().addAll(eName, eQty, eSub, eRm);
        editTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Label drawerError = new Label("");
        drawerError.setStyle("-fx-text-fill: #F44336; -fx-font-size: 11px;");
        drawerError.setWrapText(true);

        Button save   = new Button("Save Changes");
        Button cancel = new Button("Cancel");
        save.getStyleClass().add("btn-primary");
        cancel.getStyleClass().add("btn-secondary");
        save.setMaxWidth(Double.MAX_VALUE);
        cancel.setMaxWidth(Double.MAX_VALUE);
        cancel.setOnAction(e -> closeDrawer());

        save.setOnAction(e -> {
            drawerError.setText("");
            if (editCart.isEmpty()) {
                drawerError.setText("Order must have at least one item.");
                return;
            }
            // Build new details list from editCart
            List<OrderDetail> newDetails = new ArrayList<>();
            for (CartItem ci : editCart) {
                OrderDetail d = new OrderDetail();
                d.setProductId(ci.getProduct().getId());
                d.setQuantity(ci.getQuantity());
                newDetails.add(d);
            }
            order.setStatus(statusCombo.getValue());
            order.setShippingAddress(addrField.getText().trim());
            order.setShippingCity(cityFld.getText().trim());
            order.setShippingPostal(postalFld.getText().trim());
            order.setNotes(notesFld.getText().trim());
            try {
                orderService.updateOrder(order, newDetails,
                        UserSession.getInstance().getCurrentUser());
                MainApp.getInstance().showToast("Order updated.", "success");
                closeDrawer();
                loadOrderPage();
                if (mainController != null) mainController.notifyProductsChanged();
            } catch (Exception ex) {
                drawerError.setText(ex.getMessage());
            }
        });

        panel.getChildren().addAll(
                title,
                labeled("Status", statusCombo),
                labeled("Shipping Address", addrField),
                new HBox(8, withLabel("City", cityFld), withLabel("Postal", postalFld)),
                labeled("Notes", notesFld),
                new Label("Items:"),
                editTable,
                drawerError,
                save, cancel);

        drawerOverlay.getChildren().add(panel);
        rootStack.getChildren().add(drawerOverlay);
    }

    /** Qty column with +/− buttons for the edit drawer table. */
    private TableColumn<CartItem, CartItem> buildEditQtyColumn(
            ObservableList<CartItem> cart, TableView<CartItem> table) {
        TableColumn<CartItem, CartItem> col = new TableColumn<>("Qty");
        col.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue()));
        col.setCellFactory(c -> new TableCell<>() {
            private final Button minus = new Button("−");
            private final Label  num   = new Label();
            private final Button plus  = new Button("+");
            private final HBox   box   = new HBox(4, minus, num, plus);
            {
                box.setAlignment(Pos.CENTER);
                minus.setStyle("-fx-min-width:22;-fx-min-height:22;-fx-padding:0;");
                plus.setStyle ("-fx-min-width:22;-fx-min-height:22;-fx-padding:0;");
                minus.setOnAction(e -> {
                    CartItem ci = table.getItems().get(getIndex());
                    if (ci.getQuantity() > 1) ci.setQuantity(ci.getQuantity() - 1);
                    else cart.remove(ci);
                    table.refresh();
                });
                plus.setOnAction(e -> {
                    CartItem ci = table.getItems().get(getIndex());
                    ci.setQuantity(ci.getQuantity() + 1);
                    table.refresh();
                });
            }
            @Override protected void updateItem(CartItem ci, boolean empty) {
                super.updateItem(ci, empty);
                if (empty || ci == null) { setGraphic(null); return; }
                num.setText(String.valueOf(ci.getQuantity()));
                setGraphic(box);
            }
        });
        col.setSortable(false);
        return col;
    }

    private void closeDrawer() {
        rootStack.getChildren().remove(drawerOverlay);
        drawerOverlay = null;
    }

    // =========================================================================
    // Cancel / Delete helpers
    // =========================================================================

    private void confirmDelete(Order o) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Order");
        alert.setHeaderText("Permanently delete Order #" + o.getId() + "?");
        alert.setContentText("Stock will be restored if the order was still active.");
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    orderService.deleteOrder(o.getId(),
                            UserSession.getInstance().getCurrentUser());
                    MainApp.getInstance().showToast("Order deleted.", "info");
                    loadOrderPage();
                    if (mainController != null) mainController.notifyProductsChanged();
                } catch (Exception e) {
                    MainApp.getInstance().showToast(e.getMessage(), "error");
                }
            }
        });
    }

    private void confirmCancel(Order o) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel Order");
        alert.setHeaderText("Cancel Order #" + o.getId() + "?");
        alert.setContentText("Stock will be restored.");
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    orderService.cancelOrder(o.getId(),
                            UserSession.getInstance().getCurrentUser());
                    MainApp.getInstance().showToast("Order cancelled.", "info");
                    loadOrderPage();
                    if (mainController != null) mainController.notifyProductsChanged();
                } catch (Exception e) {
                    MainApp.getInstance().showToast(e.getMessage(), "error");
                }
            }
        });
    }

    // =========================================================================
    // UI helpers
    // =========================================================================

    private Node labeled(String text, Node control) {
        VBox box = new VBox(4);
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #757575;");
        box.getChildren().addAll(lbl, control);
        return box;
    }

    private VBox withLabel(String text, TextField field) {
        VBox box = new VBox(4);
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #757575;");
        box.getChildren().addAll(lbl, field);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private String nvl(String s) { return s != null ? s : ""; }

    // =========================================================================
    // CartItem – inner model class
    // =========================================================================

    /** Holds a Product + quantity for the cart and edit-drawer tables. */
    public static class CartItem {
        private final Product product;
        private int quantity;

        public CartItem(Product product, int quantity) {
            this.product  = product;
            this.quantity = quantity;
        }

        public Product getProduct()        { return product; }
        public int     getQuantity()       { return quantity; }
        public void    setQuantity(int q)  { this.quantity = q; }
        public double  getSubtotal()       { return product.getPrice() * quantity; }
    }
}
