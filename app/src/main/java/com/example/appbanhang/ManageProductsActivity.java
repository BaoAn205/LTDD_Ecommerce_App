package com.example.appbanhang;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageProductsActivity extends AppCompatActivity {

    private static final String TAG = "ManageProductsActivity";

    private RecyclerView productsRecyclerView;
    private ProductAdminAdapter adapter;
    private List<Product> productList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_products);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Set the toolbar as the action bar
        toolbar.setNavigationOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        productsRecyclerView = findViewById(R.id.productsRecyclerView);
        FloatingActionButton fabAddProduct = findViewById(R.id.fab_add_product);

        productList = new ArrayList<>();
        adapter = new ProductAdminAdapter(this, productList);
        productsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        productsRecyclerView.setAdapter(adapter);

        // Click listeners are now set up to open the new activity
        fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(ManageProductsActivity.this, AddEditProductActivity.class);
            startActivity(intent);
        });

        adapter.setOnItemClickListener(new ProductAdminAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(Product product) {
                Intent intent = new Intent(ManageProductsActivity.this, AddEditProductActivity.class);
                intent.putExtra("EDIT_PRODUCT", product);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Product product) {
                new AlertDialog.Builder(ManageProductsActivity.this)
                        .setTitle("Xác nhận Xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa sản phẩm '" + product.getName() + "'?")
                        .setPositiveButton("Xóa", (dialog, which) -> deleteProduct(product))
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list every time the activity is resumed
        fetchProducts();
    }

    private void fetchProducts() {
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
                        adapter.setProducts(productList);
                    } else {
                        Log.w(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    private void deleteProduct(Product product) {
        if (product.getId() == null || product.getId().isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID sản phẩm.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("products").document(product.getId()).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ManageProductsActivity.this, "Đã xóa sản phẩm thành công.", Toast.LENGTH_SHORT).show();
                    fetchProducts(); // Refresh the list after deletion
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ManageProductsActivity.this, "Lỗi khi xóa sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.manage_products_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });

        return true;
    }
}
