package com.example.appbanhang;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddEditProductActivity extends AppCompatActivity {

    private TextInputEditText editProductName, editProductPrice, editProductDescription, editProductImageName, editProductCategory;
    private Button saveProductButton;
    private MaterialToolbar toolbar;

    private FirebaseFirestore db;
    private Product currentProduct;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_product);

        db = FirebaseFirestore.getInstance();

        // --- View Initialization ---
        toolbar = findViewById(R.id.toolbar);
        editProductName = findViewById(R.id.edit_product_name);
        editProductPrice = findViewById(R.id.edit_product_price);
        editProductDescription = findViewById(R.id.edit_product_description);
        editProductImageName = findViewById(R.id.edit_product_image_name);
        editProductCategory = findViewById(R.id.edit_product_category);
        saveProductButton = findViewById(R.id.save_product_button);

        // --- Check for Edit Mode ---
        if (getIntent().hasExtra("EDIT_PRODUCT")) {
            currentProduct = (Product) getIntent().getSerializableExtra("EDIT_PRODUCT");
            isEditMode = true;
            setupEditMode();
        }

        // --- Toolbar Setup ---
        toolbar.setNavigationOnClickListener(v -> finish());
        if (!isEditMode) {
            toolbar.setTitle("Thêm Sản phẩm Mới");
        } else {
            toolbar.setTitle("Sửa Sản phẩm");
        }

        // --- Save Button Logic ---
        saveProductButton.setOnClickListener(v -> saveProduct());
    }

    private void setupEditMode() {
        if (currentProduct != null) {
            editProductName.setText(currentProduct.getName());
            editProductPrice.setText(String.valueOf(currentProduct.getPrice()));
            editProductDescription.setText(currentProduct.getDescription());
            editProductImageName.setText(currentProduct.getImage());
            editProductCategory.setText(currentProduct.getCategory());
        }
    }

    private void saveProduct() {
        // --- Get data from fields ---
        String name = editProductName.getText().toString().trim();
        String priceStr = editProductPrice.getText().toString().trim();
        String description = editProductDescription.getText().toString().trim();
        String imageName = editProductImageName.getText().toString().trim();
        String category = editProductCategory.getText().toString().trim();

        // --- Validate input ---
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(imageName) || TextUtils.isEmpty(category)) {
            Toast.makeText(this, "Vui lòng điền tất cả các trường", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá không hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Create or Update Product Object ---
        Product productToSave = new Product(name, price, imageName, description, category);

        if (isEditMode) {
            // --- UPDATE existing product ---
            if (currentProduct != null && currentProduct.getId() != null) {
                db.collection("products").document(currentProduct.getId()).set(productToSave)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(AddEditProductActivity.this, "Cập nhật sản phẩm thành công!", Toast.LENGTH_SHORT).show();
                            finish(); // Go back to the manage screen
                        })
                        .addOnFailureListener(e -> Toast.makeText(AddEditProductActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        } else {
            // --- ADD new product ---
            db.collection("products").add(productToSave)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(AddEditProductActivity.this, "Thêm sản phẩm thành công!", Toast.LENGTH_SHORT).show();
                        finish(); // Go back to the manage screen
                    })
                    .addOnFailureListener(e -> Toast.makeText(AddEditProductActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}
