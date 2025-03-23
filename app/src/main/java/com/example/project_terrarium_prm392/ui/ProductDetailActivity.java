package com.example.project_terrarium_prm392.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.project_terrarium_prm392.R;
import com.example.project_terrarium_prm392.models.CartItem;
import com.example.project_terrarium_prm392.models.Product;
import com.example.project_terrarium_prm392.repository.TerrariumRepository;
import com.example.project_terrarium_prm392.ui.auth.LoginActivity;
import com.example.project_terrarium_prm392.utils.TokenManager;
import com.squareup.picasso.Picasso;

public class ProductDetailActivity extends AppCompatActivity {

    private static final String TAG = "ProductDetailActivity";
    public static final String EXTRA_PRODUCT_ID = "product_id";
    private ImageView imageViewProduct;
    private TextView textViewProductName;
    private TextView textViewProductPrice;
    private TextView textViewProductCategory;
    private TextView textViewProductDescription;
    private Button buttonAddToCart;
    private ProgressBar progressBar;

    private int productId;
    private Product currentProduct;
    private TokenManager tokenManager;
    private TerrariumRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        
        try {
            // Initialize UI components
            Toolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitle("Chi tiết sản phẩm");
            toolbar.setNavigationIcon(R.drawable.ic_launcher_foreground);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
            
            imageViewProduct = findViewById(R.id.imageViewProduct);
            textViewProductName = findViewById(R.id.textViewProductName);
            textViewProductPrice = findViewById(R.id.textViewProductPrice);
            textViewProductCategory = findViewById(R.id.textViewProductCategory);
            textViewProductDescription = findViewById(R.id.textViewProductDescription);
            buttonAddToCart = findViewById(R.id.buttonAddToCart);
            progressBar = findViewById(R.id.progressBar);
            
            // Initialize token manager and repository
            tokenManager = new TokenManager(this);
            repository = new TerrariumRepository(this);
            
            // Get product ID from intent
            productId = getIntent().getIntExtra(EXTRA_PRODUCT_ID, -1);
            
            if (productId == -1) {
                Toast.makeText(this, "Không tìm thấy ID sản phẩm", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            // Load product details
            loadProductDetails(productId);
            
            // Set up add to cart button
//            buttonAddToCart.setOnClickListener(v -> {
//                addToCart();
//            });
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khởi tạo ProductDetailActivity: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
//    private void addToCart() {
//        // Kiểm tra đăng nhập trước khi thêm vào giỏ hàng
//        if (!tokenManager.isLoggedIn()) {
//            // Hiển thị dialog yêu cầu đăng nhập
//            new AlertDialog.Builder(this)
//                .setTitle("Yêu cầu đăng nhập")
//                .setMessage("Bạn cần đăng nhập để thêm sản phẩm vào giỏ hàng.")
//                .setPositiveButton("Đăng nhập", (dialog, which) -> {
//                    // Chuyển đến màn hình đăng nhập
//                    Intent intent = new Intent(ProductDetailActivity.this, LoginActivity.class);
//                    startActivity(intent);
//                })
//                .setNegativeButton("Hủy", null)
//                .show();
//            return;
//        }
//
//        // Đã đăng nhập, tiếp tục thêm vào giỏ hàng
//        if (currentProduct != null) {
//            // Tạo CartItem object
//            CartItem cartItem = new CartItem();
//            cartItem.setProductId(currentProduct.getId());
//            cartItem.setQuantity(1); // Mặc định là 1
//
//            progressBar.setVisibility(View.VISIBLE);
//
//            // Gọi API thêm vào giỏ hàng
//            repository.addToCart(cartItem, new TerrariumRepository.RepositoryCallback<CartItem>() {
//                @Override
//                public void onSuccess(CartItem result) {
//                    progressBar.setVisibility(View.GONE);
//                    Toast.makeText(ProductDetailActivity.this, "Đã thêm " + currentProduct.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
//                }
//
//                @Override
//                public void onError(Exception e) {
//                    progressBar.setVisibility(View.GONE);
//                    Toast.makeText(ProductDetailActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    Log.e(TAG, "Lỗi thêm vào giỏ hàng: " + e.getMessage(), e);
//                }
//            });
//        } else {
//            Toast.makeText(this, "Không thể thêm vào giỏ hàng, dữ liệu sản phẩm không có sẵn", Toast.LENGTH_SHORT).show();
//        }
//    }

    private void loadProductDetails(int productId) {
        try {
            progressBar.setVisibility(View.VISIBLE);
            
            // Gọi API thực tế thay vì dữ liệu mẫu
            repository.getProductById(productId, new TerrariumRepository.RepositoryCallback<Product>() {
                @Override
                public void onSuccess(Product result) {
                    progressBar.setVisibility(View.GONE);
                    if (result != null) {
                        Log.d(TAG, "Đã nhận thông tin sản phẩm: " + result.getName());
                        currentProduct = result;
                        displayProductDetails(result);
                    } else {
                        Toast.makeText(ProductDetailActivity.this, "Không tìm thấy thông tin sản phẩm", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                
                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Lỗi API: " + e.getMessage(), e);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ProductDetailActivity.this, "Lỗi tải thông tin sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Tạm thời tạo sản phẩm mẫu nếu có lỗi
                    loadMockProductDetails();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong loadProductDetails: " + e.getMessage(), e);
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            loadMockProductDetails(); // Tải dữ liệu mẫu nếu có ngoại lệ
        }
    }
    
    private void loadMockProductDetails() {
        // Tạo sản phẩm mẫu để test
        Product mockProduct = new Product();
        mockProduct.setId(productId);
        mockProduct.setName("Terrarium Plant Sample");
        mockProduct.setDescription("Đây là một cây đẹp hoàn hảo cho bể cảnh của bạn. Nó yêu cầu bảo trì tối thiểu và mang lại vẻ ngoài xanh tươi.");
        mockProduct.setPrice(249000);
        mockProduct.setImageUrl("https://example.com/image.jpg");
        mockProduct.setCategory("Cây cảnh");
        
        progressBar.setVisibility(View.GONE);
        displayProductDetails(mockProduct);
    }
    
    private void displayProductDetails(Product product) {
        textViewProductName.setText(product.getName());
        textViewProductPrice.setText(String.format("%,.0f₫", product.getPrice()));
        
        String category = product.getCategory();
        if (category == null || category.isEmpty()) {
            category = "Chung"; // Danh mục mặc định nếu không có
        }
        textViewProductCategory.setText(category);
        
        textViewProductDescription.setText(product.getDescription());
        
        // Tải hình ảnh bằng Picasso
        try {
            // Sử dụng ảnh mặc định nếu URL rỗng hoặc null
            String imageUrl = product.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Picasso.get()
                      .load(imageUrl)
                      .placeholder(R.drawable.ic_launcher_background)
                      .error(R.drawable.ic_launcher_background)
                      .into(imageViewProduct);
            } else {
                imageViewProduct.setImageResource(R.drawable.ic_launcher_background);
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi tải hình ảnh: " + e.getMessage(), e);
            imageViewProduct.setImageResource(R.drawable.ic_launcher_background);
        }
        
        // Update button state if product is out of stock
        if (product.getStockQuantity() <= 0) {
            buttonAddToCart.setEnabled(false);
            buttonAddToCart.setText("Hết hàng");
        } else {
            buttonAddToCart.setEnabled(true);
            buttonAddToCart.setText("Thêm vào giỏ hàng");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 