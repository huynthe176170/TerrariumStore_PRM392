package com.example.project_terrarium_prm392.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.project_terrarium_prm392.R;
import com.example.project_terrarium_prm392.api.ApiClient;
import com.example.project_terrarium_prm392.api.TerrariumApiService;
import com.example.project_terrarium_prm392.models.Order;
import com.example.project_terrarium_prm392.ui.adapters.OrderAdapter;
import com.example.project_terrarium_prm392.utils.TokenManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderListActivity extends AppCompatActivity {

    private static final String TAG = "OrderListActivity";
    private RecyclerView recyclerViewOrders;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView textViewEmpty;
    private OrderAdapter orderAdapter;
    private TerrariumApiService apiService;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        // Khởi tạo Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Đơn hàng của tôi");

        // Khởi tạo API Service và TokenManager
        apiService = ApiClient.getApiService();
        tokenManager = new TokenManager(this);

        // Ánh xạ các view
        recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
        textViewEmpty = findViewById(R.id.textViewEmpty);

        // Cấu hình RecyclerView
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter = new OrderAdapter(this, new ArrayList<>());
        recyclerViewOrders.setAdapter(orderAdapter);

        // Cấu hình SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::fetchOrders);

        // Debug info
        Log.d(TAG, "Token: " + tokenManager.getToken());
        Log.d(TAG, "User ID: " + tokenManager.getUserId());
        Log.d(TAG, "Username: " + tokenManager.getUsername());

        // Load dữ liệu
        fetchOrders();
    }

    private void fetchOrders() {
        showLoading();

        String authToken = tokenManager.getAuthorizationHeader();
        Log.d(TAG, "Authorization Token: " + authToken);
        
        try {
            // Thông báo URL endpoint đang được gọi
            Log.d(TAG, "Fetching all orders from endpoint: order");
            Call<List<Order>> call = apiService.getAllOrders(authToken);
            
            call.enqueue(new Callback<List<Order>>() {
                @Override
                public void onResponse(@NonNull Call<List<Order>> call, @NonNull Response<List<Order>> response) {
                    hideLoading();
                    swipeRefreshLayout.setRefreshing(false);

                    if (response.isSuccessful() && response.body() != null) {
                        List<Order> orders = response.body();
                        Log.d(TAG, "All orders received: " + orders.size());
                        if (orders.isEmpty()) {
                            showEmptyView();
                        } else {
                            showOrderList(orders);
                        }
                    } else {
                        String errorMsg = "Không thể lấy danh sách đơn hàng";
                        int statusCode = response.code();
                        Log.e(TAG, "Error: " + statusCode + " - " + response.message());
                        Log.e(TAG, "Request URL: " + call.request().url());
                        
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
                        
                        // Hiển thị hướng dẫn để người dùng biết dịch vụ chưa sẵn sàng
                        if (statusCode == 404) {
                            textViewEmpty.setText("Dịch vụ đang bảo trì. \nVui lòng thử lại sau.\nMã lỗi: 404 Not Found");
                            textViewEmpty.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<Order>> call, @NonNull Throwable t) {
                    hideLoading();
                    swipeRefreshLayout.setRefreshing(false);
                    Log.e(TAG, "Network error: ", t);
                    showError("Lỗi kết nối: " + t.getMessage());
                    
                    // Hiển thị hướng dẫn cụ thể cho lỗi kết nối
                    textViewEmpty.setText("Không thể kết nối đến máy chủ.\nVui lòng kiểm tra kết nối mạng và thử lại.");
                    textViewEmpty.setVisibility(View.VISIBLE);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception when fetching orders", e);
            hideLoading();
            showError("Lỗi ứng dụng: " + e.getMessage());
        }
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewOrders.setVisibility(View.GONE);
        textViewEmpty.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void showOrderList(List<Order> orders) {
        recyclerViewOrders.setVisibility(View.VISIBLE);
        textViewEmpty.setVisibility(View.GONE);
        
        // Log chi tiết về đơn hàng
        int currentUserId = tokenManager.getUserId();
        Log.d(TAG, "Current user ID: " + currentUserId);
        
        for (Order order : orders) {
            Log.d(TAG, "Order ID: " + order.getId() + 
                    ", User ID: " + order.getUserId() + 
                    ", Date: " + order.getOrderDate() +
                    ", Status: " + order.getStatus() +
                    ", Total: " + order.getTotalPrice());
        }
        
        // Lọc danh sách đơn hàng theo userId
        List<Order> filteredOrders = new ArrayList<>();
        for (Order order : orders) {
            if (order.getUserId() == currentUserId) {
                filteredOrders.add(order);
                Log.d(TAG, "Added order to filtered list: " + order.getId());
            }
        }
        
        if (filteredOrders.isEmpty()) {
            Log.d(TAG, "No orders for current user after filtering");
            showEmptyView();
        } else {
            Log.d(TAG, "Showing " + filteredOrders.size() + " orders for user ID: " + currentUserId);
            orderAdapter.updateData(filteredOrders);
        }
    }

    private void showEmptyView() {
        recyclerViewOrders.setVisibility(View.GONE);
        textViewEmpty.setVisibility(View.VISIBLE);
        textViewEmpty.setText("Bạn chưa có đơn hàng nào.\nHãy mua sắm và đặt hàng!");
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error: " + message);
        textViewEmpty.setText("Có lỗi xảy ra: " + message);
        textViewEmpty.setVisibility(View.VISIBLE);
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