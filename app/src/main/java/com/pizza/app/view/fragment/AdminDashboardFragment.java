package com.pizza.app.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.pizza.app.R;
import com.pizza.app.databinding.FragmentAdminDashboardBinding;
import com.pizza.app.util.CurrencyUtils;
import com.pizza.app.viewmodel.AdminViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Dashboard admin — doanh thu hôm nay/tuần/tháng, biểu đồ, số đơn
 */
public class AdminDashboardFragment extends Fragment {

    private FragmentAdminDashboardBinding binding;
    private AdminViewModel                viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                              @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding   = FragmentAdminDashboardBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadRevenueSummary();
        loadOrderSummary();
        setupBarChart();
    }

    private void loadRevenueSummary() {
        viewModel.getRevenueSummary().observe(getViewLifecycleOwner(), result -> {
            if (!result.isSuccess() || result.data == null) return;
            Map<String, Long> summary = result.data;

            binding.tvRevenueToday.setText(CurrencyUtils.format(
                    summary.getOrDefault("today", 0L)));
            binding.tvRevenueWeek.setText(CurrencyUtils.format(
                    summary.getOrDefault("week", 0L)));
            binding.tvRevenueMonth.setText(CurrencyUtils.format(
                    summary.getOrDefault("month", 0L)));

            // Cập nhật biểu đồ
            updateChart(summary);
        });
    }

    private void loadOrderSummary() {
        // Đếm số đơn theo từng trạng thái
        viewModel.getOrders("all").observe(getViewLifecycleOwner(), result -> {
            if (!result.isSuccess() || result.data == null) return;

            int pending = 0, preparing = 0, delivering = 0, completed = 0;
            for (com.pizza.app.model.Order order : result.data) {
                switch (order.getStatus()) {
                    case com.pizza.app.model.Order.STATUS_PENDING:    pending++;    break;
                    case com.pizza.app.model.Order.STATUS_PREPARING:  preparing++;  break;
                    case com.pizza.app.model.Order.STATUS_DELIVERING: delivering++; break;
                    case com.pizza.app.model.Order.STATUS_COMPLETED:  completed++;  break;
                }
            }

            binding.tvOrderPending.setText(String.valueOf(pending));
            binding.tvOrderPreparing.setText(String.valueOf(preparing));
            binding.tvOrderDelivering.setText(String.valueOf(delivering));
            binding.tvOrderCompleted.setText(String.valueOf(completed));

            // Badge trên tab Orders nếu có đơn mới chờ xử lý
            if (pending > 0) {
                com.google.android.material.badge.BadgeDrawable badge =
                        ((com.pizza.app.view.activity.AdminMainActivity) requireActivity())
                                .binding.adminBottomNav.getOrCreateBadge(R.id.admin_nav_orders);
                badge.setNumber(pending);
                badge.setVisible(true);
            }
        });
    }

    private void setupBarChart() {
        binding.barChart.getDescription().setEnabled(false);
        binding.barChart.getLegend().setEnabled(false);
        binding.barChart.setDrawGridBackground(false);
        binding.barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        binding.barChart.getAxisRight().setEnabled(false);
    }

    private void updateChart(Map<String, Long> summary) {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, summary.getOrDefault("today", 0L)));
        entries.add(new BarEntry(1, summary.getOrDefault("week",  0L)));
        entries.add(new BarEntry(2, summary.getOrDefault("month", 0L)));

        BarDataSet dataSet = new BarDataSet(entries, "Doanh thu");
        dataSet.setColor(requireContext().getColor(R.color.red_primary));

        binding.barChart.setData(new BarData(dataSet));
        binding.barChart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
