package com.pizza.app.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.pizza.app.databinding.ActivityRegisterBinding;
import com.pizza.app.util.ValidationUtils;
import com.pizza.app.viewmodel.AuthViewModel;

/**
 * Màn hình đăng ký tài khoản mới
 */
public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private AuthViewModel           viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding   = ActivityRegisterBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnRegister.setOnClickListener(v -> attemptRegister());
        binding.tvLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String name     = binding.etName.getText().toString().trim();
        String email    = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString();
        String confirm  = binding.etConfirmPassword.getText().toString();

        // Validate từng trường
        boolean valid = true;
        if (!ValidationUtils.isNotEmpty(name)) {
            binding.tilName.setError("Nhập họ tên"); valid = false;
        } else {
            binding.tilName.setError(null);
        }
        if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.setError("Email không hợp lệ"); valid = false;
        } else {
            binding.tilEmail.setError(null);
        }
        if (!ValidationUtils.isValidPassword(password)) {
            binding.tilPassword.setError("Mật khẩu tối thiểu 8 ký tự, gồm chữ và số"); valid = false;
        } else {
            binding.tilPassword.setError(null);
        }
        if (!ValidationUtils.isPasswordMatch(password, confirm)) {
            binding.tilConfirmPassword.setError("Mật khẩu xác nhận không khớp"); valid = false;
        } else {
            binding.tilConfirmPassword.setError(null);
        }

        if (!valid) return;

        setLoading(true);
        viewModel.register(name, email, password).observe(this, result -> {
            if (result.isLoading()) return;
            setLoading(false);
            if (result.isSuccess()) {
                Toast.makeText(this,
                        "Đăng ký thành công! Kiểm tra email để xác thực tài khoản.",
                        Toast.LENGTH_LONG).show();
                // Chuyển về Login
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnRegister.setEnabled(!loading);
    }
}
