package com.pizza.app.view.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pizza.app.databinding.ActivityAdminOrderDetailBinding;
import com.pizza.app.model.Order;
import com.pizza.app.util.Constants;
import com.pizza.app.util.CurrencyUtils;
import com.pizza.app.util.DateUtils;
import com.pizza.app.view.adapter.OrderItemAdapter;
import com.pizza.app.viewmodel.AdminViewModel;

/**
 * Chi tiết đơn hàng dành cho Admin — xem + cập nhật trạng thái
 */
public class AdminOrderDetailActivity extends AppCompatActivity {

    private ActivityAdminOrderDetailBinding binding;
    private AdminViewModel                  viewModel;
    private String                          orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding   = ActivityAdminOrderDetailBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(AdminViewModel.class);
        setContentView(binding.getRoot());

        orderId = getIntent().getStringExtra(Constants.EXTRA_ORDER_ID);
        if (orderId == null) { finish(); return; }

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        observeOrder();
    }

    private void observeOrder() {
        viewModel.getOrderById(orderId).observe(this, result -> {
            if (!result.isSuccess() || result.data == null) return;
            bindOrder(result.data);
        });
    }

    private void bindOrder(Order order) {
        binding.tvOrderId.setText("#" + order.getId().substring(0, 8).toUpperCase());
        binding.tvStatus.setText(statusLabel(order.getStatus()));
        binding.tvCustomerName.setText(order.getUserName());
        binding.tvCustomerPhone.setText(order.getUserPhone());
        binding.tvAddress.setText(order.getDeliveryAddress());
        binding.tvPaymentMethod.setText(paymentLabel(order.getPaymentMethod()));
        binding.tvSubtotal.setText(CurrencyUtils.format(order.getSubtotal()));
        binding.tvShipping.setText(CurrencyUtils.format(order.getShippingFee()));
        binding.tvDiscount.setText(order.getDiscount() > 0
                ? "-" + CurrencyUtils.format(order.getDiscount()) : "0đ");
        binding.tvTotal.setText(CurrencyUtils.format(order.getTotal()));
        if (order.getCreatedAt() != null) {
            binding.tvCreatedAt.setText(DateUtils.formatFull(order.getCreatedAt()));
        }
        if (order.getNote() != null && !order.getNote().isEmpty()) {
            binding.tvNote.setText("Ghi chú: " + order.getNote());
            binding.tvNote.setVisibility(View.VISIBLE);
        }

        // Danh sách món
        OrderItemAdapter itemAdapter = new OrderItemAdapter();
        binding.rvItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvItems.setAdapter(itemAdapter);
        binding.rvItems.setNestedScrollingEnabled(false);
        itemAdapter.setItems(order.getItems());

        // Spinner cập nhật trạng thái
        String[] statuses = {
                Order.STATUS_PENDING, Order.STATUS_CONFIRMED,
                Order.STATUS_PREPARING, Order.STATUS_DELIVERING,
                Order.STATUS_COMPLETED, Order.STATUS_CANCELLED
        };
        String[] labels = {"Chờ", "Xác nhận", "Đang làm", "Đang giao", "Hoàn thành", "Huỷ"};

        android.widget.ArrayAdapter<String> spinnerAdapter =
                new android.widget.ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, labels);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerStatus.setAdapter(spinnerAdapter);

        // Set vị trí hiện tại
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equals(order.getStatus())) {
                binding.spinnerStatus.setSelection(i);
                break;
            }
        }

        binding.btnUpdateStatus.setOnClickListener(v -> {
            int pos = binding.spinnerStatus.getSelectedItemPosition();
            String newStatus = statuses[pos];
            viewModel.updateOrderStatus(orderId, newStatus, "Admin cập nhật")
                    .observe(this, r -> {
                        if (r.isSuccess()) {
                            Toast.makeText(this, "Đã cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private String statusLabel(String status) {
        switch (status) {
            case Order.STATUS_PENDING:    return "Chờ xác nhận";
            case Order.STATUS_CONFIRMED:  return "Đã xác nhận";
            case Order.STATUS_PREPARING:  return "Đang làm";
            case Order.STATUS_DELIVERING: return "Đang giao";
            case Order.STATUS_COMPLETED:  return "Hoàn thành";
            case Order.STATUS_CANCELLED:  return "Đã huỷ";
            default: return status;
        }
    }

    private String paymentLabel(String method) {
        switch (method != null ? method : "") {
            case Order.PAYMENT_COD:     return "Tiền mặt (COD)";
            case Order.PAYMENT_MOMO:    return "MoMo";
            case Order.PAYMENT_VNPAY:   return "VNPay";
            case Order.PAYMENT_ZALOPAY: return "ZaloPay";
            default: return method;
        }
    }
}
