package com.pizza.app.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pizza.app.databinding.ItemSizeBinding;
import com.pizza.app.model.ProductSize;
import com.pizza.app.util.CurrencyUtils;

import java.util.ArrayList;
import java.util.List;

public class SizeAdapter extends RecyclerView.Adapter<SizeAdapter.VH> {

    public interface OnSizeClick { void onClick(ProductSize size); }

    private final List<ProductSize> items = new ArrayList<>();
    private final OnSizeClick       listener;
    private ProductSize             selectedSize;

    public SizeAdapter(OnSizeClick listener) { this.listener = listener; }

    public void setItems(List<ProductSize> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    public void setSelectedSize(ProductSize size) {
        selectedSize = size;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemSizeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ProductSize size = items.get(position);
        boolean selected = selectedSize != null && selectedSize.getCode().equals(size.getCode());
        holder.bind(size, selected);
        holder.itemView.setOnClickListener(v -> {
            selectedSize = size;
            notifyDataSetChanged();
            listener.onClick(size);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        private final ItemSizeBinding binding;

        VH(ItemSizeBinding b) { super(b.getRoot()); binding = b; }

        void bind(ProductSize size, boolean selected) {
            binding.tvSizeName.setText(size.getName());
            binding.tvSizePrice.setText(CurrencyUtils.format(size.getPrice()));
            binding.getRoot().setSelected(selected);
        }
    }
}
