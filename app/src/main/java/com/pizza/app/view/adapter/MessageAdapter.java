package com.pizza.app.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pizza.app.databinding.ItemMessageMeBinding;
import com.pizza.app.databinding.ItemMessageOtherBinding;
import com.pizza.app.model.Message;
import com.pizza.app.util.GlideHelper;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_ME    = 1;
    private static final int VIEW_OTHER = 2;

    private final List<Message> items = new ArrayList<>();
    private final String        currentUid;

    public MessageAdapter(String currentUid) { this.currentUid = currentUid; }

    public void setItems(List<Message> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return currentUid.equals(items.get(position).getSenderId()) ? VIEW_ME : VIEW_OTHER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_ME) {
            return new MeVH(ItemMessageMeBinding.inflate(inflater, parent, false));
        }
        return new OtherVH(ItemMessageOtherBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message msg = items.get(position);
        if (holder instanceof MeVH) ((MeVH) holder).bind(msg);
        else ((OtherVH) holder).bind(msg);
    }

    @Override
    public int getItemCount() { return items.size(); }

    // ── ViewHolder tin nhắn của mình ──────────────────────────────
    static class MeVH extends RecyclerView.ViewHolder {
        private final ItemMessageMeBinding binding;

        MeVH(ItemMessageMeBinding b) { super(b.getRoot()); binding = b; }

        void bind(Message msg) {
            if (msg.isImage()) {
                binding.tvText.setVisibility(View.GONE);
                binding.ivImage.setVisibility(View.VISIBLE);
                GlideHelper.loadProduct(binding.ivImage, msg.getImageUrl());
            } else {
                binding.tvText.setVisibility(View.VISIBLE);
                binding.ivImage.setVisibility(View.GONE);
                binding.tvText.setText(msg.getText());
            }
        }
    }

    // ── ViewHolder tin nhắn của người khác ────────────────────────
    static class OtherVH extends RecyclerView.ViewHolder {
        private final ItemMessageOtherBinding binding;

        OtherVH(ItemMessageOtherBinding b) { super(b.getRoot()); binding = b; }

        void bind(Message msg) {
            binding.tvSenderName.setText(msg.getSenderName());
            GlideHelper.loadAvatar(binding.ivAvatar, msg.getSenderAvatar());

            if (msg.isImage()) {
                binding.tvText.setVisibility(View.GONE);
                binding.ivImage.setVisibility(View.VISIBLE);
                GlideHelper.loadProduct(binding.ivImage, msg.getImageUrl());
            } else {
                binding.tvText.setVisibility(View.VISIBLE);
                binding.ivImage.setVisibility(View.GONE);
                binding.tvText.setText(msg.getText());
            }
        }
    }
}
