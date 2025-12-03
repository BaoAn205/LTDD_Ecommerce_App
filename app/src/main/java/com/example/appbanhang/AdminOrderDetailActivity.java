package com.example.appbanhang;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

public class AdminOrderDetailActivity extends AppCompatActivity {

    private static final String TAG = "AdminOrderDetail";

    private TextView detailOrderId, detailOrderDate, detailOrderTotal, detailOrderStatus, detailShippingAddress, detailOrderItems;
    private Button btnConfirmOrder, btnCancelOrder;

    private FirebaseFirestore db;
    private DocumentReference orderRef;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_detail);

        db = FirebaseFirestore.getInstance();

        // Get order ID from intent
        orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy ID đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        orderRef = db.collection("orders").document(orderId);

        initializeViews();
        setupToolbar();
        loadOrderDetails();
        setupActionButtons();
    }

    private void initializeViews() {
        detailOrderId = findViewById(R.id.detail_order_id);
        detailOrderDate = findViewById(R.id.detail_order_date);
        detailOrderTotal = findViewById(R.id.detail_order_total);
        detailOrderStatus = findViewById(R.id.detail_order_status);
        detailShippingAddress = findViewById(R.id.detail_shipping_address);
        detailOrderItems = findViewById(R.id.detail_order_items);
        btnConfirmOrder = findViewById(R.id.btn_confirm_order);
        btnCancelOrder = findViewById(R.id.btn_cancel_order);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadOrderDetails() {
        orderRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Order order = documentSnapshot.toObject(Order.class);
                if (order != null) {
                    order.setId(documentSnapshot.getId());
                    populateUi(order);
                }
            } else {
                Toast.makeText(this, "Đơn hàng không tồn tại.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error loading order details", e);
            Toast.makeText(this, "Lỗi khi tải chi tiết đơn hàng.", Toast.LENGTH_SHORT).show();
        });
    }

    private void populateUi(Order order) {
        detailOrderId.setText("Mã đơn hàng: #" + order.getId().substring(0, 7).toUpperCase());
        if (order.getOrderDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            detailOrderDate.setText("Ngày đặt: " + sdf.format(order.getOrderDate()));
        }
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        detailOrderTotal.setText("Tổng tiền: " + formatter.format(order.getTotalPrice()));
        detailOrderStatus.setText("Trạng thái: " + order.getStatus());

        // Format shipping address
        Map<String, String> addressMap = order.getShippingAddress();
        if (addressMap != null) {
            // SỬA LỖI: Dùng đúng key là "fullName" thay vì "receiverName"
            String addressString = "Người nhận: " + addressMap.get("fullName") + "\n"
                    + addressMap.get("phoneNumber") + "\n"
                    + addressMap.get("streetAddress") + ", " + addressMap.get("city");
            detailShippingAddress.setText(addressString);
        }

        // Format items list
        StringBuilder itemsBuilder = new StringBuilder();
        if (order.getItems() != null) {
            for (CartItem item : order.getItems()) {
                itemsBuilder.append("- ").append(item.getProductName())
                        .append(" (x").append(item.getQuantity()).append(")\n");
            }
        }
        detailOrderItems.setText(itemsBuilder.toString());
    }

    private void setupActionButtons() {
        btnConfirmOrder.setOnClickListener(v -> updateOrderStatus("Đã xử lý"));
        btnCancelOrder.setOnClickListener(v -> updateOrderStatus("Đã hủy bởi Admin"));
    }

    private void updateOrderStatus(String newStatus) {
        orderRef.update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật trạng thái thành công", Toast.LENGTH_SHORT).show();
                    detailOrderStatus.setText("Trạng thái: " + newStatus); // Update UI immediately
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error updating status", e);
                });
    }
}
