package esprit.rania.controller;

import esprit.rania.models.Post;
import esprit.rania.services.PostService;
import esprit.rania.utilities.AlertHelper;
import esprit.rania.utilities.DateFormatter;
import esprit.rania.utilities.Validator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class PostController implements Initializable {

    @FXML
    private TableView<Post> postsTable;
    @FXML
    private TableColumn<Post, Integer> idColumn;
    @FXML
    private TableColumn<Post, String> titleColumn;
    @FXML
    private TableColumn<Post, String> authorColumn;
    @FXML
    private TableColumn<Post, LocalDateTime> createdAtColumn;

    @FXML
    private TextField titleField;
    @FXML
    private TextArea contentArea;
    @FXML
    private TextField authorField;
    @FXML
    private TextField searchField;
    @FXML
    private Label postCountLabel;
    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button clearButton;

    private PostService postService;
    private ObservableList<Post> postsList;
    private Post selectedPost;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        postService = new PostService();
        postsList = FXCollections.observableArrayList();

        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // Format the date column
        createdAtColumn.setCellFactory(column -> new TableCell<Post, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(DateFormatter.formatDateTime(item));
                }
            }
        });

        // Load all posts
        loadPosts();

        // Set up table selection listener
        postsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedPost = newSelection;
                populateFields(newSelection);
            }
        });

        // Set up search field listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                loadPosts();
            } else {
                searchPosts(newValue);
            }
        });
    }

    // Load all posts from database
    private void loadPosts() {
        List<Post> posts = postService.getAllPosts();
        postsList.clear();
        postsList.addAll(posts);
        postsTable.setItems(postsList);
        if (postCountLabel != null) {
            postCountLabel.setText(String.valueOf(posts.size()));
        }
    }

    // Search posts by title
    private void searchPosts(String keyword) {
        List<Post> posts = postService.searchPostsByTitle(keyword);
        postsList.clear();
        postsList.addAll(posts);
        postsTable.setItems(postsList);
    }

    // Populate fields with selected post data
    private void populateFields(Post post) {
        titleField.setText(post.getTitle());
        contentArea.setText(post.getContent());
        authorField.setText(post.getAuthor());
    }

    // Clear all input fields
    @FXML
    private void handleClear() {
        titleField.clear();
        contentArea.clear();
        authorField.clear();
        selectedPost = null;
        postsTable.getSelectionModel().clearSelection();
    }

    // Add a new post
    @FXML
    private void handleAdd() {
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();
        String author = authorField.getText().trim();

        // Validate inputs
        Validator.ValidationResult titleValidation = Validator.validatePostTitle(title);
        if (!titleValidation.isValid()) {
            AlertHelper.showError("Validation Error", titleValidation.getMessage());
            return;
        }

        Validator.ValidationResult contentValidation = Validator.validatePostContent(content);
        if (!contentValidation.isValid()) {
            AlertHelper.showError("Validation Error", contentValidation.getMessage());
            return;
        }

        if (!Validator.isNotEmpty(author)) {
            AlertHelper.showError("Validation Error", "Author name cannot be empty");
            return;
        }

        // Create new post (using userId = 1 as default, you can change this based on logged-in user)
        Post newPost = new Post(title, content, author, 1);

        if (postService.createPost(newPost)) {
            AlertHelper.showSuccess("Success", "Post created successfully!");
            loadPosts();
            handleClear();
        } else {
            AlertHelper.showError("Error", "Failed to create post. Please try again.");
        }
    }

    // Update existing post
    @FXML
    private void handleUpdate() {
        if (selectedPost == null) {
            AlertHelper.showWarning("No Selection", "Please select a post to update.");
            return;
        }

        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();

        // Validate inputs
        Validator.ValidationResult titleValidation = Validator.validatePostTitle(title);
        if (!titleValidation.isValid()) {
            AlertHelper.showError("Validation Error", titleValidation.getMessage());
            return;
        }

        Validator.ValidationResult contentValidation = Validator.validatePostContent(content);
        if (!contentValidation.isValid()) {
            AlertHelper.showError("Validation Error", contentValidation.getMessage());
            return;
        }

        // Update post
        selectedPost.setTitle(title);
        selectedPost.setContent(content);

        if (postService.updatePost(selectedPost)) {
            AlertHelper.showSuccess("Success", "Post updated successfully!");
            loadPosts();
            handleClear();
        } else {
            AlertHelper.showError("Error", "Failed to update post. Please try again.");
        }
    }

    // Delete selected post
    @FXML
    private void handleDelete() {
        if (selectedPost == null) {
            AlertHelper.showWarning("No Selection", "Please select a post to delete.");
            return;
        }

        boolean confirmed = AlertHelper.showConfirmation(
                "Confirm Deletion",
                "Are you sure you want to delete this post? This will also delete all comments associated with it."
        );

        if (confirmed) {
            if (postService.deletePost(selectedPost.getId())) {
                AlertHelper.showSuccess("Success", "Post deleted successfully!");
                loadPosts();
                handleClear();
            } else {
                AlertHelper.showError("Error", "Failed to delete post. Please try again.");
            }
        }
    }

    // Refresh posts list
    @FXML
    private void handleRefresh() {
        loadPosts();
        handleClear();
        AlertHelper.showInfo("Refreshed", "Posts list has been refreshed.");
    }
}
