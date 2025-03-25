package com.example.project_terrarium_prm392.ui;

import android.graphics.Bitmap;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_terrarium_prm392.R;
import com.example.project_terrarium_prm392.api.ApiClient;
import com.example.project_terrarium_prm392.api.TerrariumApiService;
import com.example.project_terrarium_prm392.models.Order;
import com.example.project_terrarium_prm392.models.OrderDetail;
import com.example.project_terrarium_prm392.ui.adapters.OrderDetailAdapter;
import com.example.project_terrarium_prm392.utils.QRCodeGenerator;
import com.example.project_terrarium_prm392.utils.TokenManager;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailActivity extends AppCompatActivity {

    private static final String TAG = "OrderDetailActivity";
    private TextView textOrderId;
    private TextView textOrderDate;
    private TextView textOrderStatus;
    private TextView textShippingAddress;
    private TextView textSubtotal;
    private TextView textShippingFee;
    private TextView textTotal;
    private RecyclerView recyclerViewOrderItems;
    private ProgressBar progressBar;
    
    // QR Code Payment
    private ImageView imageQRCode;
    private TextView textPaymentInfo;
    private TextView textAmount;
    private Button buttonPaymentCompleted;
    
    // Seller Info for QR Code
    private static final String SELLER_NAME = "NGUYEN TRUONG HUY";
    private static final String SELLER_PHONE = "8666771508";

    private TerrariumApiService apiService;
    private TokenManager tokenManager;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private OrderDetailAdapter orderDetailAdapter;

    private int orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        // Lấy orderId từ Intent
        orderId = getIntent().getIntExtra("ORDER_ID", -1);
        if (orderId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Chi tiết đơn hàng #" + orderId);

        // Khởi tạo API Service và TokenManager
        apiService = ApiClient.getApiService();
        tokenManager = new TokenManager(this);

        // Debug info
        Log.d(TAG, "Order ID: " + orderId);
        Log.d(TAG, "Token: " + tokenManager.getToken());
        Log.d(TAG, "User ID: " + tokenManager.getUserId());

        // Ánh xạ các view
        textOrderId = findViewById(R.id.textOrderId);
        textOrderDate = findViewById(R.id.textOrderDate);
        textOrderStatus = findViewById(R.id.textOrderStatus);
        textShippingAddress = findViewById(R.id.textShippingAddress);
        textSubtotal = findViewById(R.id.textSubtotal);
        textShippingFee = findViewById(R.id.textShippingFee);
        textTotal = findViewById(R.id.textTotal);
        recyclerViewOrderItems = findViewById(R.id.recyclerViewOrderItems);
        progressBar = findViewById(R.id.progressBar);
        
        // Ánh xạ các view QR code
        imageQRCode = findViewById(R.id.imageQRCode);
        textPaymentInfo = findViewById(R.id.textPaymentInfo);
        textAmount = findViewById(R.id.textAmount);
        buttonPaymentCompleted = findViewById(R.id.buttonPaymentCompleted);
        
        // Thiết lập thông tin thanh toán
        textPaymentInfo.setText(SELLER_NAME + "\n" + SELLER_PHONE);
        
        // Thiết lập sự kiện nút "Đã thanh toán"
        buttonPaymentCompleted.setOnClickListener(v -> {
            Toast.makeText(OrderDetailActivity.this, "Cảm ơn bạn đã thanh toán! Chúng tôi sẽ xử lý đơn hàng của bạn.", Toast.LENGTH_LONG).show();
            buttonPaymentCompleted.setEnabled(false);
            buttonPaymentCompleted.setText("Đã xác nhận thanh toán");
            // Có thể gọi API để cập nhật trạng thái thanh toán ở đây
        });

        // Cấu hình RecyclerView
        recyclerViewOrderItems.setLayoutManager(new LinearLayoutManager(this));
        orderDetailAdapter = new OrderDetailAdapter(this, new ArrayList<>());
        recyclerViewOrderItems.setAdapter(orderDetailAdapter);

        // Load dữ liệu
        fetchOrderDetail();
    }

    private void fetchOrderDetail() {
        showLoading();

        String authToken = tokenManager.getAuthorizationHeader();
        Log.d(TAG, "Authorization Token: " + authToken);
        
        Call<Order> call = apiService.getOrderById(authToken, orderId);

        call.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(@NonNull Call<Order> call, @NonNull Response<Order> response) {
                hideLoading();

                if (response.isSuccessful() && response.body() != null) {
                    Order order = response.body();
                    Log.d(TAG, "Order received: " + order.getId());
                    displayOrderDetail(order);
                    generateQRCode(order); // Tạo QR code cho đơn hàng
                } else {
                    String errorMsg = "Không thể lấy chi tiết đơn hàng";
                    int statusCode = response.code();
                    Log.e(TAG, "Error: " + statusCode + " - " + response.message());
                    
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                            errorMsg += ": " + errorBody;
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    
                    showError(errorMsg + " (Code: " + statusCode + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Order> call, @NonNull Throwable t) {
                hideLoading();
                Log.e(TAG, "Network error: ", t);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
    
    /**
     * Tạo QR code cho đơn hàng
     */
    private void generateQRCode(Order order) {
        // Hiển thị số tiền
        double totalAmount = order.getTotalPrice();
        textAmount.setText(currencyFormat.format(totalAmount));
        
        // Tạo nội dung QR code
        String qrContent = QRCodeGenerator.createPaymentContent(
                String.valueOf(order.getId()),
                totalAmount,
                SELLER_NAME,
                SELLER_PHONE
        );
        
        // Tạo ảnh QR code
        Bitmap qrBitmap = QRCodeGenerator.generateQRCode(qrContent, 500, 500);
        
        // Hiển thị QR code
        if (qrBitmap != null) {
            imageQRCode.setImageBitmap(qrBitmap);
        } else {
            Toast.makeText(this, "Không thể tạo mã QR", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayOrderDetail(Order order) {
        // Hiển thị thông tin cơ bản của đơn hàng
        textOrderId.setText(String.valueOf(order.getId()));
        textOrderDate.setText(dateFormat.format(order.getOrderDate()));
        
        // Xử lý trạng thái
        String statusText = getStatusText(order.getStatus());
        textOrderStatus.setText(statusText);
        textOrderStatus.setBackgroundResource(getStatusBackground(order.getStatus()));
        
        // Hiển thị địa chỉ giao hàng
        textShippingAddress.setText(order.getShippingAddress());
        
        // Tính toán và hiển thị thông tin thanh toán
        double subtotal = calculateSubtotal(order);
        double shippingFee = 10000; // Hardcoded phí vận chuyển, có thể lấy từ API
        double total = order.getTotalPrice();
        
        textSubtotal.setText(currencyFormat.format(subtotal));
        textShippingFee.setText(currencyFormat.format(shippingFee));
        textTotal.setText(currencyFormat.format(total));
        
        // Hiển thị danh sách sản phẩm
        if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            orderDetailAdapter = new OrderDetailAdapter(this, order.getOrderDetails());
            recyclerViewOrderItems.setAdapter(orderDetailAdapter);
        }
    }

    private double calculateSubtotal(Order order) {
        double subtotal = 0;
        if (order.getOrderDetails() != null) {
            for (OrderDetail detail : order.getOrderDetails()) {
                subtotal += detail.getPrice() * detail.getQuantity();
            }
        }
        return subtotal;
    }

    private String getStatusText(String status) {
        switch (status.toLowerCase()) {
            case "pending":
                return "Chờ xác nhận";
            case "confirmed":
                return "Đã xác nhận";
            case "shipping":
                return "Đang giao hàng";
            case "delivered":
                return "Đã giao hàng";
            case "cancelled":
                return "Đã hủy";
            default:
                return status;
        }
    }

    private int getStatusBackground(String status) {
        // Có thể tạo các drawable khác nhau cho các trạng thái khác nhau
        return R.drawable.status_background;
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error: " + message);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 