package com.example.project_terrarium_prm392.models;

public class JwtSettings {
    private String secretKey;
    private int expirationMinutes;

    // Constructors
    public JwtSettings() {
    }

    public JwtSettings(String secretKey, int expirationMinutes) {
        this.secretKey = secretKey;
        this.expirationMinutes = expirationMinutes;
    }

    // Getters and Setters
    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public int getExpirationMinutes() {
        return expirationMinutes;
    }

    public void setExpirationMinutes(int expirationMinutes) {
        this.expirationMinutes = expirationMinutes;
    }
} 