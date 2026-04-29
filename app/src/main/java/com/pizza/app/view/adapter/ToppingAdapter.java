package com.pizza.app.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pizza.app.databinding.ItemToppingBinding;
import com.pizza.app.model.Topping;
import com.pizza.app.util.CurrencyUtils;

import java.util.ArrayList;
import java.util.List;

public class ToppingAdapter extends RecyclerView.Adapter<ToppingAdapter.VH> {

    public interface OnToppingClick { void onClick(Topping topping); }

    private final List<Topping>  items    = new ArrayList<>();
    private final List<String>   selected = new ArrayList<>(); // IDs đang được chọn
    private final OnToppingClick listener;

    public ToppingAdapter(OnToppingClick listener) { this.listener = listener; }

    public void setItems(List<Topping> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    public void setSelectedToppings(List<Topping> selectedList) {
        selected.clear();
        if (selectedList != null) {
            for (Topping t : selectedList) selected.add(t.getId());
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemToppingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Topping t = items.get(position);
        holder.bind(t, selected.contains(t.getId()));
        holder.itemView.setOnClickListener(v -> {
            if (selected.contains(t.getId())) selected.remove(t.getId());
            else selected.add(t.getId());
            notifyItemChanged(position);
            listener.onClick(t);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        private final ItemToppingBinding binding;

        VH(ItemToppingBinding b) { super(b.getRoot()); binding = b; }

        void bind(Topping t, boolean isSelected) {
            binding.tvToppingName.setText(t.getName());
            binding.tvToppingPrice.setText("+" + CurrencyUtils.format(t.getPrice()));
            binding.cbTopping.setChecked(isSelected);
            binding.getRoot().setSelected(isSelected);
        }
    }
}
