package com.example.appbanhang; // Đảm bảo đúng tên package của bạn

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText inputEmail;
    private EditText inputFullName;
    private EditText inputUsername;
    private EditText inputPassword;
    private Button registerButton;
    private TextView logInText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 1. ÁNH XẠ CÁC VIEW (Sử dụng ID cũ của bạn)
        inputEmail = findViewById(R.id.inputName1);      // Email
        inputFullName = findViewById(R.id.inputName2);   // Full Name
        inputUsername = findViewById(R.id.inputName3);   // Username
        inputPassword = findViewById(R.id.inputName4);   // Password
        registerButton = findViewById(R.id.registerButton);
        logInText = findViewById(R.id.logInText);

        // 2. XỬ LÝ NÚT ĐĂNG KÝ
        if (registerButton != null) {
            registerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performRegistration();
                }
            });
        }

        // 3. XỬ LÝ NÚT CHUYỂN VỀ ĐĂNG NHẬP
        if (logInText != null) {
            logInText.setOnClickListener(v -> {
                // Giả sử MainActivity là trang Đăng nhập
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }

    private void performRegistration() {
        String email = inputEmail.getText().toString().trim();
        String fullName = inputFullName.getText().toString().trim();
        String username = inputUsername.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        // Kiểm tra rỗng cơ bản
        if (email.isEmpty() || fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ tất cả các trường!", Toast.LENGTH_LONG).show();
            return;
        }

        // Logic đăng ký thành công (thay bằng API/DB sau)
        Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_LONG).show();

        // Chuyển người dùng về trang Đăng nhập
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}