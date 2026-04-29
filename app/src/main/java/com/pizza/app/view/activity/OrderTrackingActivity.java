package com.pizza.app.view.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.GeoPoint;
import com.pizza.app.R;
import com.pizza.app.databinding.ActivityOrderTrackingBinding;
import com.pizza.app.model.Order;
import com.pizza.app.util.Constants;
import com.pizza.app.util.DateUtils;
import com.pizza.app.viewmodel.OrderTrackingViewModel;

/**
 * Theo dõi đơn hàng real-time — bản đồ vị trí shipper + timeline trạng thái
 */
public class OrderTrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityOrderTrackingBinding binding;
    private OrderTrackingViewModel       viewModel;
    private GoogleMap                    googleMap;
    private String                       orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding   = ActivityOrderTrackingBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(OrderTrackingViewModel.class);
        setContentView(binding.getRoot());

        orderId = getIntent().getStringExtra(Constants.EXTRA_ORDER_ID);
        if (orderId == null) { finish(); return; }

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // Khởi tạo Google Map
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        observeOrder();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
    }

    private void observeOrder() {
        viewModel.trackOrder(orderId).observe(this, result -> {
            if (!result.isSuccess() || result.data == null) return;
            Order order = result.data;
            updateStatusUI(order);
            updateMapMarkers(order);
        });
    }

    private void updateStatusUI(Order order) {
        binding.tvOrderId.setText("Đơn hàng #" + order.getId().substring(0, 8).toUpperCase());
        binding.tvStatus.setText(getStatusLabel(order.getStatus()));
        binding.tvStatusColor.setBackgroundColor(getStatusColor(order.getStatus()));

        // Thời gian tạo
        if (order.getCreatedAt() != null) {
            binding.tvOrderTime.setText(DateUtils.formatFull(order.getCreatedAt()));
        }

        // Thông tin shipper
        boolean hasShipper = order.getShipperId() != null && !order.getShipperId().isEmpty();
        binding.layoutShipperInfo.setVisibility(hasShipper ? View.VISIBLE : View.GONE);
        if (hasShipper) {
            binding.tvShipperName.setText(order.getShipperName());
            binding.tvShipperPhone.setText(order.getShipperPhone());

            // Gọi điện shipper
            binding.btnCallShipper.setOnClickListener(v -> {
                Intent call = new Intent(Intent.ACTION_DIAL,
                        Uri.parse("tel:" + order.getShipperPhone()));
                startActivity(call);
            });
        }

        // ETA
        if (order.getEstimatedTime() != null && !order.getEstimatedTime().isEmpty()) {
            binding.tvEta.setVisibility(View.VISIBLE);
            binding.tvEta.setText("Dự kiến: " + order.getEstimatedTime());
        }

        // Nút huỷ — chỉ hiện khi đang pending
        binding.btnCancel.setVisibility(order.isPending() ? View.VISIBLE : View.GONE);
        binding.btnCancel.setOnClickListener(v -> cancelOrder());

        updateStatusTimeline(order);
    }

    private void updateStatusTimeline(Order order) {
        // Highlight các bước đã qua trong progress bar timeline
        String status = order.getStatus();
        int step = 0;
        switch (status) {
            case Order.STATUS_CONFIRMED:  step = 1; break;
            case Order.STATUS_PREPARING:  step = 2; break;
            case Order.STATUS_DELIVERING: step = 3; break;
            case Order.STATUS_COMPLETED:  step = 4; break;
        }
        // Active color cho các step đã qua
        int activeColor   = getColor(R.color.red_primary);
        int inactiveColor = getColor(R.color.divider);

        binding.stepPending.setBackgroundColor(step >= 0 ? activeColor : inactiveColor);
        binding.stepConfirmed.setBackgroundColor(step >= 1 ? activeColor : inactiveColor);
        binding.stepPreparing.setBackgroundColor(step >= 2 ? activeColor : inactiveColor);
        binding.stepDelivering.setBackgroundColor(step >= 3 ? activeColor : inactiveColor);
        binding.stepCompleted.setBackgroundColor(step >= 4 ? activeColor : inactiveColor);
    }

    private void updateMapMarkers(Order order) {
        if (googleMap == null) return;
        googleMap.clear();

        // Marker địa chỉ giao hàng
        if (order.getDeliveryLat() != 0 && order.getDeliveryLng() != 0) {
            LatLng deliveryLatLng = new LatLng(order.getDeliveryLat(), order.getDeliveryLng());
            googleMap.addMarker(new MarkerOptions()
                    .position(deliveryLatLng)
                    .title("Địa chỉ giao hàng")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(deliveryLatLng, 14f));
        }

        // Marker vị trí shipper (real-time)
        GeoPoint shipperLoc = order.getShipperLocation();
        if (shipperLoc != null) {
            LatLng shipperLatLng = new LatLng(shipperLoc.getLatitude(), shipperLoc.getLongitude());
            googleMap.addMarker(new MarkerOptions()
                    .position(shipperLatLng)
                    .title(order.getShipperName() != null ? order.getShipperName() : "Shipper")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        }
    }

    private void cancelOrder() {
        viewModel.cancelOrder(orderId).observe(this, result -> {
            if (result.isSuccess()) {
                binding.btnCancel.setVisibility(View.GONE);
                android.widget.Toast.makeText(this, "Đã huỷ đơn hàng", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getStatusLabel(String status) {
        switch (status) {
            case Order.STATUS_PENDING:    return "Chờ xác nhận";
            case Order.STATUS_CONFIRMED:  return "Đã xác nhận";
            case Order.STATUS_PREPARING:  return "Đang chuẩn bị";
            case Order.STATUS_DELIVERING: return "Đang giao hàng";
            case Order.STATUS_COMPLETED:  return "Đã giao thành công";
            case Order.STATUS_CANCELLED:  return "Đã huỷ";
            default: return status;
        }
    }

    private int getStatusColor(String status) {
        switch (status) {
            case Order.STATUS_COMPLETED:  return getColor(R.color.green);
            case Order.STATUS_CANCELLED:  return getColor(R.color.grey);
            case Order.STATUS_DELIVERING: return getColor(R.color.blue);
            default: return getColor(R.color.red_primary);
        }
    }
}
