package com.pizza.app.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pizza.app.databinding.ItemAddressBinding;
import com.pizza.app.model.Address;

import java.util.ArrayList;
import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.VH> {

    public interface OnDeleteClick { void onDelete(String addressId); }

    private final List<Address> items = new ArrayList<>();
    private final OnDeleteClick listener;

    public AddressAdapter(OnDeleteClick listener) { this.listener = listener; }

    public void setItems(List<Address> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    public int getItemCount() { return items.size(); }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemAddressBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Address addr = items.get(position);
        holder.bind(addr);
        holder.binding.btnDelete.setOnClickListener(v -> listener.onDelete(addr.getId()));
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemAddressBinding binding;

        VH(ItemAddressBinding b) { super(b.getRoot()); binding = b; }

        void bind(Address addr) {
            binding.tvLabel.setText(addr.getLabel());
            binding.tvFullAddress.setText(addr.getFullAddress());
            binding.ivDefault.setVisibility(addr.isDefault()
                    ? android.view.View.VISIBLE : android.view.View.GONE);
        }
    }
}
