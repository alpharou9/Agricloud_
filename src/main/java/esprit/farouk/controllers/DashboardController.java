package esprit.farouk.controllers;

import esprit.farouk.models.Role;
import esprit.farouk.models.User;
import esprit.farouk.services.CartService;
import esprit.farouk.services.ChatbotService;
import esprit.farouk.services.EventService;
import esprit.farouk.services.FarmService;
import esprit.farouk.services.OrderService;
import esprit.farouk.services.PostService;
import esprit.farouk.services.ProductService;
import esprit.farouk.services.RoleService;
import esprit.farouk.services.UserService;
import esprit.farouk.utils.UIUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();
    private final CartService cartService = new CartService();
    private final PostService postService = new PostService();
    private final EventService eventService = new EventService();

    private User currentUser;
    private String currentRoleName = "";
    private Stage chatStage = null;

    // Sub-controllers
    private UserManagementController userManagementController;
    private FarmController farmController;
    private MarketController marketController;
    private BlogController blogController;
    private EventController eventController;

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

        // Instantiate sub-controllers
        userManagementController = new UserManagementController(contentArea, currentUser, userNameLabel);
        farmController = new FarmController(contentArea, currentUser);
        marketController = new MarketController(contentArea, currentUser);
        blogController = new BlogController(contentArea, currentUser);
        eventController = new EventController(contentArea, currentUser);

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
        this.currentRoleName = roleName;

        if ("admin".equalsIgnoreCase(roleName)) {
            Button homeBtn = new Button("Home");
            homeBtn.getStyleClass().add("sidebar-button");
            homeBtn.setMaxWidth(Double.MAX_VALUE);
            homeBtn.setOnAction(e -> { setActiveSidebarButton(homeBtn); showHomeView(); });

            Button usersBtn = new Button("Users");
            usersBtn.getStyleClass().add("sidebar-button");
            usersBtn.setMaxWidth(Double.MAX_VALUE);
            usersBtn.setOnAction(e -> { setActiveSidebarButton(usersBtn); userManagementController.showUsersView(); });

            Button rolesBtn = new Button("Roles");
            rolesBtn.getStyleClass().add("sidebar-button");
            rolesBtn.setMaxWidth(Double.MAX_VALUE);
            rolesBtn.setOnAction(e -> { setActiveSidebarButton(rolesBtn); userManagementController.showRolesView(); });

            Button farmsBtn = new Button("Farms");
            farmsBtn.getStyleClass().add("sidebar-button");
            farmsBtn.setMaxWidth(Double.MAX_VALUE);
            farmsBtn.setOnAction(e -> { setActiveSidebarButton(farmsBtn); farmController.showFarmsView(); });

            Button productsBtn = new Button("Products");
            productsBtn.getStyleClass().add("sidebar-button");
            productsBtn.setMaxWidth(Double.MAX_VALUE);
            productsBtn.setOnAction(e -> { setActiveSidebarButton(productsBtn); marketController.showProductsView(); });

            Button ordersBtn = new Button("Orders");
            ordersBtn.getStyleClass().add("sidebar-button");
            ordersBtn.setMaxWidth(Double.MAX_VALUE);
            ordersBtn.setOnAction(e -> { setActiveSidebarButton(ordersBtn); marketController.showAllOrdersView(); });

            Button postsBtn = new Button("Posts");
            postsBtn.getStyleClass().add("sidebar-button");
            postsBtn.setMaxWidth(Double.MAX_VALUE);
            postsBtn.setOnAction(e -> { setActiveSidebarButton(postsBtn); blogController.showPostsView(); });

            Button blogBtn = new Button("Blog");
            blogBtn.getStyleClass().add("sidebar-button");
            blogBtn.setMaxWidth(Double.MAX_VALUE);
            blogBtn.setOnAction(e -> { setActiveSidebarButton(blogBtn); blogController.showBlogView(); });

            Button eventsBtn = new Button("Events");
            eventsBtn.getStyleClass().add("sidebar-button");
            eventsBtn.setMaxWidth(Double.MAX_VALUE);
            eventsBtn.setOnAction(e -> { setActiveSidebarButton(eventsBtn); eventController.showEventsView(); });

            Button statsBtn = new Button("Statistics");
            statsBtn.getStyleClass().add("sidebar-button");
            statsBtn.setMaxWidth(Double.MAX_VALUE);
            statsBtn.setOnAction(e -> { setActiveSidebarButton(statsBtn); userManagementController.showStatisticsView(); });

            Button profileBtn = new Button("Profile");
            profileBtn.getStyleClass().add("sidebar-button");
            profileBtn.setMaxWidth(Double.MAX_VALUE);
            profileBtn.setOnAction(e -> { setActiveSidebarButton(profileBtn); userManagementController.showProfileView(); });

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
            profileBtn.setOnAction(e -> { setActiveSidebarButton(profileBtn); userManagementController.showProfileView(); });

            Button myFarmsBtn = new Button("My Farms");
            myFarmsBtn.getStyleClass().add("sidebar-button");
            myFarmsBtn.setMaxWidth(Double.MAX_VALUE);
            myFarmsBtn.setOnAction(e -> { setActiveSidebarButton(myFarmsBtn); farmController.showMyFarmsView(); });

            Button myProductsBtn = new Button("My Products");
            myProductsBtn.getStyleClass().add("sidebar-button");
            myProductsBtn.setMaxWidth(Double.MAX_VALUE);
            myProductsBtn.setOnAction(e -> { setActiveSidebarButton(myProductsBtn); marketController.showMyProductsView(); });

            Button farmerOrdersBtn = new Button("My Orders");
            farmerOrdersBtn.getStyleClass().add("sidebar-button");
            farmerOrdersBtn.setMaxWidth(Double.MAX_VALUE);
            farmerOrdersBtn.setOnAction(e -> { setActiveSidebarButton(farmerOrdersBtn); marketController.showSellerOrdersView(); });

            Button farmerBlogBtn = new Button("Blog");
            farmerBlogBtn.getStyleClass().add("sidebar-button");
            farmerBlogBtn.setMaxWidth(Double.MAX_VALUE);
            farmerBlogBtn.setOnAction(e -> { setActiveSidebarButton(farmerBlogBtn); blogController.showBlogView(); });

            Button farmerEventsBtn = new Button("Events");
            farmerEventsBtn.getStyleClass().add("sidebar-button");
            farmerEventsBtn.setMaxWidth(Double.MAX_VALUE);
            farmerEventsBtn.setOnAction(e -> { setActiveSidebarButton(farmerEventsBtn); eventController.showBrowseEventsView(); });

            sidebarMenu.getChildren().addAll(homeBtn, profileBtn, myFarmsBtn, myProductsBtn, farmerOrdersBtn, farmerBlogBtn, farmerEventsBtn);
            setActiveSidebarButton(homeBtn);
            showHomeView();
        } else if ("guest".equalsIgnoreCase(roleName)) {
            Button homeBtn = new Button("Home");
            homeBtn.getStyleClass().add("sidebar-button");
            homeBtn.setMaxWidth(Double.MAX_VALUE);
            homeBtn.setOnAction(e -> { setActiveSidebarButton(homeBtn); showHomeView(); });

            Button shopBtn = new Button("Shop");
            shopBtn.getStyleClass().add("sidebar-button");
            shopBtn.setMaxWidth(Double.MAX_VALUE);
            shopBtn.setOnAction(e -> { setActiveSidebarButton(shopBtn); marketController.showShopView(); });

            Button cartBtn = new Button("Cart");
            cartBtn.getStyleClass().add("sidebar-button");
            cartBtn.setMaxWidth(Double.MAX_VALUE);
            cartBtn.setOnAction(e -> { setActiveSidebarButton(cartBtn); marketController.showCartView(); });

            Button guestOrdersBtn = new Button("My Orders");
            guestOrdersBtn.getStyleClass().add("sidebar-button");
            guestOrdersBtn.setMaxWidth(Double.MAX_VALUE);
            guestOrdersBtn.setOnAction(e -> { setActiveSidebarButton(guestOrdersBtn); marketController.showCustomerOrdersView(); });

            Button guestBlogBtn = new Button("Blog");
            guestBlogBtn.getStyleClass().add("sidebar-button");
            guestBlogBtn.setMaxWidth(Double.MAX_VALUE);
            guestBlogBtn.setOnAction(e -> { setActiveSidebarButton(guestBlogBtn); blogController.showBlogView(); });

            Button guestEventsBtn = new Button("Events");
            guestEventsBtn.getStyleClass().add("sidebar-button");
            guestEventsBtn.setMaxWidth(Double.MAX_VALUE);
            guestEventsBtn.setOnAction(e -> { setActiveSidebarButton(guestEventsBtn); eventController.showBrowseEventsView(); });

            sidebarMenu.getChildren().addAll(homeBtn, shopBtn, cartBtn, guestOrdersBtn, guestBlogBtn, guestEventsBtn);
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
            profileBtn.setOnAction(e -> { setActiveSidebarButton(profileBtn); userManagementController.showProfileView(); });

            Button shopBtn = new Button("Shop");
            shopBtn.getStyleClass().add("sidebar-button");
            shopBtn.setMaxWidth(Double.MAX_VALUE);
            shopBtn.setOnAction(e -> { setActiveSidebarButton(shopBtn); marketController.showShopView(); });

            Button cartBtn = new Button("Cart");
            cartBtn.getStyleClass().add("sidebar-button");
            cartBtn.setMaxWidth(Double.MAX_VALUE);
            cartBtn.setOnAction(e -> { setActiveSidebarButton(cartBtn); marketController.showCartView(); });

            Button customerOrdersBtn = new Button("My Orders");
            customerOrdersBtn.getStyleClass().add("sidebar-button");
            customerOrdersBtn.setMaxWidth(Double.MAX_VALUE);
            customerOrdersBtn.setOnAction(e -> { setActiveSidebarButton(customerOrdersBtn); marketController.showCustomerOrdersView(); });

            Button customerBlogBtn = new Button("Blog");
            customerBlogBtn.getStyleClass().add("sidebar-button");
            customerBlogBtn.setMaxWidth(Double.MAX_VALUE);
            customerBlogBtn.setOnAction(e -> { setActiveSidebarButton(customerBlogBtn); blogController.showBlogView(); });

            Button customerEventsBtn = new Button("Events");
            customerEventsBtn.getStyleClass().add("sidebar-button");
            customerEventsBtn.setMaxWidth(Double.MAX_VALUE);
            customerEventsBtn.setOnAction(e -> { setActiveSidebarButton(customerEventsBtn); eventController.showBrowseEventsView(); });

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

        String roleName = this.currentRoleName;

        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
        String roleDisplay = roleName.isEmpty() ? "User" : roleName.substring(0, 1).toUpperCase() + roleName.substring(1);
        Label subtitle = new Label(dateStr + "  |  " + roleDisplay);
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
                    UIUtils.createStatCard("Total Users", String.valueOf(totalUsers), "stat-card-total"),
                    UIUtils.createStatCard("Total Farms", String.valueOf(totalFarms), "stat-card-farms"),
                    UIUtils.createStatCard("Total Products", String.valueOf(totalProducts), "stat-card-products"),
                    UIUtils.createStatCard("Total Orders", String.valueOf(totalOrders), "stat-card-orders"),
                    UIUtils.createStatCard("Total Posts", String.valueOf(totalPosts), "stat-card-posts"),
                    UIUtils.createStatCard("Total Events", String.valueOf(totalEvents), "stat-card-events")
                );
            } else if ("farmer".equalsIgnoreCase(roleName)) {
                int myFarms = farmService.getByUserId(currentUser.getId()).size();
                int myProducts = productService.getByUserId(currentUser.getId()).size();
                long pendingOrders = orderService.getBySellerId(currentUser.getId()).stream()
                        .filter(o -> "pending".equals(o.getStatus())).count();
                int upcomingEvents = eventService.getUpcoming().size();

                cards.getChildren().addAll(
                    UIUtils.createStatCard("My Farms", String.valueOf(myFarms), "stat-card-farms"),
                    UIUtils.createStatCard("My Products", String.valueOf(myProducts), "stat-card-products"),
                    UIUtils.createStatCard("Pending Orders", String.valueOf(pendingOrders), "stat-card-orders"),
                    UIUtils.createStatCard("Upcoming Events", String.valueOf(upcomingEvents), "stat-card-events")
                );
            } else {
                // Customer
                int myOrders = orderService.getByCustomerId(currentUser.getId()).size();
                int cartItems = cartService.getCartItems(currentUser.getId()).size();
                int publishedPosts = postService.getPublished().size();
                int upcomingEvents = eventService.getUpcoming().size();

                cards.getChildren().addAll(
                    UIUtils.createStatCard("My Orders", String.valueOf(myOrders), "stat-card-orders"),
                    UIUtils.createStatCard("Cart Items", String.valueOf(cartItems), "stat-card-products"),
                    UIUtils.createStatCard("Blog Posts", String.valueOf(publishedPosts), "stat-card-posts"),
                    UIUtils.createStatCard("Upcoming Events", String.valueOf(upcomingEvents), "stat-card-events")
                );
            }
        } catch (SQLException e) {
            UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load stats: " + e.getMessage());
        }

        welcomeCard.getChildren().add(cards);
        container.getChildren().add(welcomeCard);

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        contentArea.getChildren().add(scroll);
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

    @FXML
    private void handleOpenChatbot() {
        if (chatStage != null && chatStage.isShowing()) {
            chatStage.toFront();
            return;
        }
        chatStage = buildChatWindow();
        chatStage.show();
    }

    private Stage buildChatWindow() {
        Stage stage = new Stage();
        stage.setTitle("AgriBot \u2014 AI Farm Assistant");
        stage.initModality(Modality.NONE);
        stage.setMinWidth(400);
        stage.setMinHeight(500);

        // Messages area
        VBox messagesBox = new VBox(12);
        messagesBox.setPadding(new Insets(15));

        ScrollPane scrollPane = new ScrollPane(messagesBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Auto-scroll when new messages appear
        messagesBox.heightProperty().addListener((obs, oldVal, newVal) ->
                scrollPane.setVvalue(1.0));

        // Welcome message
        addBotBubble(messagesBox, "Hello! I'm AgriBot \uD83C\uDF31 your AI farming assistant.\n\nAsk me anything about crops, soil, diseases, pricing, or any farming topic!");

        // Input bar
        TextField inputField = new TextField();
        inputField.setPromptText("Ask about crops, diseases, pricing...");
        inputField.getStyleClass().add("chatbot-input");
        HBox.setHgrow(inputField, Priority.ALWAYS);

        Button sendBtn = new Button("Send");
        sendBtn.getStyleClass().add("chatbot-send-btn");

        HBox inputBar = new HBox(10, inputField, sendBtn);
        inputBar.setPadding(new Insets(10, 15, 15, 15));
        inputBar.setAlignment(Pos.CENTER);

        VBox root = new VBox(scrollPane, inputBar);
        root.getStyleClass().add("chatbot-window");

        // Conversation history kept in memory for this session
        List<Map<String, String>> history = new java.util.ArrayList<>();
        ChatbotService chatbotService = new ChatbotService();

        Runnable sendMessage = () -> {
            String text = inputField.getText().trim();
            if (text.isEmpty()) return;

            inputField.clear();
            inputField.setDisable(true);
            sendBtn.setDisable(true);

            addUserBubble(messagesBox, text);

            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", text);
            history.add(userMsg);

            HBox typingBubble = addBotBubble(messagesBox, "AgriBot is thinking...");

            new Thread(() -> {
                try {
                    String reply = chatbotService.sendMessage(history);

                    Map<String, String> assistantMsg = new HashMap<>();
                    assistantMsg.put("role", "assistant");
                    assistantMsg.put("content", reply);
                    history.add(assistantMsg);

                    Platform.runLater(() -> {
                        messagesBox.getChildren().remove(typingBubble);
                        addBotBubble(messagesBox, reply);
                        inputField.setDisable(false);
                        sendBtn.setDisable(false);
                        inputField.requestFocus();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        messagesBox.getChildren().remove(typingBubble);
                        addBotBubble(messagesBox, "Sorry, I couldn't connect. " + ex.getMessage());
                        inputField.setDisable(false);
                        sendBtn.setDisable(false);
                    });
                }
            }).start();
        };

        sendBtn.setOnAction(e -> sendMessage.run());
        inputField.setOnAction(e -> sendMessage.run());

        Scene scene = new Scene(root, 430, 580);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        stage.setScene(scene);
        return stage;
    }

    private HBox addBotBubble(VBox container, String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(290);
        label.getStyleClass().add("chat-bubble-bot");

        HBox row = new HBox(label);
        row.setAlignment(Pos.CENTER_LEFT);
        container.getChildren().add(row);
        return row;
    }

    private void addUserBubble(VBox container, String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(290);
        label.getStyleClass().add("chat-bubble-user");

        HBox row = new HBox(label);
        row.setAlignment(Pos.CENTER_RIGHT);
        container.getChildren().add(row);
    }
}
