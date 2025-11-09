package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class NotiActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noti);

        Button back = findViewById(R.id.backhome);
        back.setOnClickListener(v -> {
            Intent intent = new Intent(NotiActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }
}