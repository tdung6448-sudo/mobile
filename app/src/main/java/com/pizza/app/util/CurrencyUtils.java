package com.pizza.app.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Định dạng tiền tệ VND
 */
public final class CurrencyUtils {

    private static final DecimalFormat FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        FORMAT = new DecimalFormat("#,###", symbols);
    }

    private CurrencyUtils() {}

    /** Định dạng: 150.000đ */
    public static String format(long amount) {
        return FORMAT.format(amount) + "đ";
    }

    /** Định dạng: 150.000 (không có ký hiệu) */
    public static String formatNoSuffix(long amount) {
        return FORMAT.format(amount);
    }

    /** Định dạng: +5.000đ hoặc -20.000đ */
    public static String formatWithSign(long amount) {
        return (amount >= 0 ? "+" : "") + format(amount);
    }
}
