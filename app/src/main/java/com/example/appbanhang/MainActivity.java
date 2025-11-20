package com.example.appbanhang;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView; // <<<< BẮT BUỘC PHẢI IMPORT NÀY
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // Khai báo biến (Nếu bạn đã khai báo trước đó, cần đảm bảo không có Button nào được khai báo cho register)
    private EditText inputUsername, inputPassword;
    // (Không cần khai báo ở đây nếu bạn chỉ khai báo trong onCreate)


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        inputUsername = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);

        // 1. ÁNH XẠ NÚT ĐĂNG NHẬP (BUTTON) - Vẫn giữ là Button
        Button loginButton = findViewById(R.id.loginButton);

        // 2. ÁNH XẠ NÚT ĐĂNG KÝ (TEXTVIEW) - ĐÃ SỬA LỖI ClassCastException
        TextView registerButton = findViewById(R.id.registerButton);

        // Logic xử lý khi nhấn nút ĐĂNG NHẬP
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = inputUsername.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                // Kiểm tra rỗng
                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this,
                            "Vui lòng nhập đầy đủ Username và Password!",
                            Toast.LENGTH_SHORT).show();
                }
                else if (username.equals("admin") && (password.equals("admin"))) {
                    // Khi bấm nút, chuyển sang HomeActivity
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(intent);
                    // Tùy chọn: finish() để ngăn người dùng quay lại màn hình đăng nhập
                    // finish();
                }
                else {
                    Toast.makeText(MainActivity.this,
                            "Wrong username & password!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Logic xử lý khi nhấn liên kết ĐĂNG KÝ
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}