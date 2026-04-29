package com.pizza.app.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pizza.app.databinding.ItemProductImageBinding;
import com.pizza.app.util.GlideHelper;

import java.util.ArrayList;
import java.util.List;

public class ProductImageAdapter extends RecyclerView.Adapter<ProductImageAdapter.VH> {

    private final List<String> urls;

    public ProductImageAdapter(List<String> urls) {
        this.urls = urls != null ? urls : new ArrayList<>();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemProductImageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        GlideHelper.loadProduct(holder.binding.ivImage, urls.get(position));
    }

    @Override
    public int getItemCount() { return urls.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemProductImageBinding binding;
        VH(ItemProductImageBinding b) { super(b.getRoot()); binding = b; }
    }
}
