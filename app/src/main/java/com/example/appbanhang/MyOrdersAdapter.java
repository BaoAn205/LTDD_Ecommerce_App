package com.example.appbanhang;

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

public class MyOrdersAdapter extends RecyclerView.Adapter<MyOrdersAdapter.OrderViewHolder> {

    private final List<Order> orderList;

    public MyOrdersAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(orderList.get(position));
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        // Thêm TextView cho danh sách sản phẩm
        TextView orderIdTextView, orderDateTextView, orderTotalTextView, orderStatusTextView, orderItemsTextView;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTextView = itemView.findViewById(R.id.order_id_textview);
            orderDateTextView = itemView.findViewById(R.id.order_date_textview);
            orderTotalTextView = itemView.findViewById(R.id.order_total_textview);
            orderStatusTextView = itemView.findViewById(R.id.order_status_textview);
            // Tìm TextView mới bằng ID
            orderItemsTextView = itemView.findViewById(R.id.order_items_textview);
        }

        void bind(Order order) {
            orderIdTextView.setText("Mã đơn hàng: #..." + itemView.getContext().getString(R.string.app_name) + getAdapterPosition());

            if (order.getOrderDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                orderDateTextView.setText("Ngày đặt: " + sdf.format(order.getOrderDate()));
            } else {
                orderDateTextView.setText("Ngày đặt: N/A");
            }

            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            orderTotalTextView.setText("Tổng tiền: " + formatter.format(order.getTotalPrice()));

            orderStatusTextView.setText(order.getStatus());

            // Logic mới để hiển thị danh sách sản phẩm
            List<CartItem> items = order.getItems();
            if (items != null && !items.isEmpty()) {
                StringBuilder itemsBuilder = new StringBuilder();
                for (int i = 0; i < items.size(); i++) {
                    CartItem item = items.get(i);
                    if (item != null && item.getProductName() != null) {
                        itemsBuilder.append(i + 1)
                                .append(". ")
                                .append(item.getProductName());
                        if (i < items.size() - 1) {
                            itemsBuilder.append("\n"); // Xuống dòng cho sản phẩm tiếp theo
                        }
                    }
                }
                orderItemsTextView.setText(itemsBuilder.toString());
                orderItemsTextView.setVisibility(View.VISIBLE);
            } else {
                // Ẩn đi nếu không có sản phẩm nào
                orderItemsTextView.setVisibility(View.GONE);
            }
        }
    }
}
