package com.example.appbanhang;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable;

public class Product implements Serializable {

    @Exclude
    private String id;

    private String name;
    private double price;
    private String image;
    private String description;
    private String category;

    @Exclude // This field is calculated, not stored in Firestore product documents
    private int soldCount = 0;

    public Product() {
        // Required empty constructor for Firestore
    }

    public Product(String name, double price, String image, String description, String category) {
        this.name = name;
        this.price = price;
        this.image = image;
        this.description = description;
        this.category = category;
    }

    //<editor-fold desc="Getters and Setters">
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getSoldCount() {
        return soldCount;
    }

    public void setSoldCount(int soldCount) {
        this.soldCount = soldCount;
    }
    //</editor-fold>
}
