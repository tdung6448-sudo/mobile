package com.pizza.app.view.activity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.storage.FirebaseStorage;
import com.pizza.app.databinding.ActivityAdminProductEditBinding;
import com.pizza.app.model.Crust;
import com.pizza.app.model.Product;
import com.pizza.app.model.ProductSize;
import com.pizza.app.util.Constants;
import com.pizza.app.util.GlideHelper;
import com.pizza.app.viewmodel.AdminViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Form thêm / sửa sản phẩm (Admin)
 */
public class AdminProductEditActivity extends AppCompatActivity {

    private ActivityAdminProductEditBinding binding;
    private AdminViewModel                  viewModel;
    private Product                         product;
    private String                          uploadedImageUrl = "";

    private final ActivityResultLauncher<String> imageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::uploadProductImage);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding   = ActivityAdminProductEditBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(AdminViewModel.class);
        setContentView(binding.getRoot());

        String productId = getIntent().getStringExtra(Constants.EXTRA_PRODUCT_ID);
        binding.toolbar.setTitle(productId == null ? "Thêm sản phẩm" : "Sửa sản phẩm");
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.ivProductImage.setOnClickListener(v -> imageLauncher.launch("image/*"));
        binding.btnSave.setOnClickListener(v -> saveProduct());

        if (productId != null) loadProduct(productId);
        else product = new Product();

        setupDefaultSizes();
        setupDefaultCrusts();
    }

    private void loadProduct(String productId) {
        viewModel.getAllProducts().observe(this, result -> {
            if (!result.isSuccess() || result.data == null) return;
            for (Product p : result.data) {
                if (p.getId().equals(productId)) {
                    product = p;
                    bindProductToForm(p);
                    return;
                }
            }
        });
    }

    private void bindProductToForm(Product p) {
        binding.etName.setText(p.getName());
        binding.etDescription.setText(p.getDescription());
        binding.etIngredients.setText(p.getIngredients());
        binding.etBasePrice.setText(String.valueOf(p.getBasePrice()));
        binding.switchAvailable.setChecked(p.isAvailable());
        binding.switchFeatured.setChecked(p.isFeatured());
        binding.switchBestSeller.setChecked(p.isBestSeller());
        binding.switchNew.setChecked(p.isNew());

        if (p.getThumbnail() != null && !p.getThumbnail().isEmpty()) {
            GlideHelper.loadProduct(binding.ivProductImage, p.getThumbnail());
            uploadedImageUrl = p.getThumbnail();
        }
    }

    /** Thiết lập 3 size mặc định S/M/L */
    private void setupDefaultSizes() {
        if (product == null || product.getSizes() == null || product.getSizes().isEmpty()) {
            List<ProductSize> sizes = new ArrayList<>();
            sizes.add(new ProductSize("S", "Nhỏ (S)", 85_000L));
            sizes.add(new ProductSize("M", "Vừa (M)", 115_000L));
            sizes.add(new ProductSize("L", "Lớn (L)", 145_000L));
            binding.etSizeS.setText("85000");
            binding.etSizeM.setText("115000");
            binding.etSizeL.setText("145000");
        }
    }

    private void setupDefaultCrusts() {
        if (product == null || product.getCrusts() == null || product.getCrusts().isEmpty()) {
            binding.etCrustThin.setText("0");
            binding.etCrustThick.setText("0");
            binding.etCrustCheese.setText("20000");
        }
    }

    private void uploadProductImage(Uri uri) {
        if (uri == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);

        String path = Constants.STORAGE_PRODUCTS + "/" + UUID.randomUUID() + ".jpg";
        FirebaseStorage.getInstance().getReference(path)
                .putFile(uri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    return FirebaseStorage.getInstance().getReference(path).getDownloadUrl();
                })
                .addOnSuccessListener(downloadUri -> {
                    binding.progressBar.setVisibility(View.GONE);
                    uploadedImageUrl = downloadUri.toString();
                    GlideHelper.loadProduct(binding.ivProductImage, uploadedImageUrl);
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Upload ảnh thất bại", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveProduct() {
        String name = binding.etName.getText().toString().trim();
        if (name.isEmpty()) {
            binding.tilName.setError("Nhập tên sản phẩm");
            return;
        }

        if (product == null) product = new Product();
        product.setName(name);
        product.setDescription(binding.etDescription.getText().toString().trim());
        product.setIngredients(binding.etIngredients.getText().toString().trim());

        try {
            product.setBasePrice(Long.parseLong(binding.etBasePrice.getText().toString()));
        } catch (NumberFormatException e) { product.setBasePrice(0); }

        product.setAvailable(binding.switchAvailable.isChecked());
        product.setFeatured(binding.switchFeatured.isChecked());
        product.setBestSeller(binding.switchBestSeller.isChecked());
        product.setNew(binding.switchNew.isChecked());

        // Sizes
        List<ProductSize> sizes = new ArrayList<>();
        sizes.add(new ProductSize("S", "Nhỏ (S)", parseLong(binding.etSizeS.getText().toString())));
        sizes.add(new ProductSize("M", "Vừa (M)", parseLong(binding.etSizeM.getText().toString())));
        sizes.add(new ProductSize("L", "Lớn (L)", parseLong(binding.etSizeL.getText().toString())));
        product.setSizes(sizes);

        // Crusts
        List<Crust> crusts = new ArrayList<>();
        crusts.add(new Crust("thin",   "Mỏng giòn",   parseLong(binding.etCrustThin.getText().toString())));
        crusts.add(new Crust("thick",  "Dày xốp",     parseLong(binding.etCrustThick.getText().toString())));
        crusts.add(new Crust("cheese", "Viền phô mai", parseLong(binding.etCrustCheese.getText().toString())));
        product.setCrusts(crusts);

        // Ảnh
        if (!uploadedImageUrl.isEmpty()) {
            product.setImages(new ArrayList<>(Arrays.asList(uploadedImageUrl)));
        }

        setLoading(true);
        viewModel.saveProduct(product).observe(this, result -> {
            setLoading(false);
            if (result.isSuccess()) {
                Toast.makeText(this, "Đã lưu sản phẩm", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private long parseLong(String s) {
        try { return Long.parseLong(s.trim()); }
        catch (NumberFormatException e) { return 0L; }
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSave.setEnabled(!loading);
    }
}
