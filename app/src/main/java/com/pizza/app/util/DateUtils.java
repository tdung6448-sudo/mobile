package com.pizza.app.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Tiện ích xử lý ngày giờ
 */
public final class DateUtils {

    private static final SimpleDateFormat FORMAT_FULL    = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("vi"));
    private static final SimpleDateFormat FORMAT_DATE    = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi"));
    private static final SimpleDateFormat FORMAT_TIME    = new SimpleDateFormat("HH:mm", new Locale("vi"));
    private static final SimpleDateFormat FORMAT_MONTH   = new SimpleDateFormat("MM/yyyy", new Locale("vi"));

    private DateUtils() {}

    public static String formatFull(Date date) {
        return date != null ? FORMAT_FULL.format(date) : "";
    }

    public static String formatDate(Date date) {
        return date != null ? FORMAT_DATE.format(date) : "";
    }

    public static String formatTime(Date date) {
        return date != null ? FORMAT_TIME.format(date) : "";
    }

    public static String formatMonth(Date date) {
        return date != null ? FORMAT_MONTH.format(date) : "";
    }

    /** Hiển thị thời gian tương đối: "vừa xong", "5 phút trước", "2 giờ trước" */
    public static String timeAgo(Date date) {
        if (date == null) return "";
        long diffMs = System.currentTimeMillis() - date.getTime();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs);
        long hours   = TimeUnit.MILLISECONDS.toHours(diffMs);
        long days    = TimeUnit.MILLISECONDS.toDays(diffMs);

        if (minutes < 1)   return "vừa xong";
        if (minutes < 60)  return minutes + " phút trước";
        if (hours   < 24)  return hours + " giờ trước";
        if (days    < 7)   return days + " ngày trước";
        return formatDate(date);
    }
}
