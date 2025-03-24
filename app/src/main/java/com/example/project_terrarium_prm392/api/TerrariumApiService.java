package com.example.project_terrarium_prm392.api;

import com.example.project_terrarium_prm392.api.request.LoginRequest;
import com.example.project_terrarium_prm392.api.request.RegisterRequest;
import com.example.project_terrarium_prm392.api.response.AuthResponse;
import com.example.project_terrarium_prm392.models.Cart;
import com.example.project_terrarium_prm392.models.CartItem;
import com.example.project_terrarium_prm392.models.Category;
import com.example.project_terrarium_prm392.models.Order;
import com.example.project_terrarium_prm392.models.Payment;
import com.example.project_terrarium_prm392.models.Product;
import com.example.project_terrarium_prm392.models.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TerrariumApiService {
    
    // Authentication
    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest loginRequest);
    
    @POST("auth/register")
    Call<User> register(@Body RegisterRequest registerRequest);
    
    // User endpoints
    @GET("auth/profile")
    Call<User> getUserProfile(@Header("Authorization") String token);
    
    @PUT("users/profile")
    Call<User> updateUserProfile(@Header("Authorization") String token, @Body User user);
    
    // Category endpoints
    @GET("category")
    Call<List<Category>> getAllCategories();
    
    @GET("categories/{id}")
    Call<Category> getCategoryById(@Path("id") int categoryId);
    
    // Product endpoints
    @GET("product")
    Call<List<Product>> getAllProducts();
    
    @GET("product")
    Call<List<Product>> getProductsWithOptions(
        @Query("ascending") boolean ascending,
        @Query("pageNumber") int pageNumber,
        @Query("pageSize") int pageSize
    );
    
    @GET("product/category/{categoryId}")
    Call<List<Product>> getProductsByCategory(@Path("categoryId") int categoryId);
    
    @GET("product/{id}")
    Call<Product> getProductById(@Path("id") int productId);
    
    @GET("products/search")
    Call<List<Product>> searchProducts(@Query("query") String searchQuery);
    
    // Cart endpoints
    @GET("cart/{userId}")
    Call<Cart> getUserCart(@Header("Authorization") String authHeader, @Path("userId") int userId);
    
    @POST("cart/add")
    Call<CartItem> addItemToCart(@Header("Authorization") String authHeader, @Body CartItem cartItem);
    
    @PUT("cart/update-quantity")
    Call<okhttp3.ResponseBody> updateCartItem(@Header("Authorization") String authHeader, @Body CartItem cartItem);
    
    @DELETE("cart/remove/{userId}/{productId}")
    Call<Void> removeCartItem(@Header("Authorization") String authHeader, @Path("userId") int userId, @Path("productId") int productId);
    
    @GET("cart/total/{userId}")
    Call<Double> getCartTotal(@Header("Authorization") String authHeader, @Path("userId") int userId);
    
    // Order endpoints
    @GET("order")
    Call<List<Order>> getUserOrders(@Header("Authorization") String token);
    
    @GET("orders/{id}")
    Call<Order> getOrderById(@Header("Authorization") String token, @Path("id") int orderId);
    
    @POST("order")
    Call<Order> createOrder(@Header("Authorization") String token, @Body Order order);
    
    // Payment endpoints
    @POST("payment")
    Call<Payment> createPayment(@Header("Authorization") String token, @Body Payment payment);
    
    @GET("payments/order/{orderId}")
    Call<Payment> getPaymentByOrderId(@Header("Authorization") String token, @Path("orderId") int orderId);
} 