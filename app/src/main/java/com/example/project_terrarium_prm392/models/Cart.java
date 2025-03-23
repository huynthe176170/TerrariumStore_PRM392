package com.example.project_terrarium_prm392.models;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private int id;
    private int userId;
    private User user;
    private List<CartItem> cartItems = new ArrayList<>();

    // Constructors
    public Cart() {
    }

    public Cart(int id, int userId) {
        this.id = id;
        this.userId = userId;
    }

    // Methods
    public double getTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    public void addCartItem(CartItem item) {
        this.cartItems.add(item);
    }

    public void removeCartItem(CartItem item) {
        this.cartItems.remove(item);
    }

    public void clearCart() {
        this.cartItems.clear();
    }
} 