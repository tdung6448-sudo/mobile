package com.pizza.app.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Model người dùng — tương ứng collection Firestore: users/{uid}
 */
public class User {

    public static final String ROLE_CUSTOMER = "customer";
    public static final String ROLE_ADMIN    = "admin";
    public static final String ROLE_SHIPPER  = "shipper";

    @DocumentId
    private String uid;
    private String name;
    private String email;
    private String phone;
    private String avatar;
    private String role;
    private String fcmToken;
    private boolean emailVerified;
    private boolean isBlocked;
    private List<Address> addresses;

    @ServerTimestamp
    private Date createdAt;

    // Constructor rỗng bắt buộc cho Firestore deserialization
    public User() {
        addresses = new ArrayList<>();
        role = ROLE_CUSTOMER;
    }

    public User(String uid, String name, String email, String role) {
        this.uid   = uid;
        this.name  = name;
        this.email = email;
        this.role  = role;
        this.addresses = new ArrayList<>();
    }

    // ── Getters & Setters ──────────────────────────────────────────

    public String getUid()                     { return uid; }
    public void   setUid(String uid)           { this.uid = uid; }

    public String getName()                    { return name; }
    public void   setName(String name)         { this.name = name; }

    public String getEmail()                   { return email; }
    public void   setEmail(String email)       { this.email = email; }

    public String getPhone()                   { return phone; }
    public void   setPhone(String phone)       { this.phone = phone; }

    public String getAvatar()                  { return avatar; }
    public void   setAvatar(String avatar)     { this.avatar = avatar; }

    public String getRole()                    { return role; }
    public void   setRole(String role)         { this.role = role; }

    public String getFcmToken()                { return fcmToken; }
    public void   setFcmToken(String token)    { this.fcmToken = token; }

    public boolean isEmailVerified()           { return emailVerified; }
    public void    setEmailVerified(boolean v) { this.emailVerified = v; }

    public boolean isBlocked()                 { return isBlocked; }
    public void    setBlocked(boolean blocked) { this.isBlocked = blocked; }

    public List<Address> getAddresses()        { return addresses; }
    public void setAddresses(List<Address> a)  { this.addresses = a; }

    public Date getCreatedAt()                 { return createdAt; }
    public void setCreatedAt(Date d)           { this.createdAt = d; }

    // ── Helpers ────────────────────────────────────────────────────

    public boolean isAdmin()    { return ROLE_ADMIN.equals(role); }
    public boolean isShipper()  { return ROLE_SHIPPER.equals(role); }
    public boolean isCustomer() { return ROLE_CUSTOMER.equals(role); }

    /** Trả về địa chỉ mặc định, null nếu chưa có */
    public Address getDefaultAddress() {
        if (addresses == null) return null;
        for (Address a : addresses) {
            if (a.isDefault()) return a;
        }
        return addresses.isEmpty() ? null : addresses.get(0);
    }
}
