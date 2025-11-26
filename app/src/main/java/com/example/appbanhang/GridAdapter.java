package com.example.appbanhang;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class GridAdapter extends ArrayAdapter<Product> {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    public GridAdapter(@NonNull Context context, @NonNull List<Product> products) {
        super(context, 0, products);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.grid_item, parent, false);
        }

        Product currentProduct = getItem(position);

        ImageView imageView = listItemView.findViewById(R.id.gridImage);
        TextView nameTextView = listItemView.findViewById(R.id.gridTextName);
        TextView priceTextView = listItemView.findViewById(R.id.gridTextPrice);
        ImageButton addToCartButton = listItemView.findViewById(R.id.addToCartButton);

        if (currentProduct != null) {
            nameTextView.setText(currentProduct.getName());

            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            priceTextView.setText(formatter.format(currentProduct.getPrice()));

            int imageId = getContext().getResources().getIdentifier(currentProduct.getImage(), "drawable", getContext().getPackageName());
            if (imageId != 0) {
                imageView.setImageResource(imageId);
            } else {
                imageView.setImageResource(R.drawable.ic_launcher_background);
            }

            addToCartButton.setOnClickListener(v -> addToCart(currentProduct));

            listItemView.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), ProductDetailActivity.class);
                intent.putExtra("PRODUCT_DETAIL", currentProduct);

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        (Activity) getContext(), imageView, "product_image_transition");
                getContext().startActivity(intent, options.toBundle());
            });
        }

        return listItemView;
    }

    private void addToCart(Product product) {
        if (currentUser == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        db.collection("cart")
                .whereEqualTo("userId", userId)
                .whereEqualTo("productId", product.getId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshot = task.getResult();
                        if (snapshot != null && !snapshot.isEmpty()) {
                            // Product exists, update quantity
                            DocumentReference docRef = snapshot.getDocuments().get(0).getReference();
                            int currentQuantity = snapshot.getDocuments().get(0).getLong("quantity").intValue();
                            docRef.update("quantity", currentQuantity + 1)
                                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Đã cập nhật giỏ hàng", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        } else {
                            // Product does not exist, create new cart item
                            CartItem newItem = new CartItem();
                            newItem.setUserId(userId);
                            newItem.setProductId(product.getId());
                            newItem.setProductName(product.getName());
                            newItem.setProductPrice(product.getPrice());
                            newItem.setProductImage(product.getImage());
                            newItem.setQuantity(1);

                            db.collection("cart").add(newItem)
                                    .addOnSuccessListener(documentReference -> Toast.makeText(getContext(), "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        Log.w("GridAdapter", "Error checking cart", task.getException());
                        Toast.makeText(getContext(), "Lỗi khi kiểm tra giỏ hàng", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
