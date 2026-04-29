package com.pizza.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pizza.app.model.CartItem;
import com.pizza.app.model.Voucher;
import com.pizza.app.repository.AuthRepository.Result;
import com.pizza.app.repository.CartRepository;
import com.pizza.app.repository.VoucherRepository;
import com.pizza.app.util.Constants;

import java.util.List;

/**
 * ViewModel giỏ hàng — kết hợp CartRepository + VoucherRepository
 * Dùng AndroidViewModel để truy cập Context (cần cho SharedPrefs)
 */
public class CartViewModel extends AndroidViewModel {

    private final CartRepository    cartRepo;
    private final VoucherRepository voucherRepo = new VoucherRepository();

    private final MutableLiveData<Voucher> appliedVoucher = new MutableLiveData<>(null);
    private final MutableLiveData<Long>    shippingFee    = new MutableLiveData<>(Constants.SHIPPING_BASE_FEE);

    public CartViewModel(@NonNull Application app) {
        super(app);
        cartRepo = new CartRepository(app);
    }

    // ── Giỏ hàng ──────────────────────────────────────────────────

    public LiveData<List<CartItem>> getCart() {
        return cartRepo.getCart();
    }

    public void addToCart(CartItem item) {
        cartRepo.addToCart(item);
    }

    public void updateQuantity(int position, int newQty) {
        cartRepo.updateQuantity(position, newQty);
    }

    public void removeItem(int position) {
        cartRepo.removeItem(position);
    }

    public void clearCart() {
        cartRepo.clearCart();
        appliedVoucher.setValue(null);
    }

    // ── Tính tiền ─────────────────────────────────────────────────

    public long getSubtotal() {
        return cartRepo.getSubtotal();
    }

    public long getDiscount() {
        Voucher v = appliedVoucher.getValue();
        return v != null ? v.calculateDiscount(getSubtotal()) : 0L;
    }

    public long getTotal() {
        long fee = shippingFee.getValue() != null ? shippingFee.getValue() : Constants.SHIPPING_BASE_FEE;
        return getSubtotal() - getDiscount() + fee;
    }

    public int getTotalItemCount() {
        return cartRepo.getTotalItemCount();
    }

    // ── Phí vận chuyển ────────────────────────────────────────────

    public void setShippingFee(long fee) {
        shippingFee.setValue(fee);
    }

    public LiveData<Long> getShippingFee() {
        return shippingFee;
    }

    /**
     * Tính phí ship dựa trên khoảng cách (km)
     * Công thức: 15.000đ + 5.000đ/km vượt quá 3km
     */
    public static long calculateShippingFee(double distanceKm) {
        if (distanceKm <= Constants.SHIPPING_FREE_RADIUS) {
            return Constants.SHIPPING_BASE_FEE;
        }
        double extra = distanceKm - Constants.SHIPPING_FREE_RADIUS;
        return Constants.SHIPPING_BASE_FEE + (long)(extra * Constants.SHIPPING_EXTRA_PER_KM);
    }

    // ── Voucher ───────────────────────────────────────────────────

    public LiveData<Result<Voucher>> applyVoucher(String code) {
        LiveData<Result<Voucher>> liveData = voucherRepo.getVoucher(code, getSubtotal());
        // Observer nên được đặt ở Activity/Fragment để cập nhật appliedVoucher
        return liveData;
    }

    public void setAppliedVoucher(Voucher voucher) {
        appliedVoucher.setValue(voucher);
    }

    public void removeVoucher() {
        appliedVoucher.setValue(null);
    }

    public LiveData<Voucher> getAppliedVoucher() {
        return appliedVoucher;
    }
}
