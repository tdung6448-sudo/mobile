package com.pizza.app.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pizza.app.databinding.ItemCartBinding;
import com.pizza.app.model.CartItem;
import com.pizza.app.util.CurrencyUtils;
import com.pizza.app.util.GlideHelper;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.VH> {

    public interface OnQuantityChange  { void onChange(int position, int newQty); }
    public interface OnRemoveItem      { void onRemove(int position); }

    private final List<CartItem>    items = new ArrayList<>();
    private final OnQuantityChange  quantityChangeListener;
    private final OnRemoveItem      removeListener;

    public CartAdapter(OnQuantityChange qcl, OnRemoveItem rl) {
        this.quantityChangeListener = qcl;
        this.removeListener         = rl;
    }

    public void setItems(List<CartItem> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemCartBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(items.get(position), position);
    }

    @Override
    public int getItemCount() { return items.size(); }

    class VH extends RecyclerView.ViewHolder {
        private final ItemCartBinding binding;

        VH(ItemCartBinding b) {
            super(b.getRoot());
            binding = b;
        }

        void bind(CartItem item, int position) {
            binding.tvProductName.setText(item.getProductName());
            binding.tvVariant.setText(item.getVariantDescription());
            binding.tvPrice.setText(CurrencyUtils.format(item.getLineTotal()));
            binding.tvQuantity.setText(String.valueOf(item.getQuantity()));
            GlideHelper.loadProduct(binding.ivProduct, item.getProductImage());

            binding.btnDecrease.setOnClickListener(v ->
                    quantityChangeListener.onChange(position, item.getQuantity() - 1));
            binding.btnIncrease.setOnClickListener(v ->
                    quantityChangeListener.onChange(position, item.getQuantity() + 1));
            binding.btnRemove.setOnClickListener(v ->
                    removeListener.onRemove(position));
        }
    }
}
