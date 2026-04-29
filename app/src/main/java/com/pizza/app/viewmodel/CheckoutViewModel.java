package com.pizza.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.pizza.app.model.Address;
import com.pizza.app.model.CartItem;
import com.pizza.app.model.Order;
import com.pizza.app.model.OrderItem;
import com.pizza.app.model.Voucher;
import com.pizza.app.repository.AuthRepository.Result;
import com.pizza.app.repository.CartRepository;
import com.pizza.app.repository.OrderRepository;
import com.pizza.app.repository.PaymentRepository;
import com.pizza.app.repository.VoucherRepository;
import com.pizza.app.util.Constants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ViewModel luồng thanh toán — tổng hợp thông tin từ Cart + Address + Payment
 */
public class CheckoutViewModel extends AndroidViewModel {

    private final CartRepository    cartRepo;
    private final OrderRepository   orderRepo   = new OrderRepository();
    private final VoucherRepository voucherRepo = new VoucherRepository();
    private final PaymentRepository paymentRepo = new PaymentRepository();

    // Dữ liệu checkout
    private final MutableLiveData<Address> selectedAddress  = new MutableLiveData<>();
    private final MutableLiveData<String>  paymentMethod    = new MutableLiveData<>(Order.PAYMENT_COD);
    private final MutableLiveData<Voucher> appliedVoucher   = new MutableLiveData<>();
    private final MutableLiveData<Long>    shippingFee      = new MutableLiveData<>(Constants.SHIPPING_BASE_FEE);
    private final MutableLiveData<String>  orderNote        = new MutableLiveData<>("");

    public CheckoutViewModel(@NonNull Application app) {
        super(app);
        cartRepo = new CartRepository(app);
    }

    // ── Setters ────────────────────────────────────────────────────

    public void setSelectedAddress(Address address)    { selectedAddress.setValue(address); }
    public void setPaymentMethod(String method)        { paymentMethod.setValue(method); }
    public void setAppliedVoucher(Voucher voucher)     { appliedVoucher.setValue(voucher); }
    public void setShippingFee(long fee)               { shippingFee.setValue(fee); }
    public void setOrderNote(String note)              { orderNote.setValue(note); }

    // ── Getters ────────────────────────────────────────────────────

    public LiveData<Address> getSelectedAddress()  { return selectedAddress; }
    public LiveData<String>  getPaymentMethod()    { return paymentMethod; }
    public LiveData<Voucher> getAppliedVoucher()   { return appliedVoucher; }
    public LiveData<Long>    getShippingFee()      { return shippingFee; }
    public LiveData<String>  getOrderNote()        { return orderNote; }

    // ── Tính toán ──────────────────────────────────────────────────

    public long getSubtotal() {
        return cartRepo.getSubtotal();
    }

    public long getDiscount() {
        Voucher v = appliedVoucher.getValue();
        return v != null ? v.calculateDiscount(getSubtotal()) : 0L;
    }

    public long getFee() {
        return shippingFee.getValue() != null ? shippingFee.getValue() : Constants.SHIPPING_BASE_FEE;
    }

    public long getTotal() {
        return getSubtotal() - getDiscount() + getFee();
    }

    // ── Đặt hàng ──────────────────────────────────────────────────

    public LiveData<Result<String>> placeOrder(String userName, String userPhone) {
        // Xây dựng Order từ dữ liệu hiện tại
        Order order = new Order();
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        order.setUserId(uid);
        order.setUserName(userName);
        order.setUserPhone(userPhone);

        // Chuyển CartItem → OrderItem
        List<CartItem> cartItems = cartRepo.getCart().getValue();
        List<OrderItem> orderItems = new ArrayList<>();
        if (cartItems != null) {
            for (CartItem ci : cartItems) {
                orderItems.add(OrderItem.fromCartItem(ci));
            }
        }
        order.setItems(orderItems);
        order.setSubtotal(getSubtotal());
        order.setShippingFee(getFee());
        order.setDiscount(getDiscount());
        order.setTotal(getTotal());

        Voucher v = appliedVoucher.getValue();
        if (v != null) order.setVoucherCode(v.getCode());

        String method = paymentMethod.getValue();
        order.setPaymentMethod(method != null ? method : Order.PAYMENT_COD);
        if (Order.PAYMENT_COD.equals(method)) {
            order.setPaymentStatus(Order.PAY_STATUS_PENDING);
        }

        Address addr = selectedAddress.getValue();
        if (addr != null) {
            order.setDeliveryAddress(addr.getFullAddress());
            order.setDeliveryLat(addr.getLat());
            order.setDeliveryLng(addr.getLng());
        }

        String note = orderNote.getValue();
        order.setNote(note != null ? note : "");

        // Thêm entry đầu tiên vào statusHistory
        Order.StatusHistory initial = new Order.StatusHistory(
                Order.STATUS_PENDING, new Date(), "Đơn hàng vừa được tạo");
        order.getStatusHistory().add(initial);

        // Tăng usedCount nếu có voucher
        if (v != null) voucherRepo.incrementUsedCount(v.getCode());

        return orderRepo.createOrder(order);
    }

    // ── Payment URLs ───────────────────────────────────────────────

    public LiveData<Result<String>> getMoMoPaymentUrl(String orderId, long amount) {
        return paymentRepo.createMoMoPaymentUrl(orderId, amount,
                "Thanh toán Pizza App - " + orderId);
    }

    public LiveData<Result<String>> getVNPayUrl(String orderId, long amount) {
        return paymentRepo.createVNPayUrl(orderId, amount,
                "Thanh toán Pizza App " + orderId, "127.0.0.1");
    }

    // ── Validate trước khi đặt ────────────────────────────────────

    public boolean isReadyToOrder() {
        return selectedAddress.getValue() != null
                && cartRepo.getTotalItemCount() > 0;
    }

    /** Lấy voucher */
    public LiveData<Result<Voucher>> validateVoucher(String code) {
        return voucherRepo.getVoucher(code, getSubtotal());
    }
}
