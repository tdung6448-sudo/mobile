package com.pizza.app.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pizza.app.databinding.ItemBannerBinding;
import com.pizza.app.model.Banner;
import com.pizza.app.util.GlideHelper;

import java.util.ArrayList;
import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.VH> {

    private final List<Banner> items = new ArrayList<>();

    public void setItems(List<Banner> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemBannerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        private final ItemBannerBinding binding;

        VH(ItemBannerBinding b) {
            super(b.getRoot());
            binding = b;
        }

        void bind(Banner banner) {
            GlideHelper.loadBanner(binding.ivBanner, banner.getImageUrl());
            binding.tvBannerTitle.setText(banner.getTitle());
        }
    }
}
