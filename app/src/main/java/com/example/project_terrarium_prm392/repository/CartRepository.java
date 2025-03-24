package com.example.project_terrarium_prm392.repository;

import android.content.Context;
import android.util.Log;

import com.example.project_terrarium_prm392.api.ApiClient;
import com.example.project_terrarium_prm392.api.TerrariumApiService;
import com.example.project_terrarium_prm392.models.Cart;
import com.example.project_terrarium_prm392.models.CartItem;
import com.example.project_terrarium_prm392.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartRepository {
    private static final String TAG = "CartRepository";
    private final TerrariumApiService apiService;
    private final Context context;
    private final TokenManager tokenManager;

    public CartRepository(Context context) {
        this.context = context;
        this.apiService = ApiClient.getApiService();
        this.tokenManager = new TokenManager(context);
    }

    public void getUserCart(CartCallback callback) {
        if (!tokenManager.isLoggedIn()) {
            callback.onError("User not logged in");
            return;
        }

        int userId = tokenManager.getUserId();
        Log.d(TAG, "Getting cart for userId: " + userId);
        
        if (userId == -1) {
            callback.onError("User ID not found");
            return;
        }

        String authHeader = tokenManager.getAuthorizationHeader();
        Log.d(TAG, "Authorization header: " + authHeader);

        apiService.getUserCart(authHeader, userId).enqueue(new Callback<Cart>() {
            @Override
            public void onResponse(Call<Cart> call, Response<Cart> response) {
                Log.d(TAG, "Response code: " + response.code());
                Log.d(TAG, "Response message: " + response.message());
                if (!response.isSuccessful()) {
                    try {
                        Log.e(TAG, "Error body: " + response.errorBody().string());
                    } catch (Exception e) {
                        Log.e(TAG, "Could not read error body", e);
                    }
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    Cart cart = response.body();
                    Log.d(TAG, "Cart received - ID: " + cart.getId() + ", Items count: " + 
                          (cart.getCartItems() != null ? cart.getCartItems().size() : 0));
                    callback.onSuccess(cart);
                } else {
                    callback.onError("Failed to get cart: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Cart> call, Throwable t) {
                Log.e(TAG, "Network error getting cart", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void addItemToCart(CartItem cartItem, CartItemCallback callback) {
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
                Log.e(TAG, "Add to cart error", t);
            }
        });
    }

    public void updateCartItem(CartItem cartItem, CartItemCallback callback) {
        if (!tokenManager.isLoggedIn()) {
            callback.onError("User not logged in");
            return;
        }

        apiService.updateCartItem(tokenManager.getAuthorizationHeader(), cartItem.getId(), cartItem).enqueue(new Callback<CartItem>() {
            @Override
            public void onResponse(Call<CartItem> call, Response<CartItem> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to update cart item: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<CartItem> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
                Log.e(TAG, "Update cart item error", t);
            }
        });
    }

    public void removeCartItem(int cartItemId, OperationCallback callback) {
        if (!tokenManager.isLoggedIn()) {
            callback.onError("User not logged in");
            return;
        }

        apiService.removeCartItem(tokenManager.getAuthorizationHeader(), cartItemId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("Failed to remove cart item: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
                Log.e(TAG, "Remove cart item error", t);
            }
        });
    }

    public interface CartCallback {
        void onSuccess(Cart cart);
        void onError(String message);
    }

    public interface CartItemCallback {
        void onSuccess(CartItem cartItem);
        void onError(String message);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(String message);
    }
} 