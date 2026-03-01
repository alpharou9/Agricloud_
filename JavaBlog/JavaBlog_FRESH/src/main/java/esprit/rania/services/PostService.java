package esprit.rania.services;

import esprit.rania.database.DatabaseConnection;
import esprit.rania.models.Post;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PostService {

    // Helper method to safely extract Post from ResultSet
    private Post extractPostFromResultSet(ResultSet rs) throws SQLException {
        Post post = new Post(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getString("author"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime(),
                rs.getInt("user_id")
        );
        
        // Try to get image_path, but don't fail if column doesn't exist
        try {
            String imagePath = rs.getString("image_path");
            post.setImagePath(imagePath);
        } catch (SQLException e) {
            // Column doesn't exist, that's OK - leave it null
            System.out.println("Note: image_path column not found (this is OK if you haven't added it yet)");
        }
        
        return post;
    }

    // Create a new post
    public boolean createPost(Post post) {
        Connection connection = DatabaseConnection.getConnection();
        if (connection == null) {
            System.err.println("❌ Cannot create post: Database connection is null");
            return false;
        }
        
        // Check if image_path column exists
        boolean hasImageColumn = checkImagePathColumn(connection);
        
        String query;
        if (hasImageColumn) {
            query = "INSERT INTO posts (title, content, author, user_id, image_path, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        } else {
            query = "INSERT INTO posts (title, content, author, user_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
        }
        
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, post.getTitle());
            stmt.setString(2, post.getContent());
            stmt.setString(3, post.getAuthor());
            stmt.setInt(4, post.getUserId());
            
            if (hasImageColumn) {
                stmt.setString(5, post.getImagePath());
                stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            } else {
                stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            }
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getInt(1));
                }
                System.out.println("✅ Post created successfully!");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error creating post: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Check if image_path column exists
    private boolean checkImagePathColumn(Connection conn) {
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "posts", "image_path");
            return columns.next();
        } catch (SQLException e) {
            return false;
        }
    }

    // Get all posts
    public List<Post> getAllPosts() {
        Connection connection = DatabaseConnection.getConnection();
        if (connection == null) {
            System.err.println("❌ Cannot get posts: Database connection is null");
            return new ArrayList<>();
        }
        
        List<Post> posts = new ArrayList<>();
        String query = "SELECT * FROM posts ORDER BY created_at DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                posts.add(extractPostFromResultSet(rs));
            }
            
            System.out.println("✅ Fetched " + posts.size() + " posts from database");
            
        } catch (SQLException e) {
            System.err.println("❌ Error fetching posts: " + e.getMessage());
            e.printStackTrace();
        }
        
        return posts;
    }

    // Get post by ID
    public Post getPostById(int id) {
        Connection connection = DatabaseConnection.getConnection();
        if (connection == null) {
            System.err.println("❌ Cannot get post: Database connection is null");
            return null;
        }
        
        String query = "SELECT * FROM posts WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractPostFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching post: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Update a post
    public boolean updatePost(Post post) {
        Connection connection = DatabaseConnection.getConnection();
        if (connection == null) {
            System.err.println("❌ Cannot update post: Database connection is null");
            return false;
        }
        
        boolean hasImageColumn = checkImagePathColumn(connection);
        String query;
        
        if (hasImageColumn) {
            query = "UPDATE posts SET title = ?, content = ?, image_path = ?, updated_at = ? WHERE id = ?";
        } else {
            query = "UPDATE posts SET title = ?, content = ?, updated_at = ? WHERE id = ?";
        }
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, post.getTitle());
            stmt.setString(2, post.getContent());
            
            if (hasImageColumn) {
                stmt.setString(3, post.getImagePath());
                stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setInt(5, post.getId());
            } else {
                stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setInt(4, post.getId());
            }
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Post updated successfully!");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error updating post: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Delete a post
    public boolean deletePost(int id) {
        Connection connection = DatabaseConnection.getConnection();
        if (connection == null) {
            System.err.println("❌ Cannot delete post: Database connection is null");
            return false;
        }
        
        String deleteComments = "DELETE FROM comments WHERE post_id = ?";
        String deletePost = "DELETE FROM posts WHERE id = ?";
        
        try {
            try (PreparedStatement stmt = connection.prepareStatement(deleteComments)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }
            
            try (PreparedStatement stmt = connection.prepareStatement(deletePost)) {
                stmt.setInt(1, id);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("✅ Post deleted successfully!");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error deleting post: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Search posts by title
    public List<Post> searchPostsByTitle(String keyword) {
        Connection connection = DatabaseConnection.getConnection();
        if (connection == null) {
            System.err.println("❌ Cannot search posts: Database connection is null");
            return new ArrayList<>();
        }
        
        List<Post> posts = new ArrayList<>();
        String query = "SELECT * FROM posts WHERE title LIKE ? ORDER BY created_at DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                posts.add(extractPostFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error searching posts: " + e.getMessage());
            e.printStackTrace();
        }
        return posts;
    }

    // Get posts by user ID
    public List<Post> getPostsByUserId(int userId) {
        Connection connection = DatabaseConnection.getConnection();
        if (connection == null) {
            System.err.println("❌ Cannot get user posts: Database connection is null");
            return new ArrayList<>();
        }
        
        List<Post> posts = new ArrayList<>();
        String query = "SELECT * FROM posts WHERE user_id = ? ORDER BY created_at DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                posts.add(extractPostFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching user posts: " + e.getMessage());
            e.printStackTrace();
        }
        return posts;
    }
}
