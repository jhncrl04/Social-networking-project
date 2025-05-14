package com.example.fakebook;

import java.util.Date;

public class Message {
    private String messageId;
    private String senderName;
    private Date lastUpdated;
    private String lastMessage;

    public Message(){}

    public Message(String messageId, String senderName, Date lastUpdated, String lastMessage) {
        this.messageId = messageId;
        this.senderName = senderName;
        this.lastUpdated = lastUpdated;
        this.lastMessage = lastMessage;
    }

    // Getters
    public String getSenderName() { return senderName; }
    public String getMessageId() { return messageId; }
    public String getLastMessage() { return lastMessage; }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}
