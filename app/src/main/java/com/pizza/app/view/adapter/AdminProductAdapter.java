package com.pizza.app.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pizza.app.databinding.ItemAdminProductBinding;
import com.pizza.app.model.Product;
import com.pizza.app.util.CurrencyUtils;
import com.pizza.app.util.GlideHelper;

import java.util.ArrayList;
import java.util.List;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.VH> {

    public interface OnEditClick      { void onEdit(Product product); }
    public interface OnToggleClick    { void onToggle(Product product, boolean isAvailable); }
    public interface OnDeleteClick    { void onDelete(Product product); }

    private final List<Product>  items = new ArrayList<>();
    private final OnEditClick    editListener;
    private final OnToggleClick  toggleListener;
    private final OnDeleteClick  deleteListener;

    public AdminProductAdapter(OnEditClick el, OnToggleClick tl, OnDeleteClick dl) {
        this.editListener   = el;
        this.toggleListener = tl;
        this.deleteListener = dl;
    }

    public void setItems(List<Product> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemAdminProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Product p = items.get(position);
        holder.bind(p);
        holder.binding.btnEdit.setOnClickListener(v -> editListener.onEdit(p));
        holder.binding.switchAvailable.setOnCheckedChangeListener((btn, checked) ->
                toggleListener.onToggle(p, checked));
        holder.binding.btnDelete.setOnClickListener(v -> deleteListener.onDelete(p));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemAdminProductBinding binding;

        VH(ItemAdminProductBinding b) { super(b.getRoot()); binding = b; }

        void bind(Product p) {
            binding.tvName.setText(p.getName());
            binding.tvPrice.setText(CurrencyUtils.format(p.getBasePrice()));
            binding.tvSoldCount.setText("Đã bán: " + p.getSoldCount());
            // Tạm thời tắt listener để tránh trigger khi bind
            binding.switchAvailable.setOnCheckedChangeListener(null);
            binding.switchAvailable.setChecked(p.isAvailable());
            GlideHelper.loadProduct(binding.ivProduct, p.getThumbnail());
        }
    }
}
