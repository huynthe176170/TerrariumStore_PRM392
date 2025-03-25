package com.example.project_terrarium_prm392.api.request;

public class UpdateProfileRequest {
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String currentPassword;
    private String newPassword;

    public UpdateProfileRequest() {
    }

    public UpdateProfileRequest(String fullName, String email, String phone, String address) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    public UpdateProfileRequest(String fullName, String email, String phone, String address, String currentPassword, String newPassword) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
} 