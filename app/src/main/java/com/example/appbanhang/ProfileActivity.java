package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private TextView userNameTextView, userEmailTextView, logoutButton;
    private ImageView backButton;
    // Thêm biến cho các mục có thể nhấn
    private ConstraintLayout myOrdersSection, addressSection, personalInfoSection;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize Views
        userNameTextView = findViewById(R.id.userName);
        userEmailTextView = findViewById(R.id.userEmail);
        logoutButton = findViewById(R.id.logoutButton);
        backButton = findViewById(R.id.backButton);
        myOrdersSection = findViewById(R.id.ordersSection);
        addressSection = findViewById(R.id.addressSection);
        personalInfoSection = findViewById(R.id.personalInfoSection); // Tìm view của mục thông tin cá nhân

        // Load user information
        loadUserProfile();

        // Set up click listeners
        backButton.setOnClickListener(v -> finish());

        // Sự kiện click cho mục "Thông tin cá nhân"
        personalInfoSection.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        myOrdersSection.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MyOrdersActivity.class);
            startActivity(intent);
        });

        addressSection.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AddressListActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(ProfileActivity.this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            DocumentReference userRef = db.collection("users").document(uid);

            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Get data and set it to TextViews
                        String fullName = document.getString("fullName");
                        String email = document.getString("email");

                        userNameTextView.setText(fullName != null ? fullName : "N/A");
                        userEmailTextView.setText(email != null ? email : "N/A");
                    } else {
                        Log.d(TAG, "No such document");
                        Toast.makeText(ProfileActivity.this, "Không tìm thấy thông tin người dùng.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    Toast.makeText(ProfileActivity.this, "Lỗi khi tải thông tin.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
