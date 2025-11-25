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
        TextView productDescriptionView = findViewById(R.id.product_detail_description);
        Button backButton = findViewById(R.id.back_button);

        // Lấy dữ liệu Product được gửi qua Intent
        Product product = (Product) getIntent().getSerializableExtra("PRODUCT_DETAIL");

        // Kiểm tra xem product có null không và hiển thị dữ liệu
        if (product != null) {
            int imageId = getResources().getIdentifier(product.getImage(), "drawable", getPackageName());
            productImageView.setImageResource(imageId);
            
            productNameView.setText(product.getName());
            productDescriptionView.setText(product.getDescription());

            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            productPriceView.setText(formatter.format(product.getPrice()));

        } else {
            Toast.makeText(this, "Không tìm thấy thông tin sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Xử lý sự kiện cho nút quay lại với hiệu ứng
        backButton.setOnClickListener(v -> {
            supportFinishAfterTransition();
        });
    }

    // Ghi đè nút back vật lý để cũng có hiệu ứng
    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
        super.onBackPressed();
    }
}
