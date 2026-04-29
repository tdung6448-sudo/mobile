package com.pizza.app.model;

/**
 * Kích cỡ pizza và giá tương ứng
 */
public class ProductSize {

    private String name;    // "Nhỏ (S)", "Vừa (M)", "Lớn (L)"
    private long   price;   // Giá theo đơn vị VND (long để tránh float rounding)
    private String code;    // "S", "M", "L"

    public ProductSize() {}

    public ProductSize(String code, String name, long price) {
        this.code  = code;
        this.name  = name;
        this.price = price;
    }

    public String getName()              { return name; }
    public void   setName(String name)   { this.name = name; }

    public long   getPrice()             { return price; }
    public void   setPrice(long price)   { this.price = price; }

    public String getCode()              { return code; }
    public void   setCode(String code)   { this.code = code; }
}
