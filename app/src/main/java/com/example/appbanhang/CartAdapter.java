package com.example.appbanhang;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final List<CartItem> cartItems;
    private final Runnable onCartChangedCallback;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    public CartAdapter(List<CartItem> cartItems, Runnable onCartChangedCallback) {
        this.cartItems = cartItems;
        this.onCartChangedCallback = onCartChangedCallback;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView productImageView;
        TextView productNameTextView, productPriceTextView, quantityTextView;
        ImageButton increaseQuantityButton, decreaseQuantityButton, deleteButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.productImageView);
            productNameTextView = itemView.findViewById(R.id.productNameTextView);
            productPriceTextView = itemView.findViewById(R.id.productPriceTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            increaseQuantityButton = itemView.findViewById(R.id.increaseQuantityButton);
            decreaseQuantityButton = itemView.findViewById(R.id.decreaseQuantityButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        public void bind(final CartItem cartItem) {
            if (currentUser == null) return; // Should not happen if user is in CartActivity

            productNameTextView.setText(cartItem.getProductName());
            quantityTextView.setText(String.valueOf(cartItem.getQuantity()));

            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            productPriceTextView.setText(formatter.format(cartItem.getProductPrice()));

            int imageId = itemView.getContext().getResources().getIdentifier(cartItem.getProductImage(), "drawable", itemView.getContext().getPackageName());
            productImageView.setImageResource(imageId != 0 ? imageId : R.drawable.ic_launcher_background);

            increaseQuantityButton.setOnClickListener(v -> {
                db.collection("users").document(currentUser.getUid()).collection("cart").document(cartItem.getId())
                        .update("quantity", cartItem.getQuantity() + 1)
                        .addOnSuccessListener(aVoid -> onCartChangedCallback.run())
                        .addOnFailureListener(e -> Toast.makeText(itemView.getContext(), "Lỗi", Toast.LENGTH_SHORT).show());
            });

            decreaseQuantityButton.setOnClickListener(v -> {
                if (cartItem.getQuantity() > 1) {
                    db.collection("users").document(currentUser.getUid()).collection("cart").document(cartItem.getId())
                            .update("quantity", cartItem.getQuantity() - 1)
                            .addOnSuccessListener(aVoid -> onCartChangedCallback.run());
                } else {
                    deleteItem(cartItem);
                }
            });

            deleteButton.setOnClickListener(v -> deleteItem(cartItem));
        }

        private void deleteItem(CartItem cartItem) {
             if (currentUser == null) return;
            db.collection("users").document(currentUser.getUid()).collection("cart").document(cartItem.getId()).delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(itemView.getContext(), "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
                        onCartChangedCallback.run();
                    })
                    .addOnFailureListener(e -> Toast.makeText(itemView.getContext(), "Lỗi khi xóa", Toast.LENGTH_SHORT).show());
        }
    }
}
