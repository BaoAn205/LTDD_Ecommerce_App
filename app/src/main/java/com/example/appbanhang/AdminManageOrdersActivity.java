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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminManageOrdersActivity extends AppCompatActivity implements AdminOrdersAdapter.OnOrderItemClickListener {

    private static final String TAG = "AdminManageOrders";

    private RecyclerView ordersRecyclerView;
    private AdminOrdersAdapter adapter;
    private List<Order> orderList;
    private TextView emptyOrdersText;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_orders);

        db = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        emptyOrdersText = findViewById(R.id.empty_orders_text);

        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchAllOrders();
    }

    private void setupRecyclerView() {
        orderList = new ArrayList<>();
        adapter = new AdminOrdersAdapter(this, orderList);
        adapter.setOnOrderItemClickListener(this);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ordersRecyclerView.setAdapter(adapter);
    }

    private void fetchAllOrders() {
        db.collection("orders")
                .orderBy("orderDate", Query.Direction.DESCENDING) // Show newest orders first
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        orderList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Order order = document.toObject(Order.class);
                            order.setId(document.getId()); // Set the document ID on the order object
                            orderList.add(order);
                        }
                        adapter.notifyDataSetChanged();
                        updateEmptyView();
                    } else {
                        Log.e(TAG, "Error getting orders: ", task.getException());
                        Toast.makeText(this, "Lỗi khi tải đơn hàng.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateEmptyView() {
        if (orderList.isEmpty()) {
            ordersRecyclerView.setVisibility(View.GONE);
            emptyOrdersText.setVisibility(View.VISIBLE);
        } else {
            ordersRecyclerView.setVisibility(View.VISIBLE);
            emptyOrdersText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(Order order) {
        // Navigate to AdminOrderDetailActivity, passing the order ID
        Intent intent = new Intent(this, AdminOrderDetailActivity.class);
        intent.putExtra("ORDER_ID", order.getId());
        startActivity(intent);
    }
}
