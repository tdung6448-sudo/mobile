# ── Firebase ──────────────────────────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ── Firestore & Realtime DB model classes ────────────────────────────────────
# Giữ tất cả class trong package model để Firestore deserialize đúng
-keep class com.pizza.app.model.** { *; }

# ── Retrofit & OkHttp ─────────────────────────────────────────────────────────
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# ── Gson ─────────────────────────────────────────────────────────────────────
-keepattributes EnclosingMethod
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }

# ── Glide ────────────────────────────────────────────────────────────────────
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# ── Lottie ───────────────────────────────────────────────────────────────────
-keep class com.airbnb.lottie.** { *; }

# ── Stripe ───────────────────────────────────────────────────────────────────
-keep class com.stripe.android.** { *; }
-dontwarn com.stripe.android.**

# ── MPAndroidChart ────────────────────────────────────────────────────────────
-keep class com.github.mikephil.charting.** { *; }

# ── Google Maps & Places ─────────────────────────────────────────────────────
-keep class com.google.android.libraries.places.** { *; }

# ── ViewBinding — không cần giữ vì generate lúc compile ─────────────────────
# -keep class com.pizza.app.databinding.** { *; }

# ── Enum classes (cần giữ khi dùng Switch/When) ──────────────────────────────
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ── Parcelable ────────────────────────────────────────────────────────────────
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ── Serializable ─────────────────────────────────────────────────────────────
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ── Xóa log trong bản release ────────────────────────────────────────────────
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
