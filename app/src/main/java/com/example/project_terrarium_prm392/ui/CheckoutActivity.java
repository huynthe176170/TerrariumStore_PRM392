package com.example.project_terrarium_prm392.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project_terrarium_prm392.R;
import com.example.project_terrarium_prm392.models.Cart;
import com.example.project_terrarium_prm392.repository.CartRepository;
import com.example.project_terrarium_prm392.utils.TokenManager;
import com.google.android.material.textfield.TextInputLayout;

import java.text.NumberFormat;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {
    private TextView textViewItemCount;
    private TextView textViewTotalPrice;
    private RadioGroup radioGroupAddress;
    private RadioButton radioDefaultAddress;
    private RadioButton radioNewAddress;
    private TextInputLayout textInputLayoutAddress;
    private Button buttonCheckout;
    private ProgressBar progressBar;

    private CartRepository cartRepository;
    private TokenManager tokenManager;
    private Cart currentCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        initViews();
        setupListeners();
        loadCartData();
    }

    private void initViews() {
        textViewItemCount = findViewById(R.id.textViewItemCount);
        textViewTotalPrice = findViewById(R.id.textViewTotalPrice);
        radioGroupAddress = findViewById(R.id.radioGroupAddress);
        radioDefaultAddress = findViewById(R.id.radioDefaultAddress);
        radioNewAddress = findViewById(R.id.radioNewAddress);
        textInputLayoutAddress = findViewById(R.id.textInputLayoutAddress);
        buttonCheckout = findViewById(R.id.buttonCheckout);
        progressBar = findViewById(R.id.progressBar);

        cartRepository = new CartRepository(this);
        tokenManager = new TokenManager(this);
    }

    private void setupListeners() {
        radioGroupAddress.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioNewAddress) {
                textInputLayoutAddress.setVisibility(View.VISIBLE);
            } else {
                textInputLayoutAddress.setVisibility(View.GONE);
            }
        });

        buttonCheckout.setOnClickListener(v -> handleCheckout());
    }

    private void loadCartData() {
        showLoading(true);
        cartRepository.getUserCart(new CartRepository.CartCallback() {
            @Override
            public void onSuccess(Cart cart) {
                showLoading(false);
                currentCart = cart;
                updateUI(cart);
            }

            @Override
            public void onError(String message) {
                showLoading(false);
                Toast.makeText(CheckoutActivity.this, message, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateUI(Cart cart) {
        if (cart.getCartItems() != null) {
            int itemCount = cart.getCartItems().size();
            textViewItemCount.setText(String.format("%d sản phẩm", itemCount));

            double total = cart.getCartItems().stream()
                    .mapToDouble(item -> item.getPrice() * item.getQuantity())
                    .sum();
            textViewTotalPrice.setText(formatPrice(total));
        }
    }

    private void handleCheckout() {
        if (!validateInput()) {
            return;
        }

        showLoading(true);
        buttonCheckout.setEnabled(false);

        if (radioNewAddress.isChecked() && textInputLayoutAddress.getEditText() != null) {
            String newAddress = textInputLayoutAddress.getEditText().getText().toString();
            cartRepository.checkoutWithAddress(newAddress, new CartRepository.OperationCallback() {
                @Override
                public void onSuccess() {
                    showLoading(false);
                    Toast.makeText(CheckoutActivity.this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }

                @Override
                public void onError(String message) {
                    showLoading(false);
                    buttonCheckout.setEnabled(true);
                    Toast.makeText(CheckoutActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            cartRepository.checkout(new CartRepository.OperationCallback() {
                @Override
                public void onSuccess() {
                    showLoading(false);
                    Toast.makeText(CheckoutActivity.this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }

                @Override
                public void onError(String message) {
                    showLoading(false);
                    buttonCheckout.setEnabled(true);
                    Toast.makeText(CheckoutActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean validateInput() {
        if (currentCart == null || currentCart.getCartItems() == null || currentCart.getCartItems().isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (radioNewAddress.isChecked()) {
            if (textInputLayoutAddress.getEditText() == null || 
                textInputLayoutAddress.getEditText().getText().toString().trim().isEmpty()) {
                textInputLayoutAddress.setError("Vui lòng nhập địa chỉ giao hàng");
                return false;
            }
            textInputLayoutAddress.setError(null);
        }

        return true;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(price) + " đ";
    }
} 