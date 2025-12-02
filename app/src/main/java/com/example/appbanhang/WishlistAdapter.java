package com.example.appbanhang;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder> {

    private final Context context;
    private final List<Product> productList;
    private OnItemDeleteListener deleteListener;

    public interface OnItemDeleteListener {
        void onDeleteClick(Product product);
    }

    public void setOnItemDeleteListener(OnItemDeleteListener listener) {
        this.deleteListener = listener;
    }

    public WishlistAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public WishlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wishlist, parent, false);
        return new WishlistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product, deleteListener);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class WishlistViewHolder extends RecyclerView.ViewHolder {
        ImageView productImageView;
        TextView productNameTextView, productPriceTextView;
        ImageButton deleteButton;

        WishlistViewHolder(@NonNull View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.productImageView);
            productNameTextView = itemView.findViewById(R.id.productNameTextView);
            productPriceTextView = itemView.findViewById(R.id.productPriceTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        void bind(final Product product, final OnItemDeleteListener listener) {
            productNameTextView.setText(product.getName());

            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            productPriceTextView.setText(formatter.format(product.getPrice()));

            // You might need a more robust way to load images, e.g., using Glide or Picasso
            int imageId = itemView.getContext().getResources().getIdentifier(product.getImage(), "drawable", itemView.getContext().getPackageName());
            if (imageId != 0) {
                productImageView.setImageResource(imageId);
            } else {
                productImageView.setImageResource(R.drawable.ic_launcher_background); // Default image
            }

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(product);
                }
            });
        }
    }
}
