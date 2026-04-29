package com.pizza.app.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pizza.app.databinding.ItemOrderItemBinding;
import com.pizza.app.model.OrderItem;
import com.pizza.app.util.CurrencyUtils;
import com.pizza.app.util.GlideHelper;

import java.util.ArrayList;
import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.VH> {

    private final List<OrderItem> items = new ArrayList<>();

    public void setItems(List<OrderItem> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemOrderItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        private final ItemOrderItemBinding binding;

        VH(ItemOrderItemBinding b) { super(b.getRoot()); binding = b; }

        void bind(OrderItem item) {
            binding.tvName.setText(item.getProductName() + " x" + item.getQuantity());
            binding.tvVariant.setText(item.getSizeName()
                    + (item.getCrustName() != null ? " • " + item.getCrustName() : ""));
            binding.tvPrice.setText(CurrencyUtils.format(item.getLineTotal()));
            GlideHelper.loadProduct(binding.ivProduct, item.getProductImage());
        }
    }
}
