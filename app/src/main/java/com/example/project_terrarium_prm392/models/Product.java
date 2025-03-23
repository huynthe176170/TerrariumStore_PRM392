package com.example.project_terrarium_prm392.models;

import com.google.gson.annotations.SerializedName;

public class Product {
    private int id;
    private String name;
    private String description;
    private double price;
    
    @SerializedName("imageUrl")
    private String imageUrl;
    
    @SerializedName("stockQuantity")
    private int stockQuantity;
    
    @SerializedName("categoryId")
    private int categoryId;
    
    @SerializedName("categoryName")
    private String category;
    
    public Product() {
        // Empty constructor
    }
    
    // Getters and setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public int getStockQuantity() {
        return stockQuantity;
    }
    
    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
    
    public int getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
} 