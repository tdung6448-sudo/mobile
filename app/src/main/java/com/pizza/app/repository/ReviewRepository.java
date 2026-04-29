package com.pizza.app.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.pizza.app.model.Review;
import com.pizza.app.repository.AuthRepository.Result;
import com.pizza.app.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository đánh giá sản phẩm
 */
public class ReviewRepository {

    private final FirebaseFirestore db;

    public ReviewRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /** Lấy danh sách review của sản phẩm */
    public LiveData<Result<List<Review>>> getReviews(String productId) {
        MutableLiveData<Result<List<Review>>> result = new MutableLiveData<>();

        db.collection(Constants.COL_REVIEWS)
          .whereEqualTo("productId", productId)
          .orderBy("createdAt", Query.Direction.DESCENDING)
          .limit(Constants.PAGE_SIZE_REVIEWS)
          .addSnapshotListener((snap, e) -> {
              if (e != null) { result.setValue(Result.error(e.getMessage())); return; }
              List<Review> list = new ArrayList<>();
              if (snap != null) {
                  for (DocumentSnapshot doc : snap.getDocuments()) {
                      Review r = doc.toObject(Review.class);
                      if (r != null) list.add(r);
                  }
              }
              result.setValue(Result.success(list));
          });

        return result;
    }

    /** Gửi đánh giá — cập nhật ratingAvg và ratingCount trong product */
    public LiveData<Result<Void>> submitReview(Review review) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();
        result.setValue(Result.loading());

        db.collection(Constants.COL_REVIEWS)
          .add(review)
          .addOnSuccessListener(ref -> {
              // Cập nhật điểm TB trong product (dùng transaction để chính xác)
              updateProductRating(review.getProductId(), review.getRating(), result);
          })
          .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));

        return result;
    }

    /** Cập nhật ratingAvg và ratingCount bằng Firestore transaction */
    private void updateProductRating(String productId, float newRating,
                                      MutableLiveData<Result<Void>> result) {
        db.runTransaction(transaction -> {
            DocumentSnapshot productDoc = transaction.get(
                    db.collection(Constants.COL_PRODUCTS).document(productId));

            float currentAvg   = productDoc.contains("ratingAvg")
                    ? ((Double) productDoc.get("ratingAvg")).floatValue() : 0f;
            long  currentCount = productDoc.contains("ratingCount")
                    ? (Long) productDoc.get("ratingCount") : 0L;

            long  newCount = currentCount + 1;
            float newAvg   = ((currentAvg * currentCount) + newRating) / newCount;

            Map<String, Object> update = new HashMap<>();
            update.put("ratingAvg",   newAvg);
            update.put("ratingCount", newCount);
            transaction.update(db.collection(Constants.COL_PRODUCTS).document(productId), update);
            return null;
        })
        .addOnSuccessListener(v -> result.setValue(Result.success(null)))
        .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));
    }
}
