package com.pizza.app.viewmodel;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.PhoneAuthCredential;
import com.pizza.app.model.User;
import com.pizza.app.repository.AuthRepository;
import com.pizza.app.repository.AuthRepository.Result;

/**
 * ViewModel xác thực — tách logic khỏi Activity/Fragment
 */
public class AuthViewModel extends ViewModel {

    private final AuthRepository repo = new AuthRepository();

    // ── Auth state ─────────────────────────────────────────────────

    public boolean isLoggedIn() {
        return repo.isLoggedIn();
    }

    public com.google.firebase.auth.FirebaseUser getCurrentFirebaseUser() {
        return repo.getCurrentUser();
    }

    // ── Đăng ký / Đăng nhập ───────────────────────────────────────

    public LiveData<Result<User>> register(String name, String email, String password) {
        return repo.registerWithEmail(name, email, password);
    }

    public LiveData<Result<User>> loginWithEmail(String email, String password) {
        return repo.loginWithEmail(email, password);
    }

    public LiveData<Result<User>> loginWithGoogle(String idToken) {
        return repo.loginWithGoogle(idToken);
    }

    public LiveData<Result<User>> loginWithPhone(PhoneAuthCredential credential, String name) {
        return repo.loginWithPhoneCredential(credential, name);
    }

    public LiveData<Result<Void>> sendPasswordReset(String email) {
        return repo.sendPasswordResetEmail(email);
    }

    public LiveData<Result<Void>> changePassword(String currentPw, String newPw) {
        return repo.changePassword(currentPw, newPw);
    }

    public LiveData<Result<String>> uploadAvatar(String uid, Uri imageUri) {
        return repo.uploadAvatar(uid, imageUri);
    }

    public void logout() {
        repo.logout();
    }

    public LiveData<Result<Void>> deleteAccount() {
        return repo.deleteAccount();
    }
}
