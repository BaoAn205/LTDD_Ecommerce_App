package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private ExpandableHeightGridView gridView;
    private GridAdapter adapter;
    private List<Product> productList;
    private FirebaseFirestore db;
    private static final String TAG = "HomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // --- Toolbar & Top Buttons --- 
        ImageButton cartButton = findViewById(R.id.cartButton);
        cartButton.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, CartActivity.class));
        });

        ImageButton notificationButton = findViewById(R.id.notificationButton);
        notificationButton.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, NotiActivity.class));
        });

        // --- Bottom Navigation --- 
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Already on the home screen
                return true;
            } else if (itemId == R.id.nav_favorites) {
                startActivity(new Intent(HomeActivity.this, WishlistActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                // Go to Profile screen
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });

        // --- GridView setup with Firestore Data ---
        db = FirebaseFirestore.getInstance();
        gridView = findViewById(R.id.gridView);
        productList = new ArrayList<>();
        adapter = new GridAdapter(this, productList);
        gridView.setAdapter(adapter);

        fetchProductsFromFirestore();

    }

    private void fetchProductsFromFirestore() {
        db.collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productList.clear(); 
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            product.setId(document.getId());
                            productList.add(product);
                        }
                        adapter.notifyDataSetChanged(); 
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }
}
