package com.pizza.app.model;

/**
 * Địa chỉ giao hàng — lưu trong mảng addresses[] của User
 */
public class Address {

    private String id;
    private String label;        // "Nhà", "Công ty", v.v.
    private String fullAddress;  // Địa chỉ đầy đủ hiển thị
    private double lat;
    private double lng;
    private boolean isDefault;

    public Address() {}

    public Address(String id, String label, String fullAddress, double lat, double lng) {
        this.id          = id;
        this.label       = label;
        this.fullAddress = fullAddress;
        this.lat         = lat;
        this.lng         = lng;
    }

    public String  getId()                    { return id; }
    public void    setId(String id)           { this.id = id; }

    public String  getLabel()                 { return label; }
    public void    setLabel(String label)     { this.label = label; }

    public String  getFullAddress()           { return fullAddress; }
    public void    setFullAddress(String a)   { this.fullAddress = a; }

    public double  getLat()                   { return lat; }
    public void    setLat(double lat)         { this.lat = lat; }

    public double  getLng()                   { return lng; }
    public void    setLng(double lng)         { this.lng = lng; }

    public boolean isDefault()                { return isDefault; }
    public void    setDefault(boolean d)      { this.isDefault = d; }
}
