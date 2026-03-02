package org.example.dao;

import org.example.model.Order;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC access layer for the orders table.
 * This class only stores/retrieves order rows; it does NOT touch the products
 * table (stock management is the responsibility of ProductService).
 */
public class OrderDAO {

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    public List<Order> getAll() throws SQLException {
        List<Order> list = new ArrayList<>();
        String sql = """
            SELECT o.*, p.name AS product_name
              FROM orders o
              LEFT JOIN products p ON o.product_id = p.id
             ORDER BY o.created_at DESC
            """;
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Order> getByCustomerId(long customerId) throws SQLException {
        List<Order> list = new ArrayList<>();
        String sql = """
            SELECT o.*, p.name AS product_name
              FROM orders o
              LEFT JOIN products p ON o.product_id = p.id
             WHERE o.customer_id = ?
             ORDER BY o.created_at DESC
            """;
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public Order getById(long id) throws SQLException {
        String sql = """
            SELECT o.*, p.name AS product_name
              FROM orders o
              LEFT JOIN products p ON o.product_id = p.id
             WHERE o.id = ?
            """;
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public int getActiveOrderCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders WHERE status NOT IN ('delivered','cancelled')";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public double getTotalRevenue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_price),0) FROM orders WHERE status = 'delivered'";
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
                (customer_id, product_id, seller_id, quantity, unit_price, total_price,
                 status, shipping_address, shipping_city, shipping_postal, notes, delivery_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, o.getCustomerId());
            ps.setLong(2, o.getProductId());
            ps.setLong(3, o.getSellerId());
            ps.setInt(4, o.getQuantity());
            ps.setDouble(5, o.getUnitPrice());
            ps.setDouble(6, o.getTotalPrice());
            ps.setString(7, o.getStatus());
            ps.setString(8, o.getShippingAddress());
            ps.setString(9, o.getShippingCity());
            ps.setString(10, o.getShippingPostal());
            ps.setString(11, o.getNotes());
            if (o.getDeliveryDate() != null)
                ps.setDate(12, Date.valueOf(o.getDeliveryDate()));
            else
                ps.setNull(12, Types.DATE);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) o.setId(keys.getLong(1));
            }
        }
    }

    public void update(Order o) throws SQLException {
        String sql = """
            UPDATE orders
               SET product_id=?, quantity=?, unit_price=?, total_price=?, status=?,
                   shipping_address=?, shipping_city=?, shipping_postal=?, notes=?,
                   delivery_date=?, updated_at=NOW()
             WHERE id=?
            """;
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setLong(1, o.getProductId());
            ps.setInt(2, o.getQuantity());
            ps.setDouble(3, o.getUnitPrice());
            ps.setDouble(4, o.getTotalPrice());
            ps.setString(5, o.getStatus());
            ps.setString(6, o.getShippingAddress());
            ps.setString(7, o.getShippingCity());
            ps.setString(8, o.getShippingPostal());
            ps.setString(9, o.getNotes());
            if (o.getDeliveryDate() != null)
                ps.setDate(10, Date.valueOf(o.getDeliveryDate()));
            else
                ps.setNull(10, Types.DATE);
            ps.setLong(11, o.getId());
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
        o.setProductId(rs.getLong("product_id"));
        o.setSellerId(rs.getLong("seller_id"));
        o.setProductName(rs.getString("product_name"));
        o.setQuantity(rs.getInt("quantity"));
        o.setUnitPrice(rs.getDouble("unit_price"));
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
