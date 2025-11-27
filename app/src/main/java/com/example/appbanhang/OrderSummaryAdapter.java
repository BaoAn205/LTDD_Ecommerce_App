package com.example.appbanhang;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderSummaryAdapter extends RecyclerView.Adapter<OrderSummaryAdapter.ViewHolder> {

    private final List<CartItem> items;

    public OrderSummaryAdapter(List<CartItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_summary, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productNameTextView, quantityTextView, itemTotalTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameTextView = itemView.findViewById(R.id.productNameTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            itemTotalTextView = itemView.findViewById(R.id.itemTotalTextView);
        }

        void bind(CartItem item) {
            productNameTextView.setText(item.getProductName());
            quantityTextView.setText("Số lượng: " + item.getQuantity());

            double totalItemPrice = item.getProductPrice() * item.getQuantity();
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            itemTotalTextView.setText(formatter.format(totalItemPrice));
        }
    }
}
