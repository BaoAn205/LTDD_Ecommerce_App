package com.example.appbanhang;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.util.Log;
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

    @Override
    protected void onStart() {
        super.onStart();
        // Mã ở đây sẽ chạy khi activity bắt đầu được hiển thị.
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Mã ở đây sẽ chạy khi activity đã sẵn sàng để người dùng tương tác.
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Mã ở đây sẽ chạy khi activity bị tạm dừng (ví dụ: có một activity khác che lên).
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Mã ở đây sẽ chạy khi activity không còn được hiển thị trên màn hình.
    }

    /**
     * Phương thức này được gọi khi người dùng quay trở lại Activity này
     * từ một Activity khác (ví dụ: từ màn hình Đăng ký quay lại).
     * Đây là nơi tốt nhất để xóa các trường nhập liệu để đảm bảo sự "sạch sẽ"
     * cho phiên đăng nhập mới.
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        // Xóa văn bản trong các trường EditText
        inputUsername.setText("");
        inputPassword.setText("");
        // Đặt con trỏ vào trường username để người dùng có thể nhập ngay
        inputUsername.requestFocus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Mã ở đây sẽ chạy ngay trước khi activity bị hủy.
    }
}
