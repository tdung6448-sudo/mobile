package com.pizza.app.service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.pizza.app.util.Constants;
import com.pizza.app.util.NotificationUtils;

import java.util.Map;

/**
 * Xử lý FCM push notification — cả foreground lẫn background
 */
public class PizzaFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "PizzaFCM";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        Map<String, String> data = message.getData();
        String type    = data.get("type");
        String orderId = data.get("orderId");

        String title = message.getNotification() != null
                ? message.getNotification().getTitle() : data.get("title");
        String body  = message.getNotification() != null
                ? message.getNotification().getBody() : data.get("body");

        if (title == null) title = "Pizza App";
        if (body  == null) body  = "";

        // Phân loại và hiển thị notification phù hợp
        if ("new_order".equals(type)) {
            NotificationUtils.showNewOrderNotification(this, orderId, body);
        } else if ("order_status".equals(type)) {
            NotificationUtils.showOrderStatusNotification(this, orderId, title, body);
        } else {
            NotificationUtils.showPromoNotification(this, title, body);
        }

        Log.d(TAG, "Message received: type=" + type + ", title=" + title);
    }

    /** Gọi khi FCM token được làm mới — cập nhật vào Firestore */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM token: " + token);
        updateTokenInFirestore(token);
    }

    private void updateTokenInFirestore(String token) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseFirestore.getInstance()
                .collection(Constants.COL_USERS)
                .document(uid)
                .update("fcmToken", token)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update FCM token", e));
    }
}
