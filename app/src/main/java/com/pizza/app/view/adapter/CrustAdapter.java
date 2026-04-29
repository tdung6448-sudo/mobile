package com.pizza.app.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pizza.app.databinding.ItemCrustBinding;
import com.pizza.app.model.Crust;
import com.pizza.app.util.CurrencyUtils;

import java.util.ArrayList;
import java.util.List;

public class CrustAdapter extends RecyclerView.Adapter<CrustAdapter.VH> {

    public interface OnCrustClick { void onClick(Crust crust); }

    private final List<Crust> items = new ArrayList<>();
    private final OnCrustClick listener;
    private Crust selectedCrust;

    public CrustAdapter(OnCrustClick listener) { this.listener = listener; }

    public void setItems(List<Crust> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    public void setSelectedCrust(Crust crust) {
        selectedCrust = crust;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemCrustBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Crust crust = items.get(position);
        boolean selected = selectedCrust != null && selectedCrust.getId().equals(crust.getId());
        holder.bind(crust, selected);
        holder.itemView.setOnClickListener(v -> {
            selectedCrust = crust;
            notifyDataSetChanged();
            listener.onClick(crust);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        private final ItemCrustBinding binding;

        VH(ItemCrustBinding b) { super(b.getRoot()); binding = b; }

        void bind(Crust crust, boolean selected) {
            binding.tvCrustName.setText(crust.getName());
            binding.tvExtraPrice.setText(crust.getExtraPrice() > 0
                    ? "+" + CurrencyUtils.format(crust.getExtraPrice()) : "Miễn phí");
            binding.getRoot().setSelected(selected);
        }
    }
}
