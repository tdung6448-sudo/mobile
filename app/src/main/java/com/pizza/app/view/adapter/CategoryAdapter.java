package com.pizza.app.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.pizza.app.databinding.ItemCategoryBinding;
import com.pizza.app.model.Category;
import com.pizza.app.util.GlideHelper;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.VH> {

    public interface OnCategoryClick { void onClick(Category category); }

    private final List<Category> items = new ArrayList<>();
    private final OnCategoryClick listener;
    private String selectedId = null;

    public CategoryAdapter(OnCategoryClick listener) {
        this.listener = listener;
    }

    public void setItems(List<Category> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    public void setSelectedId(String id) {
        selectedId = id;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemCategoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Category cat = items.get(position);
        holder.bind(cat, cat.getId().equals(selectedId));
        holder.itemView.setOnClickListener(v -> {
            selectedId = cat.getId();
            notifyDataSetChanged();
            listener.onClick(cat);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        private final ItemCategoryBinding binding;

        VH(ItemCategoryBinding b) {
            super(b.getRoot());
            binding = b;
        }

        void bind(Category cat, boolean selected) {
            binding.tvCategoryName.setText(cat.getName());
            GlideHelper.loadProduct(binding.ivCategory, cat.getImage());
            // Highlight khi được chọn
            float alpha = selected ? 1f : 0.6f;
            binding.getRoot().setAlpha(alpha);
            binding.ivCategory.setScaleX(selected ? 1.1f : 1f);
            binding.ivCategory.setScaleY(selected ? 1.1f : 1f);
        }
    }
}
