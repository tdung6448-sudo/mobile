package com.pizza.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pizza.app.model.CartItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper quản lý SharedPreferences — giỏ hàng, wishlist, cài đặt
 */
public class SharedPrefsHelper {

    private final SharedPreferences prefs;
    private final Gson gson;

    public SharedPrefsHelper(Context context) {
        prefs = context.getSharedPreferences(Constants.PREF_FILE, Context.MODE_PRIVATE);
        gson  = new Gson();
    }

    // ── Cart ────────────────────────────────────────────────────────

    public List<CartItem> getCart() {
        String json = prefs.getString(Constants.PREF_CART, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<CartItem>>() {}.getType();
        List<CartItem> cart = gson.fromJson(json, type);
        return cart != null ? cart : new ArrayList<>();
    }

    public void saveCart(List<CartItem> cart) {
        prefs.edit().putString(Constants.PREF_CART, gson.toJson(cart)).apply();
    }

    public void clearCart() {
        prefs.edit().remove(Constants.PREF_CART).apply();
    }

    // ── Wishlist ────────────────────────────────────────────────────

    public List<String> getWishlist() {
        String json = prefs.getString(Constants.PREF_WISHLIST, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<String>>() {}.getType();
        List<String> wishlist = gson.fromJson(json, type);
        return wishlist != null ? wishlist : new ArrayList<>();
    }

    public void saveWishlist(List<String> productIds) {
        prefs.edit().putString(Constants.PREF_WISHLIST, gson.toJson(productIds)).apply();
    }

    public boolean isInWishlist(String productId) {
        return getWishlist().contains(productId);
    }

    public void toggleWishlist(String productId) {
        List<String> wishlist = getWishlist();
        if (wishlist.contains(productId)) {
            wishlist.remove(productId);
        } else {
            wishlist.add(productId);
        }
        saveWishlist(wishlist);
    }

    // ── Settings ────────────────────────────────────────────────────

    public boolean isRememberMe()           { return prefs.getBoolean(Constants.PREF_REMEMBER_ME, false); }
    public void    setRememberMe(boolean v) { prefs.edit().putBoolean(Constants.PREF_REMEMBER_ME, v).apply(); }

    public String  getLanguage()            { return prefs.getString(Constants.PREF_LANGUAGE, "vi"); }
    public void    setLanguage(String lang) { prefs.edit().putString(Constants.PREF_LANGUAGE, lang).apply(); }

    public boolean isDarkMode()             { return prefs.getBoolean(Constants.PREF_DARK_MODE, false); }
    public void    setDarkMode(boolean on)  { prefs.edit().putBoolean(Constants.PREF_DARK_MODE, on).apply(); }

    public String  getFcmToken()            { return prefs.getString(Constants.PREF_FCM_TOKEN, ""); }
    public void    setFcmToken(String tok)  { prefs.edit().putString(Constants.PREF_FCM_TOKEN, tok).apply(); }

    /** Xóa tất cả dữ liệu khi đăng xuất */
    public void clearAll() {
        prefs.edit()
             .remove(Constants.PREF_CART)
             .remove(Constants.PREF_WISHLIST)
             .remove(Constants.PREF_REMEMBER_ME)
             .apply();
    }
}
