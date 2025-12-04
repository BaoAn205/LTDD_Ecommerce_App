package com.example.appbanhang;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class ViewHistoryItem {
    private String productId;
    private Date lastViewed;

    public ViewHistoryItem() {
        // Required for Firestore
    }

    public ViewHistoryItem(String productId) {
        this.productId = productId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    @ServerTimestamp
    public Date getLastViewed() {
        return lastViewed;
    }

    public void setLastViewed(Date lastViewed) {
        this.lastViewed = lastViewed;
    }
}
