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
import com.example.project_terrarium_prm392.models.Product;
import com.example.project_terrarium_prm392.repository.CartRepository;
import com.example.project_terrarium_prm392.repository.TerrariumRepository;
import com.example.project_terrarium_prm392.ui.adapters.CartAdapter;
import com.example.project_terrarium_prm392.ui.auth.LoginActivity;
import com.example.project_terrarium_prm392.utils.TokenManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartItemListener {
    private static final String TAG = "CartActivity";

    private CartRepository cartRepository;
    private CartAdapter cartAdapter;
    
    // UI components
    private Toolbar toolbar;
    private RecyclerView recyclerViewCart;
    private TextView emptyCartMessage;
    private CardView checkoutPanel;
    private Button buttonCheckout;
    private TextView textViewTotalAmount;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    private TokenManager tokenManager;
    private TerrariumRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        
        Log.d(TAG, "CartActivity onCreate");
        
        tokenManager = new TokenManager(this);
        repository = new TerrariumRepository(this);
        
        // Debug logging
        Log.d("CartActivity", "Token: " + tokenManager.getToken());
        Log.d("CartActivity", "UserId: " + tokenManager.getUserId());
        Log.d("CartActivity", "Username: " + tokenManager.getUsername());
        Log.d("CartActivity", "Is logged in: " + tokenManager.isLoggedIn());
        
        if (!tokenManager.isLoggedIn()) {
            Toast.makeText(this, "Please login to view cart", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        
        // Initialize views
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

        // Initialize repository
        cartRepository = new CartRepository(this);
        
        // Initialize CartAdapter
        cartAdapter = new CartAdapter(this, this);
        
        // Setup RecyclerView
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCart.setAdapter(cartAdapter);
        
        // Setup swipe refresh
        swipeRefreshLayout.setOnRefreshListener(this::loadCart);

        // Setup checkout button
        buttonCheckout.setOnClickListener(v -> proceedToCheckout());
        
        // Load cart data
        loadCart();
    }

    private void loadCart() {
        showLoading(true);
        
        cartRepository.getUserCart(new CartRepository.CartCallback() {
            @Override
            public void onSuccess(Cart cart) {
                showLoading(false);
                if (cart != null && cart.getCartItems() != null && !cart.getCartItems().isEmpty()) {
                    cartAdapter.setCartItems(cart.getCartItems());
                    updateTotalPrice();
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

    private void showEmptyCart() {
        emptyCartMessage.setVisibility(View.VISIBLE);
        recyclerViewCart.setVisibility(View.GONE);
        checkoutPanel.setVisibility(View.GONE);
    }

    private void updateTotalPrice() {
        double totalPrice = cartAdapter.getTotalPrice();
        textViewTotalAmount.setText(formatCurrency(totalPrice));
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
        
        cartRepository.updateCartItem(cartItem, new CartRepository.CartItemCallback() {
            @Override
            public void onSuccess(CartItem updatedCartItem) {
                showLoading(false);
                // Update the item in the list
                for (int i = 0; i < cartAdapter.getCartItems().size(); i++) {
                    if (cartAdapter.getCartItems().get(i).getId() == updatedCartItem.getId()) {
                        cartAdapter.getCartItems().set(i, updatedCartItem);
                        cartAdapter.notifyItemChanged(i);
                        break;
                    }
                }
                updateTotalPrice();
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
        
        cartRepository.removeCartItem(cartItem.getId(), new CartRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                showLoading(false);
                // Remove the item from the adapter
                cartAdapter.getCartItems().remove(cartItem);
                cartAdapter.notifyDataSetChanged();
                updateTotalPrice();
                
                if (cartAdapter.getCartItems().isEmpty()) {
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
        if (cartAdapter.getCartItems().isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống. Vui lòng thêm sản phẩm vào giỏ hàng.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Navigate to checkout activity
        // Intent intent = new Intent(this, CheckoutActivity.class);
        // startActivity(intent);
        
        // Temporary toast until checkout is implemented
        Toast.makeText(this, "Tính năng thanh toán đang được phát triển", Toast.LENGTH_SHORT).show();
    }

    private void showCartContent() {
        emptyCartMessage.setVisibility(View.GONE);
        recyclerViewCart.setVisibility(View.VISIBLE);
        checkoutPanel.setVisibility(View.VISIBLE);
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