package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private static final String TAG = "CartActivity";

    private RecyclerView cartRecyclerView;
    private CartAdapter adapter;
    private ArrayList<CartItem> cartItems;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private TextView totalPriceTextView;
    private TextView emptyCartTextView;
    private ProgressBar progressBar;

    private double totalPrice = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        emptyCartTextView = findViewById(R.id.emptyView);
        progressBar = findViewById(R.id.progressBar);
        Button checkoutButton = findViewById(R.id.checkoutButton);

        setupRecyclerView();

        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem giỏ hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadCartItems();

        checkoutButton.setOnClickListener(v -> handleCheckout());
    }

    private void handleCheckout() {
        if (cartItems == null || cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check for address before proceeding to checkout
        db.collection("users").document(currentUser.getUid()).collection("addresses")
                .limit(1) // We only need to know if at least one address exists
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshot = task.getResult();
                        if (snapshot != null && !snapshot.isEmpty()) {
                            // Address exists, proceed to checkout
                            Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                            intent.putExtra("CART_ITEMS", (Serializable) cartItems);
                            intent.putExtra("TOTAL_PRICE", totalPrice);
                            startActivity(intent);
                        } else {
                            // No address found, redirect to add address form
                            Toast.makeText(CartActivity.this, "Vui lòng thêm địa chỉ giao hàng", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(CartActivity.this, AddressFormActivity.class));
                        }
                    } else {
                        Log.e(TAG, "Error checking for address", task.getException());
                        Toast.makeText(CartActivity.this, "Lỗi khi kiểm tra địa chỉ.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupRecyclerView() {
        cartItems = new ArrayList<>();
        adapter = new CartAdapter(cartItems, this::loadCartItems);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartRecyclerView.setAdapter(adapter);
    }

    private void loadCartItems() {
        progressBar.setVisibility(View.VISIBLE);
        cartRecyclerView.setVisibility(View.GONE);
        emptyCartTextView.setVisibility(View.GONE);

        db.collection("users").document(currentUser.getUid()).collection("cart")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null) {
                        cartItems.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            CartItem item = document.toObject(CartItem.class);
                            item.setId(document.getId());
                            cartItems.add(item);
                        }
                        adapter.notifyDataSetChanged();
                        updateTotalPrice();

                        if (cartItems.isEmpty()) {
                            emptyCartTextView.setVisibility(View.VISIBLE);
                            cartRecyclerView.setVisibility(View.GONE);
                        } else {
                            emptyCartTextView.setVisibility(View.GONE);
                            cartRecyclerView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.e(TAG, "Error loading cart items: ", task.getException());
                        Toast.makeText(CartActivity.this, "Lỗi khi tải giỏ hàng.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateTotalPrice() {
        totalPrice = 0;
        for (CartItem item : cartItems) {
            totalPrice += item.getProductPrice() * item.getQuantity();
        }
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        totalPriceTextView.setText(formatter.format(totalPrice));
    }
}
