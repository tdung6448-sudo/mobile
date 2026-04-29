package com.pizza.app.repository;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pizza.app.BuildConfig;
import com.pizza.app.repository.AuthRepository.Result;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Repository xử lý thanh toán — MoMo, VNPay deep link / URL
 *
 * QUAN TRỌNG: Trong production, HMAC signing PHẢI thực hiện ở server-side (Cloud Functions)
 * để bảo vệ secret key. Code này dùng cho sandbox/demo.
 */
public class PaymentRepository {

    private static final String VNPAY_ENDPOINT_SANDBOX =
            "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

    // ── MoMo ──────────────────────────────────────────────────────

    /**
     * Tạo deep link MoMo để mở app MoMo thanh toán.
     * Production: gọi server của bạn để lấy payUrl từ MoMo API (server ký HMAC).
     */
    public LiveData<Result<String>> createMoMoPaymentUrl(String orderId, long amount,
                                                           String orderInfo) {
        MutableLiveData<Result<String>> result = new MutableLiveData<>();
        result.setValue(Result.loading());

        String partnerCode = BuildConfig.MOMO_PARTNER_CODE;
        String accessKey   = BuildConfig.MOMO_ACCESS_KEY;
        String secretKey   = BuildConfig.MOMO_SECRET_KEY;
        String requestId   = orderId + "_" + System.currentTimeMillis();
        String redirectUrl = "momo://app";
        String ipnUrl      = "https://your-backend.com/momo/ipn";
        String requestType = "payWithMethod";
        String extraData   = "";

        String rawSignature = "accessKey=" + accessKey
                + "&amount=" + amount
                + "&extraData=" + extraData
                + "&ipnUrl=" + ipnUrl
                + "&orderId=" + orderId
                + "&orderInfo=" + orderInfo
                + "&partnerCode=" + partnerCode
                + "&redirectUrl=" + redirectUrl
                + "&requestId=" + requestId
                + "&requestType=" + requestType;

        try {
            String signature = hmacSha256(rawSignature, secretKey);
            String deepLink = "momo://app?action=payment"
                    + "&partnerCode=" + partnerCode
                    + "&requestId=" + requestId
                    + "&orderId=" + orderId
                    + "&amount=" + amount
                    + "&orderInfo=" + URLEncoder.encode(orderInfo, "UTF-8")
                    + "&signature=" + signature;
            result.setValue(Result.success(deepLink));
        } catch (Exception e) {
            result.setValue(Result.error("Lỗi tạo link MoMo: " + e.getMessage()));
        }

        return result;
    }

    /** Mở app MoMo bằng Intent */
    public static void openMoMo(Context context, String deepLink) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(deepLink));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            // App MoMo chưa cài — mở CH Play
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=com.mservice.momotransfer")));
        }
    }

    // ── VNPay ─────────────────────────────────────────────────────

    /** Tạo URL thanh toán VNPay */
    public LiveData<Result<String>> createVNPayUrl(String orderId, long amount,
                                                     String orderInfo, String clientIp) {
        MutableLiveData<Result<String>> result = new MutableLiveData<>();
        result.setValue(Result.loading());

        try {
            TreeMap<String, String> params = new TreeMap<>();
            params.put("vnp_Version",    "2.1.0");
            params.put("vnp_Command",    "pay");
            params.put("vnp_TmnCode",    BuildConfig.VNPAY_TMN_CODE);
            params.put("vnp_Amount",     String.valueOf(amount * 100));
            params.put("vnp_CurrCode",   "VND");
            params.put("vnp_TxnRef",     orderId);
            params.put("vnp_OrderInfo",  orderInfo);
            params.put("vnp_OrderType",  "food");
            params.put("vnp_Locale",     "vn");
            params.put("vnp_ReturnUrl",  "vnpay://app");
            params.put("vnp_IpAddr",     clientIp);
            params.put("vnp_CreateDate",
                    new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date()));

            StringBuilder query    = new StringBuilder();
            StringBuilder hashData = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                query.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                     .append("=")
                     .append(URLEncoder.encode(entry.getValue(), "UTF-8"))
                     .append("&");
                hashData.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            String queryStr   = query.substring(0, query.length() - 1);
            String hashString = hashData.substring(0, hashData.length() - 1);
            String secureHash = hmacSha512(hashString, BuildConfig.VNPAY_HASH_SECRET);

            result.setValue(Result.success(VNPAY_ENDPOINT_SANDBOX + "?" + queryStr
                    + "&vnp_SecureHash=" + secureHash));
        } catch (Exception e) {
            result.setValue(Result.error("Lỗi tạo link VNPay: " + e.getMessage()));
        }

        return result;
    }

    /** Mở URL trong WebView thay vì browser ngoài */
    public static void openVNPay(Context context, String payUrl) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(payUrl));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    // ── HMAC helpers ──────────────────────────────────────────────

    private String hmacSha256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return bytesToHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    private String hmacSha512(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
        return bytesToHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
