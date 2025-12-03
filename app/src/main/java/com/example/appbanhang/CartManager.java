package com.example.appbanhang;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class CartManager {

    private static final String TAG = "CartManager";
    private static CartManager instance;
    private FirebaseFirestore db;

    private CartManager() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public interface CartCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public void addProduct(Context context, String userId, Product product, int quantity, final CartCallback callback) {
        if (userId == null || product == null || product.getId() == null) {
            callback.onFailure(new IllegalArgumentException("User ID or Product is invalid."));
            return;
        }

        CollectionReference cartRef = db.collection("users").document(userId).collection("cart");

        cartRef.whereEqualTo("productId", product.getId()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (!task.getResult().isEmpty()) {
                    // Product exists, update quantity
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        long currentQuantity = document.getLong("quantity");
                        long newQuantity = currentQuantity + quantity;
                        cartRef.document(document.getId()).update("quantity", newQuantity)
                                .addOnSuccessListener(aVoid -> callback.onSuccess())
                                .addOnFailureListener(e -> callback.onFailure(e));
                    }
                } else {
                    // Product does not exist, add new item
                    CartItem newItem = new CartItem();
                    newItem.setProductId(product.getId());
                    newItem.setProductName(product.getName());
                    newItem.setProductPrice(product.getPrice());
                    newItem.setQuantity(quantity);
                    newItem.setProductImage(product.getImage());
                    newItem.setUserId(userId);

                    cartRef.add(newItem)
                            .addOnSuccessListener(documentReference -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onFailure(e));
                }
            } else {
                Log.w(TAG, "Error checking cart.", task.getException());
                callback.onFailure(task.getException());
            }
        });
    }
}
