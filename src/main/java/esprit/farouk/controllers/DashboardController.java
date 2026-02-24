package esprit.farouk.controllers;

import esprit.farouk.models.CartItem;
import esprit.farouk.models.Comment;
import esprit.farouk.models.Event;
import esprit.farouk.models.Farm;
import esprit.farouk.models.Field;
import esprit.farouk.models.Order;
import esprit.farouk.models.Participation;
import esprit.farouk.models.Post;
import esprit.farouk.models.Product;
import esprit.farouk.models.Role;
import esprit.farouk.models.User;
import esprit.farouk.services.CartService;
import esprit.farouk.services.CommentService;
import esprit.farouk.services.EventService;
import esprit.farouk.services.FarmService;
import esprit.farouk.services.FieldService;
import esprit.farouk.services.OrderService;
import esprit.farouk.services.ParticipationService;
import esprit.farouk.services.PostService;
import esprit.farouk.services.ProductService;
import esprit.farouk.services.RoleService;
import esprit.farouk.services.UserService;
import esprit.farouk.utils.EmailUtils;
import esprit.farouk.utils.TranslationUtils;
import esprit.farouk.utils.ValidationUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DashboardController {

    @FXML
    private Label userNameLabel;

    @FXML
    private VBox sidebarMenu;

    @FXML
    private StackPane contentArea;

    private final UserService userService = new UserService();
    private final RoleService roleService = new RoleService();
    private final FarmService farmService = new FarmService();
    private final FieldService fieldService = new FieldService();
    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();
    private final CartService cartService = new CartService();
    private final PostService postService = new PostService();
    private final CommentService commentService = new CommentService();
    private final EventService eventService = new EventService();
    private final ParticipationService participationService = new ParticipationService();

    private User currentUser;

    private void setActiveSidebarButton(Button activeBtn) {
        for (var node : sidebarMenu.getChildren()) {
            node.getStyleClass().remove("sidebar-button-active");
        }
        if (!activeBtn.getStyleClass().contains("sidebar-button-active")) {
            activeBtn.getStyleClass().add("sidebar-button-active");
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        userNameLabel.setText("Welcome, " + user.getName());

        sidebarMenu.getChildren().clear();

        String roleName = "";
        try {
            Role role = roleService.getById(user.getRoleId());
            if (role != null) {
                roleName = role.getName();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if ("admin".equalsIgnoreCase(roleName)) {
            Button homeBtn = new Button("Home");
            homeBtn.getStyleClass().add("sidebar-button");
            homeBtn.setMaxWidth(Double.MAX_VALUE);
            homeBtn.setOnAction(e -> { setActiveSidebarButton(homeBtn); showHomeView(); });

            Button usersBtn = new Button("Users");
            usersBtn.getStyleClass().add("sidebar-button");
            usersBtn.setMaxWidth(Double.MAX_VALUE);
            usersBtn.setOnAction(e -> { setActiveSidebarButton(usersBtn); showUsersView(); });

            Button rolesBtn = new Button("Roles");
            rolesBtn.getStyleClass().add("sidebar-button");
            rolesBtn.setMaxWidth(Double.MAX_VALUE);
            rolesBtn.setOnAction(e -> { setActiveSidebarButton(rolesBtn); showRolesView(); });

            Button farmsBtn = new Button("Farms");
            farmsBtn.getStyleClass().add("sidebar-button");
            farmsBtn.setMaxWidth(Double.MAX_VALUE);
            farmsBtn.setOnAction(e -> { setActiveSidebarButton(farmsBtn); showFarmsView(); });

            Button productsBtn = new Button("Products");
            productsBtn.getStyleClass().add("sidebar-button");
            productsBtn.setMaxWidth(Double.MAX_VALUE);
            productsBtn.setOnAction(e -> { setActiveSidebarButton(productsBtn); showProductsView(); });

            Button ordersBtn = new Button("Orders");
            ordersBtn.getStyleClass().add("sidebar-button");
            ordersBtn.setMaxWidth(Double.MAX_VALUE);
            ordersBtn.setOnAction(e -> { setActiveSidebarButton(ordersBtn); showAllOrdersView(); });

            Button postsBtn = new Button("Posts");
            postsBtn.getStyleClass().add("sidebar-button");
            postsBtn.setMaxWidth(Double.MAX_VALUE);
            postsBtn.setOnAction(e -> { setActiveSidebarButton(postsBtn); showPostsView(); });

            Button blogBtn = new Button("Blog");
            blogBtn.getStyleClass().add("sidebar-button");
            blogBtn.setMaxWidth(Double.MAX_VALUE);
            blogBtn.setOnAction(e -> { setActiveSidebarButton(blogBtn); showBlogView(); });

            Button eventsBtn = new Button("Events");
            eventsBtn.getStyleClass().add("sidebar-button");
            eventsBtn.setMaxWidth(Double.MAX_VALUE);
            eventsBtn.setOnAction(e -> { setActiveSidebarButton(eventsBtn); showEventsView(); });

            Button statsBtn = new Button("Statistics");
            statsBtn.getStyleClass().add("sidebar-button");
            statsBtn.setMaxWidth(Double.MAX_VALUE);
            statsBtn.setOnAction(e -> { setActiveSidebarButton(statsBtn); showStatisticsView(); });

            Button profileBtn = new Button("Profile");
            profileBtn.getStyleClass().add("sidebar-button");
            profileBtn.setMaxWidth(Double.MAX_VALUE);
            profileBtn.setOnAction(e -> { setActiveSidebarButton(profileBtn); showProfileView(); });

            sidebarMenu.getChildren().addAll(homeBtn, usersBtn, rolesBtn, farmsBtn, productsBtn, ordersBtn, postsBtn, blogBtn, eventsBtn, statsBtn, profileBtn);
            setActiveSidebarButton(homeBtn);
            showHomeView();
        } else if ("farmer".equalsIgnoreCase(roleName)) {
            Button homeBtn = new Button("Home");
            homeBtn.getStyleClass().add("sidebar-button");
            homeBtn.setMaxWidth(Double.MAX_VALUE);
            homeBtn.setOnAction(e -> { setActiveSidebarButton(homeBtn); showHomeView(); });

            Button profileBtn = new Button("Profile");
            profileBtn.getStyleClass().add("sidebar-button");
            profileBtn.setMaxWidth(Double.MAX_VALUE);
            profileBtn.setOnAction(e -> { setActiveSidebarButton(profileBtn); showProfileView(); });

            Button myFarmsBtn = new Button("My Farms");
            myFarmsBtn.getStyleClass().add("sidebar-button");
            myFarmsBtn.setMaxWidth(Double.MAX_VALUE);
            myFarmsBtn.setOnAction(e -> { setActiveSidebarButton(myFarmsBtn); showMyFarmsView(); });

            Button myProductsBtn = new Button("My Products");
            myProductsBtn.getStyleClass().add("sidebar-button");
            myProductsBtn.setMaxWidth(Double.MAX_VALUE);
            myProductsBtn.setOnAction(e -> { setActiveSidebarButton(myProductsBtn); showMyProductsView(); });

            Button farmerOrdersBtn = new Button("My Orders");
            farmerOrdersBtn.getStyleClass().add("sidebar-button");
            farmerOrdersBtn.setMaxWidth(Double.MAX_VALUE);
            farmerOrdersBtn.setOnAction(e -> { setActiveSidebarButton(farmerOrdersBtn); showSellerOrdersView(); });

            Button farmerBlogBtn = new Button("Blog");
            farmerBlogBtn.getStyleClass().add("sidebar-button");
            farmerBlogBtn.setMaxWidth(Double.MAX_VALUE);
            farmerBlogBtn.setOnAction(e -> { setActiveSidebarButton(farmerBlogBtn); showBlogView(); });

            Button farmerEventsBtn = new Button("Events");
            farmerEventsBtn.getStyleClass().add("sidebar-button");
            farmerEventsBtn.setMaxWidth(Double.MAX_VALUE);
            farmerEventsBtn.setOnAction(e -> { setActiveSidebarButton(farmerEventsBtn); showBrowseEventsView(); });

            sidebarMenu.getChildren().addAll(homeBtn, profileBtn, myFarmsBtn, myProductsBtn, farmerOrdersBtn, farmerBlogBtn, farmerEventsBtn);
            setActiveSidebarButton(homeBtn);
            showHomeView();
        } else {
            // Customer or other roles
            Button homeBtn = new Button("Home");
            homeBtn.getStyleClass().add("sidebar-button");
            homeBtn.setMaxWidth(Double.MAX_VALUE);
            homeBtn.setOnAction(e -> { setActiveSidebarButton(homeBtn); showHomeView(); });

            Button profileBtn = new Button("Profile");
            profileBtn.getStyleClass().add("sidebar-button");
            profileBtn.setMaxWidth(Double.MAX_VALUE);
            profileBtn.setOnAction(e -> { setActiveSidebarButton(profileBtn); showProfileView(); });

            Button shopBtn = new Button("Shop");
            shopBtn.getStyleClass().add("sidebar-button");
            shopBtn.setMaxWidth(Double.MAX_VALUE);
            shopBtn.setOnAction(e -> { setActiveSidebarButton(shopBtn); showShopView(); });

            Button cartBtn = new Button("Cart");
            cartBtn.getStyleClass().add("sidebar-button");
            cartBtn.setMaxWidth(Double.MAX_VALUE);
            cartBtn.setOnAction(e -> { setActiveSidebarButton(cartBtn); showCartView(); });

            Button customerOrdersBtn = new Button("My Orders");
            customerOrdersBtn.getStyleClass().add("sidebar-button");
            customerOrdersBtn.setMaxWidth(Double.MAX_VALUE);
            customerOrdersBtn.setOnAction(e -> { setActiveSidebarButton(customerOrdersBtn); showCustomerOrdersView(); });

            Button customerBlogBtn = new Button("Blog");
            customerBlogBtn.getStyleClass().add("sidebar-button");
            customerBlogBtn.setMaxWidth(Double.MAX_VALUE);
            customerBlogBtn.setOnAction(e -> { setActiveSidebarButton(customerBlogBtn); showBlogView(); });

            Button customerEventsBtn = new Button("Events");
            customerEventsBtn.getStyleClass().add("sidebar-button");
            customerEventsBtn.setMaxWidth(Double.MAX_VALUE);
            customerEventsBtn.setOnAction(e -> { setActiveSidebarButton(customerEventsBtn); showBrowseEventsView(); });

            sidebarMenu.getChildren().addAll(homeBtn, profileBtn, shopBtn, cartBtn, customerOrdersBtn, customerBlogBtn, customerEventsBtn);
            setActiveSidebarButton(homeBtn);
            showHomeView();
        }
    }

    // ==================== Home / Welcome Page ====================

    private void showHomeView() {
        contentArea.getChildren().clear();

        VBox container = new VBox(20);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(30));

        // Welcome greeting
        Label welcomeTitle = new Label("Welcome back, " + currentUser.getName() + "!");
        welcomeTitle.getStyleClass().add("welcome-title");

        String roleName = "";
        try {
            Role role = roleService.getById(currentUser.getRoleId());
            if (role != null) roleName = role.getName();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
        Label subtitle = new Label(dateStr + "  |  " + roleName.substring(0, 1).toUpperCase() + roleName.substring(1));
        subtitle.getStyleClass().add("welcome-subtitle");

        VBox header = new VBox(5);
        header.setAlignment(Pos.CENTER);
        header.getChildren().addAll(welcomeTitle, subtitle);

        // Welcome card container
        VBox welcomeCard = new VBox(25);
        welcomeCard.getStyleClass().add("welcome-card");
        welcomeCard.setPadding(new Insets(30));
        welcomeCard.setAlignment(Pos.TOP_CENTER);
        welcomeCard.setMaxWidth(900);

        welcomeCard.getChildren().add(header);

        // Stat cards based on role
        HBox cards = new HBox(15);
        cards.setAlignment(Pos.CENTER);

        try {
            if ("admin".equalsIgnoreCase(roleName)) {
                int totalUsers = userService.getAll().size();
                int totalFarms = farmService.getAll().size();
                int totalProducts = productService.getAll().size();
                int totalOrders = orderService.getAll().size();
                int totalPosts = postService.getAll().size();
                int totalEvents = eventService.getAll().size();

                cards.getChildren().addAll(
                    createStatCard("Total Users", String.valueOf(totalUsers), "stat-card-total"),
                    createStatCard("Total Farms", String.valueOf(totalFarms), "stat-card-farms"),
                    createStatCard("Total Products", String.valueOf(totalProducts), "stat-card-products"),
                    createStatCard("Total Orders", String.valueOf(totalOrders), "stat-card-orders"),
                    createStatCard("Total Posts", String.valueOf(totalPosts), "stat-card-posts"),
                    createStatCard("Total Events", String.valueOf(totalEvents), "stat-card-events")
                );
            } else if ("farmer".equalsIgnoreCase(roleName)) {
                int myFarms = farmService.getByUserId(currentUser.getId()).size();
                int myProducts = productService.getByUserId(currentUser.getId()).size();
                long pendingOrders = orderService.getBySellerId(currentUser.getId()).stream()
                        .filter(o -> "pending".equals(o.getStatus())).count();
                int upcomingEvents = eventService.getUpcoming().size();

                cards.getChildren().addAll(
                    createStatCard("My Farms", String.valueOf(myFarms), "stat-card-farms"),
                    createStatCard("My Products", String.valueOf(myProducts), "stat-card-products"),
                    createStatCard("Pending Orders", String.valueOf(pendingOrders), "stat-card-orders"),
                    createStatCard("Upcoming Events", String.valueOf(upcomingEvents), "stat-card-events")
                );
            } else {
                // Customer
                int myOrders = orderService.getByCustomerId(currentUser.getId()).size();
                int cartItems = cartService.getCartItems(currentUser.getId()).size();
                int publishedPosts = postService.getPublished().size();
                int upcomingEvents = eventService.getUpcoming().size();

                cards.getChildren().addAll(
                    createStatCard("My Orders", String.valueOf(myOrders), "stat-card-orders"),
                    createStatCard("Cart Items", String.valueOf(cartItems), "stat-card-products"),
                    createStatCard("Blog Posts", String.valueOf(publishedPosts), "stat-card-posts"),
                    createStatCard("Upcoming Events", String.valueOf(upcomingEvents), "stat-card-events")
                );
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load stats: " + e.getMessage());
        }

        welcomeCard.getChildren().add(cards);
        container.getChildren().add(welcomeCard);

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        contentArea.getChildren().add(scroll);
    }

    @FXML
    private void showUsersView() {
        contentArea.getChildren().clear();

        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_LEFT);

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("User Management");
        title.getStyleClass().add("content-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("action-button-add");
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("action-button-edit");
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("action-button-delete");
        Button blockBtn = new Button("Block/Unblock");
        blockBtn.getStyleClass().add("action-button-block");

        header.getChildren().addAll(title, spacer, addBtn, editBtn, deleteBtn, blockBtn);

        // Search & Filter bar
        HBox filterBar = new HBox(10);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search by name or email...");
        searchField.getStyleClass().add("search-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        ComboBox<String> statusFilter = new ComboBox<>(FXCollections.observableArrayList("All", "Active", "Inactive", "Blocked"));
        statusFilter.setValue("All");
        statusFilter.getStyleClass().add("filter-combo");

        filterBar.getChildren().addAll(searchField, statusFilter);

        // Table
        TableView<User> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<User, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(60);

        TableColumn<User, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<User, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));

        TableColumn<User, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<User, Long> roleCol = new TableColumn<>("Role ID");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("roleId"));
        roleCol.setMaxWidth(80);

        table.getColumns().addAll(idCol, nameCol, emailCol, phoneCol, statusCol, roleCol);

        // Load data with FilteredList + SortedList
        ObservableList<User> masterData = FXCollections.observableArrayList();
        try {
            masterData.addAll(userService.getAll());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load users: " + e.getMessage());
        }

        FilteredList<User> filteredData = new FilteredList<>(masterData, p -> true);

        // Update predicate when search text or status filter changes
        searchField.textProperty().addListener((obs, oldVal, newVal) ->
                filteredData.setPredicate(user -> filterUser(user, newVal, statusFilter.getValue())));
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) ->
                filteredData.setPredicate(user -> filterUser(user, searchField.getText(), newVal)));

        SortedList<User> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        // Reload helper
        Runnable reloadTable = () -> {
            masterData.clear();
            try {
                masterData.addAll(userService.getAll());
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load users: " + ex.getMessage());
            }
        };

        // Button actions
        addBtn.setOnAction(e -> {
            showUserFormDialog(null);
            reloadTable.run();
        });

        editBtn.setOnAction(e -> {
            User selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to edit.");
                return;
            }
            showUserFormDialog(selected);
            reloadTable.run();
        });

        deleteBtn.setOnAction(e -> {
            User selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to delete.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete user \"" + selected.getName() + "\"?");
            confirm.setHeaderText("Confirm Deletion");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    userService.delete(selected.getId());
                    reloadTable.run();
                } catch (SQLException ex) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete user: " + ex.getMessage());
                }
            }
        });

        blockBtn.setOnAction(e -> {
            User selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to block/unblock.");
                return;
            }
            if (currentUser != null && selected.getId() == currentUser.getId()) {
                showAlert(Alert.AlertType.WARNING, "Action Denied", "You cannot block your own account.");
                return;
            }
            String newStatus = "blocked".equals(selected.getStatus()) ? "active" : "blocked";
            String action = "blocked".equals(newStatus) ? "block" : "unblock";
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to " + action + " \"" + selected.getName() + "\"?");
            confirm.setHeaderText("Confirm " + action.substring(0, 1).toUpperCase() + action.substring(1));
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    selected.setStatus(newStatus);
                    userService.update(selected);
                    reloadTable.run();
                } catch (SQLException ex) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to " + action + " user: " + ex.getMessage());
                }
            }
        });

        container.getChildren().addAll(header, filterBar, table);
        contentArea.getChildren().add(container);
    }

    private boolean filterUser(User user, String searchText, String statusValue) {
        boolean matchesSearch = true;
        if (searchText != null && !searchText.trim().isEmpty()) {
            String lower = searchText.trim().toLowerCase();
            matchesSearch = (user.getName() != null && user.getName().toLowerCase().contains(lower))
                    || (user.getEmail() != null && user.getEmail().toLowerCase().contains(lower));
        }
        boolean matchesStatus = true;
        if (statusValue != null && !"All".equals(statusValue)) {
            matchesStatus = statusValue.toLowerCase().equals(user.getStatus());
        }
        return matchesSearch && matchesStatus;
    }

    @FXML
    private void showRolesView() {
        contentArea.getChildren().clear();

        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_LEFT);

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Role Management");
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

        // Table
        TableView<Role> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Role, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(60);

        TableColumn<Role, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Role, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        table.getColumns().addAll(idCol, nameCol, descCol);

        loadRoles(table);

        // Button actions
        addBtn.setOnAction(e -> {
            showRoleFormDialog(null);
            loadRoles(table);
        });

        editBtn.setOnAction(e -> {
            Role selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a role to edit.");
                return;
            }
            showRoleFormDialog(selected);
            loadRoles(table);
        });

        deleteBtn.setOnAction(e -> {
            Role selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a role to delete.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete role \"" + selected.getName() + "\"?");
            confirm.setHeaderText("Confirm Deletion");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    roleService.delete(selected.getId());
                    loadRoles(table);
                } catch (SQLException ex) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete role: " + ex.getMessage());
                }
            }
        });

        container.getChildren().addAll(header, table);
        contentArea.getChildren().add(container);
    }

    private void showUserFormDialog(User user) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(user == null ? "Add User" : "Edit User");
        dialog.setHeaderText(user == null ? "Create a new user" : "Edit user: " + user.getName());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField(user != null ? user.getName() : "");
        nameField.setPromptText("Name");
        TextField emailField = new TextField(user != null ? user.getEmail() : "");
        emailField.setPromptText("Email");
        TextField phoneField = new TextField(user != null ? user.getPhone() : "");
        phoneField.setPromptText("Phone");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList("active", "inactive", "blocked"));
        statusCombo.setValue(user != null ? user.getStatus() : "active");

        ComboBox<Role> roleCombo = new ComboBox<>();
        try {
            List<Role> roles = roleService.getAll();
            roleCombo.setItems(FXCollections.observableArrayList(roles));
            roleCombo.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Role item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getName());
                }
            });
            roleCombo.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Role item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getName());
                }
            });
            if (user != null) {
                for (Role r : roles) {
                    if (r.getId() == user.getRoleId()) {
                        roleCombo.setValue(r);
                        break;
                    }
                }
            } else if (!roles.isEmpty()) {
                roleCombo.setValue(roles.get(0));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load roles: " + e.getMessage());
        }

        int row = 0;
        grid.add(new Label("Name:"), 0, row);
        grid.add(nameField, 1, row++);
        grid.add(new Label("Email:"), 0, row);
        grid.add(emailField, 1, row++);
        grid.add(new Label("Phone:"), 0, row);
        grid.add(phoneField, 1, row++);
        if (user == null) {
            grid.add(new Label("Password:"), 0, row);
            grid.add(passwordField, 1, row++);
        }
        grid.add(new Label("Status:"), 0, row);
        grid.add(statusCombo, 1, row++);
        grid.add(new Label("Role:"), 0, row);
        grid.add(roleCombo, 1, row);

        dialog.getDialogPane().setContent(grid);

        // Validation loop: re-show dialog if validation fails
        while (true) {
            Optional<ButtonType> result = dialog.showAndWait();
            if (!result.isPresent() || result.get() != ButtonType.OK) {
                break;
            }

            String nameVal = nameField.getText().trim();
            String emailVal = emailField.getText().trim();
            String phoneVal = phoneField.getText().trim();

            if (!ValidationUtils.isValidName(nameVal)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Name must be at least 2 characters and contain only letters.");
                continue;
            }
            if (!ValidationUtils.isValidEmail(emailVal)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid email address.");
                continue;
            }
            if (!ValidationUtils.isValidPhone(phoneVal)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Phone must be 8-15 digits, optionally starting with +.");
                continue;
            }
            if (user == null && passwordField.getText().length() < 6) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Password must be at least 6 characters.");
                continue;
            }

            try {
                if (user == null) {
                    User newUser = new User();
                    newUser.setName(nameVal);
                    newUser.setEmail(emailVal);
                    newUser.setPhone(phoneVal);
                    newUser.setPassword(passwordField.getText());
                    newUser.setStatus(statusCombo.getValue());
                    newUser.setRoleId(roleCombo.getValue() != null ? roleCombo.getValue().getId() : 1);
                    userService.add(newUser);
                } else {
                    user.setName(nameVal);
                    user.setEmail(emailVal);
                    user.setPhone(phoneVal);
                    user.setStatus(statusCombo.getValue());
                    user.setRoleId(roleCombo.getValue() != null ? roleCombo.getValue().getId() : user.getRoleId());
                    userService.update(user);
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save user: " + e.getMessage());
            }
            break;
        }
    }

    private void showRoleFormDialog(Role role) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(role == null ? "Add Role" : "Edit Role");
        dialog.setHeaderText(role == null ? "Create a new role" : "Edit role: " + role.getName());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField(role != null ? role.getName() : "");
        nameField.setPromptText("Role name");
        TextField descField = new TextField(role != null ? role.getDescription() : "");
        descField.setPromptText("Description");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (role == null) {
                    Role newRole = new Role();
                    newRole.setName(nameField.getText().trim());
                    newRole.setDescription(descField.getText().trim());
                    roleService.add(newRole);
                } else {
                    role.setName(nameField.getText().trim());
                    role.setDescription(descField.getText().trim());
                    roleService.update(role);
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save role: " + e.getMessage());
            }
        }
    }

    private static final String UPLOADS_DIR = "uploads/profile_pictures/";

    private void showProfileView() {
        contentArea.getChildren().clear();

        VBox container = new VBox(20);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(30));

        Label title = new Label("My Profile");
        title.getStyleClass().add("content-title");

        // Profile picture
        ImageView profileImage = new ImageView();
        profileImage.setFitWidth(120);
        profileImage.setFitHeight(120);
        profileImage.setPreserveRatio(false);
        profileImage.setSmooth(true);
        Circle clip = new Circle(60, 60, 60);
        profileImage.setClip(clip);
        loadProfileImage(profileImage, currentUser.getProfilePicture());

        Button changePicBtn = new Button("Change Picture");
        changePicBtn.getStyleClass().add("profile-pic-button");
        final String[] selectedPicPath = {null};
        changePicBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose Profile Picture");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            File file = fileChooser.showOpenDialog(contentArea.getScene().getWindow());
            if (file != null) {
                selectedPicPath[0] = file.getAbsolutePath();
                profileImage.setImage(new Image(file.toURI().toString(), 120, 120, false, true));
            }
        });

        Button deletePicBtn = new Button("Remove Picture");
        deletePicBtn.getStyleClass().add("profile-pic-delete-button");
        final boolean[] deletePic = {false};
        deletePicBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to remove your profile picture?");
            confirm.setHeaderText("Remove Profile Picture");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                selectedPicPath[0] = null;
                deletePic[0] = true;
                profileImage.setImage(null);
                profileImage.setStyle("-fx-background-color: #e0e0e0;");
            }
        });

        HBox picButtons = new HBox(10, changePicBtn, deletePicBtn);
        picButtons.setAlignment(Pos.CENTER);

        VBox picBox = new VBox(10, profileImage, picButtons);
        picBox.setAlignment(Pos.CENTER);

        GridPane form = new GridPane();
        form.getStyleClass().add("profile-form");
        form.setHgap(15);
        form.setVgap(15);
        form.setPadding(new Insets(25));
        form.setMaxWidth(450);

        TextField nameField = new TextField(currentUser.getName());
        nameField.setPromptText("Full Name");
        nameField.getStyleClass().add("text-input");

        TextField emailField = new TextField(currentUser.getEmail());
        emailField.setPromptText("Email");
        emailField.getStyleClass().add("text-input");

        TextField phoneField = new TextField(currentUser.getPhone() != null ? currentUser.getPhone() : "");
        phoneField.setPromptText("Phone");
        phoneField.getStyleClass().add("text-input");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("New password (leave empty to keep current)");
        passwordField.getStyleClass().add("text-input");

        Label feedbackLabel = new Label();
        feedbackLabel.setVisible(false);
        feedbackLabel.setManaged(false);

        int row = 0;
        form.add(new Label("Name:"), 0, row);
        form.add(nameField, 1, row++);
        form.add(new Label("Email:"), 0, row);
        form.add(emailField, 1, row++);
        form.add(new Label("Phone:"), 0, row);
        form.add(phoneField, 1, row++);
        form.add(new Label("Password:"), 0, row);
        form.add(passwordField, 1, row++);

        Button saveBtn = new Button("Save");
        saveBtn.getStyleClass().add("profile-save-button");
        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            if (name.isEmpty() || email.isEmpty()) {
                feedbackLabel.setText("Name and email are required.");
                feedbackLabel.getStyleClass().setAll("error-label");
                feedbackLabel.setVisible(true);
                feedbackLabel.setManaged(true);
                return;
            }
            if (!ValidationUtils.isValidName(name)) {
                feedbackLabel.setText("Name must be at least 2 characters and contain only letters.");
                feedbackLabel.getStyleClass().setAll("error-label");
                feedbackLabel.setVisible(true);
                feedbackLabel.setManaged(true);
                return;
            }
            if (!ValidationUtils.isValidEmail(email)) {
                feedbackLabel.setText("Please enter a valid email address.");
                feedbackLabel.getStyleClass().setAll("error-label");
                feedbackLabel.setVisible(true);
                feedbackLabel.setManaged(true);
                return;
            }
            if (!ValidationUtils.isValidPhone(phone)) {
                feedbackLabel.setText("Phone must be 8-15 digits, optionally starting with +.");
                feedbackLabel.getStyleClass().setAll("error-label");
                feedbackLabel.setVisible(true);
                feedbackLabel.setManaged(true);
                return;
            }
            try {
                // Handle profile picture
                if (deletePic[0]) {
                    String oldPic = currentUser.getProfilePicture();
                    if (oldPic != null && !oldPic.isEmpty()) {
                        File oldFile = new File(oldPic);
                        oldFile.delete();
                    }
                    currentUser.setProfilePicture(null);
                    deletePic[0] = false;
                } else if (selectedPicPath[0] != null) {
                    String savedPath = saveProfilePicture(selectedPicPath[0], currentUser.getId());
                    currentUser.setProfilePicture(savedPath);
                }

                currentUser.setName(name);
                currentUser.setEmail(email);
                currentUser.setPhone(phone);
                userService.update(currentUser);

                String newPassword = passwordField.getText();
                if (!newPassword.isEmpty()) {
                    if (newPassword.length() < 6) {
                        feedbackLabel.setText("Password must be at least 6 characters.");
                        feedbackLabel.getStyleClass().setAll("error-label");
                        feedbackLabel.setVisible(true);
                        feedbackLabel.setManaged(true);
                        return;
                    }
                    userService.updatePassword(currentUser.getId(), newPassword);
                }

                userNameLabel.setText("Welcome, " + currentUser.getName());
                feedbackLabel.setText("Profile updated successfully!");
                feedbackLabel.getStyleClass().setAll("success-label");
                feedbackLabel.setVisible(true);
                feedbackLabel.setManaged(true);
                passwordField.clear();
                selectedPicPath[0] = null;
            } catch (SQLException ex) {
                feedbackLabel.setText("Failed to update profile: " + ex.getMessage());
                feedbackLabel.getStyleClass().setAll("error-label");
                feedbackLabel.setVisible(true);
                feedbackLabel.setManaged(true);
            } catch (IOException ex) {
                feedbackLabel.setText("Failed to save profile picture.");
                feedbackLabel.getStyleClass().setAll("error-label");
                feedbackLabel.setVisible(true);
                feedbackLabel.setManaged(true);
            }
        });

        form.add(saveBtn, 1, row);

        // Face Recognition section (All roles except Guest)
        VBox faceRecognitionBox = null;
        try {
            Role role = roleService.getById(currentUser.getRoleId());
            if (role != null && !"guest".equalsIgnoreCase(role.getName())) {
                faceRecognitionBox = createFaceRecognitionSection();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        if (faceRecognitionBox != null) {
            container.getChildren().addAll(title, picBox, form, feedbackLabel, faceRecognitionBox);
        } else {
            container.getChildren().addAll(title, picBox, form, feedbackLabel);
        }
        contentArea.getChildren().add(container);
    }

    private VBox createFaceRecognitionSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setMaxWidth(450);
        section.setStyle("-fx-background-color: -color-surface; -fx-background-radius: 12; " +
                         "-fx-border-color: -color-border; -fx-border-radius: 12; -fx-border-width: 1;");

        Label sectionTitle = new Label("Face Recognition");
        sectionTitle.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: -color-brand;");

        HBox statusRow = new HBox(10);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        Label statusLabel = new Label();
        Button actionButton = new Button();

        try {
            boolean hasEnrollment = userService.hasFaceEnrollment(currentUser.getId());

            if (hasEnrollment) {
                statusLabel.setText("✓ Face recognition enabled");
                statusLabel.setStyle("-fx-text-fill: -color-success; -fx-font-weight: bold;");

                actionButton.setText("Remove Face Data");
                actionButton.getStyleClass().add("profile-pic-delete-button");
                actionButton.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Are you sure you want to remove your face recognition data?");
                    confirm.setHeaderText("Remove Face Data");
                    if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                        try {
                            userService.removeFaceEnrollment(currentUser.getId());
                            Alert success = new Alert(Alert.AlertType.INFORMATION,
                                    "Face recognition data removed successfully.");
                            success.showAndWait();
                            showProfileView(); // Refresh view
                        } catch (SQLException ex) {
                            Alert error = new Alert(Alert.AlertType.ERROR,
                                    "Failed to remove face data: " + ex.getMessage());
                            error.showAndWait();
                        }
                    }
                });
            } else {
                statusLabel.setText("Face recognition not enabled");
                statusLabel.setStyle("-fx-text-fill: -color-text-muted;");

                actionButton.setText("Setup Face Recognition");
                actionButton.getStyleClass().add("profile-pic-button");
                actionButton.setOnAction(e -> showFaceEnrollmentDialog());
            }

        } catch (SQLException ex) {
            statusLabel.setText("Error checking face enrollment status");
            statusLabel.setStyle("-fx-text-fill: -color-danger;");
            actionButton.setDisable(true);
        }

        statusRow.getChildren().addAll(statusLabel, actionButton);

        Label description = new Label(
                "Face recognition allows you to login quickly and securely by scanning your face.");
        description.setWrapText(true);
        description.setStyle("-fx-text-fill: -color-text-muted; -fx-font-size: 12;");

        section.getChildren().addAll(sectionTitle, statusRow, description);
        return section;
    }

    private void showFaceEnrollmentDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/face_enrollment.fxml"));
            VBox dialogContent = loader.load();

            FaceEnrollmentController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Stage dialog = new Stage();
            dialog.setTitle("Face Enrollment");
            dialog.setScene(new Scene(dialogContent));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(contentArea.getScene().getWindow());

            dialog.setOnHidden(e -> showProfileView()); // Refresh profile view when dialog closes

            dialog.showAndWait();

        } catch (IOException ex) {
            Alert error = new Alert(Alert.AlertType.ERROR,
                    "Failed to open face enrollment dialog: " + ex.getMessage());
            error.showAndWait();
        }
    }

    private String saveProfilePicture(String sourcePath, long userId) throws IOException {
        Path uploadsPath = Paths.get(UPLOADS_DIR);
        if (!Files.exists(uploadsPath)) {
            Files.createDirectories(uploadsPath);
        }
        String extension = sourcePath.substring(sourcePath.lastIndexOf('.'));
        String fileName = "user_" + userId + extension;
        Path destination = uploadsPath.resolve(fileName);
        Files.copy(Paths.get(sourcePath), destination, StandardCopyOption.REPLACE_EXISTING);
        return destination.toString();
    }

    private void loadProfileImage(ImageView imageView, String picturePath) {
        if (picturePath != null && !picturePath.isEmpty()) {
            File file = new File(picturePath);
            if (file.exists()) {
                imageView.setImage(new Image(file.toURI().toString(), 120, 120, false, true));
                return;
            }
        }
        // Gray placeholder when no picture
        imageView.setStyle("-fx-background-color: #e0e0e0;");
    }

    private void showStatisticsView() {
        contentArea.getChildren().clear();

        VBox container = new VBox(20);
        container.setAlignment(Pos.TOP_LEFT);
        container.setPadding(new Insets(10));

        Label title = new Label("User Statistics");
        title.getStyleClass().add("content-title");

        List<User> users;
        List<Role> roles;
        try {
            users = userService.getAll();
            roles = roleService.getAll();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load data: " + e.getMessage());
            return;
        }

        int total = users.size();
        long active = users.stream().filter(u -> "active".equals(u.getStatus())).count();
        long inactive = users.stream().filter(u -> "inactive".equals(u.getStatus())).count();
        long blocked = users.stream().filter(u -> "blocked".equals(u.getStatus())).count();

        // Stat cards
        HBox cards = new HBox(15);
        cards.setAlignment(Pos.CENTER_LEFT);
        cards.getChildren().addAll(
                createStatCard("Total Users", String.valueOf(total), "stat-card-total"),
                createStatCard("Active", String.valueOf(active), "stat-card-active"),
                createStatCard("Inactive", String.valueOf(inactive), "stat-card-inactive"),
                createStatCard("Blocked", String.valueOf(blocked), "stat-card-blocked")
        );

        // Charts row
        HBox chartsRow = new HBox(20);
        VBox.setVgrow(chartsRow, Priority.ALWAYS);

        // Pie chart - Users per role
        Map<Long, String> roleNames = new LinkedHashMap<>();
        for (Role r : roles) roleNames.put(r.getId(), r.getName());

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        Map<Long, Long> roleCounts = new LinkedHashMap<>();
        for (User u : users) {
            roleCounts.merge(u.getRoleId(), 1L, Long::sum);
        }
        for (Map.Entry<Long, Long> entry : roleCounts.entrySet()) {
            String name = roleNames.getOrDefault(entry.getKey(), "Role " + entry.getKey());
            pieData.add(new PieChart.Data(name + " (" + entry.getValue() + ")", entry.getValue()));
        }
        PieChart pieChart = new PieChart(pieData);
        pieChart.setTitle("Users per Role");
        pieChart.setLabelsVisible(true);
        pieChart.setMaxHeight(350);
        HBox.setHgrow(pieChart, Priority.ALWAYS);

        // Bar chart - Registrations over last 7 days
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Registrations");
        yAxis.setTickUnit(1);
        yAxis.setMinorTickVisible(false);

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Registrations (Last 7 Days)");
        barChart.setLegendVisible(false);
        barChart.setMaxHeight(350);
        HBox.setHgrow(barChart, Priority.ALWAYS);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd");
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            long count = users.stream()
                    .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().toLocalDate().equals(day))
                    .count();
            series.getData().add(new XYChart.Data<>(day.format(fmt), count));
        }
        barChart.getData().add(series);

        chartsRow.getChildren().addAll(pieChart, barChart);

        container.getChildren().addAll(title, cards, chartsRow);
        contentArea.getChildren().add(container);
    }

    private VBox createStatCard(String label, String value, String styleClass) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().addAll("stat-card", styleClass);
        card.setPadding(new Insets(15, 25, 15, 25));
        card.setMinWidth(130);

        Label valLabel = new Label(value);
        valLabel.getStyleClass().add("stat-card-value");
        Label nameLabel = new Label(label);
        nameLabel.getStyleClass().add("stat-card-label");

        card.getChildren().addAll(valLabel, nameLabel);
        return card;
    }

    // ==================== Farm Management (Admin) ====================

    private void showFarmsView() {
        contentArea.getChildren().clear();

        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_LEFT);

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Farm Management");
        title.getStyleClass().add("content-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("action-button-add");
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("action-button-edit");
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("action-button-delete");
        Button approveBtn = new Button("Approve");
        approveBtn.getStyleClass().add("action-button-approve");
        Button rejectBtn = new Button("Reject");
        rejectBtn.getStyleClass().add("action-button-reject");
        Button fieldsBtn = new Button("Fields");
        fieldsBtn.getStyleClass().add("action-button-edit");

        header.getChildren().addAll(title, spacer, addBtn, editBtn, deleteBtn, approveBtn, rejectBtn, fieldsBtn);

        // Search & Filter bar
        HBox filterBar = new HBox(10);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search by name or location...");
        searchField.getStyleClass().add("search-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        ComboBox<String> statusFilter = new ComboBox<>(FXCollections.observableArrayList("All", "Pending", "Approved", "Rejected", "Inactive"));
        statusFilter.setValue("All");
        statusFilter.getStyleClass().add("filter-combo");

        filterBar.getChildren().addAll(searchField, statusFilter);

        // Table
        TableView<Farm> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Farm, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(60);

        TableColumn<Farm, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Farm, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));

        TableColumn<Farm, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("farmType"));

        TableColumn<Farm, Double> areaCol = new TableColumn<>("Area");
        areaCol.setCellValueFactory(new PropertyValueFactory<>("area"));
        areaCol.setMaxWidth(80);

        TableColumn<Farm, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Farm, Long> userCol = new TableColumn<>("User ID");
        userCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        userCol.setMaxWidth(80);

        table.getColumns().addAll(idCol, nameCol, locationCol, typeCol, areaCol, statusCol, userCol);

        // Load data with FilteredList + SortedList
        ObservableList<Farm> masterData = FXCollections.observableArrayList();
        try {
            masterData.addAll(farmService.getAll());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load farms: " + e.getMessage());
        }

        FilteredList<Farm> filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) ->
                filteredData.setPredicate(farm -> filterFarm(farm, newVal, statusFilter.getValue())));
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) ->
                filteredData.setPredicate(farm -> filterFarm(farm, searchField.getText(), newVal)));

        SortedList<Farm> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        // Reload helper
        Runnable reloadTable = () -> {
            masterData.clear();
            try {
                masterData.addAll(farmService.getAll());
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load farms: " + ex.getMessage());
            }
        };

        // Button actions
        addBtn.setOnAction(e -> {
            showFarmFormDialog(null, true);
            reloadTable.run();
        });

        editBtn.setOnAction(e -> {
            Farm selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a farm to edit.");
                return;
            }
            showFarmFormDialog(selected, true);
            reloadTable.run();
        });

        deleteBtn.setOnAction(e -> {
            Farm selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a farm to delete.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete farm \"" + selected.getName() + "\"?");
            confirm.setHeaderText("Confirm Deletion");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    farmService.delete(selected.getId());
                    reloadTable.run();
                } catch (SQLException ex) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete farm: " + ex.getMessage());
                }
            }
        });

        approveBtn.setOnAction(e -> {
            Farm selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a farm to approve.");
                return;
            }
            if ("approved".equals(selected.getStatus())) {
                showAlert(Alert.AlertType.INFORMATION, "Already Approved", "This farm is already approved.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Approve farm \"" + selected.getName() + "\"?");
            confirm.setHeaderText("Confirm Approval");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    selected.setStatus("approved");
                    selected.setApprovedAt(java.time.LocalDateTime.now());
                    selected.setApprovedBy(currentUser.getId());
                    farmService.update(selected);
                    reloadTable.run();
                } catch (SQLException ex) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to approve farm: " + ex.getMessage());
                }
            }
        });

        rejectBtn.setOnAction(e -> {
            Farm selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a farm to reject.");
                return;
            }
            if ("rejected".equals(selected.getStatus())) {
                showAlert(Alert.AlertType.INFORMATION, "Already Rejected", "This farm is already rejected.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Reject farm \"" + selected.getName() + "\"?");
            confirm.setHeaderText("Confirm Rejection");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    selected.setStatus("rejected");
                    selected.setApprovedAt(null);
                    selected.setApprovedBy(null);
                    farmService.update(selected);
                    reloadTable.run();
                } catch (SQLException ex) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to reject farm: " + ex.getMessage());
                }
            }
        });

        fieldsBtn.setOnAction(e -> {
            Farm selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a farm to view its fields.");
                return;
            }
            showFieldsView(selected);
        });

        container.getChildren().addAll(header, filterBar, table);
        contentArea.getChildren().add(container);
    }

    private boolean filterFarm(Farm farm, String searchText, String statusValue) {
        boolean matchesSearch = true;
        if (searchText != null && !searchText.trim().isEmpty()) {
            String lower = searchText.trim().toLowerCase();
            matchesSearch = (farm.getName() != null && farm.getName().toLowerCase().contains(lower))
                    || (farm.getLocation() != null && farm.getLocation().toLowerCase().contains(lower));
        }
        boolean matchesStatus = true;
        if (statusValue != null && !"All".equals(statusValue)) {
            matchesStatus = statusValue.toLowerCase().equals(farm.getStatus());
        }
        return matchesSearch && matchesStatus;
    }

    // ==================== My Farms (Farmer) ====================

    private void showMyFarmsView() {
        contentArea.getChildren().clear();

        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_LEFT);

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("My Farms");
        title.getStyleClass().add("content-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("action-button-add");
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("action-button-edit");
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("action-button-delete");
        Button fieldsBtn = new Button("Fields");
        fieldsBtn.getStyleClass().add("action-button-edit");

        header.getChildren().addAll(title, spacer, addBtn, editBtn, deleteBtn, fieldsBtn);

        // Search & Filter bar
        HBox filterBar = new HBox(10);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search by name or location...");
        searchField.getStyleClass().add("search-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        ComboBox<String> statusFilter = new ComboBox<>(FXCollections.observableArrayList("All", "Pending", "Approved", "Rejected", "Inactive"));
        statusFilter.setValue("All");
        statusFilter.getStyleClass().add("filter-combo");

        filterBar.getChildren().addAll(searchField, statusFilter);

        // Table
        TableView<Farm> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Farm, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(60);

        TableColumn<Farm, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Farm, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));

        TableColumn<Farm, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("farmType"));

        TableColumn<Farm, Double> areaCol = new TableColumn<>("Area");
        areaCol.setCellValueFactory(new PropertyValueFactory<>("area"));
        areaCol.setMaxWidth(80);

        TableColumn<Farm, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(idCol, nameCol, locationCol, typeCol, areaCol, statusCol);

        // Load data - only current user's farms
        ObservableList<Farm> masterData = FXCollections.observableArrayList();
        try {
            masterData.addAll(farmService.getByUserId(currentUser.getId()));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load farms: " + e.getMessage());
        }

        FilteredList<Farm> filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) ->
                filteredData.setPredicate(farm -> filterFarm(farm, newVal, statusFilter.getValue())));
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) ->
                filteredData.setPredicate(farm -> filterFarm(farm, searchField.getText(), newVal)));

        SortedList<Farm> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        // Reload helper
        Runnable reloadTable = () -> {
            masterData.clear();
            try {
                masterData.addAll(farmService.getByUserId(currentUser.getId()));
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load farms: " + ex.getMessage());
            }
        };

        // Button actions
        addBtn.setOnAction(e -> {
            showFarmFormDialog(null, false);
            reloadTable.run();
        });

        editBtn.setOnAction(e -> {
            Farm selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a farm to edit.");
                return;
            }
            showFarmFormDialog(selected, false);
            reloadTable.run();
        });

        deleteBtn.setOnAction(e -> {
            Farm selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a farm to delete.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete farm \"" + selected.getName() + "\"?");
            confirm.setHeaderText("Confirm Deletion");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    farmService.delete(selected.getId());
                    reloadTable.run();
                } catch (SQLException ex) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete farm: " + ex.getMessage());
                }
            }
        });

        fieldsBtn.setOnAction(e -> {
            Farm selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a farm to view its fields.");
                return;
            }
            showFieldsView(selected);
        });

        container.getChildren().addAll(header, filterBar, table);
        contentArea.getChildren().add(container);
    }

    // ==================== Farm Form Dialog ====================

    private void showFarmFormDialog(Farm farm, boolean isAdmin) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(farm == null ? "Add Farm" : "Edit Farm");
        dialog.setHeaderText(farm == null ? "Create a new farm" : "Edit farm: " + farm.getName());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField(farm != null ? farm.getName() : "");
        nameField.setPromptText("Farm name");
        TextField locationField = new TextField(farm != null ? farm.getLocation() : "");
        locationField.setPromptText("Location");
        TextField latField = new TextField(farm != null && farm.getLatitude() != null ? farm.getLatitude().toString() : "");
        latField.setPromptText("Latitude (optional)");
        TextField lngField = new TextField(farm != null && farm.getLongitude() != null ? farm.getLongitude().toString() : "");
        lngField.setPromptText("Longitude (optional)");

        Button mapPickerBtn = new Button("Pick on Map");
        mapPickerBtn.getStyleClass().add("action-button-add");
        mapPickerBtn.setOnAction(ev -> showMapPickerDialog(latField, lngField));

        TextField areaField = new TextField(farm != null && farm.getArea() != null ? farm.getArea().toString() : "");
        areaField.setPromptText("Area in hectares (optional)");
        TextField typeField = new TextField(farm != null ? (farm.getFarmType() != null ? farm.getFarmType() : "") : "");
        typeField.setPromptText("Farm type (optional)");
        TextField descField = new TextField(farm != null ? (farm.getDescription() != null ? farm.getDescription() : "") : "");
        descField.setPromptText("Description (optional)");

        int row = 0;
        grid.add(new Label("Name:"), 0, row);
        grid.add(nameField, 1, row++);
        grid.add(new Label("Location:"), 0, row);
        grid.add(locationField, 1, row++);
        grid.add(new Label("Coordinates:"), 0, row);
        HBox coordBox = new HBox(8, latField, lngField, mapPickerBtn);
        coordBox.setAlignment(Pos.CENTER_LEFT);
        grid.add(coordBox, 1, row++);
        grid.add(new Label("Area:"), 0, row);
        grid.add(areaField, 1, row++);
        grid.add(new Label("Type:"), 0, row);
        grid.add(typeField, 1, row++);
        grid.add(new Label("Description:"), 0, row);
        grid.add(descField, 1, row++);

        // Admin-only fields: status and user selection
        ComboBox<String> statusCombo = null;
        TextField userIdField = null;
        if (isAdmin) {
            statusCombo = new ComboBox<>(FXCollections.observableArrayList("pending", "approved", "rejected", "inactive"));
            statusCombo.setValue(farm != null ? farm.getStatus() : "pending");
            grid.add(new Label("Status:"), 0, row);
            grid.add(statusCombo, 1, row++);

            userIdField = new TextField(farm != null ? String.valueOf(farm.getUserId()) : "");
            userIdField.setPromptText("User ID (owner)");
            grid.add(new Label("User ID:"), 0, row);
            grid.add(userIdField, 1, row);
        }

        dialog.getDialogPane().setContent(grid);

        final ComboBox<String> finalStatusCombo = statusCombo;
        final TextField finalUserIdField = userIdField;

        // Validation loop
        while (true) {
            Optional<ButtonType> result = dialog.showAndWait();
            if (!result.isPresent() || result.get() != ButtonType.OK) {
                break;
            }

            String nameVal = nameField.getText().trim();
            String locationVal = locationField.getText().trim();

            if (nameVal.length() < 2) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Farm name must be at least 2 characters.");
                continue;
            }
            if (locationVal.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Location is required.");
                continue;
            }

            // Parse optional numeric fields
            Double latVal = null;
            Double lngVal = null;
            Double areaVal = null;

            if (!latField.getText().trim().isEmpty()) {
                try {
                    latVal = Double.parseDouble(latField.getText().trim());
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "Latitude must be a valid number.");
                    continue;
                }
            }
            if (!lngField.getText().trim().isEmpty()) {
                try {
                    lngVal = Double.parseDouble(lngField.getText().trim());
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "Longitude must be a valid number.");
                    continue;
                }
            }
            if (!areaField.getText().trim().isEmpty()) {
                try {
                    areaVal = Double.parseDouble(areaField.getText().trim());
                    if (areaVal < 0) {
                        showAlert(Alert.AlertType.ERROR, "Validation Error", "Area must be a positive number.");
                        continue;
                    }
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "Area must be a valid number.");
                    continue;
                }
            }

            long ownerUserId = currentUser.getId();
            if (isAdmin && finalUserIdField != null && !finalUserIdField.getText().trim().isEmpty()) {
                try {
                    ownerUserId = Long.parseLong(finalUserIdField.getText().trim());
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "User ID must be a valid number.");
                    continue;
                }
            }

            try {
                if (farm == null) {
                    Farm newFarm = new Farm();
                    newFarm.setUserId(ownerUserId);
                    newFarm.setName(nameVal);
                    newFarm.setLocation(locationVal);
                    newFarm.setLatitude(latVal);
                    newFarm.setLongitude(lngVal);
                    newFarm.setArea(areaVal);
                    newFarm.setFarmType(typeField.getText().trim().isEmpty() ? null : typeField.getText().trim());
                    newFarm.setDescription(descField.getText().trim().isEmpty() ? null : descField.getText().trim());
                    newFarm.setStatus(isAdmin && finalStatusCombo != null ? finalStatusCombo.getValue() : "pending");
                    farmService.add(newFarm);
                } else {
                    farm.setName(nameVal);
                    farm.setLocation(locationVal);
                    farm.setLatitude(latVal);
                    farm.setLongitude(lngVal);
                    farm.setArea(areaVal);
                    farm.setFarmType(typeField.getText().trim().isEmpty() ? null : typeField.getText().trim());
                    farm.setDescription(descField.getText().trim().isEmpty() ? null : descField.getText().trim());
                    if (isAdmin) {
                        if (finalStatusCombo != null) farm.setStatus(finalStatusCombo.getValue());
                        if (finalUserIdField != null && !finalUserIdField.getText().trim().isEmpty()) {
                            farm.setUserId(ownerUserId);
                        }
                    }
                    farmService.update(farm);
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save farm: " + e.getMessage());
            }
            break;
        }
    }

    // ==================== Map Picker Dialog ====================

    private void showMapPickerDialog(TextField latField, TextField lngField) {
        Dialog<ButtonType> mapDialog = new Dialog<>();
        mapDialog.setTitle("Pick Location on Map");
        mapDialog.setHeaderText("Click on the map to select coordinates");
        mapDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        mapDialog.setResizable(true);

        WebView webView = new WebView();
        webView.setPrefSize(700, 500);
        WebEngine engine = webView.getEngine();

        // Initial coordinates: use existing values or default to Tunisia center
        String initLat = latField.getText().trim().isEmpty() ? "36.8" : latField.getText().trim();
        String initLng = lngField.getText().trim().isEmpty() ? "10.18" : lngField.getText().trim();

        String html = "<!DOCTYPE html><html><head>" +
            "<meta charset='utf-8'/>" +
            "<meta name='viewport' content='width=device-width, initial-scale=1.0'/>" +
            "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>" +
            "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
            "<style>html,body,#map{margin:0;padding:0;width:100%;height:100%;}</style>" +
            "</head><body>" +
            "<div id='map'></div>" +
            "<script>" +
            "var map = L.map('map').setView([" + initLat + "," + initLng + "], 8);" +
            "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{" +
            "  attribution:'OpenStreetMap'," +
            "  maxZoom:19" +
            "}).addTo(map);" +
            "var marker = null;" +
            "var selectedLat = " + initLat + ";" +
            "var selectedLng = " + initLng + ";" +
            // If there were existing coordinates, place initial marker
            (latField.getText().trim().isEmpty() ? "" :
                "marker = L.marker([" + initLat + "," + initLng + "]).addTo(map);") +
            "map.on('click', function(e) {" +
            "  selectedLat = e.latlng.lat;" +
            "  selectedLng = e.latlng.lng;" +
            "  if(marker) map.removeLayer(marker);" +
            "  marker = L.marker([selectedLat, selectedLng]).addTo(map);" +
            "  document.title = selectedLat.toFixed(8) + ',' + selectedLng.toFixed(8);" +
            "});" +
            "</script></body></html>";

        engine.loadContent(html);

        mapDialog.getDialogPane().setContent(webView);
        mapDialog.getDialogPane().setPrefSize(720, 550);

        Optional<ButtonType> result = mapDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String title = (String) engine.executeScript("document.title");
            if (title != null && title.contains(",")) {
                String[] parts = title.split(",");
                latField.setText(parts[0]);
                lngField.setText(parts[1]);
            }
        }
    }

    // ==================== Fields View ====================

    private void showFieldsView(Farm farm) {
        contentArea.getChildren().clear();

        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_LEFT);

        // Header with back button
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("action-button-block");
        backBtn.setOnAction(e -> {
            // Determine if admin or farmer to go back to the correct view
            String roleName = "";
            try {
                Role role = roleService.getById(currentUser.getRoleId());
                if (role != null) roleName = role.getName();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            if ("admin".equalsIgnoreCase(roleName)) {
                showFarmsView();
            } else {
                showMyFarmsView();
            }
        });

        Label title = new Label("Fields of \"" + farm.getName() + "\"");
        title.getStyleClass().add("content-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("action-button-add");
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("action-button-edit");
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("action-button-delete");

        header.getChildren().addAll(backBtn, title, spacer, addBtn, editBtn, deleteBtn);

        // Table
        TableView<Field> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Field, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(60);

        TableColumn<Field, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Field, Double> areaCol = new TableColumn<>("Area");
        areaCol.setCellValueFactory(new PropertyValueFactory<>("area"));
        areaCol.setMaxWidth(80);

        TableColumn<Field, String> soilCol = new TableColumn<>("Soil Type");
        soilCol.setCellValueFactory(new PropertyValueFactory<>("soilType"));

        TableColumn<Field, String> cropCol = new TableColumn<>("Crop Type");
        cropCol.setCellValueFactory(new PropertyValueFactory<>("cropType"));

        TableColumn<Field, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(idCol, nameCol, areaCol, soilCol, cropCol, statusCol);

        // Load data
        ObservableList<Field> masterData = FXCollections.observableArrayList();
        try {
            masterData.addAll(fieldService.getByFarmId(farm.getId()));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load fields: " + e.getMessage());
        }
        table.setItems(masterData);

        // Reload helper
        Runnable reloadTable = () -> {
            masterData.clear();
            try {
                masterData.addAll(fieldService.getByFarmId(farm.getId()));
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load fields: " + ex.getMessage());
            }
        };

        // Button actions
        addBtn.setOnAction(e -> {
            showFieldFormDialog(null, farm.getId());
            reloadTable.run();
        });

        editBtn.setOnAction(e -> {
            Field selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a field to edit.");
                return;
            }
            showFieldFormDialog(selected, farm.getId());
            reloadTable.run();
        });

        deleteBtn.setOnAction(e -> {
            Field selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a field to delete.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete field \"" + selected.getName() + "\"?");
            confirm.setHeaderText("Confirm Deletion");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    fieldService.delete(selected.getId());
                    reloadTable.run();
                } catch (SQLException ex) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete field: " + ex.getMessage());
                }
            }
        });

        container.getChildren().addAll(header, table);
        contentArea.getChildren().add(container);
    }

    // ==================== Field Form Dialog ====================

    private void showFieldFormDialog(Field field, long farmId) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(field == null ? "Add Field" : "Edit Field");
        dialog.setHeaderText(field == null ? "Create a new field" : "Edit field: " + field.getName());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField(field != null ? field.getName() : "");
        nameField.setPromptText("Field name");
        TextField areaField = new TextField(field != null ? String.valueOf(field.getArea()) : "");
        areaField.setPromptText("Area (required, positive number)");
        TextField soilField = new TextField(field != null ? (field.getSoilType() != null ? field.getSoilType() : "") : "");
        soilField.setPromptText("Soil type (optional)");
        TextField cropField = new TextField(field != null ? (field.getCropType() != null ? field.getCropType() : "") : "");
        cropField.setPromptText("Crop type (optional)");

        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList("active", "inactive", "fallow"));
        statusCombo.setValue(field != null ? field.getStatus() : "active");

        int row = 0;
        grid.add(new Label("Name:"), 0, row);
        grid.add(nameField, 1, row++);
        grid.add(new Label("Area:"), 0, row);
        grid.add(areaField, 1, row++);
        grid.add(new Label("Soil Type:"), 0, row);
        grid.add(soilField, 1, row++);
        grid.add(new Label("Crop Type:"), 0, row);
        grid.add(cropField, 1, row++);
        grid.add(new Label("Status:"), 0, row);
        grid.add(statusCombo, 1, row);

        dialog.getDialogPane().setContent(grid);

        // Validation loop
        while (true) {
            Optional<ButtonType> result = dialog.showAndWait();
            if (!result.isPresent() || result.get() != ButtonType.OK) {
                break;
            }

            String nameVal = nameField.getText().trim();
            if (nameVal.length() < 2) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Field name must be at least 2 characters.");
                continue;
            }

            double areaVal;
            try {
                areaVal = Double.parseDouble(areaField.getText().trim());
                if (areaVal <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "Area must be a positive number.");
                    continue;
                }
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Area must be a valid positive number.");
                continue;
            }

            try {
                if (field == null) {
                    Field newField = new Field();
                    newField.setFarmId(farmId);
                    newField.setName(nameVal);
                    newField.setArea(areaVal);
                    newField.setSoilType(soilField.getText().trim().isEmpty() ? null : soilField.getText().trim());
                    newField.setCropType(cropField.getText().trim().isEmpty() ? null : cropField.getText().trim());
                    newField.setStatus(statusCombo.getValue());
                    fieldService.add(newField);
                } else {
                    field.setName(nameVal);
                    field.setArea(areaVal);
                    field.setSoilType(soilField.getText().trim().isEmpty() ? null : soilField.getText().trim());
                    field.setCropType(cropField.getText().trim().isEmpty() ? null : cropField.getText().trim());
                    field.setStatus(statusCombo.getValue());
                    fieldService.update(field);
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save field: " + e.getMessage());
            }
            break;
        }
    }

    // ==================== Products Management (Admin) ====================

    private void showProductsView() {
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
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load products: " + e.getMessage());
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
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load products: " + ex.getMessage());
            }
        };

        approveBtn.setOnAction(e -> {
            Product selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to approve.");
                return;
            }
            try {
                selected.setStatus("approved");
                selected.setApprovedAt(java.time.LocalDateTime.now());
                selected.setApprovedBy(currentUser.getId());
                productService.update(selected);
                reloadTable.run();
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to approve product: " + ex.getMessage());
            }
        });

        rejectBtn.setOnAction(e -> {
            Product selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to reject.");
                return;
            }
            try {
                selected.setStatus("rejected");
                selected.setApprovedAt(null);
                selected.setApprovedBy(null);
                productService.update(selected);
                reloadTable.run();
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to reject product: " + ex.getMessage());
            }
        });

        deleteBtn.setOnAction(e -> {
            Product selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to delete.");
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
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete product: " + ex.getMessage());
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

    // ==================== All Orders (Admin) ====================

    private void showAllOrdersView() {
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
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load orders: " + e.getMessage());
        }
        table.setItems(masterData);

        Runnable reloadTable = () -> {
            masterData.clear();
            try {
                masterData.addAll(orderService.getAll());
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load orders: " + ex.getMessage());
            }
        };

        updateStatusBtn.setOnAction(e -> {
            Order selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an order.");
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
            try {
                orderService.updateStatus(order.getId(), statusCombo.getValue());
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update order: " + e.getMessage());
            }
        }
    }

    // ==================== My Products (Farmer) ====================

    private void showMyProductsView() {
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
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load products: " + e.getMessage());
        }
        table.setItems(masterData);

        Runnable reloadTable = () -> {
            masterData.clear();
            try {
                masterData.addAll(productService.getByUserId(currentUser.getId()));
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load products: " + ex.getMessage());
            }
        };

        addBtn.setOnAction(e -> {
            showProductFormDialog(null);
            reloadTable.run();
        });

        editBtn.setOnAction(e -> {
            Product selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to edit.");
                return;
            }
            showProductFormDialog(selected);
            reloadTable.run();
        });

        deleteBtn.setOnAction(e -> {
            Product selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to delete.");
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
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete product: " + ex.getMessage());
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
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Product name must be at least 2 characters.");
                continue;
            }

            double priceVal;
            int qtyVal;
            try {
                priceVal = Double.parseDouble(priceField.getText().trim());
                if (priceVal < 0) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "Price must be positive.");
                    continue;
                }
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Price must be a valid number.");
                continue;
            }

            try {
                qtyVal = Integer.parseInt(qtyField.getText().trim());
                if (qtyVal < 0) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "Quantity must be positive.");
                    continue;
                }
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Quantity must be a valid integer.");
                continue;
            }

            String unitVal = unitField.getText().trim();
            if (unitVal.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Unit is required.");
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
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save product: " + e.getMessage());
            }
            break;
        }
    }

    // ==================== Seller Orders (Farmer) ====================

    private void showSellerOrdersView() {
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
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load orders: " + e.getMessage());
        }
        table.setItems(masterData);

        Runnable reloadTable = () -> {
            masterData.clear();
            try {
                masterData.addAll(orderService.getBySellerId(currentUser.getId()));
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load orders: " + ex.getMessage());
            }
        };

        updateStatusBtn.setOnAction(e -> {
            Order selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an order.");
                return;
            }
            showOrderStatusDialog(selected);
            reloadTable.run();
        });

        container.getChildren().addAll(header, table);
        contentArea.getChildren().add(container);
    }

    // ==================== Shop (Customer) ====================

    private void showShopView() {
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
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load products: " + e.getMessage());
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
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to add to cart.");
                return;
            }
            int qty = qtySpinner.getValue();
            if (qty > selected.getQuantity()) {
                showAlert(Alert.AlertType.WARNING, "Insufficient Stock", "Only " + selected.getQuantity() + " available.");
                return;
            }
            try {
                cartService.addToCart(currentUser.getId(), selected.getId(), qty);
                showAlert(Alert.AlertType.INFORMATION, "Added", "Added " + qty + " x " + selected.getName() + " to cart.");
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add to cart: " + ex.getMessage());
            }
        });

        container.getChildren().addAll(header, table, cartControls);
        contentArea.getChildren().add(container);
    }

    // ==================== Cart (Customer) ====================

    private void showCartView() {
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
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load cart: " + ex.getMessage());
            }
        };

        reloadCart.run();
        table.setItems(cartData);

        removeBtn.setOnAction(e -> {
            CartItem selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an item to remove.");
                return;
            }
            try {
                cartService.removeFromCart(currentUser.getId(), selected.getProductId());
                reloadCart.run();
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to remove item: " + ex.getMessage());
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
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to clear cart: " + ex.getMessage());
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
                showAlert(Alert.AlertType.WARNING, "Empty Cart", "Your cart is empty.");
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
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Email is required.");
                continue;
            }
            if (!esprit.farouk.utils.ValidationUtils.isValidEmail(email)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid email address.");
                continue;
            }
            if (phone.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Phone number is required.");
                continue;
            }
            if (!esprit.farouk.utils.ValidationUtils.isValidPhone(phone)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid phone number (8-15 digits).");
                continue;
            }
            if (address.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Address is required.");
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
                    esprit.farouk.utils.EmailUtils.sendOrderConfirmation(email, currentUser.getName(), firstOrderId, grandTotal);
                } catch (javax.mail.MessagingException e) {
                    System.err.println("Failed to send confirmation email: " + e.getMessage());
                    // Don't show error to user, order was still placed successfully
                }

                reloadCart.run();
                showAlert(Alert.AlertType.INFORMATION, "Order Placed",
                    "Your order has been placed successfully!\n\nA confirmation email has been sent to " + email +
                    ".\n\nYour order will be delivered within 3 business days.");
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to place order: " + ex.getMessage());
            }
            break;
        }
    }

    // ==================== Customer Orders ====================

    private void showCustomerOrdersView() {
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
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load orders: " + e.getMessage());
        }
        table.setItems(masterData);

        container.getChildren().addAll(header, table);
        contentArea.getChildren().add(container);
    }

    // ==================== Posts Management (Admin) ====================

    private void showPostsView() {
        contentArea.getChildren().clear();

        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_LEFT);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Post Management");
        title.getStyleClass().add("content-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button publishBtn = new Button("Publish");
        publishBtn.getStyleClass().add("action-button-approve");
        Button unpublishBtn = new Button("Unpublish");
        unpublishBtn.getStyleClass().add("action-button-block");
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("action-button-delete");

        header.getChildren().addAll(title, spacer, publishBtn, unpublishBtn, deleteBtn);

        HBox filterBar = new HBox(10);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search by title...");
        searchField.getStyleClass().add("search-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        ComboBox<String> statusFilter = new ComboBox<>(FXCollections.observableArrayList("All", "Draft", "Published", "Unpublished"));
        statusFilter.setValue("All");
        statusFilter.getStyleClass().add("filter-combo");

        filterBar.getChildren().addAll(searchField, statusFilter);

        TableView<Post> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Post, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(60);

        TableColumn<Post, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Post, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Post, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Post, Integer> viewsCol = new TableColumn<>("Views");
        viewsCol.setCellValueFactory(new PropertyValueFactory<>("views"));
        viewsCol.setMaxWidth(60);

        TableColumn<Post, Long> userCol = new TableColumn<>("Author");
        userCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        userCol.setMaxWidth(80);

        table.getColumns().addAll(idCol, titleCol, categoryCol, statusCol, viewsCol, userCol);

        ObservableList<Post> masterData = FXCollections.observableArrayList();
        try {
            masterData.addAll(postService.getAll());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load posts: " + e.getMessage());
        }

        FilteredList<Post> filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) ->
                filteredData.setPredicate(post -> filterPost(post, newVal, statusFilter.getValue())));
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) ->
                filteredData.setPredicate(post -> filterPost(post, searchField.getText(), newVal)));

        SortedList<Post> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        Runnable reloadTable = () -> {
            masterData.clear();
            try {
                masterData.addAll(postService.getAll());
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load posts: " + ex.getMessage());
            }
        };

        publishBtn.setOnAction(e -> {
            Post selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a post to publish.");
                return;
            }
            try {
                postService.publish(selected.getId());
                reloadTable.run();
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to publish post: " + ex.getMessage());
            }
        });

        unpublishBtn.setOnAction(e -> {
            Post selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a post to unpublish.");
                return;
            }
            try {
                postService.unpublish(selected.getId());
                reloadTable.run();
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to unpublish post: " + ex.getMessage());
            }
        });

        deleteBtn.setOnAction(e -> {
            Post selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a post to delete.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete post \"" + selected.getTitle() + "\"?");
            confirm.setHeaderText("Confirm Deletion");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    postService.delete(selected.getId());
                    reloadTable.run();
                } catch (SQLException ex) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete post: " + ex.getMessage());
                }
            }
        });

        container.getChildren().addAll(header, filterBar, table);
        contentArea.getChildren().add(container);
    }

    private boolean filterPost(Post post, String searchText, String statusValue) {
        boolean matchesSearch = true;
        if (searchText != null && !searchText.trim().isEmpty()) {
            String lower = searchText.trim().toLowerCase();
            matchesSearch = post.getTitle() != null && post.getTitle().toLowerCase().contains(lower);
        }
        boolean matchesStatus = true;
        if (statusValue != null && !"All".equals(statusValue)) {
            matchesStatus = statusValue.toLowerCase().equals(post.getStatus());
        }
        return matchesSearch && matchesStatus;
    }

    // ==================== Comments Management (Admin) ====================

    // ==================== Blog View (Farmer & Customer) ====================

    private void showBlogView() {
        contentArea.getChildren().clear();

        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_LEFT);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Blog");
        title.getStyleClass().add("content-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField searchField = new TextField();
        searchField.setPromptText("Search posts...");
        searchField.getStyleClass().add("search-field");

        Button myPostsBtn = new Button("My Posts");
        myPostsBtn.getStyleClass().add("action-button-edit");
        myPostsBtn.setOnAction(e -> showMyPostsView());

        header.getChildren().addAll(title, spacer, searchField, myPostsBtn);

        TableView<Post> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Post, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Post, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("userName"));

        TableColumn<Post, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Post, Integer> viewsCol = new TableColumn<>("Views");
        viewsCol.setCellValueFactory(new PropertyValueFactory<>("views"));
        viewsCol.setMaxWidth(60);

        table.getColumns().addAll(titleCol, authorCol, categoryCol, viewsCol);

        ObservableList<Post> masterData = FXCollections.observableArrayList();
        try {
            masterData.addAll(postService.getPublished());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load posts: " + e.getMessage());
        }

        FilteredList<Post> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) ->
                filteredData.setPredicate(post -> {
                    if (newVal == null || newVal.trim().isEmpty()) return true;
                    String lower = newVal.toLowerCase();
                    return (post.getTitle() != null && post.getTitle().toLowerCase().contains(lower))
                            || (post.getCategory() != null && post.getCategory().toLowerCase().contains(lower));
                }));

        SortedList<Post> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        // Double-click to open post
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Post selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showPostDetailView(selected);
                }
            }
        });

        container.getChildren().addAll(header, table);
        contentArea.getChildren().add(container);
    }

    // ==================== My Posts View ====================

    private void showMyPostsView() {
        contentArea.getChildren().clear();

        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_LEFT);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("action-button-block");
        backBtn.setOnAction(e -> showBlogView());

        Label title = new Label("My Posts");
        title.getStyleClass().add("content-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("New Post");
        addBtn.getStyleClass().add("action-button-add");
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("action-button-edit");
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("action-button-delete");

        header.getChildren().addAll(backBtn, title, spacer, addBtn, editBtn, deleteBtn);

        TableView<Post> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Post, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(60);

        TableColumn<Post, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Post, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Post, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Post, Integer> viewsCol = new TableColumn<>("Views");
        viewsCol.setCellValueFactory(new PropertyValueFactory<>("views"));
        viewsCol.setMaxWidth(60);

        table.getColumns().addAll(idCol, titleCol, categoryCol, statusCol, viewsCol);

        ObservableList<Post> masterData = FXCollections.observableArrayList();
        try {
            masterData.addAll(postService.getByUserId(currentUser.getId()));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load posts: " + e.getMessage());
        }
        table.setItems(masterData);

        Runnable reloadTable = () -> {
            masterData.clear();
            try {
                masterData.addAll(postService.getByUserId(currentUser.getId()));
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load posts: " + ex.getMessage());
            }
        };

        addBtn.setOnAction(e -> {
            showPostFormDialog(null);
            reloadTable.run();
        });

        editBtn.setOnAction(e -> {
            Post selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a post to edit.");
                return;
            }
            showPostFormDialog(selected);
            reloadTable.run();
        });

        deleteBtn.setOnAction(e -> {
            Post selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a post to delete.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete post \"" + selected.getTitle() + "\"?");
            confirm.setHeaderText("Confirm Deletion");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    postService.delete(selected.getId());
                    reloadTable.run();
                } catch (SQLException ex) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete post: " + ex.getMessage());
                }
            }
        });

        container.getChildren().addAll(header, table);
        contentArea.getChildren().add(container);
    }

    // ==================== Post Form Dialog ====================

    private void showPostFormDialog(Post post) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(post == null ? "New Post" : "Edit Post");
        dialog.setHeaderText(post == null ? "Create a new blog post" : "Edit post: " + post.getTitle());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField titleField = new TextField(post != null ? post.getTitle() : "");
        titleField.setPromptText("Post title");
        titleField.setPrefWidth(400);

        TextArea contentArea = new TextArea(post != null ? post.getContent() : "");
        contentArea.setPromptText("Post content");
        contentArea.setPrefRowCount(8);
        contentArea.setWrapText(true);

        TextField excerptField = new TextField(post != null && post.getExcerpt() != null ? post.getExcerpt() : "");
        excerptField.setPromptText("Short excerpt (optional)");

        ComboBox<String> categoryCombo = new ComboBox<>(FXCollections.observableArrayList(
                "News", "Tips & Tricks", "Farming Guide", "Success Stories", "Events", "Other"));
        categoryCombo.setPromptText("Select category");
        if (post != null && post.getCategory() != null) {
            categoryCombo.setValue(post.getCategory());
        }

        int row = 0;
        grid.add(new Label("Title:"), 0, row);
        grid.add(titleField, 1, row++);
        grid.add(new Label("Content:"), 0, row);
        grid.add(contentArea, 1, row++);
        grid.add(new Label("Excerpt:"), 0, row);
        grid.add(excerptField, 1, row++);
        grid.add(new Label("Category:"), 0, row);
        grid.add(categoryCombo, 1, row);

        dialog.getDialogPane().setContent(grid);

        while (true) {
            Optional<ButtonType> result = dialog.showAndWait();
            if (!result.isPresent() || result.get() != ButtonType.OK) {
                break;
            }

            String titleVal = titleField.getText().trim();
            String contentVal = contentArea.getText().trim();

            if (titleVal.length() < 3) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Title must be at least 3 characters.");
                continue;
            }
            if (contentVal.length() < 10) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Content must be at least 10 characters.");
                continue;
            }

            try {
                if (post == null) {
                    Post newPost = new Post();
                    newPost.setUserId(currentUser.getId());
                    newPost.setTitle(titleVal);
                    newPost.setSlug(generateSlug(titleVal));
                    newPost.setContent(contentVal);
                    newPost.setExcerpt(excerptField.getText().trim().isEmpty() ? null : excerptField.getText().trim());
                    newPost.setCategory(categoryCombo.getValue());
                    newPost.setStatus("draft");
                    postService.add(newPost);
                } else {
                    post.setTitle(titleVal);
                    post.setSlug(generateSlug(titleVal));
                    post.setContent(contentVal);
                    post.setExcerpt(excerptField.getText().trim().isEmpty() ? null : excerptField.getText().trim());
                    post.setCategory(categoryCombo.getValue());
                    postService.update(post);
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save post: " + e.getMessage());
            }
            break;
        }
    }

    private String generateSlug(String title) {
        if (title == null) return "";
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    // ==================== Post Detail View ====================

    private void showPostDetailView(Post post) {
        try {
            postService.incrementViews(post.getId());
        } catch (SQLException e) {
            // Ignore view increment errors
        }

        contentArea.getChildren().clear();

        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_LEFT);
        container.setPadding(new Insets(10));

        Button backBtn = new Button("Back to Blog");
        backBtn.getStyleClass().add("action-button-block");
        backBtn.setOnAction(e -> showBlogView());

        Label titleLabel = new Label(post.getTitle());
        titleLabel.getStyleClass().add("content-title");
        titleLabel.setWrapText(true);

        Label metaLabel = new Label("By: " + (post.getUserName() != null ? post.getUserName() : "Unknown") +
                " | Category: " + (post.getCategory() != null ? post.getCategory() : "N/A") +
                " | Views: " + post.getViews());
        metaLabel.setStyle("-fx-text-fill: #757575;");

        Label contentLabel = new Label(post.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 14;");

        Separator sep = new Separator();

        Label commentsTitle = new Label("Comments");
        commentsTitle.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        VBox commentsBox = new VBox(10);
        boolean isPostOwner = post.getUserId() == currentUser.getId();
        boolean adminCheck = false;
        try {
            Role role = roleService.getById(currentUser.getRoleId());
            if (role != null && "admin".equalsIgnoreCase(role.getName())) {
                adminCheck = true;
            }
        } catch (SQLException e) {
            // Ignore role check errors
        }
        final boolean isAdmin = adminCheck;
        final boolean canDeleteAll = isAdmin || isPostOwner;
        try {
            List<Comment> comments = commentService.getByPostId(post.getId());
            if (comments.isEmpty()) {
                commentsBox.getChildren().add(new Label("No comments yet."));
            } else {
                for (Comment c : comments) {
                    VBox commentCard = new VBox(5);
                    commentCard.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10; -fx-background-radius: 5;");

                    HBox commentHeader = new HBox(10);
                    commentHeader.setAlignment(Pos.CENTER_LEFT);
                    Label userName = new Label(c.getUserName() != null ? c.getUserName() : "User " + c.getUserId());
                    userName.setStyle("-fx-font-weight: bold;");
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    commentHeader.getChildren().addAll(userName, spacer);

                    // Admin, post owner, or comment author can delete
                    if (canDeleteAll || c.getUserId() == currentUser.getId()) {
                        Button deleteBtn = new Button("Delete");
                        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10;");
                        deleteBtn.setOnAction(ev -> {
                            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this comment?");
                            confirm.setHeaderText("Confirm Deletion");
                            Optional<ButtonType> result = confirm.showAndWait();
                            if (result.isPresent() && result.get() == ButtonType.OK) {
                                try {
                                    commentService.delete(c.getId());
                                    showPostDetailView(post); // Refresh
                                } catch (SQLException ex) {
                                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete comment: " + ex.getMessage());
                                }
                            }
                        });
                        commentHeader.getChildren().add(deleteBtn);
                    }

                    // Admin can block/unblock the comment author
                    if (isAdmin && c.getUserId() != currentUser.getId()) {
                        Button blockBtn = new Button();
                        try {
                            User commentUser = userService.getById(c.getUserId());
                            boolean isBlocked = commentUser != null && "blocked".equalsIgnoreCase(commentUser.getStatus());
                            if (isBlocked) {
                                blockBtn.setText("Blocked");
                                blockBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 10;");
                            } else {
                                blockBtn.setText("Block User");
                                blockBtn.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-size: 10;");
                            }
                        } catch (SQLException ex) {
                            blockBtn.setText("Block User");
                            blockBtn.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-size: 10;");
                        }
                        blockBtn.setOnAction(ev -> {
                            try {
                                User userToToggle = userService.getById(c.getUserId());
                                if (userToToggle != null) {
                                    boolean currentlyBlocked = "blocked".equalsIgnoreCase(userToToggle.getStatus());
                                    String action = currentlyBlocked ? "Unblock" : "Block";
                                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                                        action + " user '" + userToToggle.getName() + "'?");
                                    confirm.setHeaderText("Confirm " + action);
                                    Optional<ButtonType> result = confirm.showAndWait();
                                    if (result.isPresent() && result.get() == ButtonType.OK) {
                                        if (currentlyBlocked) {
                                            userToToggle.setStatus("active");
                                            userService.update(userToToggle);
                                            showAlert(Alert.AlertType.INFORMATION, "User Unblocked",
                                                "User '" + userToToggle.getName() + "' has been unblocked.");
                                        } else {
                                            userToToggle.setStatus("blocked");
                                            userService.update(userToToggle);
                                            showAlert(Alert.AlertType.INFORMATION, "User Blocked",
                                                "User '" + userToToggle.getName() + "' has been blocked.");
                                        }
                                        showPostDetailView(post); // Refresh
                                    }
                                }
                            } catch (SQLException ex) {
                                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update user: " + ex.getMessage());
                            }
                        });
                        commentHeader.getChildren().add(blockBtn);
                    }

                    Label commentText = new Label(c.getContent());
                    commentText.setWrapText(true);
                    commentCard.getChildren().addAll(commentHeader, commentText);
                    commentsBox.getChildren().add(commentCard);
                }
            }
        } catch (SQLException e) {
            commentsBox.getChildren().add(new Label("Failed to load comments."));
        }

        // Add comment section
        Label addCommentLabel = new Label("Add a Comment");
        addCommentLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        TextArea commentInput = new TextArea();
        commentInput.setPromptText("Write your comment...");
        commentInput.setPrefRowCount(3);
        commentInput.setWrapText(true);

        Button submitCommentBtn = new Button("Submit Comment");
        submitCommentBtn.getStyleClass().add("action-button-add");
        submitCommentBtn.setOnAction(e -> {
            String commentText = commentInput.getText().trim();
            if (commentText.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Empty Comment", "Please write something.");
                return;
            }
            try {
                Comment newComment = new Comment();
                newComment.setPostId(post.getId());
                newComment.setUserId(currentUser.getId());
                newComment.setContent(commentText);
                newComment.setStatus("approved");
                commentService.add(newComment);
                showAlert(Alert.AlertType.INFORMATION, "Comment Submitted", "Your comment has been posted.");
                commentInput.clear();
                showPostDetailView(post); // Refresh to show new comment
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit comment: " + ex.getMessage());
            }
        });

        ScrollPane scrollPane = new ScrollPane();
        VBox scrollContent = new VBox(15, titleLabel, metaLabel, contentLabel, sep, commentsTitle, commentsBox,
                addCommentLabel, commentInput, submitCommentBtn);
        scrollContent.setPadding(new Insets(10));
        scrollPane.setContent(scrollContent);
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Translate button
        ComboBox<String> langBox = new ComboBox<>();
        langBox.getItems().addAll("French", "Arabic", "Spanish", "German");
        langBox.setValue("French");

        Button translateBtn = new Button("Translate");
        translateBtn.getStyleClass().add("action-button-edit");
        translateBtn.setOnAction(ev -> {
            String selectedLang = langBox.getValue();
            String langPair = switch (selectedLang) {
                case "Arabic"  -> "en|ar";
                case "Spanish" -> "en|es";
                case "German"  -> "en|de";
                default        -> "en|fr";
            };
            translateBtn.setDisable(true);
            translateBtn.setText("Translating...");
            String origTitle   = post.getTitle();
            String origContent = post.getContent();
            new Thread(() -> {
                try {
                    String tTitle   = TranslationUtils.translate(origTitle, langPair);
                    String tContent = TranslationUtils.translate(origContent, langPair);
                    Platform.runLater(() -> {
                        translateBtn.setDisable(false);
                        translateBtn.setText("Translate");
                        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
                        dialog.setTitle("Translation — " + selectedLang);
                        dialog.setHeaderText(tTitle);
                        TextArea ta = new TextArea(tContent);
                        ta.setWrapText(true);
                        ta.setEditable(false);
                        ta.setPrefSize(520, 300);
                        dialog.getDialogPane().setContent(ta);
                        dialog.showAndWait();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        translateBtn.setDisable(false);
                        translateBtn.setText("Translate");
                        showAlert(Alert.AlertType.ERROR, "Translation Failed",
                                "Could not translate the post:\n" + ex.getMessage());
                    });
                }
            }).start();
        });

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);
        HBox topBar = new HBox(10, backBtn, topSpacer, langBox, translateBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);

        container.getChildren().addAll(topBar, scrollPane);
        contentArea.getChildren().add(container);
    }

    // ==================== Events View (Admin) ====================

    private void showEventsView() {
        contentArea.getChildren().clear();

        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_LEFT);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Event Management");
        title.getStyleClass().add("content-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("action-button-add");
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("action-button-edit");
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("action-button-delete");
        Button cancelBtn = new Button("Cancel Event");
        cancelBtn.getStyleClass().add("action-button-reject");

        header.getChildren().addAll(title, spacer, addBtn, editBtn, deleteBtn, cancelBtn);

        HBox filterBar = new HBox(10);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search by title or location...");
        searchField.getStyleClass().add("search-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        ComboBox<String> statusFilter = new ComboBox<>(FXCollections.observableArrayList("All", "Upcoming", "Ongoing", "Completed", "Cancelled"));
        statusFilter.setValue("All");
        statusFilter.getStyleClass().add("filter-combo");

        filterBar.getChildren().addAll(searchField, statusFilter);

        TableView<Event> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Event, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(50);

        TableColumn<Event, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Event, String> organizerCol = new TableColumn<>("Organizer");
        organizerCol.setCellValueFactory(new PropertyValueFactory<>("userName"));

        TableColumn<Event, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));

        TableColumn<Event, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Event, Integer> participantsCol = new TableColumn<>("Participants");
        participantsCol.setCellValueFactory(new PropertyValueFactory<>("participantCount"));
        participantsCol.setMaxWidth(80);

        TableColumn<Event, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(idCol, titleCol, organizerCol, locationCol, categoryCol, participantsCol, statusCol);

        ObservableList<Event> masterData = FXCollections.observableArrayList();
        try {
            masterData.addAll(eventService.getAll());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load events: " + e.getMessage());
        }

        FilteredList<Event> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) ->
                filteredData.setPredicate(event -> filterEvent(event, newVal, statusFilter.getValue())));
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) ->
                filteredData.setPredicate(event -> filterEvent(event, searchField.getText(), newVal)));

        SortedList<Event> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        // Double-click to view participants
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Event selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showEventParticipantsView(selected);
                }
            }
        });

        Runnable reloadTable = () -> {
            masterData.clear();
            try {
                masterData.addAll(eventService.getAll());
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load events: " + ex.getMessage());
            }
        };

        addBtn.setOnAction(e -> {
            showEventFormDialog(null);
            reloadTable.run();
        });

        editBtn.setOnAction(e -> {
            Event selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an event to edit.");
                return;
            }
            showEventFormDialog(selected);
            reloadTable.run();
        });

        deleteBtn.setOnAction(e -> {
            Event selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an event to delete.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete event '" + selected.getTitle() + "'?");
            confirm.setHeaderText("Confirm Deletion");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    eventService.delete(selected.getId());
                    reloadTable.run();
                } catch (SQLException ex) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete event: " + ex.getMessage());
                }
            }
        });

        cancelBtn.setOnAction(e -> {
            Event selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an event to cancel.");
                return;
            }
            if ("cancelled".equals(selected.getStatus())) {
                showAlert(Alert.AlertType.WARNING, "Already Cancelled", "This event is already cancelled.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Cancel event '" + selected.getTitle() + "'?");
            confirm.setHeaderText("Confirm Cancellation");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    eventService.cancel(selected.getId());
                    reloadTable.run();
                } catch (SQLException ex) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to cancel event: " + ex.getMessage());
                }
            }
        });

        container.getChildren().addAll(header, filterBar, table);
        contentArea.getChildren().add(container);
    }

    private boolean filterEvent(Event event, String searchText, String statusFilter) {
        boolean matchesSearch = true;
        boolean matchesStatus = true;

        if (searchText != null && !searchText.trim().isEmpty()) {
            String lower = searchText.toLowerCase();
            matchesSearch = (event.getTitle() != null && event.getTitle().toLowerCase().contains(lower))
                    || (event.getLocation() != null && event.getLocation().toLowerCase().contains(lower));
        }

        if (statusFilter != null && !"All".equals(statusFilter)) {
            matchesStatus = statusFilter.toLowerCase().equals(event.getStatus());
        }

        return matchesSearch && matchesStatus;
    }

    private void showEventFormDialog(Event event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(event == null ? "Add Event" : "Edit Event");
        dialog.setHeaderText(event == null ? "Create a new event" : "Edit event details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        titleField.setPromptText("Event title");
        if (event != null) titleField.setText(event.getTitle());

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Event description");
        descriptionArea.setPrefRowCount(3);
        if (event != null && event.getDescription() != null) descriptionArea.setText(event.getDescription());

        TextField locationField = new TextField();
        locationField.setPromptText("Location");
        if (event != null) locationField.setText(event.getLocation());

        DatePicker eventDatePicker = new DatePicker();
        eventDatePicker.setPromptText("Event date");
        if (event != null && event.getEventDate() != null) eventDatePicker.setValue(event.getEventDate().toLocalDate());

        TextField eventTimeField = new TextField();
        eventTimeField.setPromptText("Time (HH:mm)");
        if (event != null && event.getEventDate() != null) {
            eventTimeField.setText(event.getEventDate().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        }

        TextField capacityField = new TextField();
        capacityField.setPromptText("Capacity (optional)");
        if (event != null && event.getCapacity() != null) capacityField.setText(String.valueOf(event.getCapacity()));

        ComboBox<String> categoryCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Workshop", "Seminar", "Fair", "Training", "Networking", "Exhibition", "Conference", "Other"));
        categoryCombo.setPromptText("Select category");
        if (event != null && event.getCategory() != null) categoryCombo.setValue(event.getCategory());

        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList(
                "upcoming", "ongoing", "completed", "cancelled"));
        statusCombo.setValue(event != null ? event.getStatus() : "upcoming");

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionArea, 1, 1);
        grid.add(new Label("Location:"), 0, 2);
        grid.add(locationField, 1, 2);
        grid.add(new Label("Date:"), 0, 3);
        grid.add(eventDatePicker, 1, 3);
        grid.add(new Label("Time:"), 0, 4);
        grid.add(eventTimeField, 1, 4);
        grid.add(new Label("Capacity:"), 0, 5);
        grid.add(capacityField, 1, 5);
        grid.add(new Label("Category:"), 0, 6);
        grid.add(categoryCombo, 1, 6);
        grid.add(new Label("Status:"), 0, 7);
        grid.add(statusCombo, 1, 7);

        dialog.getDialogPane().setContent(grid);

        while (true) {
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == saveButtonType) {
                String titleText = titleField.getText().trim();
                String descText = descriptionArea.getText().trim();
                String locationText = locationField.getText().trim();
                LocalDate dateVal = eventDatePicker.getValue();
                String timeText = eventTimeField.getText().trim();

                if (titleText.isEmpty() || titleText.length() < 3) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "Title must be at least 3 characters.");
                    continue;
                }
                if (locationText.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "Location is required.");
                    continue;
                }
                if (dateVal == null) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "Event date is required.");
                    continue;
                }

                java.time.LocalTime timeVal = java.time.LocalTime.of(9, 0);
                if (!timeText.isEmpty()) {
                    try {
                        timeVal = java.time.LocalTime.parse(timeText, DateTimeFormatter.ofPattern("HH:mm"));
                    } catch (Exception ex) {
                        showAlert(Alert.AlertType.ERROR, "Validation Error", "Invalid time format. Use HH:mm.");
                        continue;
                    }
                }

                Integer capacity = null;
                if (!capacityField.getText().trim().isEmpty()) {
                    try {
                        capacity = Integer.parseInt(capacityField.getText().trim());
                        if (capacity <= 0) throw new NumberFormatException();
                    } catch (NumberFormatException ex) {
                        showAlert(Alert.AlertType.ERROR, "Validation Error", "Capacity must be a positive number.");
                        continue;
                    }
                }

                try {
                    if (event == null) {
                        Event newEvent = new Event();
                        newEvent.setUserId(currentUser.getId());
                        newEvent.setTitle(titleText);
                        newEvent.setSlug(titleText.toLowerCase().replaceAll("[^a-z0-9]+", "-"));
                        newEvent.setDescription(descText);
                        newEvent.setLocation(locationText);
                        newEvent.setEventDate(java.time.LocalDateTime.of(dateVal, timeVal));
                        newEvent.setCapacity(capacity);
                        newEvent.setCategory(categoryCombo.getValue());
                        newEvent.setStatus(statusCombo.getValue());
                        eventService.add(newEvent);
                    } else {
                        event.setTitle(titleText);
                        event.setSlug(titleText.toLowerCase().replaceAll("[^a-z0-9]+", "-"));
                        event.setDescription(descText);
                        event.setLocation(locationText);
                        event.setEventDate(java.time.LocalDateTime.of(dateVal, timeVal));
                        event.setCapacity(capacity);
                        event.setCategory(categoryCombo.getValue());
                        event.setStatus(statusCombo.getValue());
                        eventService.update(event);
                    }
                    break;
                } catch (SQLException ex) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", ex.getMessage());
                }
            } else {
                break;
            }
        }
    }

    private void showEventParticipantsView(Event event) {
        contentArea.getChildren().clear();

        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_LEFT);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("action-button-block");
        backBtn.setOnAction(e -> showEventsView());

        Label title = new Label("Participants: " + event.getTitle());
        title.getStyleClass().add("content-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button markAttendedBtn = new Button("Mark Attended");
        markAttendedBtn.getStyleClass().add("action-button-approve");
        Button removeBtn = new Button("Remove");
        removeBtn.getStyleClass().add("action-button-delete");

        header.getChildren().addAll(backBtn, title, spacer, markAttendedBtn, removeBtn);

        TableView<Participation> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Participation, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(50);

        TableColumn<Participation, String> userCol = new TableColumn<>("Participant");
        userCol.setCellValueFactory(new PropertyValueFactory<>("userName"));

        TableColumn<Participation, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Participation, Boolean> attendedCol = new TableColumn<>("Attended");
        attendedCol.setCellValueFactory(new PropertyValueFactory<>("attended"));

        table.getColumns().addAll(idCol, userCol, statusCol, attendedCol);

        ObservableList<Participation> data = FXCollections.observableArrayList();
        try {
            data.addAll(participationService.getByEventId(event.getId()));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load participants: " + e.getMessage());
        }
        table.setItems(data);

        Runnable reloadTable = () -> {
            data.clear();
            try {
                data.addAll(participationService.getByEventId(event.getId()));
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load participants: " + ex.getMessage());
            }
        };

        markAttendedBtn.setOnAction(e -> {
            Participation selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a participant.");
                return;
            }
            try {
                participationService.markAttended(selected.getId());
                reloadTable.run();
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to mark attended: " + ex.getMessage());
            }
        });

        removeBtn.setOnAction(e -> {
            Participation selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a participant to remove.");
                return;
            }
            try {
                participationService.delete(selected.getId());
                reloadTable.run();
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to remove participant: " + ex.getMessage());
            }
        });

        container.getChildren().addAll(header, table);
        contentArea.getChildren().add(container);
    }

    // ==================== Browse Events View (Farmer & Customer) ====================

    private void showBrowseEventsView() {
        contentArea.getChildren().clear();

        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_LEFT);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Upcoming Events");
        title.getStyleClass().add("content-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField searchField = new TextField();
        searchField.setPromptText("Search events...");
        searchField.getStyleClass().add("search-field");

        Button myParticipationsBtn = new Button("My Registrations");
        myParticipationsBtn.getStyleClass().add("action-button-edit");
        myParticipationsBtn.setOnAction(e -> showMyParticipationsView());

        header.getChildren().addAll(title, spacer, searchField, myParticipationsBtn);

        TableView<Event> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Event, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Event, String> organizerCol = new TableColumn<>("Organizer");
        organizerCol.setCellValueFactory(new PropertyValueFactory<>("userName"));

        TableColumn<Event, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));

        TableColumn<Event, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Event, Integer> participantsCol = new TableColumn<>("Participants");
        participantsCol.setCellValueFactory(new PropertyValueFactory<>("participantCount"));
        participantsCol.setMaxWidth(80);

        table.getColumns().addAll(titleCol, organizerCol, locationCol, categoryCol, participantsCol);

        ObservableList<Event> masterData = FXCollections.observableArrayList();
        try {
            masterData.addAll(eventService.getUpcoming());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load events: " + e.getMessage());
        }

        FilteredList<Event> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) ->
                filteredData.setPredicate(event -> {
                    if (newVal == null || newVal.trim().isEmpty()) return true;
                    String lower = newVal.toLowerCase();
                    return (event.getTitle() != null && event.getTitle().toLowerCase().contains(lower))
                            || (event.getLocation() != null && event.getLocation().toLowerCase().contains(lower));
                }));

        SortedList<Event> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        // Double-click to view details and register
        table.setOnMouseClicked(ev -> {
            if (ev.getClickCount() == 2) {
                Event selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showEventDetailView(selected);
                }
            }
        });

        container.getChildren().addAll(header, table);
        contentArea.getChildren().add(container);
    }

    private void showEventDetailView(Event event) {
        contentArea.getChildren().clear();

        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_LEFT);
        container.setPadding(new Insets(10));

        Button backBtn = new Button("Back to Events");
        backBtn.getStyleClass().add("action-button-block");
        backBtn.setOnAction(e -> showBrowseEventsView());

        Label titleLabel = new Label(event.getTitle());
        titleLabel.getStyleClass().add("content-title");
        titleLabel.setWrapText(true);

        Label metaLabel = new Label("By: " + (event.getUserName() != null ? event.getUserName() : "Unknown") +
                " | Location: " + event.getLocation() +
                " | Category: " + (event.getCategory() != null ? event.getCategory() : "N/A"));
        metaLabel.setStyle("-fx-text-fill: #757575;");

        Label dateLabel = new Label("Date: " + (event.getEventDate() != null ?
                event.getEventDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")) : "TBD"));
        dateLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        Label capacityLabel = new Label("Capacity: " + (event.getCapacity() != null ?
                event.getParticipantCount() + " / " + event.getCapacity() : event.getParticipantCount() + " registered"));

        Label descriptionLabel = new Label(event.getDescription() != null ? event.getDescription() : "No description.");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 14;");

        // Check if already registered
        Button registerBtn = new Button("Register for Event");
        registerBtn.getStyleClass().add("action-button-add");

        try {
            Participation existing = participationService.getByEventAndUser(event.getId(), currentUser.getId());
            if (existing != null) {
                if ("cancelled".equals(existing.getStatus())) {
                    registerBtn.setText("Register Again");
                } else {
                    registerBtn.setText("Already Registered");
                    registerBtn.setDisable(true);
                }
            }
        } catch (SQLException e) {
            // Ignore
        }

        // Check capacity
        if (event.getCapacity() != null && event.getParticipantCount() >= event.getCapacity()) {
            registerBtn.setText("Event Full");
            registerBtn.setDisable(true);
        }

        registerBtn.setOnAction(e -> {
            try {
                Participation existing = participationService.getByEventAndUser(event.getId(), currentUser.getId());
                if (existing != null) {
                    if (!"cancelled".equals(existing.getStatus())) {
                        showAlert(Alert.AlertType.WARNING, "Already Registered", "You are already registered for this event.");
                        return;
                    }
                    // Re-register: update existing record
                    existing.setStatus("confirmed");
                    existing.setCancelledAt(null);
                    existing.setCancelledReason(null);
                    participationService.update(existing);
                } else {
                    // New registration
                    Participation p = new Participation();
                    p.setEventId(event.getId());
                    p.setUserId(currentUser.getId());
                    p.setStatus("confirmed");
                    participationService.add(p);
                }
                // Send ticket email with QR code in background thread
                Participation registered = participationService.getByEventAndUser(event.getId(), currentUser.getId());
                final long participationId = registered != null ? registered.getId() : 0;
                final Event eventSnapshot = event;
                new Thread(() -> {
                    try {
                        EmailUtils.sendEventTicket(currentUser.getEmail(), currentUser.getName(), eventSnapshot, participationId);
                    } catch (Exception ex) {
                        System.err.println("Failed to send ticket email: " + ex.getMessage());
                    }
                }).start();
                showAlert(Alert.AlertType.INFORMATION, "Registered!", "You have successfully registered for this event!\nA ticket with QR code has been sent to " + currentUser.getEmail());
                showEventDetailView(eventService.getById(event.getId())); // Refresh
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to register: " + ex.getMessage());
            }
        });

        VBox content = new VBox(15, titleLabel, metaLabel, dateLabel, capacityLabel, new Separator(), descriptionLabel, registerBtn);
        content.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        container.getChildren().addAll(backBtn, scrollPane);
        contentArea.getChildren().add(container);
    }

    private void showMyParticipationsView() {
        contentArea.getChildren().clear();

        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_LEFT);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("action-button-block");
        backBtn.setOnAction(e -> showBrowseEventsView());

        Label title = new Label("My Event Registrations");
        title.getStyleClass().add("content-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button cancelBtn = new Button("Cancel Registration");
        cancelBtn.getStyleClass().add("action-button-reject");

        header.getChildren().addAll(backBtn, title, spacer, cancelBtn);

        TableView<Participation> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Participation, String> eventCol = new TableColumn<>("Event");
        eventCol.setCellValueFactory(new PropertyValueFactory<>("eventTitle"));

        TableColumn<Participation, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Participation, Boolean> attendedCol = new TableColumn<>("Attended");
        attendedCol.setCellValueFactory(new PropertyValueFactory<>("attended"));

        table.getColumns().addAll(eventCol, statusCol, attendedCol);

        ObservableList<Participation> data = FXCollections.observableArrayList();
        try {
            data.addAll(participationService.getByUserId(currentUser.getId()));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load registrations: " + e.getMessage());
        }
        table.setItems(data);

        Runnable reloadTable = () -> {
            data.clear();
            try {
                data.addAll(participationService.getByUserId(currentUser.getId()));
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load registrations: " + ex.getMessage());
            }
        };

        cancelBtn.setOnAction(e -> {
            Participation selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a registration to cancel.");
                return;
            }
            if ("cancelled".equals(selected.getStatus())) {
                showAlert(Alert.AlertType.WARNING, "Already Cancelled", "This registration is already cancelled.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Cancel registration for '" + selected.getEventTitle() + "'?");
            confirm.setHeaderText("Confirm Cancellation");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    participationService.cancel(selected.getId(), "User cancelled");
                    reloadTable.run();
                } catch (SQLException ex) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to cancel: " + ex.getMessage());
                }
            }
        });

        container.getChildren().addAll(header, table);
        contentArea.getChildren().add(container);
    }

    @FXML
    private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) contentArea.getScene().getWindow();
            Scene scene = new Scene(root, 800, 600);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadUsers(TableView<User> table) {
        try {
            table.setItems(FXCollections.observableArrayList(userService.getAll()));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load users: " + e.getMessage());
        }
    }

    private void loadRoles(TableView<Role> table) {
        try {
            table.setItems(FXCollections.observableArrayList(roleService.getAll()));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load roles: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
