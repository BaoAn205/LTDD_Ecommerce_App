package com.example.appbanhang;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        // Set the navigation icon click listener to finish the activity
        toolbar.setNavigationOnClickListener(v -> finish());
    }
}
