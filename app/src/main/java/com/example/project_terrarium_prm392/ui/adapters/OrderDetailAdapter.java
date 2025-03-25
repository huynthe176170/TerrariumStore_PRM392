package com.example.project_terrarium_prm392.ui.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project_terrarium_prm392.R;
import com.example.project_terrarium_prm392.api.ApiClient;
import com.example.project_terrarium_prm392.api.TerrariumApiService;
import com.example.project_terrarium_prm392.models.OrderDetail;
import com.example.project_terrarium_prm392.models.Product;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.OrderDetailViewHolder> {

    private static final String TAG = "OrderDetailAdapter";
    private final List<OrderDetail> orderDetailList;
    private final Context context;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final TerrariumApiService apiService;

    public OrderDetailAdapter(Context context, List<OrderDetail> orderDetailList) {
        this.context = context;
        this.orderDetailList = orderDetailList;
        this.apiService = ApiClient.getApiService();
        
        // Log thông tin debug
        for (OrderDetail detail : orderDetailList) {
            Log.d(TAG, "OrderDetail - ID: " + detail.getId() 
                    + ", ProductID: " + detail.getProductId() 
                    + ", Product: " + (detail.getProduct() != null ? detail.getProduct().getName() : "null")
                    + ", Price: " + detail.getPrice()
                    + ", Quantity: " + detail.getQuantity());
        }
    }

    @NonNull
    @Override
    public OrderDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_detail, parent, false);
        return new OrderDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDetailViewHolder holder, int position) {
        OrderDetail orderDetail = orderDetailList.get(position);
        
        // Hiển thị thông tin giá, số lượng và tổng tiền ngay lập tức
        holder.textProductPrice.setText(currencyFormat.format(orderDetail.getPrice()));
        holder.textProductQuantity.setText(String.valueOf(orderDetail.getQuantity()));
        
        double itemTotal = orderDetail.getPrice() * orderDetail.getQuantity();
        holder.textItemTotal.setText(currencyFormat.format(itemTotal));
        
        // Thiết lập placeholder trước
        holder.textProductName.setText("Đang tải...");
        holder.imageProduct.setImageResource(R.drawable.placeholder_image);
        
        // Cấu hình thông tin sản phẩm
        Product product = orderDetail.getProduct();
        if (product != null && product.getName() != null && !product.getName().isEmpty()) {
            Log.d(TAG, "Using cached product: " + product.getName());
            displayProductInfo(holder, product);
        } else {
            int productId = orderDetail.getProductId();
            Log.d(TAG, "Product not in cache, fetching product ID: " + productId);
            fetchProductInfo(productId, holder);
        }
    }

    private void displayProductInfo(OrderDetailViewHolder holder, Product product) {
        // Hiển thị tên sản phẩm
        if (product.getName() != null && !product.getName().isEmpty()) {
            holder.textProductName.setText(product.getName());
        } else {
            holder.textProductName.setText("Sản phẩm #" + product.getId());
        }
        
        // Load hình ảnh sản phẩm
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            String imageUrl = product.getImageUrl();
            
            // Xử lý URL nếu cần thiết
            if (!imageUrl.startsWith("http")) {
                // Thêm URL cơ sở nếu đường dẫn là tương đối
                imageUrl = "https://10.0.2.2:7024" + imageUrl;
            }
            
            Log.d(TAG, "Loading image from URL: " + imageUrl);
            
            try {
                Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .centerCrop()
                    .into(holder.imageProduct);
            } catch (Exception e) {
                Log.e(TAG, "Error loading image: " + e.getMessage());
                holder.imageProduct.setImageResource(R.drawable.error_image);
            }
        } else {
            Log.d(TAG, "No image URL for product: " + product.getId());
            holder.imageProduct.setImageResource(R.drawable.placeholder_image);
        }
    }
    
    private void fetchProductInfo(int productId, OrderDetailViewHolder holder) {
        Log.d(TAG, "Fetching product info for productId: " + productId);
        
        Call<Product> call = apiService.getProductById(productId);
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(@NonNull Call<Product> call, @NonNull Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Product product = response.body();
                    Log.d(TAG, "Received product: " + product.getName() + ", Image URL: " + product.getImageUrl());
                    displayProductInfo(holder, product);
                    
                    // Cập nhật product trong orderDetail để lần sau không cần gọi lại
                    for (OrderDetail detail : orderDetailList) {
                        if (detail.getProductId() == productId) {
                            detail.setProduct(product);
                        }
                    }
                } else {
                    int statusCode = response.code();
                    Log.e(TAG, "Failed to get product. Code: " + statusCode);
                    try {
                        if (response.errorBody() != null) {
                            Log.e(TAG, "Error Body: " + response.errorBody().string());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Product> call, @NonNull Throwable t) {
                Log.e(TAG, "Error fetching product: " + t.getMessage());
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderDetailList.size();
    }

    static class OrderDetailViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProduct;
        TextView textProductName;
        TextView textProductPrice;
        TextView textProductQuantity;
        TextView textItemTotal;

        public OrderDetailViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProduct = itemView.findViewById(R.id.imageProduct);
            textProductName = itemView.findViewById(R.id.textProductName);
            textProductPrice = itemView.findViewById(R.id.textProductPrice);
            textProductQuantity = itemView.findViewById(R.id.textProductQuantity);
            textItemTotal = itemView.findViewById(R.id.textItemTotal);
        }
    }
} 