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
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.project_terrarium_prm392.R;
import com.example.project_terrarium_prm392.models.Product;
import com.example.project_terrarium_prm392.repository.TerrariumRepository;
import com.example.project_terrarium_prm392.ui.adapters.ProductAdapter;

import java.util.ArrayList;
import java.util.List;

public class ProductListActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {

    private static final String TAG = "ProductListActivity";
    private RecyclerView recyclerViewProducts;
    private ProductAdapter adapter;
    private ProgressBar progressBar;
    private TextView textViewError;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        
        try {
            // Initialize UI components
            Toolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitle("Terrarium Products");
            // Don't call setSupportActionBar(toolbar) to avoid conflicts
            
            recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
            swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
            progressBar = findViewById(R.id.progressBar);
            textViewError = findViewById(R.id.textViewError);
            
            // Setup RecyclerView
            recyclerViewProducts.setLayoutManager(new GridLayoutManager(this, 2));
            adapter = new ProductAdapter(this);
            adapter.setOnProductClickListener(this);
            recyclerViewProducts.setAdapter(adapter);
            
            // Setup SwipeRefreshLayout
            swipeRefreshLayout.setOnRefreshListener(this::loadProducts);
            
            // Load products
            loadProducts();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing ProductListActivity: " + e.getMessage());
            Toast.makeText(this, "Error initializing: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProducts() {
        try {
            progressBar.setVisibility(View.VISIBLE);
            textViewError.setVisibility(View.GONE);
            
            // Log thông tin
            Log.d(TAG, "Bắt đầu tải danh sách sản phẩm...");
            
            // Call the repository to fetch products
            TerrariumRepository repository = new TerrariumRepository(this);
            repository.getProducts(new TerrariumRepository.RepositoryCallback<List<Product>>() {
                @Override
                public void onSuccess(List<Product> result) {
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    
                    Log.d(TAG, "Tải sản phẩm thành công, số lượng: " + (result != null ? result.size() : 0));
                    
                    if (result != null && !result.isEmpty()) {
                        adapter.setProducts(result);
                        
                        // Ẩn lỗi nếu có
                        textViewError.setVisibility(View.GONE);
                    } else {
                        // Trong trường hợp API trả về danh sách rỗng, hiển thị thông báo
                        textViewError.setText("Không có sản phẩm nào");
                        textViewError.setVisibility(View.VISIBLE);
                        
                        // Vẫn load sample data nếu API không có dữ liệu
                        adapter.setProducts(createSampleProducts());
                    }
                }
                
                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Lỗi API: " + e.getMessage(), e);
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    
                    // Hiển thị thông báo lỗi với một phần là lời khuyên
                    textViewError.setVisibility(View.VISIBLE);
                    textViewError.setText("Lỗi kết nối API: " + e.getMessage() + 
                                        "\n\nHiển thị dữ liệu mẫu thay thế");
                    
                    // Load sample data in case of error
                    List<Product> sampleProducts = createSampleProducts();
                    adapter.setProducts(sampleProducts);
                    
                    // Hiển thị toast thông báo ngắn
                    Toast.makeText(ProductListActivity.this, 
                                "Hiển thị dữ liệu mẫu do lỗi kết nối API", 
                                Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong loadProducts: " + e.getMessage(), e);
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            textViewError.setVisibility(View.VISIBLE);
            textViewError.setText("Lỗi: " + e.getMessage());
            
            // Vẫn hiển thị dữ liệu mẫu ngay cả khi có lỗi
            adapter.setProducts(createSampleProducts());
        }
    }

    private List<Product> createSampleProducts() {
        List<Product> products = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            Product product = new Product();
            product.setId(i);
            product.setName("Terrarium Plant " + i);
            product.setPrice(10.99 + i);
            product.setDescription("Beautiful plant for your terrarium. Perfect for small spaces.");
            product.setStockQuantity(10 + i);
            product.setCategoryId(1);
            products.add(product);
        }
        
        return products;
    }

    @Override
    public void onProductClick(Product product) {
        // Navigate to product detail activity
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.getId());
        startActivity(intent);
    }
} 