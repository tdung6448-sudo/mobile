package com.pizza.app.util;

import android.util.Patterns;

/**
 * Kiểm tra dữ liệu đầu vào người dùng
 */
public final class ValidationUtils {

    private ValidationUtils() {}

    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        // Số điện thoại Việt Nam: 10 chữ số, bắt đầu bằng 0
        return phone.trim().matches("^0[0-9]{9}$");
    }

    public static boolean isValidPassword(String password) {
        // Tối thiểu 8 ký tự, có chữ và số
        return password != null && password.length() >= 8
                && password.matches(".*[a-zA-Z].*")
                && password.matches(".*[0-9].*");
    }

    public static boolean isPasswordMatch(String password, String confirm) {
        return password != null && password.equals(confirm);
    }

    public static boolean isNotEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
