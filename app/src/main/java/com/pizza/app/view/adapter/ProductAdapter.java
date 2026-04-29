package com.pizza.app.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.pizza.app.databinding.ItemProductBinding;
import com.pizza.app.model.Product;
import com.pizza.app.util.CurrencyUtils;
import com.pizza.app.util.GlideHelper;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.VH> {

    public interface OnProductClick { void onClick(Product product); }

    private final List<Product> items = new ArrayList<>();
    private final OnProductClick listener;

    public ProductAdapter(OnProductClick listener) {
        this.listener = listener;
    }

    public void setItems(List<Product> list) {
        // DiffUtil — chỉ cập nhật các item thay đổi, tránh blink toàn bộ list
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return items.size(); }
            @Override public int getNewListSize() { return list != null ? list.size() : 0; }
            @Override public boolean areItemsTheSame(int o, int n) {
                return items.get(o).getId().equals(list.get(n).getId());
            }
            @Override public boolean areContentsTheSame(int o, int n) {
                Product a = items.get(o), b = list.get(n);
                return a.getName().equals(b.getName())
                        && a.getBasePrice() == b.getBasePrice()
                        && a.getRatingAvg() == b.getRatingAvg();
            }
        });

        items.clear();
        if (list != null) items.addAll(list);
        diff.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Product p = items.get(position);
        holder.bind(p);
        holder.itemView.setOnClickListener(v -> listener.onClick(p));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        private final ItemProductBinding binding;

        VH(ItemProductBinding b) {
            super(b.getRoot());
            binding = b;
        }

        void bind(Product p) {
            binding.tvName.setText(p.getName());
            binding.tvPrice.setText(CurrencyUtils.format(p.getBasePrice()));
            binding.ratingBar.setRating(p.getRatingAvg());
            binding.tvSoldCount.setText(p.getSoldCount() + " đã bán");
            GlideHelper.loadProduct(binding.ivProduct, p.getThumbnail());

            // Badges
            android.view.View badgeNew = binding.badgeNew;
            android.view.View badgeBest = binding.badgeBestSeller;
            if (badgeNew  != null) badgeNew.setVisibility(p.isNew()        ? android.view.View.VISIBLE : android.view.View.GONE);
            if (badgeBest != null) badgeBest.setVisibility(p.isBestSeller() ? android.view.View.VISIBLE : android.view.View.GONE);
        }
    }
}
