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

public class GridAdapter extends ArrayAdapter<Product> {

    public GridAdapter(@NonNull Context context, @NonNull List<Product> products) {
        super(context, 0, products);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.grid_item, parent, false);
        }

        Product currentProduct = getItem(position);

        ImageView imageView = listItemView.findViewById(R.id.gridImage);
        TextView textView = listItemView.findViewById(R.id.gridText);

        if (currentProduct != null) {
            // Lấy ID ảnh từ tên file (String) trong drawable
            int imageId = getContext().getResources().getIdentifier(currentProduct.getImage(), "drawable", getContext().getPackageName());
            
            // Kiểm tra xem có tìm thấy ảnh không trước khi thiết lập
            if (imageId != 0) {
                imageView.setImageResource(imageId);
            } else {
                // Đặt một ảnh mặc định nếu không tìm thấy
                imageView.setImageResource(R.drawable.ic_launcher_background); 
            }
            
            textView.setText(currentProduct.getName());
        }

        return listItemView;
    }
}
