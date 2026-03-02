package org.example.service;

import org.example.dao.OrderDAO;
import org.example.model.Order;
import org.example.model.Product;
import org.example.model.User;

import java.sql.SQLException;
import java.util.List;

/**
 * Business-logic layer for the Orders module.
 *
 * Dependency direction: OrderService → ProductService (for stock checks/updates).
 * ProductService has NO knowledge of orders (one-way dependency).
 *
 * Responsibilities:
 *  - Enforce role rules: only farmers can place orders.
 *  - Validate stock before creating an order.
 *  - Decrement stock on order creation; restore it on cancellation.
 *  - Adjust stock when an order's quantity is edited.
 */
public class OrderService {

    private final OrderDAO orderDAO = new OrderDAO();
    // Orders depend on Products – one-way
    private final ProductService productService = new ProductService();

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    /**
     * Creates a new order for a farmer.
     * Validates that the target product is approved and has sufficient stock,
     * then decrements stock atomically after persisting the order.
     */
    public void createOrder(Order order, User requester) throws Exception {
        if (requester.getRole() != User.Role.FARMER) {
            throw new SecurityException("Only farmers can place orders.");
        }

        Product product = productService.getProductById(order.getProductId());
        if (product == null) {
            throw new Exception("Selected product does not exist.");
        }
        if (!"approved".equals(product.getStatus())) {
            throw new Exception("You can only order approved products. " +
                                "This product is currently '" + product.getStatus() + "'.");
        }
        if (product.getQuantity() < order.getQuantity()) {
            throw new Exception("Insufficient stock. Available: " +
                                product.getQuantity() + " " + product.getUnit() +
                                ", requested: " + order.getQuantity() + ".");
        }

        order.setCustomerId(requester.getId());
        order.setSellerId(product.getUserId());
        order.setUnitPrice(product.getPrice());
        order.setTotalPrice(product.getPrice() * order.getQuantity());
        order.setStatus("pending");

        orderDAO.insert(order);

        // Decrement stock AFTER the order row is committed
        productService.decrementStock(order.getProductId(), order.getQuantity());
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    /**
     * Updates an existing order. When the quantity or product changes, stock is
     * adjusted: the old quantity is restored first, then the new quantity is
     * consumed from the (potentially different) product.
     *
     * @param updated      new order data (id must match an existing row)
     * @param previous     snapshot of the order BEFORE editing (loaded by the controller)
     */
    public void updateOrder(Order updated, Order previous, User requester) throws Exception {
        if (requester.getRole() == User.Role.FARMER &&
                previous.getCustomerId() != requester.getId()) {
            throw new SecurityException("Farmers can only edit their own orders.");
        }

        boolean productChanged = updated.getProductId() != previous.getProductId();
        boolean qtyChanged     = updated.getQuantity()  != previous.getQuantity();

        if (productChanged) {
            // Restore stock to previous product, then consume from new product
            if (!"cancelled".equals(previous.getStatus()) &&
                !"delivered".equals(previous.getStatus())) {
                productService.incrementStock(previous.getProductId(), previous.getQuantity());
            }
            Product newProduct = productService.getProductById(updated.getProductId());
            if (newProduct == null) throw new Exception("New product not found.");
            if (!"approved".equals(newProduct.getStatus())) {
                throw new Exception("Cannot switch to a non-approved product.");
            }
            productService.decrementStock(updated.getProductId(), updated.getQuantity());
            updated.setSellerId(newProduct.getUserId());
            updated.setUnitPrice(newProduct.getPrice());
            updated.setTotalPrice(newProduct.getPrice() * updated.getQuantity());
        } else if (qtyChanged &&
                   !"cancelled".equals(previous.getStatus()) &&
                   !"delivered".equals(previous.getStatus())) {
            int diff = updated.getQuantity() - previous.getQuantity();
            if (diff > 0) {
                productService.decrementStock(updated.getProductId(), diff);
            } else {
                productService.incrementStock(updated.getProductId(), -diff);
            }
            updated.setTotalPrice(updated.getUnitPrice() * updated.getQuantity());
        }

        orderDAO.update(updated);
    }

    // -------------------------------------------------------------------------
    // Cancel / Delete
    // -------------------------------------------------------------------------

    /**
     * Cancels an order and restores its stock (unless already delivered/cancelled).
     */
    public void cancelOrder(long orderId, User requester) throws Exception {
        Order order = orderDAO.getById(orderId);
        if (order == null) throw new Exception("Order not found: id=" + orderId);
        if (requester.getRole() == User.Role.FARMER &&
                order.getCustomerId() != requester.getId()) {
            throw new SecurityException("Farmers can only cancel their own orders.");
        }

        boolean alreadyFinal = "cancelled".equals(order.getStatus()) ||
                               "delivered".equals(order.getStatus());
        if (!alreadyFinal) {
            productService.incrementStock(order.getProductId(), order.getQuantity());
        }

        order.setStatus("cancelled");
        orderDAO.update(order);
    }

    /**
     * Hard-deletes an order row. Does NOT restore stock (use cancelOrder for that).
     * Only admins should invoke this.
     */
    public void deleteOrder(long orderId, User requester) throws Exception {
        if (requester.getRole() != User.Role.ADMIN) {
            throw new SecurityException("Only admins can permanently delete orders.");
        }
        orderDAO.delete(orderId);
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    /** All orders – admin view. */
    public List<Order> getAllOrders() throws SQLException {
        return orderDAO.getAll();
    }

    /** Orders placed by a specific farmer – farmer view. */
    public List<Order> getFarmerOrders(long farmerId) throws SQLException {
        return orderDAO.getByCustomerId(farmerId);
    }

    public int getActiveOrderCount() throws SQLException {
        return orderDAO.getActiveOrderCount();
    }

    public double getTotalRevenue() throws SQLException {
        return orderDAO.getTotalRevenue();
    }

    /**
     * Updates only the status of an existing order — no stock adjustment.
     * Used by admins when moving an order through workflow stages
     * (confirmed → processing → shipped → delivered) without changing product or quantity.
     */
    public void updateOrderStatus(long orderId, String newStatus, User requester) throws Exception {
        Order order = orderDAO.getById(orderId);
        if (order == null) throw new Exception("Order not found: id=" + orderId);

        // Cancelling via this method restores stock (same rule as cancelOrder)
        if ("cancelled".equals(newStatus) &&
            !("cancelled".equals(order.getStatus()) || "delivered".equals(order.getStatus()))) {
            productService.incrementStock(order.getProductId(), order.getQuantity());
        }

        order.setStatus(newStatus);
        orderDAO.update(order);
    }
}
