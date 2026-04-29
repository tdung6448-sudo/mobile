package com.pizza.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.pizza.app.model.Address;
import com.pizza.app.model.Order;
import com.pizza.app.model.User;
import com.pizza.app.repository.AuthRepository.Result;
import com.pizza.app.repository.OrderRepository;
import com.pizza.app.repository.UserRepository;

import java.util.List;

/**
 * ViewModel hồ sơ cá nhân và lịch sử đơn hàng
 */
public class ProfileViewModel extends ViewModel {

    private final UserRepository  userRepo  = new UserRepository();
    private final OrderRepository orderRepo = new OrderRepository();

    public LiveData<Result<User>> getUser(String uid) {
        return userRepo.getUserById(uid);
    }

    public LiveData<Result<Void>> updateProfile(String uid, String name, String phone) {
        return userRepo.updateProfile(uid, name, phone);
    }

    public LiveData<Result<Void>> addAddress(String uid, Address address) {
        return userRepo.addAddress(uid, address);
    }

    public LiveData<Result<Void>> removeAddress(String uid, String addressId) {
        return userRepo.removeAddress(uid, addressId);
    }

    public LiveData<Result<List<Order>>> getOrderHistory(String userId, String statusFilter) {
        return orderRepo.getOrdersByUser(userId, statusFilter);
    }
}
