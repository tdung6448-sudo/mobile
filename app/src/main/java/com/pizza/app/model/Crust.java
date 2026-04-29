package com.pizza.app.model;

/**
 * Loại đế bánh pizza và phụ phí (nếu có)
 */
public class Crust {

    private String id;
    private String name;        // "Mỏng giòn", "Dày xốp", "Viền phô mai"
    private long   extraPrice;  // Phụ phí thêm (0 nếu không thu thêm)

    public Crust() {}

    public Crust(String id, String name, long extraPrice) {
        this.id         = id;
        this.name       = name;
        this.extraPrice = extraPrice;
    }

    public String getId()                    { return id; }
    public void   setId(String id)           { this.id = id; }

    public String getName()                  { return name; }
    public void   setName(String name)       { this.name = name; }

    public long   getExtraPrice()            { return extraPrice; }
    public void   setExtraPrice(long price)  { this.extraPrice = price; }
}
