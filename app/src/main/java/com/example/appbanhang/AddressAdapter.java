package com.example.appbanhang;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    public interface OnAddressDeleteListener {
        void onDeleteAddress(Address address);
    }

    private List<Address> addressList;
    private OnAddressDeleteListener deleteListener;

    public AddressAdapter(List<Address> addressList, OnAddressDeleteListener deleteListener) {
        this.addressList = addressList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = addressList.get(position);
        holder.bind(address, deleteListener);
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    public void updateAddresses(List<Address> newAddresses) {
        this.addressList = newAddresses;
        notifyDataSetChanged();
    }

    static class AddressViewHolder extends RecyclerView.ViewHolder {
        private TextView receiverNameAndPhone, streetAddress, cityAddress;
        private Chip defaultAddressChip;
        private ImageButton deleteAddressButton;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverNameAndPhone = itemView.findViewById(R.id.receiverNameAndPhone);
            streetAddress = itemView.findViewById(R.id.streetAddress);
            cityAddress = itemView.findViewById(R.id.cityAddress);
            defaultAddressChip = itemView.findViewById(R.id.defaultAddressChip);
            deleteAddressButton = itemView.findViewById(R.id.deleteAddressButton);
        }

        public void bind(final Address address, final OnAddressDeleteListener listener) {
            String nameAndPhone = address.getReceiverName() + " | " + address.getPhoneNumber();
            receiverNameAndPhone.setText(nameAndPhone);
            streetAddress.setText(address.getStreetAddress());
            cityAddress.setText(address.getCity());

            if (address.isDefault()) {
                defaultAddressChip.setVisibility(View.VISIBLE);
            } else {
                defaultAddressChip.setVisibility(View.GONE);
            }

            deleteAddressButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteAddress(address);
                }
            });
        }
    }
}
