package com.example.project_terrarium_prm392.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Order {
    private int id;
    private int userId;
    private User user;
    private Date orderDate;
    private double totalPrice;
    private String status;
    private String shippingAddress;
    private List<OrderDetail> orderDetails = new ArrayList<>();

    // Constructors
    public Order() {
        this.orderDate = new Date();
        this.status = "Pending";
    }

    public Order(int id, int userId, double totalPrice, String shippingAddress) {
        this.id = id;
        this.userId = userId;
        this.orderDate = new Date();
        this.totalPrice = totalPrice;
        this.status = "Pending";
        this.shippingAddress = shippingAddress;
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

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public List<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }
} 