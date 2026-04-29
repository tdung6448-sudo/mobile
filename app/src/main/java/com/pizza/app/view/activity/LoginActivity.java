package com.pizza.app.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.pizza.app.R;
import com.pizza.app.databinding.ActivityLoginBinding;
import com.pizza.app.model.User;
import com.pizza.app.util.SharedPrefsHelper;
import com.pizza.app.util.ValidationUtils;
import com.pizza.app.viewmodel.AuthViewModel;

/**
 * Màn hình đăng nhập — Email/Password + Google Sign-In
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel        viewModel;
    private GoogleSignInClient   googleSignInClient;

    // Launcher cho Google Sign-In result
    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    viewModel.loginWithGoogle(account.getIdToken()).observe(this, r -> {
                        setLoading(false);
                        if (r.isSuccess()) navigateByRole(r.data);
                        else showError(r.message);
                    });
                } catch (ApiException e) {
                    setLoading(false);
                    showError("Google Sign-In thất bại: " + e.getMessage());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding   = ActivityLoginBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setContentView(binding.getRoot());

        setupGoogleSignIn();
        setupClickListeners();
        restoreRememberMe();
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Từ google-services.json
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupClickListeners() {
        // Đăng nhập email
        binding.btnLogin.setOnClickListener(v -> attemptEmailLogin());

        // Đăng nhập Google
        binding.btnGoogleLogin.setOnClickListener(v -> {
            setLoading(true);
            googleSignInLauncher.launch(googleSignInClient.getSignInIntent());
        });

        // Đăng nhập SĐT
        binding.btnPhoneLogin.setOnClickListener(v ->
                startActivity(new Intent(this, PhoneAuthActivity.class)));

        // Đăng ký
        binding.tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        // Quên mật khẩu
        binding.tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void attemptEmailLogin() {
        String email    = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString();

        // Validate
        if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.setError("Email không hợp lệ");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("Nhập mật khẩu");
            return;
        }
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);

        // Lưu Remember Me
        new SharedPrefsHelper(this).setRememberMe(binding.cbRememberMe.isChecked());

        setLoading(true);
        viewModel.loginWithEmail(email, password).observe(this, result -> {
            setLoading(false);
            if (result.isLoading()) return;
            if (result.isSuccess()) navigateByRole(result.data);
            else showError(result.message);
        });
    }

    /** Khôi phục email nếu Remember Me đã bật */
    private void restoreRememberMe() {
        SharedPrefsHelper prefs = new SharedPrefsHelper(this);
        if (prefs.isRememberMe()) {
            binding.cbRememberMe.setChecked(true);
        }
    }

    /** Phân quyền sau đăng nhập thành công */
    private void navigateByRole(User user) {
        Intent intent;
        if (user.isAdmin()) {
            intent = new Intent(this, AdminMainActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!loading);
        binding.btnGoogleLogin.setEnabled(!loading);
    }

    private void showError(String message) {
        Toast.makeText(this, message != null ? message : "Đăng nhập thất bại", Toast.LENGTH_LONG).show();
    }
}
