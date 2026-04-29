package com.pizza.app.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Tin nhắn chat — collection Firestore: chats/{chatId}/messages/{messageId}
 */
public class Message {

    public static final String TYPE_TEXT  = "text";
    public static final String TYPE_IMAGE = "image";

    @DocumentId
    private String  id;
    private String  senderId;
    private String  senderName;
    private String  senderAvatar;
    private String  text;
    private String  imageUrl;
    private String  type;
    private boolean isRead;

    @ServerTimestamp
    private Date timestamp;

    public Message() {
        type = TYPE_TEXT;
    }

    public String  getId()                         { return id; }
    public void    setId(String id)                { this.id = id; }

    public String  getSenderId()                   { return senderId; }
    public void    setSenderId(String uid)         { this.senderId = uid; }

    public String  getSenderName()                 { return senderName; }
    public void    setSenderName(String name)      { this.senderName = name; }

    public String  getSenderAvatar()               { return senderAvatar; }
    public void    setSenderAvatar(String avatar)  { this.senderAvatar = avatar; }

    public String  getText()                       { return text; }
    public void    setText(String text)            { this.text = text; }

    public String  getImageUrl()                   { return imageUrl; }
    public void    setImageUrl(String url)         { this.imageUrl = url; }

    public String  getType()                       { return type; }
    public void    setType(String type)            { this.type = type; }

    public boolean isRead()                        { return isRead; }
    public void    setRead(boolean read)           { this.isRead = read; }

    public Date    getTimestamp()                  { return timestamp; }
    public void    setTimestamp(Date ts)           { this.timestamp = ts; }

    public boolean isImage() { return TYPE_IMAGE.equals(type); }
}
