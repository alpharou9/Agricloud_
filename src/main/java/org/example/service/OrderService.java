package org.example.service;

import org.example.dao.DatabaseConnection;
import org.example.dao.OrderDAO;
import org.example.dao.OrderDetailDAO;
import org.example.model.Order;
import org.example.model.OrderDetail;
import org.example.model.Product;
import org.example.model.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Business-logic layer for the Orders module.
 *
 * Dependency direction:  OrderService → ProductService (stock)
 *                        ProductService has NO knowledge of orders.
 *
 * All multi-step DB operations (create / update / delete) run inside a
 * single JDBC transaction so partial writes never happen.
 */
public class OrderService {

    private final OrderDAO       orderDAO       = new OrderDAO();
    private final OrderDetailDAO detailDAO      = new OrderDetailDAO();
    private final ProductService productService = new ProductService();

    // =========================================================================
    // Create
    // =========================================================================

    /**
     * Saves a new multi-product order.
     * 1. Validates each item (product exists, approved, sufficient stock).
     * 2. Inserts the order header.
     * 3. Inserts all detail rows in a batch.
     * 4. Decrements stock for every product.
     * Everything runs in one transaction; any failure rolls back completely.
     */
    public void createOrder(Order order, List<OrderDetail> details, User requester)
            throws Exception {
        if (requester.getRole() != User.Role.FARMER) {
            throw new SecurityException("Only farmers can place orders.");
        }
        if (details == null || details.isEmpty()) {
            throw new Exception("Please add at least one product to the order.");
        }

        // Validate & price each item
        double total = 0;
        for (OrderDetail d : details) {
            Product p = productService.getProductById(d.getProductId());
            if (p == null) throw new Exception("Product not found (id=" + d.getProductId() + ").");
            if (!"approved".equals(p.getStatus()))
                throw new Exception("'" + p.getName() + "' is not available for ordering.");
            if (p.getQuantity() < d.getQuantity())
                throw new Exception("Insufficient stock for '" + p.getName() +
                        "'. Available: " + p.getQuantity() + ", requested: " + d.getQuantity() + ".");
            d.setUnitPrice(p.getPrice());
            d.setSubtotal(p.getPrice() * d.getQuantity());
            total += d.getSubtotal();
        }

        order.setCustomerId(requester.getId());
        order.setTotalPrice(total);
        order.setStatus("pending");

        Connection conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(false);
        try {
            orderDAO.insert(order);                          // sets order.id
            detailDAO.insertAll(order.getId(), details);
            for (OrderDetail d : details) {
                productService.decrementStock(d.getProductId(), d.getQuantity());
            }
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // =========================================================================
    // Update
    // =========================================================================

    /**
     * Replaces the items of an existing order and recalculates the total.
     * Stock for the old items is restored first, then consumed by the new items.
     * Only the order owner (farmer) or an admin may call this.
     */
    public void updateOrder(Order order, List<OrderDetail> newDetails, User requester)
            throws Exception {
        Order existing = orderDAO.getById(order.getId());
        if (existing == null) throw new Exception("Order not found.");
        if (requester.getRole() == User.Role.FARMER &&
                existing.getCustomerId() != requester.getId()) {
            throw new SecurityException("Farmers can only edit their own orders.");
        }
        if (newDetails == null || newDetails.isEmpty()) {
            throw new Exception("An order must have at least one product.");
        }

        // Validate & price new items
        double total = 0;
        for (OrderDetail d : newDetails) {
            Product p = productService.getProductById(d.getProductId());
            if (p == null) throw new Exception("Product not found (id=" + d.getProductId() + ").");
            d.setUnitPrice(p.getPrice());
            d.setSubtotal(p.getPrice() * d.getQuantity());
            total += d.getSubtotal();
        }

        List<OrderDetail> oldDetails = detailDAO.getByOrderId(order.getId());

        Connection conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(false);
        try {
            // 1. Restore stock for old items (if order is still active)
            boolean active = !("cancelled".equals(existing.getStatus()) ||
                               "delivered".equals(existing.getStatus()));
            if (active) {
                for (OrderDetail old : oldDetails) {
                    productService.incrementStock(old.getProductId(), old.getQuantity());
                }
            }
            // 2. Delete old detail rows and insert new ones
            detailDAO.deleteByOrderId(order.getId());
            detailDAO.insertAll(order.getId(), newDetails);
            // 3. Decrement stock for new items
            if (active) {
                for (OrderDetail d : newDetails) {
                    productService.decrementStock(d.getProductId(), d.getQuantity());
                }
            }
            // 4. Update order header
            order.setTotalPrice(total);
            orderDAO.update(order);
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    /**
     * Updates only the order status.
     * If transitioning TO 'cancelled', stock is restored.
     * If transitioning FROM 'cancelled' to an active status, stock is NOT re-consumed
     * (admin responsibility to verify availability first).
     */
    public void updateOrderStatus(long orderId, String newStatus, User requester)
            throws Exception {
        Order order = orderDAO.getById(orderId);
        if (order == null) throw new Exception("Order not found.");

        boolean wasFinal = "cancelled".equals(order.getStatus()) ||
                           "delivered".equals(order.getStatus());

        if ("cancelled".equals(newStatus) && !wasFinal) {
            List<OrderDetail> details = detailDAO.getByOrderId(orderId);
            for (OrderDetail d : details) {
                productService.incrementStock(d.getProductId(), d.getQuantity());
            }
        }

        order.setStatus(newStatus);
        orderDAO.update(order);
    }

    // =========================================================================
    // Cancel / Delete
    // =========================================================================

    /** Cancels an order and restores stock (unless already delivered/cancelled). */
    public void cancelOrder(long orderId, User requester) throws Exception {
        Order order = orderDAO.getById(orderId);
        if (order == null) throw new Exception("Order not found.");
        if (requester.getRole() == User.Role.FARMER &&
                order.getCustomerId() != requester.getId()) {
            throw new SecurityException("Farmers can only cancel their own orders.");
        }
        updateOrderStatus(orderId, "cancelled", requester);
    }

    /** Hard-deletes an order (admin only). Restores stock if the order was still active. */
    public void deleteOrder(long orderId, User requester) throws Exception {
        if (requester.getRole() != User.Role.ADMIN) {
            throw new SecurityException("Only admins can permanently delete orders.");
        }
        Order order = orderDAO.getById(orderId);
        if (order == null) return;

        boolean active = !("cancelled".equals(order.getStatus()) ||
                           "delivered".equals(order.getStatus()));
        Connection conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(false);
        try {
            if (active) {
                List<OrderDetail> details = detailDAO.getByOrderId(orderId);
                for (OrderDetail d : details) {
                    productService.incrementStock(d.getProductId(), d.getQuantity());
                }
            }
            orderDAO.delete(orderId);   // CASCADE deletes order_details rows
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // =========================================================================
    // Queries
    // =========================================================================

    public List<Order> getAllOrders() throws SQLException { return orderDAO.getAll(); }

    public List<Order> getFarmerOrders(long farmerId) throws SQLException {
        return orderDAO.getByCustomerId(farmerId);
    }

    // ── Paginated ──────────────────────────────────────────────────────────

    public List<Order> getAllOrdersPage(int pageSize, int offset) throws SQLException {
        return orderDAO.getPage(pageSize, offset);
    }

    public List<Order> getFarmerOrdersPage(long farmerId, int pageSize, int offset)
            throws SQLException {
        return orderDAO.getPageByCustomer(farmerId, pageSize, offset);
    }

    public int countAllOrders() throws SQLException    { return orderDAO.countAll(); }

    public int countFarmerOrders(long farmerId) throws SQLException {
        return orderDAO.countByCustomer(farmerId);
    }

    // ── Detail / stats ─────────────────────────────────────────────────────

    public List<OrderDetail> getOrderDetails(long orderId) throws SQLException {
        return detailDAO.getByOrderId(orderId);
    }

    public int getActiveOrderCount() throws SQLException {
        return orderDAO.getActiveOrderCount();
    }

    public double getTotalRevenue() throws SQLException {
        return orderDAO.getTotalRevenue();
    }
}
