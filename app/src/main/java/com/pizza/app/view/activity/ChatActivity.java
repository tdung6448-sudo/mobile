package com.pizza.app.view.activity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.pizza.app.databinding.ActivityChatBinding;
import com.pizza.app.model.Message;
import com.pizza.app.repository.ChatRepository;
import com.pizza.app.util.Constants;
import com.pizza.app.view.adapter.MessageAdapter;

import java.util.UUID;

/**
 * Chat real-time giữa khách và cửa hàng
 */
public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private MessageAdapter      adapter;
    private ChatRepository      chatRepo;
    private String              chatId;
    private String              currentUid;
    private String              currentName;

    private final ActivityResultLauncher<String> imageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::sendImage);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding  = ActivityChatBinding.inflate(getLayoutInflater());
        chatRepo = new ChatRepository();
        setContentView(binding.getRoot());

        chatId     = getIntent().getStringExtra(Constants.EXTRA_CHAT_ID);
        currentUid = FirebaseAuth.getInstance().getUid();

        if (chatId == null || currentUid == null) { finish(); return; }

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.toolbar.setTitle("Chat với cửa hàng");

        setupRecyclerView();
        loadMessages();
        setupSendButton();
        binding.btnAttach.setOnClickListener(v -> imageLauncher.launch("image/*"));
    }

    private void setupRecyclerView() {
        adapter = new MessageAdapter(currentUid);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Tin nhắn mới nhất ở dưới
        binding.rvMessages.setLayoutManager(layoutManager);
        binding.rvMessages.setAdapter(adapter);
    }

    private void loadMessages() {
        chatRepo.getMessages(chatId).observe(this, result -> {
            if (result.isSuccess() && result.data != null) {
                adapter.setItems(result.data);
                // Scroll xuống tin nhắn cuối
                if (!result.data.isEmpty()) {
                    binding.rvMessages.scrollToPosition(result.data.size() - 1);
                }
                // Đánh dấu đã đọc
                chatRepo.markAllAsRead(chatId, currentUid);
            }
        });
    }

    private void setupSendButton() {
        binding.btnSend.setOnClickListener(v -> {
            String text = binding.etMessage.getText().toString().trim();
            if (text.isEmpty()) return;

            Message message = new Message();
            message.setSenderId(currentUid);
            message.setText(text);
            message.setType(Message.TYPE_TEXT);

            chatRepo.sendMessage(chatId, message).observe(this, result -> {
                if (result.isSuccess()) {
                    binding.etMessage.setText("");
                }
            });
        });
    }

    private void sendImage(Uri uri) {
        if (uri == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);

        String path = Constants.STORAGE_CHAT + "/" + chatId + "_" + UUID.randomUUID() + ".jpg";
        FirebaseStorage.getInstance().getReference(path)
                .putFile(uri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    return FirebaseStorage.getInstance().getReference(path).getDownloadUrl();
                })
                .addOnSuccessListener(downloadUri -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Message message = new Message();
                    message.setSenderId(currentUid);
                    message.setImageUrl(downloadUri.toString());
                    message.setType(Message.TYPE_IMAGE);
                    chatRepo.sendMessage(chatId, message);
                })
                .addOnFailureListener(e -> binding.progressBar.setVisibility(View.GONE));
    }
}
