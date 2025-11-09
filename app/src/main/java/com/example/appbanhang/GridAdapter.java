package com.example.appbanhang;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

// 1. Kế thừa từ ArrayAdapter<Product> để làm việc trực tiếp với Product
public class GridAdapter extends ArrayAdapter<Product> {

    // 2. Constructor bây giờ nhận vào một List<Product>
    public GridAdapter(@NonNull Context context, @NonNull List<Product> products) {
        // Gọi constructor của lớp cha, truyền vào resource layout là 0 vì ta sẽ tự định nghĩa trong getView
        super(context, 0, products);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Tối ưu hóa việc tạo View bằng cách tái sử dụng convertView
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.grid_item, parent, false);
        }

        // 3. Lấy đối tượng Product tại vị trí hiện tại
        Product currentProduct = getItem(position);

        // Ánh xạ các View trong layout grid_item.xml
        ImageView imageView = listItemView.findViewById(R.id.gridImage);
        TextView textView = listItemView.findViewById(R.id.gridText);

        // 4. Gán dữ liệu từ đối tượng Product vào View
        if (currentProduct != null) {
            imageView.setImageResource(currentProduct.getImage());
            textView.setText(currentProduct.getName());
            // Nếu bạn muốn hiển thị giá, bạn cũng có thể thêm một TextView cho giá
            // và gán dữ liệu ở đây: textViewPrice.setText(String.valueOf(currentProduct.getPrice()));
        }

        return listItemView;
    }
}
