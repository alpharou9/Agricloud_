package esprit.farouk.controllers;

import esprit.farouk.models.CartItem;
import esprit.farouk.models.Order;
import esprit.farouk.models.Product;
import esprit.farouk.models.User;
import esprit.farouk.services.CartService;
import esprit.farouk.services.OrderService;
import esprit.farouk.services.ProductService;
import esprit.farouk.utils.EmailUtils;
import esprit.farouk.utils.SMSUtils;
import esprit.farouk.utils.UIUtils;
import esprit.farouk.utils.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.Optional;
import javax.mail.MessagingException;

public class MarketController {

    private final StackPane contentArea;
    private final User currentUser;
    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();
    private final CartService cartService = new CartService();

    public MarketController(StackPane contentArea, User currentUser) {
        this.contentArea = contentArea;
        this.currentUser = currentUser;
    }

    public void showProductsView() {
        contentArea.getChildren().clear();

        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_LEFT);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Product Management");
        title.getStyleClass().add("content-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button approveBtn = new Button("Approve");
        approveBtn.getStyleClass().add("action-button-approve");
        Button rejectBtn = new Button("Reject");
        rejectBtn.getStyleClass().add("action-button-reject");
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("action-button-delete");

        header.getChildren().addAll(title, spacer, approveBtn, rejectBtn, deleteBtn);

        HBox filterBar = new HBox(10);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search by name...");
        searchField.getStyleClass().add("search-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        ComboBox<String> statusFilter = new ComboBox<>(FXCollections.observableArrayList("All", "Pending", "Approved", "Rejected", "Sold_out"));
        statusFilter.setValue("All");
        statusFilter.getStyleClass().add("filter-combo");

        filterBar.getChildren().addAll(searchField, statusFilter);

        TableView<Product> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Product, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(60);

        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<Product, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setMaxWidth(60);

        TableColumn<Product, String> unitCol = new TableColumn<>("Unit");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));

        TableColumn<Product, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Product, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Product, Long> userCol = new TableColumn<>("Seller ID");
        userCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        userCol.setMaxWidth(80);

        table.getColumns().addAll(idCol, nameCol, priceCol, qtyCol, unitCol, categoryCol, statusCol, userCol);

        ObservableList<Product> masterData = FXCollections.observableArrayList();
        try {
            masterData.addAll(productService.getAll());
        } catch (SQLException e) {
            UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load products: " + e.getMessage());
        }

        FilteredList<Product> filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) ->
                filteredData.setPredicate(product -> filterProduct(product, newVal, statusFilter.getValue())));
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) ->
                filteredData.setPredicate(product -> filterProduct(product, searchField.getText(), newVal)));

        SortedList<Product> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        Runnable reloadTable = () -> {
            masterData.clear();
            try {
                masterData.addAll(productService.getAll());
            } catch (SQLException ex) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load products: " + ex.getMessage());
            }
        };

        approveBtn.setOnAction(e -> {
            Product selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to approve.");
                return;
            }
            try {
                selected.setStatus("approved");
                selected.setApprovedAt(java.time.LocalDateTime.now());
                selected.setApprovedBy(currentUser.getId());
                productService.update(selected);
                reloadTable.run();
            } catch (SQLException ex) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to approve product: " + ex.getMessage());
            }
        });

        rejectBtn.setOnAction(e -> {
            Product selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to reject.");
                return;
            }
            try {
                selected.setStatus("rejected");
                selected.setApprovedAt(null);
                selected.setApprovedBy(null);
                productService.update(selected);
                reloadTable.run();
            } catch (SQLException ex) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to reject product: " + ex.getMessage());
            }
        });

        deleteBtn.setOnAction(e -> {
            Product selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to delete.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete product \"" + selected.getName() + "\"?");
            confirm.setHeaderText("Confirm Deletion");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    productService.delete(selected.getId());
                    reloadTable.run();
                } catch (SQLException ex) {
                    UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete product: " + ex.getMessage());
                }
            }
        });

        container.getChildren().addAll(header, filterBar, table);
        contentArea.getChildren().add(container);
    }

    private boolean filterProduct(Product product, String searchText, String statusValue) {
        boolean matchesSearch = true;
        if (searchText != null && !searchText.trim().isEmpty()) {
            String lower = searchText.trim().toLowerCase();
            matchesSearch = product.getName() != null && product.getName().toLowerCase().contains(lower);
        }
        boolean matchesStatus = true;
        if (statusValue != null && !"All".equals(statusValue)) {
            matchesStatus = statusValue.toLowerCase().equals(product.getStatus());
        }
        return matchesSearch && matchesStatus;
    }

    public void showAllOrdersView() {
        contentArea.getChildren().clear();

        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_LEFT);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("All Orders");
        title.getStyleClass().add("content-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button updateStatusBtn = new Button("Update Status");
        updateStatusBtn.getStyleClass().add("action-button-edit");

        header.getChildren().addAll(title, spacer, updateStatusBtn);

        TableView<Order> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Order, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(60);

        TableColumn<Order, Long> productCol = new TableColumn<>("Product");
        productCol.setCellValueFactory(new PropertyValueFactory<>("productId"));

        TableColumn<Order, Long> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));

        TableColumn<Order, Long> sellerCol = new TableColumn<>("Seller");
        sellerCol.setCellValueFactory(new PropertyValueFactory<>("sellerId"));

        TableColumn<Order, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setMaxWidth(60);

        TableColumn<Order, Double> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        TableColumn<Order, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(idCol, productCol, customerCol, sellerCol, qtyCol, totalCol, statusCol);

        ObservableList<Order> masterData = FXCollections.observableArrayList();
        try {
            masterData.addAll(orderService.getAll());
        } catch (SQLException e) {
            UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load orders: " + e.getMessage());
        }
        table.setItems(masterData);

        Runnable reloadTable = () -> {
            masterData.clear();
            try {
                masterData.addAll(orderService.getAll());
            } catch (SQLException ex) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load orders: " + ex.getMessage());
            }
        };

        updateStatusBtn.setOnAction(e -> {
            Order selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an order.");
                return;
            }
            showOrderStatusDialog(selected);
            reloadTable.run();
        });

        container.getChildren().addAll(header, table);
        contentArea.getChildren().add(container);
    }

    private void showOrderStatusDialog(Order order) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Update Order Status");
        dialog.setHeaderText("Order #" + order.getId());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList(
                "pending", "confirmed", "processing", "shipped", "delivered", "cancelled"));
        statusCombo.setValue(order.getStatus());

        VBox content = new VBox(10);
        content.getChildren().addAll(new Label("Status:"), statusCombo);
        content.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(content);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String newStatus = statusCombo.getValue();
            try {
                orderService.updateStatus(order.getId(), newStatus);
            } catch (SQLException e) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to update order: " + e.getMessage());
                return;
            }
            // Send SMS notification to customer in background
            new Thread(() -> {
                try {
                    String productName = "Order item";
                    try {
                        Product p = productService.getById(order.getProductId());
                        if (p != null) productName = p.getName();
                    } catch (Exception ignored) {}
                    SMSUtils.sendOrderStatusUpdate(
                            order.getShippingPhone(),
                            order.getId(),
                            newStatus,
                            productName,
                            order.getTotalPrice()
                    );
                } catch (Exception ex) {
                    System.err.println("SMS notification failed: " + ex.getMessage());
                }
            }).start();
        }
    }

    public void showMyProductsView() {
        contentArea.getChildren().clear();

        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_LEFT);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("My Products");
        title.getStyleClass().add("content-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("action-button-add");
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("action-button-edit");
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("action-button-delete");

        header.getChildren().addAll(title, spacer, addBtn, editBtn, deleteBtn);

        TableView<Product> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Product, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(60);

        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<Product, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setMaxWidth(60);

        TableColumn<Product, String> unitCol = new TableColumn<>("Unit");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));

        TableColumn<Product, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Product, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Product, Integer> viewsCol = new TableColumn<>("Views");
        viewsCol.setCellValueFactory(new PropertyValueFactory<>("views"));
        viewsCol.setMaxWidth(60);

        table.getColumns().addAll(idCol, nameCol, priceCol, qtyCol, unitCol, categoryCol, statusCol, viewsCol);

        ObservableList<Product> masterData = FXCollections.observableArrayList();
        try {
            masterData.addAll(productService.getByUserId(currentUser.getId()));
        } catch (SQLException e) {
            UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load products: " + e.getMessage());
        }
        table.setItems(masterData);

        Runnable reloadTable = () -> {
            masterData.clear();
            try {
                masterData.addAll(productService.getByUserId(currentUser.getId()));
            } catch (SQLException ex) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load products: " + ex.getMessage());
            }
        };

        addBtn.setOnAction(e -> {
            showProductFormDialog(null);
            reloadTable.run();
        });

        editBtn.setOnAction(e -> {
            Product selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to edit.");
                return;
            }
            showProductFormDialog(selected);
            reloadTable.run();
        });

        deleteBtn.setOnAction(e -> {
            Product selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to delete.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete product \"" + selected.getName() + "\"?");
            confirm.setHeaderText("Confirm Deletion");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    productService.delete(selected.getId());
                    reloadTable.run();
                } catch (SQLException ex) {
                    UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete product: " + ex.getMessage());
                }
            }
        });

        container.getChildren().addAll(header, table);
        contentArea.getChildren().add(container);
    }

    private void showProductFormDialog(Product product) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(product == null ? "Add Product" : "Edit Product");
        dialog.setHeaderText(product == null ? "Create a new product" : "Edit product: " + product.getName());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField(product != null ? product.getName() : "");
        nameField.setPromptText("Product name");
        TextField descField = new TextField(product != null && product.getDescription() != null ? product.getDescription() : "");
        descField.setPromptText("Description");
        TextField priceField = new TextField(product != null ? String.valueOf(product.getPrice()) : "");
        priceField.setPromptText("Price");
        TextField qtyField = new TextField(product != null ? String.valueOf(product.getQuantity()) : "");
        qtyField.setPromptText("Quantity");
        TextField unitField = new TextField(product != null ? product.getUnit() : "");
        unitField.setPromptText("Unit (kg, piece, etc.)");

        ComboBox<String> categoryCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Fruits", "Vegetables", "Dairy", "Meat", "Grains", "Herbs", "Honey", "Eggs", "Other"));
        categoryCombo.setPromptText("Select category");
        if (product != null && product.getCategory() != null) {
            categoryCombo.setValue(product.getCategory());
        }

        int row = 0;
        grid.add(new Label("Name:"), 0, row);
        grid.add(nameField, 1, row++);
        grid.add(new Label("Description:"), 0, row);
        grid.add(descField, 1, row++);
        grid.add(new Label("Price:"), 0, row);
        grid.add(priceField, 1, row++);
        grid.add(new Label("Quantity:"), 0, row);
        grid.add(qtyField, 1, row++);
        grid.add(new Label("Unit:"), 0, row);
        grid.add(unitField, 1, row++);
        grid.add(new Label("Category:"), 0, row);
        grid.add(categoryCombo, 1, row);

        dialog.getDialogPane().setContent(grid);

        while (true) {
            Optional<ButtonType> result = dialog.showAndWait();
            if (!result.isPresent() || result.get() != ButtonType.OK) {
                break;
            }

            String nameVal = nameField.getText().trim();
            if (nameVal.length() < 2) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", "Product name must be at least 2 characters.");
                continue;
            }

            double priceVal;
            int qtyVal;
            try {
                priceVal = Double.parseDouble(priceField.getText().trim());
                if (priceVal < 0) {
                    UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", "Price must be positive.");
                    continue;
                }
            } catch (NumberFormatException ex) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", "Price must be a valid number.");
                continue;
            }

            try {
                qtyVal = Integer.parseInt(qtyField.getText().trim());
                if (qtyVal < 0) {
                    UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", "Quantity must be positive.");
                    continue;
                }
            } catch (NumberFormatException ex) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", "Quantity must be a valid integer.");
                continue;
            }

            String unitVal = unitField.getText().trim();
            if (unitVal.isEmpty()) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", "Unit is required.");
                continue;
            }

            try {
                if (product == null) {
                    Product newProduct = new Product();
                    newProduct.setUserId(currentUser.getId());
                    newProduct.setName(nameVal);
                    newProduct.setDescription(descField.getText().trim().isEmpty() ? null : descField.getText().trim());
                    newProduct.setPrice(priceVal);
                    newProduct.setQuantity(qtyVal);
                    newProduct.setUnit(unitVal);
                    newProduct.setCategory(categoryCombo.getValue());
                    newProduct.setStatus("pending");
                    productService.add(newProduct);
                } else {
                    product.setName(nameVal);
                    product.setDescription(descField.getText().trim().isEmpty() ? null : descField.getText().trim());
                    product.setPrice(priceVal);
                    product.setQuantity(qtyVal);
                    product.setUnit(unitVal);
                    product.setCategory(categoryCombo.getValue());
                    productService.update(product);
                }
            } catch (SQLException e) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to save product: " + e.getMessage());
            }
            break;
        }
    }

    public void showSellerOrdersView() {
        contentArea.getChildren().clear();

        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_LEFT);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Orders for My Products");
        title.getStyleClass().add("content-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button updateStatusBtn = new Button("Update Status");
        updateStatusBtn.getStyleClass().add("action-button-edit");

        header.getChildren().addAll(title, spacer, updateStatusBtn);

        TableView<Order> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Order, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(60);

        TableColumn<Order, Long> productCol = new TableColumn<>("Product");
        productCol.setCellValueFactory(new PropertyValueFactory<>("productId"));

        TableColumn<Order, Long> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));

        TableColumn<Order, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setMaxWidth(60);

        TableColumn<Order, Double> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        TableColumn<Order, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Order, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("shippingAddress"));

        table.getColumns().addAll(idCol, productCol, customerCol, qtyCol, totalCol, statusCol, addressCol);

        ObservableList<Order> masterData = FXCollections.observableArrayList();
        try {
            masterData.addAll(orderService.getBySellerId(currentUser.getId()));
        } catch (SQLException e) {
            UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load orders: " + e.getMessage());
        }
        table.setItems(masterData);

        Runnable reloadTable = () -> {
            masterData.clear();
            try {
                masterData.addAll(orderService.getBySellerId(currentUser.getId()));
            } catch (SQLException ex) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load orders: " + ex.getMessage());
            }
        };

        updateStatusBtn.setOnAction(e -> {
            Order selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an order.");
                return;
            }
            showOrderStatusDialog(selected);
            reloadTable.run();
        });

        container.getChildren().addAll(header, table);
        contentArea.getChildren().add(container);
    }

    public void showShopView() {
        contentArea.getChildren().clear();

        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_LEFT);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Shop");
        title.getStyleClass().add("content-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField searchField = new TextField();
        searchField.setPromptText("Search products...");
        searchField.getStyleClass().add("search-field");

        header.getChildren().addAll(title, spacer, searchField);

        TableView<Product> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Product, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<Product, Integer> qtyCol = new TableColumn<>("Available");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setMaxWidth(80);

        TableColumn<Product, String> unitCol = new TableColumn<>("Unit");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));

        TableColumn<Product, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        table.getColumns().addAll(nameCol, descCol, priceCol, qtyCol, unitCol, categoryCol);

        ObservableList<Product> masterData = FXCollections.observableArrayList();
        try {
            masterData.addAll(productService.getApproved());
        } catch (SQLException e) {
            UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load products: " + e.getMessage());
        }

        FilteredList<Product> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) ->
                filteredData.setPredicate(product -> {
                    if (newVal == null || newVal.trim().isEmpty()) return true;
                    String lower = newVal.toLowerCase();
                    return (product.getName() != null && product.getName().toLowerCase().contains(lower))
                            || (product.getCategory() != null && product.getCategory().toLowerCase().contains(lower));
                }));

        SortedList<Product> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        // Add to cart section
        HBox cartControls = new HBox(10);
        cartControls.setAlignment(Pos.CENTER_LEFT);
        Label qtyLabel = new Label("Quantity:");
        Spinner<Integer> qtySpinner = new Spinner<>(1, 100, 1);
        qtySpinner.setEditable(true);
        qtySpinner.setPrefWidth(80);
        Button addToCartBtn = new Button("Add to Cart");
        addToCartBtn.getStyleClass().add("action-button-add");

        cartControls.getChildren().addAll(qtyLabel, qtySpinner, addToCartBtn);

        addToCartBtn.setOnAction(e -> {
            Product selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to add to cart.");
                return;
            }
            int qty = qtySpinner.getValue();
            if (qty > selected.getQuantity()) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "Insufficient Stock", "Only " + selected.getQuantity() + " available.");
                return;
            }
            try {
                cartService.addToCart(currentUser.getId(), selected.getId(), qty);
                UIUtils.showAlert(Alert.AlertType.INFORMATION, "Added", "Added " + qty + " x " + selected.getName() + " to cart.");
            } catch (SQLException ex) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to add to cart: " + ex.getMessage());
            }
        });

        container.getChildren().addAll(header, table, cartControls);
        contentArea.getChildren().add(container);
    }

    public void showCartView() {
        contentArea.getChildren().clear();

        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_LEFT);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Shopping Cart");
        title.getStyleClass().add("content-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button removeBtn = new Button("Remove");
        removeBtn.getStyleClass().add("action-button-delete");
        Button clearBtn = new Button("Clear Cart");
        clearBtn.getStyleClass().add("action-button-block");

        header.getChildren().addAll(title, spacer, removeBtn, clearBtn);

        TableView<CartItem> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<CartItem, String> nameCol = new TableColumn<>("Product");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));

        TableColumn<CartItem, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("productPrice"));

        TableColumn<CartItem, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setMaxWidth(60);

        TableColumn<CartItem, String> unitCol = new TableColumn<>("Unit");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("productUnit"));

        TableColumn<CartItem, Double> subtotalCol = new TableColumn<>("Subtotal");
        subtotalCol.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        table.getColumns().addAll(nameCol, priceCol, qtyCol, unitCol, subtotalCol);

        ObservableList<CartItem> cartData = FXCollections.observableArrayList();
        Label totalLabel = new Label("Total: 0.00");
        totalLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        Runnable reloadCart = () -> {
            cartData.clear();
            try {
                cartData.addAll(cartService.getCartItems(currentUser.getId()));
                double total = cartService.getCartTotal(currentUser.getId());
                totalLabel.setText(String.format("Total: %.2f", total));
            } catch (SQLException ex) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load cart: " + ex.getMessage());
            }
        };

        reloadCart.run();
        table.setItems(cartData);

        removeBtn.setOnAction(e -> {
            CartItem selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an item to remove.");
                return;
            }
            try {
                cartService.removeFromCart(currentUser.getId(), selected.getProductId());
                reloadCart.run();
            } catch (SQLException ex) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to remove item: " + ex.getMessage());
            }
        });

        clearBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Clear all items from cart?");
            confirm.setHeaderText("Clear Cart");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    cartService.clearCart(currentUser.getId());
                    reloadCart.run();
                } catch (SQLException ex) {
                    UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to clear cart: " + ex.getMessage());
                }
            }
        });

        // Checkout section
        HBox checkoutBox = new HBox(20);
        checkoutBox.setAlignment(Pos.CENTER_RIGHT);
        Button checkoutBtn = new Button("Checkout");
        checkoutBtn.getStyleClass().add("action-button-approve");
        checkoutBox.getChildren().addAll(totalLabel, checkoutBtn);

        checkoutBtn.setOnAction(e -> {
            if (cartData.isEmpty()) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "Empty Cart", "Your cart is empty.");
                return;
            }
            showCheckoutDialog(cartData, reloadCart);
        });

        container.getChildren().addAll(header, table, checkoutBox);
        contentArea.getChildren().add(container);
    }

    private void showCheckoutDialog(ObservableList<CartItem> cartItems, Runnable reloadCart) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Checkout");
        dialog.setHeaderText("Enter shipping details");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField emailField = new TextField();
        emailField.setPromptText("Email address");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone number");
        TextField addressField = new TextField();
        addressField.setPromptText("Shipping address");
        TextField cityField = new TextField();
        cityField.setPromptText("City");
        TextField postalField = new TextField();
        postalField.setPromptText("Postal code");
        TextField notesField = new TextField();
        notesField.setPromptText("Notes (optional)");

        grid.add(new Label("Email:"), 0, 0);
        grid.add(emailField, 1, 0);
        grid.add(new Label("Phone:"), 0, 1);
        grid.add(phoneField, 1, 1);
        grid.add(new Label("Address:"), 0, 2);
        grid.add(addressField, 1, 2);
        grid.add(new Label("City:"), 0, 3);
        grid.add(cityField, 1, 3);
        grid.add(new Label("Postal:"), 0, 4);
        grid.add(postalField, 1, 4);
        grid.add(new Label("Notes:"), 0, 5);
        grid.add(notesField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        while (true) {
            Optional<ButtonType> result = dialog.showAndWait();
            if (!result.isPresent() || result.get() != ButtonType.OK) {
                break;
            }

            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressField.getText().trim();

            // Validation
            if (email.isEmpty()) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", "Email is required.");
                continue;
            }
            if (!ValidationUtils.isValidEmail(email)) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid email address.");
                continue;
            }
            if (phone.isEmpty()) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", "Phone number is required.");
                continue;
            }
            if (!ValidationUtils.isValidPhone(phone)) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid phone number (8-15 digits).");
                continue;
            }
            if (address.isEmpty()) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", "Address is required.");
                continue;
            }

            try {
                double grandTotal = 0;
                long firstOrderId = 0;

                // Create order for each cart item
                for (CartItem item : cartItems) {
                    Product product = productService.getById(item.getProductId());
                    if (product == null) continue;

                    Order order = new Order();
                    order.setCustomerId(currentUser.getId());
                    order.setProductId(item.getProductId());
                    order.setSellerId(product.getUserId());
                    order.setQuantity(item.getQuantity());
                    order.setUnitPrice(product.getPrice());
                    order.setTotalPrice(product.getPrice() * item.getQuantity());
                    order.setShippingAddress(address);
                    order.setShippingCity(cityField.getText().trim());
                    order.setShippingPostal(postalField.getText().trim());
                    order.setShippingEmail(email);
                    order.setShippingPhone(phone);
                    order.setNotes(notesField.getText().trim().isEmpty() ? null : notesField.getText().trim());
                    order.setStatus("pending");
                    order.setOrderDate(java.time.LocalDateTime.now());

                    orderService.add(order);
                    productService.decrementQuantity(product.getId(), item.getQuantity());

                    grandTotal += order.getTotalPrice();
                    if (firstOrderId == 0) {
                        // Get the last inserted order ID (approximation - in production use LAST_INSERT_ID())
                        firstOrderId = System.currentTimeMillis();
                    }
                }

                cartService.clearCart(currentUser.getId());

                // Send order confirmation email
                try {
                    EmailUtils.sendOrderConfirmation(email, currentUser.getName(), firstOrderId, grandTotal);
                } catch (MessagingException e) {
                    System.err.println("Failed to send confirmation email: " + e.getMessage());
                    // Don't show error to user, order was still placed successfully
                }

                reloadCart.run();
                UIUtils.showAlert(Alert.AlertType.INFORMATION, "Order Placed",
                    "Your order has been placed successfully!\n\nA confirmation email has been sent to " + email +
                    ".\n\nYour order will be delivered within 3 business days.");
            } catch (SQLException ex) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to place order: " + ex.getMessage());
            }
            break;
        }
    }

    public void showCustomerOrdersView() {
        contentArea.getChildren().clear();

        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_LEFT);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("My Orders");
        title.getStyleClass().add("content-title");

        header.getChildren().add(title);

        TableView<Order> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Order, Long> idCol = new TableColumn<>("Order #");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(80);

        TableColumn<Order, Long> productCol = new TableColumn<>("Product");
        productCol.setCellValueFactory(new PropertyValueFactory<>("productId"));

        TableColumn<Order, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setMaxWidth(60);

        TableColumn<Order, Double> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        TableColumn<Order, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Order, java.time.LocalDateTime> dateCol = new TableColumn<>("Order Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("orderDate"));

        table.getColumns().addAll(idCol, productCol, qtyCol, totalCol, statusCol, dateCol);

        ObservableList<Order> masterData = FXCollections.observableArrayList();
        try {
            masterData.addAll(orderService.getByCustomerId(currentUser.getId()));
        } catch (SQLException e) {
            UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load orders: " + e.getMessage());
        }
        table.setItems(masterData);

        container.getChildren().addAll(header, table);
        contentArea.getChildren().add(container);
    }
}
