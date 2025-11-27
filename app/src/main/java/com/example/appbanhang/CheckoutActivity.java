package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CheckoutActivity extends AppCompatActivity {

    private static final String TAG = "CheckoutActivity";

    // Data
    private ArrayList<CartItem> cartItems;
    private double totalPrice = 0;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    // Views
    private TextView userNameAndPhone, userAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // --- Basic Setup ---
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // --- Intent Data Check ---
        if (currentUser == null) {
            showErrorAndFinish("Vui lòng đăng nhập để thanh toán.");
            return;
        }

        cartItems = (ArrayList<CartItem>) getIntent().getSerializableExtra("CART_ITEMS");
        totalPrice = getIntent().getDoubleExtra("TOTAL_PRICE", 0);

        if (cartItems == null || cartItems.isEmpty()) {
            showErrorAndFinish("Không có sản phẩm để thanh toán.");
            return;
        }

        // --- Toolbar ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // --- UI Setup ---
        setupViews();
        populateUserInfo();
        setupRecyclerView();
    }

    private void setupViews() {
        TextView totalPriceTextView = findViewById(R.id.simple_checkout_total_price);
        Button confirmButton = findViewById(R.id.simple_checkout_confirm_button);
        userNameAndPhone = findViewById(R.id.checkout_user_name_and_phone);
        userAddress = findViewById(R.id.checkout_user_address);

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        totalPriceTextView.setText(formatter.format(totalPrice));

        confirmButton.setOnClickListener(v -> placeOrder(v));
    }

    private void populateUserInfo() {
        db.collection("users").document(currentUser.getUid()).get().addOnSuccessListener(userDocument -> {
            if (userDocument.exists()) {
                String fullName = userDocument.getString("fullName");
                db.collection("users").document(currentUser.getUid()).collection("addresses").limit(1).get()
                    .addOnSuccessListener(addressSnapshot -> {
                        if (!addressSnapshot.isEmpty()) {
                            DocumentReference firstAddressDoc = addressSnapshot.getDocuments().get(0).getReference();
                            firstAddressDoc.get().addOnSuccessListener(addressDocument -> {
                                String street = addressDocument.getString("streetAddress");
                                String city = addressDocument.getString("city");
                                String phone = addressDocument.getString("phoneNumber");

                                userNameAndPhone.setText(String.format("%s | %s", fullName, phone));
                                userAddress.setText(String.format("%s, %s", street, city));
                            });
                        } else {
                            userNameAndPhone.setText(String.format("%s | Chưa có SĐT", fullName));
                            userAddress.setText("Chưa có thông tin địa chỉ");
                        }
                    });
            }
        });
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.checkout_items_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        OrderSummaryAdapter adapter = new OrderSummaryAdapter(cartItems);
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);
    }

    private void placeOrder(android.view.View button) {
        button.setEnabled(false);
        Toast.makeText(this, "Đặt hàng thành công", Toast.LENGTH_SHORT).show();

        // Create Address Map
        Map<String, String> shippingAddress = new HashMap<>();
        String[] nameAndPhone = userNameAndPhone.getText().toString().split("\\|");
        shippingAddress.put("fullName", nameAndPhone.length > 0 ? nameAndPhone[0].trim() : "");
        shippingAddress.put("phoneNumber", nameAndPhone.length > 1 ? nameAndPhone[1].trim() : "");
        shippingAddress.put("streetAddress", userAddress.getText().toString());

        // Create Order
        Order newOrder = new Order();
        newOrder.setUserId(currentUser.getUid());
        newOrder.setItems(cartItems);
        newOrder.setTotalPrice(totalPrice);
        newOrder.setStatus("Đang xử lý");
        newOrder.setShippingAddress(shippingAddress);

        db.collection("orders").add(newOrder)
                .addOnSuccessListener(documentReference -> clearCart())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error placing order", e);
                    Toast.makeText(CheckoutActivity.this, "Đặt hàng thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    button.setEnabled(true);
                });
    }

    private void clearCart() {
        WriteBatch batch = db.batch();
        db.collection("cart").whereEqualTo("userId", currentUser.getUid()).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentReference doc : queryDocumentSnapshots.getDocuments().stream().map(d -> d.getReference()).toList()) {
                        batch.delete(doc);
                    }
                    batch.commit().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            onCartClearedSuccessfully();
                        } else {
                            Log.e(TAG, "Error clearing cart", task.getException());
                            Toast.makeText(CheckoutActivity.this, "Đặt hàng thành công, nhưng có lỗi khi xóa giỏ hàng.", Toast.LENGTH_LONG).show();
                        }
                    });
                });
    }

    private void onCartClearedSuccessfully() {
        Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(CheckoutActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    
    private void showErrorAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }
}
