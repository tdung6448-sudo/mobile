package com.pizza.app.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.pizza.app.databinding.ActivityPhoneAuthBinding;
import com.pizza.app.model.User;
import com.pizza.app.viewmodel.AuthViewModel;

import java.util.concurrent.TimeUnit;

/**
 * Đăng nhập / đăng ký bằng số điện thoại qua OTP
 */
public class PhoneAuthActivity extends AppCompatActivity {

    private ActivityPhoneAuthBinding binding;
    private AuthViewModel            viewModel;
    private String                   verificationId;
    private CountDownTimer           countDownTimer;

    // Trạng thái UI: PHONE (nhập SĐT) hoặc OTP (nhập mã)
    private boolean isOtpStep = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding   = ActivityPhoneAuthBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnSendOtp.setOnClickListener(v -> {
            if (!isOtpStep) sendOtp();
            else            verifyOtp();
        });

        binding.tvResendOtp.setOnClickListener(v -> {
            binding.tvResendOtp.setEnabled(false);
            sendOtp();
        });
    }

    private void sendOtp() {
        String phone = binding.etPhone.getText().toString().trim();
        if (phone.isEmpty() || !phone.matches("^0[0-9]{9}$")) {
            binding.tilPhone.setError("Số điện thoại không hợp lệ");
            return;
        }
        binding.tilPhone.setError(null);

        // Firebase yêu cầu định dạng +84xxxxxxxxx
        String phoneE164 = "+84" + phone.substring(1);

        setLoading(true);

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                .setPhoneNumber(phoneE164)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        // Auto-verify (thiết bị test hoặc SIM đặc biệt)
                        setLoading(false);
                        loginWithCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        setLoading(false);
                        Toast.makeText(PhoneAuthActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String vid,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        verificationId = vid;
                        setLoading(false);
                        showOtpStep();
                        startCountDown();
                    }
                })
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyOtp() {
        String code = binding.etOtp.getText().toString().trim();
        if (code.length() != 6) {
            binding.tilOtp.setError("Nhập đúng 6 chữ số OTP");
            return;
        }
        binding.tilOtp.setError(null);

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        loginWithCredential(credential);
    }

    private void loginWithCredential(PhoneAuthCredential credential) {
        setLoading(true);
        String name = binding.etName.getText().toString().trim();
        if (name.isEmpty()) name = "Người dùng";

        viewModel.loginWithPhone(credential, name).observe(this, result -> {
            if (result.isLoading()) return;
            setLoading(false);
            if (result.isSuccess()) {
                navigateByRole(result.data);
            } else {
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showOtpStep() {
        isOtpStep = true;
        binding.layoutOtp.setVisibility(View.VISIBLE);
        binding.btnSendOtp.setText("Xác nhận OTP");
        binding.etPhone.setEnabled(false);
        Toast.makeText(this, "Mã OTP đã được gửi!", Toast.LENGTH_SHORT).show();
    }

    private void startCountDown() {
        if (countDownTimer != null) countDownTimer.cancel();
        binding.tvResendOtp.setEnabled(false);

        countDownTimer = new CountDownTimer(60_000, 1_000) {
            @Override
            public void onTick(long ms) {
                binding.tvResendOtp.setText("Gửi lại sau " + ms / 1000 + "s");
            }

            @Override
            public void onFinish() {
                binding.tvResendOtp.setEnabled(true);
                binding.tvResendOtp.setText("Gửi lại OTP");
            }
        }.start();
    }

    private void navigateByRole(User user) {
        Intent intent = new Intent(this, user.isAdmin() ? AdminMainActivity.class : MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSendOtp.setEnabled(!loading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
