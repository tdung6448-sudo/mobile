package com.pizza.app.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pizza.app.R;
import com.pizza.app.databinding.FragmentHomeBinding;
import com.pizza.app.view.activity.ProductDetailActivity;
import com.pizza.app.view.activity.SearchActivity;
import com.pizza.app.view.adapter.BannerAdapter;
import com.pizza.app.view.adapter.CategoryAdapter;
import com.pizza.app.view.adapter.ProductAdapter;
import com.pizza.app.util.Constants;
import com.pizza.app.viewmodel.HomeViewModel;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Trang chủ — banner, danh mục, sản phẩm nổi bật / mới / best seller
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel       viewModel;
    private BannerAdapter       bannerAdapter;
    private Timer               bannerTimer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                              @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding   = FragmentHomeBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupSearch();
        setupBanner();
        setupCategories();
        setupFeaturedProducts();
        setupNewProducts();
        setupBestSellers();
    }

    private void setupSearch() {
        binding.etSearch.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SearchActivity.class)));
        binding.etSearch.setFocusable(false); // Bấm để mở SearchActivity thay vì gõ trực tiếp
    }

    private void setupBanner() {
        bannerAdapter = new BannerAdapter();
        binding.viewPagerBanner.setAdapter(bannerAdapter);
        binding.dotsIndicator.attachTo(binding.viewPagerBanner);

        viewModel.getBanners().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess() && result.data != null) {
                bannerAdapter.setItems(result.data);
                startBannerAutoScroll();
            }
        });
    }

    /** Auto-scroll banner mỗi 3 giây */
    private void startBannerAutoScroll() {
        if (bannerTimer != null) bannerTimer.cancel();
        bannerTimer = new Timer();
        bannerTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (!isAdded() || binding == null) return;
                    int count   = bannerAdapter.getItemCount();
                    int current = binding.viewPagerBanner.getCurrentItem();
                    binding.viewPagerBanner.setCurrentItem((current + 1) % count, true);
                });
            }
        }, 3000, 3000);
    }

    private void setupCategories() {
        CategoryAdapter adapter = new CategoryAdapter(category -> {
            // Chuyển sang MenuFragment filter theo category
            Intent intent = new Intent(requireContext(), com.pizza.app.view.activity.MainActivity.class);
            // Truyền categoryId qua argument fragment
            MenuFragment menuFragment = new MenuFragment();
            Bundle args = new Bundle();
            args.putString(Constants.EXTRA_CATEGORY_ID, category.getId());
            menuFragment.setArguments(args);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, menuFragment)
                    .commit();
        });

        binding.rvCategories.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvCategories.setAdapter(adapter);

        viewModel.getCategories().observe(getViewLifecycleOwner(), result -> {
            binding.shimmerCategories.stopShimmer();
            binding.shimmerCategories.setVisibility(View.GONE);
            if (result.isSuccess() && result.data != null) {
                adapter.setItems(result.data);
            }
        });
    }

    private void setupFeaturedProducts() {
        ProductAdapter adapter = buildProductAdapter();
        binding.rvFeatured.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvFeatured.setAdapter(adapter);

        viewModel.getFeaturedProducts().observe(getViewLifecycleOwner(), result -> {
            binding.shimmerFeatured.stopShimmer();
            binding.shimmerFeatured.setVisibility(View.GONE);
            if (result.isSuccess() && result.data != null) {
                adapter.setItems(result.data);
                binding.sectionFeatured.setVisibility(
                        result.data.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void setupNewProducts() {
        ProductAdapter adapter = buildProductAdapter();
        binding.rvNew.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvNew.setAdapter(adapter);

        viewModel.getNewProducts().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess() && result.data != null) {
                adapter.setItems(result.data);
                binding.sectionNew.setVisibility(
                        result.data.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void setupBestSellers() {
        ProductAdapter adapter = buildProductAdapter();
        binding.rvBestSeller.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvBestSeller.setAdapter(adapter);

        viewModel.getBestSellers().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess() && result.data != null) {
                adapter.setItems(result.data);
                binding.sectionBestSeller.setVisibility(
                        result.data.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });
    }

    private ProductAdapter buildProductAdapter() {
        return new ProductAdapter(product -> {
            Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
            intent.putExtra(Constants.EXTRA_PRODUCT_ID, product.getId());
            startActivity(intent);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (bannerTimer != null) bannerTimer.cancel();
        binding = null;
    }
}
