package org.example.dao;

import org.example.model.Order;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OrderDAO {

    public List<Order> getAll() {
        System.out.println("[OrderDAO.getAll] >>> START");
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, p.name AS product_name FROM orders o " +
                     "LEFT JOIN products p ON o.product_id = p.id ORDER BY o.id";
        try {
            Connection conn = DatabaseConnection.getConnection();
            System.out.println("[OrderDAO.getAll] Got connection, executing: " + sql);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            // Detect which columns actually exist in the result set
            Set<String> columns = getColumnNames(rs);
            System.out.println("[OrderDAO.getAll] Available columns: " + columns);

            while (rs.next()) {
                Order o = new Order();
                o.setId(rs.getLong("id"));
                if (columns.contains("customer_id")) o.setCustomerId(rs.getLong("customer_id"));
                if (columns.contains("product_id")) o.setProductId(rs.getLong("product_id"));
                if (columns.contains("seller_id")) o.setSellerId(rs.getLong("seller_id"));
                if (columns.contains("product_name")) o.setProductName(rs.getString("product_name"));
                if (columns.contains("quantity")) o.setQuantity(rs.getInt("quantity"));
                if (columns.contains("unit_price")) o.setUnitPrice(rs.getDouble("unit_price"));
                if (columns.contains("total_price")) o.setTotalPrice(rs.getDouble("total_price"));
                if (columns.contains("status")) o.setStatus(rs.getString("status"));
                if (columns.contains("shipping_address")) o.setShippingAddress(rs.getString("shipping_address"));
                if (columns.contains("shipping_city")) o.setShippingCity(rs.getString("shipping_city"));
                if (columns.contains("shipping_postal")) o.setShippingPostal(rs.getString("shipping_postal"));
                if (columns.contains("notes")) o.setNotes(rs.getString("notes"));
                if (columns.contains("order_date")) {
                    Timestamp orderDate = rs.getTimestamp("order_date");
                    if (orderDate != null) o.setOrderDate(orderDate.toLocalDateTime());
                }
                if (columns.contains("delivery_date")) {
                    Date deliveryDate = rs.getDate("delivery_date");
                    if (deliveryDate != null) o.setDeliveryDate(deliveryDate.toLocalDate());
                }
                if (columns.contains("created_at")) {
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) o.setCreatedAt(createdAt.toLocalDateTime());
                }
                if (columns.contains("updated_at")) {
                    Timestamp updatedAt = rs.getTimestamp("updated_at");
                    if (updatedAt != null) o.setUpdatedAt(updatedAt.toLocalDateTime());
                }
                orders.add(o);
                System.out.println("[OrderDAO.getAll] Loaded order: id=" + o.getId() + " product=" + o.getProductName() + " qty=" + o.getQuantity() + " total=" + o.getTotalPrice() + " status=" + o.getStatus());
            }
            rs.close();
            stmt.close();
            System.out.println("[OrderDAO.getAll] Total orders loaded: " + orders.size());
        } catch (SQLException e) {
            System.out.println("[OrderDAO.getAll] !!! SQL ERROR: " + e.getMessage());
            System.out.println("[OrderDAO.getAll] !!! SQLState: " + e.getSQLState());
            System.out.println("[OrderDAO.getAll] !!! ErrorCode: " + e.getErrorCode());
            e.printStackTrace();
        }
        System.out.println("[OrderDAO.getAll] <<< END returning " + orders.size() + " orders");
        return orders;
    }

    public void insert(Order order) {
        System.out.println("[OrderDAO.insert] >>> START");
        System.out.println("[OrderDAO.insert] Order data:");
        System.out.println("[OrderDAO.insert]   customerId=" + order.getCustomerId());
        System.out.println("[OrderDAO.insert]   productId=" + order.getProductId());
        System.out.println("[OrderDAO.insert]   sellerId=" + order.getSellerId());
        System.out.println("[OrderDAO.insert]   quantity=" + order.getQuantity());
        System.out.println("[OrderDAO.insert]   unitPrice=" + order.getUnitPrice());
        System.out.println("[OrderDAO.insert]   totalPrice=" + order.getTotalPrice());
        System.out.println("[OrderDAO.insert]   status=" + order.getStatus());
        System.out.println("[OrderDAO.insert]   shippingAddress=" + order.getShippingAddress());
        System.out.println("[OrderDAO.insert]   shippingCity=" + order.getShippingCity());
        System.out.println("[OrderDAO.insert]   shippingPostal=" + order.getShippingPostal());
        System.out.println("[OrderDAO.insert]   notes=" + order.getNotes());
        System.out.println("[OrderDAO.insert]   deliveryDate=" + order.getDeliveryDate());

        String sql = "INSERT INTO orders (customer_id, product_id, seller_id, quantity, unit_price, total_price, " +
                     "status, shipping_address, shipping_city, shipping_postal, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        System.out.println("[OrderDAO.insert] SQL: " + sql);
        try {
            Connection conn = DatabaseConnection.getConnection();
            System.out.println("[OrderDAO.insert] Got connection OK");
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, order.getCustomerId());
            System.out.println("[OrderDAO.insert] Set param 1 (customer_id) = " + order.getCustomerId());
            ps.setLong(2, order.getProductId());
            System.out.println("[OrderDAO.insert] Set param 2 (product_id) = " + order.getProductId());
            ps.setLong(3, order.getSellerId());
            System.out.println("[OrderDAO.insert] Set param 3 (seller_id) = " + order.getSellerId());
            ps.setInt(4, order.getQuantity());
            System.out.println("[OrderDAO.insert] Set param 4 (quantity) = " + order.getQuantity());
            ps.setDouble(5, order.getUnitPrice());
            System.out.println("[OrderDAO.insert] Set param 5 (unit_price) = " + order.getUnitPrice());
            ps.setDouble(6, order.getTotalPrice());
            System.out.println("[OrderDAO.insert] Set param 6 (total_price) = " + order.getTotalPrice());
            String statusVal = order.getStatus() != null ? order.getStatus() : "pending";
            ps.setString(7, statusVal);
            System.out.println("[OrderDAO.insert] Set param 7 (status) = " + statusVal);
            ps.setString(8, order.getShippingAddress());
            System.out.println("[OrderDAO.insert] Set param 8 (shipping_address) = " + order.getShippingAddress());
            ps.setString(9, order.getShippingCity());
            System.out.println("[OrderDAO.insert] Set param 9 (shipping_city) = " + order.getShippingCity());
            ps.setString(10, order.getShippingPostal());
            System.out.println("[OrderDAO.insert] Set param 10 (shipping_postal) = " + order.getShippingPostal());
            ps.setString(11, order.getNotes());
            System.out.println("[OrderDAO.insert] Set param 11 (notes) = " + order.getNotes());

            System.out.println("[OrderDAO.insert] Executing INSERT...");
            int rows = ps.executeUpdate();
            System.out.println("[OrderDAO.insert] INSERT executed OK. Rows affected: " + rows);
            ps.close();
        } catch (SQLException e) {
            System.out.println("[OrderDAO.insert] !!! SQL ERROR: " + e.getMessage());
            System.out.println("[OrderDAO.insert] !!! SQLState: " + e.getSQLState());
            System.out.println("[OrderDAO.insert] !!! ErrorCode: " + e.getErrorCode());
            e.printStackTrace();
        }
        System.out.println("[OrderDAO.insert] <<< END");
    }

    public void update(Order order) {
        System.out.println("[OrderDAO.update] >>> START for order id=" + order.getId());
        System.out.println("[OrderDAO.update] Order data: productId=" + order.getProductId() + " qty=" + order.getQuantity() + " unitPrice=" + order.getUnitPrice() + " total=" + order.getTotalPrice() + " status=" + order.getStatus());

        String sql = "UPDATE orders SET product_id=?, quantity=?, unit_price=?, total_price=?, " +
                     "status=?, shipping_address=?, shipping_city=?, shipping_postal=?, notes=?, delivery_date=? WHERE id=?";
        System.out.println("[OrderDAO.update] SQL: " + sql);
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, order.getProductId());
            ps.setInt(2, order.getQuantity());
            ps.setDouble(3, order.getUnitPrice());
            ps.setDouble(4, order.getTotalPrice());
            ps.setString(5, order.getStatus());
            ps.setString(6, order.getShippingAddress());
            ps.setString(7, order.getShippingCity());
            ps.setString(8, order.getShippingPostal());
            ps.setString(9, order.getNotes());
            if (order.getDeliveryDate() != null) {
                ps.setDate(10, Date.valueOf(order.getDeliveryDate()));
            } else {
                ps.setNull(10, Types.DATE);
            }
            ps.setLong(11, order.getId());
            System.out.println("[OrderDAO.update] Executing UPDATE...");
            int rows = ps.executeUpdate();
            System.out.println("[OrderDAO.update] UPDATE executed OK. Rows affected: " + rows);
            ps.close();
        } catch (SQLException e) {
            System.out.println("[OrderDAO.update] !!! SQL ERROR: " + e.getMessage());
            System.out.println("[OrderDAO.update] !!! SQLState: " + e.getSQLState());
            e.printStackTrace();
        }
        System.out.println("[OrderDAO.update] <<< END");
    }

    public void delete(long id) {
        System.out.println("[OrderDAO.delete] >>> START for id=" + id);
        String sql = "DELETE FROM orders WHERE id=?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            System.out.println("[OrderDAO.delete] Executing DELETE...");
            int rows = ps.executeUpdate();
            System.out.println("[OrderDAO.delete] DELETE executed OK. Rows affected: " + rows);
            ps.close();
        } catch (SQLException e) {
            System.out.println("[OrderDAO.delete] !!! SQL ERROR: " + e.getMessage());
            System.out.println("[OrderDAO.delete] !!! SQLState: " + e.getSQLState());
            e.printStackTrace();
        }
        System.out.println("[OrderDAO.delete] <<< END");
    }

    public int getActiveOrderCount() {
        String sql = "SELECT COUNT(*) FROM orders WHERE status NOT IN ('delivered','cancelled')";
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) return rs.getInt(1);
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("[OrderDAO.getActiveOrderCount] ERROR: " + e.getMessage());
        }
        return 0;
    }

    public double getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(total_price),0) FROM orders WHERE status = 'delivered'";
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) return rs.getDouble(1);
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("[OrderDAO.getTotalRevenue] ERROR: " + e.getMessage());
        }
        return 0.0;
    }

    private Set<String> getColumnNames(ResultSet rs) throws SQLException {
        Set<String> columns = new HashSet<>();
        ResultSetMetaData meta = rs.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            columns.add(meta.getColumnLabel(i).toLowerCase());
        }
        return columns;
    }
}
