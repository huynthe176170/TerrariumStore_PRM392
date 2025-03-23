package com.example.project_terrarium_prm392.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project_terrarium_prm392.R;
import com.example.project_terrarium_prm392.api.ApiClient;
import com.example.project_terrarium_prm392.api.TerrariumApiService;
import com.example.project_terrarium_prm392.api.request.RegisterRequest;
import com.example.project_terrarium_prm392.models.User;
import com.example.project_terrarium_prm392.repository.TerrariumRepository;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText editTextFullName, editTextEmail, editTextPhone, editTextAddress, editTextUsername, editTextPassword, editTextConfirmPassword;
    private Button buttonRegister;
    private ProgressBar progressBar;
    private TextView textViewLogin;
    private TerrariumApiService apiService;
    private TerrariumRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize UI components
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        progressBar = findViewById(R.id.progressBar);
        textViewLogin = findViewById(R.id.textViewLogin);

        // Initialize API service and repository
        apiService = ApiClient.getApiService();
        repository = new TerrariumRepository(this);

        // Set up click listeners
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Return to login activity
            }
        });
    }

    private void register() {
        // Get input values
        String fullName = editTextFullName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // Validate inputs
        if (fullName.isEmpty()) {
            editTextFullName.setError("Full name is required");
            editTextFullName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email");
            editTextEmail.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            editTextPhone.setError("Phone number is required");
            editTextPhone.requestFocus();
            return;
        }

        if (address.isEmpty()) {
            editTextAddress.setError("Address is required");
            editTextAddress.requestFocus();
            return;
        }

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

        if (password.length() < 6) {
            editTextPassword.setError("Password should be at least 6 characters");
            editTextPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords don't match");
            editTextConfirmPassword.requestFocus();
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        buttonRegister.setEnabled(false);

        // Create register request directly
        RegisterRequest registerRequest = new RegisterRequest(username, password, fullName, email, phone, address);

        // Send registration request to API
        apiService.register(registerRequest).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                progressBar.setVisibility(View.GONE);
                buttonRegister.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RegisterActivity.this, "Registration successful! Please login.", Toast.LENGTH_LONG).show();
                    // Navigate back to login screen
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    // Handle error
                    String errorMessage = "Registration failed";
                    if (response.code() == 400) {
                        errorMessage = "Username already exists";
                    }
                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                buttonRegister.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Alternative approach using the repository
        /*
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setAddress(address);
        user.setUsername(username);
        user.setPasswordHash(password); // Note: The api will hash this password
        
        repository.register(user, new TerrariumRepository.ApiCallback<User>() {
            @Override
            public void onSuccess(User result) {
                progressBar.setVisibility(View.GONE);
                buttonRegister.setEnabled(true);
                
                Toast.makeText(RegisterActivity.this, "Registration successful! Please login.", Toast.LENGTH_LONG).show();
                // Navigate back to login screen
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                buttonRegister.setEnabled(true);
                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
        */
    }
} 