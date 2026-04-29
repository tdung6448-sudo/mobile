package com.pizza.app.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pizza.app.model.Address;
import com.pizza.app.model.User;
import com.pizza.app.repository.AuthRepository.Result;
import com.pizza.app.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Repository quản lý thông tin user — profile, sổ địa chỉ
 */
public class UserRepository {

    private final FirebaseFirestore db;

    public UserRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /** Lấy thông tin user theo UID */
    public LiveData<Result<User>> getUserById(String uid) {
        MutableLiveData<Result<User>> result = new MutableLiveData<>();

        db.collection(Constants.COL_USERS).document(uid)
          .addSnapshotListener((doc, e) -> {
              if (e != null) { result.setValue(Result.error(e.getMessage())); return; }
              if (doc != null && doc.exists()) {
                  result.setValue(Result.success(doc.toObject(User.class)));
              }
          });

        return result;
    }

    /** Cập nhật tên, SĐT */
    public LiveData<Result<Void>> updateProfile(String uid, String name, String phone) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();
        Map<String, Object> update = new HashMap<>();
        update.put("name",  name);
        update.put("phone", phone);

        db.collection(Constants.COL_USERS).document(uid)
          .update(update)
          .addOnSuccessListener(v -> result.setValue(Result.success(null)))
          .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));

        return result;
    }

    // ── Quản lý sổ địa chỉ ────────────────────────────────────────

    /** Thêm địa chỉ mới vào danh sách */
    public LiveData<Result<Void>> addAddress(String uid, Address address) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();
        address.setId(UUID.randomUUID().toString());

        db.collection(Constants.COL_USERS).document(uid)
          .get()
          .addOnSuccessListener(doc -> {
              User user = doc.toObject(User.class);
              if (user == null) { result.setValue(Result.error("Lỗi tài khoản")); return; }

              List<Address> addresses = user.getAddresses();
              if (addresses == null) addresses = new ArrayList<>();

              // Nếu là địa chỉ đầu tiên — đặt làm mặc định
              if (addresses.isEmpty()) address.setDefault(true);
              // Nếu đặt làm mặc định — bỏ flag mặc định của địa chỉ cũ
              if (address.isDefault()) {
                  for (Address a : addresses) a.setDefault(false);
              }

              addresses.add(address);
              db.collection(Constants.COL_USERS).document(uid)
                .update("addresses", addresses)
                .addOnSuccessListener(v -> result.setValue(Result.success(null)))
                .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));
          })
          .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));

        return result;
    }

    /** Xóa địa chỉ theo ID */
    public LiveData<Result<Void>> removeAddress(String uid, String addressId) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();

        db.collection(Constants.COL_USERS).document(uid)
          .get()
          .addOnSuccessListener(doc -> {
              User user = doc.toObject(User.class);
              if (user == null) { result.setValue(Result.error("Lỗi tài khoản")); return; }

              List<Address> addresses = user.getAddresses();
              if (addresses == null) { result.setValue(Result.success(null)); return; }

              addresses.removeIf(a -> a.getId().equals(addressId));

              db.collection(Constants.COL_USERS).document(uid)
                .update("addresses", addresses)
                .addOnSuccessListener(v -> result.setValue(Result.success(null)))
                .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));
          })
          .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));

        return result;
    }

    /** Admin: lấy danh sách tất cả user */
    public LiveData<Result<List<User>>> getAllUsers() {
        MutableLiveData<Result<List<User>>> result = new MutableLiveData<>();

        db.collection(Constants.COL_USERS)
          .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
          .addSnapshotListener((snap, e) -> {
              if (e != null) { result.setValue(Result.error(e.getMessage())); return; }
              List<User> list = new ArrayList<>();
              if (snap != null) {
                  for (DocumentSnapshot doc : snap.getDocuments()) {
                      User u = doc.toObject(User.class);
                      if (u != null) list.add(u);
                  }
              }
              result.setValue(Result.success(list));
          });

        return result;
    }

    /** Admin: khoá / mở khoá tài khoản */
    public LiveData<Result<Void>> setUserBlocked(String uid, boolean blocked) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();
        db.collection(Constants.COL_USERS).document(uid)
          .update("isBlocked", blocked)
          .addOnSuccessListener(v -> result.setValue(Result.success(null)))
          .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));
        return result;
    }
}
