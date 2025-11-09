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
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private EditText inputUsername, inputPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


            inputUsername = findViewById(R.id.inputName1);
            inputPassword = findViewById(R.id.inputName2);

            Button loginButton = findViewById(R.id.supabutton1); // id nút "Đăng nhập"
            Button registerButton = findViewById(R.id.supabutton2);

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
                    }
                    else {
                        Toast.makeText(MainActivity.this,
                                "Wrong username & password!",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });

            registerButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            });
    }
}