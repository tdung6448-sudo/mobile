package com.pizza.app.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Đánh giá sản phẩm — collection Firestore: reviews/{reviewId}
 */
public class Review {

    @DocumentId
    private String id;
    private String       userId;
    private String       userName;
    private String       userAvatar;
    private String       productId;
    private String       orderId;
    private float        rating;      // 1-5 sao
    private String       comment;
    private List<String> images;      // Ảnh đính kèm

    @ServerTimestamp
    private Date createdAt;

    public Review() {
        images = new ArrayList<>();
    }

    public String getId()                         { return id; }
    public void   setId(String id)                { this.id = id; }

    public String getUserId()                     { return userId; }
    public void   setUserId(String uid)           { this.userId = uid; }

    public String getUserName()                   { return userName; }
    public void   setUserName(String name)        { this.userName = name; }

    public String getUserAvatar()                 { return userAvatar; }
    public void   setUserAvatar(String avatar)    { this.userAvatar = avatar; }

    public String getProductId()                  { return productId; }
    public void   setProductId(String pid)        { this.productId = pid; }

    public String getOrderId()                    { return orderId; }
    public void   setOrderId(String oid)          { this.orderId = oid; }

    public float  getRating()                     { return rating; }
    public void   setRating(float rating)         { this.rating = rating; }

    public String getComment()                    { return comment; }
    public void   setComment(String comment)      { this.comment = comment; }

    public List<String> getImages()               { return images; }
    public void   setImages(List<String> images)  { this.images = images; }

    public Date   getCreatedAt()                  { return createdAt; }
    public void   setCreatedAt(Date d)            { this.createdAt = d; }
}
