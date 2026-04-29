package com.pizza.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.pizza.app.model.Order;
import com.pizza.app.model.Product;
import com.pizza.app.model.User;
import com.pizza.app.model.Voucher;
import com.pizza.app.repository.AuthRepository.Result;
import com.pizza.app.repository.OrderRepository;
import com.pizza.app.repository.ProductRepository;
import com.pizza.app.repository.UserRepository;
import com.pizza.app.repository.VoucherRepository;

import java.util.List;
import java.util.Map;

/**
 * ViewModel cho toàn bộ màn hình Admin — tập trung một nơi để dễ quản lý
 */
public class AdminViewModel extends ViewModel {

    private final OrderRepository   orderRepo   = new OrderRepository();
    private final ProductRepository productRepo = new ProductRepository();
    private final UserRepository    userRepo    = new UserRepository();
    private final VoucherRepository voucherRepo = new VoucherRepository();

    // ── Dashboard ─────────────────────────────────────────────────

    public LiveData<Result<Map<String, Long>>> getRevenueSummary() {
        return orderRepo.getRevenueSummary();
    }

    // ── Đơn hàng ──────────────────────────────────────────────────

    public LiveData<Result<List<Order>>> getOrders(String statusFilter) {
        return orderRepo.getAllOrdersForAdmin(statusFilter);
    }

    public LiveData<Result<Order>> getOrderById(String orderId) {
        return orderRepo.getOrderById(orderId);
    }

    public LiveData<Result<Void>> updateOrderStatus(String orderId, String status, String note) {
        return orderRepo.updateOrderStatus(orderId, status, note);
    }

    public LiveData<Result<Void>> assignShipper(String orderId, String shipperId,
                                                  String shipperName, String phone) {
        return orderRepo.assignShipper(orderId, shipperId, shipperName, phone);
    }

    // ── Sản phẩm ──────────────────────────────────────────────────

    public LiveData<Result<List<Product>>> getAllProducts() {
        return productRepo.getAllProductsForAdmin();
    }

    public LiveData<Result<String>> saveProduct(Product product) {
        return productRepo.saveProduct(product);
    }

    public LiveData<Result<Void>> toggleAvailability(String productId, boolean available) {
        return productRepo.toggleProductAvailability(productId, available);
    }

    public LiveData<Result<Void>> deleteProduct(String productId) {
        return productRepo.deleteProduct(productId);
    }

    // ── Users ─────────────────────────────────────────────────────

    public LiveData<Result<List<User>>> getAllUsers() {
        return userRepo.getAllUsers();
    }

    public LiveData<Result<Void>> setUserBlocked(String uid, boolean blocked) {
        return userRepo.setUserBlocked(uid, blocked);
    }

    // ── Vouchers ──────────────────────────────────────────────────

    public LiveData<Result<List<Voucher>>> getAllVouchers() {
        return voucherRepo.getAllVouchers();
    }

    public LiveData<Result<Void>> saveVoucher(Voucher voucher) {
        return voucherRepo.saveVoucher(voucher);
    }

    public LiveData<Result<Void>> deleteVoucher(String code) {
        return voucherRepo.deleteVoucher(code);
    }
}
