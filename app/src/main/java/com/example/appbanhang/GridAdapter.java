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

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class GridAdapter extends ArrayAdapter<Product> {

    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

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
        TextView soldCountTextView = listItemView.findViewById(R.id.gridTextSoldCount);
        ImageButton addToCartButton = listItemView.findViewById(R.id.addToCartButton);

        if (currentProduct != null) {
            nameTextView.setText(currentProduct.getName());

            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            priceTextView.setText(formatter.format(currentProduct.getPrice()));

            // **SỬA LẠI LOGIC HIỂN THỊ SỐ LƯỢNG ĐÃ BÁN**
            // Luôn hiển thị, kể cả khi số lượng là 0
            soldCountTextView.setText("Đã bán " + currentProduct.getSoldCount());
            soldCountTextView.setVisibility(View.VISIBLE);

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

        CartManager.getInstance().addProduct(getContext(), currentUser.getUid(), product, 1, new CartManager.CartCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.w("GridAdapter", "Error adding to cart", e);
            }
        });
    }
}
