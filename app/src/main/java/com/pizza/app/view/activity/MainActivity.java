package com.pizza.app.view.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.pizza.app.R;
import com.pizza.app.databinding.ActivityMainBinding;
import com.pizza.app.util.Constants;
import com.pizza.app.view.fragment.CartFragment;
import com.pizza.app.view.fragment.HomeFragment;
import com.pizza.app.view.fragment.MenuFragment;
import com.pizza.app.view.fragment.OrdersFragment;
import com.pizza.app.view.fragment.ProfileFragment;
import com.pizza.app.viewmodel.CartViewModel;

/**
 * MainActivity dành cho Customer — chứa BottomNavigationView + 5 Fragment
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private CartViewModel       cartViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding       = ActivityMainBinding.inflate(getLayoutInflater());
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        setContentView(binding.getRoot());

        setupBottomNavigation();
        observeCartBadge();
        subscribeToFCMTopics();

        // Hiện Home fragment mặc định
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private void setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            if      (id == R.id.nav_home)    fragment = new HomeFragment();
            else if (id == R.id.nav_menu)    fragment = new MenuFragment();
            else if (id == R.id.nav_cart)    fragment = new CartFragment();
            else if (id == R.id.nav_orders)  fragment = new OrdersFragment();
            else if (id == R.id.nav_profile) fragment = new ProfileFragment();

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    /** Badge đỏ trên icon giỏ hàng — hiển thị số lượng món */
    private void observeCartBadge() {
        cartViewModel.getCart().observe(this, cartItems -> {
            int count = 0;
            if (cartItems != null) {
                for (com.pizza.app.model.CartItem item : cartItems) count += item.getQuantity();
            }

            BadgeDrawable badge = binding.bottomNav.getOrCreateBadge(R.id.nav_cart);
            if (count > 0) {
                badge.setVisible(true);
                badge.setNumber(count);
            } else {
                badge.setVisible(false);
            }
        });
    }

    private void subscribeToFCMTopics() {
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.TOPIC_PROMOTIONS);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    /** Công khai để CartFragment/HomeFragment có thể chuyển tab */
    public void navigateToTab(int itemId) {
        binding.bottomNav.setSelectedItemId(itemId);
    }
}
