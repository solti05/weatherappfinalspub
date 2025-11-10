package com.example.weatherappfinals;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weatherappfinals.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth auth;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        progress = new ProgressDialog(this);
        progress.setCancelable(false);
        progress.setMessage("Creating account...");

        setupClickListeners();
    }

    private void setupClickListeners() {
        // Sign Up Button
        binding.btnSignUp.setOnClickListener(v -> {
            String fullName = binding.etFullName.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

            if (validateInput(fullName, email, password, confirmPassword)) {
                registerUser(fullName, email, password);
            }
        });

        // Log In Text - Go back to login
        binding.tvLogin.setOnClickListener(v -> finish());
    }

    private boolean validateInput(String fullName, String email, String password, String confirmPassword) {
        // Validate Full Name
        if (TextUtils.isEmpty(fullName)) {
            binding.tilFullName.setError("Please enter your full name");
            binding.etFullName.requestFocus();
            return false;
        }
        binding.tilFullName.setError(null);

        // Validate Email
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError("Please enter your email");
            binding.etEmail.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Please enter a valid email");
            binding.etEmail.requestFocus();
            return false;
        }
        binding.tilEmail.setError(null);

        // Validate Password
        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("Please create a password");
            binding.etPassword.requestFocus();
            return false;
        }
        if (password.length() < 6) {
            binding.tilPassword.setError("Password must be at least 6 characters");
            binding.etPassword.requestFocus();
            return false;
        }
        binding.tilPassword.setError(null);

        // Validate Confirm Password
        if (TextUtils.isEmpty(confirmPassword)) {
            binding.tilConfirmPassword.setError("Please confirm your password");
            binding.etConfirmPassword.requestFocus();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError("Passwords do not match");
            binding.etConfirmPassword.requestFocus();
            return false;
        }
        binding.tilConfirmPassword.setError(null);

        return true;
    }

    private void setLoading(boolean loading) {
        binding.btnSignUp.setEnabled(!loading);
        binding.tvLogin.setEnabled(!loading);
        if (loading) {
            progress.show();
            binding.btnSignUp.setText("Creating Account...");
        } else {
            progress.dismiss();
            binding.btnSignUp.setText(R.string.sign_up);
        }
    }

    private void registerUser(String fullName, String email, String password) {
        setLoading(true);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Update user profile with name
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(fullName)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        setLoading(false);

                                        if (profileTask.isSuccessful()) {
                                            Toast.makeText(
                                                    RegisterActivity.this,
                                                    "Account created successfully!",
                                                    Toast.LENGTH_SHORT
                                            ).show();

                                            // Navigate to main activity
                                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(
                                                    RegisterActivity.this,
                                                    "Profile update failed",
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                        }
                                    });
                        }
                    } else {
                        setLoading(false);

                        // Show error message
                        String errorMessage = task.getException() != null
                                ? task.getException().getMessage()
                                : "Registration failed";
                        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progress != null && progress.isShowing()) {
            progress.dismiss();
        }
    }
}