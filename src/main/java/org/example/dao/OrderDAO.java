package org.example.dao;

import org.example.model.Order;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC access layer for the orders header table.
 * Line items live in OrderDetailDAO / order_details.
 */
public class OrderDAO {

    // -------------------------------------------------------------------------
    // Paginated reads
    // -------------------------------------------------------------------------

    /** All orders – admin view, newest first, server-side paged. */
    public List<Order> getPage(int pageSize, int offset) throws SQLException {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY created_at DESC LIMIT ? OFFSET ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, pageSize);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /** Orders for a specific customer, newest first, server-side paged. */
    public List<Order> getPageByCustomer(long customerId, int pageSize, int offset)
            throws SQLException {
        List<Order> list = new ArrayList<>();
        String sql = """
            SELECT * FROM orders
             WHERE customer_id = ?
             ORDER BY created_at DESC
             LIMIT ? OFFSET ?
            """;
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setLong(1, customerId);
            ps.setInt(2, pageSize);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // Count helpers (for pagination controls)
    // -------------------------------------------------------------------------

    public int countAll() throws SQLException {
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM orders")) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int countByCustomer(long customerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders WHERE customer_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    // -------------------------------------------------------------------------
    // Full list (non-paginated – used by DashboardController stats)
    // -------------------------------------------------------------------------

    public List<Order> getAll() throws SQLException {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY created_at DESC";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Order> getByCustomerId(long customerId) throws SQLException {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE customer_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public Order getById(long id) throws SQLException {
        String sql = "SELECT * FROM orders WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Dashboard stats
    // -------------------------------------------------------------------------

    public int getActiveOrderCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders WHERE status NOT IN ('delivered','cancelled')";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public double getTotalRevenue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_price),0) FROM orders WHERE status='delivered'";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        }
        return 0.0;
    }

    // -------------------------------------------------------------------------
    // Write
    // -------------------------------------------------------------------------

    public void insert(Order o) throws SQLException {
        String sql = """
            INSERT INTO orders
                (customer_id, total_price, status,
                 shipping_address, shipping_city, shipping_postal, notes, delivery_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, o.getCustomerId());
            ps.setDouble(2, o.getTotalPrice());
            ps.setString(3, o.getStatus());
            ps.setString(4, o.getShippingAddress());
            ps.setString(5, o.getShippingCity());
            ps.setString(6, o.getShippingPostal());
            ps.setString(7, o.getNotes());
            if (o.getDeliveryDate() != null)
                ps.setDate(8, Date.valueOf(o.getDeliveryDate()));
            else
                ps.setNull(8, Types.DATE);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) o.setId(keys.getLong(1));
            }
        }
    }

    public void update(Order o) throws SQLException {
        String sql = """
            UPDATE orders
               SET total_price=?, status=?,
                   shipping_address=?, shipping_city=?, shipping_postal=?,
                   notes=?, delivery_date=?, updated_at=NOW()
             WHERE id=?
            """;
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setDouble(1, o.getTotalPrice());
            ps.setString(2, o.getStatus());
            ps.setString(3, o.getShippingAddress());
            ps.setString(4, o.getShippingCity());
            ps.setString(5, o.getShippingPostal());
            ps.setString(6, o.getNotes());
            if (o.getDeliveryDate() != null)
                ps.setDate(7, Date.valueOf(o.getDeliveryDate()));
            else
                ps.setNull(7, Types.DATE);
            ps.setLong(8, o.getId());
            ps.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM orders WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    // -------------------------------------------------------------------------
    // Mapping
    // -------------------------------------------------------------------------

    private Order map(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getLong("id"));
        o.setCustomerId(rs.getLong("customer_id"));
        o.setTotalPrice(rs.getDouble("total_price"));
        o.setStatus(rs.getString("status"));
        o.setShippingAddress(rs.getString("shipping_address"));
        o.setShippingCity(rs.getString("shipping_city"));
        o.setShippingPostal(rs.getString("shipping_postal"));
        o.setNotes(rs.getString("notes"));
        Timestamp od = rs.getTimestamp("order_date");
        o.setOrderDate(od != null ? od.toLocalDateTime() : null);
        Date dd = rs.getDate("delivery_date");
        o.setDeliveryDate(dd != null ? dd.toLocalDate() : null);
        Timestamp ca = rs.getTimestamp("created_at");
        o.setCreatedAt(ca != null ? ca.toLocalDateTime() : null);
        Timestamp ua = rs.getTimestamp("updated_at");
        o.setUpdatedAt(ua != null ? ua.toLocalDateTime() : null);
        return o;
    }
}
