package com.example.fakebook;

import java.util.Date;

public class MessagePreview {
    private Date lastMessageTime;
    private String lastMessageText;
    private String senderId;
    private String receiverId;

    // Required no-arg constructor
    public MessagePreview() {}

    public MessagePreview(String senderId, String receiverId, Date lastMessageTime, String lastMessageText) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.lastMessageTime = lastMessageTime;
        this.lastMessageText = lastMessageText;
    }

    // Getters
    public String getLastMessageText() {
        return lastMessageText;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public Date getLastMessageTime() {
        return lastMessageTime;
    }

    // Setters
    public void setLastMessageText(String lastMessageText) {
        this.lastMessageText = lastMessageText;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public void setLastMessageTime(Date lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }
}
