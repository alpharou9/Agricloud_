package org.example.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Order header – one row in the orders table.
 * Individual line items live in OrderDetail (order_details table).
 */
public class Order {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private long          id;
    private long          customerId;
    private double        totalPrice;
    /** pending | confirmed | processing | shipped | delivered | cancelled */
    private String        status;
    private String        shippingAddress;
    private String        shippingCity;
    private String        shippingPostal;
    private String        notes;
    private LocalDateTime orderDate;
    private LocalDate     deliveryDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Order() {}

    // ── Getters / Setters ──────────────────────────────────────────────────

    public long getId()                          { return id; }
    public void setId(long id)                   { this.id = id; }

    public long getCustomerId()                  { return customerId; }
    public void setCustomerId(long cid)          { this.customerId = cid; }

    public double getTotalPrice()                { return totalPrice; }
    public void setTotalPrice(double tp)         { this.totalPrice = tp; }

    public String getStatus()                    { return status; }
    public void setStatus(String status)         { this.status = status; }

    public String getShippingAddress()           { return shippingAddress; }
    public void setShippingAddress(String a)     { this.shippingAddress = a; }

    public String getShippingCity()              { return shippingCity; }
    public void setShippingCity(String c)        { this.shippingCity = c; }

    public String getShippingPostal()            { return shippingPostal; }
    public void setShippingPostal(String p)      { this.shippingPostal = p; }

    public String getNotes()                     { return notes; }
    public void setNotes(String notes)           { this.notes = notes; }

    public LocalDateTime getOrderDate()          { return orderDate; }
    public void setOrderDate(LocalDateTime od)   { this.orderDate = od; }

    public LocalDate getDeliveryDate()           { return deliveryDate; }
    public void setDeliveryDate(LocalDate dd)    { this.deliveryDate = dd; }

    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void setCreatedAt(LocalDateTime ca)   { this.createdAt = ca; }

    public LocalDateTime getUpdatedAt()          { return updatedAt; }
    public void setUpdatedAt(LocalDateTime ua)   { this.updatedAt = ua; }

    /** Formatted order date for display (e.g. "Mar 01, 2026"). */
    public String getFormattedDate() {
        if (orderDate != null) return orderDate.format(FMT);
        if (createdAt  != null) return createdAt.format(FMT);
        return "—";
    }

    @Override
    public String toString() {
        return "Order #" + id + " – $" + String.format("%.2f", totalPrice);
    }
}
