package com.example.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class SearchUserActivity extends AppCompatActivity {

    private TextInputEditText searchInput;
    private Button searchButton;
    private ImageButton backButton;
    private RecyclerView usersRecyclerView;
    private UserAdapter userAdapter;
    private DatabaseReference usersRef;
    private List<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        searchInput = findViewById(R.id.searchPhoneEditText);
        searchButton = findViewById(R.id.searchButton);
        backButton = findViewById(R.id.back_btn);
        usersRecyclerView = findViewById(R.id.usersRecyclerView);

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList, this::openChatActivity);

        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(userAdapter);

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        backButton.setOnClickListener(v -> finish());

        searchButton.setOnClickListener(v -> searchForUser());
    }

    private void searchForUser() {
        String phoneNumber = searchInput.getText().toString().trim();
        // Check if the search bar is empty
        if (phoneNumber.isEmpty()) {
            // Clear the user list and update the RecyclerView
            userList.clear();
            userAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Search cleared", Toast.LENGTH_SHORT).show();
            return;  // Exit the method if the search input is empty
        }

        // Proceed with the search if the phone number is not empty
        if (phoneNumber.length() < 10) {
            Toast.makeText(this, "Please enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use phone number as the key to directly access the user node
        usersRef.child(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear(); // Clear previous search results
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        userList.add(user);  // Add user to the list
                    }
                    userAdapter.notifyDataSetChanged(); // Update RecyclerView
                } else {
                    Toast.makeText(SearchUserActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SearchUserActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void openChatActivity(User user) {
        Intent chatIntent = new Intent(SearchUserActivity.this, ChatActivity.class);
        chatIntent.putExtra("userPhone", user.getPhoneNumber());  // Pass just the phone number
        chatIntent.putExtra("userName", user.getName());
        startActivity(chatIntent);
    }
}