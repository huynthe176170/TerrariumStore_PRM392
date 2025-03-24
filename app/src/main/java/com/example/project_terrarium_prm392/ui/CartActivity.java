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

    private CartRepository cartRepository;
    private CartAdapter cartAdapter;
    
    private RecyclerView recyclerViewCart;
    private TextView emptyCartMessage;
    private CardView checkoutPanel;
    private Button buttonCheckout;
    private TextView textViewTotalAmount;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        
        Log.d(TAG, "CartActivity onCreate");
        
        tokenManager = new TokenManager(this);
        
        if (!tokenManager.isLoggedIn()) {
            Toast.makeText(this, "Please login to view cart", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        
        initializeViews();
        setupRecyclerView();
        setupSwipeRefresh();
        setupCheckoutButton();
        
        // Load cart data
        loadCart();
    }

    private void initializeViews() {
        recyclerViewCart = findViewById(R.id.recyclerViewCart);
        progressBar = findViewById(R.id.progressBar);
        emptyCartMessage = findViewById(R.id.emptyCartMessage);
        checkoutPanel = findViewById(R.id.checkoutPanel);
        buttonCheckout = findViewById(R.id.buttonCheckout);
        textViewTotalAmount = findViewById(R.id.textViewTotalAmount);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        
        if (recyclerViewCart == null) {
            Log.e(TAG, "RecyclerView not found in layout");
            Toast.makeText(this, "Error initializing cart view", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        cartRepository = new CartRepository(this);
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
        buttonCheckout.setOnClickListener(v -> proceedToCheckout());
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
                textViewTotalAmount.setText(formatCurrency(total));
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
        checkoutPanel.setVisibility(View.GONE);
    }

    private void showCartContent() {
        emptyCartMessage.setVisibility(View.GONE);
        recyclerViewCart.setVisibility(View.VISIBLE);
        checkoutPanel.setVisibility(View.VISIBLE);
    }

    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(amount) + " đ";
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

    private void proceedToCheckout() {
        if (cartAdapter.getItemCount() == 0) {
            Toast.makeText(this, "Giỏ hàng trống. Vui lòng thêm sản phẩm vào giỏ hàng.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Navigate to checkout activity
        // Intent intent = new Intent(this, CheckoutActivity.class);
        // startActivity(intent);
        
        // Temporary toast until checkout is implemented
        Toast.makeText(this, "Tính năng thanh toán đang được phát triển", Toast.LENGTH_SHORT).show();
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