package com.example.appbanhang;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class FilterBottomSheetFragment extends BottomSheetDialogFragment {

    public static final String TAG = "FilterBottomSheet";

    public enum SortOption {
        DEFAULT,
        PRICE_ASC,
        PRICE_DESC,
        BEST_SELLING
    }

    private FilterListener listener;

    public interface FilterListener {
        void onFilterSelected(SortOption sortOption);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // A common pattern is to have the hosting Fragment implement the listener
        if (getParentFragment() instanceof FilterListener) {
            listener = (FilterListener) getParentFragment();
        } else {
            throw new RuntimeException(context.toString() + " must implement FilterListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RadioGroup filterGroup = view.findViewById(R.id.filter_group);
        filterGroup.setOnCheckedChangeListener((group, checkedId) -> {
            SortOption selectedOption = SortOption.DEFAULT;
            if (checkedId == R.id.filter_price_asc) {
                selectedOption = SortOption.PRICE_ASC;
            } else if (checkedId == R.id.filter_price_desc) {
                selectedOption = SortOption.PRICE_DESC;
            } else if (checkedId == R.id.filter_best_selling) {
                selectedOption = SortOption.BEST_SELLING;
            }

            if (listener != null) {
                listener.onFilterSelected(selectedOption);
            }
            dismiss(); // Automatically close the bottom sheet after selection
        });
    }
}
