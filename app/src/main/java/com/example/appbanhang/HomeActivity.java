package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private GridView gridView;
    private GridAdapter adapter;
    private List<Product> productList;
    private FirebaseFirestore db;
    private static final String TAG = "HomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // --- Toolbar & Navigation Buttons (Logic kept as is) ---
        Button weightsButton = findViewById(R.id.Weights);
        Button cardioButton = findViewById(R.id.Cardio);
        Button apparelButton = findViewById(R.id.Apparel);
        Button yogaButton = findViewById(R.id.Yoga); // Logout button
        ImageButton notificationButton = findViewById(R.id.notificationButton);
        ImageButton profileButton = findViewById(R.id.profileButton);



        weightsButton.setOnClickListener(v -> {
            // Logic for Weights button
        });
        notificationButton.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, NotiActivity.class));
        });
        apparelButton.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, HelpActivity.class));
        });
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Logout Logic
        yogaButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(HomeActivity.this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // --- GridView setup with Firestore Data ---
        db = FirebaseFirestore.getInstance();
        gridView = findViewById(R.id.gridView);
        productList = new ArrayList<>();
        adapter = new GridAdapter(this, productList);
        gridView.setAdapter(adapter);

        fetchProductsFromFirestore();

        // --- GridView Item Click Listener for Product Detail ---
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Product selectedProduct = productList.get(position);
            Intent intent = new Intent(HomeActivity.this, ProductDetailActivity.class);
            intent.putExtra("PRODUCT_DETAIL", selectedProduct);

            ImageView productImageView = view.findViewById(R.id.gridImage);
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    HomeActivity.this, productImageView, "product_image_transition");
            startActivity(intent, options.toBundle());
        });
    }

    private void fetchProductsFromFirestore() {
        db.collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productList.clear(); // Clear old data
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            productList.add(product);
                            Log.d(TAG, document.getId() + " => " + document.getData());
                        }
                        adapter.notifyDataSetChanged(); // Refresh the GridView
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                        Toast.makeText(HomeActivity.this, "Lỗi khi tải dữ liệu sản phẩm.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
