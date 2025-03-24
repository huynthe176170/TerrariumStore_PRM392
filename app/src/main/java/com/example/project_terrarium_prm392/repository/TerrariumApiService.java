package com.example.project_terrarium_prm392.repository;

import com.example.project_terrarium_prm392.models.Cart;
import com.example.project_terrarium_prm392.models.CartItem;

import okhttp3.ResponseBody;
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
    @GET("cart/{userId}")
    Call<Cart> getUserCart(@Header("Authorization") String authHeader, @Path("userId") int userId);

    @POST("cart/add")
    Call<ResponseBody> addItemToCart(@Header("Authorization") String authHeader, @Body CartItem cartItem);

    @PUT("cart/update")
    Call<ResponseBody> updateCartItem(@Header("Authorization") String authHeader, @Body CartItem cartItem);

    @DELETE("cart/{userId}/remove")
    Call<Void> removeCartItem(@Header("Authorization") String authHeader, @Path("userId") int userId, @Query("productId") int productId);

    @GET("cart/{userId}/total")
    Call<Double> getCartTotal(@Header("Authorization") String authHeader, @Path("userId") int userId);
} 