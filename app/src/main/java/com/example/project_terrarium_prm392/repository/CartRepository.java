package com.example.project_terrarium_prm392.repository;

import android.content.Context;
import android.util.Log;

import com.example.project_terrarium_prm392.api.ApiClient;
import com.example.project_terrarium_prm392.api.TerrariumApiService;
import com.example.project_terrarium_prm392.models.Cart;
import com.example.project_terrarium_prm392.models.CartItem;
import com.example.project_terrarium_prm392.models.CheckoutDTO;
import com.example.project_terrarium_prm392.utils.TokenManager;

import okhttp3.ResponseBody;
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
                    Log.d(TAG, "Cart received - Items count: " + 
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

        cartItem.setUserId(tokenManager.getUserId());
        String authHeader = tokenManager.getAuthorizationHeader();

        apiService.addItemToCart(authHeader, cartItem).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseString = response.body() != null ? response.body().string() : "";
                        Log.d(TAG, "Item added to cart successfully: " + responseString);
                        callback.onSuccess(cartItem);
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading response body", e);
                        callback.onSuccess(cartItem); // Still consider it successful if we can't read response
                    }
                } else {
                    String errorMessage = "Failed to add item to cart";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.e(TAG, "Add to cart failed: " + errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Network error adding item to cart", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void updateCartItem(CartItem cartItem, CartItemCallback callback) {
        if (!tokenManager.isLoggedIn()) {
            callback.onError("User not logged in");
            return;
        }

        cartItem.setUserId(tokenManager.getUserId());
        String authHeader = tokenManager.getAuthorizationHeader();

        apiService.updateCartItem(authHeader, cartItem).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseString = response.body() != null ? response.body().string() : "";
                        Log.d(TAG, "Cart item updated successfully: " + responseString);
                        callback.onSuccess(cartItem);
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading response body", e);
                        callback.onSuccess(cartItem); // Still consider it successful if we can't read response
                    }
                } else {
                    String errorMessage = "Failed to update cart item";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.e(TAG, "Update cart item failed: " + errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Network error updating cart item", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void removeCartItem(int cartItemId, int productId, OperationCallback callback) {
        if (!tokenManager.isLoggedIn()) {
            callback.onError("User not logged in");
            return;
        }

        int userId = tokenManager.getUserId();
        String authHeader = tokenManager.getAuthorizationHeader();

        apiService.removeCartItem(authHeader, userId, productId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Cart item removed successfully");
                    callback.onSuccess();
                } else {
                    String errorMessage = "Failed to remove cart item";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.e(TAG, "Remove cart item failed: " + errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Network error removing cart item", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getCartTotal(CartTotalCallback callback) {
        if (!tokenManager.isLoggedIn()) {
            callback.onError("User not logged in");
            return;
        }

        int userId = tokenManager.getUserId();
        String authHeader = tokenManager.getAuthorizationHeader();

        apiService.getCartTotal(authHeader, userId).enqueue(new Callback<Double>() {
            @Override
            public void onResponse(Call<Double> call, Response<Double> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Cart total retrieved successfully: " + response.body());
                    callback.onSuccess(response.body());
                } else {
                    String errorMessage = "Failed to get cart total";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.e(TAG, "Get cart total failed: " + errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<Double> call, Throwable t) {
                Log.e(TAG, "Network error getting cart total", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void checkout(OperationCallback callback) {
        if (!tokenManager.isLoggedIn()) {
            callback.onError("User not logged in");
            return;
        }

        int userId = tokenManager.getUserId();
        String authHeader = tokenManager.getAuthorizationHeader();

        apiService.checkout(authHeader, userId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseString = response.body() != null ? response.body().string() : "";
                        Log.d(TAG, "Checkout successful: " + responseString);
                        callback.onSuccess();
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading response body", e);
                        callback.onSuccess(); // Still consider it successful if we can't read response
                    }
                } else {
                    String errorMessage = "Failed to checkout";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.e(TAG, "Checkout failed: " + errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Network error during checkout", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void checkoutWithAddress(String shippingAddress, OperationCallback callback) {
        if (!tokenManager.isLoggedIn()) {
            callback.onError("User not logged in");
            return;
        }

        int userId = tokenManager.getUserId();
        String authHeader = tokenManager.getAuthorizationHeader();

        CheckoutDTO checkoutDto = new CheckoutDTO(userId, shippingAddress);

        apiService.checkoutWithAddress(authHeader, checkoutDto).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseString = response.body() != null ? response.body().string() : "";
                        Log.d(TAG, "Checkout successful: " + responseString);
                        callback.onSuccess();
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading response body", e);
                        callback.onSuccess(); // Still consider it successful if we can't read response
                    }
                } else {
                    String errorMessage = "Failed to checkout";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.e(TAG, "Checkout failed: " + errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Network error during checkout", t);
                callback.onError("Network error: " + t.getMessage());
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

    public interface CartTotalCallback {
        void onSuccess(double total);
        void onError(String message);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(String message);
    }
} 