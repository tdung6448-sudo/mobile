package com.pizza.app.model;

import com.google.firebase.firestore.DocumentId;

import java.util.Date;

/**
 * Voucher giảm giá — collection Firestore: vouchers/{code}
 */
public class Voucher {

    public static final String TYPE_PERCENT = "percent"; // Giảm theo %
    public static final String TYPE_AMOUNT  = "amount";  // Giảm số tiền cố định

    @DocumentId
    private String code;
    private String type;
    private long   value;        // Giá trị: % hoặc số tiền VND
    private long   minOrder;     // Đơn tối thiểu để áp dụng
    private long   maxDiscount;  // Giảm tối đa (chỉ áp dụng cho type=percent)
    private Date   validFrom;
    private Date   validTo;
    private int    usageLimit;   // Số lượt dùng tối đa (-1 = không giới hạn)
    private int    usedCount;
    private String description;
    private boolean isActive;

    public Voucher() {}

    public String  getCode()                    { return code; }
    public void    setCode(String code)         { this.code = code; }

    public String  getType()                    { return type; }
    public void    setType(String type)         { this.type = type; }

    public long    getValue()                   { return value; }
    public void    setValue(long value)         { this.value = value; }

    public long    getMinOrder()                { return minOrder; }
    public void    setMinOrder(long min)        { this.minOrder = min; }

    public long    getMaxDiscount()             { return maxDiscount; }
    public void    setMaxDiscount(long max)     { this.maxDiscount = max; }

    public Date    getValidFrom()               { return validFrom; }
    public void    setValidFrom(Date d)         { this.validFrom = d; }

    public Date    getValidTo()                 { return validTo; }
    public void    setValidTo(Date d)           { this.validTo = d; }

    public int     getUsageLimit()              { return usageLimit; }
    public void    setUsageLimit(int limit)     { this.usageLimit = limit; }

    public int     getUsedCount()               { return usedCount; }
    public void    setUsedCount(int count)      { this.usedCount = count; }

    public String  getDescription()             { return description; }
    public void    setDescription(String desc)  { this.description = desc; }

    public boolean isActive()                   { return isActive; }
    public void    setActive(boolean active)    { this.isActive = active; }

    // ── Helpers ────────────────────────────────────────────────────

    /** Kiểm tra voucher còn hiệu lực không */
    public boolean isValid(long orderAmount) {
        Date now = new Date();
        if (!isActive) return false;
        if (validFrom != null && now.before(validFrom)) return false;
        if (validTo   != null && now.after(validTo))    return false;
        if (usageLimit >= 0 && usedCount >= usageLimit) return false;
        if (orderAmount < minOrder) return false;
        return true;
    }

    /** Tính số tiền được giảm cho đơn hàng */
    public long calculateDiscount(long orderAmount) {
        if (!isValid(orderAmount)) return 0;
        if (TYPE_AMOUNT.equals(type)) {
            return Math.min(value, orderAmount);
        } else { // TYPE_PERCENT
            long discount = orderAmount * value / 100;
            if (maxDiscount > 0) discount = Math.min(discount, maxDiscount);
            return discount;
        }
    }
}
