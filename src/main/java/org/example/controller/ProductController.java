package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.example.MainApp;
import org.example.dao.ProductDAO;
import org.example.model.Product;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class ProductController {

    private static final long DEFAULT_USER_ID = 1;
    private static final String UPLOADS_DIR = "uploads/products";
    private static final int LOW_STOCK_THRESHOLD = 5;

    private static final List<String> CATEGORIES = Arrays.asList(
            "Fruits", "Vegetables", "Grains", "Dairy", "Livestock", "Other");
    private static final List<String> UNITS = Arrays.asList(
            "kg", "g", "lb", "ton", "piece", "dozen", "liter", "gallon");
    private static final List<String> STATUSES = Arrays.asList(
            "pending", "approved", "rejected", "sold_out");
    private static final Map<String, String> CATEGORY_COLORS = Map.of(
            "Fruits", "category-fruits",
            "Vegetables", "category-vegetables",
            "Grains", "category-grains",
            "Dairy", "category-dairy",
            "Livestock", "category-livestock",
            "Other", "category-other"
    );

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
            ".png", ".jpg", ".jpeg", ".gif", ".bmp", ".webp");
    private static final long MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024;
    private static final int MAX_DESCRIPTION_LENGTH = 2000;

    private final ProductDAO productDAO = new ProductDAO();

    // Search, filter, sort
    private final TextField searchField = new TextField();
    private final ComboBox<String> filterCategoryCombo = new ComboBox<>();
    private final ComboBox<String> filterStatusCombo = new ComboBox<>();
    private final ComboBox<String> sortCombo = new ComboBox<>();

    // Card grid
    private FlowPane productGrid;
    private ScrollPane scrollPane;
    private ObservableList<Product> allProducts = FXCollections.observableArrayList();

    // Drawer
    private StackPane rootStack;
    private HBox drawerOverlay;

    public StackPane getView() {
        rootStack = new StackPane();

        try {
            Files.createDirectories(Path.of(UPLOADS_DIR));
        } catch (IOException e) {
            System.out.println("[ProductController] Could not create uploads dir: " + e.getMessage());
        }

        // ===== MAIN CONTENT =====
        VBox mainContent = new VBox();
        mainContent.setStyle("-fx-background-color: transparent;");

        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, 8, 0));

        searchField.setPromptText("Search products...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(180);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        filterCategoryCombo.setItems(FXCollections.observableArrayList(
                "All Categories", "Fruits", "Vegetables", "Grains", "Dairy", "Livestock", "Other"));
        filterCategoryCombo.setValue("All Categories");
        filterCategoryCombo.getStyleClass().add("filter-combo");

        filterStatusCombo.setItems(FXCollections.observableArrayList(
                "All Status", "pending", "approved", "rejected", "sold_out"));
        filterStatusCombo.setValue("All Status");
        filterStatusCombo.getStyleClass().add("filter-combo");

        sortCombo.setItems(FXCollections.observableArrayList(
                "Name A-Z", "Name Z-A", "Price: Low-High", "Price: High-Low", "Low Stock First", "Newest"));
        sortCombo.setValue("Name A-Z");
        sortCombo.getStyleClass().add("filter-combo");

        Button addBtn = new Button("+ Add Product");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> openDrawer(null));

        topBar.getChildren().addAll(searchField, filterCategoryCombo, filterStatusCombo, sortCombo, addBtn);

        productGrid = new FlowPane();
        productGrid.getStyleClass().add("product-grid");
        productGrid.setPrefWrapLength(600);

        searchField.textProperty().addListener((obs, o, n) -> refreshGrid());
        filterCategoryCombo.valueProperty().addListener((obs, o, n) -> refreshGrid());
        filterStatusCombo.valueProperty().addListener((obs, o, n) -> refreshGrid());
        sortCombo.valueProperty().addListener((obs, o, n) -> refreshGrid());

        VBox scrollContent = new VBox(16);
        scrollContent.setPadding(new Insets(4));
        scrollContent.getChildren().addAll(topBar, productGrid);

        scrollPane = new ScrollPane(scrollContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("products-scroll");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        mainContent.getChildren().add(scrollPane);
        VBox.setVgrow(mainContent, Priority.ALWAYS);

        rootStack.getChildren().add(mainContent);

        refreshGrid();
        return rootStack;
    }

    // ==================== DRAWER ====================

    private void openDrawer(Product existing) {
        closeDrawer();

        // --- Drawer form fields ---
        TextField nameField = new TextField();
        nameField.setPromptText("Product name");
        ComboBox<String> categoryCombo = new ComboBox<>(FXCollections.observableArrayList(CATEGORIES));
        categoryCombo.setPromptText("Category");
        categoryCombo.setMaxWidth(Double.MAX_VALUE);
        TextField priceField = new TextField();
        priceField.setPromptText("0.00");
        TextField quantityField = new TextField();
        quantityField.setPromptText("0");
        ComboBox<String> unitCombo = new ComboBox<>(FXCollections.observableArrayList(UNITS));
        unitCombo.setPromptText("Unit");
        unitCombo.setMaxWidth(Double.MAX_VALUE);
        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList(STATUSES));
        statusCombo.setValue("pending");
        statusCombo.setMaxWidth(Double.MAX_VALUE);
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(2);
        descriptionArea.setPromptText("Description (optional)");

        // Inline errors
        Label nameError = new Label();
        nameError.getStyleClass().add("error-message");
        nameError.setVisible(false); nameError.setManaged(false);
        Label priceError = new Label();
        priceError.getStyleClass().add("error-message");
        priceError.setVisible(false); priceError.setManaged(false);
        Label qtyError = new Label();
        qtyError.getStyleClass().add("error-message");
        qtyError.setVisible(false); qtyError.setManaged(false);

        // Image
        ImageView imagePreview = new ImageView();
        imagePreview.setFitWidth(80);
        imagePreview.setFitHeight(60);
        imagePreview.setPreserveRatio(true);
        imagePreview.setSmooth(true);
        Label imagePathLabel = new Label("No image");
        imagePathLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: -agri-text-muted;");
        final String[] selectedImagePath = {null};

        Button uploadBtn = new Button("Choose Image");
        uploadBtn.getStyleClass().add("btn-upload");
        uploadBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select Product Image");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.webp"));
            File f = fc.showOpenDialog(rootStack.getScene().getWindow());
            if (f != null) {
                String ext = f.getName().toLowerCase();
                ext = ext.contains(".") ? ext.substring(ext.lastIndexOf('.')) : "";
                if (!ALLOWED_IMAGE_EXTENSIONS.contains(ext)) {
                    MainApp.showToast("Invalid image format", "error"); return;
                }
                if (f.length() > MAX_IMAGE_SIZE_BYTES) {
                    MainApp.showToast("Image must be < 5MB", "error"); return;
                }
                try {
                    String uniqueName = System.currentTimeMillis() + "_" + f.getName();
                    Path dest = Path.of(UPLOADS_DIR, uniqueName);
                    Files.copy(f.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
                    selectedImagePath[0] = dest.toString().replace("\\", "/");
                    imagePreview.setImage(new Image(f.toURI().toString(), 80, 60, true, true));
                    imagePathLabel.setText(f.getName());
                } catch (IOException ex) {
                    MainApp.showToast("Image upload failed", "error");
                }
            }
        });

        // Pre-fill for editing
        if (existing != null) {
            nameField.setText(existing.getName());
            categoryCombo.setValue(existing.getCategory());
            priceField.setText(String.valueOf(existing.getPrice()));
            quantityField.setText(String.valueOf(existing.getQuantity()));
            unitCombo.setValue(existing.getUnit());
            statusCombo.setValue(existing.getStatus());
            descriptionArea.setText(existing.getDescription());
            selectedImagePath[0] = existing.getImage();
            if (existing.getImage() != null && !existing.getImage().isEmpty()) {
                try {
                    File imgFile = new File(existing.getImage());
                    if (imgFile.exists()) {
                        imagePreview.setImage(new Image(imgFile.toURI().toString(), 80, 60, true, true));
                        imagePathLabel.setText(imgFile.getName());
                    }
                } catch (Exception ignored) {}
            }
        }

        // Inline validation on focus lost
        nameField.focusedProperty().addListener((obs, was, is) -> {
            if (!is) {
                String v = nameField.getText() == null ? "" : nameField.getText().trim();
                if (v.isEmpty()) showFieldError(nameField, nameError, "Required");
                else if (v.length() < 3) showFieldError(nameField, nameError, "Min 3 chars");
                else clearFieldError(nameField, nameError);
            }
        });
        priceField.focusedProperty().addListener((obs, was, is) -> {
            if (!is) {
                try {
                    double p = Double.parseDouble(priceField.getText().trim());
                    if (p <= 0) showFieldError(priceField, priceError, "Must be > 0");
                    else clearFieldError(priceField, priceError);
                } catch (Exception e) { showFieldError(priceField, priceError, "Invalid"); }
            }
        });
        quantityField.focusedProperty().addListener((obs, was, is) -> {
            if (!is) {
                try {
                    int q = Integer.parseInt(quantityField.getText().trim());
                    if (q < 0) showFieldError(quantityField, qtyError, "Must be >= 0");
                    else clearFieldError(quantityField, qtyError);
                } catch (Exception e) { showFieldError(quantityField, qtyError, "Invalid"); }
            }
        });

        // --- Build drawer body (2-column aligned) ---
        VBox body = new VBox(10);
        body.getStyleClass().add("drawer-body");

        // Name (full width)
        VBox nameCol = fieldCol("Name *", nameField, nameError);

        // Category + Unit row
        HBox catUnitRow = new HBox(10);
        catUnitRow.getStyleClass().add("drawer-field-row");
        VBox catCol = fieldCol("Category *", categoryCombo, null);
        VBox unitCol = fieldCol("Unit *", unitCombo, null);
        HBox.setHgrow(catCol, Priority.ALWAYS);
        HBox.setHgrow(unitCol, Priority.ALWAYS);
        catUnitRow.getChildren().addAll(catCol, unitCol);

        // Price + Quantity row
        HBox priceQtyRow = new HBox(10);
        priceQtyRow.getStyleClass().add("drawer-field-row");
        VBox priceCol = fieldCol("Price *", priceField, priceError);
        VBox qtyCol = fieldCol("Quantity *", quantityField, qtyError);
        HBox.setHgrow(priceCol, Priority.ALWAYS);
        HBox.setHgrow(qtyCol, Priority.ALWAYS);
        priceQtyRow.getChildren().addAll(priceCol, qtyCol);

        // Status (half width)
        VBox statusCol = fieldCol("Status", statusCombo, null);

        // Description
        VBox descCol = fieldCol("Description", descriptionArea, null);

        // Image section
        Label imgSectionLbl = new Label("PHOTO");
        imgSectionLbl.getStyleClass().add("drawer-section-label");

        HBox imageRow = new HBox(10);
        imageRow.setAlignment(Pos.CENTER_LEFT);
        StackPane imgFrame = new StackPane(imagePreview);
        imgFrame.getStyleClass().add("image-frame");
        imgFrame.setPrefSize(80, 60);
        imgFrame.setMinSize(80, 60);
        VBox imgActions = new VBox(4, imagePathLabel, uploadBtn);
        imageRow.getChildren().addAll(imgFrame, imgActions);

        body.getChildren().addAll(nameCol, catUnitRow, priceQtyRow, statusCol, descCol, imgSectionLbl, imageRow);

        // Wrap body in scroll
        ScrollPane bodyScroll = new ScrollPane(body);
        bodyScroll.setFitToWidth(true);
        bodyScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        bodyScroll.getStyleClass().add("products-scroll");
        VBox.setVgrow(bodyScroll, Priority.ALWAYS);

        // --- Header ---
        HBox header = new HBox();
        header.getStyleClass().add("drawer-header");
        header.setAlignment(Pos.CENTER_LEFT);
        Label titleLbl = new Label(existing == null ? "Add Product" : "Edit Product");
        titleLbl.getStyleClass().add("drawer-title");
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        Button closeBtn = new Button("\u2715");
        closeBtn.getStyleClass().add("drawer-close");
        closeBtn.setOnAction(e -> closeDrawer());
        header.getChildren().addAll(titleLbl, headerSpacer, closeBtn);

        // --- Actions bar ---
        HBox actions = new HBox(8);
        actions.getStyleClass().add("drawer-actions");

        Button saveBtn = new Button(existing == null ? "Save" : "Update");
        saveBtn.getStyleClass().add("btn-primary");
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("btn-secondary");
        cancelBtn.setOnAction(e -> closeDrawer());

        Region actionSpacer = new Region();
        HBox.setHgrow(actionSpacer, Priority.ALWAYS);

        if (existing != null) {
            Button deleteBtn = new Button("Delete");
            deleteBtn.getStyleClass().add("btn-link-danger");
            deleteBtn.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Delete \"" + existing.getName() + "\"?", ButtonType.YES, ButtonType.NO);
                confirm.showAndWait().ifPresent(r -> {
                    if (r == ButtonType.YES) {
                        productDAO.delete(existing.getId());
                        MainApp.showToast("Product deleted", "success");
                        closeDrawer();
                        refreshGrid();
                    }
                });
            });
            actions.getChildren().addAll(deleteBtn, actionSpacer, cancelBtn, saveBtn);
        } else {
            actions.getChildren().addAll(actionSpacer, cancelBtn, saveBtn);
        }

        saveBtn.setOnAction(e -> {
            String errors = validateFields(nameField, categoryCombo, priceField, quantityField, unitCombo, descriptionArea);
            if (!errors.isEmpty()) {
                MainApp.showToast(errors.trim(), "warning");
                return;
            }
            if (existing == null) {
                Product p = new Product(DEFAULT_USER_ID, nameField.getText().trim(),
                        descriptionArea.getText() == null ? "" : descriptionArea.getText().trim(),
                        Double.parseDouble(priceField.getText().trim()),
                        Integer.parseInt(quantityField.getText().trim()),
                        unitCombo.getValue(), categoryCombo.getValue());
                String status = statusCombo.getValue() != null ? statusCombo.getValue() : "pending";
                if (Integer.parseInt(quantityField.getText().trim()) == 0) status = "sold_out";
                p.setStatus(status);
                p.setImage(selectedImagePath[0]);
                productDAO.insert(p);
                MainApp.showToast("Product added!", "success");
            } else {
                existing.setName(nameField.getText().trim());
                existing.setCategory(categoryCombo.getValue());
                existing.setPrice(Double.parseDouble(priceField.getText().trim()));
                existing.setQuantity(Integer.parseInt(quantityField.getText().trim()));
                existing.setUnit(unitCombo.getValue());
                String status = statusCombo.getValue();
                if (existing.getQuantity() == 0) status = "sold_out";
                existing.setStatus(status);
                existing.setDescription(descriptionArea.getText() == null ? "" : descriptionArea.getText().trim());
                existing.setImage(selectedImagePath[0]);
                productDAO.update(existing);
                MainApp.showToast("Product updated!", "success");
            }
            closeDrawer();
            refreshGrid();
        });

        // --- Assemble drawer panel ---
        VBox drawerPanel = new VBox();
        drawerPanel.getStyleClass().add("drawer-panel");
        drawerPanel.getChildren().addAll(header, bodyScroll, actions);

        // --- Backdrop ---
        Region backdrop = new Region();
        backdrop.getStyleClass().add("drawer-backdrop");
        backdrop.setOnMouseClicked(e -> closeDrawer());
        HBox.setHgrow(backdrop, Priority.ALWAYS);

        // --- Overlay ---
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

    // ==================== PRODUCT CARD ====================

    private VBox createProductCard(Product product) {
        VBox card = new VBox();
        card.getStyleClass().add("product-card");

        StackPane imageArea = new StackPane();
        imageArea.getStyleClass().add("product-card-image");
        imageArea.setMinHeight(120); imageArea.setPrefHeight(120); imageArea.setMaxHeight(120);

        if (product.getImage() != null && !product.getImage().isEmpty()) {
            try {
                File imgFile = new File(product.getImage());
                if (imgFile.exists()) {
                    ImageView iv = new ImageView(new Image(imgFile.toURI().toString(), 175, 120, true, true));
                    iv.setFitWidth(175); iv.setFitHeight(120); iv.setPreserveRatio(true); iv.setSmooth(true);
                    imageArea.getChildren().add(iv);
                } else {
                    imageArea.getChildren().add(createPlaceholderIcon(product.getCategory()));
                }
            } catch (Exception e) {
                imageArea.getChildren().add(createPlaceholderIcon(product.getCategory()));
            }
        } else {
            imageArea.getChildren().add(createPlaceholderIcon(product.getCategory()));
        }

        if (product.getQuantity() <= LOW_STOCK_THRESHOLD && !"sold_out".equals(product.getStatus())) {
            Label lowBadge = new Label("Low Stock");
            lowBadge.getStyleClass().add("low-stock-badge");
            StackPane.setAlignment(lowBadge, Pos.TOP_RIGHT);
            StackPane.setMargin(lowBadge, new Insets(6, 6, 0, 0));
            imageArea.getChildren().add(lowBadge);
        }

        VBox info = new VBox(4);
        info.getStyleClass().add("product-card-info");

        Label nameLabel = new Label(product.getName() != null ? product.getName() : "Unnamed");
        nameLabel.getStyleClass().add("product-card-name");
        nameLabel.setMaxWidth(160);

        String priceText = String.format("$%.2f", product.getPrice());
        if (product.getUnit() != null) priceText += "/" + product.getUnit();
        Label priceLabel = new Label(priceText);
        priceLabel.getStyleClass().add("product-card-price");

        Label categoryLabel = new Label(product.getCategory() != null ? product.getCategory() : "Other");
        categoryLabel.getStyleClass().add("category-chip");
        categoryLabel.getStyleClass().add(CATEGORY_COLORS.getOrDefault(product.getCategory(), "category-other"));

        Label qtyLabel = new Label("Qty: " + product.getQuantity());
        qtyLabel.setStyle("-fx-font-size: 10.5px; -fx-text-fill: -agri-text-muted;");

        info.getChildren().addAll(nameLabel, priceLabel, categoryLabel, qtyLabel);

        HBox actions = new HBox(4);
        actions.getStyleClass().add("card-actions");

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("btn-icon");
        editBtn.setOnAction(e -> openDrawer(product));

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().addAll("btn-icon", "btn-icon-danger");
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete \"" + product.getName() + "\"?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.YES) {
                    productDAO.delete(product.getId());
                    MainApp.showToast("Product deleted", "success");
                    refreshGrid();
                }
            });
        });

        actions.getChildren().addAll(editBtn, deleteBtn);
        card.getChildren().addAll(imageArea, info, actions);

        card.setOnMouseClicked(e -> {
            if (e.getTarget() instanceof Button) return;
            openDrawer(product);
        });

        return card;
    }

    private Label createPlaceholderIcon(String category) {
        String icon = switch (category != null ? category : "") {
            case "Fruits" -> "F"; case "Vegetables" -> "V"; case "Grains" -> "G";
            case "Dairy" -> "D"; case "Livestock" -> "L"; default -> "?";
        };
        Label lbl = new Label(icon);
        lbl.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: -agri-text-muted;");
        return lbl;
    }

    // ==================== HELPERS ====================

    private VBox fieldCol(String labelText, Control field, Label error) {
        VBox col = new VBox(3);
        col.getStyleClass().add("drawer-field-col");
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("form-label");
        col.getChildren().addAll(lbl, field);
        if (error != null) col.getChildren().add(error);
        if (field instanceof TextField) ((TextField) field).setMaxWidth(Double.MAX_VALUE);
        if (field instanceof TextArea) ((TextArea) field).setMaxWidth(Double.MAX_VALUE);
        return col;
    }

    private void showFieldError(TextField field, Label errorLabel, String msg) {
        if (!field.getStyleClass().contains("field-error")) field.getStyleClass().add("field-error");
        errorLabel.setText(msg); errorLabel.setVisible(true); errorLabel.setManaged(true);
    }

    private void clearFieldError(TextField field, Label errorLabel) {
        field.getStyleClass().remove("field-error");
        errorLabel.setVisible(false); errorLabel.setManaged(false);
    }

    private void refreshGrid() {
        allProducts.setAll(productDAO.getAll());
        String searchText = searchField.getText();
        String category = filterCategoryCombo.getValue();
        String status = filterStatusCombo.getValue();
        String sort = sortCombo.getValue();

        List<Product> filtered = new ArrayList<>();
        for (Product p : allProducts) {
            boolean ok = true;
            if (searchText != null && !searchText.trim().isEmpty()) {
                String lower = searchText.toLowerCase().trim();
                ok = (p.getName() != null && p.getName().toLowerCase().contains(lower))
                        || (p.getDescription() != null && p.getDescription().toLowerCase().contains(lower));
            }
            if (ok && category != null && !"All Categories".equals(category))
                ok = category.equals(p.getCategory());
            if (ok && status != null && !"All Status".equals(status))
                ok = status.equals(p.getStatus());
            if (ok) filtered.add(p);
        }

        if (sort != null) {
            switch (sort) {
                case "Name A-Z" -> filtered.sort(Comparator.comparing(p -> p.getName() != null ? p.getName().toLowerCase() : ""));
                case "Name Z-A" -> filtered.sort(Comparator.comparing((Product p) -> p.getName() != null ? p.getName().toLowerCase() : "").reversed());
                case "Price: Low-High" -> filtered.sort(Comparator.comparingDouble(Product::getPrice));
                case "Price: High-Low" -> filtered.sort(Comparator.comparingDouble(Product::getPrice).reversed());
                case "Low Stock First" -> filtered.sort(Comparator.comparingInt(Product::getQuantity));
                case "Newest" -> filtered.sort(Comparator.comparingLong(Product::getId).reversed());
            }
        }

        productGrid.getChildren().clear();
        if (filtered.isEmpty()) {
            VBox emptyState = new VBox(12);
            emptyState.getStyleClass().add("empty-state");
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPrefWidth(400);
            Label icon = new Label("?");
            icon.getStyleClass().add("empty-state-icon");
            Label titleLbl = new Label("No products found");
            titleLbl.getStyleClass().add("empty-state-title");
            Button addCta = new Button("+ Add Your First Product");
            addCta.getStyleClass().add("btn-primary");
            addCta.setOnAction(e -> openDrawer(null));
            emptyState.getChildren().addAll(icon, titleLbl, addCta);
            productGrid.getChildren().add(emptyState);
        } else {
            for (Product p : filtered) productGrid.getChildren().add(createProductCard(p));
        }
    }

    private String validateFields(TextField nameField, ComboBox<String> categoryCombo,
                                   TextField priceField, TextField quantityField,
                                   ComboBox<String> unitCombo, TextArea descriptionArea) {
        StringBuilder err = new StringBuilder();
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        if (name.isEmpty()) err.append("Name required. ");
        else if (name.length() < 3) err.append("Name too short. ");
        else if (name.length() > 100) err.append("Name too long. ");
        if (categoryCombo.getValue() == null) err.append("Category required. ");
        if (unitCombo.getValue() == null) err.append("Unit required. ");
        try { double p = Double.parseDouble(priceField.getText().trim()); if (p <= 0) err.append("Price > 0. "); }
        catch (Exception e) { err.append("Invalid price. "); }
        try { int q = Integer.parseInt(quantityField.getText().trim()); if (q < 0) err.append("Qty >= 0. "); }
        catch (Exception e) { err.append("Invalid qty. "); }
        String desc = descriptionArea.getText() == null ? "" : descriptionArea.getText().trim();
        if (desc.length() > MAX_DESCRIPTION_LENGTH) err.append("Description too long. ");
        return err.toString();
    }
}
