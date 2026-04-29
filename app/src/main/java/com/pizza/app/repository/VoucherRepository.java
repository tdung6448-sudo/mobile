package com.pizza.app.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pizza.app.model.Voucher;
import com.pizza.app.repository.AuthRepository.Result;
import com.pizza.app.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository quản lý voucher
 */
public class VoucherRepository {

    private final FirebaseFirestore db;

    public VoucherRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /** Kiểm tra và lấy thông tin voucher theo code */
    public LiveData<Result<Voucher>> getVoucher(String code, long orderAmount) {
        MutableLiveData<Result<Voucher>> result = new MutableLiveData<>();
        result.setValue(Result.loading());

        db.collection(Constants.COL_VOUCHERS)
          .document(code.toUpperCase())
          .get()
          .addOnSuccessListener(doc -> {
              if (!doc.exists()) {
                  result.setValue(Result.error("Mã voucher không tồn tại"));
                  return;
              }
              Voucher voucher = doc.toObject(Voucher.class);
              if (voucher == null) {
                  result.setValue(Result.error("Lỗi dữ liệu voucher"));
                  return;
              }
              if (!voucher.isValid(orderAmount)) {
                  result.setValue(Result.error("Voucher không hợp lệ hoặc đã hết hạn"));
                  return;
              }
              result.setValue(Result.success(voucher));
          })
          .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));

        return result;
    }

    /** Tăng usedCount khi áp dụng voucher thành công */
    public void incrementUsedCount(String code) {
        db.collection(Constants.COL_VOUCHERS)
          .document(code.toUpperCase())
          .update("usedCount", FieldValue.increment(1));
    }

    /** Admin: lấy tất cả voucher */
    public LiveData<Result<List<Voucher>>> getAllVouchers() {
        MutableLiveData<Result<List<Voucher>>> result = new MutableLiveData<>();

        db.collection(Constants.COL_VOUCHERS)
          .addSnapshotListener((snap, e) -> {
              if (e != null) { result.setValue(Result.error(e.getMessage())); return; }
              List<Voucher> list = new ArrayList<>();
              if (snap != null) {
                  for (DocumentSnapshot doc : snap.getDocuments()) {
                      Voucher v = doc.toObject(Voucher.class);
                      if (v != null) list.add(v);
                  }
              }
              result.setValue(Result.success(list));
          });

        return result;
    }

    /** Admin: tạo / cập nhật voucher */
    public LiveData<Result<Void>> saveVoucher(Voucher voucher) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();
        result.setValue(Result.loading());

        db.collection(Constants.COL_VOUCHERS)
          .document(voucher.getCode().toUpperCase())
          .set(voucher)
          .addOnSuccessListener(v -> result.setValue(Result.success(null)))
          .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));

        return result;
    }

    /** Admin: xóa voucher */
    public LiveData<Result<Void>> deleteVoucher(String code) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();
        db.collection(Constants.COL_VOUCHERS).document(code.toUpperCase())
          .delete()
          .addOnSuccessListener(v -> result.setValue(Result.success(null)))
          .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));
        return result;
    }
}
