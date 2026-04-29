package com.pizza.app.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pizza.app.databinding.ItemReviewBinding;
import com.pizza.app.model.Review;
import com.pizza.app.util.DateUtils;
import com.pizza.app.util.GlideHelper;

import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.VH> {

    private final List<Review> items = new ArrayList<>();

    public void setItems(List<Review> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemReviewBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        private final ItemReviewBinding binding;

        VH(ItemReviewBinding b) { super(b.getRoot()); binding = b; }

        void bind(Review r) {
            binding.tvUserName.setText(r.getUserName());
            binding.ratingBar.setRating(r.getRating());
            binding.tvComment.setText(r.getComment());
            if (r.getCreatedAt() != null) {
                binding.tvDate.setText(DateUtils.timeAgo(r.getCreatedAt()));
            }
            GlideHelper.loadAvatar(binding.ivAvatar, r.getUserAvatar());
        }
    }
}
