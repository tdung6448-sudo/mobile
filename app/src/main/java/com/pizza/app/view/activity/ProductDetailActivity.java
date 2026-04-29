package com.pizza.app.view.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.firebase.auth.FirebaseAuth;
import com.pizza.app.databinding.ActivityProductDetailBinding;
import com.pizza.app.model.CartItem;
import com.pizza.app.model.Crust;
import com.pizza.app.model.Product;
import com.pizza.app.model.ProductSize;
import com.pizza.app.model.Topping;
import com.pizza.app.util.Constants;
import com.pizza.app.util.CurrencyUtils;
import com.pizza.app.view.adapter.CrustAdapter;
import com.pizza.app.view.adapter.ProductImageAdapter;
import com.pizza.app.view.adapter.ReviewAdapter;
import com.pizza.app.view.adapter.SizeAdapter;
import com.pizza.app.view.adapter.ToppingAdapter;
import com.pizza.app.viewmodel.CartViewModel;
import com.pizza.app.viewmodel.ProductDetailViewModel;

/**
 * Màn hình chi tiết sản phẩm — chọn size, đế, topping, số lượng → thêm vào giỏ
 */
public class ProductDetailActivity extends AppCompatActivity {

    private ActivityProductDetailBinding binding;
    private ProductDetailViewModel       detailViewModel;
    private CartViewModel                cartViewModel;
    private String                       productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding         = ActivityProductDetailBinding.inflate(getLayoutInflater());
        detailViewModel = new ViewModelProvider(this).get(ProductDetailViewModel.class);
        cartViewModel   = new ViewModelProvider(this).get(CartViewModel.class);
        setContentView(binding.getRoot());

        productId = getIntent().getStringExtra(Constants.EXTRA_PRODUCT_ID);
        if (productId == null) { finish(); return; }

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        setupQuantityControls();
        loadProduct();
    }

    private void loadProduct() {
        detailViewModel.getProduct(productId).observe(this, result -> {
            if (result.isSuccess() && result.data != null) {
                Product product = result.data;
                detailViewModel.setProduct(product);
                bindProductUI(product);
                setupSizeSelector(product);
                setupCrustSelector(product);
                setupToppingSelector(product);
                loadReviews();
            }
        });
    }

    private void bindProductUI(Product product) {
        binding.tvProductName.setText(product.getName());
        binding.tvDescription.setText(product.getDescription());
        binding.tvIngredients.setText(product.getIngredients());
        binding.ratingBar.setRating(product.getRatingAvg());
        binding.tvRatingCount.setText("(" + product.getRatingCount() + " đánh giá)");

        // Ảnh slideshow
        ProductImageAdapter imageAdapter = new ProductImageAdapter(product.getImages());
        binding.viewPagerImages.setAdapter(imageAdapter);
        binding.dotsIndicator.attachTo(binding.viewPagerImages);

        // Badge trạng thái
        binding.badgeNew.setVisibility(product.isNew() ? View.VISIBLE : View.GONE);
        binding.badgeBestSeller.setVisibility(product.isBestSeller() ? View.VISIBLE : View.GONE);

        // Allergens
        if (product.getAllergens() != null && !product.getAllergens().isEmpty()) {
            binding.tvAllergens.setText("Dị ứng: " + android.text.TextUtils.join(", ", product.getAllergens()));
            binding.tvAllergens.setVisibility(View.VISIBLE);
        }
    }

    private void setupSizeSelector(Product product) {
        SizeAdapter sizeAdapter = new SizeAdapter(size -> detailViewModel.selectSize(size));
        binding.rvSizes.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvSizes.setAdapter(sizeAdapter);
        sizeAdapter.setItems(product.getSizes());

        detailViewModel.getSelectedSize().observe(this, size -> {
            sizeAdapter.setSelectedSize(size);
            updateTotalPrice();
        });
    }

    private void setupCrustSelector(Product product) {
        if (product.getCrusts() == null || product.getCrusts().isEmpty()) {
            binding.sectionCrust.setVisibility(View.GONE);
            return;
        }
        CrustAdapter crustAdapter = new CrustAdapter(crust -> detailViewModel.selectCrust(crust));
        binding.rvCrusts.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvCrusts.setAdapter(crustAdapter);
        crustAdapter.setItems(product.getCrusts());

        detailViewModel.getSelectedCrust().observe(this, crust -> {
            crustAdapter.setSelectedCrust(crust);
            updateTotalPrice();
        });
    }

    private void setupToppingSelector(Product product) {
        if (product.getToppings() == null || product.getToppings().isEmpty()) {
            binding.sectionToppings.setVisibility(View.GONE);
            return;
        }
        ToppingAdapter toppingAdapter = new ToppingAdapter(topping -> {
            detailViewModel.toggleTopping(topping);
        });

        FlexboxLayoutManager flexManager = new FlexboxLayoutManager(this);
        flexManager.setFlexDirection(FlexDirection.ROW);
        flexManager.setJustifyContent(JustifyContent.FLEX_START);
        binding.rvToppings.setLayoutManager(flexManager);
        binding.rvToppings.setAdapter(toppingAdapter);
        toppingAdapter.setItems(product.getToppings());

        detailViewModel.getSelectedToppings().observe(this, selected -> {
            toppingAdapter.setSelectedToppings(selected);
            updateTotalPrice();
        });
    }

    private void setupQuantityControls() {
        binding.btnDecrease.setOnClickListener(v -> {
            Integer qty = detailViewModel.getQuantity().getValue();
            if (qty != null && qty > 1) detailViewModel.setQuantity(qty - 1);
        });
        binding.btnIncrease.setOnClickListener(v -> {
            Integer qty = detailViewModel.getQuantity().getValue();
            detailViewModel.setQuantity(qty != null ? qty + 1 : 1);
        });

        detailViewModel.getQuantity().observe(this, qty -> {
            binding.tvQuantity.setText(String.valueOf(qty));
            updateTotalPrice();
        });

        // Thêm vào giỏ với animation
        binding.btnAddToCart.setOnClickListener(v -> addToCart());
    }

    private void updateTotalPrice() {
        detailViewModel.getTotalPrice().observe(this, total -> {
            if (total != null) {
                binding.btnAddToCart.setText("Thêm vào giỏ — " + CurrencyUtils.format(total));
            }
        });
    }

    private void addToCart() {
        CartItem item = detailViewModel.buildCartItem();
        if (item == null) return;

        cartViewModel.addToCart(item);
        binding.btnAddToCart.setText("Đã thêm vào giỏ ✓");
        binding.btnAddToCart.postDelayed(() ->
                binding.btnAddToCart.setText("Thêm vào giỏ — "
                        + CurrencyUtils.format(detailViewModel.getTotalPrice().getValue() != null
                                ? detailViewModel.getTotalPrice().getValue() : 0L)),
                1500);
    }

    private void loadReviews() {
        ReviewAdapter reviewAdapter = new ReviewAdapter();
        binding.rvReviews.setLayoutManager(new LinearLayoutManager(this));
        binding.rvReviews.setNestedScrollingEnabled(false);
        binding.rvReviews.setAdapter(reviewAdapter);

        detailViewModel.getReviews(productId).observe(this, result -> {
            if (result.isSuccess() && result.data != null) {
                reviewAdapter.setItems(result.data);
                binding.tvNoReviews.setVisibility(result.data.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }
}
