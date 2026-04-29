package com.pizza.app.view.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.pizza.app.databinding.ActivityChangePasswordBinding;
import com.pizza.app.util.ValidationUtils;
import com.pizza.app.viewmodel.AuthViewModel;

public class ChangePasswordActivity extends AppCompatActivity {

    private ActivityChangePasswordBinding binding;
    private AuthViewModel                 viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding   = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnChange.setOnClickListener(v -> attemptChange());
    }

    private void attemptChange() {
        String current = binding.etCurrentPassword.getText().toString();
        String newPw   = binding.etNewPassword.getText().toString();
        String confirm = binding.etConfirmPassword.getText().toString();

        if (!ValidationUtils.isValidPassword(newPw)) {
            binding.tilNewPassword.setError("Mật khẩu tối thiểu 8 ký tự, gồm chữ và số");
            return;
        }
        if (!ValidationUtils.isPasswordMatch(newPw, confirm)) {
            binding.tilConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            return;
        }

        binding.tilNewPassword.setError(null);
        binding.tilConfirmPassword.setError(null);
        setLoading(true);

        viewModel.changePassword(current, newPw).observe(this, result -> {
            setLoading(false);
            if (result.isLoading()) return;
            if (result.isSuccess()) {
                Toast.makeText(this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnChange.setEnabled(!loading);
    }
}
