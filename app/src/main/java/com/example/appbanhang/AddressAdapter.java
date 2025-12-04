package com.example.appbanhang;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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

    public interface OnAddressInteractionListener {
        void onDeleteAddress(Address address);
        void onSetDefaultAddress(Address address);
    }

    private List<Address> addressList;
    private OnAddressInteractionListener listener;

    public AddressAdapter(List<Address> addressList, OnAddressInteractionListener listener) {
        this.addressList = addressList;
        this.listener = listener;
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
        holder.bind(address, listener);
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

        public void bind(final Address address, final OnAddressInteractionListener listener) {
            String nameAndPhone = address.getReceiverName() + " | " + address.getPhoneNumber();
            receiverNameAndPhone.setText(nameAndPhone);
            streetAddress.setText(address.getStreetAddress());
            cityAddress.setText(address.getCity());

            defaultAddressChip.setVisibility(address.isDefault() ? View.VISIBLE : View.GONE);

            deleteAddressButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteAddress(address);
                }
            });

            itemView.setOnLongClickListener(v -> {
                showOptionsDialog(itemView.getContext(), address, listener);
                return true;
            });
        }

        private void showOptionsDialog(Context context, Address address, OnAddressInteractionListener listener) {
            final CharSequence[] options = {"Đặt làm địa chỉ mặc định", "Chỉnh sửa địa chỉ"};

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Tùy chọn cho địa chỉ");
            builder.setItems(options, (dialog, item) -> {
                if (options[item].equals("Đặt làm địa chỉ mặc định")) {
                    if (listener != null) {
                        listener.onSetDefaultAddress(address);
                    }
                } else if (options[item].equals("Chỉnh sửa địa chỉ")) {
                    Intent intent = new Intent(context, AddressFormActivity.class);
                    intent.putExtra("EDIT_ADDRESS_ID", address.getId());
                    context.startActivity(intent);
                }
            });
            builder.show();
        }
    }
}
