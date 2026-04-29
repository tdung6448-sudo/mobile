package com.pizza.app.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Đơn hàng — collection Firestore: orders/{orderId}
 */
public class Order {

    // ── Trạng thái đơn hàng ────────────────────────────────────────
    public static final String STATUS_PENDING    = "pending";    // Chờ xác nhận
    public static final String STATUS_CONFIRMED  = "confirmed";  // Đã xác nhận
    public static final String STATUS_PREPARING  = "preparing";  // Đang làm
    public static final String STATUS_DELIVERING = "delivering"; // Đang giao
    public static final String STATUS_COMPLETED  = "completed";  // Hoàn thành
    public static final String STATUS_CANCELLED  = "cancelled";  // Đã huỷ

    // ── Phương thức thanh toán ─────────────────────────────────────
    public static final String PAYMENT_COD     = "cod";
    public static final String PAYMENT_MOMO    = "momo";
    public static final String PAYMENT_VNPAY   = "vnpay";
    public static final String PAYMENT_ZALOPAY = "zalopay";
    public static final String PAYMENT_STRIPE  = "stripe";

    // ── Trạng thái thanh toán ──────────────────────────────────────
    public static final String PAY_STATUS_PENDING = "pending";
    public static final String PAY_STATUS_PAID    = "paid";
    public static final String PAY_STATUS_FAILED  = "failed";
    public static final String PAY_STATUS_REFUNDED = "refunded";

    @DocumentId
    private String id;
    private String          userId;
    private String          userName;
    private String          userPhone;
    private List<OrderItem> items;
    private long            subtotal;
    private long            shippingFee;
    private long            discount;
    private long            total;
    private String          voucherCode;
    private String          paymentMethod;
    private String          paymentStatus;
    private String          paymentTransactionId;

    // Địa chỉ giao hàng snapshot (lưu lại tại thời điểm đặt)
    private String  deliveryAddress;
    private double  deliveryLat;
    private double  deliveryLng;

    private String  note;
    private String  status;
    private List<StatusHistory> statusHistory;

    // Thông tin shipper
    private String   shipperId;
    private String   shipperName;
    private String   shipperPhone;
    private GeoPoint shipperLocation;  // Vị trí realtime của shipper
    private String   estimatedTime;    // ETA dạng chuỗi "15 phút"

    @ServerTimestamp
    private Date createdAt;
    private Date updatedAt;

    public Order() {
        items         = new ArrayList<>();
        statusHistory = new ArrayList<>();
        status        = STATUS_PENDING;
        paymentStatus = PAY_STATUS_PENDING;
    }

    // ── Getters & Setters (đầy đủ cho Firestore) ──────────────────

    public String getId()                              { return id; }
    public void   setId(String id)                     { this.id = id; }

    public String getUserId()                          { return userId; }
    public void   setUserId(String uid)                { this.userId = uid; }

    public String getUserName()                        { return userName; }
    public void   setUserName(String name)             { this.userName = name; }

    public String getUserPhone()                       { return userPhone; }
    public void   setUserPhone(String phone)           { this.userPhone = phone; }

    public List<OrderItem> getItems()                  { return items; }
    public void   setItems(List<OrderItem> items)      { this.items = items; }

    public long   getSubtotal()                        { return subtotal; }
    public void   setSubtotal(long subtotal)           { this.subtotal = subtotal; }

    public long   getShippingFee()                     { return shippingFee; }
    public void   setShippingFee(long fee)             { this.shippingFee = fee; }

    public long   getDiscount()                        { return discount; }
    public void   setDiscount(long discount)           { this.discount = discount; }

    public long   getTotal()                           { return total; }
    public void   setTotal(long total)                 { this.total = total; }

    public String getVoucherCode()                     { return voucherCode; }
    public void   setVoucherCode(String code)          { this.voucherCode = code; }

    public String getPaymentMethod()                   { return paymentMethod; }
    public void   setPaymentMethod(String method)      { this.paymentMethod = method; }

    public String getPaymentStatus()                   { return paymentStatus; }
    public void   setPaymentStatus(String status)      { this.paymentStatus = status; }

    public String getPaymentTransactionId()            { return paymentTransactionId; }
    public void   setPaymentTransactionId(String tid)  { this.paymentTransactionId = tid; }

    public String getDeliveryAddress()                 { return deliveryAddress; }
    public void   setDeliveryAddress(String addr)      { this.deliveryAddress = addr; }

    public double getDeliveryLat()                     { return deliveryLat; }
    public void   setDeliveryLat(double lat)           { this.deliveryLat = lat; }

    public double getDeliveryLng()                     { return deliveryLng; }
    public void   setDeliveryLng(double lng)           { this.deliveryLng = lng; }

    public String getNote()                            { return note; }
    public void   setNote(String note)                 { this.note = note; }

    public String getStatus()                          { return status; }
    public void   setStatus(String status)             { this.status = status; }

    public List<StatusHistory> getStatusHistory()      { return statusHistory; }
    public void   setStatusHistory(List<StatusHistory> h) { this.statusHistory = h; }

    public String   getShipperId()                     { return shipperId; }
    public void     setShipperId(String id)            { this.shipperId = id; }

    public String   getShipperName()                   { return shipperName; }
    public void     setShipperName(String name)        { this.shipperName = name; }

    public String   getShipperPhone()                  { return shipperPhone; }
    public void     setShipperPhone(String phone)      { this.shipperPhone = phone; }

    public GeoPoint getShipperLocation()               { return shipperLocation; }
    public void     setShipperLocation(GeoPoint loc)   { this.shipperLocation = loc; }

    public String   getEstimatedTime()                 { return estimatedTime; }
    public void     setEstimatedTime(String eta)       { this.estimatedTime = eta; }

    public Date getCreatedAt()                         { return createdAt; }
    public void setCreatedAt(Date d)                   { this.createdAt = d; }

    public Date getUpdatedAt()                         { return updatedAt; }
    public void setUpdatedAt(Date d)                   { this.updatedAt = d; }

    // ── Helpers ────────────────────────────────────────────────────

    public boolean isPending()    { return STATUS_PENDING.equals(status); }
    public boolean isDelivering() { return STATUS_DELIVERING.equals(status); }
    public boolean isCompleted()  { return STATUS_COMPLETED.equals(status); }
    public boolean isCancelled()  { return STATUS_CANCELLED.equals(status); }

    /** Số món trong đơn */
    public int getTotalItemCount() {
        if (items == null) return 0;
        int count = 0;
        for (OrderItem item : items) count += item.getQuantity();
        return count;
    }

    // ── Inner class lịch sử trạng thái ────────────────────────────

    public static class StatusHistory {
        private String status;
        private Date   timestamp;
        private String note;

        public StatusHistory() {}

        public StatusHistory(String status, Date timestamp, String note) {
            this.status    = status;
            this.timestamp = timestamp;
            this.note      = note;
        }

        public String getStatus()             { return status; }
        public void   setStatus(String s)     { this.status = s; }

        public Date   getTimestamp()          { return timestamp; }
        public void   setTimestamp(Date t)    { this.timestamp = t; }

        public String getNote()               { return note; }
        public void   setNote(String note)    { this.note = note; }
    }
}
