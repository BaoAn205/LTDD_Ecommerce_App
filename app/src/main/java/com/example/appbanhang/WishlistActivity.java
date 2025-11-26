package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class WishlistActivity extends AppCompatActivity {

    private static final String TAG = "WishlistActivity";

    private RecyclerView wishlistRecyclerView;
    private ProductAdminAdapter adapter; // We can reuse the admin adapter
    private List<Product> favoriteProductList;
    private TextView emptyWishlistText;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        // --- Initialize --- 
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // --- Toolbar --- 
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // --- Views --- 
        wishlistRecyclerView = findViewById(R.id.wishlistRecyclerView);
        emptyWishlistText = findViewById(R.id.empty_wishlist_text);

        // --- RecyclerView Setup --- 
        favoriteProductList = new ArrayList<>();
        // Reusing ProductAdminAdapter as it has the desired layout and functionality (edit/delete)
        // You can create a new, simpler adapter if you don't want admin functions here
        adapter = new ProductAdminAdapter(this, favoriteProductList);
        wishlistRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        wishlistRecyclerView.setAdapter(adapter);

        if (currentUser != null) {
            fetchFavoriteProducts();
        } else {
            Toast.makeText(this, "Vui lòng đăng nhập để xem danh sách yêu thích.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchFavoriteProducts() {
        DocumentReference userRef = db.collection("users").document(currentUser.getUid());

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> wishlistIds = (List<String>) documentSnapshot.get("wishlist");
                if (wishlistIds != null && !wishlistIds.isEmpty()) {
                    loadProductsFromIds(wishlistIds);
                } else {
                    showEmptyView();
                }
            } else {
                showEmptyView();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error fetching user wishlist", e);
            showEmptyView();
        });
    }

    private void loadProductsFromIds(List<String> productIds) {
        favoriteProductList.clear();
        // Firestore doesn't have a great "IN" query for more than 10 items in a single query.
        // For a small wishlist, this is fine. For a large one, a more complex approach is needed.
        for (String id : productIds) {
            db.collection("products").document(id).get().addOnSuccessListener(productSnapshot -> {
                if (productSnapshot.exists()) {
                    Product product = productSnapshot.toObject(Product.class);
                    product.setId(productSnapshot.getId());
                    favoriteProductList.add(product);
                    adapter.notifyDataSetChanged();
                    showDataView();
                }
            });
        }
    }

    private void showEmptyView() {
        wishlistRecyclerView.setVisibility(View.GONE);
        emptyWishlistText.setVisibility(View.VISIBLE);
    }

    private void showDataView() {
        wishlistRecyclerView.setVisibility(View.VISIBLE);
        emptyWishlistText.setVisibility(View.GONE);
    }
}
