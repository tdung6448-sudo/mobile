package com.pizza.app.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Một dòng trong đơn hàng đã đặt — lưu trong Firestore: orders/{id}/items[]
 */
public class OrderItem {

    private String       productId;
    private String       productName;
    private String       productImage;
    private String       sizeCode;
    private String       sizeName;
    private String       crustName;
    private List<String> toppingNames;
    private int          quantity;
    private long         unitPrice;
    private long         lineTotal;
    private String       note;

    public OrderItem() {
        toppingNames = new ArrayList<>();
    }

    /** Tạo OrderItem từ CartItem để lưu vào đơn hàng */
    public static OrderItem fromCartItem(CartItem cartItem) {
        OrderItem item = new OrderItem();
        item.productId    = cartItem.getProductId();
        item.productName  = cartItem.getProductName();
        item.productImage = cartItem.getProductImage();
        item.sizeCode     = cartItem.getSizeCode();
        item.sizeName     = cartItem.getSizeName();
        item.crustName    = cartItem.getCrustName();
        item.quantity     = cartItem.getQuantity();
        item.unitPrice    = cartItem.getUnitPrice();
        item.lineTotal    = cartItem.getLineTotal();
        item.note         = cartItem.getNote();

        if (cartItem.getSelectedToppings() != null) {
            for (Topping t : cartItem.getSelectedToppings()) {
                item.toppingNames.add(t.getName());
            }
        }
        return item;
    }

    public String       getProductId()              { return productId; }
    public void         setProductId(String id)     { this.productId = id; }

    public String       getProductName()             { return productName; }
    public void         setProductName(String name)  { this.productName = name; }

    public String       getProductImage()            { return productImage; }
    public void         setProductImage(String img)  { this.productImage = img; }

    public String       getSizeCode()                { return sizeCode; }
    public void         setSizeCode(String code)     { this.sizeCode = code; }

    public String       getSizeName()                { return sizeName; }
    public void         setSizeName(String name)     { this.sizeName = name; }

    public String       getCrustName()               { return crustName; }
    public void         setCrustName(String name)    { this.crustName = name; }

    public List<String> getToppingNames()            { return toppingNames; }
    public void         setToppingNames(List<String> t) { this.toppingNames = t; }

    public int          getQuantity()                { return quantity; }
    public void         setQuantity(int qty)         { this.quantity = qty; }

    public long         getUnitPrice()               { return unitPrice; }
    public void         setUnitPrice(long price)     { this.unitPrice = price; }

    public long         getLineTotal()               { return lineTotal; }
    public void         setLineTotal(long total)     { this.lineTotal = total; }

    public String       getNote()                    { return note; }
    public void         setNote(String note)         { this.note = note; }
}
