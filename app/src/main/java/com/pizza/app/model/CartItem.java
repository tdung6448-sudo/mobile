package com.pizza.app.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Một dòng trong giỏ hàng — lưu cục bộ (SharedPreferences + Gson)
 */
public class CartItem {

    private String  productId;
    private String  productName;
    private String  productImage;
    private String  sizeCode;      // "S", "M", "L"
    private String  sizeName;      // "Nhỏ", "Vừa", "Lớn"
    private String  crustId;
    private String  crustName;
    private List<Topping> selectedToppings;
    private int     quantity;
    private long    unitPrice;     // Giá 1 đơn vị (đã tính size + toppings)
    private String  note;

    public CartItem() {
        selectedToppings = new ArrayList<>();
        quantity = 1;
    }

    // ── Getters & Setters ──────────────────────────────────────────

    public String  getProductId()                  { return productId; }
    public void    setProductId(String id)         { this.productId = id; }

    public String  getProductName()                { return productName; }
    public void    setProductName(String name)     { this.productName = name; }

    public String  getProductImage()               { return productImage; }
    public void    setProductImage(String img)     { this.productImage = img; }

    public String  getSizeCode()                   { return sizeCode; }
    public void    setSizeCode(String code)        { this.sizeCode = code; }

    public String  getSizeName()                   { return sizeName; }
    public void    setSizeName(String name)        { this.sizeName = name; }

    public String  getCrustId()                    { return crustId; }
    public void    setCrustId(String id)           { this.crustId = id; }

    public String  getCrustName()                  { return crustName; }
    public void    setCrustName(String name)       { this.crustName = name; }

    public List<Topping> getSelectedToppings()     { return selectedToppings; }
    public void    setSelectedToppings(List<Topping> t) { this.selectedToppings = t; }

    public int     getQuantity()                   { return quantity; }
    public void    setQuantity(int qty)            { this.quantity = qty; }

    public long    getUnitPrice()                  { return unitPrice; }
    public void    setUnitPrice(long price)        { this.unitPrice = price; }

    public String  getNote()                       { return note; }
    public void    setNote(String note)            { this.note = note; }

    // ── Helpers ────────────────────────────────────────────────────

    /** Tổng giá dòng này = đơn giá × số lượng */
    public long getLineTotal() {
        return unitPrice * quantity;
    }

    /** Mô tả ngắn tùy chỉnh: "Vừa • Viền phô mai • +Nấm" */
    public String getVariantDescription() {
        StringBuilder sb = new StringBuilder();
        if (sizeName != null)  sb.append(sizeName);
        if (crustName != null) sb.append(" • ").append(crustName);
        if (selectedToppings != null && !selectedToppings.isEmpty()) {
            sb.append(" • +");
            for (int i = 0; i < selectedToppings.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(selectedToppings.get(i).getName());
            }
        }
        return sb.toString();
    }

    /** Key duy nhất để so sánh 2 CartItem (cùng sản phẩm + cùng variant) */
    public String getUniqueKey() {
        StringBuilder sb = new StringBuilder(productId)
                .append("_").append(sizeCode)
                .append("_").append(crustId);
        if (selectedToppings != null) {
            for (Topping t : selectedToppings) {
                sb.append("_").append(t.getId());
            }
        }
        return sb.toString();
    }
}
