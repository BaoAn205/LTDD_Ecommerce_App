package com.example.appbanhang;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Notification {
    private String title;
    private String message;
    private boolean isRead;
    private Date timestamp;

    public Notification() {
        // Required for Firestore
    }

    public Notification(String title, String message) {
        this.title = title;
        this.message = message;
        this.isRead = false; // By default, a new notification is unread
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    @ServerTimestamp
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
