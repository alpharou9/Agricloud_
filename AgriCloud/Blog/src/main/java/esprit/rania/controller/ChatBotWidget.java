package esprit.rania.controller;

import esprit.rania.api.ChatBotService;
import esprit.rania.models.Post;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

import java.util.List;

/**
 * Floating chatbot bubble widget.
 * Add it to any StackPane (root layout) to show a floating chat button.
 *
 * Usage in BlogPostController:
 *   ChatBotWidget chatBot = new ChatBotWidget();
 *   rootStackPane.getChildren().add(chatBot.getWidget());
 */
public class ChatBotWidget {

    private final StackPane widget;
    private VBox chatWindow;
    private VBox messagesBox;
    private TextField inputField;
    private boolean isOpen = false;
    private String blogContext = "No posts available yet.";

    public ChatBotWidget() {
        widget = new StackPane();
        widget.setPickOnBounds(false); // allows clicks to pass through transparent areas
        StackPane.setAlignment(widget, Pos.BOTTOM_RIGHT);
        buildWidget();
    }

    private void buildWidget() {
        // ── Chat Window ──────────────────────────────────────────────
        chatWindow = new VBox();
        chatWindow.setPickOnBounds(false);
        chatWindow.setSpacing(0);
        chatWindow.setPrefWidth(340);
        chatWindow.setPrefHeight(460);
        chatWindow.setMaxWidth(340);
        chatWindow.setMaxHeight(460);
        chatWindow.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 24, 0, 0, 6);"
        );
        chatWindow.setVisible(false);
        chatWindow.setManaged(false);

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 16, 14, 16));
        header.setStyle(
                "-fx-background-color: linear-gradient(135deg, #16a34a, #059669);" +
                        "-fx-background-radius: 16 16 0 0;"
        );

        Label botIcon = new Label("🤖");
        botIcon.setStyle("-fx-font-size: 20;");

        VBox headerText = new VBox(2);
        Label botName = new Label("AgriCloud Assistant");
        botName.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: white;");
        Label botStatus = new Label("Ask me about blog posts!");
        botStatus.setStyle("-fx-font-size: 11; -fx-text-fill: rgba(255,255,255,0.85);");
        headerText.getChildren().addAll(botName, botStatus);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 2 6;"
        );
        closeBtn.setOnAction(e -> toggleChat());

        header.getChildren().addAll(botIcon, headerText, headerSpacer, closeBtn);

        // Messages area
        messagesBox = new VBox(10);
        messagesBox.setPadding(new Insets(14));

        ScrollPane scrollPane = new ScrollPane(messagesBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: white; -fx-background-color: white; -fx-border-width: 0;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Add welcome message
        addBotMessage("👋 Hi! I'm your AgriCloud blog assistant. Ask me to summarize a post, explain a topic, or compare posts!");

        // Input area
        HBox inputRow = new HBox(8);
        inputRow.setAlignment(Pos.CENTER);
        inputRow.setPadding(new Insets(10, 12, 12, 12));
        inputRow.setStyle(
                "-fx-background-color: #f8fafc;" +
                        "-fx-background-radius: 0 0 16 16;" +
                        "-fx-border-color: #e5e7eb;" +
                        "-fx-border-width: 1 0 0 0;"
        );

        inputField = new TextField();
        inputField.setPromptText("Ask about a blog post...");
        inputField.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #d1fae5;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 20;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 8 14;" +
                        "-fx-font-size: 13;"
        );
        HBox.setHgrow(inputField, Priority.ALWAYS);
        inputField.setOnAction(e -> sendMessage());

        Button sendBtn = new Button("➤");
        sendBtn.setStyle(
                "-fx-background-color: #16a34a;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 8 14;" +
                        "-fx-cursor: hand;"
        );
        sendBtn.setOnAction(e -> sendMessage());

        inputRow.getChildren().addAll(inputField, sendBtn);
        chatWindow.getChildren().addAll(header, scrollPane, inputRow);

        // ── Floating Bubble Button ───────────────────────────────────
        StackPane bubble = new StackPane();
        bubble.setPrefSize(56, 56);
        bubble.setMinSize(56, 56);
        bubble.setMaxSize(56, 56);
        bubble.setStyle(
                "-fx-background-color: linear-gradient(135deg, #16a34a, #059669);" +
                        "-fx-background-radius: 28;" +
                        "-fx-effect: dropshadow(gaussian, rgba(22,163,74,0.4), 16, 0, 0, 4);" +
                        "-fx-cursor: hand;"
        );

        Label bubbleIcon = new Label("💬");
        bubbleIcon.setStyle("-fx-font-size: 22;");
        bubble.getChildren().add(bubbleIcon);
        bubble.setOnMouseClicked(e -> toggleChat());

        bubble.setOnMouseEntered(e -> bubble.setStyle(
                "-fx-background-color: linear-gradient(135deg, #15803d, #047857);" +
                        "-fx-background-radius: 28;" +
                        "-fx-effect: dropshadow(gaussian, rgba(22,163,74,0.5), 20, 0, 0, 6);" +
                        "-fx-cursor: hand;"
        ));
        bubble.setOnMouseExited(e -> bubble.setStyle(
                "-fx-background-color: linear-gradient(135deg, #16a34a, #059669);" +
                        "-fx-background-radius: 28;" +
                        "-fx-effect: dropshadow(gaussian, rgba(22,163,74,0.4), 16, 0, 0, 4);" +
                        "-fx-cursor: hand;"
        ));

        // ── Layout: stack chat window above bubble ───────────────────
        VBox layout = new VBox(10);
        layout.setPickOnBounds(false);
        layout.setAlignment(Pos.BOTTOM_RIGHT);
        layout.setPadding(new Insets(0, 24, 24, 0));
        layout.getChildren().addAll(chatWindow, bubble);

        widget.getChildren().add(layout);
        StackPane.setAlignment(layout, Pos.BOTTOM_RIGHT);
    }

    private void toggleChat() {
        isOpen = !isOpen;
        chatWindow.setVisible(isOpen);
        chatWindow.setManaged(isOpen);
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        inputField.clear();
        addUserMessage(text);

        // Show typing indicator
        Label typing = new Label("🤖 Thinking...");
        typing.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12; -fx-font-style: italic;");
        messagesBox.getChildren().add(typing);
        scrollToBottom();

        // Call API in background
        String context = blogContext;
        new Thread(() -> {
            String response = ChatBotService.chat(text, context);
            Platform.runLater(() -> {
                messagesBox.getChildren().remove(typing);
                addBotMessage(response);
                scrollToBottom();
            });
        }).start();
    }

    private void addUserMessage(String text) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_RIGHT);

        Label msg = new Label(text);
        msg.setWrapText(true);
        msg.setMaxWidth(240);
        msg.setStyle(
                "-fx-background-color: #16a34a;" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 10 14;" +
                        "-fx-background-radius: 16 16 4 16;" +
                        "-fx-font-size: 13;"
        );
        row.getChildren().add(msg);
        messagesBox.getChildren().add(row);
        scrollToBottom();
    }

    private void addBotMessage(String text) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        // Small bot avatar
        StackPane avatar = new StackPane();
        avatar.setPrefSize(28, 28);
        avatar.setMinSize(28, 28);
        Circle circle = new Circle(14);
        circle.setStyle("-fx-fill: #f0fdf4;");
        Label icon = new Label("🤖");
        icon.setStyle("-fx-font-size: 12;");
        avatar.getChildren().addAll(circle, icon);
        avatar.setStyle("-fx-border-color: #d1fae5; -fx-border-radius: 14; -fx-border-width: 1;");

        Label msg = new Label(text);
        msg.setWrapText(true);
        msg.setMaxWidth(240);
        msg.setStyle(
                "-fx-background-color: #f0fdf4;" +
                        "-fx-text-fill: #1e293b;" +
                        "-fx-padding: 10 14;" +
                        "-fx-background-radius: 16 16 16 4;" +
                        "-fx-font-size: 13;" +
                        "-fx-border-color: #d1fae5;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 16 16 16 4;"
        );

        row.getChildren().addAll(avatar, msg);
        messagesBox.getChildren().add(row);
        scrollToBottom();
    }

    private void scrollToBottom() {
        Platform.runLater(() -> {
            ScrollPane sp = (ScrollPane) chatWindow.getChildren().get(1);
            sp.setVvalue(1.0);
        });
    }

    /**
     * Update the bot's knowledge with current blog posts
     * Call this every time posts are loaded/refreshed
     */
    public void updateBlogContext(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            blogContext = "No blog posts available yet.";
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (Post post : posts) {
            sb.append("---\n");
            sb.append("Title: ").append(post.getTitle()).append("\n");
            sb.append("Author: ").append(post.getAuthor()).append("\n");
            sb.append("Content: ").append(post.getContent()).append("\n\n");
        }
        blogContext = sb.toString();
        System.out.println("🤖 ChatBot updated with " + posts.size() + " posts");
    }

    public StackPane getWidget() {
        return widget;
    }
}