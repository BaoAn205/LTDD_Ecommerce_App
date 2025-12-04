package com.example.appbanhang;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    private static final String TAG = "ProductDetailActivity";
    private ImageView favoriteIcon;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private Product currentProduct;
    private boolean isFavorite = false;

    private RecyclerView reviewsRecyclerView;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;

    private TextView quantityText;
    private int quantity = 1;

    private RatingBar averageRatingBar;
    private TextView averageRatingText, reviewCountText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        ImageView productImageView = findViewById(R.id.product_detail_image);
        TextView productNameView = findViewById(R.id.product_detail_name);
        TextView productPriceView = findViewById(R.id.product_detail_price);
        TextView productDescriptionView = findViewById(R.id.product_detail_description);
        favoriteIcon = findViewById(R.id.favorite_icon);
        Button writeReviewButton = findViewById(R.id.write_review_button);
        Button addToCartButton = findViewById(R.id.add_to_cart_button);
        ImageButton increaseButton = findViewById(R.id.button_increase);
        ImageButton decreaseButton = findViewById(R.id.button_decrease);
        quantityText = findViewById(R.id.quantity_text);
        averageRatingBar = findViewById(R.id.average_rating_bar);
        averageRatingText = findViewById(R.id.average_rating_text);
        reviewCountText = findViewById(R.id.review_count_text);

        // **SỬA LẠI LOGIC NÚT QUAY LẠI**
        toolbar.setNavigationOnClickListener(v -> supportFinishAfterTransition());

        currentProduct = (Product) getIntent().getSerializableExtra("PRODUCT_DETAIL");

        if (currentProduct != null && currentUser != null) {
            populateProductDetails(productImageView, productNameView, productPriceView, productDescriptionView);
            checkIfFavorite();
            setupReviews();
            loadReviews();
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin sản phẩm hoặc người dùng.", Toast.LENGTH_SHORT).show();
            finish();
        }

        favoriteIcon.setOnClickListener(v -> toggleFavorite());
        writeReviewButton.setOnClickListener(v -> showAddReviewDialog());
        addToCartButton.setOnClickListener(v -> addToCart());
        increaseButton.setOnClickListener(v -> updateQuantity(1));
        decreaseButton.setOnClickListener(v -> updateQuantity(-1));
    }

    private void updateQuantity(int change) {
        if (quantity + change >= 1) {
            quantity += change;
            quantityText.setText(String.valueOf(quantity));
        }
    }

    private void addToCart() {
        if (currentProduct != null && currentUser != null) {
             CartManager.getInstance().addProduct(this, currentUser.getUid(), currentProduct, quantity, new CartManager.CartCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(ProductDetailActivity.this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(ProductDetailActivity.this, "Lỗi khi thêm vào giỏ hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupReviews() {
        reviewsRecyclerView = findViewById(R.id.reviews_recyclerview);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(reviewList);
        reviewsRecyclerView.setAdapter(reviewAdapter);
    }

    private void loadReviews() {
        if (currentProduct.getId() == null) return;

        db.collection("products").document(currentProduct.getId()).collection("reviews")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        reviewList.clear();
                        double totalRating = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Review review = document.toObject(Review.class);
                            reviewList.add(review);
                            totalRating += review.getRating();
                        }
                        reviewAdapter.notifyDataSetChanged();
                        
                        if (!reviewList.isEmpty()) {
                            double avg = totalRating / reviewList.size();
                            averageRatingBar.setRating((float) avg);
                            averageRatingText.setText(String.format(Locale.US, "%.1f", avg));
                            reviewCountText.setText(String.format(Locale.US, "(%d đánh giá)", reviewList.size()));
                        } else {
                            averageRatingText.setText("Chưa có đánh giá");
                            reviewCountText.setText("");
                        }

                    } else {
                        Log.w(TAG, "Error getting reviews.", task.getException());
                    }
                });
    }

    private void showAddReviewDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_review, null);
        builder.setView(dialogView);

        final RatingBar ratingBar = dialogView.findViewById(R.id.rating_bar_input);
        final EditText commentInput = dialogView.findViewById(R.id.comment_input);

        builder.setPositiveButton("Gửi", (dialog, which) -> {
            float rating = ratingBar.getRating();
            String comment = commentInput.getText().toString().trim();
            if (rating > 0 && !comment.isEmpty()) {
                submitReview(rating, comment);
            } else {
                Toast.makeText(this, "Vui lòng xếp hạng và viết bình luận", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void submitReview(float rating, String comment) {
         db.collection("users").document(currentUser.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()){
                String userName = documentSnapshot.getString("fullName");
                 Review review = new Review(currentUser.getUid(), userName, rating, comment);
                 saveReviewToFirestore(review);
            }
         });
    }

    private void saveReviewToFirestore(Review review) {
        db.collection("products").document(currentProduct.getId()).collection("reviews")
            .add(review)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(this, "Đánh giá của bạn đã được gửi", Toast.LENGTH_SHORT).show();
                loadReviews(); // Refresh the reviews list
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi: Không thể gửi đánh giá", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Error adding review", e);
            });
    }

    private void populateProductDetails(ImageView imageView, TextView nameView, TextView priceView, TextView descriptionView) {
        int imageId = getResources().getIdentifier(currentProduct.getImage(), "drawable", getPackageName());
        imageView.setImageResource(imageId != 0 ? imageId : R.drawable.product_placeholder_background);

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
                isFavorite = wishlist != null && wishlist.contains(currentProduct.getId());
                updateFavoriteButtonUI();
            }
        }).addOnFailureListener(e -> Log.w(TAG, "Error checking wishlist", e));
    }

    private void toggleFavorite() {
        if (currentProduct.getId() == null) return;
        DocumentReference userRef = db.collection("users").document(currentUser.getUid());

        FieldValue fieldValue = isFavorite ? FieldValue.arrayRemove(currentProduct.getId()) : FieldValue.arrayUnion(currentProduct.getId());
        userRef.update("wishlist", fieldValue)
             .addOnSuccessListener(aVoid -> {
                 isFavorite = !isFavorite;
                 updateFavoriteButtonUI();
                 Toast.makeText(this, isFavorite ? "Đã thêm vào danh sách yêu thích" : "Đã xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
             });
    }

    private void updateFavoriteButtonUI() {
        if (isFavorite) {
            favoriteIcon.setImageResource(R.drawable.ic_heart_filled_red);
        } else {
            favoriteIcon.setImageResource(R.drawable.ic_heart_outline);
        }
    }
}
