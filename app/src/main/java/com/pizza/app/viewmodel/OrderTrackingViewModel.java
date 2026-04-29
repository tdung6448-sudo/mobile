package com.pizza.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.pizza.app.model.Order;
import com.pizza.app.repository.AuthRepository.Result;
import com.pizza.app.repository.OrderRepository;

/**
 * ViewModel theo dõi trạng thái đơn hàng real-time
 */
public class OrderTrackingViewModel extends ViewModel {

    private final OrderRepository repo = new OrderRepository();

    /** Lắng nghe real-time cập nhật đơn hàng (bao gồm vị trí shipper) */
    public LiveData<Result<Order>> trackOrder(String orderId) {
        return repo.getOrderById(orderId);
    }

    public LiveData<Result<Void>> cancelOrder(String orderId) {
        return repo.updateOrderStatus(orderId, Order.STATUS_CANCELLED,
                "Khách hàng huỷ đơn");
    }
}
