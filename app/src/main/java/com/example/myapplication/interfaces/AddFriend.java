package com.example.myapplication.interfaces;

import com.example.myapplication.models.User;

public interface AddFriend {
    void onAddFriend(String userId,String username,int position);

    void onClick(User user);

    void unFriend(String userId,int position);
}
