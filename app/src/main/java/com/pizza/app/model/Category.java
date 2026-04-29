package com.pizza.app.model;

import com.google.firebase.firestore.DocumentId;

/**
 * Danh mục sản phẩm — collection Firestore: categories/{categoryId}
 */
public class Category {

    @DocumentId
    private String id;
    private String name;
    private String image;
    private int    order;    // Thứ tự hiển thị
    private boolean isActive;

    public Category() {}

    public Category(String id, String name, String image, int order) {
        this.id       = id;
        this.name     = name;
        this.image    = image;
        this.order    = order;
        this.isActive = true;
    }

    public String  getId()                  { return id; }
    public void    setId(String id)         { this.id = id; }

    public String  getName()                { return name; }
    public void    setName(String name)     { this.name = name; }

    public String  getImage()               { return image; }
    public void    setImage(String image)   { this.image = image; }

    public int     getOrder()               { return order; }
    public void    setOrder(int order)      { this.order = order; }

    public boolean isActive()               { return isActive; }
    public void    setActive(boolean a)     { this.isActive = a; }
}
