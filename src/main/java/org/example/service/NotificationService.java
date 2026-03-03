package org.example.service;

import org.example.dao.NotificationDAO;
import org.example.dao.OrderDAO;
import org.example.model.Notification;
import org.example.model.Order;
import org.example.model.Product;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Business logic for in-app notifications.
 *
 * Persisted (DB): ORDER_STATUS — created when an order is placed or its status changes.
 * Dynamic (memory): LOW_STOCK — generated from current stock levels.
 *                   PAYMENT_DUE — pending orders older than PAYMENT_DUE_DAYS days.
 */
public class NotificationService {

    private static final int LOW_STOCK_THRESHOLD  = 5;
    private static final int PAYMENT_DUE_DAYS     = 7;
    private static final int MAX_DB_NOTIFS        = 50;

    private final NotificationDAO notifDAO      = new NotificationDAO();
    private final ProductService  productService = new ProductService();
    private final OrderDAO        orderDAO       = new OrderDAO();

    // =========================================================================
    // Create
    // =========================================================================

    /**
     * Persists an ORDER_STATUS notification.
     * Called by OrderService after any status change.
     */
    public void createOrderStatusNotif(Order order, String status) {
        String title   = statusTitle(status);
        String message = "Order #" + order.getId() + " for "
                + (order.getProductName() != null ? order.getProductName() : "product")
                + " is now " + status + ".";
        Notification n = new Notification(
                Notification.Type.ORDER_STATUS, title, message,
                order.getId(), "ORDER");
        try {
            notifDAO.insert(n);
        } catch (Exception e) {
            System.err.println("[Notif] Could not persist notification: " + e.getMessage());
        }
    }

    // =========================================================================
    // Query
    // =========================================================================

    /**
     * Total unread count = DB unread + dynamic (low-stock + payment-due).
     */
    public int getUnreadCount() {
        int dbUnread = 0;
        try { dbUnread = notifDAO.countUnread(); } catch (Exception ignored) {}
        int dynamic = generateLowStockNotifs().size() + generatePaymentDueNotifs().size();
        return dbUnread + dynamic;
    }

    /**
     * Merges persisted + dynamic notifications, sorted newest-first.
     */
    public List<Notification> getAllNotifications() {
        List<Notification> all = new ArrayList<>();
        try { all.addAll(notifDAO.getAll(MAX_DB_NOTIFS)); } catch (Exception ignored) {}
        all.addAll(generateLowStockNotifs());
        all.addAll(generatePaymentDueNotifs());
        all.sort(Comparator.comparing(
                n -> n.getCreatedAt() != null ? n.getCreatedAt() : LocalDateTime.MIN,
                Comparator.reverseOrder()));
        return all;
    }

    // =========================================================================
    // Mark / Clear
    // =========================================================================

    public void markRead(long id) {
        try { notifDAO.markRead(id); } catch (Exception ignored) {}
    }

    public void markAllRead() {
        try { notifDAO.markAllRead(); } catch (Exception ignored) {}
    }

    public void clearRead() {
        try { notifDAO.deleteRead(); } catch (Exception ignored) {}
    }

    // =========================================================================
    // Dynamic generators
    // =========================================================================

    private List<Notification> generateLowStockNotifs() {
        List<Notification> list = new ArrayList<>();
        try {
            for (Product p : productService.getLowStockProducts(LOW_STOCK_THRESHOLD)) {
                Notification n = new Notification(
                        Notification.Type.LOW_STOCK,
                        "Low stock: " + p.getName(),
                        "Only " + p.getQuantity() + " " + p.getUnit() + " remaining.",
                        p.getId(), "PRODUCT");
                list.add(n);
            }
        } catch (Exception ignored) {}
        return list;
    }

    private List<Notification> generatePaymentDueNotifs() {
        List<Notification> list = new ArrayList<>();
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(PAYMENT_DUE_DAYS);
            for (Order o : orderDAO.getAll()) {
                if ("pending".equals(o.getStatus())
                        && o.getCreatedAt() != null
                        && o.getCreatedAt().isBefore(cutoff)) {
                    Notification n = new Notification(
                            Notification.Type.PAYMENT_DUE,
                            "Payment due: Order #" + o.getId(),
                            "Order #" + o.getId() + " has been pending for over "
                                    + PAYMENT_DUE_DAYS + " days.",
                            o.getId(), "ORDER");
                    list.add(n);
                }
            }
        } catch (Exception ignored) {}
        return list;
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private String statusTitle(String status) {
        return switch (status == null ? "" : status) {
            case "pending"    -> "Order Placed";
            case "confirmed"  -> "Order Confirmed";
            case "processing" -> "Order Processing";
            case "shipped"    -> "Order Shipped";
            case "delivered"  -> "Order Delivered";
            case "cancelled"  -> "Order Cancelled";
            default           -> "Order Updated";
        };
    }
}
