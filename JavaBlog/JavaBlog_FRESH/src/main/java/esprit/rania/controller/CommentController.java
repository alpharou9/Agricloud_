package esprit.rania.controller;

import esprit.rania.models.Comment;
import esprit.rania.models.Post;
import esprit.rania.services.CommentService;
import esprit.rania.services.PostService;
import esprit.rania.utilities.AlertHelper;
import esprit.rania.utilities.DateFormatter;
import esprit.rania.utilities.Validator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class CommentController implements Initializable {

    // ── Feed view ────────────────────────────────────────────────────────────
    @FXML private ScrollPane commentsScrollPane;
    @FXML private VBox commentsContainer;
    @FXML private ComboBox<Post> postComboBox;
    @FXML private Label commentCountLabel;
    @FXML private HBox filterBanner;
    @FXML private Label filterBannerLabel;

    // ── Comment form ─────────────────────────────────────────────────────────
    @FXML private VBox commentFormPanel;
    @FXML private Label commentFormTitleLabel;
    @FXML private ComboBox<Post> commentPostCombo;
    @FXML private TextField commentAuthorField;
    @FXML private TextArea commentContentArea;
    @FXML private Button commentSubmitButton;

    private CommentService commentService;
    private PostService postService;
    private boolean listenToCombo = true;
    
    // State for edit mode
    private Comment editingComment = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        commentService = new CommentService();
        postService = new PostService();
        setupComboBox();
        loadPosts();
        loadAllComments();
    }

    // ── ComboBox setup ───────────────────────────────────────────────────────
    private void setupComboBox() {
        // Filter combo
        postComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Post item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle());
            }
        });
        postComboBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Post item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "All posts (show everything)" : item.getTitle());
            }
        });
        postComboBox.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (!listenToCombo) return;
            if (selected == null) {
                hideBanner();
                loadAllComments();
            } else {
                showBanner(selected.getTitle());
                loadCommentsForPost(selected);
            }
        });

        // Form combo
        commentPostCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Post item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle());
            }
        });
        commentPostCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Post item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Select a post..." : item.getTitle());
            }
        });
    }

    private void loadPosts() {
        Post current = postComboBox.getSelectionModel().getSelectedItem();
        listenToCombo = false;
        List<Post> posts = postService.getAllPosts();
        postComboBox.getItems().clear();
        postComboBox.getItems().addAll(posts);
        commentPostCombo.getItems().clear();
        commentPostCombo.getItems().addAll(posts);
        if (current != null) {
            postComboBox.getItems().stream()
                .filter(p -> p.getId() == current.getId())
                .findFirst()
                .ifPresent(p -> postComboBox.getSelectionModel().select(p));
        }
        listenToCombo = true;
    }

    private void loadAllComments() {
        commentsContainer.getChildren().clear();
        List<Post> posts = postService.getAllPosts();
        int total = 0;
        for (Post post : posts) {
            List<Comment> comments = commentService.getCommentsByPostId(post.getId());
            if (!comments.isEmpty()) {
                commentsContainer.getChildren().add(buildGroupHeader(post, comments.size()));
                for (Comment c : comments)
                    commentsContainer.getChildren().add(buildCommentCard(c, post));
                total += comments.size();
            }
        }
        if (total == 0)
            commentsContainer.getChildren().add(buildEmptyState("No comments yet", "Comments will appear here once added."));
        updateCount(total);
    }

    private void loadCommentsForPost(Post post) {
        commentsContainer.getChildren().clear();
        List<Comment> comments = commentService.getCommentsByPostId(post.getId());
        if (comments.isEmpty()) {
            commentsContainer.getChildren().add(buildEmptyState("No comments on this post", "Be the first to comment!"));
        } else {
            commentsContainer.getChildren().add(buildGroupHeader(post, comments.size()));
            for (Comment c : comments)
                commentsContainer.getChildren().add(buildCommentCard(c, post));
        }
        updateCount(comments.size());
    }

    // ── Form handlers ────────────────────────────────────────────────────────
    @FXML
    private void handleShowAddForm() {
        editingComment = null;
        commentFormTitleLabel.setText("✍ Add a New Comment");
        commentSubmitButton.setText("Post Comment");
        commentPostCombo.setDisable(false);
        commentPostCombo.getSelectionModel().clearSelection();
        commentAuthorField.clear();
        commentContentArea.clear();
        commentFormPanel.setVisible(true);
        commentFormPanel.setManaged(true);
    }

    @FXML
    private void handleCancelCommentForm() {
        commentFormPanel.setVisible(false);
        commentFormPanel.setManaged(false);
        editingComment = null;
    }

    @FXML
    private void handleSubmitCommentForm() {
        Post selectedPost = commentPostCombo.getSelectionModel().getSelectedItem();
        String author = commentAuthorField.getText().trim();
        String content = commentContentArea.getText().trim();

        // Validation
        if (editingComment == null && selectedPost == null) {
            AlertHelper.showWarning("No Post Selected", "Please select which post to comment on.");
            return;
        }
        if (!Validator.isNotEmpty(author)) {
            AlertHelper.showError("Validation Error", "Author name is required.");
            return;
        }
        Validator.ValidationResult cv = Validator.validateCommentContent(content);
        if (!cv.isValid()) {
            AlertHelper.showError("Validation Error", cv.getMessage());
            return;
        }

        if (editingComment == null) {
            // Create new
            Comment newComment = new Comment(content, author, selectedPost.getId(), 1);
            if (commentService.createComment(newComment)) {
                AlertHelper.showSuccess("Posted!", "Comment added successfully.");
                handleCancelCommentForm();
                handleRefresh();
            } else {
                AlertHelper.showError("Error", "Could not post comment.");
            }
        } else {
            // Update existing
            editingComment.setContent(content);
            editingComment.setAuthor(author);
            if (commentService.updateComment(editingComment)) {
                AlertHelper.showSuccess("Updated!", "Comment updated successfully.");
                handleCancelCommentForm();
                handleRefresh();
            } else {
                AlertHelper.showError("Error", "Could not update comment.");
            }
        }
    }

    private void openEditForm(Comment comment, Post post) {
        editingComment = comment;
        commentFormTitleLabel.setText("✏ Edit Comment");
        commentSubmitButton.setText("Save Changes");
        
        // Pre-select the post (disabled since we can't change it)
        commentPostCombo.getSelectionModel().select(post);
        commentPostCombo.setDisable(true);
        
        commentAuthorField.setText(comment.getAuthor());
        commentContentArea.setText(comment.getContent());
        
        commentFormPanel.setVisible(true);
        commentFormPanel.setManaged(true);
        commentsScrollPane.setVvalue(0);
    }

    // ── Button handlers ──────────────────────────────────────────────────────
    @FXML
    private void handleRefresh() {
        loadPosts();
        Post selected = postComboBox.getSelectionModel().getSelectedItem();
        if (selected == null) loadAllComments();
        else loadCommentsForPost(selected);
    }

    @FXML
    private void handleClearFilter() {
        listenToCombo = false;
        postComboBox.getSelectionModel().clearSelection();
        listenToCombo = true;
        hideBanner();
        loadAllComments();
    }

    private void showBanner(String postTitle) {
        if (filterBanner != null) {
            filterBannerLabel.setText("Showing comments for:  \"" + postTitle + "\"");
            filterBanner.setVisible(true);
            filterBanner.setManaged(true);
        }
    }

    private void hideBanner() {
        if (filterBanner != null) {
            filterBanner.setVisible(false);
            filterBanner.setManaged(false);
        }
    }

    private void updateCount(int n) {
        if (commentCountLabel != null) commentCountLabel.setText(String.valueOf(n));
    }

    // ── UI builders ──────────────────────────────────────────────────────────
    private HBox buildGroupHeader(Post post, int count) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 0, 4, 0));
        Label icon = new Label("📰");
        icon.setStyle("-fx-font-size: 15;");
        Label title = new Label(post.getTitle());
        title.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #15803d;");
        Label chip = new Label(count + " comment" + (count != 1 ? "s" : ""));
        chip.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; -fx-font-size: 11; " +
                     "-fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 3 10;");
        row.getChildren().addAll(icon, title, chip);
        return row;
    }

    private HBox buildCommentCard(Comment comment, Post post) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.TOP_LEFT);
        row.setPadding(new Insets(2, 0, 4, 16));

        VBox leftCol = new VBox(0);
        leftCol.setAlignment(Pos.TOP_CENTER);
        StackPane avatar = buildAvatar(comment.getAuthor(), 36);
        Region threadLine = new Region();
        threadLine.setPrefWidth(2);
        threadLine.setMinHeight(20);
        threadLine.setStyle("-fx-background-color: #d1fae5; -fx-background-radius: 1;");
        VBox.setVgrow(threadLine, Priority.ALWAYS);
        leftCol.getChildren().addAll(avatar, threadLine);

        VBox bubble = new VBox(6);
        bubble.getStyleClass().add("comment-card");
        HBox.setHgrow(bubble, Priority.ALWAYS);

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        Label authorLabel = new Label(comment.getAuthor());
        authorLabel.getStyleClass().add("comment-author");
        Label dot = new Label("·");
        dot.setStyle("-fx-text-fill: #cbd5e1;");
        Label dateLabel = new Label(DateFormatter.getRelativeTime(comment.getCreatedAt()));
        dateLabel.getStyleClass().add("comment-date");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        // Edit button
        Button editBtn = new Button("✏");
        editBtn.getStyleClass().add("comment-edit-btn");
        editBtn.setTooltip(new Tooltip("Edit comment"));
        editBtn.setOnAction(e -> openEditForm(comment, post));

        // Delete button
        Button deleteBtn = new Button("✕");
        deleteBtn.getStyleClass().add("comment-delete-btn");
        deleteBtn.setTooltip(new Tooltip("Delete comment"));
        deleteBtn.setOnAction(e -> {
            if (AlertHelper.showConfirmation("Delete Comment", "Permanently delete this comment?")) {
                if (commentService.deleteComment(comment.getId())) {
                    handleRefresh();
                } else {
                    AlertHelper.showError("Error", "Could not delete comment.");
                }
            }
        });

        header.getChildren().addAll(authorLabel, dot, dateLabel, sp, editBtn, deleteBtn);

        Label contentLabel = new Label(comment.getContent());
        contentLabel.getStyleClass().add("comment-content");
        contentLabel.setWrapText(true);

        bubble.getChildren().addAll(header, contentLabel);
        row.getChildren().addAll(leftCol, bubble);
        return row;
    }

    private StackPane buildAvatar(String name, int size) {
        StackPane pane = new StackPane();
        int r = size / 2;
        pane.setPrefSize(size, size);
        pane.setMinSize(size, size);
        pane.setMaxSize(size, size);
        Circle circle = new Circle(r);
        String[] colors = {"#16a34a","#0d9488","#2563eb","#7c3aed","#dc2626","#ea580c","#15803d"};
        circle.setStyle("-fx-fill: " + colors[Math.abs(name.charAt(0)) % colors.length] + ";");
        Label lbl = new Label(String.valueOf(name.charAt(0)).toUpperCase());
        lbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: " + (r - 4) + ";");
        pane.getChildren().addAll(circle, lbl);
        return pane;
    }

    private VBox buildEmptyState(String title, String subtitle) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(60));
        Label icon = new Label("💬");
        icon.setStyle("-fx-font-size: 46;");
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #374151;");
        Label subLbl = new Label(subtitle);
        subLbl.setStyle("-fx-font-size: 13; -fx-text-fill: #94a3b8; -fx-text-alignment: center;");
        subLbl.setWrapText(true);
        box.getChildren().addAll(icon, titleLbl, subLbl);
        return box;
    }
}
