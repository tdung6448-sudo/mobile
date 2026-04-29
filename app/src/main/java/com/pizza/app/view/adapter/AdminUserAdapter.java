package com.pizza.app.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pizza.app.databinding.ItemAdminUserBinding;
import com.pizza.app.model.User;
import com.pizza.app.util.DateUtils;
import com.pizza.app.util.GlideHelper;

import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.VH> {

    public interface OnBlockClick { void onClick(User user); }

    private final List<User>  items = new ArrayList<>();
    private final OnBlockClick listener;

    public AdminUserAdapter(OnBlockClick listener) { this.listener = listener; }

    public void setItems(List<User> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemAdminUserBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        User u = items.get(position);
        holder.bind(u);
        holder.binding.btnBlock.setOnClickListener(v -> listener.onClick(u));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemAdminUserBinding binding;

        VH(ItemAdminUserBinding b) { super(b.getRoot()); binding = b; }

        void bind(User u) {
            binding.tvName.setText(u.getName());
            binding.tvEmail.setText(u.getEmail());
            binding.tvRole.setText(u.getRole());
            if (u.getCreatedAt() != null) {
                binding.tvJoined.setText("Tham gia: " + DateUtils.formatDate(u.getCreatedAt()));
            }
            GlideHelper.loadAvatar(binding.ivAvatar, u.getAvatar());
            binding.btnBlock.setText(u.isBlocked() ? "Mở khoá" : "Khoá");
            binding.btnBlock.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            u.isBlocked()
                                    ? itemView.getContext().getColor(com.pizza.app.R.color.green)
                                    : itemView.getContext().getColor(com.pizza.app.R.color.red_primary)));
        }
    }
}
