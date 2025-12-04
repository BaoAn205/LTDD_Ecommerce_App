package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class AddressListActivity extends AppCompatActivity implements AddressAdapter.OnAddressInteractionListener {

    private static final String TAG = "AddressListActivity";

    private RecyclerView addressesRecyclerView;
    private AddressAdapter addressAdapter;
    private List<Address> addressList = new ArrayList<>();
    private FloatingActionButton fabAddAddress;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private CollectionReference addressesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_list);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        addressesRef = db.collection("users").document(currentUser.getUid()).collection("addresses");

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        addressesRecyclerView = findViewById(R.id.addressesRecyclerView);
        fabAddAddress = findViewById(R.id.fabAddAddress);

        setupRecyclerView();

        fabAddAddress.setOnClickListener(v -> {
            Intent intent = new Intent(AddressListActivity.this, AddressFormActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAddresses();
    }

    private void setupRecyclerView() {
        addressesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        addressAdapter = new AddressAdapter(addressList, this);
        addressesRecyclerView.setAdapter(addressAdapter);
    }

    private void loadAddresses() {
        addressesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                addressList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Address address = document.toObject(Address.class);
                    address.setId(document.getId());
                    addressList.add(address);
                }
                // Sort addresses to show default first
                addressList.sort((a1, a2) -> Boolean.compare(a2.isDefault(), a1.isDefault()));
                addressAdapter.updateAddresses(addressList);
            } else {
                Log.e(TAG, "Error loading addresses: ", task.getException());
            }
        });
    }

    @Override
    public void onDeleteAddress(Address address) {
        if (address.getId() == null) {
            Toast.makeText(this, "Không thể xóa địa chỉ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (address.isDefault()){
            Toast.makeText(this, "Không thể xóa địa chỉ mặc định", Toast.LENGTH_SHORT).show();
            return;
        }

        addressesRef.document(address.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AddressListActivity.this, "Đã xóa địa chỉ", Toast.LENGTH_SHORT).show();
                    loadAddresses(); // Refresh the list
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddressListActivity.this, "Lỗi khi xóa địa chỉ", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error deleting address", e);
                });
    }

    @Override
    public void onSetDefaultAddress(Address newDefaultAddress) {
        if (newDefaultAddress.isDefault()) {
            Toast.makeText(this, "Địa chỉ này đã là mặc định", Toast.LENGTH_SHORT).show();
            return;
        }

        WriteBatch batch = db.batch();

        // 1. Unset the current default address
        addressesRef.whereEqualTo("default", true).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for(QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                batch.update(doc.getReference(), "default", false);
            }

            // 2. Set the new default address
            DocumentReference newDefaultRef = addressesRef.document(newDefaultAddress.getId());
            batch.update(newDefaultRef, "default", true);

            // 3. Commit the batch
            batch.commit().addOnSuccessListener(aVoid -> {
                Toast.makeText(AddressListActivity.this, "Đã đặt làm địa chỉ mặc định", Toast.LENGTH_SHORT).show();
                loadAddresses(); // Refresh the list with new default address
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi khi đặt địa chỉ mặc định", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error setting default address", e);
            });
        });
    }
}
