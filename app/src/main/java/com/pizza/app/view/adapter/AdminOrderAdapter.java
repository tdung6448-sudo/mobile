package com.pizza.app.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pizza.app.R;
import com.pizza.app.databinding.ItemAdminOrderBinding;
import com.pizza.app.model.Order;
import com.pizza.app.util.CurrencyUtils;
import com.pizza.app.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.VH> {

    public interface OnOrderClick { void onClick(Order order); }

    private final List<Order>  items = new ArrayList<>();
    private final OnOrderClick listener;

    public AdminOrderAdapter(OnOrderClick listener) { this.listener = listener; }

    public void setItems(List<Order> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemAdminOrderBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Order o = items.get(position);
        holder.bind(o);
        holder.itemView.setOnClickListener(v -> listener.onClick(o));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        private final ItemAdminOrderBinding binding;

        VH(ItemAdminOrderBinding b) { super(b.getRoot()); binding = b; }

        void bind(Order o) {
            binding.tvOrderId.setText("#" + o.getId().substring(0, 8).toUpperCase());
            binding.tvCustomer.setText(o.getUserName() + " • " + o.getUserPhone());
            binding.tvTotal.setText(CurrencyUtils.format(o.getTotal()));
            binding.tvStatus.setText(statusLabel(o.getStatus()));
            binding.tvItemCount.setText(o.getTotalItemCount() + " món");
            if (o.getCreatedAt() != null) {
                binding.tvDate.setText(DateUtils.formatFull(o.getCreatedAt()));
            }
            // Dot màu trạng thái
            binding.statusDot.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(statusColor(o.getStatus())));
        }

        private String statusLabel(String s) {
            switch (s) {
                case Order.STATUS_PENDING:    return "Chờ xác nhận";
                case Order.STATUS_CONFIRMED:  return "Đã xác nhận";
                case Order.STATUS_PREPARING:  return "Đang làm";
                case Order.STATUS_DELIVERING: return "Đang giao";
                case Order.STATUS_COMPLETED:  return "Hoàn thành";
                case Order.STATUS_CANCELLED:  return "Đã huỷ";
                default: return s;
            }
        }

        private int statusColor(String s) {
            android.content.Context ctx = itemView.getContext();
            switch (s) {
                case Order.STATUS_COMPLETED:  return ctx.getColor(R.color.green);
                case Order.STATUS_CANCELLED:  return ctx.getColor(R.color.grey);
                case Order.STATUS_DELIVERING: return ctx.getColor(R.color.blue);
                default: return ctx.getColor(R.color.red_primary);
            }
        }
    }
}
