package com.example.appbanhang;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editFullName, editUsername, editNewPassword, editConfirmPassword;
    private Button btnSaveProfile;
    private MaterialToolbar toolbar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        toolbar = findViewById(R.id.toolbar);
        editFullName = findViewById(R.id.edit_profile_fullname);
        editUsername = findViewById(R.id.edit_profile_username); // Find the new EditText
        editNewPassword = findViewById(R.id.edit_profile_new_password);
        editConfirmPassword = findViewById(R.id.edit_profile_confirm_password);
        btnSaveProfile = findViewById(R.id.btn_save_profile);

        setupToolbar();
        loadCurrentUserData();

        btnSaveProfile.setOnClickListener(v -> saveProfileChanges());
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadCurrentUserData() {
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String fullName = documentSnapshot.getString("fullName");
                            String username = documentSnapshot.getString("username"); // Load username
                            editFullName.setText(fullName);
                            editUsername.setText(username); // Set username
                        }
                    });
        }
    }

    private void saveProfileChanges() {
        String newFullName = editFullName.getText().toString().trim();
        String newUsername = editUsername.getText().toString().trim(); // Get new username
        String newPassword = editNewPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(newFullName)) {
            editFullName.setError("Họ tên không được để trống");
            return;
        }
        if (TextUtils.isEmpty(newUsername)) {
            editUsername.setError("Username không được để trống");
            return;
        }

        // Update user info in Firestore
        updateUserInfoInFirestore(newFullName, newUsername);

        // Update Password if provided
        if (!TextUtils.isEmpty(newPassword)) {
            if (!newPassword.equals(confirmPassword)) {
                editConfirmPassword.setError("Mật khẩu không khớp");
                return;
            }
            if (newPassword.length() < 6) {
                editNewPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
                return;
            }
            updatePassword(newPassword);
        } else {
            // If only user info is updated and password fields are empty
            Toast.makeText(this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void updateUserInfoInFirestore(String newFullName, String newUsername) {
        if (currentUser != null) {
            DocumentReference userDoc = db.collection("users").document(currentUser.getUid());
            Map<String, Object> updates = new HashMap<>();
            updates.put("fullName", newFullName);
            updates.put("username", newUsername);

            userDoc.update(updates)
                    .addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, "Lỗi khi cập nhật thông tin", Toast.LENGTH_SHORT).show());
        }
    }

    private void updatePassword(String newPassword) {
        if (currentUser != null) {
            currentUser.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditProfileActivity.this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
                            finish(); // Finish activity after all updates are done
                        } else {
                            Toast.makeText(EditProfileActivity.this, "Lỗi khi cập nhật mật khẩu: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }
}
