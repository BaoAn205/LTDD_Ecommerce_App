package com.example.appbanhang;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ExpandableHeightGridView gridView;
    private GridAdapter adapter;
    private List<Product> allProductList;
    private List<Product> displayedProductList;
    private FirebaseFirestore db;
    private SearchView searchView;

    private Button weightsButton, cardioButton, apparelButton, yogaButton;
    private String selectedCategory = null;
    private TextView seeAllButton;
    private ImageButton notificationButton, cartButton;

    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final long DEBOUNCE_DELAY = 500;

    private static final String TAG = "HomeFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupClickListeners();
        setupSearchView();
        setupGridView();
        fetchProductsFromFirestore();
    }

    private void initializeViews(View view) {
        notificationButton = view.findViewById(R.id.notificationButton);
        cartButton = view.findViewById(R.id.cartButton);
        weightsButton = view.findViewById(R.id.Weights);
        cardioButton = view.findViewById(R.id.Cardio);
        apparelButton = view.findViewById(R.id.Apparel);
        yogaButton = view.findViewById(R.id.Yoga);
        seeAllButton = view.findViewById(R.id.seeAllText);
        searchView = view.findViewById(R.id.searchView);
        gridView = view.findViewById(R.id.gridView);
    }

    private void setupClickListeners() {
        notificationButton.setOnClickListener(v -> startActivity(new Intent(getActivity(), NotiActivity.class)));
        cartButton.setOnClickListener(v -> startActivity(new Intent(getActivity(), CartActivity.class)));

        weightsButton.setOnClickListener(v -> onCategorySelected("Weights"));
        cardioButton.setOnClickListener(v -> onCategorySelected("Cardio"));
        apparelButton.setOnClickListener(v -> onCategorySelected("Apparel"));
        yogaButton.setOnClickListener(v -> onCategorySelected("Yoga & Pilates"));
        seeAllButton.setOnClickListener(v -> {
            selectedCategory = null;
            updateCategoryButtonsUI();
            applyFilters();
        });

         gridView.setOnItemClickListener((parent, view, position, id) -> {
            Product selectedProduct = displayedProductList.get(position);
            Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
            intent.putExtra("PRODUCT_DETAIL", selectedProduct);

            ImageView productImageView = view.findViewById(R.id.gridImage);
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    getActivity(), productImageView, "product_image_transition");
            startActivity(intent, options.toBundle());
        });
    }

    private void setupGridView(){
         db = FirebaseFirestore.getInstance();
        allProductList = new ArrayList<>();
        displayedProductList = new ArrayList<>();
        adapter = new GridAdapter(getContext(), displayedProductList);
        gridView.setAdapter(adapter);
    }

    private void fetchProductsFromFirestore() {
        db.collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allProductList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            product.setId(document.getId());
                            allProductList.add(product);
                        }
                        applyFilters(); // Initial filter apply
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    private void setupSearchView() {
        searchRunnable = this::applyFilters;

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchHandler.removeCallbacks(searchRunnable);
                applyFilters();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchHandler.removeCallbacks(searchRunnable);
                searchHandler.postDelayed(searchRunnable, DEBOUNCE_DELAY);
                return true;
            }
        });
    }

    private void onCategorySelected(String category) {
        if (category.equals(selectedCategory)) {
            selectedCategory = null;
        } else {
            selectedCategory = category;
        }
        updateCategoryButtonsUI();
        applyFilters();
    }

    private void updateCategoryButtonsUI() {
        styleButton(weightsButton, "Weights".equals(selectedCategory));
        styleButton(cardioButton, "Cardio".equals(selectedCategory));
        styleButton(apparelButton, "Apparel".equals(selectedCategory));
        styleButton(yogaButton, "Yoga & Pilates".equals(selectedCategory));
    }

    private void styleButton(Button button, boolean isSelected) {
        if (isSelected) {
            button.getBackground().setTint(Color.parseColor("#F8E5D1"));
            button.setTextColor(Color.parseColor("#DE7403"));
            button.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            button.getBackground().setTint(Color.parseColor("#EAEAEA"));
            button.setTextColor(Color.parseColor("#666666"));
            button.setTypeface(Typeface.DEFAULT);
        }
    }

    private void applyFilters() {
        String query = searchView.getQuery().toString().toLowerCase().trim();
        List<Product> filteredList = new ArrayList<>();

        for (Product product : allProductList) {
            boolean categoryMatch = (selectedCategory == null) || 
                                    (product.getCategory() != null && selectedCategory.equalsIgnoreCase(product.getCategory()));

            boolean searchMatch = query.isEmpty() || 
                                  (product.getName() != null && product.getName().toLowerCase().contains(query));

            if (categoryMatch && searchMatch) {
                filteredList.add(product);
            }
        }

        displayedProductList.clear();
        displayedProductList.addAll(filteredList);
        adapter.notifyDataSetChanged();
    }
}
