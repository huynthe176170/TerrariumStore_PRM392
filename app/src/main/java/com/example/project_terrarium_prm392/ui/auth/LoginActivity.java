package com.example.project_terrarium_prm392.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project_terrarium_prm392.MainActivity;
import com.example.project_terrarium_prm392.R;
import com.example.project_terrarium_prm392.api.ApiClient;
import com.example.project_terrarium_prm392.api.TerrariumApiService;
import com.example.project_terrarium_prm392.api.request.LoginRequest;
import com.example.project_terrarium_prm392.api.response.AuthResponse;
import com.example.project_terrarium_prm392.utils.TokenManager;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editTextUsername, editTextPassword;
    private Button buttonLogin, buttonBack;
    private ProgressBar progressBar;
    private TextView textViewRegister;
    private TokenManager tokenManager;
    private TerrariumApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI components
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        progressBar = findViewById(R.id.progressBar);
        textViewRegister = findViewById(R.id.textViewRegister);
        buttonBack = findViewById(R.id.buttonBack);

        // Initialize token manager and API service
        tokenManager = new TokenManager(this);
        apiService = ApiClient.getApiService();

        // Check if user is already logged in
        if (tokenManager.isLoggedIn()) {
            navigateToMainActivity();
            return;
        }
        
        // Kiểm tra nếu được mở từ nút Register
        if (getIntent().getBooleanExtra("SHOW_REGISTER", false)) {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            // Không finish() để người dùng có thể quay lại màn hình đăng nhập
        }

        // Set up click listeners
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
        
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Quay lại màn hình chính
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    private void login() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validate inputs
        if (username.isEmpty()) {
            editTextUsername.setError("Username is required");
            editTextUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        buttonLogin.setEnabled(false);

        // Create login request
        LoginRequest loginRequest = new LoginRequest(username, password);

        // Send login request to API
        apiService.login(loginRequest).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                progressBar.setVisibility(View.GONE);
                buttonLogin.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    
                    // Save user session
                    tokenManager.saveToken(authResponse.getToken());
                    tokenManager.saveUserId(authResponse.getUserId());
                    tokenManager.saveUsername(authResponse.getUsername());
                    tokenManager.saveUserRole(authResponse.getRole());

                    // Navigate to main activity
                    navigateToMainActivity();
                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                } else {
                    // Handle error
                    Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                buttonLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        // Xóa activity stack để người dùng không thể quay lại màn hình đăng nhập sau khi đã đăng nhập
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
} 