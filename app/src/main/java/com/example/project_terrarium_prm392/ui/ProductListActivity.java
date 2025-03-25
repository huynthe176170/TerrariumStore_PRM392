package com.example.project_terrarium_prm392.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.project_terrarium_prm392.R;
import com.example.project_terrarium_prm392.api.ApiClient;
import com.example.project_terrarium_prm392.api.TerrariumApiService;
import com.example.project_terrarium_prm392.models.Category;
import com.example.project_terrarium_prm392.models.Product;
import com.example.project_terrarium_prm392.ui.adapters.CategoryAdapter;
import com.example.project_terrarium_prm392.ui.adapters.ProductAdapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductListActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener, CategoryAdapter.OnCategoryClickListener {

    private static final String TAG = "ProductListActivity";
    private RecyclerView recyclerViewProducts;
    private RecyclerView recyclerViewCategories;
    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;
    private ProgressBar progressBar;
    private TextView textViewError;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TerrariumApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_product_list);
            
            // Initialize API service
            apiService = ApiClient.getApiService();
            
            // Initialize UI components
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            
            recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
            recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
            swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
            progressBar = findViewById(R.id.progressBar);
            textViewError = findViewById(R.id.textViewError);
            
            // Setup RecyclerViews
            setupRecyclerViews();
            
            // Load initial data
            loadCategories();
            loadProducts();
            
            // Setup SwipeRefreshLayout
            swipeRefreshLayout.setOnRefreshListener(() -> {
                // Reload current category or all products
                if (categoryAdapter.getSelectedPosition() == 0) {
                    loadProducts();
                } else {
                    Category selectedCategory = getCategoryAtPosition(categoryAdapter.getSelectedPosition());
                    if (selectedCategory != null) {
                        loadProductsByCategory(selectedCategory.getId());
                    } else {
                        loadProducts();
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error initializing ProductListActivity: " + e.getMessage());
            Toast.makeText(this, "Error initializing: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerViews() {
        // Setup Products RecyclerView
        productAdapter = new ProductAdapter(this);
        recyclerViewProducts.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewProducts.setAdapter(productAdapter);
        
        // Setup Categories RecyclerView
        categoryAdapter = new CategoryAdapter(this);
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewCategories.setAdapter(categoryAdapter);
    }

    private void loadCategories() {
        showLoading(true);
        apiService.getAllCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    categoryAdapter.setCategories(response.body());
                    // Select "All" category by default
                    categoryAdapter.selectCategory(0);
                } else {
                    showError("Failed to load categories");
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                showLoading(false);
                showError("Network error: " + t.getMessage());
                Log.e(TAG, "Failed to load categories: " + t.getMessage());
            }
        });
    }

    private void loadProducts() {
        showLoading(true);
        apiService.getAllProducts().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    productAdapter.setProducts(response.body());
                    showError(false);
                } else {
                    showError("Failed to load products");
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                showError("Network error: " + t.getMessage());
                Log.e(TAG, "Failed to load products: " + t.getMessage());
            }
        });
    }

    private void loadProductsByCategory(int categoryId) {
        showLoading(true);
        Log.d(TAG, "Loading products for category ID: " + categoryId);
        
        apiService.getProductsByCategory(categoryId).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body();
                    Log.d(TAG, "Loaded " + products.size() + " products for category ID: " + categoryId);
                    
                    productAdapter.setProducts(products);
                    
                    if (products.isEmpty()) {
                        showError("Không có sản phẩm nào trong danh mục này");
                    } else {
                        showError(false);
                    }
                } else {
                    int errorCode = response.code();
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body: " + e.getMessage());
                    }
                    
                    Log.e(TAG, "API error: " + errorCode + " - " + errorBody);
                    showError("Không thể tải sản phẩm cho danh mục này (Mã lỗi: " + errorCode + ")");
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                Log.e(TAG, "Network error when loading products by category: " + t.getMessage(), t);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private Category getCategoryAtPosition(int position) {
        if (position <= 0) {
            return null; // "All" category
        } 
        
        try {
            // Get the adjusted position (subtract 1 for the "All" category)
            return getCategoryFromAdapter(position - 1);
        } catch (Exception e) {
            Log.e(TAG, "Error getting category at position: " + e.getMessage());
            return null;
        }
    }
    
    private Category getCategoryFromAdapter(int position) {
        try {
            // This method assumes the CategoryAdapter has a way to get a Category at a specific position
            // If your adapter doesn't have this method, you might need to add it
            List<Category> categories = ((CategoryAdapter)recyclerViewCategories.getAdapter()).getCategories();
            if (categories != null && position >= 0 && position < categories.size()) {
                return categories.get(position);
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error getting category from adapter: " + e.getMessage());
            return null;
        }
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        showError(true);
        textViewError.setText(message);
    }

    private void showError(boolean show) {
        textViewError.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onProductClick(Product product) {
        // Navigate to product detail activity
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.getId());
        startActivity(intent);
    }

    @Override
    public void onCategoryClick(Category category, int position) {
        if (position == 0) {
            // "All" category was selected
            loadProducts();
        } else if (category != null) {
            // A specific category was selected
            Toast.makeText(this, "Selected category: " + category.getName(), Toast.LENGTH_SHORT).show();
            loadProductsByCategory(category.getId());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 