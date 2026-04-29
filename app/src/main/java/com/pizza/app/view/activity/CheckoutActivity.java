package com.pizza.app.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.pizza.app.databinding.ActivityCheckoutBinding;
import com.pizza.app.model.Address;
import com.pizza.app.model.Order;
import com.pizza.app.model.User;
import com.pizza.app.repository.UserRepository;
import com.pizza.app.util.Constants;
import com.pizza.app.util.CurrencyUtils;
import com.pizza.app.viewmodel.CartViewModel;
import com.pizza.app.viewmodel.CheckoutViewModel;

/**
 * Màn hình thanh toán — chọn địa chỉ, phương thức thanh toán, xác nhận đặt
 */
public class CheckoutActivity extends AppCompatActivity {

    private ActivityCheckoutBinding binding;
    private CheckoutViewModel       viewModel;
    private CartViewModel           cartViewModel;

    // Launcher nhận kết quả từ MapPickerActivity
    private final ActivityResultLauncher<Intent> mapPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    double lat     = result.getData().getDoubleExtra(Constants.EXTRA_LAT, 0);
                    double lng     = result.getData().getDoubleExtra(Constants.EXTRA_LNG, 0);
                    String address = result.getData().getStringExtra(Constants.EXTRA_ADDRESS);

                    Address addr = new Address();
                    addr.setFullAddress(address);
                    addr.setLat(lat);
                    addr.setLng(lng);
                    viewModel.setSelectedAddress(addr);

                    // Tính phí ship dựa khoảng cách (giả lập — thực tế dùng Directions API)
                    double distKm = 3.5; // TODO: tính từ Directions API
                    viewModel.setShippingFee(CartViewModel.calculateShippingFee(distKm));
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding       = ActivityCheckoutBinding.inflate(getLayoutInflater());
        viewModel     = new ViewModelProvider(this).get(CheckoutViewModel.class);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        setupAddressSection();
        setupPaymentMethods();
        setupVoucherInput();
        setupPriceSummary();
        setupPlaceOrderButton();
    }

    private void setupAddressSection() {
        // Load địa chỉ mặc định của user
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            new UserRepository().getUserById(uid).observe(this, result -> {
                if (result.isSuccess() && result.data != null) {
                    User user = result.data;
                    Address defaultAddr = user.getDefaultAddress();
                    if (defaultAddr != null) {
                        viewModel.setSelectedAddress(defaultAddr);
                    }
                    // Cập nhật thông tin người nhận
                    binding.tvReceiverName.setText(user.getName());
                    binding.tvReceiverPhone.setText(user.getPhone());
                }
            });
        }

        // Nút chọn địa chỉ trên bản đồ
        binding.btnPickAddress.setOnClickListener(v ->
                mapPickerLauncher.launch(new Intent(this, MapPickerActivity.class)));

        viewModel.getSelectedAddress().observe(this, address -> {
            if (address != null) {
                binding.tvDeliveryAddress.setText(address.getFullAddress());
                binding.tvDeliveryAddress.setTextColor(getColor(com.pizza.app.R.color.text_primary));
            }
        });
    }

    private void setupPaymentMethods() {
        binding.rgPayment.setOnCheckedChangeListener((group, checkedId) -> {
            if      (checkedId == binding.rbCod.getId())     viewModel.setPaymentMethod(Order.PAYMENT_COD);
            else if (checkedId == binding.rbMomo.getId())    viewModel.setPaymentMethod(Order.PAYMENT_MOMO);
            else if (checkedId == binding.rbVnpay.getId())   viewModel.setPaymentMethod(Order.PAYMENT_VNPAY);
            else if (checkedId == binding.rbZalopay.getId()) viewModel.setPaymentMethod(Order.PAYMENT_ZALOPAY);
        });
        // COD mặc định
        binding.rbCod.setChecked(true);
    }

    private void setupVoucherInput() {
        binding.btnApplyVoucher.setOnClickListener(v -> {
            String code = binding.etVoucher.getText().toString().trim();
            if (code.isEmpty()) return;

            viewModel.validateVoucher(code).observe(this, result -> {
                if (result.isSuccess() && result.data != null) {
                    viewModel.setAppliedVoucher(result.data);
                    binding.tvVoucherStatus.setText("Áp dụng thành công: -"
                            + CurrencyUtils.format(result.data.calculateDiscount(viewModel.getSubtotal())));
                    binding.tvVoucherStatus.setTextColor(getColor(com.pizza.app.R.color.green));
                } else {
                    binding.tvVoucherStatus.setText(result.message);
                    binding.tvVoucherStatus.setTextColor(getColor(com.pizza.app.R.color.red_primary));
                }
            });
        });
    }

    private void setupPriceSummary() {
        // Cập nhật bảng tổng tiền mỗi khi phí ship hoặc voucher thay đổi
        viewModel.getShippingFee().observe(this, fee -> refreshSummary());
        viewModel.getAppliedVoucher().observe(this, v -> refreshSummary());
        refreshSummary();
    }

    private void refreshSummary() {
        binding.tvSubtotal.setText(CurrencyUtils.format(viewModel.getSubtotal()));
        binding.tvShipping.setText(CurrencyUtils.format(viewModel.getFee()));
        long discount = viewModel.getDiscount();
        binding.tvDiscount.setText(discount > 0 ? "-" + CurrencyUtils.format(discount) : "0đ");
        binding.tvTotal.setText(CurrencyUtils.format(viewModel.getTotal()));
        binding.btnPlaceOrder.setText("Đặt hàng — " + CurrencyUtils.format(viewModel.getTotal()));
    }

    private void setupPlaceOrderButton() {
        binding.etNote.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                viewModel.setOrderNote(s.toString());
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        binding.btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void placeOrder() {
        if (!viewModel.isReadyToOrder()) {
            Toast.makeText(this, "Vui lòng chọn địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        setLoading(true);

        new UserRepository().getUserById(uid).observe(this, userResult -> {
            if (!userResult.isSuccess() || userResult.data == null) {
                setLoading(false);
                return;
            }
            User user = userResult.data;

            viewModel.placeOrder(user.getName(), user.getPhone()).observe(this, result -> {
                setLoading(false);
                if (result.isSuccess()) {
                    String orderId = result.data;
                    handlePaymentAfterOrder(orderId);
                } else {
                    Toast.makeText(this, "Đặt hàng thất bại: " + result.message, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void handlePaymentAfterOrder(String orderId) {
        String method = viewModel.getPaymentMethod().getValue();

        if (Order.PAYMENT_COD.equals(method)) {
            // COD → chuyển thẳng sang tracking
            goToOrderTracking(orderId);
            return;
        }

        if (Order.PAYMENT_MOMO.equals(method)) {
            viewModel.getMoMoPaymentUrl(orderId, viewModel.getTotal())
                    .observe(this, r -> {
                        if (r.isSuccess()) {
                            com.pizza.app.repository.PaymentRepository.openMoMo(this, r.data);
                            goToOrderTracking(orderId);
                        }
                    });
        } else if (Order.PAYMENT_VNPAY.equals(method)) {
            viewModel.getVNPayUrl(orderId, viewModel.getTotal())
                    .observe(this, r -> {
                        if (r.isSuccess()) {
                            com.pizza.app.repository.PaymentRepository.openVNPay(this, r.data);
                            goToOrderTracking(orderId);
                        }
                    });
        } else {
            goToOrderTracking(orderId);
        }
    }

    private void goToOrderTracking(String orderId) {
        // Xóa giỏ hàng sau khi đặt thành công
        cartViewModel.clearCart();

        Intent intent = new Intent(this, OrderTrackingActivity.class);
        intent.putExtra(Constants.EXTRA_ORDER_ID, orderId);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnPlaceOrder.setEnabled(!loading);
    }
}
