package com.example.project_terrarium_prm392.models;

public class CheckoutDTO {
    private int userId;
    private String shippingAddress;

    public CheckoutDTO(int userId, String shippingAddress) {
        this.userId = userId;
        this.shippingAddress = shippingAddress;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
} 