package com.pizza.app.model;

import com.google.firebase.firestore.DocumentId;

/**
 * Banner quảng cáo trên trang chủ — collection Firestore: banners/{bannerId}
 */
public class Banner {

    @DocumentId
    private String id;
    private String imageUrl;
    private String title;
    private String actionType;   // "product", "category", "url", "voucher"
    private String actionValue;  // productId / categoryId / URL / voucherCode
    private int    order;
    private boolean isActive;

    public Banner() {}

    public String  getId()                       { return id; }
    public void    setId(String id)              { this.id = id; }

    public String  getImageUrl()                 { return imageUrl; }
    public void    setImageUrl(String url)       { this.imageUrl = url; }

    public String  getTitle()                    { return title; }
    public void    setTitle(String title)        { this.title = title; }

    public String  getActionType()               { return actionType; }
    public void    setActionType(String type)    { this.actionType = type; }

    public String  getActionValue()              { return actionValue; }
    public void    setActionValue(String value)  { this.actionValue = value; }

    public int     getOrder()                    { return order; }
    public void    setOrder(int order)           { this.order = order; }

    public boolean isActive()                    { return isActive; }
    public void    setActive(boolean active)     { this.isActive = active; }
}
