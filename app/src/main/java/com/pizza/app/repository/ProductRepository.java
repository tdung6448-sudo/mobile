package com.pizza.app.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.pizza.app.model.Banner;
import com.pizza.app.model.Category;
import com.pizza.app.model.Product;
import com.pizza.app.repository.AuthRepository.Result;
import com.pizza.app.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository sản phẩm — tải danh sách, tìm kiếm, CRUD (admin)
 */
public class ProductRepository {

    private final FirebaseFirestore db;

    // Cache query cursor cho phân trang
    private DocumentSnapshot lastProductSnapshot;

    public ProductRepository() {
        db = FirebaseFirestore.getInstance();
    }

    // ── Danh mục ───────────────────────────────────────────────────

    public LiveData<Result<List<Category>>> getCategories() {
        MutableLiveData<Result<List<Category>>> result = new MutableLiveData<>();

        db.collection(Constants.COL_CATEGORIES)
          .whereEqualTo("isActive", true)
          .orderBy("order")
          .addSnapshotListener((snap, e) -> {
              if (e != null) {
                  result.setValue(Result.error(e.getMessage()));
                  return;
              }
              List<Category> list = new ArrayList<>();
              if (snap != null) {
                  for (DocumentSnapshot doc : snap.getDocuments()) {
                      Category cat = doc.toObject(Category.class);
                      if (cat != null) list.add(cat);
                  }
              }
              result.setValue(Result.success(list));
          });

        return result;
    }

    // ── Banner ─────────────────────────────────────────────────────

    public LiveData<Result<List<Banner>>> getBanners() {
        MutableLiveData<Result<List<Banner>>> result = new MutableLiveData<>();

        db.collection(Constants.COL_BANNERS)
          .whereEqualTo("isActive", true)
          .orderBy("order")
          .get()
          .addOnSuccessListener(snap -> {
              List<Banner> list = new ArrayList<>();
              for (DocumentSnapshot doc : snap.getDocuments()) {
                  Banner b = doc.toObject(Banner.class);
                  if (b != null) list.add(b);
              }
              result.setValue(Result.success(list));
          })
          .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));

        return result;
    }

    // ── Sản phẩm nổi bật trên trang chủ ───────────────────────────

    public LiveData<Result<List<Product>>> getFeaturedProducts() {
        MutableLiveData<Result<List<Product>>> result = new MutableLiveData<>();

        db.collection(Constants.COL_PRODUCTS)
          .whereEqualTo("isAvailable", true)
          .whereEqualTo("isFeatured", true)
          .orderBy("soldCount", Query.Direction.DESCENDING)
          .limit(8)
          .addSnapshotListener((snap, e) -> {
              if (e != null) { result.setValue(Result.error(e.getMessage())); return; }
              result.setValue(Result.success(toProductList(snap)));
          });

        return result;
    }

    /** Sản phẩm mới ra mắt */
    public LiveData<Result<List<Product>>> getNewProducts() {
        MutableLiveData<Result<List<Product>>> result = new MutableLiveData<>();

        db.collection(Constants.COL_PRODUCTS)
          .whereEqualTo("isAvailable", true)
          .whereEqualTo("isNew", true)
          .orderBy("createdAt", Query.Direction.DESCENDING)
          .limit(6)
          .get()
          .addOnSuccessListener(snap -> result.setValue(Result.success(toProductList(snap))))
          .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));

        return result;
    }

    /** Sản phẩm best seller */
    public LiveData<Result<List<Product>>> getBestSellers() {
        MutableLiveData<Result<List<Product>>> result = new MutableLiveData<>();

        db.collection(Constants.COL_PRODUCTS)
          .whereEqualTo("isAvailable", true)
          .whereEqualTo("isBestSeller", true)
          .orderBy("soldCount", Query.Direction.DESCENDING)
          .limit(10)
          .get()
          .addOnSuccessListener(snap -> result.setValue(Result.success(toProductList(snap))))
          .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));

        return result;
    }

    // ── Sản phẩm theo danh mục — có phân trang ────────────────────

    public LiveData<Result<List<Product>>> getProductsByCategory(String categoryId, boolean loadMore) {
        MutableLiveData<Result<List<Product>>> result = new MutableLiveData<>();

        if (!loadMore) lastProductSnapshot = null;

        Query query = db.collection(Constants.COL_PRODUCTS)
                        .whereEqualTo("categoryId", categoryId)
                        .whereEqualTo("isAvailable", true)
                        .orderBy("soldCount", Query.Direction.DESCENDING)
                        .limit(Constants.PAGE_SIZE_PRODUCTS);

        if (lastProductSnapshot != null) {
            query = query.startAfter(lastProductSnapshot);
        }

        query.get()
             .addOnSuccessListener(snap -> {
                 if (!snap.isEmpty()) {
                     lastProductSnapshot = snap.getDocuments()
                             .get(snap.size() - 1); // Lưu cursor cho trang tiếp
                 }
                 result.setValue(Result.success(toProductList(snap)));
             })
             .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));

        return result;
    }

    // ── Chi tiết sản phẩm ─────────────────────────────────────────

    public LiveData<Result<Product>> getProductById(String productId) {
        MutableLiveData<Result<Product>> result = new MutableLiveData<>();

        db.collection(Constants.COL_PRODUCTS)
          .document(productId)
          .addSnapshotListener((doc, e) -> {
              if (e != null) { result.setValue(Result.error(e.getMessage())); return; }
              if (doc != null && doc.exists()) {
                  result.setValue(Result.success(doc.toObject(Product.class)));
              } else {
                  result.setValue(Result.error("Sản phẩm không tồn tại"));
              }
          });

        return result;
    }

    // ── Tìm kiếm ──────────────────────────────────────────────────

    /**
     * Tìm kiếm bằng prefix name — Firestore hỗ trợ range query trên chuỗi
     * Giải pháp đơn giản: query startAt / endAt với ký tự unicode cao nhất
     */
    public LiveData<Result<List<Product>>> searchProducts(String keyword) {
        MutableLiveData<Result<List<Product>>> result = new MutableLiveData<>();

        if (keyword == null || keyword.trim().isEmpty()) {
            result.setValue(Result.success(new ArrayList<>()));
            return result;
        }

        String lower  = keyword.trim().toLowerCase();
        String upper  = lower + ""; // Ký tự unicode lớn nhất để endAt

        db.collection(Constants.COL_PRODUCTS)
          .whereEqualTo("isAvailable", true)
          .orderBy("name")
          .startAt(lower)
          .endAt(upper)
          .limit(20)
          .get()
          .addOnSuccessListener(snap -> result.setValue(Result.success(toProductList(snap))))
          .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));

        return result;
    }

    // ── Admin: CRUD ────────────────────────────────────────────────

    public LiveData<Result<String>> saveProduct(Product product) {
        MutableLiveData<Result<String>> result = new MutableLiveData<>();
        result.setValue(Result.loading());

        if (product.getId() == null || product.getId().isEmpty()) {
            // Thêm mới
            db.collection(Constants.COL_PRODUCTS)
              .add(product)
              .addOnSuccessListener(ref -> result.setValue(Result.success(ref.getId())))
              .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));
        } else {
            // Cập nhật
            db.collection(Constants.COL_PRODUCTS)
              .document(product.getId())
              .set(product)
              .addOnSuccessListener(v -> result.setValue(Result.success(product.getId())))
              .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));
        }
        return result;
    }

    public LiveData<Result<Void>> toggleProductAvailability(String productId, boolean isAvailable) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();
        Map<String, Object> update = new HashMap<>();
        update.put("isAvailable", isAvailable);

        db.collection(Constants.COL_PRODUCTS).document(productId)
          .update(update)
          .addOnSuccessListener(v -> result.setValue(Result.success(null)))
          .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));

        return result;
    }

    public LiveData<Result<Void>> deleteProduct(String productId) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();
        db.collection(Constants.COL_PRODUCTS).document(productId)
          .delete()
          .addOnSuccessListener(v -> result.setValue(Result.success(null)))
          .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));
        return result;
    }

    // ── Admin: lấy toàn bộ sản phẩm (không filter isAvailable) ───

    public LiveData<Result<List<Product>>> getAllProductsForAdmin() {
        MutableLiveData<Result<List<Product>>> result = new MutableLiveData<>();

        db.collection(Constants.COL_PRODUCTS)
          .orderBy("createdAt", Query.Direction.DESCENDING)
          .addSnapshotListener((snap, e) -> {
              if (e != null) { result.setValue(Result.error(e.getMessage())); return; }
              result.setValue(Result.success(toProductList(snap)));
          });

        return result;
    }

    // ── Private helper ─────────────────────────────────────────────

    private List<Product> toProductList(QuerySnapshot snap) {
        List<Product> list = new ArrayList<>();
        if (snap == null) return list;
        for (DocumentSnapshot doc : snap.getDocuments()) {
            Product p = doc.toObject(Product.class);
            if (p != null) list.add(p);
        }
        return list;
    }
}
