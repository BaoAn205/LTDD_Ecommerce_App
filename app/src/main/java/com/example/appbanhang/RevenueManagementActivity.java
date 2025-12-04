package com.example.appbanhang;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View; // **THÊM IMPORT CÒN THIẾU**
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class RevenueManagementActivity extends AppCompatActivity {

    private static final String TAG = "RevenueActivity";

    private BarChart revenueBarChart;
    private PieChart categoryPieChart;
    private TextView totalRevenueTextView;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revenue_management);

        db = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        totalRevenueTextView = findViewById(R.id.totalRevenueTextView);
        revenueBarChart = findViewById(R.id.revenueBarChart);
        categoryPieChart = findViewById(R.id.categoryPieChart);

        fetchData();
    }

    private void fetchData() {
        db.collection("orders")
                .whereEqualTo("status", "Đã xử lý")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Order> processedOrders = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            processedOrders.add(document.toObject(Order.class));
                        }
                        processRevenueData(processedOrders);
                        processCategoryData(processedOrders);
                    } else {
                        Log.e(TAG, "Error fetching revenue data", task.getException());
                    }
                });
    }

    private void processRevenueData(List<Order> orders) {
        double totalRevenue = 0;
        Map<String, Double> monthlyRevenue = new TreeMap<>();
        Calendar cal = Calendar.getInstance();

        for (int i = 0; i < 6; i++) {
            String monthKey = String.format(Locale.US, "%d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
            monthlyRevenue.put(monthKey, 0.0);
            cal.add(Calendar.MONTH, -1);
        }

        for (Order order : orders) {
            totalRevenue += order.getTotalPrice();
            if (order.getOrderDate() != null) {
                cal.setTime(order.getOrderDate());
                String monthKey = String.format(Locale.US, "%d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
                if (monthlyRevenue.containsKey(monthKey)) {
                    double currentRevenue = monthlyRevenue.get(monthKey);
                    monthlyRevenue.put(monthKey, currentRevenue + order.getTotalPrice());
                }
            }
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        totalRevenueTextView.setText("Tổng doanh thu: " + formatter.format(totalRevenue));
        setupBarChart(monthlyRevenue);
    }

    private void processCategoryData(List<Order> orders) {
        Map<String, Integer> categoryCounts = new HashMap<>();

        db.collection("products").get().addOnSuccessListener(productSnapshots -> {
            Map<String, String> productIdToCategoryMap = new HashMap<>();
            for(QueryDocumentSnapshot doc : productSnapshots) {
                Product product = doc.toObject(Product.class);
                productIdToCategoryMap.put(doc.getId(), product.getCategory());
            }

            for (Order order : orders) {
                for (CartItem item : order.getItems()) {
                    String category = productIdToCategoryMap.get(item.getProductId());
                    if (category != null && !category.isEmpty()) {
                        int currentCount = categoryCounts.getOrDefault(category, 0);
                        categoryCounts.put(category, currentCount + item.getQuantity());
                    }
                }
            }

            setupPieChart(categoryCounts);
        });
    }

    private void setupBarChart(Map<String, Double> monthlyRevenue) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, Double> entry : monthlyRevenue.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue().floatValue()));
            String[] parts = entry.getKey().split("-");
            labels.add("T" + Integer.parseInt(parts[1]) + "/" + parts[0].substring(2));
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Doanh thu 6 tháng gần nhất");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.DKGRAY);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);

        revenueBarChart.setData(barData);
        revenueBarChart.setFitBars(true);

        XAxis xAxis = revenueBarChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = revenueBarChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value >= 1_000_000) {
                    return String.format(Locale.US, "%.1ftr", value / 1_000_000);
                } else if (value >= 1_000) {
                    return String.format(Locale.US, "%.0fk", value / 1_000);
                }
                return String.valueOf((int) value);
            }
        });

        revenueBarChart.getDescription().setEnabled(false);
        revenueBarChart.getAxisRight().setEnabled(false);
        revenueBarChart.getLegend().setTextSize(12f);
        revenueBarChart.setExtraBottomOffset(10f);
        revenueBarChart.invalidate();
    }

    private void setupPieChart(Map<String, Integer> categoryCounts) {
        if (categoryCounts.isEmpty()) {
            categoryPieChart.setVisibility(View.GONE);
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setSliceSpace(3f);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter(categoryPieChart));

        categoryPieChart.setData(pieData);
        categoryPieChart.setUsePercentValues(true);
        categoryPieChart.getDescription().setEnabled(false);
        categoryPieChart.setDrawHoleEnabled(true);
        categoryPieChart.setHoleColor(Color.TRANSPARENT);
        categoryPieChart.setEntryLabelColor(Color.BLACK);
        categoryPieChart.setEntryLabelTextSize(12f);

        Legend legend = categoryPieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        categoryPieChart.animateY(1400);
        categoryPieChart.invalidate();
    }
}
