package org.example.dao;

import org.example.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductDAO {

    public List<Product> getAll() {
        System.out.println("[ProductDAO.getAll] >>> START");
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY id";
        try {
            Connection conn = DatabaseConnection.getConnection();
            System.out.println("[ProductDAO.getAll] Got connection, executing: " + sql);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            // Detect which columns actually exist in the result set
            Set<String> columns = getColumnNames(rs);
            System.out.println("[ProductDAO.getAll] Available columns: " + columns);

            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getLong("id"));
                if (columns.contains("user_id")) p.setUserId(rs.getLong("user_id"));
                if (columns.contains("farm_id")) {
                    long farmId = rs.getLong("farm_id");
                    p.setFarmId(rs.wasNull() ? null : farmId);
                }
                p.setName(rs.getString("name"));
                if (columns.contains("description")) p.setDescription(rs.getString("description"));
                p.setPrice(rs.getDouble("price"));
                p.setQuantity(rs.getInt("quantity"));
                if (columns.contains("unit")) p.setUnit(rs.getString("unit"));
                if (columns.contains("category")) p.setCategory(rs.getString("category"));
                if (columns.contains("image")) p.setImage(rs.getString("image"));
                if (columns.contains("status")) p.setStatus(rs.getString("status"));
                if (columns.contains("views")) p.setViews(rs.getInt("views"));
                if (columns.contains("created_at")) {
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) p.setCreatedAt(createdAt.toLocalDateTime());
                }
                if (columns.contains("updated_at")) {
                    Timestamp updatedAt = rs.getTimestamp("updated_at");
                    if (updatedAt != null) p.setUpdatedAt(updatedAt.toLocalDateTime());
                }
                products.add(p);
                System.out.println("[ProductDAO.getAll] Loaded product: id=" + p.getId() + " name=" + p.getName());
            }
            rs.close();
            stmt.close();
            System.out.println("[ProductDAO.getAll] Total products loaded: " + products.size());
        } catch (SQLException e) {
            System.out.println("[ProductDAO.getAll] !!! SQL ERROR: " + e.getMessage());
            System.out.println("[ProductDAO.getAll] !!! SQLState: " + e.getSQLState());
            System.out.println("[ProductDAO.getAll] !!! ErrorCode: " + e.getErrorCode());
            e.printStackTrace();
        }
        System.out.println("[ProductDAO.getAll] <<< END returning " + products.size() + " products");
        return products;
    }

    public void insert(Product product) {
        System.out.println("[ProductDAO.insert] >>> START");
        System.out.println("[ProductDAO.insert] Product data:");
        System.out.println("[ProductDAO.insert]   userId=" + product.getUserId());
        System.out.println("[ProductDAO.insert]   name=" + product.getName());
        System.out.println("[ProductDAO.insert]   price=" + product.getPrice());
        System.out.println("[ProductDAO.insert]   unit=" + product.getUnit());
        System.out.println("[ProductDAO.insert]   category=" + product.getCategory());
        System.out.println("[ProductDAO.insert]   image=" + product.getImage());
        System.out.println("[ProductDAO.insert]   status=" + product.getStatus());

        String sql = "INSERT INTO products (user_id, farm_id, name, description, price, quantity, unit, category, image, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        System.out.println("[ProductDAO.insert] SQL: " + sql);
        try {
            Connection conn = DatabaseConnection.getConnection();
            System.out.println("[ProductDAO.insert] Got connection OK");
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, product.getUserId());
            if (product.getFarmId() != null) {
                ps.setLong(2, product.getFarmId());
            } else {
                ps.setNull(2, Types.BIGINT);
            }
            ps.setString(3, product.getName());
            ps.setString(4, product.getDescription());
            ps.setDouble(5, product.getPrice());
            ps.setInt(6, product.getQuantity());
            ps.setString(7, product.getUnit());
            ps.setString(8, product.getCategory());
            if (product.getImage() != null && !product.getImage().isEmpty()) {
                ps.setString(9, product.getImage());
            } else {
                ps.setNull(9, Types.VARCHAR);
            }
            String statusVal = product.getStatus() != null ? product.getStatus() : "pending";
            ps.setString(10, statusVal);

            System.out.println("[ProductDAO.insert] Executing INSERT...");
            int rows = ps.executeUpdate();
            System.out.println("[ProductDAO.insert] INSERT executed OK. Rows affected: " + rows);
            ps.close();
        } catch (SQLException e) {
            System.out.println("[ProductDAO.insert] !!! SQL ERROR: " + e.getMessage());
            System.out.println("[ProductDAO.insert] !!! SQLState: " + e.getSQLState());
            System.out.println("[ProductDAO.insert] !!! ErrorCode: " + e.getErrorCode());
            e.printStackTrace();
        }
        System.out.println("[ProductDAO.insert] <<< END");
    }

    public void update(Product product) {
        System.out.println("[ProductDAO.update] >>> START for product id=" + product.getId());
        System.out.println("[ProductDAO.update] Product data: name=" + product.getName() + " image=" + product.getImage());

        String sql = "UPDATE products SET name=?, description=?, price=?, quantity=?, unit=?, category=?, image=?, status=? WHERE id=?";
        System.out.println("[ProductDAO.update] SQL: " + sql);
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setDouble(3, product.getPrice());
            ps.setInt(4, product.getQuantity());
            ps.setString(5, product.getUnit());
            ps.setString(6, product.getCategory());
            if (product.getImage() != null && !product.getImage().isEmpty()) {
                ps.setString(7, product.getImage());
            } else {
                ps.setNull(7, Types.VARCHAR);
            }
            ps.setString(8, product.getStatus());
            ps.setLong(9, product.getId());
            System.out.println("[ProductDAO.update] Executing UPDATE...");
            int rows = ps.executeUpdate();
            System.out.println("[ProductDAO.update] UPDATE executed OK. Rows affected: " + rows);
            ps.close();
        } catch (SQLException e) {
            System.out.println("[ProductDAO.update] !!! SQL ERROR: " + e.getMessage());
            System.out.println("[ProductDAO.update] !!! SQLState: " + e.getSQLState());
            e.printStackTrace();
        }
        System.out.println("[ProductDAO.update] <<< END");
    }

    public void delete(long id) {
        System.out.println("[ProductDAO.delete] >>> START for id=" + id);
        String sql = "DELETE FROM products WHERE id=?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            System.out.println("[ProductDAO.delete] Executing DELETE...");
            int rows = ps.executeUpdate();
            System.out.println("[ProductDAO.delete] DELETE executed OK. Rows affected: " + rows);
            ps.close();
        } catch (SQLException e) {
            System.out.println("[ProductDAO.delete] !!! SQL ERROR: " + e.getMessage());
            System.out.println("[ProductDAO.delete] !!! SQLState: " + e.getSQLState());
            e.printStackTrace();
        }
        System.out.println("[ProductDAO.delete] <<< END");
    }

    public int getProductCount() {
        String sql = "SELECT COUNT(*) FROM products";
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) return rs.getInt(1);
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("[ProductDAO.getProductCount] ERROR: " + e.getMessage());
        }
        return 0;
    }

    public List<Product> getLowStockProducts(int threshold) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE quantity <= ? AND status != 'sold_out'";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, threshold);
            ResultSet rs = ps.executeQuery();
            Set<String> columns = getColumnNames(rs);
            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getLong("id"));
                p.setName(rs.getString("name"));
                p.setPrice(rs.getDouble("price"));
                p.setQuantity(rs.getInt("quantity"));
                if (columns.contains("category")) p.setCategory(rs.getString("category"));
                if (columns.contains("status")) p.setStatus(rs.getString("status"));
                products.add(p);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("[ProductDAO.getLowStockProducts] ERROR: " + e.getMessage());
        }
        return products;
    }

    public void updateStatus(long id, String status) {
        String sql = "UPDATE products SET status=? WHERE id=?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setLong(2, id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            System.out.println("[ProductDAO.updateStatus] ERROR: " + e.getMessage());
        }
    }

    public Product getById(long id) {
        String sql = "SELECT * FROM products WHERE id=?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            Set<String> columns = getColumnNames(rs);
            if (rs.next()) {
                Product p = new Product();
                p.setId(rs.getLong("id"));
                if (columns.contains("user_id")) p.setUserId(rs.getLong("user_id"));
                p.setName(rs.getString("name"));
                p.setPrice(rs.getDouble("price"));
                p.setQuantity(rs.getInt("quantity"));
                if (columns.contains("unit")) p.setUnit(rs.getString("unit"));
                if (columns.contains("category")) p.setCategory(rs.getString("category"));
                if (columns.contains("status")) p.setStatus(rs.getString("status"));
                rs.close();
                ps.close();
                return p;
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("[ProductDAO.getById] ERROR: " + e.getMessage());
        }
        return null;
    }

    private Set<String> getColumnNames(ResultSet rs) throws SQLException {
        Set<String> columns = new HashSet<>();
        ResultSetMetaData meta = rs.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            columns.add(meta.getColumnName(i).toLowerCase());
        }
        return columns;
    }
}
