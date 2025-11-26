package com.example.appbanhang;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

public class AddressFormActivity extends AppCompatActivity {

    private EditText inputReceiverName, inputPhoneNumber, inputStreetAddress, inputCity;
    private Switch switchDefaultAddress;
    private Button btnSaveAddress;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_form);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        inputReceiverName = findViewById(R.id.inputReceiverName);
        inputPhoneNumber = findViewById(R.id.inputPhoneNumber);
        inputStreetAddress = findViewById(R.id.inputStreetAddress);
        inputCity = findViewById(R.id.inputCity);
        switchDefaultAddress = findViewById(R.id.switchDefaultAddress);
        btnSaveAddress = findViewById(R.id.btnSaveAddress);

        btnSaveAddress.setOnClickListener(v -> saveAddress());
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

        CollectionReference addressesRef = db.collection("users").document(currentUser.getUid()).collection("addresses");
        Address newAddress = new Address(receiverName, phoneNumber, streetAddress, city, isDefault);

        if (isDefault) {
            // If this new address is set to default, we must unset any other default address.
            Query query = addressesRef.whereEqualTo("default", true);
            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    WriteBatch batch = db.batch();
                    task.getResult().forEach(documentSnapshot -> {
                        batch.update(documentSnapshot.getReference(), "default", false);
                    });
                    // Add the new address and commit the batch
                    batch.set(addressesRef.document(), newAddress);
                    batch.commit().addOnSuccessListener(aVoid -> {
                        Toast.makeText(AddressFormActivity.this, "Đã lưu địa chỉ mặc định", Toast.LENGTH_SHORT).show();
                        finish();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(AddressFormActivity.this, "Lỗi khi lưu địa chỉ", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Toast.makeText(AddressFormActivity.this, "Lỗi khi cập nhật địa chỉ mặc định", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Simply add the new address
            addressesRef.add(newAddress).addOnSuccessListener(documentReference -> {
                Toast.makeText(AddressFormActivity.this, "Đã lưu địa chỉ", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(AddressFormActivity.this, "Lỗi khi lưu địa chỉ", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
