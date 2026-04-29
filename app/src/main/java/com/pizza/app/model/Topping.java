package com.pizza.app.model;

/**
 * Topping thêm vào pizza
 */
public class Topping {

    private String  id;
    private String  name;       // "Xúc xích", "Phô mai thêm", "Nấm"
    private long    price;      // Giá thêm mỗi topping
    private boolean isSelected; // Trạng thái chọn trên UI (không lưu Firestore)

    public Topping() {}

    public Topping(String id, String name, long price) {
        this.id    = id;
        this.name  = name;
        this.price = price;
    }

    public String  getId()                    { return id; }
    public void    setId(String id)           { this.id = id; }

    public String  getName()                  { return name; }
    public void    setName(String name)       { this.name = name; }

    public long    getPrice()                 { return price; }
    public void    setPrice(long price)       { this.price = price; }

    public boolean isSelected()               { return isSelected; }
    public void    setSelected(boolean sel)   { this.isSelected = sel; }
}
