package com.example.appbanhang;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MyOrdersAdapter extends RecyclerView.Adapter<MyOrdersAdapter.OrderViewHolder> {

    private final List<Order> orderList;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "MyOrdersAdapter";

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

    class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdTextView, orderDateTextView, orderTotalTextView, orderStatusTextView, orderItemsTextView;
        Button cancelOrderButton;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTextView = itemView.findViewById(R.id.order_id_textview);
            orderDateTextView = itemView.findViewById(R.id.order_date_textview);
            orderTotalTextView = itemView.findViewById(R.id.order_total_textview);
            orderStatusTextView = itemView.findViewById(R.id.order_status_textview);
            orderItemsTextView = itemView.findViewById(R.id.order_items_textview);
            cancelOrderButton = itemView.findViewById(R.id.cancel_order_button);
        }

        void bind(Order order) {
            orderIdTextView.setText("Mã đơn hàng: " + order.getId());

            if (order.getOrderDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                orderDateTextView.setText("Ngày đặt: " + sdf.format(order.getOrderDate()));
            } else {
                orderDateTextView.setText("Ngày đặt: N/A");
            }

            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            orderTotalTextView.setText("Tổng tiền: " + formatter.format(order.getTotalPrice()));

            orderStatusTextView.setText(order.getStatus());

            List<CartItem> items = order.getItems();
            if (items != null && !items.isEmpty()) {
                StringBuilder itemsBuilder = new StringBuilder();
                for (int i = 0; i < items.size(); i++) {
                    CartItem item = items.get(i);
                    if (item != null && item.getProductName() != null) {
                        itemsBuilder.append(i + 1).append(". ").append(item.getProductName());
                        if (i < items.size() - 1) {
                            itemsBuilder.append("\n");
                        }
                    }
                }
                orderItemsTextView.setText(itemsBuilder.toString());
                orderItemsTextView.setVisibility(View.VISIBLE);
            } else {
                orderItemsTextView.setVisibility(View.GONE);
            }

            if ("Đang xử lý".equalsIgnoreCase(order.getStatus())) {
                cancelOrderButton.setVisibility(View.VISIBLE);
                cancelOrderButton.setOnClickListener(v -> showCancelDialog(order, itemView.getContext()));
            } else {
                cancelOrderButton.setVisibility(View.GONE);
            }
        }

        private void showCancelDialog(final Order order, Context context) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogView = inflater.inflate(R.layout.dialog_cancel_order, null);
            builder.setView(dialogView);

            RadioGroup reasonGroup = dialogView.findViewById(R.id.cancellation_reason_group);
            EditText otherReasonInput = dialogView.findViewById(R.id.other_reason_input);

            reasonGroup.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.reason_other) {
                    otherReasonInput.setVisibility(View.VISIBLE);
                } else {
                    otherReasonInput.setVisibility(View.GONE);
                }
            });

            builder.setTitle("Hủy đơn hàng")
                   .setPositiveButton("Xác nhận", (dialog, which) -> {
                       int selectedId = reasonGroup.getCheckedRadioButtonId();
                       String reason = "";

                       if (selectedId == -1) {
                           Toast.makeText(context, "Vui lòng chọn một lý do", Toast.LENGTH_SHORT).show();
                           return;
                       }

                       if (selectedId == R.id.reason_other) {
                           reason = otherReasonInput.getText().toString().trim();
                           if (reason.isEmpty()) {
                               Toast.makeText(context, "Vui lòng nhập lý do khác", Toast.LENGTH_SHORT).show();
                               return;
                           }
                       } else {
                           RadioButton selectedRadioButton = dialogView.findViewById(selectedId);
                           reason = selectedRadioButton.getText().toString();
                       }

                       cancelOrder(order, reason, context);
                   })
                   .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
            
            builder.create().show();
        }

        private void cancelOrder(Order order, String reason, Context context) {
            db.collection("orders").document(order.getId())
                .update("status", "Đã hủy bởi user", "cancellationReason", reason)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show();
                    order.setStatus("Đã hủy bởi user");
                    order.setCancellationReason(reason);
                    createCancellationNotification(order);
                    notifyItemChanged(getAdapterPosition());
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Lỗi khi hủy đơn hàng", Toast.LENGTH_SHORT).show());
        }

        private void createCancellationNotification(Order order) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) return;

            String title = "Đơn hàng đã được hủy";
            String message = "Bạn đã hủy đơn hàng #" + order.getId().substring(0, 5).toUpperCase() + ".";
            Notification notification = new Notification(title, message);

            db.collection("users").document(currentUser.getUid()).collection("notifications")
                .add(notification)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "Cancellation notification created."))
                .addOnFailureListener(e -> Log.w(TAG, "Error creating cancellation notification", e));
        }
    }
}
