package com.pizza.app.view.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.pizza.app.R;
import com.pizza.app.databinding.ActivityAdminMainBinding;
import com.pizza.app.view.fragment.AdminDashboardFragment;
import com.pizza.app.view.fragment.AdminOrdersFragment;
import com.pizza.app.view.fragment.AdminProductsFragment;
import com.pizza.app.view.fragment.AdminUsersFragment;
import com.pizza.app.view.fragment.AdminVouchersFragment;

/**
 * Admin main — BottomNav với 5 tab quản lý
 */
public class AdminMainActivity extends AppCompatActivity {

    ActivityAdminMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNav();

        // Dashboard mặc định
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.adminFragmentContainer, new AdminDashboardFragment())
                    .commit();
        }

        // Nếu được mở từ notification đơn hàng mới → chuyển sang tab Orders
        if (getIntent().hasExtra(com.pizza.app.util.Constants.EXTRA_ORDER_ID)) {
            binding.adminBottomNav.setSelectedItemId(R.id.admin_nav_orders);
        }
    }

    private void setupBottomNav() {
        binding.adminBottomNav.setOnItemSelectedListener(item -> {
            androidx.fragment.app.Fragment fragment = null;
            int id = item.getItemId();

            if      (id == R.id.admin_nav_dashboard) fragment = new AdminDashboardFragment();
            else if (id == R.id.admin_nav_products)  fragment = new AdminProductsFragment();
            else if (id == R.id.admin_nav_orders)    fragment = new AdminOrdersFragment();
            else if (id == R.id.admin_nav_users)     fragment = new AdminUsersFragment();
            else if (id == R.id.admin_nav_vouchers)  fragment = new AdminVouchersFragment();

            if (fragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.adminFragmentContainer, fragment)
                        .commit();
                return true;
            }
            return false;
        });
    }
}
