package com.pizza.app.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pizza.app.databinding.ActivitySearchBinding;
import com.pizza.app.util.Constants;
import com.pizza.app.view.adapter.ProductAdapter;
import com.pizza.app.viewmodel.HomeViewModel;

/**
 * Tìm kiếm sản phẩm với debounce 400ms
 */
public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private HomeViewModel         viewModel;
    private ProductAdapter        adapter;
    private final Handler         debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable              debounceRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding   = ActivitySearchBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupRecyclerView();
        setupSearchInput();

        // Focus và hiện bàn phím ngay
        binding.etSearch.requestFocus();
    }

    private void setupRecyclerView() {
        adapter = new ProductAdapter(product -> {
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra(Constants.EXTRA_PRODUCT_ID, product.getId());
            startActivity(intent);
        });
        binding.rvResults.setLayoutManager(new LinearLayoutManager(this));
        binding.rvResults.setAdapter(adapter);
    }

    private void setupSearchInput() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int st, int b, int count) {
                // Debounce 400ms — tránh gọi Firestore mỗi ký tự
                if (debounceRunnable != null) debounceHandler.removeCallbacks(debounceRunnable);
                debounceRunnable = () -> performSearch(s.toString());
                debounceHandler.postDelayed(debounceRunnable, 400);
            }
        });

        binding.btnClear.setOnClickListener(v -> {
            binding.etSearch.setText("");
            adapter.setItems(null);
            binding.tvEmpty.setVisibility(View.GONE);
        });
    }

    private void performSearch(String keyword) {
        if (keyword.trim().isEmpty()) {
            adapter.setItems(null);
            binding.tvEmpty.setVisibility(View.GONE);
            return;
        }
        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.search(keyword).observe(this, result -> {
            binding.progressBar.setVisibility(View.GONE);
            if (result.isSuccess() && result.data != null) {
                adapter.setItems(result.data);
                binding.tvEmpty.setVisibility(result.data.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (debounceRunnable != null) debounceHandler.removeCallbacks(debounceRunnable);
    }
}
