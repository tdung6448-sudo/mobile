package com.pizza.app;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

/**
 * Application class — khởi tạo các SDK một lần duy nhất khi app chạy
 */
public class PizzaApplication extends Application {

    public static final String CHANNEL_ORDERS   = "channel_orders";
    public static final String CHANNEL_PROMO    = "channel_promotions";
    public static final String CHANNEL_CHAT     = "channel_chat";
    public static final String CHANNEL_SHIPPER  = "channel_shipper_location";

    @Override
    public void onCreate() {
        super.onCreate();

        // Khởi tạo Firebase
        FirebaseApp.initializeApp(this);
        configureFirestore();

        // Tạo Notification Channels (bắt buộc trên Android 8+)
        createNotificationChannels();
    }

    /** Cấu hình Firestore: bật offline persistence + cache size */
    private void configureFirestore() {
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)         // Cache offline
                .setCacheSizeBytes(50 * 1024 * 1024) // 50 MB
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);
    }

    /** Tạo các kênh thông báo phân loại cho Android 8.0+ */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationManager manager = getSystemService(NotificationManager.class);

        manager.createNotificationChannel(new NotificationChannel(
                CHANNEL_ORDERS,
                "Đơn hàng",
                NotificationManager.IMPORTANCE_HIGH));

        manager.createNotificationChannel(new NotificationChannel(
                CHANNEL_PROMO,
                "Khuyến mãi",
                NotificationManager.IMPORTANCE_DEFAULT));

        manager.createNotificationChannel(new NotificationChannel(
                CHANNEL_CHAT,
                "Tin nhắn",
                NotificationManager.IMPORTANCE_HIGH));

        NotificationChannel shipperChannel = new NotificationChannel(
                CHANNEL_SHIPPER,
                "Vị trí giao hàng",
                NotificationManager.IMPORTANCE_LOW);
        // Ít quan trọng — chạy âm thầm cho foreground service shipper
        shipperChannel.setShowBadge(false);
        manager.createNotificationChannel(shipperChannel);
    }
}
