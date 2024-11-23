package com.example.chatapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ChatActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final int REQUEST_VIDEO_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_CAMERA_AND_MIC_PERMISSION = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private ImageButton sendLocationButton;
    private ImageButton recordVideoButton;
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
    private Uri videoUri;

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
        sendLocationButton = findViewById(R.id.sendLocationButton);
        recordVideoButton = findViewById(R.id.recordVideoButton);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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

        // Set up send button
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

        // Set up send location button
        sendLocationButton.setOnClickListener(v -> {
            // Check for location permissions
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Request location permissions
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                // Get and send location
                sendLocationMessage();
            }
        });

        recordVideoButton.setOnClickListener(v -> {
            checkCameraPermission();
            Log.d("ChatActivity", "Camera button clicked");
            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            startActivityForResult(takeVideoIntent, 1);
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

    private void sendLocationMessage() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        // Generate Google Maps link
                        String locationMessage = "https://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();

                        // Send the location as a message
                        sendMessage(locationMessage);
                    }
                });
    }

    // Override to handle location permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            sendLocationMessage();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK && data != null) {
            Uri videoUri = data.getData();
            if (videoUri != null) {
                Log.d("ChatActivity", "Video captured: " + videoUri.toString());
                uploadVideoToFirebase(videoUri);
            } else {
                Log.e("ChatActivity", "Video URI is null");
            }
        }
    }


    private void uploadVideoToFirebase(Uri videoUri) {
        if (chatRoomRef == null) {
            Log.e("ChatActivity", "chatRoomRef is null, cannot upload video.");
            return;
        }

        String messageId = chatRoomRef.child("messages").push().getKey();
        if (messageId == null) {
            Log.e("ChatActivity", "Failed to generate message ID for video.");
            return;
        }

        // Store video directly in the "videos" folder
        StorageReference videoRef = FirebaseStorage.getInstance()
                .getReference("videos/" + messageId + ".mp4");

        videoRef.putFile(videoUri)
                .addOnSuccessListener(taskSnapshot -> {
                    videoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String videoUrl = uri.toString();
                        Log.d("ChatActivity", "Video uploaded successfully: " + videoUrl);
                        saveVideoMessageToDatabase(videoUrl, messageId);
                    }).addOnFailureListener(e -> {
                        Log.e("ChatActivity", "Failed to get video download URL", e);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("ChatActivity", "Failed to upload video", e);
                });
    }

    private void saveVideoMessageToDatabase(String videoUrl, String messageId) {
        // Create a new message object with video URL
        Message videoMessage = new Message(currentUserId, otherUserId, "", System.currentTimeMillis());
        videoMessage.setVideoUrl(videoUrl); // Set the video URL in the message object

        // Save the message in the chat room
        chatRoomRef.child("messages").child(messageId).setValue(videoMessage)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("ChatActivity", "Video message saved successfully");
                    } else {
                        Log.e("ChatActivity", "Failed to save video message: ", task.getException());
                    }
                });
    }



    private void sendVideoMessage(String videoUrl) {
        // Generate a unique message ID
        String messageId = chatRoomRef.child("messages").push().getKey();
        if (messageId == null) {
            Log.e("ChatActivity", "Failed to generate message ID. Cannot send video message.");
            return;
        }

        // Create a video message object
        Message videoMessage = new Message(currentUserId, otherUserId, "", System.currentTimeMillis());
        videoMessage.setVideoUrl(videoUrl); // Set the video URL

        // Save the video message under the "messages" node
        chatRoomRef.child("messages").child(messageId).setValue(videoMessage)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("ChatActivity", "Video message sent successfully: " + videoUrl);
                    } else {
                        Log.e("ChatActivity", "Failed to send video message: ", task.getException());
                    }
                })
                .addOnFailureListener(e -> Log.e("ChatActivity", "Error while sending video message: ", e));
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO}, REQUEST_CAMERA_PERMISSION);
        }


    }
}






