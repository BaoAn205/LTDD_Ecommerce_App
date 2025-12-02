package com.example.appbanhang;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable;

public class Product implements Serializable {

    // The document ID from Firestore. We use @Exclude so Firestore doesn't try to save it as a field.
    @Exclude
    private String id;

    private String name;
    private double price;
    private String image;
    private String description;

    // Empty constructor required for Firestore
    public Product() {}

    public Product(String name, double price, String image, String description) {
        this.name = name;
        this.price = price;
        this.image = image;
        this.description = description;
    }

    // --- Getters ---
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getImage() {
        return image;
    }

    public String getDescription() {
        return description;
    }

    // --- Setters ---
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
