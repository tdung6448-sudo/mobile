package com.pizza.app.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pizza.app.model.CartItem;
import com.pizza.app.util.SharedPrefsHelper;

import java.util.List;

/**
 * Repository giỏ hàng — lưu cục bộ bằng SharedPreferences + Gson.
 * Không cần Firestore vì giỏ hàng là dữ liệu tạm của thiết bị.
 */
public class CartRepository {

    private final SharedPrefsHelper prefs;
    private final MutableLiveData<List<CartItem>> cartLiveData = new MutableLiveData<>();

    public CartRepository(Context context) {
        prefs = new SharedPrefsHelper(context);
        cartLiveData.setValue(prefs.getCart());
    }

    // ── Observable giỏ hàng ────────────────────────────────────────

    public LiveData<List<CartItem>> getCart() {
        return cartLiveData;
    }

    // ── Thêm sản phẩm vào giỏ ─────────────────────────────────────

    public void addToCart(CartItem newItem) {
        List<CartItem> cart = prefs.getCart();
        String newKey = newItem.getUniqueKey();

        // Nếu đã có cùng variant — tăng số lượng
        for (CartItem item : cart) {
            if (item.getUniqueKey().equals(newKey)) {
                item.setQuantity(item.getQuantity() + newItem.getQuantity());
                save(cart);
                return;
            }
        }
        cart.add(newItem);
        save(cart);
    }

    // ── Cập nhật số lượng ──────────────────────────────────────────

    public void updateQuantity(int position, int newQuantity) {
        List<CartItem> cart = prefs.getCart();
        if (position < 0 || position >= cart.size()) return;

        if (newQuantity <= 0) {
            cart.remove(position);
        } else {
            cart.get(position).setQuantity(newQuantity);
        }
        save(cart);
    }

    // ── Xóa 1 dòng ────────────────────────────────────────────────

    public void removeItem(int position) {
        List<CartItem> cart = prefs.getCart();
        if (position < 0 || position >= cart.size()) return;
        cart.remove(position);
        save(cart);
    }

    // ── Xóa toàn bộ giỏ ──────────────────────────────────────────

    public void clearCart() {
        prefs.clearCart();
        cartLiveData.setValue(prefs.getCart());
    }

    // ── Tổng số món ────────────────────────────────────────────────

    public int getTotalItemCount() {
        List<CartItem> cart = prefs.getCart();
        int count = 0;
        for (CartItem item : cart) count += item.getQuantity();
        return count;
    }

    // ── Tổng tiền hàng ─────────────────────────────────────────────

    public long getSubtotal() {
        List<CartItem> cart = prefs.getCart();
        long total = 0;
        for (CartItem item : cart) total += item.getLineTotal();
        return total;
    }

    // ── Private helper ─────────────────────────────────────────────

    private void save(List<CartItem> cart) {
        prefs.saveCart(cart);
        cartLiveData.setValue(cart);
    }
}
