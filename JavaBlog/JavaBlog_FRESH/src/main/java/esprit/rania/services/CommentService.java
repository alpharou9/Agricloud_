package esprit.rania.services;

import esprit.rania.database.DatabaseConnection;
import esprit.rania.models.Comment;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommentService {

    // Create a new comment
    public boolean createComment(Comment comment) {
        Connection connection = DatabaseConnection.getConnection();
        if (connection == null) {
            System.err.println("Cannot create comment: Database connection is null");
            return false;
        }
        String query = "INSERT INTO comments (content, author, post_id, user_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, comment.getContent());
            stmt.setString(2, comment.getAuthor());
            stmt.setInt(3, comment.getPostId());
            stmt.setInt(4, comment.getUserId());
            stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    comment.setId(rs.getInt(1));
                }
                System.out.println("Comment created successfully!");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating comment: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Get all comments for a specific post
    public List<Comment> getCommentsByPostId(int postId) {
        Connection connection = DatabaseConnection.getConnection();
        if (connection == null) {
            System.err.println("Cannot get comments: Database connection is null");
            return new ArrayList<>();
        }
        List<Comment> comments = new ArrayList<>();
        String query = "SELECT * FROM comments WHERE post_id = ? ORDER BY created_at ASC";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Comment comment = new Comment(
                        rs.getInt("id"),
                        rs.getString("content"),
                        rs.getString("author"),
                        rs.getInt("post_id"),
                        rs.getInt("user_id"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                );
                comments.add(comment);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching comments: " + e.getMessage());
            e.printStackTrace();
        }
        return comments;
    }

    // Get comment by ID
    public Comment getCommentById(int id) {
        Connection connection = DatabaseConnection.getConnection();
        if (connection == null) {
            System.err.println("Cannot get comment: Database connection is null");
            return null;
        }
        String query = "SELECT * FROM comments WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Comment(
                        rs.getInt("id"),
                        rs.getString("content"),
                        rs.getString("author"),
                        rs.getInt("post_id"),
                        rs.getInt("user_id"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching comment: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Update a comment
    public boolean updateComment(Comment comment) {
        Connection connection = DatabaseConnection.getConnection();
        if (connection == null) {
            System.err.println("Cannot update comment: Database connection is null");
            return false;
        }
        String query = "UPDATE comments SET content = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, comment.getContent());
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(3, comment.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Comment updated successfully!");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating comment: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Delete a comment
    public boolean deleteComment(int id) {
        Connection connection = DatabaseConnection.getConnection();
        if (connection == null) {
            System.err.println("Cannot delete comment: Database connection is null");
            return false;
        }
        String query = "DELETE FROM comments WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Comment deleted successfully!");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting comment: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Get all comments by a user
    public List<Comment> getCommentsByUserId(int userId) {
        Connection connection = DatabaseConnection.getConnection();
        if (connection == null) {
            System.err.println("Cannot get user comments: Database connection is null");
            return new ArrayList<>();
        }
        List<Comment> comments = new ArrayList<>();
        String query = "SELECT * FROM comments WHERE user_id = ? ORDER BY created_at DESC";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Comment comment = new Comment(
                        rs.getInt("id"),
                        rs.getString("content"),
                        rs.getString("author"),
                        rs.getInt("post_id"),
                        rs.getInt("user_id"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                );
                comments.add(comment);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user comments: " + e.getMessage());
            e.printStackTrace();
        }
        return comments;
    }

    // Get comment count for a post
    public int getCommentCountByPostId(int postId) {
        Connection connection = DatabaseConnection.getConnection();
        if (connection == null) {
            System.err.println("Cannot get comment count: Database connection is null");
            return 0;
        }
        String query = "SELECT COUNT(*) as count FROM comments WHERE post_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error counting comments: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
}
