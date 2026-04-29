package com.pizza.app.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.pizza.app.PizzaApplication;
import com.pizza.app.R;
import com.pizza.app.view.activity.MainActivity;
import com.pizza.app.view.activity.OrderTrackingActivity;

/**
 * Tiện ích tạo và hiển thị Notification
 */
public final class NotificationUtils {

    private static int notifId = 1000;

    private NotificationUtils() {}

    /** Thông báo đơn hàng mới (dùng cho Admin) */
    public static void showNewOrderNotification(Context ctx, String orderId, String message) {
        Intent intent = new Intent(ctx, com.pizza.app.view.activity.AdminMainActivity.class);
        intent.putExtra(Constants.EXTRA_ORDER_ID, orderId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pi = PendingIntent.getActivity(ctx, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notif = new NotificationCompat.Builder(ctx, PizzaApplication.CHANNEL_ORDERS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Đơn hàng mới!")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .build();

        NotificationManager manager = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(notifId++, notif);
    }

    /** Thông báo cập nhật trạng thái đơn hàng (dùng cho Customer) */
    public static void showOrderStatusNotification(Context ctx, String orderId,
                                                    String title, String body) {
        Intent intent = new Intent(ctx, OrderTrackingActivity.class);
        intent.putExtra(Constants.EXTRA_ORDER_ID, orderId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pi = PendingIntent.getActivity(ctx, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notif = new NotificationCompat.Builder(ctx, PizzaApplication.CHANNEL_ORDERS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .build();

        NotificationManager manager = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(notifId++, notif);
    }

    /** Thông báo khuyến mãi */
    public static void showPromoNotification(Context ctx, String title, String body) {
        Intent intent = new Intent(ctx, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pi = PendingIntent.getActivity(ctx, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notif = new NotificationCompat.Builder(ctx, PizzaApplication.CHANNEL_PROMO)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .build();

        NotificationManager manager = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(notifId++, notif);
    }
}
