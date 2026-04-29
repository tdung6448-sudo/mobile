package com.pizza.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.pizza.app.model.CartItem;
import com.pizza.app.model.Crust;
import com.pizza.app.model.Product;
import com.pizza.app.model.ProductSize;
import com.pizza.app.model.Review;
import com.pizza.app.model.Topping;
import com.pizza.app.repository.AuthRepository.Result;
import com.pizza.app.repository.ProductRepository;
import com.pizza.app.repository.ReviewRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel chi tiết sản phẩm — quản lý lựa chọn size/crust/topping
 */
public class ProductDetailViewModel extends ViewModel {

    private final ProductRepository productRepo = new ProductRepository();
    private final ReviewRepository  reviewRepo  = new ReviewRepository();

    // Trạng thái lựa chọn của người dùng
    private final MutableLiveData<ProductSize>  selectedSize    = new MutableLiveData<>();
    private final MutableLiveData<Crust>        selectedCrust   = new MutableLiveData<>();
    private final MutableLiveData<List<Topping>> selectedToppings = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer>       quantity        = new MutableLiveData<>(1);
    private final MutableLiveData<Long>          totalPrice      = new MutableLiveData<>(0L);

    private Product currentProduct;

    // ── Load dữ liệu ──────────────────────────────────────────────

    public LiveData<Result<Product>> getProduct(String productId) {
        return productRepo.getProductById(productId);
    }

    public LiveData<Result<List<Review>>> getReviews(String productId) {
        return reviewRepo.getReviews(productId);
    }

    // ── Gán sản phẩm hiện tại và khởi tạo lựa chọn mặc định ──────

    public void setProduct(Product product) {
        currentProduct = product;

        // Chọn mặc định size M (hoặc phần tử đầu tiên)
        if (product.getSizes() != null && !product.getSizes().isEmpty()) {
            ProductSize defaultSize = product.getSizes().get(0);
            for (ProductSize s : product.getSizes()) {
                if ("M".equals(s.getCode())) { defaultSize = s; break; }
            }
            selectedSize.setValue(defaultSize);
        }

        // Chọn đế mặc định (phần tử đầu)
        if (product.getCrusts() != null && !product.getCrusts().isEmpty()) {
            selectedCrust.setValue(product.getCrusts().get(0));
        }

        recalculateTotal();
    }

    // ── Cập nhật lựa chọn ─────────────────────────────────────────

    public void selectSize(ProductSize size) {
        selectedSize.setValue(size);
        recalculateTotal();
    }

    public void selectCrust(Crust crust) {
        selectedCrust.setValue(crust);
        recalculateTotal();
    }

    public void toggleTopping(Topping topping) {
        List<Topping> current = selectedToppings.getValue();
        if (current == null) current = new ArrayList<>();

        boolean found = false;
        for (Topping t : current) {
            if (t.getId().equals(topping.getId())) {
                current.remove(t);
                found = true;
                break;
            }
        }
        if (!found) current.add(topping);

        selectedToppings.setValue(current);
        recalculateTotal();
    }

    public void setQuantity(int qty) {
        if (qty < 1) return;
        quantity.setValue(qty);
        recalculateTotal();
    }

    // ── Observables ────────────────────────────────────────────────

    public LiveData<ProductSize>   getSelectedSize()     { return selectedSize; }
    public LiveData<Crust>         getSelectedCrust()    { return selectedCrust; }
    public LiveData<List<Topping>> getSelectedToppings() { return selectedToppings; }
    public LiveData<Integer>       getQuantity()         { return quantity; }
    public LiveData<Long>          getTotalPrice()       { return totalPrice; }

    // ── Build CartItem ─────────────────────────────────────────────

    public CartItem buildCartItem() {
        if (currentProduct == null) return null;

        CartItem item = new CartItem();
        item.setProductId(currentProduct.getId());
        item.setProductName(currentProduct.getName());
        item.setProductImage(currentProduct.getThumbnail());
        item.setQuantity(quantity.getValue() != null ? quantity.getValue() : 1);

        ProductSize size = selectedSize.getValue();
        if (size != null) {
            item.setSizeCode(size.getCode());
            item.setSizeName(size.getName());
        }

        Crust crust = selectedCrust.getValue();
        if (crust != null) {
            item.setCrustId(crust.getId());
            item.setCrustName(crust.getName());
        }

        item.setSelectedToppings(new ArrayList<>(
                selectedToppings.getValue() != null ? selectedToppings.getValue() : new ArrayList<>()));

        item.setUnitPrice(calculateUnitPrice());
        return item;
    }

    // ── Private helpers ────────────────────────────────────────────

    private long calculateUnitPrice() {
        if (currentProduct == null) return 0;

        ProductSize size = selectedSize.getValue();
        long price = (size != null) ? size.getPrice() : currentProduct.getBasePrice();

        Crust crust = selectedCrust.getValue();
        if (crust != null) price += crust.getExtraPrice();

        List<Topping> toppings = selectedToppings.getValue();
        if (toppings != null) {
            for (Topping t : toppings) price += t.getPrice();
        }

        return price;
    }

    private void recalculateTotal() {
        int qty = quantity.getValue() != null ? quantity.getValue() : 1;
        totalPrice.setValue(calculateUnitPrice() * qty);
    }

    /** Gửi đánh giá */
    public LiveData<Result<Void>> submitReview(Review review) {
        return reviewRepo.submitReview(review);
    }
}
