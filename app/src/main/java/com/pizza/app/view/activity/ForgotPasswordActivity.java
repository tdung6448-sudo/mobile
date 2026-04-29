package com.pizza.app.view.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.pizza.app.databinding.ActivityForgotPasswordBinding;
import com.pizza.app.util.ValidationUtils;
import com.pizza.app.viewmodel.AuthViewModel;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private AuthViewModel                 viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding   = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnSendReset.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            if (!ValidationUtils.isValidEmail(email)) {
                binding.tilEmail.setError("Email không hợp lệ");
                return;
            }
            binding.tilEmail.setError(null);
            setLoading(true);

            viewModel.sendPasswordReset(email).observe(this, result -> {
                if (result.isLoading()) return;
                setLoading(false);
                if (result.isSuccess()) {
                    Toast.makeText(this,
                            "Đã gửi link đặt lại mật khẩu. Kiểm tra hộp thư email của bạn.",
                            Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSendReset.setEnabled(!loading);
    }
}
