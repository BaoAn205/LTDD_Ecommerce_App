package com.example.appbanhang;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AdminOrdersAdapter extends RecyclerView.Adapter<AdminOrdersAdapter.OrderViewHolder> {

    private final Context context;
    private final List<Order> orderList;
    private OnOrderItemClickListener listener;

    public interface OnOrderItemClickListener {
        void onItemClick(Order order);
    }

    public void setOnOrderItemClickListener(OnOrderItemClickListener listener) {
        this.listener = listener;
    }

    public AdminOrdersAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order, listener);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdTextView, orderDateTextView, orderTotalTextView, orderStatusTextView;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTextView = itemView.findViewById(R.id.order_id_textview);
            orderDateTextView = itemView.findViewById(R.id.order_date_textview);
            orderTotalTextView = itemView.findViewById(R.id.order_total_textview);
            orderStatusTextView = itemView.findViewById(R.id.order_status_textview);
        }

        void bind(final Order order, final OnOrderItemClickListener listener) {
            // In a real app, you would have a real order ID. Here we use the document ID.
            orderIdTextView.setText("Mã đơn hàng: #" + order.getId().substring(0, 7).toUpperCase());

            if (order.getOrderDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                orderDateTextView.setText("Ngày đặt: " + sdf.format(order.getOrderDate()));
            } else {
                orderDateTextView.setText("Ngày đặt: N/A");
            }

            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            orderTotalTextView.setText("Tổng tiền: " + formatter.format(order.getTotalPrice()));

            orderStatusTextView.setText(order.getStatus());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(order);
                }
            });
        }
    }
}
