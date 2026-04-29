package com.pizza.app.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pizza.app.databinding.FragmentAdminVouchersBinding;
import com.pizza.app.model.Voucher;
import com.pizza.app.view.adapter.AdminVoucherAdapter;
import com.pizza.app.viewmodel.AdminViewModel;

public class AdminVouchersFragment extends Fragment {

    private FragmentAdminVouchersBinding binding;
    private AdminViewModel               viewModel;
    private AdminVoucherAdapter          adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                              @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding   = FragmentAdminVouchersBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new AdminVoucherAdapter(
                voucher -> showVoucherDialog(voucher),
                code -> new AlertDialog.Builder(requireContext())
                        .setTitle("Xóa voucher")
                        .setMessage("Xóa mã " + code + "?")
                        .setPositiveButton("Xóa", (d, w) ->
                                viewModel.deleteVoucher(code).observe(getViewLifecycleOwner(), r -> {}))
                        .setNegativeButton("Huỷ", null)
                        .show()
        );

        binding.rvVouchers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvVouchers.setAdapter(adapter);
        binding.fabAdd.setOnClickListener(v -> showVoucherDialog(null));

        viewModel.getAllVouchers().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess() && result.data != null) {
                adapter.setItems(result.data);
            }
        });
    }

    /** Dialog tạo / sửa voucher */
    private void showVoucherDialog(@Nullable Voucher existing) {
        View dialogView = getLayoutInflater().inflate(
                com.pizza.app.R.layout.dialog_voucher, null);

        com.google.android.material.textfield.TextInputEditText etCode =
                dialogView.findViewById(com.pizza.app.R.id.etCode);
        com.google.android.material.textfield.TextInputEditText etValue =
                dialogView.findViewById(com.pizza.app.R.id.etValue);
        com.google.android.material.textfield.TextInputEditText etMinOrder =
                dialogView.findViewById(com.pizza.app.R.id.etMinOrder);
        android.widget.RadioGroup rgType =
                dialogView.findViewById(com.pizza.app.R.id.rgType);

        if (existing != null) {
            etCode.setText(existing.getCode());
            etCode.setEnabled(false); // Không cho đổi code
            etValue.setText(String.valueOf(existing.getValue()));
            etMinOrder.setText(String.valueOf(existing.getMinOrder()));
            if (Voucher.TYPE_PERCENT.equals(existing.getType())) {
                dialogView.findViewById(com.pizza.app.R.id.rbPercent)
                        .setSelected(true);
            }
        }

        String title = existing == null ? "Tạo voucher" : "Sửa voucher";
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton("Lưu", (d, w) -> {
                    Voucher voucher = existing != null ? existing : new Voucher();
                    voucher.setCode(etCode.getText().toString().toUpperCase());
                    voucher.setValue(Long.parseLong(etValue.getText().toString()));
                    voucher.setMinOrder(Long.parseLong(etMinOrder.getText().toString()));
                    voucher.setType(rgType.getCheckedRadioButtonId()
                            == com.pizza.app.R.id.rbPercent
                            ? Voucher.TYPE_PERCENT : Voucher.TYPE_AMOUNT);
                    voucher.setActive(true);

                    viewModel.saveVoucher(voucher).observe(getViewLifecycleOwner(), result -> {
                        if (result.isSuccess()) {
                            Toast.makeText(requireContext(), "Đã lưu voucher", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
