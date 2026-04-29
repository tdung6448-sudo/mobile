package com.pizza.app.repository;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.pizza.app.model.User;
import com.pizza.app.util.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Repository xử lý toàn bộ logic xác thực — giao tiếp với Firebase Auth + Firestore
 */
public class AuthRepository {

    private final FirebaseAuth      auth;
    private final FirebaseFirestore db;
    private final FirebaseStorage   storage;

    public AuthRepository() {
        auth    = FirebaseAuth.getInstance();
        db      = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    // ── Trạng thái hiện tại ────────────────────────────────────────

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    // ── Đăng ký bằng Email/Password ─────────────────────────────────

    public LiveData<Result<User>> registerWithEmail(String name, String email, String password) {
        MutableLiveData<Result<User>> result = new MutableLiveData<>();
        result.setValue(Result.loading());

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                FirebaseUser fbUser = authResult.getUser();
                if (fbUser == null) {
                    result.setValue(Result.error("Lỗi tạo tài khoản"));
                    return;
                }
                // Gửi email xác thực
                fbUser.sendEmailVerification();
                // Tạo document user trong Firestore
                User user = new User(fbUser.getUid(), name, email, User.ROLE_CUSTOMER);
                saveUserToFirestore(user, result);
            })
            .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));

        return result;
    }

    // ── Đăng nhập bằng Email/Password ─────────────────────────────

    public LiveData<Result<User>> loginWithEmail(String email, String password) {
        MutableLiveData<Result<User>> result = new MutableLiveData<>();
        result.setValue(Result.loading());

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                FirebaseUser fbUser = authResult.getUser();
                if (fbUser == null) {
                    result.setValue(Result.error("Đăng nhập thất bại"));
                    return;
                }
                fetchAndUpdateUser(fbUser.getUid(), result);
            })
            .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));

        return result;
    }

    // ── Đăng nhập Google ──────────────────────────────────────────

    public LiveData<Result<User>> loginWithGoogle(String idToken) {
        MutableLiveData<Result<User>> result = new MutableLiveData<>();
        result.setValue(Result.loading());

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
            .addOnSuccessListener(authResult -> {
                FirebaseUser fbUser = authResult.getUser();
                if (fbUser == null) {
                    result.setValue(Result.error("Lỗi Google Sign-In"));
                    return;
                }
                // Nếu user mới — tạo document trong Firestore
                boolean isNew = authResult.getAdditionalUserInfo() != null
                        && authResult.getAdditionalUserInfo().isNewUser();
                if (isNew) {
                    User user = new User(fbUser.getUid(),
                            fbUser.getDisplayName(), fbUser.getEmail(), User.ROLE_CUSTOMER);
                    if (fbUser.getPhotoUrl() != null) {
                        user.setAvatar(fbUser.getPhotoUrl().toString());
                    }
                    user.setEmailVerified(true); // Google đã xác thực
                    saveUserToFirestore(user, result);
                } else {
                    fetchAndUpdateUser(fbUser.getUid(), result);
                }
            })
            .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));

        return result;
    }

    // ── Đăng nhập SĐT (OTP) ──────────────────────────────────────

    public LiveData<Result<User>> loginWithPhoneCredential(PhoneAuthCredential credential,
                                                            String name) {
        MutableLiveData<Result<User>> result = new MutableLiveData<>();
        result.setValue(Result.loading());

        auth.signInWithCredential(credential)
            .addOnSuccessListener(authResult -> {
                FirebaseUser fbUser = authResult.getUser();
                if (fbUser == null) {
                    result.setValue(Result.error("Lỗi xác thực OTP"));
                    return;
                }
                boolean isNew = authResult.getAdditionalUserInfo() != null
                        && authResult.getAdditionalUserInfo().isNewUser();
                if (isNew) {
                    User user = new User(fbUser.getUid(), name,
                            fbUser.getEmail() != null ? fbUser.getEmail() : "",
                            User.ROLE_CUSTOMER);
                    user.setPhone(fbUser.getPhoneNumber());
                    saveUserToFirestore(user, result);
                } else {
                    fetchAndUpdateUser(fbUser.getUid(), result);
                }
            })
            .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));

        return result;
    }

    // ── Quên mật khẩu ─────────────────────────────────────────────

    public LiveData<Result<Void>> sendPasswordResetEmail(String email) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener(v -> result.setValue(Result.success(null)))
            .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));
        return result;
    }

    // ── Đổi mật khẩu ──────────────────────────────────────────────

    public LiveData<Result<Void>> changePassword(String currentPassword, String newPassword) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            result.setValue(Result.error("Chưa đăng nhập"));
            return result;
        }

        // Re-authenticate trước khi đổi mật khẩu (yêu cầu của Firebase)
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
        user.reauthenticate(credential)
            .addOnSuccessListener(v -> user.updatePassword(newPassword)
                    .addOnSuccessListener(v2 -> result.setValue(Result.success(null)))
                    .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage()))))
            .addOnFailureListener(e -> result.setValue(Result.error("Mật khẩu hiện tại không đúng")));

        return result;
    }

    // ── Upload avatar ──────────────────────────────────────────────

    public LiveData<Result<String>> uploadAvatar(String uid, Uri imageUri) {
        MutableLiveData<Result<String>> result = new MutableLiveData<>();
        result.setValue(Result.loading());

        String path = Constants.STORAGE_AVATARS + "/" + uid + "_" + UUID.randomUUID() + ".jpg";
        storage.getReference(path)
               .putFile(imageUri)
               .continueWithTask(task -> {
                   if (!task.isSuccessful()) throw task.getException();
                   return storage.getReference(path).getDownloadUrl();
               })
               .addOnSuccessListener(uri -> {
                   String url = uri.toString();
                   // Cập nhật avatar URL vào Firestore
                   db.collection(Constants.COL_USERS).document(uid)
                     .update("avatar", url)
                     .addOnSuccessListener(v -> result.setValue(Result.success(url)))
                     .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));
               })
               .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));

        return result;
    }

    // ── Đăng xuất ─────────────────────────────────────────────────

    public void logout() {
        auth.signOut();
    }

    // ── Xóa tài khoản ─────────────────────────────────────────────

    public LiveData<Result<Void>> deleteAccount() {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            result.setValue(Result.error("Chưa đăng nhập"));
            return result;
        }
        String uid = user.getUid();
        // Đánh dấu xóa trong Firestore (soft delete), sau đó xóa Auth
        db.collection(Constants.COL_USERS).document(uid)
          .update("isDeleted", true)
          .addOnSuccessListener(v -> user.delete()
                  .addOnSuccessListener(v2 -> result.setValue(Result.success(null)))
                  .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage()))))
          .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));

        return result;
    }

    // ── Private helpers ────────────────────────────────────────────

    /** Lưu User mới vào Firestore và cập nhật FCM token */
    private void saveUserToFirestore(User user, MutableLiveData<Result<User>> result) {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
            user.setFcmToken(token);
            db.collection(Constants.COL_USERS)
              .document(user.getUid())
              .set(user)
              .addOnSuccessListener(v -> result.setValue(Result.success(user)))
              .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));
        });
    }

    /** Đọc User từ Firestore và cập nhật FCM token */
    private void fetchAndUpdateUser(String uid, MutableLiveData<Result<User>> result) {
        db.collection(Constants.COL_USERS).document(uid)
          .get()
          .addOnSuccessListener(doc -> {
              if (!doc.exists()) {
                  result.setValue(Result.error("Tài khoản không tồn tại"));
                  return;
              }
              User user = doc.toObject(User.class);
              if (user == null) {
                  result.setValue(Result.error("Lỗi dữ liệu tài khoản"));
                  return;
              }
              if (user.isBlocked()) {
                  auth.signOut();
                  result.setValue(Result.error("Tài khoản đã bị khoá"));
                  return;
              }
              // Cập nhật FCM token mới nhất
              FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
                  Map<String, Object> update = new HashMap<>();
                  update.put("fcmToken", token);
                  db.collection(Constants.COL_USERS).document(uid).update(update);
                  user.setFcmToken(token);
                  result.setValue(Result.success(user));
              });
          })
          .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));
    }

    // ── Result wrapper ─────────────────────────────────────────────

    public static class Result<T> {
        public enum Status { LOADING, SUCCESS, ERROR }

        public final Status status;
        public final T      data;
        public final String message;

        private Result(Status status, T data, String message) {
            this.status  = status;
            this.data    = data;
            this.message = message;
        }

        public static <T> Result<T> loading()              { return new Result<>(Status.LOADING, null, null); }
        public static <T> Result<T> success(T data)        { return new Result<>(Status.SUCCESS, data, null); }
        public static <T> Result<T> error(String message)  { return new Result<>(Status.ERROR, null, message); }

        public boolean isLoading() { return status == Status.LOADING; }
        public boolean isSuccess() { return status == Status.SUCCESS; }
        public boolean isError()   { return status == Status.ERROR; }
    }
}
