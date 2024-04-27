package com.example.myapplication.interfaces;

public interface ConfirmFriendRequest {
    void onConfirmFriendRequest(String requsetId,String senderId,String receiveId,String username);
    void onCancelFriendRequest(String requsetId,String username);

}
