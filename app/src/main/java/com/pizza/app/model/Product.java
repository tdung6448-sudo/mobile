package com.pizza.app.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Sản phẩm (pizza, đồ uống, ...) — collection Firestore: products/{productId}
 */
public class Product {

    @DocumentId
    private String id;
    private String       name;
    private String       description;
    private String       categoryId;
    private List<String> images;       // URL ảnh trên Firebase Storage
    private long         basePrice;    // Giá cơ bản (size S)
    private List<ProductSize> sizes;   // Danh sách size và giá
    private List<Crust>  crusts;       // Loại đế
    private List<Topping> toppings;    // Topping có thể chọn thêm
    private boolean      isAvailable;
    private boolean      isFeatured;   // Nổi bật trên trang chủ
    private boolean      isNew;        // Badge "Mới"
    private boolean      isBestSeller;
    private int          soldCount;
    private float        ratingAvg;
    private int          ratingCount;
    private List<String> allergens;    // Thành phần dị ứng
    private String       ingredients;  // Nguyên liệu hiển thị

    @ServerTimestamp
    private Date createdAt;

    public Product() {
        images    = new ArrayList<>();
        sizes     = new ArrayList<>();
        crusts    = new ArrayList<>();
        toppings  = new ArrayList<>();
        allergens = new ArrayList<>();
        isAvailable = true;
    }

    // ── Getters & Setters ──────────────────────────────────────────

    public String getId()                          { return id; }
    public void   setId(String id)                 { this.id = id; }

    public String getName()                        { return name; }
    public void   setName(String name)             { this.name = name; }

    public String getDescription()                 { return description; }
    public void   setDescription(String desc)      { this.description = desc; }

    public String getCategoryId()                  { return categoryId; }
    public void   setCategoryId(String cId)        { this.categoryId = cId; }

    public List<String> getImages()                { return images; }
    public void   setImages(List<String> images)   { this.images = images; }

    public long   getBasePrice()                   { return basePrice; }
    public void   setBasePrice(long price)         { this.basePrice = price; }

    public List<ProductSize> getSizes()            { return sizes; }
    public void   setSizes(List<ProductSize> s)    { this.sizes = s; }

    public List<Crust> getCrusts()                 { return crusts; }
    public void   setCrusts(List<Crust> crusts)    { this.crusts = crusts; }

    public List<Topping> getToppings()             { return toppings; }
    public void   setToppings(List<Topping> t)     { this.toppings = t; }

    public boolean isAvailable()                   { return isAvailable; }
    public void    setAvailable(boolean a)         { this.isAvailable = a; }

    public boolean isFeatured()                    { return isFeatured; }
    public void    setFeatured(boolean f)          { this.isFeatured = f; }

    public boolean isNew()                         { return isNew; }
    public void    setNew(boolean n)               { this.isNew = n; }

    public boolean isBestSeller()                  { return isBestSeller; }
    public void    setBestSeller(boolean b)        { this.isBestSeller = b; }

    public int    getSoldCount()                   { return soldCount; }
    public void   setSoldCount(int count)          { this.soldCount = count; }

    public float  getRatingAvg()                   { return ratingAvg; }
    public void   setRatingAvg(float avg)          { this.ratingAvg = avg; }

    public int    getRatingCount()                 { return ratingCount; }
    public void   setRatingCount(int count)        { this.ratingCount = count; }

    public List<String> getAllergens()             { return allergens; }
    public void   setAllergens(List<String> a)     { this.allergens = a; }

    public String getIngredients()                 { return ingredients; }
    public void   setIngredients(String ing)       { this.ingredients = ing; }

    public Date   getCreatedAt()                   { return createdAt; }
    public void   setCreatedAt(Date d)             { this.createdAt = d; }

    // ── Helpers ────────────────────────────────────────────────────

    /** Lấy ảnh đầu tiên (thumbnail), trả về chuỗi rỗng nếu không có */
    public String getThumbnail() {
        return (images != null && !images.isEmpty()) ? images.get(0) : "";
    }

    /** Lấy giá theo size code (S/M/L). Fallback về basePrice nếu không tìm thấy */
    public long getPriceBySizeCode(String sizeCode) {
        if (sizes != null) {
            for (ProductSize s : sizes) {
                if (s.getCode().equals(sizeCode)) return s.getPrice();
            }
        }
        return basePrice;
    }
}
