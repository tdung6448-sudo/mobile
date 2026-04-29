package com.pizza.app.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pizza.app.databinding.FragmentAdminProductsBinding;
import com.pizza.app.util.Constants;
import com.pizza.app.view.activity.AdminProductEditActivity;
import com.pizza.app.view.adapter.AdminProductAdapter;
import com.pizza.app.viewmodel.AdminViewModel;

public class AdminProductsFragment extends Fragment {

    private FragmentAdminProductsBinding binding;
    private AdminViewModel               viewModel;
    private AdminProductAdapter          adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                              @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding   = FragmentAdminProductsBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new AdminProductAdapter(
                // Edit
                product -> {
                    Intent intent = new Intent(requireContext(), AdminProductEditActivity.class);
                    intent.putExtra(Constants.EXTRA_PRODUCT_ID, product.getId());
                    startActivity(intent);
                },
                // Toggle availability
                (product, isAvailable) -> viewModel.toggleAvailability(product.getId(), isAvailable)
                        .observe(getViewLifecycleOwner(), r -> {
                            if (r.isError()) Toast.makeText(requireContext(), r.message, Toast.LENGTH_SHORT).show();
                        }),
                // Delete
                product -> new AlertDialog.Builder(requireContext())
                        .setTitle("Xóa sản phẩm")
                        .setMessage("Xóa \"" + product.getName() + "\"?")
                        .setPositiveButton("Xóa", (d, w) ->
                                viewModel.deleteProduct(product.getId())
                                        .observe(getViewLifecycleOwner(), r -> {}))
                        .setNegativeButton("Huỷ", null)
                        .show()
        );

        binding.rvProducts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvProducts.setAdapter(adapter);

        // FAB thêm sản phẩm mới
        binding.fabAddProduct.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AdminProductEditActivity.class)));

        loadProducts();
    }

    private void loadProducts() {
        viewModel.getAllProducts().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess() && result.data != null) {
                adapter.setItems(result.data);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
