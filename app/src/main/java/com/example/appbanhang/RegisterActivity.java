package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText inputEmail, inputFullName, inputUsername, inputPassword;
    private Button registerButton;
    private TextView logInText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Ánh xạ Views (giữ nguyên ID cũ của bạn để nhất quán)
        inputEmail = findViewById(R.id.inputName1);
        inputFullName = findViewById(R.id.inputName2);
        inputUsername = findViewById(R.id.inputName3);
        inputPassword = findViewById(R.id.inputName4);
        registerButton = findViewById(R.id.registerButton);
        logInText = findViewById(R.id.logInText);

        // Xử lý nút Đăng ký
        registerButton.setOnClickListener(v -> performRegistration());

        // Xử lý nút chuyển về Đăng nhập
        logInText.setOnClickListener(v -> {
            finish(); // Chỉ cần đóng activity hiện tại
        });
    }

    private void performRegistration() {
        String email = inputEmail.getText().toString().trim();
        String fullName = inputFullName.getText().toString().trim();
        String username = inputUsername.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        // Kiểm tra đầu vào
        if (TextUtils.isEmpty(email)) {
            inputEmail.setError("Email không được để trống.");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            inputPassword.setError("Mật khẩu không được để trống.");
            return;
        }
        if (password.length() < 6) {
            inputPassword.setError("Mật khẩu phải có ít nhất 6 ký tự.");
            return;
        }

        // Bắt đầu quá trình tạo tài khoản trên Firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Đăng ký thành công, bây giờ lưu thông tin bổ sung vào Firestore
                        String userId = mAuth.getCurrentUser().getUid();

                        // Tạo một đối tượng Map để lưu trữ thông tin người dùng
                        Map<String, Object> user = new HashMap<>();
                        user.put("fullName", fullName);
                        user.put("username", username);
                        user.put("email", email);

                        // Lưu vào Firestore trong collection "users" với ID của người dùng
                        db.collection("users").document(userId).set(user)
                                .addOnSuccessListener(aVoid -> {
                                    // Lưu thông tin thành công
                                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_LONG).show();
                                    finish(); // Quay lại trang đăng nhập
                                })
                                .addOnFailureListener(e -> {
                                    // Có lỗi khi lưu thông tin
                                    Toast.makeText(RegisterActivity.this, "Lỗi khi lưu thông tin: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });

                    } else {
                        // Nếu đăng ký thất bại, hiển thị thông báo lỗi
                        Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}