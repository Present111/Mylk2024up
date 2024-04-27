package com.example.myapplication.interfaces;

import com.example.myapplication.models.User;

public interface AddFriend {
    void onAddFriend(String userId,String username);

    void onClick(User user);
}
