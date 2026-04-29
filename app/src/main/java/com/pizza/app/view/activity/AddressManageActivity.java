package com.pizza.app.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.pizza.app.databinding.ActivityAddressManageBinding;
import com.pizza.app.model.Address;
import com.pizza.app.util.Constants;
import com.pizza.app.view.adapter.AddressAdapter;
import com.pizza.app.viewmodel.ProfileViewModel;

/**
 * Quản lý sổ địa chỉ — xem danh sách, thêm, xóa
 */
public class AddressManageActivity extends AppCompatActivity {

    private ActivityAddressManageBinding binding;
    private ProfileViewModel             viewModel;
    private AddressAdapter               adapter;
    private String                       currentUid;

    private final ActivityResultLauncher<Intent> mapPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    double lat     = result.getData().getDoubleExtra(Constants.EXTRA_LAT, 0);
                    double lng     = result.getData().getDoubleExtra(Constants.EXTRA_LNG, 0);
                    String address = result.getData().getStringExtra(Constants.EXTRA_ADDRESS);

                    Address addr = new Address();
                    addr.setFullAddress(address);
                    addr.setLat(lat);
                    addr.setLng(lng);
                    addr.setLabel("Địa chỉ " + (adapter.getItemCount() + 1));

                    viewModel.addAddress(currentUid, addr).observe(this, r -> {});
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding    = ActivityAddressManageBinding.inflate(getLayoutInflater());
        viewModel  = new ViewModelProvider(this).get(ProfileViewModel.class);
        currentUid = FirebaseAuth.getInstance().getUid();
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.fabAdd.setOnClickListener(v ->
                mapPickerLauncher.launch(new Intent(this, MapPickerActivity.class)));

        setupRecyclerView();
        loadAddresses();
    }

    private void setupRecyclerView() {
        adapter = new AddressAdapter(addressId ->
                viewModel.removeAddress(currentUid, addressId).observe(this, r -> {}));
        binding.rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAddresses.setAdapter(adapter);
    }

    private void loadAddresses() {
        viewModel.getUser(currentUid).observe(this, result -> {
            if (result.isSuccess() && result.data != null) {
                java.util.List<Address> addresses = result.data.getAddresses();
                adapter.setItems(addresses);
                binding.tvEmpty.setVisibility(
                        (addresses == null || addresses.isEmpty()) ? View.VISIBLE : View.GONE);
            }
        });
    }
}
