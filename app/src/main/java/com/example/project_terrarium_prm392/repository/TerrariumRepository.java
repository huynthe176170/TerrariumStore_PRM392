package com.example.project_terrarium_prm392.repository;

import android.content.Context;
import android.util.Log;

import com.example.project_terrarium_prm392.api.ApiClient;
import com.example.project_terrarium_prm392.api.TerrariumApiService;
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
import com.example.project_terrarium_prm392.utils.TokenManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TerrariumRepository {
    private static final String TAG = "TerrariumRepository";
    
    private final TerrariumApiService apiService;
    private final TokenManager tokenManager;
    
    public TerrariumRepository(Context context) {
        this.apiService = ApiClient.getApiService();
        this.tokenManager = new TokenManager(context);
    }
    
    public TokenManager getTokenManager() {
        return tokenManager;
    }
    
    // Authentication methods
    public void login(String username, String password, final ApiCallback<AuthResponse> callback) {
        LoginRequest loginRequest = new LoginRequest(username, password);
        
        apiService.login(loginRequest).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    
                    // Save user data in shared preferences
                    tokenManager.saveToken(authResponse.getToken());
                    tokenManager.saveUserId(authResponse.getUserId());
                    tokenManager.saveUsername(authResponse.getUsername());
                    tokenManager.saveUserRole(authResponse.getRole());
                    
                    callback.onSuccess(authResponse);
                } else {
                    callback.onError("Login failed: " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
                Log.e(TAG, "Login error", t);
            }
        });
    }
    
    public void register(User user, final ApiCallback<User> callback) {
        // Create RegisterRequest from User object
        RegisterRequest registerRequest = new RegisterRequest(
            user.getUsername(), 
            user.getPasswordHash(), 
            user.getFullName(), 
            user.getEmail(), 
            user.getPhone(), 
            user.getAddress()
        );
        
        apiService.register(registerRequest).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Registration failed: " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
                Log.e(TAG, "Registration error", t);
            }
        });
    }
    
    public void logout() {
        tokenManager.clearAll();
    }
    
    // User methods
    public void getUserProfile(final ApiCallback<User> callback) {
        if (!tokenManager.isLoggedIn()) {
            callback.onError("User not logged in");
            return;
        }
        
        apiService.getUserProfile(tokenManager.getAuthorizationHeader()).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get user profile: " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
                Log.e(TAG, "Get user profile error", t);
            }
        });
    }
    
    // Category methods
    public void getAllCategories(final ApiCallback<List<Category>> callback) {
        apiService.getAllCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get categories: " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
                Log.e(TAG, "Get categories error", t);
            }
        });
    }
    
    // Product methods with RepositoryCallback
    public void getProducts(final RepositoryCallback<List<Product>> callback) {
        try {
            Log.d(TAG, "Đang gọi API để lấy sản phẩm từ URL: " + apiService.toString());
            
            // Sử dụng API thực, không dùng dữ liệu mẫu
            if (false) { // Đã thay đổi thành false để sử dụng API thực
                List<Product> mockProducts = createMockProducts();
                callback.onSuccess(mockProducts);
                return;
            }
            
            apiService.getAllProducts().enqueue(new Callback<List<Product>>() {
                @Override
                public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                    Log.d(TAG, "Nhận được phản hồi API: " + response.code());
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d(TAG, "API trả về dữ liệu thành công: " + response.body().size() + " sản phẩm");
                        callback.onSuccess(response.body());
                    } else {
                        String errorMsg = "Failed to get products: " + response.message();
                        Log.e(TAG, errorMsg + ", code: " + response.code());
                        callback.onError(new Exception(errorMsg));
                    }
                }
                
                @Override
                public void onFailure(Call<List<Product>> call, Throwable t) {
                    String errorMsg = "Network error: " + t.getMessage();
                    Log.e(TAG, errorMsg, t);
                    callback.onError(new Exception(errorMsg, t));
                }
            });
        } catch (Exception e) {
            String errorMsg = "Repository exception: " + e.getMessage();
            Log.e(TAG, errorMsg, e);
            callback.onError(e);
        }
    }
    
    private List<Product> createMockProducts() {
        List<Product> products = new ArrayList<>();
        
        // Tạo 6 sản phẩm mẫu
        for (int i = 1; i <= 6; i++) {
            Product product = new Product();
            product.setId(i);
            product.setName("Terrarium Plant " + i);
            product.setPrice(10.99 + i);
            product.setDescription("Beautiful plant for your terrarium. Perfect for small spaces.");
            product.setStockQuantity(10 + i);
            product.setCategoryId(1);
            product.setCategory("Plants");
            
            // Thêm URL hình ảnh (các URL này không hoạt động, chỉ để test)
            product.setImageUrl("https://example.com/terrarium" + i + ".jpg");
            
            products.add(product);
        }
        
        return products;
    }
    
    public void getProductsByCategory(int categoryId, final ApiCallback<List<Product>> callback) {
        apiService.getProductsByCategory(categoryId).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get products by category: " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
                Log.e(TAG, "Get products by category error", t);
            }
        });
    }
    
    public void getProductById(int productId, final RepositoryCallback<Product> callback) {
        try {
            Log.d(TAG, "Đang gọi API để lấy chi tiết sản phẩm ID=" + productId);
            
            apiService.getProductById(productId).enqueue(new Callback<Product>() {
                @Override
                public void onResponse(Call<Product> call, Response<Product> response) {
                    Log.d(TAG, "Nhận được phản hồi API chi tiết sản phẩm: " + response.code());
                    if (response.isSuccessful() && response.body() != null) {
                        Product product = response.body();
                        Log.d(TAG, "Lấy chi tiết sản phẩm thành công: " + product.getName());
                        callback.onSuccess(product);
                    } else {
                        String errorMsg = "Không thể lấy chi tiết sản phẩm: " + response.message();
                        Log.e(TAG, errorMsg + ", code: " + response.code());
                        callback.onError(new Exception(errorMsg));
                    }
                }
                
                @Override
                public void onFailure(Call<Product> call, Throwable t) {
                    String errorMsg = "Lỗi kết nối: " + t.getMessage();
                    Log.e(TAG, errorMsg, t);
                    callback.onError(new Exception(errorMsg, t));
                }
            });
        } catch (Exception e) {
            String errorMsg = "Lỗi khi lấy chi tiết sản phẩm: " + e.getMessage();
            Log.e(TAG, errorMsg, e);
            callback.onError(e);
        }
    }
    
    // Cart methods
    public void getUserCart(final ApiCallback<Cart> callback) {
        if (!tokenManager.isLoggedIn()) {
            callback.onError("User not logged in");
            return;
        }
        
        int userId = tokenManager.getUserId();
        if (userId == -1) {
            callback.onError("User ID not found");
            return;
        }
        
        apiService.getUserCart(tokenManager.getAuthorizationHeader(), userId).enqueue(new Callback<Cart>() {
            @Override
            public void onResponse(Call<Cart> call, Response<Cart> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get cart: " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<Cart> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
                Log.e(TAG, "Get cart error", t);
            }
        });
    }
    
    public void addItemToCart(CartItem cartItem, final ApiCallback<CartItem> callback) {
        if (!tokenManager.isLoggedIn()) {
            callback.onError("User not logged in");
            return;
        }
        
        apiService.addItemToCart(tokenManager.getAuthorizationHeader(), cartItem).enqueue(new Callback<CartItem>() {
            @Override
            public void onResponse(Call<CartItem> call, Response<CartItem> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to add item to cart: " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<CartItem> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
                Log.e(TAG, "Add item to cart error", t);
            }
        });
    }
    
    // Order methods
    public void getUserOrders(final ApiCallback<List<Order>> callback) {
        if (!tokenManager.isLoggedIn()) {
            callback.onError("User not logged in");
            return;
        }
        
        apiService.getUserOrders(tokenManager.getAuthorizationHeader()).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to get orders: " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
                Log.e(TAG, "Get orders error", t);
            }
        });
    }
    
    public void createOrder(Order order, final ApiCallback<Order> callback) {
        if (!tokenManager.isLoggedIn()) {
            callback.onError("User not logged in");
            return;
        }
        
        apiService.createOrder(tokenManager.getAuthorizationHeader(), order).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to create order: " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
                Log.e(TAG, "Create order error", t);
            }
        });
    }
    
    // Payment methods
    public void createPayment(Payment payment, final ApiCallback<Payment> callback) {
        if (!tokenManager.isLoggedIn()) {
            callback.onError("User not logged in");
            return;
        }
        
        apiService.createPayment(tokenManager.getAuthorizationHeader(), payment).enqueue(new Callback<Payment>() {
            @Override
            public void onResponse(Call<Payment> call, Response<Payment> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to create payment: " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<Payment> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
                Log.e(TAG, "Create payment error", t);
            }
        });
    }
    
    // Callback interface for API calls
    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }
    
    // New callback interface that accepts Exception
    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
} 