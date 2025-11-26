package com.example.appbanhang;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

public class Address implements Serializable {
    private String id;
    private String receiverName;
    private String phoneNumber;
    private String streetAddress;
    private String city;
    private boolean isDefault;

    public Address() {
        // Default constructor required for calls to DataSnapshot.getValue(Address.class)
    }

    public Address(String receiverName, String phoneNumber, String streetAddress, String city, boolean isDefault) {
        this.receiverName = receiverName;
        this.phoneNumber = phoneNumber;
        this.streetAddress = streetAddress;
        this.city = city;
        this.isDefault = isDefault;
    }

    // Exclude the ID from being serialized to Firestore, as it's the document ID.
    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Other getters and setters
    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}
