package com.example.appbanhang;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable;

public class CartItem implements Serializable {

    @Exclude
    private String id; // Document ID in Firestore

    private String productId;
    private String productName;
    private Double productPrice; // Changed from double to Double
    private String productImage;
    private Integer quantity;    // Changed from int to Integer
    private String userId;

    public CartItem() {
        // Required empty constructor for Firestore
    }

    //<editor-fold desc="Getters and Setters">
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getProductPrice() {
        // Return 0.0 if productPrice is null to prevent NullPointerException
        return productPrice != null ? productPrice : 0.0;
    }

    public void setProductPrice(Double productPrice) { // Changed parameter to Double
        this.productPrice = productPrice;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public int getQuantity() {
        // Return 0 if quantity is null to prevent NullPointerException
        return quantity != null ? quantity : 0;
    }

    public void setQuantity(Integer quantity) { // Changed parameter to Integer
        this.quantity = quantity;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    //</editor-fold>
}
