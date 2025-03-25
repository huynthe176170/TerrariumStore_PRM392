package com.example.project_terrarium_prm392.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.project_terrarium_prm392.R;
import com.example.project_terrarium_prm392.models.Cart;
import com.example.project_terrarium_prm392.models.CartItem;
import com.example.project_terrarium_prm392.repository.CartRepository;
import com.example.project_terrarium_prm392.ui.adapters.CartAdapter;
import com.example.project_terrarium_prm392.ui.auth.LoginActivity;
import com.example.project_terrarium_prm392.utils.TokenManager;

import java.text.NumberFormat;
import java.util.Locale;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartItemListener {
    private static final String TAG = "CartActivity";
    private static final int REQUEST_CHECKOUT = 100;

    private CartRepository cartRepository;
    private CartAdapter cartAdapter;
    
    private RecyclerView recyclerViewCart;
    private TextView emptyCartMessage;
    private CardView layoutCheckout;
    private Button buttonCheckout;
    private Button buttonViewOrders;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView textViewTotalPrice;

    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        
        // Khởi tạo Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Giỏ hàng");
        
        // Khởi tạo TokenManager và kiểm tra đăng nhập
        tokenManager = new TokenManager(this);
        if (!tokenManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem giỏ hàng", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        
        // Khởi tạo Repository
        cartRepository = new CartRepository(this);
        
        // Khởi tạo views
        initViews();
        
        // Thiết lập RecyclerView
        setupRecyclerView();
        
        // Thiết lập SwipeRefresh
        setupSwipeRefresh();
        
        // Thiết lập nút checkout
        setupCheckoutButton();
        
        // Thiết lập nút xem đơn hàng
        setupViewOrdersButton();
        
        // Load giỏ hàng
        loadCart();
    }

    private void initViews() {
        recyclerViewCart = findViewById(R.id.recyclerViewCart);
        emptyCartMessage = findViewById(R.id.emptyCartMessage);
        layoutCheckout = findViewById(R.id.layoutCheckout);
        buttonCheckout = findViewById(R.id.buttonCheckout);
        buttonViewOrders = findViewById(R.id.buttonViewOrders);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        textViewTotalPrice = findViewById(R.id.textViewTotalPrice);
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(this, this);
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCart.setAdapter(cartAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadCart);
    }

    private void setupCheckoutButton() {
        buttonCheckout.setOnClickListener(v -> startCheckout());
    }

    private void setupViewOrdersButton() {
        buttonViewOrders.setOnClickListener(view -> {
            Intent intent = new Intent(CartActivity.this, OrderListActivity.class);
            startActivity(intent);
        });
    }

    private void loadCart() {
        showLoading(true);
        
        cartRepository.getUserCart(new CartRepository.CartCallback() {
            @Override
            public void onSuccess(Cart cart) {
                showLoading(false);
                if (cart != null && cart.getCartItems() != null && !cart.getCartItems().isEmpty()) {
                    cartAdapter.setCartItems(cart.getCartItems());
                    updateCartTotal();
                    showCartContent();
                } else {
                    showEmptyCart();
                }
            }

            @Override
            public void onError(String message) {
                showLoading(false);
                Toast.makeText(CartActivity.this, message, Toast.LENGTH_SHORT).show();
                showEmptyCart();
            }
        });
    }

    private void updateCartTotal() {
        cartRepository.getCartTotal(new CartRepository.CartTotalCallback() {
            @Override
            public void onSuccess(double total) {
                textViewTotalPrice.setText(formatPrice(total));
                buttonCheckout.setEnabled(total > 0);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(CartActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEmptyCart() {
        emptyCartMessage.setVisibility(View.VISIBLE);
        recyclerViewCart.setVisibility(View.GONE);
        layoutCheckout.setVisibility(View.GONE);
    }

    private void showCartContent() {
        emptyCartMessage.setVisibility(View.GONE);
        recyclerViewCart.setVisibility(View.VISIBLE);
        layoutCheckout.setVisibility(View.VISIBLE);
    }

    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(amount) + " đ";
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(price) + " đ";
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        swipeRefreshLayout.setRefreshing(isLoading);
    }

    @Override
    public void onIncreaseQuantity(CartItem cartItem) {
        int newQuantity = cartItem.getQuantity() + 1;
        updateCartItemQuantity(cartItem, newQuantity);
    }

    @Override
    public void onDecreaseQuantity(CartItem cartItem) {
        int newQuantity = cartItem.getQuantity() - 1;
        if (newQuantity <= 0) {
            removeCartItem(cartItem);
        } else {
            updateCartItemQuantity(cartItem, newQuantity);
        }
    }

    @Override
    public void onRemoveItem(CartItem cartItem) {
        removeCartItem(cartItem);
    }

    private void updateCartItemQuantity(CartItem cartItem, int newQuantity) {
        showLoading(true);
        cartItem.setQuantity(newQuantity);
        cartItem.setUserId(tokenManager.getUserId());
        
        cartRepository.updateCartItem(cartItem, new CartRepository.CartItemCallback() {
            @Override
            public void onSuccess(CartItem updatedCartItem) {
                showLoading(false);
                cartAdapter.updateItem(updatedCartItem);
                updateCartTotal();
            }

            @Override
            public void onError(String message) {
                showLoading(false);
                Toast.makeText(CartActivity.this, message, Toast.LENGTH_SHORT).show();
                loadCart(); // Reload cart to sync with server
            }
        });
    }

    private void removeCartItem(CartItem cartItem) {
        showLoading(true);
        
        cartRepository.removeCartItem(cartItem.getId(), cartItem.getProductId(), new CartRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                showLoading(false);
                cartAdapter.removeItem(cartItem);
                updateCartTotal();
                
                if (cartAdapter.getItemCount() == 0) {
                    showEmptyCart();
                }
            }

            @Override
            public void onError(String message) {
                showLoading(false);
                Toast.makeText(CartActivity.this, message, Toast.LENGTH_SHORT).show();
                loadCart(); // Reload cart to sync with server
            }
        });
    }

    private void startCheckout() {
        if (cartAdapter.getItemCount() == 0) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, CheckoutActivity.class);
        startActivityForResult(intent, REQUEST_CHECKOUT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECKOUT && resultCode == RESULT_OK) {
            // Checkout thành công, đóng màn hình giỏ hàng
            setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 