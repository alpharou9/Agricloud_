package esprit.farouk.services;

import esprit.farouk.models.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private Connection connection;

    public UserService() {
        this.connection = DatabaseConnection.getConnection();
    }

    /**
     * Maps a ResultSet row to a User object
     */
    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setRoleId(rs.getLong("role_id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setPhone(rs.getString("phone"));
        user.setProfilePicture(rs.getString("profile_picture"));
        user.setStatus(rs.getString("status"));

        // Handle nullable timestamps
        Timestamp emailVerified = rs.getTimestamp("email_verified_at");
        if (emailVerified != null) {
            user.setEmailVerifiedAt(emailVerified.toLocalDateTime());
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        // Get role name if available (from JOIN)
        try {
            String roleName = rs.getString("role_name");
            user.setRoleName(roleName);
        } catch (SQLException e) {
            // Column not present (no JOIN), ignore
        }

        return user;
    }

    /**
     * Creates a new user with hashed password
     */
    public boolean create(User user) {
        String sql = "INSERT INTO users (role_id, name, email, password, phone, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, user.getRoleId());
            stmt.setString(2, user.getName());
            stmt.setString(3, user.getEmail());

            // Hash password with BCrypt
            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
            stmt.setString(4, hashedPassword);

            stmt.setString(5, user.getPhone());
            stmt.setString(6, user.getStatus());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Get generated ID
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    user.setId(rs.getLong(1));
                }
                System.out.println("✓ User created successfully: " + user.getEmail());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("✗ Failed to create user: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Gets a user by ID with role information
     */
    public User getById(long id) {
        String sql = "SELECT u.*, r.name as role_name FROM users u " +
                     "LEFT JOIN roles r ON u.role_id = r.id WHERE u.id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("✗ Failed to get user by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets a user by email with role information
     */
    public User getByEmail(String email) {
        String sql = "SELECT u.*, r.name as role_name FROM users u " +
                     "LEFT JOIN roles r ON u.role_id = r.id WHERE u.email = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("✗ Failed to get user by email: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets all users with role information
     */
    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.*, r.name as role_name FROM users u " +
                     "LEFT JOIN roles r ON u.role_id = r.id ORDER BY u.created_at DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("✗ Failed to get all users: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Updates a user (without changing password if not provided)
     */
    public boolean update(User user) {
        String sql = "UPDATE users SET role_id = ?, name = ?, email = ?, phone = ?, " +
                     "profile_picture = ?, status = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, user.getRoleId());
            stmt.setString(2, user.getName());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPhone());
            stmt.setString(5, user.getProfilePicture());
            stmt.setString(6, user.getStatus());
            stmt.setLong(7, user.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✓ User updated successfully: " + user.getEmail());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("✗ Failed to update user: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Updates user password
     */
    public boolean updatePassword(long userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            stmt.setString(1, hashedPassword);
            stmt.setLong(2, userId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✓ Password updated successfully for user ID: " + userId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("✗ Failed to update password: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes a user by ID
     */
    public boolean delete(long id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✓ User deleted successfully (ID: " + id + ")");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("✗ Failed to delete user: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Authenticates a user with email and password
     */
    public User authenticate(String email, String password) {
        User user = getByEmail(email);

        if (user == null) {
            System.err.println("✗ Authentication failed: User not found");
            return null;
        }

        // Check if user is blocked
        if ("blocked".equals(user.getStatus())) {
            System.err.println("✗ Authentication failed: User is blocked");
            return null;
        }

        // Verify password
        if (BCrypt.checkpw(password, user.getPassword())) {
            System.out.println("✓ Authentication successful: " + email);
            return user;
        } else {
            System.err.println("✗ Authentication failed: Invalid password");
            return null;
        }
    }

    /**
     * Blocks a user
     */
    public boolean blockUser(long userId) {
        String sql = "UPDATE users SET status = 'blocked' WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, userId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✓ User blocked successfully (ID: " + userId + ")");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("✗ Failed to block user: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Unblocks a user
     */
    public boolean unblockUser(long userId) {
        String sql = "UPDATE users SET status = 'active' WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, userId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✓ User unblocked successfully (ID: " + userId + ")");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("✗ Failed to unblock user: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Checks if an email already exists
     */
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("✗ Failed to check email existence: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Gets user count by role
     */
    public int countByRole(long roleId) {
        String sql = "SELECT COUNT(*) FROM users WHERE role_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, roleId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("✗ Failed to count users by role: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Searches users by name or email
     */
    public List<User> search(String keyword) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.*, r.name as role_name FROM users u " +
                     "LEFT JOIN roles r ON u.role_id = r.id " +
                     "WHERE u.name LIKE ? OR u.email LIKE ? " +
                     "ORDER BY u.created_at DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("✗ Failed to search users: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }
}
