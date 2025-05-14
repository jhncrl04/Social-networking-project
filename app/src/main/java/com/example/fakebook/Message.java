package com.example.fakebook;

import java.util.Date;

public class Message {
    private Date sentDate;
    private String content;
    private String senderId;
    private String receiverId;
    private String currentUid;

    // Required no-arg constructor
    public Message() {}

    public Message(String senderId, String receiverId, Date sentDate, String content, String currentUid) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.sentDate = sentDate;
        this.content = content;
        this.currentUid = currentUid;
    }

    // Getters
    public String getContent() {
        return content;
    }
    public String getSenderId() {
        return senderId;
    }
    public String getReceiverId() {
        return receiverId;
    }
    public Date getLastMessageTime() {
        return sentDate;
    }
    public String getCurrentUid(){
        return currentUid;
    }

    // Setters
    public void setContent(String content) {
        this.content = content;
    }
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }
    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }
    public void setCurrentUid(String currentUid){
        this.currentUid = currentUid;
    }
}
