package com.example.project_terrarium_prm392.ui.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project_terrarium_prm392.MainActivity;
import com.example.project_terrarium_prm392.R;
import com.example.project_terrarium_prm392.api.ApiClient;
import com.example.project_terrarium_prm392.api.TerrariumApiService;
import com.example.project_terrarium_prm392.api.request.UpdateProfileRequest;
import com.example.project_terrarium_prm392.models.User;
import com.example.project_terrarium_prm392.utils.TokenManager;
import com.google.android.material.textfield.TextInputEditText;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private TextInputEditText editTextFullName, editTextEmail, editTextPhone, editTextAddress;
    private TextInputEditText editTextCurrentPassword, editTextNewPassword;
    private Button buttonSaveProfile, buttonBack;
    private ProgressBar progressBar;
    private TerrariumApiService apiService;
    private TokenManager tokenManager;
    private String token;
    private int userId;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize components
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextCurrentPassword = findViewById(R.id.editTextCurrentPassword);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        buttonSaveProfile = findViewById(R.id.buttonSaveProfile);
        buttonBack = findViewById(R.id.buttonBack);
        progressBar = findViewById(R.id.progressBar);

        // Get token and user ID from TokenManager
        tokenManager = new TokenManager(this);
        token = tokenManager.getToken();
        userId = tokenManager.getUserId();

        // Check if user is logged in
        if (!tokenManager.isLoggedIn()) {
            Toast.makeText(this, "You need to login first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Initialize API service
        apiService = ApiClient.getApiService();

        // Load user profile
        loadUserProfile();

        // Set button click listeners
        buttonBack.setOnClickListener(v -> onBackPressed());
        
        buttonSaveProfile.setOnClickListener(v -> {
            if (validateInputs()) {
                updateProfile();
            }
        });
    }

    private void loadUserProfile() {
        progressBar.setVisibility(View.VISIBLE);
        
        Call<User> call = apiService.getUserProfile(tokenManager.getAuthorizationHeader());
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    displayUserInfo(currentUser);
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayUserInfo(User user) {
        editTextFullName.setText(user.getFullName());
        editTextEmail.setText(user.getEmail());
        editTextPhone.setText(user.getPhone());
        editTextAddress.setText(user.getAddress());
    }

    private boolean validateInputs() {
        boolean isValid = true;
        
        String fullName = editTextFullName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        
        if (TextUtils.isEmpty(fullName)) {
            editTextFullName.setError("Full name is required");
            isValid = false;
        }
        
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email");
            isValid = false;
        }
        
        if (TextUtils.isEmpty(phone)) {
            editTextPhone.setError("Phone number is required");
            isValid = false;
        }
        
        // Check password validation
        String currentPassword = editTextCurrentPassword.getText().toString().trim();
        String newPassword = editTextNewPassword.getText().toString().trim();
        
        if (!TextUtils.isEmpty(newPassword) && TextUtils.isEmpty(currentPassword)) {
            editTextCurrentPassword.setError("Current password is required to set a new password");
            isValid = false;
        }
        
        return isValid;
    }

    private void updateProfile() {
        progressBar.setVisibility(View.VISIBLE);
        
        String fullName = editTextFullName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        String currentPassword = editTextCurrentPassword.getText().toString().trim();
        String newPassword = editTextNewPassword.getText().toString().trim();
        
        UpdateProfileRequest request = new UpdateProfileRequest(
                fullName, 
                email, 
                phone, 
                address,
                TextUtils.isEmpty(newPassword) ? null : currentPassword,
                TextUtils.isEmpty(newPassword) ? null : newPassword
        );
        
        Call<ResponseBody> call = apiService.updateUserProfile(tokenManager.getAuthorizationHeader(), userId, request);
        
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    
                    // Clear password fields
                    editTextCurrentPassword.setText("");
                    editTextNewPassword.setText("");
                    
                    // Reload profile to get updated data
                    loadUserProfile();
                } else {
                    String errorMessage = "Failed to update profile";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                    Toast.makeText(ProfileActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
} 