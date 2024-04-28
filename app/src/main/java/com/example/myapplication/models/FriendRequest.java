package com.example.myapplication.models;

import com.example.myapplication.utils.FirebaseUtils;

import java.util.UUID;

public class FriendRequest {
    private String id = UUID.randomUUID().toString();
    private String senderId = FirebaseUtils.currentUserID();
    private String receiverId;
    private String receiverName;
    private String senderName;

    public FriendRequest(String receiverId, String receiverName,String senderName) {
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.senderName = senderName;
    }

    public FriendRequest(String id, String senderId, String receiverId, String receiverName, String senderName) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.senderName = senderName;
    }

    public FriendRequest() {
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }
}
