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
import androidx.recyclerview.widget.GridLayoutManager;

import com.pizza.app.databinding.FragmentMenuBinding;
import com.pizza.app.view.activity.ProductDetailActivity;
import com.pizza.app.view.adapter.CategoryAdapter;
import com.pizza.app.view.adapter.ProductAdapter;
import com.pizza.app.util.Constants;
import com.pizza.app.viewmodel.HomeViewModel;

/**
 * Màn hình Menu — lưới sản phẩm có filter theo danh mục
 */
public class MenuFragment extends Fragment {

    private FragmentMenuBinding binding;
    private HomeViewModel       viewModel;
    private ProductAdapter      productAdapter;
    private String              selectedCategoryId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                              @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding   = FragmentMenuBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Đọc category filter từ arguments (từ HomeFragment)
        if (getArguments() != null) {
            selectedCategoryId = getArguments().getString(Constants.EXTRA_CATEGORY_ID);
        }

        setupCategoryFilter();
        setupProductGrid();
    }

    private void setupCategoryFilter() {
        CategoryAdapter adapter = new CategoryAdapter(category -> {
            selectedCategoryId = category.getId();
            loadProducts();
        });
        binding.rvCategoryFilter.setAdapter(adapter);

        viewModel.getCategories().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess() && result.data != null) {
                adapter.setItems(result.data);
                // Highlight category được chọn từ Home
                if (selectedCategoryId != null) {
                    adapter.setSelectedId(selectedCategoryId);
                    loadProducts();
                }
            }
        });
    }

    private void setupProductGrid() {
        productAdapter = new ProductAdapter(product -> {
            Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
            intent.putExtra(Constants.EXTRA_PRODUCT_ID, product.getId());
            startActivity(intent);
        });

        // 2 cột trên điện thoại
        binding.rvProducts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvProducts.setAdapter(productAdapter);

        // Load tất cả sản phẩm nổi bật khi không có filter
        if (selectedCategoryId == null) {
            viewModel.getFeaturedProducts().observe(getViewLifecycleOwner(), result -> {
                if (result.isSuccess() && result.data != null) {
                    productAdapter.setItems(result.data);
                }
                setLoading(false);
            });
        } else {
            loadProducts();
        }
    }

    private void loadProducts() {
        if (selectedCategoryId == null) return;
        setLoading(true);
        // Dùng ProductRepository trực tiếp thay vì ViewModel để filter theo category
        new com.pizza.app.repository.ProductRepository()
                .getProductsByCategory(selectedCategoryId, false)
                .observe(getViewLifecycleOwner(), result -> {
                    setLoading(false);
                    if (result.isSuccess() && result.data != null) {
                        productAdapter.setItems(result.data);
                        binding.tvEmpty.setVisibility(result.data.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }

    private void setLoading(boolean loading) {
        if (binding == null) return;
        binding.shimmerProducts.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.rvProducts.setVisibility(loading ? View.GONE : View.VISIBLE);
        if (loading) binding.shimmerProducts.startShimmer();
        else         binding.shimmerProducts.stopShimmer();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
