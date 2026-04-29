package com.pizza.app.util;

/**
 * Hằng số dùng chung toàn app — tên collection, key intent, v.v.
 */
public final class Constants {

    private Constants() {} // Ngăn khởi tạo

    // ── Firestore Collections ──────────────────────────────────────
    public static final String COL_USERS      = "users";
    public static final String COL_PRODUCTS   = "products";
    public static final String COL_CATEGORIES = "categories";
    public static final String COL_ORDERS     = "orders";
    public static final String COL_VOUCHERS   = "vouchers";
    public static final String COL_REVIEWS    = "reviews";
    public static final String COL_BANNERS    = "banners";
    public static final String COL_CHATS      = "chats";
    public static final String COL_MESSAGES   = "messages";

    // ── Realtime Database paths ────────────────────────────────────
    public static final String DB_SHIPPER_LOCATION = "shipperLocations";
    public static final String DB_ONLINE_STATUS    = "onlineStatus";

    // ── Firebase Storage paths ─────────────────────────────────────
    public static final String STORAGE_AVATARS   = "avatars";
    public static final String STORAGE_PRODUCTS  = "products";
    public static final String STORAGE_REVIEWS   = "reviews";
    public static final String STORAGE_BANNERS   = "banners";
    public static final String STORAGE_CHAT      = "chat_images";

    // ── FCM Topics ─────────────────────────────────────────────────
    public static final String TOPIC_PROMOTIONS = "promotions";
    public static final String TOPIC_ALL_USERS  = "all_users";

    // ── Intent Extra Keys ─────────────────────────────────────────
    public static final String EXTRA_PRODUCT_ID  = "extra_product_id";
    public static final String EXTRA_ORDER_ID    = "extra_order_id";
    public static final String EXTRA_CATEGORY_ID = "extra_category_id";
    public static final String EXTRA_USER_ID     = "extra_user_id";
    public static final String EXTRA_CHAT_ID     = "extra_chat_id";
    public static final String EXTRA_LAT         = "extra_lat";
    public static final String EXTRA_LNG         = "extra_lng";
    public static final String EXTRA_ADDRESS     = "extra_address";

    // ── SharedPrefs Keys ──────────────────────────────────────────
    public static final String PREF_FILE         = "pizza_prefs";
    public static final String PREF_CART         = "pref_cart";
    public static final String PREF_WISHLIST     = "pref_wishlist";
    public static final String PREF_REMEMBER_ME  = "pref_remember_me";
    public static final String PREF_LANGUAGE     = "pref_language";
    public static final String PREF_DARK_MODE    = "pref_dark_mode";
    public static final String PREF_FCM_TOKEN    = "pref_fcm_token";

    // ── Phí vận chuyển ────────────────────────────────────────────
    public static final long   SHIPPING_BASE_FEE     = 15_000L; // 15.000đ
    public static final long   SHIPPING_EXTRA_PER_KM = 5_000L;  // 5.000đ/km
    public static final double SHIPPING_FREE_RADIUS  = 3.0;     // Miễn phí trong 3km

    // ── Giới hạn phân trang ───────────────────────────────────────
    public static final int PAGE_SIZE_PRODUCTS = 12;
    public static final int PAGE_SIZE_ORDERS   = 10;
    public static final int PAGE_SIZE_REVIEWS  = 8;

    // ── Request codes ─────────────────────────────────────────────
    public static final int REQUEST_MAP_PICKER    = 1001;
    public static final int REQUEST_IMAGE_PICKER  = 1002;
    public static final int REQUEST_CAMERA        = 1003;
    public static final int REQUEST_PAYMENT_MOMO  = 2001;
    public static final int REQUEST_PAYMENT_VNPAY = 2002;
}
