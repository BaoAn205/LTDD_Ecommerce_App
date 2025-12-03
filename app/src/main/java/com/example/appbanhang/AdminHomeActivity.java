package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;

public class AdminHomeActivity extends AppCompatActivity {

    private CardView manageProductsCard, manageUsersCard, manageOrdersCard;
    private Button adminLogoutButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        mAuth = FirebaseAuth.getInstance();

        // Initialize Cards
        manageProductsCard = findViewById(R.id.manageProductsCard);
        manageUsersCard = findViewById(R.id.manageUsersCard);
        manageOrdersCard = findViewById(R.id.manageOrdersCard); // Find the new card

        // Initialize Button
        adminLogoutButton = findViewById(R.id.adminLogoutButton);

        // Set Click Listeners
        manageProductsCard.setOnClickListener(v -> {
            startActivity(new Intent(AdminHomeActivity.this, ManageProductsActivity.class));
        });

        manageUsersCard.setOnClickListener(v -> {
            startActivity(new Intent(AdminHomeActivity.this, AdminUserManagementActivity.class));
        });

        // Add click listener for the new card
        manageOrdersCard.setOnClickListener(v -> {
            startActivity(new Intent(AdminHomeActivity.this, AdminManageOrdersActivity.class));
        });

        adminLogoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(AdminHomeActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
