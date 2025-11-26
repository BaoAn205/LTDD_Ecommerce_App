package com.example.appbanhang;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    private static final String TAG = "ProductDetailActivity";
    private FloatingActionButton fabFavorite;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private Product currentProduct;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // --- View Initialization ---
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        ImageView productImageView = findViewById(R.id.product_detail_image);
        TextView productNameView = findViewById(R.id.product_detail_name);
        TextView productPriceView = findViewById(R.id.product_detail_price);
        TextView productDescriptionView = findViewById(R.id.product_detail_description);
        fabFavorite = findViewById(R.id.fab_favorite);

        // --- Toolbar Setup ---
        toolbar.setNavigationOnClickListener(v -> supportFinishAfterTransition());

        // --- Get Product Data ---
        currentProduct = (Product) getIntent().getSerializableExtra("PRODUCT_DETAIL");

        if (currentProduct != null && currentUser != null) {
            populateProductDetails(productImageView, productNameView, productPriceView, productDescriptionView);
            checkIfFavorite();
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin sản phẩm hoặc người dùng.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // --- Click Listeners ---
        fabFavorite.setOnClickListener(v -> toggleFavorite());
    }

    private void populateProductDetails(ImageView imageView, TextView nameView, TextView priceView, TextView descriptionView) {
        int imageId = getResources().getIdentifier(currentProduct.getImage(), "drawable", getPackageName());
        imageView.setImageResource(imageId != 0 ? imageId : R.drawable.ic_launcher_background);

        nameView.setText(currentProduct.getName());
        descriptionView.setText(currentProduct.getDescription());

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        priceView.setText(formatter.format(currentProduct.getPrice()));
    }

    private void checkIfFavorite() {
        DocumentReference userRef = db.collection("users").document(currentUser.getUid());
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> wishlist = (List<String>) documentSnapshot.get("wishlist");
                if (wishlist != null && wishlist.contains(currentProduct.getId())) {
                    isFavorite = true;
                } else {
                    isFavorite = false;
                }
                updateFavoriteButtonUI();
            }
        }).addOnFailureListener(e -> Log.w(TAG, "Error checking wishlist", e));
    }

    private void toggleFavorite() {
        if (currentProduct.getId() == null) return;
        DocumentReference userRef = db.collection("users").document(currentUser.getUid());

        if (isFavorite) {
            userRef.update("wishlist", FieldValue.arrayRemove(currentProduct.getId()))
                 .addOnSuccessListener(aVoid -> {
                     isFavorite = false;
                     updateFavoriteButtonUI();
                     Toast.makeText(this, "Đã xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
                 });
        } else {
            userRef.update("wishlist", FieldValue.arrayUnion(currentProduct.getId()))
                 .addOnSuccessListener(aVoid -> {
                     isFavorite = true;
                     updateFavoriteButtonUI();
                     Toast.makeText(this, "Đã thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show();
                 });
        }
    }

    private void updateFavoriteButtonUI() {
        if (isFavorite) {
            fabFavorite.setImageResource(R.drawable.ic_favorite_filled);
            fabFavorite.setSupportImageTintList(ContextCompat.getColorStateList(this, R.color.dark_primary_accent));
        } else {
            fabFavorite.setImageResource(R.drawable.ic_favorite_border);
            fabFavorite.setSupportImageTintList(ContextCompat.getColorStateList(this, android.R.color.white));
        }
    }

    // The physical back button still needs to trigger the transition
    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
        super.onBackPressed();
    }
}
