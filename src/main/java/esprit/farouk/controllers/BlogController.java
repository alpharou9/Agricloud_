package esprit.farouk.controllers;

import esprit.farouk.models.Comment;
import esprit.farouk.models.Post;
import esprit.farouk.models.Role;
import esprit.farouk.models.User;
import esprit.farouk.services.CommentService;
import esprit.farouk.services.PostService;
import esprit.farouk.services.RoleService;
import esprit.farouk.services.UserService;
import esprit.farouk.utils.TranslationUtils;
import esprit.farouk.utils.UIUtils;
import javafx.application.Platform;
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
import java.util.List;
import java.util.Optional;

public class BlogController {

    private final StackPane contentArea;
    private final User currentUser;
    private final PostService postService = new PostService();
    private final CommentService commentService = new CommentService();
    private final UserService userService = new UserService();
    private final RoleService roleService = new RoleService();

    public BlogController(StackPane contentArea, User currentUser) {
        this.contentArea = contentArea;
        this.currentUser = currentUser;
    }

    public void showPostsView() {
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
            UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load posts: " + e.getMessage());
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
                UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load posts: " + ex.getMessage());
            }
        };

        publishBtn.setOnAction(e -> {
            Post selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a post to publish.");
                return;
            }
            try {
                postService.publish(selected.getId());
                reloadTable.run();
            } catch (SQLException ex) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to publish post: " + ex.getMessage());
            }
        });

        unpublishBtn.setOnAction(e -> {
            Post selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a post to unpublish.");
                return;
            }
            try {
                postService.unpublish(selected.getId());
                reloadTable.run();
            } catch (SQLException ex) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to unpublish post: " + ex.getMessage());
            }
        });

        deleteBtn.setOnAction(e -> {
            Post selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a post to delete.");
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
                    UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete post: " + ex.getMessage());
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

    public void showBlogView() {
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
            UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load posts: " + e.getMessage());
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

    public void showMyPostsView() {
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
            UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load posts: " + e.getMessage());
        }
        table.setItems(masterData);

        Runnable reloadTable = () -> {
            masterData.clear();
            try {
                masterData.addAll(postService.getByUserId(currentUser.getId()));
            } catch (SQLException ex) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load posts: " + ex.getMessage());
            }
        };

        addBtn.setOnAction(e -> {
            showPostFormDialog(null);
            reloadTable.run();
        });

        editBtn.setOnAction(e -> {
            Post selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a post to edit.");
                return;
            }
            showPostFormDialog(selected);
            reloadTable.run();
        });

        deleteBtn.setOnAction(e -> {
            Post selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a post to delete.");
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
                    UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete post: " + ex.getMessage());
                }
            }
        });

        container.getChildren().addAll(header, table);
        contentArea.getChildren().add(container);
    }

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

        TextArea postContentArea = new TextArea(post != null ? post.getContent() : "");
        postContentArea.setPromptText("Post content");
        postContentArea.setPrefRowCount(8);
        postContentArea.setWrapText(true);

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
        grid.add(postContentArea, 1, row++);
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
            String contentVal = postContentArea.getText().trim();

            if (titleVal.length() < 3) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", "Title must be at least 3 characters.");
                continue;
            }
            if (contentVal.length() < 10) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", "Content must be at least 10 characters.");
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
                UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to save post: " + e.getMessage());
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

    public void showPostDetailView(Post post) {
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
                                    UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete comment: " + ex.getMessage());
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
                                            UIUtils.showAlert(Alert.AlertType.INFORMATION, "User Unblocked",
                                                "User '" + userToToggle.getName() + "' has been unblocked.");
                                        } else {
                                            userToToggle.setStatus("blocked");
                                            userService.update(userToToggle);
                                            UIUtils.showAlert(Alert.AlertType.INFORMATION, "User Blocked",
                                                "User '" + userToToggle.getName() + "' has been blocked.");
                                        }
                                        showPostDetailView(post); // Refresh
                                    }
                                }
                            } catch (SQLException ex) {
                                UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to update user: " + ex.getMessage());
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

        // Only non-guest users can comment
        boolean isGuest = false;
        try {
            esprit.farouk.services.RoleService guestRoleService = new esprit.farouk.services.RoleService();
            esprit.farouk.models.Role guestRole = guestRoleService.getById(currentUser.getRoleId());
            isGuest = guestRole != null && "guest".equalsIgnoreCase(guestRole.getName());
        } catch (java.sql.SQLException ex) {
            ex.printStackTrace();
        }

        ScrollPane scrollPane = new ScrollPane();
        VBox scrollContent;
        if (!isGuest) {
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
                    UIUtils.showAlert(Alert.AlertType.WARNING, "Empty Comment", "Please write something.");
                    return;
                }
                try {
                    Comment newComment = new Comment();
                    newComment.setPostId(post.getId());
                    newComment.setUserId(currentUser.getId());
                    newComment.setContent(commentText);
                    newComment.setStatus("approved");
                    commentService.add(newComment);
                    UIUtils.showAlert(Alert.AlertType.INFORMATION, "Comment Submitted", "Your comment has been posted.");
                    commentInput.clear();
                    showPostDetailView(post); // Refresh to show new comment
                } catch (SQLException ex) {
                    UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit comment: " + ex.getMessage());
                }
            });

            scrollContent = new VBox(15, titleLabel, metaLabel, contentLabel, sep, commentsTitle, commentsBox,
                    addCommentLabel, commentInput, submitCommentBtn);
        } else {
            scrollContent = new VBox(15, titleLabel, metaLabel, contentLabel, sep, commentsTitle, commentsBox);
        }
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
                        UIUtils.showAlert(Alert.AlertType.ERROR, "Translation Failed",
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
}
