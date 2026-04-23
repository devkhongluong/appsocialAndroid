package com.example.appsocialver2.activity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appsocialver2.Models.Message;
import com.example.appsocialver2.R;
import com.example.appsocialver2.adapters.MessageAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private String receiverId, receiverName, receiverAvatar;
    private String senderId;
    
    private TextView tvFriendName;
    private ImageView imgAvatarChat, btnBack, btnSend;
    private EditText edtMessage;
    private RecyclerView rcvChat;
    
    private MessageAdapter adapter;
    private List<Message> messageList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        receiverId = getIntent().getStringExtra("userId");
        receiverName = getIntent().getStringExtra("userName");
        receiverAvatar = getIntent().getStringExtra("userAvatar");
        senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        db = FirebaseFirestore.getInstance();

        initUi();
        setupChat();
        listenMessages();
    }

    private void initUi() {
        tvFriendName = findViewById(R.id.tvFriendName);
        imgAvatarChat = findViewById(R.id.imgAvatarChat);
        btnBack = findViewById(R.id.btnBack);
        btnSend = findViewById(R.id.btnSend);
        edtMessage = findViewById(R.id.edtMessage);
        rcvChat = findViewById(R.id.rcvChat);

        tvFriendName.setText(receiverName);
        if (receiverAvatar != null && !receiverAvatar.isEmpty()) {
            Glide.with(this).load(receiverAvatar).into(imgAvatarChat);
        }

        btnBack.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void setupChat() {
        messageList = new ArrayList<>();
        adapter = new MessageAdapter(this, messageList);
        rcvChat.setLayoutManager(new LinearLayoutManager(this));
        rcvChat.setAdapter(adapter);
    }

    private void sendMessage() {
        String text = edtMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        String messageId = db.collection("chats").document().getId();
        Message message = new Message(messageId, senderId, receiverId, text);
        
        db.collection("chats").document(messageId).set(message)
                .addOnSuccessListener(aVoid -> edtMessage.setText(""))
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi gửi tin nhắn", Toast.LENGTH_SHORT).show());
    }

    private void listenMessages() {
        db.collection("chats")
                .whereIn("senderId", java.util.Arrays.asList(senderId, receiverId))
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                Message msg = dc.getDocument().toObject(Message.class);
                                // Lọc tin nhắn của đúng cặp người gửi - người nhận
                                if ((msg.getSenderId().equals(senderId) && msg.getReceiverId().equals(receiverId)) ||
                                    (msg.getSenderId().equals(receiverId) && msg.getReceiverId().equals(senderId))) {
                                    messageList.add(msg);
                                    adapter.notifyItemInserted(messageList.size() - 1);
                                    rcvChat.scrollToPosition(messageList.size() - 1);
                                }
                            }
                        }
                    }
                });
    }
}
