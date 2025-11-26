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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AddressListActivity extends AppCompatActivity implements AddressAdapter.OnAddressDeleteListener {

    private static final String TAG = "AddressListActivity";

    private RecyclerView addressesRecyclerView;
    private AddressAdapter addressAdapter;
    private List<Address> addressList = new ArrayList<>();
    private FloatingActionButton fabAddAddress;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_list);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
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
        if (currentUser == null) {
            Log.w(TAG, "No user logged in");
            return;
        }

        CollectionReference addressesRef = db.collection("users")
                .document(currentUser.getUid())
                .collection("addresses");

        addressesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                addressList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Address address = document.toObject(Address.class);
                    address.setId(document.getId()); // Set the document ID
                    addressList.add(address);
                }
                addressAdapter.updateAddresses(addressList);
            } else {
                Log.e(TAG, "Error loading addresses: ", task.getException());
            }
        });
    }

    @Override
    public void onDeleteAddress(Address address) {
        if (currentUser == null || address.getId() == null) {
            Toast.makeText(this, "Không thể xóa địa chỉ", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(currentUser.getUid())
                .collection("addresses").document(address.getId())
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
}
