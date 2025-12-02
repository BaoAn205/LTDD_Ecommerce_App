package com.example.appbanhang;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

// Implement the new adapter's listener
public class WishlistFragment extends Fragment implements WishlistAdapter.OnItemDeleteListener {

    private static final String TAG = "WishlistFragment";

    private RecyclerView wishlistRecyclerView;
    private WishlistAdapter adapter; // Use the new WishlistAdapter
    private List<Product> favoriteProductList;
    private TextView emptyWishlistText;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wishlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        wishlistRecyclerView = view.findViewById(R.id.wishlistRecyclerView);
        emptyWishlistText = view.findViewById(R.id.empty_wishlist_text);

        setupRecyclerView();

        if (currentUser != null) {
            fetchFavoriteProducts();
        } else {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để xem danh sách yêu thích.", Toast.LENGTH_SHORT).show();
            showEmptyView();
        }
    }

    private void setupRecyclerView() {
        favoriteProductList = new ArrayList<>();
        adapter = new WishlistAdapter(getContext(), favoriteProductList);
        adapter.setOnItemDeleteListener(this); // Set the listener
        wishlistRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        wishlistRecyclerView.setAdapter(adapter);
    }

    private void fetchFavoriteProducts() {
        DocumentReference userRef = db.collection("users").document(currentUser.getUid());

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> wishlistIds = (List<String>) documentSnapshot.get("wishlist");
                if (wishlistIds != null && !wishlistIds.isEmpty()) {
                    loadProductsFromIds(wishlistIds);
                } else {
                    showEmptyView();
                }
            } else {
                showEmptyView();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error fetching user wishlist", e);
            showEmptyView();
        });
    }

    private void loadProductsFromIds(List<String> productIds) {
        favoriteProductList.clear();
        if (productIds.isEmpty()) {
            showEmptyView();
            return;
        }

        for (String id : productIds) {
            db.collection("products").document(id).get().addOnSuccessListener(productSnapshot -> {
                if (productSnapshot.exists()) {
                    Product product = productSnapshot.toObject(Product.class);
                    if (product != null) {
                        product.setId(productSnapshot.getId());
                        favoriteProductList.add(product);
                    }
                }
                adapter.notifyDataSetChanged();
                if (favoriteProductList.isEmpty()) {
                    showEmptyView();
                } else {
                    showDataView();
                }
            });
        }
    }

    // This method is called when the delete button in the adapter is clicked
    @Override
    public void onDeleteClick(Product product) {
        if (currentUser == null || product.getId() == null) {
            Toast.makeText(getContext(), "Không thể xóa. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference userRef = db.collection("users").document(currentUser.getUid());

        // Atomically remove the product ID from the 'wishlist' array in Firestore.
        userRef.update("wishlist", FieldValue.arrayRemove(product.getId()))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Đã xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
                    // Reload the product list to reflect the change
                    fetchFavoriteProducts();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi khi xóa sản phẩm.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error removing from wishlist", e);
                });
    }

    private void showEmptyView() {
        if (getView() != null) {
            wishlistRecyclerView.setVisibility(View.GONE);
            emptyWishlistText.setVisibility(View.VISIBLE);
        }
    }

    private void showDataView() {
        if (getView() != null) {
            wishlistRecyclerView.setVisibility(View.VISIBLE);
            emptyWishlistText.setVisibility(View.GONE);
        }
    }
}
