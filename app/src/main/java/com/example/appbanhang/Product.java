package com.example.appbanhang;

import java.io.Serializable;

public class Product implements Serializable {
    // Note: No 'id' field from Firestore, as the document ID serves as the unique identifier.
    private String name;
    private double price;
    private String image; // Changed from int to String
    private String description; // Added new field

    // Empty constructor required for Firestore to automatically map data
    public Product() {}

    public Product(String name, double price, String image, String description) {
        this.name = name;
        this.price = price;
        this.image = image;
        this.description = description;
    }

    // Getters
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

    // Setters (also useful for Firestore)
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
