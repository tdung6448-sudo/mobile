package com.pizza.app.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.tabs.TabLayout;
import com.pizza.app.databinding.FragmentAdminOrdersBinding;
import com.pizza.app.model.Order;
import com.pizza.app.util.Constants;
import com.pizza.app.view.activity.AdminOrderDetailActivity;
import com.pizza.app.view.adapter.AdminOrderAdapter;
import com.pizza.app.viewmodel.AdminViewModel;

/**
 * Danh sách đơn hàng cho Admin — real-time, có tab filter
 */
public class AdminOrdersFragment extends Fragment {

    private FragmentAdminOrdersBinding binding;
    private AdminViewModel             viewModel;
    private AdminOrderAdapter          adapter;

    private final String[] STATUS_FILTERS = {
            "all", Order.STATUS_PENDING, Order.STATUS_CONFIRMED,
            Order.STATUS_PREPARING, Order.STATUS_DELIVERING,
            Order.STATUS_COMPLETED, Order.STATUS_CANCELLED
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                              @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding   = FragmentAdminOrdersBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupTabs();
        setupRecyclerView();
        loadOrders("all");
    }

    private void setupTabs() {
        String[] labels = {"Tất cả", "Chờ", "Xác nhận", "Làm", "Đang giao", "Xong", "Huỷ"};
        for (String label : labels) {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(label));
        }
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                loadOrders(STATUS_FILTERS[tab.getPosition()]);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new AdminOrderAdapter(order -> {
            Intent intent = new Intent(requireContext(), AdminOrderDetailActivity.class);
            intent.putExtra(Constants.EXTRA_ORDER_ID, order.getId());
            startActivity(intent);
        });
        binding.rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvOrders.setAdapter(adapter);
    }

    private void loadOrders(String filter) {
        viewModel.getOrders(filter).observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess() && result.data != null) {
                adapter.setItems(result.data);
                binding.tvEmpty.setVisibility(result.data.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
