package com.pizza.app.view.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.pizza.app.databinding.ActivityPaymentResultBinding;
import com.pizza.app.model.Order;
import com.pizza.app.repository.OrderRepository;
import com.pizza.app.util.Constants;

/**
 * Nhận deep link callback từ MoMo / VNPay / ZaloPay sau khi thanh toán
 * Scheme: momo://app, vnpay://app, zalopay://app
 */
public class PaymentResultActivity extends AppCompatActivity {

    private ActivityPaymentResultBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Uri data = getIntent().getData();
        if (data != null) {
            processPaymentCallback(data);
        } else {
            showFailed("Không nhận được kết quả thanh toán");
        }

        binding.btnGoHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
    }

    private void processPaymentCallback(Uri data) {
        String scheme  = data.getScheme();
        String orderId = data.getQueryParameter("orderId");

        if (orderId == null) { showFailed("Thiếu orderId"); return; }

        // Kiểm tra resultCode
        String resultCode = data.getQueryParameter("resultCode");  // MoMo
        String vnpResponseCode = data.getQueryParameter("vnp_ResponseCode"); // VNPay

        boolean success = false;
        if ("momo".equals(scheme)) {
            success = "0".equals(resultCode);
        } else if ("vnpay".equals(scheme)) {
            success = "00".equals(vnpResponseCode);
        } else if ("zalopay".equals(scheme)) {
            String status = data.getQueryParameter("status");
            success = "1".equals(status);
        }

        String transactionId = data.getQueryParameter("transId") != null
                ? data.getQueryParameter("transId")
                : data.getQueryParameter("vnp_TransactionNo");

        if (success) {
            // Cập nhật trạng thái thanh toán trong Firestore
            new OrderRepository().updatePaymentStatus(orderId,
                    Order.PAY_STATUS_PAID,
                    transactionId != null ? transactionId : "")
                    .observe(this, r -> {});
            showSuccess(orderId);
        } else {
            showFailed("Thanh toán thất bại hoặc bị huỷ");
        }
    }

    private void showSuccess(String orderId) {
        binding.lottieResult.setAnimation("lottie_success.json");
        binding.lottieResult.playAnimation();
        binding.tvTitle.setText("Thanh toán thành công!");
        binding.tvMessage.setText("Đơn hàng của bạn đã được thanh toán.");
        binding.btnTrackOrder.setVisibility(android.view.View.VISIBLE);
        binding.btnTrackOrder.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrderTrackingActivity.class);
            intent.putExtra(Constants.EXTRA_ORDER_ID, orderId);
            startActivity(intent);
        });
    }

    private void showFailed(String reason) {
        binding.lottieResult.setAnimation("lottie_error.json");
        binding.lottieResult.playAnimation();
        binding.tvTitle.setText("Thanh toán thất bại");
        binding.tvMessage.setText(reason);
    }
}
