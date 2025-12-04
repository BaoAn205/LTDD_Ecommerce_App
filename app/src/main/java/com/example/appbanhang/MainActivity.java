package com.example.appbanhang;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private Button loginButton;
    private TextView registerButton;
    private FirebaseAuth mAuth;

    // --- ADMIN CREDENTIALS ---
    private static final String ADMIN_EMAIL = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        loginButton.setOnClickListener(v -> loginUser());

        registerButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            goToHomeActivity(false); // Don't check for last viewed product on auto-login
        }
    }

    private void loginUser() {
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            inputEmail.setError("Email không được để trống.");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            inputPassword.setError("Mật khẩu không được để trống.");
            return;
        }

        // --- ADMIN LOGIN CHECK ---
        if (email.equals(ADMIN_EMAIL) && password.equals(ADMIN_PASSWORD)) {
            Toast.makeText(MainActivity.this, "Đăng nhập với tư cách Admin thành công.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, AdminHomeActivity.class);
            startActivity(intent);
            finish();
            return; 
        }

        // --- REGULAR USER LOGIN ---
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Đăng nhập thành công.", Toast.LENGTH_SHORT).show();
                        goToHomeActivity(true); // Check for last viewed product on manual login
                    } else {
                        Toast.makeText(MainActivity.this, "Đăng nhập thất bại: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void goToHomeActivity(boolean checkLastViewed) {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        
        if (checkLastViewed) {
            SharedPreferences prefs = getSharedPreferences(ProductDetailActivity.PREFS_NAME, Context.MODE_PRIVATE);
            String lastViewedProductId = prefs.getString(ProductDetailActivity.LAST_VIEWED_PRODUCT_ID, null);

            if (lastViewedProductId != null) {
                intent.putExtra("SHOW_PRODUCT_ID", lastViewedProductId);
                // Clear the preference so it's only shown once
                prefs.edit().remove(ProductDetailActivity.LAST_VIEWED_PRODUCT_ID).apply();
            }
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
