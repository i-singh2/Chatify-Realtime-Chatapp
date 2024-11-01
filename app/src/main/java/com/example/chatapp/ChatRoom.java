package com.example.chatapp;

import java.util.HashMap;
import java.util.Map;

public class ChatRoom {

    private Map<String, String> users = new HashMap<>(); // Map of user IDs to names

    public ChatRoom() { }

    public Map<String, String> getUsers() {
        return users;
    }

    public void setUsers(Map<String, String> users) {
        this.users = users;
    }

    // Optionally add helper method to get a user's phone number if stored
    public String getUserPhone(String userId) {
        return users.containsKey(userId) ? users.get(userId) : null;
    }
}
