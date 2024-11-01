package com.example.chatapp;

public class User {
    private String name;
    private String phoneNumber;
    private String userUID;

    // Required empty constructor
    public User() {
    }

    public User(String name, String phoneNumber, String userUID) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.userUID = userUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUserUID() {
        return userUID;
    }

    public void setUserUID(String userUID) {
        this.userUID = userUID;
    }
}
