package com.pizza.app.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.pizza.app.PizzaApplication;
import com.pizza.app.R;
import com.pizza.app.util.Constants;
import com.pizza.app.view.activity.AdminMainActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * Foreground Service cập nhật vị trí shipper lên Firestore realtime.
 * Chạy khi shipper nhận đơn, dừng khi giao xong.
 */
public class ShipperLocationService extends Service {

    private static final String TAG            = "ShipperLocationSvc";
    private static final int    UPDATE_INTERVAL = 5_000; // 5 giây
    private static final int    NOTIF_ID       = 9999;

    public static final String ACTION_START = "action_start";
    public static final String ACTION_STOP  = "action_stop";
    public static final String EXTRA_ORDER_ID = "order_id";

    private FusedLocationProviderClient locationClient;
    private LocationCallback            locationCallback;
    private String                      currentOrderId;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;

        String action = intent.getAction();
        if (ACTION_START.equals(action)) {
            currentOrderId = intent.getStringExtra(EXTRA_ORDER_ID);
            startForeground(NOTIF_ID, buildNotification());
            startLocationUpdates();
        } else if (ACTION_STOP.equals(action)) {
            stopSelf();
        }
        return START_STICKY;
    }

    private void startLocationUpdates() {
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        LocationRequest request = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL)
                .setMinUpdateIntervalMillis(3_000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null || currentOrderId == null) return;

                android.location.Location loc = locationResult.getLastLocation();
                if (loc == null) return;

                uploadLocation(loc.getLatitude(), loc.getLongitude());
            }
        };

        try {
            locationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission missing", e);
            stopSelf();
        }
    }

    /** Cập nhật vị trí shipper vào Firestore: orders/{id}.shipperLocation */
    private void uploadLocation(double lat, double lng) {
        String shipperId = FirebaseAuth.getInstance().getUid();
        if (shipperId == null) return;

        Map<String, Object> update = new HashMap<>();
        update.put("shipperLocation", new GeoPoint(lat, lng));

        FirebaseFirestore.getInstance()
                .collection(Constants.COL_ORDERS)
                .document(currentOrderId)
                .update(update)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update location", e));
    }

    private Notification buildNotification() {
        Intent intent = new Intent(this, AdminMainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, PizzaApplication.CHANNEL_SHIPPER)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Đang giao hàng")
                .setContentText("Vị trí của bạn đang được chia sẻ")
                .setOngoing(true)
                .setContentIntent(pi)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationClient != null && locationCallback != null) {
            locationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
