package murray.csc325sprint1.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for representing an order in the orders list table view
 */
public class OrderListItem {
    private String orderId;
    private String pickupDate;
    private String pickupTime;
    private String total;
    private String status;
    private Map<String, Integer> orderItems; // New field to store order items

    /**
     * Constructor
     *
     * @param orderId Order ID
     * @param pickupDate Pickup date
     * @param pickupTime Pickup time
     * @param total Formatted total price
     * @param status Order status
     */
    public OrderListItem(String orderId, String pickupDate, String pickupTime, String total, String status) {
        this.orderId = orderId;
        this.pickupDate = pickupDate;
        this.pickupTime = pickupTime;
        this.total = total;
        this.status = status;
        this.orderItems = new HashMap<>();
    }

    /**
     * Constructor with order items
     *
     * @param orderId Order ID
     * @param pickupDate Pickup date
     * @param pickupTime Pickup time
     * @param total Formatted total price
     * @param status Order status
     * @param orderItems Map of item names to quantities
     */
    public OrderListItem(String orderId, String pickupDate, String pickupTime, String total, String status, Map<String, Integer> orderItems) {
        this.orderId = orderId;
        this.pickupDate = pickupDate;
        this.pickupTime = pickupTime;
        this.total = total;
        this.status = status;
        this.orderItems = orderItems != null ? orderItems : new HashMap<>();
    }

    // Getters and setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(String pickupDate) {
        this.pickupDate = pickupDate;
    }

    public String getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(String pickupTime) {
        this.pickupTime = pickupTime;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, Integer> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(Map<String, Integer> orderItems) {
        this.orderItems = orderItems != null ? orderItems : new HashMap<>();
    }
}