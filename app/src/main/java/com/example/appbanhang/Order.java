package com.example.appbanhang;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Order {

    @Exclude // Exclude from Firestore serialization
    private String id;

    private String userId;
    private List<CartItem> items;
    private Double totalPrice;
    private String status;
    private Map<String, String> shippingAddress;
    @ServerTimestamp
    private Date orderDate;

    public Order() {
        // Required empty constructor for Firestore
    }

    // Getter and Setter for the ID
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // <editor-fold desc="Other Getters and Setters">
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public double getTotalPrice() {
        return totalPrice != null ? totalPrice : 0.0;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, String> getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(Map<String, String> shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }
    // </editor-fold>
}
