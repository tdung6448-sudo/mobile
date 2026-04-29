package com.pizza.app.view.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pizza.app.R;
import com.pizza.app.model.User;
import com.pizza.app.util.Constants;
import com.pizza.app.util.SharedPrefsHelper;

/**
 * Màn hình splash — kiểm tra đăng nhập, phân quyền role, chuyển màn hình
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 1800; // ms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Áp dụng dark mode trước khi setContentView
        SharedPrefsHelper prefs = new SharedPrefsHelper(this);
        if (prefs.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(this::checkAuthState, SPLASH_DELAY);
    }

    private void checkAuthState() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Chưa đăng nhập → màn hình Login
            goTo(LoginActivity.class);
            return;
        }

        // Đã đăng nhập → kiểm tra role trong Firestore
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection(Constants.COL_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        // Document không tồn tại → đăng xuất để tránh lỗi
                        FirebaseAuth.getInstance().signOut();
                        goTo(LoginActivity.class);
                        return;
                    }
                    User user = doc.toObject(User.class);
                    if (user == null) {
                        goTo(LoginActivity.class);
                        return;
                    }
                    if (user.isBlocked()) {
                        FirebaseAuth.getInstance().signOut();
                        goTo(LoginActivity.class);
                        return;
                    }
                    // Phân quyền theo role
                    if (user.isAdmin()) {
                        goTo(AdminMainActivity.class);
                    } else {
                        goTo(MainActivity.class);
                    }
                })
                .addOnFailureListener(e -> goTo(LoginActivity.class));
    }

    private void goTo(Class<?> cls) {
        startActivity(new Intent(this, cls));
        finish();
    }
}
