package com.example.appbanhang;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

public class AddressFormActivity extends AppCompatActivity {

    private EditText inputReceiverName, inputPhoneNumber, inputStreetAddress, inputCity;
    private SwitchMaterial switchDefaultAddress;
    private Button btnSaveAddress;
    private MaterialToolbar toolbar;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private CollectionReference addressesRef;
    
    private String editingAddressId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_form);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            addressesRef = db.collection("users").document(currentUser.getUid()).collection("addresses");
        }

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        inputReceiverName = findViewById(R.id.inputReceiverName);
        inputPhoneNumber = findViewById(R.id.inputPhoneNumber);
        inputStreetAddress = findViewById(R.id.inputStreetAddress);
        inputCity = findViewById(R.id.inputCity);
        switchDefaultAddress = findViewById(R.id.switchDefaultAddress);
        btnSaveAddress = findViewById(R.id.btnSaveAddress);

        if (getIntent().hasExtra("EDIT_ADDRESS_ID")) {
            editingAddressId = getIntent().getStringExtra("EDIT_ADDRESS_ID");
            loadAddressData();
            toolbar.setTitle("Chỉnh sửa địa chỉ");
        } else {
            toolbar.setTitle("Thêm địa chỉ mới");
        }

        btnSaveAddress.setOnClickListener(v -> saveAddress());
    }

    private void loadAddressData() {
        addressesRef.document(editingAddressId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Address address = documentSnapshot.toObject(Address.class);
                if (address != null) {
                    inputReceiverName.setText(address.getReceiverName());
                    inputPhoneNumber.setText(address.getPhoneNumber());
                    inputStreetAddress.setText(address.getStreetAddress());
                    inputCity.setText(address.getCity());
                    switchDefaultAddress.setChecked(address.isDefault());
                }
            }
        });
    }

    private void saveAddress() {
        String receiverName = inputReceiverName.getText().toString().trim();
        String phoneNumber = inputPhoneNumber.getText().toString().trim();
        String streetAddress = inputStreetAddress.getText().toString().trim();
        String city = inputCity.getText().toString().trim();
        boolean isDefault = switchDefaultAddress.isChecked();

        if (TextUtils.isEmpty(receiverName) || TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(streetAddress) || TextUtils.isEmpty(city)) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser == null) {
            Toast.makeText(this, "Bạn phải đăng nhập để lưu địa chỉ", Toast.LENGTH_SHORT).show();
            return;
        }

        Address address = new Address();
        address.setReceiverName(receiverName);
        address.setPhoneNumber(phoneNumber);
        address.setStreetAddress(streetAddress);
        address.setCity(city);
        address.setDefault(isDefault);

        if (isDefault) {
            handleDefaultAddress(address);
        } else {
            saveOrUpdateAddress(address);
        }
    }

    private void handleDefaultAddress(Address address) {
        addressesRef.whereEqualTo("isDefault", true).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                WriteBatch batch = db.batch();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if (!document.getId().equals(editingAddressId)) {
                        batch.update(document.getReference(), "isDefault", false);
                    }
                }

                DocumentReference docRef = (editingAddressId != null) ? addressesRef.document(editingAddressId) : addressesRef.document();
                batch.set(docRef, address);
                
                batch.commit().addOnSuccessListener(aVoid -> {
                    Toast.makeText(AddressFormActivity.this, "Đã lưu địa chỉ", Toast.LENGTH_SHORT).show();
                    finish();
                }).addOnFailureListener(e -> Toast.makeText(AddressFormActivity.this, "Lỗi khi lưu địa chỉ", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(AddressFormActivity.this, "Lỗi khi cập nhật địa chỉ mặc định", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveOrUpdateAddress(Address address) {
         if (editingAddressId != null) {
            // Update existing address
            addressesRef.document(editingAddressId).set(address)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AddressFormActivity.this, "Đã cập nhật địa chỉ", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(AddressFormActivity.this, "Lỗi khi cập nhật", Toast.LENGTH_SHORT).show());
        } else {
            // Add new address
            addressesRef.add(address)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddressFormActivity.this, "Đã thêm địa chỉ", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(AddressFormActivity.this, "Lỗi khi thêm địa chỉ", Toast.LENGTH_SHORT).show());
        }
    }
}
