package com.example.appbanhang;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize Views
        ImageButton backButton = findViewById(R.id.backButton);
        SwitchMaterial notificationSwitch = findViewById(R.id.notificationSwitch);
        LinearLayout itemLanguage = findViewById(R.id.itemLanguage);
        SwitchMaterial darkModeSwitch = findViewById(R.id.darkModeSwitch);
        LinearLayout itemPrivacy = findViewById(R.id.itemPrivacy);
        LinearLayout itemHelpSupport = findViewById(R.id.itemHelpSupport);

        // Back Button Click Listener
        backButton.setOnClickListener(v -> finish());

        // Language Click Listener
        itemLanguage.setOnClickListener(v -> {
            Toast.makeText(SettingsActivity.this, "Chức năng chọn ngôn ngữ sắp ra mắt!", Toast.LENGTH_SHORT).show();
        });

        // Privacy Click Listener
        itemPrivacy.setOnClickListener(v -> {
            Toast.makeText(SettingsActivity.this, "Mở màn hình Quyền riêng tư", Toast.LENGTH_SHORT).show();
        });

        // Help & Support Click Listener
        itemHelpSupport.setOnClickListener(v -> {
            Toast.makeText(SettingsActivity.this, "Mở màn hình Trợ giúp & Hỗ trợ", Toast.LENGTH_SHORT).show();
        });

        // Notification Switch Listener
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(SettingsActivity.this, "Thông báo đã được bật", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SettingsActivity.this, "Thông báo đã được tắt", Toast.LENGTH_SHORT).show();
            }
        });

        // Dark Mode Switch Listener
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(SettingsActivity.this, "Chế độ tối đã được bật", Toast.LENGTH_SHORT).show();
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                Toast.makeText(SettingsActivity.this, "Chế độ tối đã được tắt", Toast.LENGTH_SHORT).show();
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
    }
}
