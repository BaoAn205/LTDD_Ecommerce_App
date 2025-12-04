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
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment implements FilterBottomSheetFragment.FilterListener {

    private ExpandableHeightGridView gridView;
    private GridAdapter adapter;
    private List<Product> allProductList;
    private List<Product> displayedProductList;
    private FirebaseFirestore db;
    private SearchView searchView;

    private Button weightsButton, cardioButton, apparelButton, yogaButton;
    private String selectedCategory = null;
    private TextView seeAllButton;
    private ImageButton notificationButton, cartButton, filterButton;

    // SLIDER COMPONENTS
    private ViewPager2 imageSlider;
    private ImageSliderAdapter sliderAdapter;
    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;

    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnableDelay;
    private static final long DEBOUNCE_DELAY = 500;
    private static final long SLIDER_DELAY = 5000;

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
        setupSlider(view);
        setupClickListeners();
        setupSearchView();
        setupGridView();
        fetchData();
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
        filterButton = view.findViewById(R.id.filterButton);
        gridView = view.findViewById(R.id.gridView);
        imageSlider = view.findViewById(R.id.imageSlider);
    }

    private void setupSlider(View view) {
        List<Integer> sliderImages = Arrays.asList(
                R.drawable.banner,
                R.drawable.banner2,
                R.drawable.banner3
        );

        sliderAdapter = new ImageSliderAdapter(getContext(), sliderImages);
        imageSlider.setAdapter(sliderAdapter);

        // Logic tự động lướt ảnh
        sliderRunnable = () -> {
            int currentItem = imageSlider.getCurrentItem();
            int nextItem = (currentItem + 1) % sliderAdapter.getItemCount();
            imageSlider.setCurrentItem(nextItem, true);
        };

        imageSlider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY);
            }
        });
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

        filterButton.setOnClickListener(v -> {
            FilterBottomSheetFragment filterSheet = new FilterBottomSheetFragment();
            filterSheet.show(getChildFragmentManager(), FilterBottomSheetFragment.TAG);
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

    @Override
    public void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY);
    }

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    private void setupGridView(){
         db = FirebaseFirestore.getInstance();
        allProductList = new ArrayList<>();
        displayedProductList = new ArrayList<>();
        adapter = new GridAdapter(getContext(), displayedProductList);
        gridView.setAdapter(adapter);
    }

    private void fetchData() {
        db.collection("orders")
                .whereEqualTo("status", "Đã xử lý")
                .get()
                .addOnSuccessListener(orderSnapshots -> {
                    Map<String, Integer> soldCounts = new HashMap<>();
                    for (QueryDocumentSnapshot orderDoc : orderSnapshots) {
                        Order order = orderDoc.toObject(Order.class);
                        if (order.getItems() != null) {
                            for (CartItem item : order.getItems()) {
                                int currentCount = soldCounts.getOrDefault(item.getProductId(), 0);
                                soldCounts.put(item.getProductId(), currentCount + item.getQuantity());
                            }
                        }
                    }
                    fetchProductsAndApplyCounts(soldCounts);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching orders for sold counts", e);
                    fetchProductsAndApplyCounts(new HashMap<>());
                });
    }

    private void fetchProductsAndApplyCounts(Map<String, Integer> soldCounts) {
        db.collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allProductList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            product.setId(document.getId());
                            int count = soldCounts.getOrDefault(product.getId(), 0);
                            product.setSoldCount(count);
                            allProductList.add(product);
                        }
                        applyFilters();
                    } else {
                        Log.w(TAG, "Error getting products.", task.getException());
                    }
                });
    }

    private void setupSearchView() {
        searchRunnableDelay = this::applyFilters;

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchHandler.removeCallbacks(searchRunnableDelay);
                applyFilters();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchHandler.removeCallbacks(searchRunnableDelay);
                searchHandler.postDelayed(searchRunnableDelay, DEBOUNCE_DELAY);
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

    @Override
    public void onFilterSelected(FilterBottomSheetFragment.SortOption sortOption) {
        switch (sortOption) {
            case PRICE_ASC:
                Collections.sort(allProductList, (p1, p2) -> Double.compare(p1.getPrice(), p2.getPrice()));
                break;
            case PRICE_DESC:
                Collections.sort(allProductList, (p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
                break;
            case BEST_SELLING:
                Collections.sort(allProductList, (p1, p2) -> Integer.compare(p2.getSoldCount(), p1.getSoldCount()));
                break;
            case DEFAULT:
                break;
        }
        applyFilters();
    }
}
