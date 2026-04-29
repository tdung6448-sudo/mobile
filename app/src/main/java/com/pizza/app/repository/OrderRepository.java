package com.pizza.app.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.pizza.app.model.Order;
import com.pizza.app.repository.AuthRepository.Result;
import com.pizza.app.util.Constants;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository đơn hàng — tạo, lấy, cập nhật trạng thái
 */
public class OrderRepository {

    private final FirebaseFirestore db;

    public OrderRepository() {
        db = FirebaseFirestore.getInstance();
    }

    // ── Tạo đơn hàng mới ─────────────────────────────────────────

    public LiveData<Result<String>> createOrder(Order order) {
        MutableLiveData<Result<String>> result = new MutableLiveData<>();
        result.setValue(Result.loading());

        db.collection(Constants.COL_ORDERS)
          .add(order)
          .addOnSuccessListener(ref -> result.setValue(Result.success(ref.getId())))
          .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));

        return result;
    }

    // ── Lấy đơn hàng theo ID — real-time ─────────────────────────

    public LiveData<Result<Order>> getOrderById(String orderId) {
        MutableLiveData<Result<Order>> result = new MutableLiveData<>();

        db.collection(Constants.COL_ORDERS)
          .document(orderId)
          .addSnapshotListener((doc, e) -> {
              if (e != null) { result.setValue(Result.error(e.getMessage())); return; }
              if (doc != null && doc.exists()) {
                  Order order = doc.toObject(Order.class);
                  result.setValue(Result.success(order));
              } else {
                  result.setValue(Result.error("Đơn hàng không tồn tại"));
              }
          });

        return result;
    }

    // ── Lịch sử đơn hàng của khách ────────────────────────────────

    public LiveData<Result<List<Order>>> getOrdersByUser(String userId, String statusFilter) {
        MutableLiveData<Result<List<Order>>> result = new MutableLiveData<>();

        Query query = db.collection(Constants.COL_ORDERS)
                        .whereEqualTo("userId", userId)
                        .orderBy("createdAt", Query.Direction.DESCENDING)
                        .limit(Constants.PAGE_SIZE_ORDERS);

        if (statusFilter != null && !statusFilter.equals("all")) {
            query = query.whereEqualTo("status", statusFilter);
        }

        query.addSnapshotListener((snap, e) -> {
            if (e != null) { result.setValue(Result.error(e.getMessage())); return; }
            result.setValue(Result.success(toOrderList(snap)));
        });

        return result;
    }

    // ── Đơn hàng cho Admin — real-time, mới nhất trước ────────────

    public LiveData<Result<List<Order>>> getAllOrdersForAdmin(String statusFilter) {
        MutableLiveData<Result<List<Order>>> result = new MutableLiveData<>();

        Query query = db.collection(Constants.COL_ORDERS)
                        .orderBy("createdAt", Query.Direction.DESCENDING)
                        .limit(50);

        if (statusFilter != null && !statusFilter.equals("all")) {
            query = query.whereEqualTo("status", statusFilter);
        }

        query.addSnapshotListener((snap, e) -> {
            if (e != null) { result.setValue(Result.error(e.getMessage())); return; }
            result.setValue(Result.success(toOrderList(snap)));
        });

        return result;
    }

    // ── Cập nhật trạng thái đơn hàng ─────────────────────────────

    public LiveData<Result<Void>> updateOrderStatus(String orderId, String newStatus, String note) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();

        // Thêm vào mảng statusHistory và cập nhật status hiện tại
        Map<String, Object> historyEntry = new HashMap<>();
        historyEntry.put("status",    newStatus);
        historyEntry.put("timestamp", Timestamp.now());
        historyEntry.put("note",      note != null ? note : "");

        Map<String, Object> update = new HashMap<>();
        update.put("status",    newStatus);
        update.put("updatedAt", new Date());

        db.collection(Constants.COL_ORDERS).document(orderId)
          .update(update)
          .addOnSuccessListener(v -> {
              // Append vào statusHistory bằng arrayUnion
              db.collection(Constants.COL_ORDERS).document(orderId)
                .update("statusHistory", com.google.firebase.firestore.FieldValue.arrayUnion(historyEntry))
                .addOnSuccessListener(v2 -> result.setValue(Result.success(null)))
                .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));
          })
          .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));

        return result;
    }

    // ── Cập nhật trạng thái thanh toán ───────────────────────────

    public LiveData<Result<Void>> updatePaymentStatus(String orderId, String payStatus,
                                                        String transactionId) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();
        Map<String, Object> update = new HashMap<>();
        update.put("paymentStatus",        payStatus);
        update.put("paymentTransactionId", transactionId);

        db.collection(Constants.COL_ORDERS).document(orderId)
          .update(update)
          .addOnSuccessListener(v -> result.setValue(Result.success(null)))
          .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));

        return result;
    }

    // ── Gán shipper cho đơn hàng ──────────────────────────────────

    public LiveData<Result<Void>> assignShipper(String orderId, String shipperId,
                                                  String shipperName, String shipperPhone) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();
        Map<String, Object> update = new HashMap<>();
        update.put("shipperId",    shipperId);
        update.put("shipperName",  shipperName);
        update.put("shipperPhone", shipperPhone);
        update.put("status",       Order.STATUS_DELIVERING);

        db.collection(Constants.COL_ORDERS).document(orderId)
          .update(update)
          .addOnSuccessListener(v -> result.setValue(Result.success(null)))
          .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));

        return result;
    }

    // ── Thống kê doanh thu cho Admin Dashboard ────────────────────

    public LiveData<Result<Map<String, Long>>> getRevenueSummary() {
        MutableLiveData<Result<Map<String, Long>>> result = new MutableLiveData<>();

        db.collection(Constants.COL_ORDERS)
          .whereEqualTo("status", Order.STATUS_COMPLETED)
          .get()
          .addOnSuccessListener(snap -> {
              long today = 0, week = 0, month = 0, total = 0;
              long now       = System.currentTimeMillis();
              long dayMs     = 86_400_000L;
              long weekMs    = 7  * dayMs;
              long monthMs   = 30 * dayMs;

              for (DocumentSnapshot doc : snap.getDocuments()) {
                  Order order = doc.toObject(Order.class);
                  if (order == null || order.getCreatedAt() == null) continue;
                  long orderTime = order.getCreatedAt().getTime();
                  long diff = now - orderTime;
                  total += order.getTotal();
                  if (diff <= dayMs)   today += order.getTotal();
                  if (diff <= weekMs)  week  += order.getTotal();
                  if (diff <= monthMs) month += order.getTotal();
              }

              Map<String, Long> summary = new HashMap<>();
              summary.put("today", today);
              summary.put("week",  week);
              summary.put("month", month);
              summary.put("total", total);
              result.setValue(Result.success(summary));
          })
          .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));

        return result;
    }

    // ── Private helper ─────────────────────────────────────────────

    private List<Order> toOrderList(com.google.firebase.firestore.QuerySnapshot snap) {
        List<Order> list = new ArrayList<>();
        if (snap == null) return list;
        for (DocumentSnapshot doc : snap.getDocuments()) {
            Order o = doc.toObject(Order.class);
            if (o != null) list.add(o);
        }
        return list;
    }
}
