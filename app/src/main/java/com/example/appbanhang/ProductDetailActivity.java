package com.example.appbanhang;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Ánh xạ các view từ layout
        ImageView productImageView = findViewById(R.id.product_detail_image);
        TextView productNameView = findViewById(R.id.product_detail_name);
        TextView productPriceView = findViewById(R.id.product_detail_price);
        Button backButton = findViewById(R.id.back_button);

        // Lấy dữ liệu Product được gửi qua Intent
        Product product = (Product) getIntent().getSerializableExtra("PRODUCT_DETAIL");

        // Kiểm tra xem product có null không và hiển thị dữ liệu
        if (product != null) {
            productImageView.setImageResource(product.getImage());
            productNameView.setText(product.getName());

            // Định dạng giá tiền cho đẹp
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            productPriceView.setText(formatter.format(product.getPrice()));

        } else {
            // Xử lý trường hợp không nhận được dữ liệu
            Toast.makeText(this, "Không tìm thấy thông tin sản phẩm", Toast.LENGTH_SHORT).show();
            finish(); // Đóng activity nếu không có dữ liệu
        }

        // Xử lý sự kiện cho nút quay lại
        backButton.setOnClickListener(v -> {
            // Chỉ cần đóng activity hiện tại để quay lại màn hình trước đó (HomeActivity)
            finish();
        });
    }
}
