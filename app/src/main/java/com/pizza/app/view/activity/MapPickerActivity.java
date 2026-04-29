package com.pizza.app.view.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.pizza.app.BuildConfig;
import com.pizza.app.R;
import com.pizza.app.databinding.ActivityMapPickerBinding;
import com.pizza.app.util.Constants;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Chọn địa chỉ giao hàng trên bản đồ — Places Autocomplete + tap để pin
 */
public class MapPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityMapPickerBinding     binding;
    private GoogleMap                    googleMap;
    private FusedLocationProviderClient  fusedLocation;
    private LatLng                       selectedLatLng;
    private String                       selectedAddress = "";

    private static final int PERM_LOCATION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapPickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo Places SDK
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), BuildConfig.MAPS_API_KEY);
        }
        fusedLocation = LocationServices.getFusedLocationProviderClient(this);

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        setupAutocomplete();

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        binding.btnConfirm.setOnClickListener(v -> confirmSelection());
    }

    private void setupAutocomplete() {
        AutocompleteSupportFragment autocomplete = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        if (autocomplete == null) return;

        autocomplete.setPlaceFields(Arrays.asList(
                com.google.android.libraries.places.api.model.Place.Field.ID,
                com.google.android.libraries.places.api.model.Place.Field.NAME,
                com.google.android.libraries.places.api.model.Place.Field.LAT_LNG,
                com.google.android.libraries.places.api.model.Place.Field.ADDRESS));

        // Giới hạn tìm kiếm trong Việt Nam
        autocomplete.setCountries("VN");

        autocomplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull com.google.android.libraries.places.api.model.Place place) {
                if (place.getLatLng() != null) {
                    selectedLatLng  = place.getLatLng();
                    selectedAddress = place.getAddress() != null ? place.getAddress() : place.getName();
                    moveMapToLocation(selectedLatLng);
                }
            }

            @Override
            public void onError(@NonNull com.google.android.gms.common.api.Status status) {}
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // Tap để chọn vị trí trực tiếp trên bản đồ
        googleMap.setOnMapClickListener(latLng -> {
            selectedLatLng = latLng;
            reverseGeocode(latLng);
            moveMapToLocation(latLng);
        });

        // Di chuyển camera đến vị trí hiện tại
        getCurrentLocation();
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERM_LOCATION);
            return;
        }
        googleMap.setMyLocationEnabled(true);
        fusedLocation.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15f));
            }
        });
    }

    /** Reverse geocode — tọa độ → chuỗi địa chỉ */
    private void reverseGeocode(LatLng latLng) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, new Locale("vi"));
                List<Address> results = geocoder.getFromLocation(
                        latLng.latitude, latLng.longitude, 1);
                if (results != null && !results.isEmpty()) {
                    Address addr = results.get(0);
                    selectedAddress = addr.getAddressLine(0);
                    runOnUiThread(() ->
                            binding.tvSelectedAddress.setText(selectedAddress));
                }
            } catch (IOException e) {
                // Geocoder không khả dụng — bỏ qua
            }
        }).start();
    }

    private void moveMapToLocation(LatLng latLng) {
        if (googleMap == null) return;
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions().position(latLng).title("Địa chỉ giao hàng"));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f));
        binding.tvSelectedAddress.setText(selectedAddress);
        binding.btnConfirm.setEnabled(true);
    }

    private void confirmSelection() {
        if (selectedLatLng == null) return;
        Intent result = new Intent();
        result.putExtra(Constants.EXTRA_LAT,     selectedLatLng.latitude);
        result.putExtra(Constants.EXTRA_LNG,     selectedLatLng.longitude);
        result.putExtra(Constants.EXTRA_ADDRESS, selectedAddress);
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERM_LOCATION && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }
    }
}
