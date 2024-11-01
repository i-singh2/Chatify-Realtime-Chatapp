package com.example.chatapp;

public class Message {
    private String sender;
    private String receiver;
    private String text;
    private long timestamp;

    // Default constructor for Firebase
    public Message() {
    }

    public Message(String sender, String receiver, String text, long timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getText() {
        return text;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
