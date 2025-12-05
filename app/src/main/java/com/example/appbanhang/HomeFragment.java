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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private RecyclerView recentlyViewedRecyclerView;
    private ViewedProductAdapter viewedProductAdapter;
    private List<Product> viewedProductList;
    private TextView recentlyViewedTitle;

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
                                    (product.getCategory() != null && product.getCategory().equalsIgnoreCase(selectedCategory));

            boolean searchMatch = query.isEmpty() || 
                                  (product.getName() != null && product.getName().toLowerCase().contains(query));

            if (categoryMatch && searchMatch) {
                filteredList.add(product);
            }
        }

        displayedProductList.clear();
        displayedProductList.addAll(filteredList);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
