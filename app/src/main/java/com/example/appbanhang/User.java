package com.example.appbanhang;

import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;

public class User implements Serializable {
    private String fullName;
    private String email;

    // Empty constructor required for Firestore
    public User() {}

    public User(String fullName, String email) {
        this.fullName = fullName;
        this.email = email;
    }

    @PropertyName("fullName")
    public String getFullName() {
        return fullName;
    }

    @PropertyName("fullName")
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
