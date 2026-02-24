package esprit.farouk.services;

import esprit.farouk.database.DatabaseConnection;
import esprit.farouk.models.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private final Connection connection;

    public UserService() {
        this.connection = DatabaseConnection.getConnection();
    }

    public void add(User user) throws SQLException {
        String sql = "INSERT INTO users (role_id, name, email, password, phone, profile_picture, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setLong(1, user.getRoleId());
        ps.setString(2, user.getName());
        ps.setString(3, user.getEmail());
        ps.setString(4, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        ps.setString(5, user.getPhone());
        ps.setString(6, user.getProfilePicture());
        ps.setString(7, user.getStatus() != null ? user.getStatus() : "active");
        ps.executeUpdate();
    }

    public void update(User user) throws SQLException {
        String sql = "UPDATE users SET role_id = ?, name = ?, email = ?, phone = ?, " +
                     "profile_picture = ?, status = ? WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setLong(1, user.getRoleId());
        ps.setString(2, user.getName());
        ps.setString(3, user.getEmail());
        ps.setString(4, user.getPhone());
        ps.setString(5, user.getProfilePicture());
        ps.setString(6, user.getStatus());
        ps.setLong(7, user.getId());
        ps.executeUpdate();
    }

    public void updatePassword(long userId, String newPassword) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        ps.setLong(2, userId);
        ps.executeUpdate();
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setLong(1, id);
        ps.executeUpdate();
    }

    public List<User> getAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            users.add(mapRow(rs));
        }
        return users;
    }

    public User getById(long id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setLong(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return mapRow(rs);
        }
        return null;
    }

    public User getByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return mapRow(rs);
        }
        return null;
    }

    public User authenticate(String email, String password) throws SQLException {
        User user = getByEmail(email);
        if (user != null && BCrypt.checkpw(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    /**
     * Creates a unique temporary guest user for this session.
     * Each guest login gets a separate user record with isolated data.
     */
    public User createUniqueGuestUser() throws SQLException {
        // Generate unique identifier for this guest session
        String sessionId = java.util.UUID.randomUUID().toString();
        String guestEmail = "guest_" + sessionId + "@agricloud.com";
        String guestName = "Guest_" + sessionId.substring(0, 8);

        // Get Guest role
        RoleService roleService = new RoleService();
        esprit.farouk.models.Role guestRole = roleService.getByName("Guest");

        if (guestRole == null) {
            throw new SQLException("Guest role not found in database");
        }

        // Create new unique guest user
        User newGuest = new User();
        newGuest.setRoleId(guestRole.getId());
        newGuest.setName(guestName);
        newGuest.setEmail(guestEmail);
        newGuest.setPassword("guest_temp_" + sessionId); // Will be hashed by add() method
        newGuest.setPhone(null);
        newGuest.setProfilePicture(null);
        newGuest.setStatus("active");

        add(newGuest);

        // Retrieve the newly created guest user
        return getByEmail(guestEmail);
    }

    /**
     * Cleans up old guest users created more than 24 hours ago.
     * Call this periodically to prevent database bloat.
     */
    public int cleanupOldGuestUsers() throws SQLException {
        String sql = "DELETE FROM users WHERE email LIKE 'guest_%@agricloud.com' " +
                     "AND created_at < NOW() - INTERVAL 24 HOUR";
        PreparedStatement ps = connection.prepareStatement(sql);
        return ps.executeUpdate();
    }

    /**
     * Enrolls face embeddings for a user.
     * @param userId The user ID
     * @param embeddingsJson JSON string of face embeddings
     */
    public void enrollFaceEmbeddings(long userId, String embeddingsJson) throws SQLException {
        String sql = "UPDATE users SET face_embeddings = ?, face_enrolled_at = NOW() WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, embeddingsJson);
        ps.setLong(2, userId);
        ps.executeUpdate();
    }

    /**
     * Gets all users who have face recognition enabled (non-null face_embeddings) and are active.
     * @return List of users with face enrollment
     */
    public List<User> getAllFaceEnabledUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE face_embeddings IS NOT NULL AND status = 'active'";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            users.add(mapRow(rs));
        }
        return users;
    }

    /**
     * Checks if a user has face enrollment.
     * @param userId The user ID
     * @return true if user has enrolled face, false otherwise
     */
    public boolean hasFaceEnrollment(long userId) throws SQLException {
        String sql = "SELECT face_embeddings FROM users WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setLong(1, userId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getString("face_embeddings") != null;
        }
        return false;
    }

    /**
     * Removes face enrollment data for a user.
     * @param userId The user ID
     */
    public void removeFaceEnrollment(long userId) throws SQLException {
        String sql = "UPDATE users SET face_embeddings = NULL, face_enrolled_at = NULL WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setLong(1, userId);
        ps.executeUpdate();
    }

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
        Timestamp emailVerified = rs.getTimestamp("email_verified_at");
        if (emailVerified != null) user.setEmailVerifiedAt(emailVerified.toLocalDateTime());
        user.setFaceEmbeddings(rs.getString("face_embeddings"));
        Timestamp faceEnrolled = rs.getTimestamp("face_enrolled_at");
        if (faceEnrolled != null) user.setFaceEnrolledAt(faceEnrolled.toLocalDateTime());
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) user.setCreatedAt(createdAt.toLocalDateTime());
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) user.setUpdatedAt(updatedAt.toLocalDateTime());
        return user;
    }
}
