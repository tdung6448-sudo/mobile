package com.pizza.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.pizza.app.model.Banner;
import com.pizza.app.model.Category;
import com.pizza.app.model.Product;
import com.pizza.app.repository.ProductRepository;
import com.pizza.app.repository.AuthRepository.Result;

import java.util.List;

/**
 * ViewModel cho màn hình trang chủ
 */
public class HomeViewModel extends ViewModel {

    private final ProductRepository repo = new ProductRepository();

    private LiveData<Result<List<Banner>>>   banners;
    private LiveData<Result<List<Category>>> categories;
    private LiveData<Result<List<Product>>>  featuredProducts;
    private LiveData<Result<List<Product>>>  newProducts;
    private LiveData<Result<List<Product>>>  bestSellers;

    // Lazy load — chỉ gọi Firestore khi cần
    public LiveData<Result<List<Banner>>> getBanners() {
        if (banners == null) banners = repo.getBanners();
        return banners;
    }

    public LiveData<Result<List<Category>>> getCategories() {
        if (categories == null) categories = repo.getCategories();
        return categories;
    }

    public LiveData<Result<List<Product>>> getFeaturedProducts() {
        if (featuredProducts == null) featuredProducts = repo.getFeaturedProducts();
        return featuredProducts;
    }

    public LiveData<Result<List<Product>>> getNewProducts() {
        if (newProducts == null) newProducts = repo.getNewProducts();
        return newProducts;
    }

    public LiveData<Result<List<Product>>> getBestSellers() {
        if (bestSellers == null) bestSellers = repo.getBestSellers();
        return bestSellers;
    }

    // Tìm kiếm
    public LiveData<Result<List<Product>>> search(String keyword) {
        return repo.searchProducts(keyword);
    }
}
