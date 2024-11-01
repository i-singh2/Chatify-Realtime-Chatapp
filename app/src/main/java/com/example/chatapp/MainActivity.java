package com.example.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView chatListRecyclerView;
    private ChatListAdapter chatListAdapter;
    private List<User> chatUserList;
    private DatabaseReference chatRoomsRef;
    private DatabaseReference usersRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views and Firebase references
        chatListRecyclerView = findViewById(R.id.chatListRecyclerView);
        FloatingActionButton newChatButton = findViewById(R.id.new_chat);
        chatUserList = new ArrayList<>();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chatRoomsRef = FirebaseDatabase.getInstance().getReference("chatRooms");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Set up RecyclerView
        chatListAdapter = new ChatListAdapter(chatUserList, this::openChatActivity);
        chatListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatListRecyclerView.setAdapter(chatListAdapter);

        // Load existing chats
        loadExistingChats();

        // Set click listener for creating new chat
        newChatButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SearchUserActivity.class);
            startActivity(intent);
        });
    }

    // Load existing chats where the current user is a participant
    private void loadExistingChats() {
        chatRoomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatUserList.clear();
                for (DataSnapshot chatRoomSnapshot : snapshot.getChildren()) {
                    DataSnapshot usersSnapshot = chatRoomSnapshot.child("users");

                    String user1 = usersSnapshot.child("user1").getValue(String.class);
                    String user2 = usersSnapshot.child("user2").getValue(String.class);

                    // Determine if the current user is part of this chat room
                    if (user1 != null && user2 != null) {
                        String otherUserId = user1.equals(currentUserId) ? user2 : (user2.equals(currentUserId) ? user1 : null);

                        if (otherUserId != null) {
                            loadUserDetails(otherUserId); // Load details from top-level "users" node
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MainActivity", "Error loading chats: ", error.toException());
            }
        });
    }

    // Load the other user's details by their ID from the top-level "users" node
    private void loadUserDetails(String otherUserId) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean userFound = false;

                // Loop through each user entry in the users node
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);

                    if (user != null) {
                        Log.d("MainActivity", "Checking user: " + user.getUserUID());

                        // Compare the userUID in the database with the provided otherUserId
                        if (otherUserId.equals(user.getUserUID())) {
                            chatUserList.add(user);
                            chatListAdapter.notifyDataSetChanged();
                            Log.d("MainActivity", "User found and added: " + user.getName() + ", " + user.getPhoneNumber());
                            userFound = true;
                            break;
                        }
                    } else {
                        Log.e("MainActivity", "User data is null for snapshot key: " + userSnapshot.getKey());
                    }
                }

                if (!userFound) {
                    Log.e("MainActivity", "User not found for ID: " + otherUserId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MainActivity", "Error loading user details: ", error.toException());
            }
        });
    }



    // Open ChatActivity with the selected user
    private void openChatActivity(User user) {
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        intent.putExtra("userName", user.getName());
        intent.putExtra("userPhone", user.getPhoneNumber());
        intent.putExtra("userUID", user.getUserUID());  // Pass the user's UID for generating chatRoom ID
        startActivity(intent);
    }
}
