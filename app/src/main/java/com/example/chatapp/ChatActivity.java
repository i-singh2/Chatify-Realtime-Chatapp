package com.example.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";
    private TextView chatTitle;
    private DatabaseReference chatRoomRef;
    private String chatRoomId;
    private String currentUserId;
    private String otherUserPhone;
    private String otherUserName;
    private ImageButton backButton, sendButton;
    private EditText messageEditText;
    private RecyclerView messagesRecyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private String otherUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize views
        chatTitle = findViewById(R.id.user_name);
        backButton = findViewById(R.id.back_btn);
        sendButton = findViewById(R.id.send_btn);
        messageEditText = findViewById(R.id.messageEditText);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);

        // Initialize RecyclerView and MessageAdapter
        messageList = new ArrayList<>();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        messageAdapter = new MessageAdapter(messageList, currentUserId);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(messageAdapter);

        // Get the other user's information from the intent
        otherUserName = getIntent().getStringExtra("userName");
        otherUserPhone = getIntent().getStringExtra("userPhone");
        chatTitle.setText("Chat with " + otherUserName);

        // Reference to the users node
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(otherUserPhone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                otherUserId = snapshot.child("userUID").getValue(String.class);
                if (otherUserId == null) {
                    Log.e(TAG, "User ID for the other user is null.");
                    Toast.makeText(ChatActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                generateSharedChatRoomId();
                setupChatRoom();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error retrieving user UID: ", error.toException());
            }
        });

        // Set up back button
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(ChatActivity.this, MainActivity.class));
            finish();
        });

        sendButton.setOnClickListener(v -> {
            String messageText = messageEditText.getText().toString().trim();
            if (!messageText.isEmpty()) {
                if (chatRoomRef != null) {  // Ensure chatRoomRef is not null
                    sendMessage(messageText);
                    messageEditText.setText("");
                } else {
                    Toast.makeText(this, "Chat room not initialized", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void generateSharedChatRoomId() {
        if (currentUserId != null && otherUserId != null) {
            chatRoomId = (currentUserId.compareTo(otherUserId) < 0)
                    ? currentUserId + "_" + otherUserId
                    : otherUserId + "_" + currentUserId;
        } else {
            Log.e(TAG, "User IDs are null. Cannot generate chat room ID.");
        }
    }


    private void setupChatRoom() {
        if (chatRoomId == null) {
            Log.e(TAG, "Chat Room ID is null. Cannot set up chat room.");
            return;
        }
        DatabaseReference chatRoomsRef = FirebaseDatabase.getInstance().getReference("chatRooms");
        chatRoomsRef.child(chatRoomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatRoomRef = chatRoomsRef.child(chatRoomId);
                if (!snapshot.exists()) {
                    chatRoomRef.child("users").child("user1").setValue(currentUserId);
                    chatRoomRef.child("users").child("user2").setValue(otherUserId);
                    Log.d(TAG, "New chat room created: " + chatRoomId);
                } else {
                    Log.d(TAG, "Existing chat room loaded: " + chatRoomId);
                }
                loadMessages();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error setting up chat room: ", error.toException());
            }
        });
    }

    private void sendMessage(String text) {
        if (chatRoomRef == null) {
            Log.e(TAG, "chatRoomRef is null, cannot send message.");
            return;
        }

        String messageId = chatRoomRef.child("messages").push().getKey();
        if (messageId == null) return;

        // Create a new message with sender, receiver, text, and timestamp
        Message message = new Message(currentUserId, otherUserId, text, System.currentTimeMillis());

        // Add the message under the "messages" node in this chat room
        chatRoomRef.child("messages").child(messageId).setValue(message)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Message sent successfully");
                    } else {
                        Log.e(TAG, "Failed to send message: ", task.getException());
                    }
                });
    }

    private void loadMessages() {
        if (chatRoomRef == null) {
            Log.e(TAG, "chatRoomRef is null, cannot load messages.");
            return;
        }

        chatRoomRef.child("messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    if (message != null) {
                        messageList.add(message);
                    }
                }
                messageAdapter.notifyDataSetChanged();
                messagesRecyclerView.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading messages: ", error.toException());
                Toast.makeText(ChatActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
