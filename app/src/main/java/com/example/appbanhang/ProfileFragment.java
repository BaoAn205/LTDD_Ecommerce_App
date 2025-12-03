package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private TextView userNameTextView, userEmailTextView, logoutButton;
    private ConstraintLayout myOrdersSection, addressSection, personalInfoSection, settingsSection;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize Views
        userNameTextView = view.findViewById(R.id.userName);
        userEmailTextView = view.findViewById(R.id.userEmail);
        logoutButton = view.findViewById(R.id.logoutButton);
        myOrdersSection = view.findViewById(R.id.ordersSection);
        addressSection = view.findViewById(R.id.addressSection);
        personalInfoSection = view.findViewById(R.id.personalInfoSection);
        settingsSection = view.findViewById(R.id.settingsSection);

        // Set up click listeners
        setupClickListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load user info every time the fragment is resumed to show latest data
        loadUserProfile();
    }

    private void setupClickListeners() {
        personalInfoSection.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), EditProfileActivity.class));
        });

        myOrdersSection.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), MyOrdersActivity.class));
        });

        addressSection.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AddressListActivity.class));
        });

        settingsSection.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        });

        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
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
                    if (document != null && document.exists()) {
                        String fullName = document.getString("fullName");
                        String email = document.getString("email");

                        userNameTextView.setText(fullName != null ? fullName : "N/A");
                        userEmailTextView.setText(email != null ? email : "N/A");
                    } else {
                        Log.d(TAG, "No such document");
                        if(getContext() != null) Toast.makeText(getContext(), "Không tìm thấy thông tin người dùng.", Toast.LENGTH_SHORT).show();

                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    if(getContext() != null) Toast.makeText(getContext(), "Lỗi khi tải thông tin.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Handle user not logged in case
            userNameTextView.setText("Khách");
            userEmailTextView.setText("Vui lòng đăng nhập");
        }
    }
}
