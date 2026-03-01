package esprit.rania.controller;

import esprit.rania.models.Post;
import esprit.rania.models.Comment;
import esprit.rania.services.PostService;
import esprit.rania.services.CommentService;
import esprit.rania.utilities.AlertHelper;
import esprit.rania.utilities.DateFormatter;
import esprit.rania.utilities.Validator;
import esprit.rania.utilities.ImageHelper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class BlogPostController implements Initializable {

    @FXML private ScrollPane postsScrollPane;
    @FXML private VBox postsContainer;
    @FXML private TextField searchField;
    @FXML private Label totalPostsLabel;

    // Form fields
    @FXML private VBox formPanel;
    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextArea contentArea;
    @FXML private Label formTitleLabel;
    @FXML private Button submitButton;
    
    // Image upload fields
    @FXML private Label imageFileLabel;
    @FXML private Button removeImageBtn;
    @FXML private VBox imagePreviewContainer;
    @FXML private StackPane imagePreviewBox;
    @FXML private ImageView imagePreview;
    @FXML private Button previewToggleBtn;

    private PostService postService;
    private CommentService commentService;
    private Post editingPost = null;
    private File selectedImageFile = null;
    private boolean previewVisible = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        postService = new PostService();
        commentService = new CommentService();
        loadPosts();

        // Live search
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) loadPosts();
            else searchPosts(newVal.trim());
        });
    }

    // ── Load & Render ──────────────────────────────────────────────────────────

    private void loadPosts() {
        List<Post> posts = postService.getAllPosts();
        System.out.println("🚀 LOADING POSTS - NEW CODE VERSION 2.0");
        System.out.println("📊 Found " + posts.size() + " posts");
        renderPosts(posts);
    }

    private void searchPosts(String keyword) {
        List<Post> posts = postService.searchPostsByTitle(keyword);
        renderPosts(posts);
    }

    private void renderPosts(List<Post> posts) {
        postsContainer.getChildren().clear();

        if (totalPostsLabel != null)
            totalPostsLabel.setText(posts.size() + " post" + (posts.size() != 1 ? "s" : ""));

        if (posts.isEmpty()) {
            VBox empty = new VBox();
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(60));
            empty.setSpacing(12);
            Label icon = new Label("🌿");
            icon.setStyle("-fx-font-size: 48;");
            Label msg = new Label("No posts yet. Be the first to write something!");
            msg.setStyle("-fx-font-size: 15; -fx-text-fill: #64748b;");
            empty.getChildren().addAll(icon, msg);
            postsContainer.getChildren().add(empty);
            return;
        }

        for (Post post : posts) {
            postsContainer.getChildren().add(buildPostCard(post));
        }
    }

    // ── Build Post Card ────────────────────────────────────────────────────────

    private VBox buildPostCard(Post post) {
        VBox card = new VBox();
        card.setSpacing(0);
        card.getStyleClass().add("blog-card");

        // ── Featured Image (if exists) - Full width at top ──
        if (post.getImagePath() != null && !post.getImagePath().isEmpty()) {
            System.out.println("🖼️ Post #" + post.getId() + " has image path: " + post.getImagePath());
            if (ImageHelper.imageExists(post.getImagePath())) {
                System.out.println("✅ Image file exists, loading...");
                try {
                    File imgFile = new File(post.getImagePath());
                    System.out.println("📁 Image file: " + imgFile.getAbsolutePath());
                    Image image = new Image(imgFile.toURI().toString());
                    System.out.println("📏 Image dimensions: " + image.getWidth() + "x" + image.getHeight());
                    
                    StackPane imageContainer = new StackPane();
                    imageContainer.getStyleClass().add("post-featured-image-container");
                    imageContainer.setPrefHeight(280);
                    imageContainer.setMaxHeight(280);
                    
                    ImageView postImage = new ImageView(image);
                    postImage.setPreserveRatio(false);
                    postImage.setFitWidth(900);
                    postImage.setFitHeight(280);
                    postImage.setSmooth(true);
                    postImage.getStyleClass().add("post-featured-image");
                    
                    imageContainer.getChildren().add(postImage);
                    card.getChildren().add(imageContainer);
                    System.out.println("✅ Image added to card!");
                } catch (Exception e) {
                    System.err.println("❌ Could not load post image: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.err.println("❌ Image file does not exist at: " + post.getImagePath());
            }
        } else {
            System.out.println("ℹ️ Post #" + post.getId() + " has no image");
        }

        // ── Card Body ──
        VBox body = new VBox();
        body.setSpacing(14);
        body.setPadding(new Insets(20, 26, 18, 26));

        // Header row: avatar + author + date + actions
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(12);

        // Avatar circle with initials
        StackPane avatar = buildAvatar(post.getAuthor());

        // Author info
        VBox authorInfo = new VBox(2);
        Label authorLabel = new Label(post.getAuthor());
        authorLabel.getStyleClass().add("post-author-name");
        Label dateLabel = new Label("🕐 " + DateFormatter.getRelativeTime(post.getCreatedAt())
                + "  ·  " + DateFormatter.formatDate(post.getCreatedAt()));
        dateLabel.getStyleClass().add("post-date");
        authorInfo.getChildren().addAll(authorLabel, dateLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Action buttons
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button editBtn = new Button("✏ Edit");
        editBtn.getStyleClass().add("card-btn-edit");
        Button deleteBtn = new Button("🗑 Delete");
        deleteBtn.getStyleClass().add("card-btn-delete");
        editBtn.setOnAction(e -> openEditForm(post));
        deleteBtn.setOnAction(e -> handleDeletePost(post));
        actions.getChildren().addAll(editBtn, deleteBtn);

        header.getChildren().addAll(avatar, authorInfo, spacer, actions);

        // Post title - Larger, more prominent
        Label titleLabel = new Label(post.getTitle());
        titleLabel.getStyleClass().add("post-title");
        titleLabel.setWrapText(true);

        // Post content preview (truncated)
        String preview = post.getContent().length() > 280
                ? post.getContent().substring(0, 280) + "…"
                : post.getContent();
        Label contentLabel = new Label(preview);
        contentLabel.getStyleClass().add("post-content-preview");
        contentLabel.setWrapText(true);

        // Separator before footer
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #e5e7eb;");
        VBox.setMargin(sep, new Insets(6, 0, 6, 0));

        // Comment count chip + expand toggle
        int commentCount = commentService.getCommentCountByPostId(post.getId());
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_LEFT);

        Label commentChip = new Label("💬 " + commentCount + " comment" + (commentCount != 1 ? "s" : ""));
        commentChip.getStyleClass().add("comment-chip");

        Label postIdChip = new Label("# " + post.getId());
        postIdChip.getStyleClass().add("post-id-chip");

        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);

        Button toggleComments = new Button("💬 View Comments");
        toggleComments.getStyleClass().add("card-btn-comments");

        footer.getChildren().addAll(commentChip, postIdChip, footerSpacer, toggleComments);

        body.getChildren().addAll(header, titleLabel, contentLabel, sep, footer);

        // ── Comments Section (collapsible) ──
        VBox commentsSection = buildCommentsSection(post);
        commentsSection.setVisible(false);
        commentsSection.setManaged(false);

        toggleComments.setOnAction(e -> {
            boolean nowVisible = !commentsSection.isVisible();
            commentsSection.setVisible(nowVisible);
            commentsSection.setManaged(nowVisible);
            toggleComments.setText(nowVisible ? "▲ Hide Comments" : "💬 View Comments");
        });

        card.getChildren().addAll(body, commentsSection);
        return card;
    }

    // ── Avatar Builder ────────────────────────────────────────────────────────

    private StackPane buildAvatar(String name) {
        StackPane avatar = new StackPane();
        avatar.setPrefSize(42, 42);
        avatar.setMinSize(42, 42);
        avatar.setMaxSize(42, 42);

        Circle circle = new Circle(21);
        // Pick a color based on first letter
        String[] colors = {"#16a34a","#15803d","#0d9488","#2563eb","#7c3aed","#dc2626","#ea580c"};
        int idx = Math.abs(name.charAt(0)) % colors.length;
        circle.setStyle("-fx-fill: " + colors[idx] + ";");

        String initials = name.length() >= 2
                ? String.valueOf(name.charAt(0)).toUpperCase() + String.valueOf(name.charAt(1)).toUpperCase()
                : String.valueOf(name.charAt(0)).toUpperCase();
        Label initialsLabel = new Label(initials);
        initialsLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");

        avatar.getChildren().addAll(circle, initialsLabel);
        return avatar;
    }

    // ── Comments Section ───────────────────────────────────────────────────────

    private VBox buildCommentsSection(Post post) {
        VBox section = new VBox();
        section.setSpacing(0);
        section.getStyleClass().add("comments-section");
        section.setPadding(new Insets(0, 24, 0, 24));

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #d1fae5;");

        VBox inner = new VBox(10);
        inner.setPadding(new Insets(16, 0, 16, 0));

        Label commentsTitle = new Label("Comments");
        commentsTitle.getStyleClass().add("comments-section-title");

        // Load and display comments
        VBox commentsList = new VBox(10);
        refreshCommentsList(commentsList, post);

        // Add comment form
        VBox addCommentForm = buildAddCommentForm(post, commentsList);

        inner.getChildren().addAll(commentsTitle, commentsList, addCommentForm);
        section.getChildren().addAll(sep, inner);
        return section;
    }

    private void refreshCommentsList(VBox commentsList, Post post) {
        commentsList.getChildren().clear();
        List<Comment> comments = commentService.getCommentsByPostId(post.getId());
        if (comments.isEmpty()) {
            Label noComments = new Label("No comments yet. Start the conversation!");
            noComments.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13; -fx-font-style: italic; -fx-padding: 8 0;");
            commentsList.getChildren().add(noComments);
        } else {
            for (Comment comment : comments) {
                commentsList.getChildren().add(buildCommentBubble(comment, post, commentsList));
            }
        }
    }

    private HBox buildCommentBubble(Comment comment, Post post, VBox commentsList) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.TOP_LEFT);
        row.setPadding(new Insets(2, 0, 2, 0));

        // Small avatar
        StackPane avatar = new StackPane();
        avatar.setPrefSize(34, 34);
        avatar.setMinSize(34, 34);
        avatar.setMaxSize(34, 34);
        Circle circle = new Circle(17);
        String[] colors = {"#16a34a","#0d9488","#2563eb","#7c3aed","#dc2626","#ea580c","#15803d"};
        int idx = Math.abs(comment.getAuthor().charAt(0)) % colors.length;
        circle.setStyle("-fx-fill: " + colors[idx] + ";");
        Label initial = new Label(String.valueOf(comment.getAuthor().charAt(0)).toUpperCase());
        initial.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12;");
        avatar.getChildren().addAll(circle, initial);

        // Bubble
        VBox bubble = new VBox(4);
        bubble.getStyleClass().add("comment-bubble");
        HBox.setHgrow(bubble, Priority.ALWAYS);

        HBox bubbleHeader = new HBox(10);
        bubbleHeader.setAlignment(Pos.CENTER_LEFT);
        Label authorLabel = new Label(comment.getAuthor());
        authorLabel.getStyleClass().add("comment-author");
        Label dateLabel = new Label(DateFormatter.getRelativeTime(comment.getCreatedAt()));
        dateLabel.getStyleClass().add("comment-date");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Delete button for comment
        Button deleteBtn = new Button("✕");
        deleteBtn.getStyleClass().add("comment-delete-btn");
        deleteBtn.setOnAction(e -> {
            if (AlertHelper.showConfirmation("Delete Comment", "Delete this comment?")) {
                commentService.deleteComment(comment.getId());
                refreshCommentsList(commentsList, post);
            }
        });

        bubbleHeader.getChildren().addAll(authorLabel, dateLabel, spacer, deleteBtn);

        Label contentLabel = new Label(comment.getContent());
        contentLabel.getStyleClass().add("comment-content");
        contentLabel.setWrapText(true);

        bubble.getChildren().addAll(bubbleHeader, contentLabel);
        row.getChildren().addAll(avatar, bubble);
        return row;
    }

    private VBox buildAddCommentForm(Post post, VBox commentsList) {
        VBox form = new VBox(8);
        form.getStyleClass().add("add-comment-form");
        form.setPadding(new Insets(12, 0, 0, 0));

        Label label = new Label("✍ Add a comment");
        label.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #374151;");

        TextField commentAuthor = new TextField();
        commentAuthor.setPromptText("Your name");
        commentAuthor.getStyleClass().add("comment-input");
        commentAuthor.setPrefWidth(200);
        commentAuthor.setMaxWidth(240);

        TextArea commentContent = new TextArea();
        commentContent.setPromptText("Write a comment...");
        commentContent.setWrapText(true);
        commentContent.setPrefRowCount(2);
        commentContent.getStyleClass().add("comment-input");

        HBox btnRow = new HBox(8);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        Button postBtn = new Button("Post Comment");
        postBtn.getStyleClass().add("card-btn-post-comment");
        postBtn.setOnAction(e -> {
            String author = commentAuthor.getText().trim();
            String content = commentContent.getText().trim();
            if (author.isEmpty() || content.isEmpty()) {
                AlertHelper.showWarning("Missing Fields", "Please enter both your name and a comment.");
                return;
            }
            Comment newComment = new Comment(content, author, post.getId(), 1);
            if (commentService.createComment(newComment)) {
                commentAuthor.clear();
                commentContent.clear();
                refreshCommentsList(commentsList, post);
            } else {
                AlertHelper.showError("Error", "Failed to post comment.");
            }
        });
        btnRow.getChildren().add(postBtn);

        form.getChildren().addAll(label, commentAuthor, commentContent, btnRow);
        return form;
    }

    // ── Form Handlers ──────────────────────────────────────────────────────────

    @FXML
    private void handleShowAddForm() {
        editingPost = null;
        formTitleLabel.setText("✍ Write a New Post");
        submitButton.setText("Publish Post");
        titleField.clear();
        authorField.clear();
        contentArea.clear();
        clearImageSelection();
        formPanel.setVisible(true);
        formPanel.setManaged(true);
    }

    @FXML
    private void handleCancelForm() {
        formPanel.setVisible(false);
        formPanel.setManaged(false);
        editingPost = null;
        clearImageSelection();
    }

    @FXML
    private void handleSubmitForm() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String content = contentArea.getText().trim();

        Validator.ValidationResult tv = Validator.validatePostTitle(title);
        if (!tv.isValid()) { AlertHelper.showError("Validation Error", tv.getMessage()); return; }
        Validator.ValidationResult cv = Validator.validatePostContent(content);
        if (!cv.isValid()) { AlertHelper.showError("Validation Error", cv.getMessage()); return; }
        if (!Validator.isNotEmpty(author)) { AlertHelper.showError("Validation Error", "Author name is required."); return; }

        // Handle image upload
        String imagePath = null;
        if (selectedImageFile != null) {
            System.out.println("📸 Image selected: " + selectedImageFile.getAbsolutePath());
            try {
                imagePath = ImageHelper.saveImage(selectedImageFile);
                System.out.println("✅ Image saved to: " + imagePath);
            } catch (Exception e) {
                System.err.println("❌ Image save failed: " + e.getMessage());
                e.printStackTrace();
                AlertHelper.showError("Image Error", "Could not save image: " + e.getMessage());
                return;
            }
        } else {
            System.out.println("ℹ️ No image selected for this post");
        }

        if (editingPost == null) {
            Post newPost = new Post(title, content, author, 1);
            newPost.setImagePath(imagePath);
            System.out.println("📝 Creating post with image path: " + imagePath);
            if (postService.createPost(newPost)) {
                System.out.println("✅ Post created successfully with ID: " + newPost.getId());
                AlertHelper.showSuccess("Published!", "Your post is live.");
                handleCancelForm();
                loadPosts();
            } else {
                System.err.println("❌ Failed to create post");
                AlertHelper.showError("Error", "Could not publish post.");
            }
        } else {
            editingPost.setTitle(title);
            editingPost.setContent(content);
            if (imagePath != null) {
                editingPost.setImagePath(imagePath);
                System.out.println("📝 Updating post with new image: " + imagePath);
            }
            if (postService.updatePost(editingPost)) {
                System.out.println("✅ Post updated successfully");
                AlertHelper.showSuccess("Updated!", "Post updated successfully.");
                handleCancelForm();
                loadPosts();
            } else {
                System.err.println("❌ Failed to update post");
                AlertHelper.showError("Error", "Could not update post.");
            }
        }
    }

    private void openEditForm(Post post) {
        editingPost = post;
        formTitleLabel.setText("✏ Edit Post");
        submitButton.setText("Save Changes");
        titleField.setText(post.getTitle());
        authorField.setText(post.getAuthor());
        contentArea.setText(post.getContent());
        formPanel.setVisible(true);
        formPanel.setManaged(true);
        postsScrollPane.setVvalue(0);
    }

    private void handleDeletePost(Post post) {
        if (AlertHelper.showConfirmation("Delete Post",
                "Delete \"" + post.getTitle() + "\"? This will also remove all its comments.")) {
            if (postService.deletePost(post.getId())) {
                loadPosts();
            } else AlertHelper.showError("Error", "Could not delete post.");
        }
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadPosts();
    }

    // ── Image Handling ───────────────────────────────────────────────────────

    @FXML
    private void handleChooseImage() {
        File file = ImageHelper.chooseImage(formPanel.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            
            // Update file label with truncated name
            String fileName = file.getName();
            if (fileName.length() > 30) {
                fileName = fileName.substring(0, 27) + "...";
            }
            imageFileLabel.setText("✓ " + fileName);
            imageFileLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 12; -fx-font-weight: bold;");
            
            // Load preview
            try {
                Image image = new Image(file.toURI().toString());
                imagePreview.setImage(image);
                imagePreviewContainer.setVisible(true);
                imagePreviewContainer.setManaged(true);
                removeImageBtn.setVisible(true);
                removeImageBtn.setManaged(true);
                
                // Show preview toggle button
                if (previewToggleBtn != null) {
                    previewToggleBtn.setVisible(true);
                    previewToggleBtn.setManaged(true);
                }
            } catch (Exception e) {
                AlertHelper.showError("Error", "Could not load image preview: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleTogglePreview() {
        previewVisible = !previewVisible;
        if (imagePreviewBox != null) {
            imagePreviewBox.setVisible(previewVisible);
            imagePreviewBox.setManaged(previewVisible);
        }
        if (previewToggleBtn != null) {
            previewToggleBtn.setText(previewVisible ? "👁 Hide Preview" : "👁 Show Preview");
        }
    }

    @FXML
    private void handleRemoveImage() {
        selectedImageFile = null;
        imageFileLabel.setText("No image selected");
        imageFileLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12;");
        imagePreview.setImage(null);
        imagePreviewContainer.setVisible(false);
        imagePreviewContainer.setManaged(false);
        removeImageBtn.setVisible(false);
        removeImageBtn.setManaged(false);
        if (imagePreviewBox != null) {
            imagePreviewBox.setVisible(false);
            imagePreviewBox.setManaged(false);
        }
        if (previewToggleBtn != null) {
            previewToggleBtn.setVisible(false);
            previewToggleBtn.setManaged(false);
        }
        previewVisible = false;
    }

    private void clearImageSelection() {
        selectedImageFile = null;
        previewVisible = false;
        if (imageFileLabel != null) {
            imageFileLabel.setText("No image selected");
            imageFileLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12;");
        }
        if (imagePreview != null) imagePreview.setImage(null);
        if (imagePreviewContainer != null) {
            imagePreviewContainer.setVisible(false);
            imagePreviewContainer.setManaged(false);
        }
        if (imagePreviewBox != null) {
            imagePreviewBox.setVisible(false);
            imagePreviewBox.setManaged(false);
        }
        if (removeImageBtn != null) {
            removeImageBtn.setVisible(false);
            removeImageBtn.setManaged(false);
        }
        if (previewToggleBtn != null) {
            previewToggleBtn.setVisible(false);
            previewToggleBtn.setManaged(false);
            previewToggleBtn.setText("👁 Show Preview");
        }
    }
}
