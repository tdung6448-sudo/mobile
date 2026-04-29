package com.pizza.app.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.pizza.app.R;
import com.pizza.app.databinding.FragmentCartBinding;
import com.pizza.app.model.CartItem;
import com.pizza.app.util.CurrencyUtils;
import com.pizza.app.view.activity.CheckoutActivity;
import com.pizza.app.view.adapter.CartAdapter;
import com.pizza.app.viewmodel.CartViewModel;

import java.util.List;

/**
 * Giỏ hàng — danh sách món, swipe-to-delete, tính tổng tiền
 */
public class CartFragment extends Fragment {

    private FragmentCartBinding binding;
    private CartViewModel       viewModel;
    private CartAdapter         adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                              @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding   = FragmentCartBinding.inflate(inflater, container, false);
        // Dùng Activity-scoped ViewModel để chia sẻ với MainActivity (badge)
        viewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        observeCart();

        binding.btnCheckout.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CheckoutActivity.class)));
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter(
                // Tăng/giảm số lượng
                (position, newQty) -> viewModel.updateQuantity(position, newQty),
                // Xóa món
                position -> {
                    CartItem removed = viewModel.getCart().getValue() != null
                            ? viewModel.getCart().getValue().get(position) : null;
                    viewModel.removeItem(position);
                    if (removed != null) showUndoSnackbar(removed, position);
                });

        binding.rvCart.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCart.setAdapter(adapter);

        // Swipe to delete
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh,
                                   @NonNull RecyclerView.ViewHolder target) { return false; }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                List<CartItem> cart = viewModel.getCart().getValue();
                CartItem removed = (cart != null && pos < cart.size()) ? cart.get(pos) : null;
                viewModel.removeItem(pos);
                if (removed != null) showUndoSnackbar(removed, pos);
            }
        }).attachToRecyclerView(binding.rvCart);
    }

    private void observeCart() {
        viewModel.getCart().observe(getViewLifecycleOwner(), cartItems -> {
            adapter.setItems(cartItems);
            boolean empty = cartItems == null || cartItems.isEmpty();
            binding.layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.layoutSummary.setVisibility(empty ? View.GONE : View.VISIBLE);

            if (!empty) updateSummary();
        });

        viewModel.getShippingFee().observe(getViewLifecycleOwner(), fee -> updateSummary());
        viewModel.getAppliedVoucher().observe(getViewLifecycleOwner(), v -> updateSummary());
    }

    private void updateSummary() {
        binding.tvSubtotal.setText(CurrencyUtils.format(viewModel.getSubtotal()));
        binding.tvShipping.setText(CurrencyUtils.format(
                viewModel.getShippingFee().getValue() != null
                        ? viewModel.getShippingFee().getValue() : 15_000L));
        long discount = viewModel.getDiscount();
        binding.tvDiscount.setText(discount > 0 ? "-" + CurrencyUtils.format(discount) : "0đ");
        binding.tvTotal.setText(CurrencyUtils.format(viewModel.getTotal()));
    }

    private void showUndoSnackbar(CartItem item, int position) {
        Snackbar.make(binding.getRoot(),
                "Đã xóa " + item.getProductName(),
                Snackbar.LENGTH_LONG)
                .setAction("Hoàn tác", v -> viewModel.addToCart(item))
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
