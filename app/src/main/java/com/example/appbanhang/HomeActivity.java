package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // --- Toolbar --- 
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        ImageView categoriesIcon = findViewById(R.id.categories_icon);

        categoriesIcon.setOnClickListener(v -> {
            Toast.makeText(HomeActivity.this, "Categories icon clicked!", Toast.LENGTH_SHORT).show();
        });

        topAppBar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_notifications) {
                startActivity(new Intent(HomeActivity.this, NotiActivity.class));
                return true;
            } else if (itemId == R.id.action_help) {
                startActivity(new Intent(HomeActivity.this, HelpActivity.class));
                return true;
            } else if (itemId == R.id.action_logout) {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });

        // --- GridView --- 
        List<Product> productList = new ArrayList<>();
        productList.add(new Product(1, "Laptop Pro", 25000000, R.drawable.pic1));
        productList.add(new Product(2, "Iphone 15", 15000000, R.drawable.pic2));
        productList.add(new Product(3, "Tablet S", 10000000, R.drawable.pic3));
        productList.add(new Product(4, "K10 RGB Gaming Headset", 5000000, R.drawable.pic4));
        productList.add(new Product(5, "Logitech G502 Wireless Mouse", 800000, R.drawable.pic5));
        productList.add(new Product(6, "Mechanical Keyboard", 2500000, R.drawable.pic6));
        productList.add(new Product(7, "Laptop Pro", 25000000, R.drawable.pic1));
        productList.add(new Product(8, "Iphone 15", 15000000, R.drawable.pic2));
        productList.add(new Product(9, "Tablet S", 10000000, R.drawable.pic3));
        productList.add(new Product(10, "K10 RGB Gaming Headset", 5000000, R.drawable.pic4));
        productList.add(new Product(11, "Logitech G502 Wireless Mouse", 800000, R.drawable.pic5));
        productList.add(new Product(12, "Mechanical Keyboard", 2500000, R.drawable.pic6));

        GridView gridView = findViewById(R.id.gridView);
        GridAdapter adapter = new GridAdapter(this, productList);
        gridView.setAdapter(adapter);

        // --- Bắt đầu phần xử lý sự kiện click cho GridView với hiệu ứng ---
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Product selectedProduct = productList.get(position);
            Intent intent = new Intent(HomeActivity.this, ProductDetailActivity.class);
            intent.putExtra("PRODUCT_DETAIL", selectedProduct);

            // Tìm ImageView bên trong item được click
            ImageView productImageView = view.findViewById(R.id.gridImage);

            // Tạo hiệu ứng chuyển cảnh
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    HomeActivity.this,
                    productImageView,
                    "product_image_transition");

            // Khởi chạy Activity với hiệu ứng
            startActivity(intent, options.toBundle());
        });
        // --- Kết thúc phần xử lý sự kiện click cho GridView ---
    }
}
