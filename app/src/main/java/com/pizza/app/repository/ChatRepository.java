package com.pizza.app.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.pizza.app.model.Message;
import com.pizza.app.repository.AuthRepository.Result;
import com.pizza.app.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository chat real-time giữa khách và cửa hàng
 * Path: chats/{userId}/messages/{messageId}
 */
public class ChatRepository {

    private final FirebaseFirestore db;

    public ChatRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /** Lắng nghe tin nhắn real-time */
    public LiveData<Result<List<Message>>> getMessages(String chatId) {
        MutableLiveData<Result<List<Message>>> result = new MutableLiveData<>();

        db.collection(Constants.COL_CHATS)
          .document(chatId)
          .collection(Constants.COL_MESSAGES)
          .orderBy("timestamp", Query.Direction.ASCENDING)
          .addSnapshotListener((snap, e) -> {
              if (e != null) { result.setValue(Result.error(e.getMessage())); return; }
              List<Message> list = new ArrayList<>();
              if (snap != null) {
                  for (DocumentSnapshot doc : snap.getDocuments()) {
                      Message m = doc.toObject(Message.class);
                      if (m != null) list.add(m);
                  }
              }
              result.setValue(Result.success(list));
          });

        return result;
    }

    /** Gửi tin nhắn */
    public LiveData<Result<Void>> sendMessage(String chatId, Message message) {
        MutableLiveData<Result<Void>> result = new MutableLiveData<>();

        db.collection(Constants.COL_CHATS)
          .document(chatId)
          .collection(Constants.COL_MESSAGES)
          .add(message)
          .addOnSuccessListener(ref -> {
              // Cập nhật lastMessage trong document chats/{chatId}
              db.collection(Constants.COL_CHATS).document(chatId)
                .set(buildChatSummary(message), com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(v -> result.setValue(Result.success(null)));
          })
          .addOnFailureListener(e -> result.setValue(Result.error(e.getMessage())));

        return result;
    }

    /** Đánh dấu tất cả tin nhắn là đã đọc */
    public void markAllAsRead(String chatId, String currentUserId) {
        db.collection(Constants.COL_CHATS)
          .document(chatId)
          .collection(Constants.COL_MESSAGES)
          .whereEqualTo("isRead", false)
          .get()
          .addOnSuccessListener(snap -> {
              for (DocumentSnapshot doc : snap.getDocuments()) {
                  Message msg = doc.toObject(Message.class);
                  if (msg != null && !currentUserId.equals(msg.getSenderId())) {
                      doc.getReference().update("isRead", true);
                  }
              }
          });
    }

    private java.util.Map<String, Object> buildChatSummary(Message message) {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("lastMessage",   message.isImage() ? "[Hình ảnh]" : message.getText());
        map.put("lastTimestamp", message.getTimestamp());
        map.put("senderId",      message.getSenderId());
        return map;
    }
}
