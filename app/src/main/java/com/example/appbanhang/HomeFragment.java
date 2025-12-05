package com.example.appbanhang;

import android.content.Context;
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
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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

    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final long DEBOUNCE_DELAY = 500;

    private RecyclerView recentlyViewedRecyclerView;
    private ViewedProductAdapter viewedProductAdapter;
    private List<Product> viewedProductList;
    private TextView recentlyViewedTitle;
    private FilterBottomSheetFragment.SortOption currentSortOption = FilterBottomSheetFragment.SortOption.DEFAULT;

    private ViewPager2 bannerViewPager;
    private BannerAdapter bannerAdapter;
    private List<Integer> bannerImages;
    private Handler bannerHandler = new Handler(Looper.getMainLooper());
    private Runnable bannerRunnable;

    private static final String TAG = "HomeFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        initializeViews(view);
        setupBanner();
        setupClickListeners();
        setupSearchView();
        setupGridView();
        setupViewedProducts();
        fetchProductsFromFirestore();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadViewedProducts();
        if (!allProductList.isEmpty()) {
            updateSoldCounts();
        }
        startBannerAutoScroll();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopBannerAutoScroll();
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
        recentlyViewedRecyclerView = view.findViewById(R.id.recentlyViewedRecyclerView);
        recentlyViewedTitle = view.findViewById(R.id.recentlyViewedTitle);
        filterButton = view.findViewById(R.id.filterButton);
        bannerViewPager = view.findViewById(R.id.bannerViewPager);
    }

    private void setupBanner() {
        bannerImages = new ArrayList<>(Arrays.asList(R.drawable.banner, R.drawable.banner2, R.drawable.banner3));
        bannerAdapter = new BannerAdapter(bannerImages);
        bannerViewPager.setAdapter(bannerAdapter);

        try {
            Field scrollerField = ViewPager2.class.getDeclaredField("mScroller");
            scrollerField.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(bannerViewPager.getContext(), new Interpolator() {
                public float getInterpolation(float t) {
                    return t;
                }
            });
            scroller.setFixedDuration(2000); // Slower scroll duration
            scrollerField.set(bannerViewPager, scroller);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, "Could not set scroller", e);
        }

        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                int currentItem = bannerViewPager.getCurrentItem();
                int totalItems = bannerAdapter.getItemCount();
                if (totalItems > 0) {
                    bannerViewPager.setCurrentItem((currentItem + 1) % totalItems);
                }
                bannerHandler.postDelayed(this, 5000); // 5 seconds delay
            }
        };
    }

    private void startBannerAutoScroll() {
        bannerHandler.postDelayed(bannerRunnable, 5000);
    }

    private void stopBannerAutoScroll() {
        bannerHandler.removeCallbacks(bannerRunnable);
    }

    private void setupClickListeners() {
        notificationButton.setOnClickListener(v -> {
            if (getActivity() != null) startActivity(new Intent(getActivity(), NotiActivity.class));
        });
        cartButton.setOnClickListener(v -> {
            if (getActivity() != null) startActivity(new Intent(getActivity(), CartActivity.class));
        });

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
            FilterBottomSheetFragment bottomSheet = new FilterBottomSheetFragment();
            bottomSheet.show(getChildFragmentManager(), FilterBottomSheetFragment.TAG);
        });

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            if (getActivity() == null) return;
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
        if (getContext() == null) return;
        allProductList = new ArrayList<>();
        displayedProductList = new ArrayList<>();
        adapter = new GridAdapter(getContext(), displayedProductList);
        gridView.setAdapter(adapter);
    }

    private void setupViewedProducts() {
        if (getContext() == null) return;
        viewedProductList = new ArrayList<>();
        viewedProductAdapter = new ViewedProductAdapter(getContext(), viewedProductList);
        recentlyViewedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recentlyViewedRecyclerView.setAdapter(viewedProductAdapter);
    }

    private void loadViewedProducts() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || recentlyViewedTitle == null || recentlyViewedRecyclerView == null) {
            if(recentlyViewedTitle != null) recentlyViewedTitle.setVisibility(View.GONE);
            if(recentlyViewedRecyclerView != null) recentlyViewedRecyclerView.setVisibility(View.GONE);
            return;
        }

        db.collection("users").document(currentUser.getUid()).collection("viewHistory")
            .orderBy("lastViewed", Query.Direction.DESCENDING).limit(10).get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots.isEmpty()) {
                     recentlyViewedTitle.setVisibility(View.GONE);
                     recentlyViewedRecyclerView.setVisibility(View.GONE);
                } else {
                     recentlyViewedTitle.setVisibility(View.VISIBLE);
                     recentlyViewedRecyclerView.setVisibility(View.VISIBLE);
                    List<String> productIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        productIds.add(doc.getString("productId"));
                    }
                    fetchViewedProductDetails(productIds);
                }
            });
    }

    private void fetchViewedProductDetails(List<String> productIds) {
        if (productIds == null || productIds.isEmpty()) return;

        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (String id : productIds) {
            if (id != null && !id.isEmpty()) {
                tasks.add(db.collection("products").document(id).get());
            }
        }

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(list -> {
            Map<String, Product> productMap = new HashMap<>();
            for (Object object : list) {
                DocumentSnapshot snapshot = (DocumentSnapshot) object;
                if(snapshot.exists()) {
                    Product product = snapshot.toObject(Product.class);
                    if (product != null) {
                        product.setId(snapshot.getId());
                        productMap.put(snapshot.getId(), product);
                    }
                }
            }

            viewedProductList.clear();
            for (String id : productIds) {
                if (productMap.containsKey(id)) {
                    viewedProductList.add(productMap.get(id));
                }
            }
            viewedProductAdapter.notifyDataSetChanged();
        });
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
                        applyFilters();
                        updateSoldCounts();
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    private void updateSoldCounts() {
        Log.d(TAG, "Starting to update sold counts...");
        db.collection("orders").whereEqualTo("status", "Đã xử lý").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Log.d(TAG, "Successfully fetched orders. Found " + task.getResult().size() + " orders with status 'Đã xử lý'.");
                Map<String, Integer> soldCounts = new HashMap<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Order order = document.toObject(Order.class);
                    Log.d(TAG, "Processing Order ID: " + document.getId() + ", Status: " + order.getStatus());

                    if (order.getItems() != null && !order.getItems().isEmpty()) {
                        Log.d(TAG, "Order " + document.getId() + " has " + order.getItems().size() + " items.");
                        for (CartItem item : order.getItems()) {
                            String productId = item.getProductId();
                            int quantity = item.getQuantity();
                            if (productId != null) {
                                soldCounts.put(productId, soldCounts.getOrDefault(productId, 0) + quantity);
                                Log.d(TAG, "  - ProductID: " + productId + ", Quantity: " + quantity);
                            } else {
                                Log.w(TAG, "  - Found an item with null productId in order " + document.getId());
                            }
                        }
                    } else {
                        Log.w(TAG, "Order " + document.getId() + " has no items or items list is null.");
                    }
                }

                Log.d(TAG, "Final sold counts map: " + soldCounts.toString());

                for (Product product : allProductList) {
                    product.setSoldCount(soldCounts.getOrDefault(product.getId(), 0));
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.d(TAG, "Updating adapter with new sold counts.");
                        applyFilters();
                    });
                }

            } else {
                Log.e(TAG, "Error getting orders for sold count", task.getException());
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
                                    (product.getCategory() != null && product.getCategory().equalsIgnoreCase(selectedCategory));

            boolean searchMatch = query.isEmpty() || 
                                  (product.getName() != null && product.getName().toLowerCase().contains(query));

            if (categoryMatch && searchMatch) {
                filteredList.add(product);
            }
        }
        // Sorting logic based on the selected sort option
        switch (currentSortOption) {
            case PRICE_ASC:
                Collections.sort(filteredList, Comparator.comparingDouble(Product::getPrice));
                break;
            case PRICE_DESC:
                Collections.sort(filteredList, (p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
                break;
            case BEST_SELLING:
                Collections.sort(filteredList, (p1, p2) -> Integer.compare(p2.getSoldCount(), p1.getSoldCount()));
                break;
            case DEFAULT:
            default:
                // No sorting or default sorting (e.g., by name)
                Collections.sort(filteredList, Comparator.comparing(Product::getName));
                break;
        }

        displayedProductList.clear();
        displayedProductList.addAll(filteredList);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onFilterSelected(FilterBottomSheetFragment.SortOption sortOption) {
        currentSortOption = sortOption;
        applyFilters();
    }
    public class FixedSpeedScroller extends Scroller {

        private int mDuration = 1000; // Default duration

        public FixedSpeedScroller(Context context) {
            super(context);
        }

        public FixedSpeedScroller(Context context, Interpolator interpolator) {
            super(context, interpolator);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            // Ignore received duration, use fixed one
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            // Ignore received duration, use fixed one
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        public void setFixedDuration(int duration) {
            mDuration = duration;
        }
    }

}
