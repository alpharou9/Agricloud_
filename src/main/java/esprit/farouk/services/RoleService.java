package esprit.farouk.services;

import esprit.farouk.models.Role;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoleService {
    private Connection connection;

    public RoleService() {
        this.connection = DatabaseConnection.getConnection();
    }

    /**
     * Maps a ResultSet row to a Role object
     */
    private Role mapRow(ResultSet rs) throws SQLException {
        Role role = new Role();
        role.setId(rs.getLong("id"));
        role.setName(rs.getString("name"));
        role.setDescription(rs.getString("description"));
        role.setPermissions(rs.getString("permissions"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            role.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            role.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return role;
    }

    /**
     * Creates a new role
     */
    public boolean create(Role role) {
        String sql = "INSERT INTO roles (name, description, permissions) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, role.getName());
            stmt.setString(2, role.getDescription());
            stmt.setString(3, role.getPermissions());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Get generated ID
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    role.setId(rs.getLong(1));
                }
                System.out.println("✓ Role created successfully: " + role.getName());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("✗ Failed to create role: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Gets a role by ID
     */
    public Role getById(long id) {
        String sql = "SELECT * FROM roles WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("✗ Failed to get role by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets a role by name
     */
    public Role getByName(String name) {
        String sql = "SELECT * FROM roles WHERE name = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("✗ Failed to get role by name: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets all roles
     */
    public List<Role> getAll() {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT * FROM roles ORDER BY name";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                roles.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("✗ Failed to get all roles: " + e.getMessage());
            e.printStackTrace();
        }
        return roles;
    }

    /**
     * Gets roles available for registration (Farmer and Customer only)
     */
    public List<Role> getRegistrationRoles() {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT * FROM roles WHERE name IN ('Farmer', 'Customer') ORDER BY name";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                roles.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("✗ Failed to get registration roles: " + e.getMessage());
            e.printStackTrace();
        }
        return roles;
    }

    /**
     * Updates a role
     */
    public boolean update(Role role) {
        String sql = "UPDATE roles SET name = ?, description = ?, permissions = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, role.getName());
            stmt.setString(2, role.getDescription());
            stmt.setString(3, role.getPermissions());
            stmt.setLong(4, role.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✓ Role updated successfully: " + role.getName());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("✗ Failed to update role: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes a role by ID
     */
    public boolean delete(long id) {
        String sql = "DELETE FROM roles WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✓ Role deleted successfully (ID: " + id + ")");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("✗ Failed to delete role: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Checks if a role name already exists
     */
    public boolean nameExists(String name) {
        String sql = "SELECT COUNT(*) FROM roles WHERE name = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("✗ Failed to check role name existence: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Checks if a role can be deleted (no users assigned)
     */
    public boolean canDelete(long roleId) {
        String sql = "SELECT COUNT(*) FROM users WHERE role_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, roleId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) == 0; // Can delete if no users have this role
            }
        } catch (SQLException e) {
            System.err.println("✗ Failed to check if role can be deleted: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
