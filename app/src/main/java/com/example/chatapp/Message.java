package com.example.chatapp;

public class Message {
    private String sender;
    private String receiver;
    private String text;
    private long timestamp;
    private String videoUrl;

    // Default constructor for Firebase
    public Message() {
    }

    public Message(String sender, String receiver, String text, long timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
        this.timestamp = timestamp;
        this.videoUrl = null;
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

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}
