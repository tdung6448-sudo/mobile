package com.pizza.app.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pizza.app.databinding.ItemAdminVoucherBinding;
import com.pizza.app.model.Voucher;
import com.pizza.app.util.CurrencyUtils;
import com.pizza.app.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class AdminVoucherAdapter extends RecyclerView.Adapter<AdminVoucherAdapter.VH> {

    public interface OnEditClick  { void onEdit(Voucher voucher); }
    public interface OnDeleteClick { void onDelete(String code); }

    private final List<Voucher>  items = new ArrayList<>();
    private final OnEditClick    editListener;
    private final OnDeleteClick  deleteListener;

    public AdminVoucherAdapter(OnEditClick el, OnDeleteClick dl) {
        editListener   = el;
        deleteListener = dl;
    }

    public void setItems(List<Voucher> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemAdminVoucherBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Voucher v = items.get(position);
        holder.bind(v);
        holder.binding.btnEdit.setOnClickListener(w -> editListener.onEdit(v));
        holder.binding.btnDelete.setOnClickListener(w -> deleteListener.onDelete(v.getCode()));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemAdminVoucherBinding binding;

        VH(ItemAdminVoucherBinding b) { super(b.getRoot()); binding = b; }

        void bind(Voucher v) {
            binding.tvCode.setText(v.getCode());
            String valueStr = Voucher.TYPE_PERCENT.equals(v.getType())
                    ? v.getValue() + "%" : CurrencyUtils.format(v.getValue());
            binding.tvValue.setText("Giảm: " + valueStr);
            binding.tvMinOrder.setText("Đơn tối thiểu: " + CurrencyUtils.format(v.getMinOrder()));
            binding.tvUsage.setText("Đã dùng: " + v.getUsedCount() + "/"
                    + (v.getUsageLimit() < 0 ? "∞" : v.getUsageLimit()));
            if (v.getValidTo() != null) {
                binding.tvExpiry.setText("HSD: " + DateUtils.formatDate(v.getValidTo()));
            }
        }
    }
}
