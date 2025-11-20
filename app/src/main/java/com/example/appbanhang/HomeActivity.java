package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import androidx.recyclerview.widget.RecyclerView; // Thêm import này
import androidx.recyclerview.widget.GridLayoutManager; // Thêm import này
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Ánh xạ các nút trên thanh top bar
        Button Weights = findViewById(R.id.Weights);
        Button Cardio = findViewById(R.id.Cardio);
        Button Apparel = findViewById(R.id.Apparel);
        Button Yoga = findViewById(R.id.Yoga);

        // Nút Home (vì đang ở trang Home, ta có thể reload hoặc làm gì khác)
        Weights.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        // Nút Thông báo → chuyển sang NotiActivity
        Cardio.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, NotiActivity.class);
            startActivity(intent);
        });

        // Nút Hỗ trợ → chuyển sang HelpActivity
        Apparel.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, HelpActivity.class);
            startActivity(intent);
        });

        // Nút Đăng xuất → quay lại trang Login
        Yoga.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // đóng HomeActivity để không quay lại được bằng nút Back
        });

        // --- Bắt đầu phần mã cho GridView (đã được refactor) ---

        // 1. Tạo danh sách sản phẩm
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


        // 2. Liên kết GridView trong XML
        GridView gridView = findViewById(R.id.gridView);

        // 3. Tạo adapter với danh sách sản phẩm
        GridAdapter adapter = new GridAdapter(this, productList);

        // 4. Gán adapter cho GridView
        gridView.setAdapter(adapter);

        // --- Kết thúc phần mã cho GridView ---
    }
}
