package com.example.appbanhang;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityOptionsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

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
    private BottomNavigationView bottomNavigationView;

    // Debouncing variables
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final long DEBOUNCE_DELAY = 500; // 500ms delay

    private static final String TAG = "HomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // --- View Initialization ---
        initializeViews();

        // --- Listeners Setup ---
        setupClickListeners();
        setupSearchView();

        // --- GridView setup with Firestore Data ---
        setupGridView();
        fetchProductsFromFirestore();
    }

    private void initializeViews() {
        // Toolbar & Top Buttons
        notificationButton = findViewById(R.id.notificationButton);
        cartButton = findViewById(R.id.cartButton);

        // Category Buttons
        weightsButton = findViewById(R.id.Weights);
        cardioButton = findViewById(R.id.Cardio);
        apparelButton = findViewById(R.id.Apparel);
        yogaButton = findViewById(R.id.Yoga);
        seeAllButton = findViewById(R.id.seeAllText);

        // SearchView
        searchView = findViewById(R.id.searchView);

        // Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        // GridView
        gridView = findViewById(R.id.gridView);
    }

    private void setupClickListeners() {
        notificationButton.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, NotiActivity.class)));
        cartButton.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, CartActivity.class)));

        // Category Buttons
        weightsButton.setOnClickListener(v -> onCategorySelected("Weights"));
        cardioButton.setOnClickListener(v -> onCategorySelected("Cardio"));
        apparelButton.setOnClickListener(v -> onCategorySelected("Apparel"));
        yogaButton.setOnClickListener(v -> onCategorySelected("Yoga & Pilates"));
        seeAllButton.setOnClickListener(v -> {
            selectedCategory = null;
            updateCategoryButtonsUI();
            applyFilters();
        });

        // Bottom Navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true; 
            } else if (itemId == R.id.nav_favorites) {
                startActivity(new Intent(HomeActivity.this, WishlistActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });
        
        // GridView Item Click
         gridView.setOnItemClickListener((parent, view, position, id) -> {
            Product selectedProduct = displayedProductList.get(position);
            Intent intent = new Intent(HomeActivity.this, ProductDetailActivity.class);
            intent.putExtra("PRODUCT_DETAIL", selectedProduct);

            ImageView productImageView = view.findViewById(R.id.gridImage);
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    HomeActivity.this, productImageView, "product_image_transition");
            startActivity(intent, options.toBundle());
        });
    }
    
    private void setupGridView(){
         db = FirebaseFirestore.getInstance();
        allProductList = new ArrayList<>();
        displayedProductList = new ArrayList<>();
        adapter = new GridAdapter(this, displayedProductList);
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
                // Perform search immediately
                searchHandler.removeCallbacks(searchRunnable);
                applyFilters();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Debounce the search query
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
