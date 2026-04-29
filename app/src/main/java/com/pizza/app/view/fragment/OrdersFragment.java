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
import com.google.firebase.auth.FirebaseAuth;
import com.pizza.app.databinding.FragmentOrdersBinding;
import com.pizza.app.model.Order;
import com.pizza.app.util.Constants;
import com.pizza.app.view.activity.OrderTrackingActivity;
import com.pizza.app.view.adapter.OrderAdapter;
import com.pizza.app.viewmodel.ProfileViewModel;

/**
 * Lịch sử đơn hàng — có tab lọc theo trạng thái
 */
public class OrdersFragment extends Fragment {

    private FragmentOrdersBinding binding;
    private ProfileViewModel      viewModel;
    private OrderAdapter          adapter;

    private final String[] STATUS_FILTERS = {
            "all", Order.STATUS_PENDING, Order.STATUS_PREPARING,
            Order.STATUS_DELIVERING, Order.STATUS_COMPLETED, Order.STATUS_CANCELLED
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                              @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding   = FragmentOrdersBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
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
        String[] labels = {"Tất cả", "Chờ xác nhận", "Đang làm",
                "Đang giao", "Hoàn thành", "Đã huỷ"};
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
        adapter = new OrderAdapter(order -> {
            // Mở trang theo dõi nếu đang giao, hoặc chi tiết nếu đã hoàn thành
            Intent intent = new Intent(requireContext(), OrderTrackingActivity.class);
            intent.putExtra(Constants.EXTRA_ORDER_ID, order.getId());
            startActivity(intent);
        });

        binding.rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvOrders.setAdapter(adapter);
    }

    private void loadOrders(String statusFilter) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        setLoading(true);
        viewModel.getOrderHistory(uid, statusFilter).observe(getViewLifecycleOwner(), result -> {
            setLoading(false);
            if (result.isSuccess() && result.data != null) {
                adapter.setItems(result.data);
                binding.tvEmpty.setVisibility(result.data.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void setLoading(boolean loading) {
        if (binding == null) return;
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.rvOrders.setVisibility(loading ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
