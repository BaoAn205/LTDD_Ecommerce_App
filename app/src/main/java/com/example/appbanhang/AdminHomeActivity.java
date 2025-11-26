package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;

public class AdminHomeActivity extends AppCompatActivity {

    private CardView manageProductsCard, manageUsersCard;
    private Button adminLogoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        // Ánh xạ các view mới
        manageProductsCard = findViewById(R.id.manageProductsCard);
        manageUsersCard = findViewById(R.id.manageUsersCard);
        adminLogoutButton = findViewById(R.id.adminLogoutButton);

        // Xử lý sự kiện cho các nút
        manageProductsCard.setOnClickListener(v -> {
            // Chuyển sang màn hình Quản lý Sản phẩm
            Intent intent = new Intent(AdminHomeActivity.this, ManageProductsActivity.class);
            startActivity(intent);
        });

        manageUsersCard.setOnClickListener(v -> {
            // Chuyển sang màn hình Quản lý Người dùng
            Intent intent = new Intent(AdminHomeActivity.this, AdminUserManagementActivity.class);
            startActivity(intent);
        });

        adminLogoutButton.setOnClickListener(v -> {
            // Đăng xuất khỏi Firebase (nếu cần)
            FirebaseAuth.getInstance().signOut();

            // Quay lại trang Đăng nhập
            Intent intent = new Intent(AdminHomeActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
