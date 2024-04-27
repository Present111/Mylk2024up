package com.example.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.AddFriendAdapter;
import com.example.myapplication.adapter.FriendRequestAdapter;
import com.example.myapplication.interfaces.AddFriend;
import com.example.myapplication.interfaces.ConfirmFriendRequest;
import com.example.myapplication.models.Chatroom;
import com.example.myapplication.models.FriendRequest;
import com.example.myapplication.models.User;
import com.example.myapplication.utils.FirebaseUtils;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchUserActivity extends AppCompatActivity implements ConfirmFriendRequest, AddFriend {
    EditText searchInput;
    ImageButton backButton;
    private RecyclerView recyclerView, rcvReceive, rcvSend, rcvUser;

    private FriendRequestAdapter reciveAdapter, sendAdapter;
    private AddFriendAdapter addFriendAdapter, friendAdapter;
    private String searchTerm = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);
        initView();
        search();
        getListFiend();
    }

    private void getListFiend() {
        FirebaseUtils.currentUserDetail().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User user = task.getResult().toObject(User.class);
                if (user != null && user.getFriends() != null) {
                    for (int i = 0; i < user.getFriends().size(); i++) {
                        FirebaseUtils.allUserCollectionReference().document(user.getFriends().get(i)).get().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                User friend = task1.getResult().toObject(User.class);
                                if (friend != null) {
                                    friendAdapter.addItem(friend);
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private List<User> filterFriends(List<User> data) {
        ArrayList<User> newList = new ArrayList<>(data);
        for (int i = 0; i < data.size(); i++) {
            for (User user : friendAdapter.getList()) {
                if (data.get(i).getUserId().equals(user.getUserId())) {
                    newList.remove(data.get(i));
                    break;
                }
            }
        }
        return newList;
    }

    private void search() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchTerm = searchInput.getText().toString().trim();
                if (searchTerm.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    rcvUser.setVisibility(View.VISIBLE);
                }
                getFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initView() {
        searchInput = findViewById(R.id.seach_username_input);
        recyclerView = findViewById(R.id.search_user_recycler_view);
        addFriendAdapter = new AddFriendAdapter(this);
        recyclerView.setAdapter(addFriendAdapter);
        rcvReceive = findViewById(R.id.rcv_receive);
        rcvSend = findViewById(R.id.rcv_send);
        reciveAdapter = new FriendRequestAdapter(this);
        rcvReceive.setAdapter(reciveAdapter);
        sendAdapter = new FriendRequestAdapter(this);
        rcvSend.setAdapter(sendAdapter);
        rcvUser = findViewById(R.id.user_recycler_view);
        friendAdapter = new AddFriendAdapter(true, this);
        rcvUser.setAdapter(friendAdapter);
    }


    private void getFilter() {
        if (searchTerm.trim().isEmpty()) return;
        FirebaseUtils.allUserCollectionReference()
                .whereNotEqualTo("userId", FirebaseUtils.currentUserID())
                .whereGreaterThanOrEqualTo("username", searchTerm.trim())
                .whereLessThanOrEqualTo("username", searchTerm.trim() + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<User> list = task.getResult().toObjects(User.class);
                        List<User> list1 = sendAdapter.filterList(list);
                        List<User> list2 = reciveAdapter.filterList(list1);
                        List<User> list3 = filterFriends(list2);
                        addFriendAdapter.setData(list3);
                        recyclerView.setVisibility(View.VISIBLE);
                        rcvUser.setVisibility(View.GONE);
                    } else {
                        if (task.getException() != null) {
                            task.getException().printStackTrace();
                        }
                    }
                });
    }


    @Override
    protected void onResume() {
        super.onResume();
        getRecive();
        getSend();
    }

    private void getSend() {
        Thread thread = new Thread(() -> {
            FirebaseUtils.InviteReference()
                    .whereEqualTo("senderId", FirebaseUtils.currentUserID())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            sendAdapter.setData(task.getResult().toObjects(FriendRequest.class));
                        } else {
                            if (task.getException() != null) {
                                task.getException().printStackTrace();
                            }
                        }
                    });
        });

        thread.start();
    }

    private void getRecive() {
        Thread thread = new Thread(() -> {
            FirebaseUtils.InviteReference()
                    .whereEqualTo("receiverId", FirebaseUtils.currentUserID())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            reciveAdapter.setData(task.getResult().toObjects(FriendRequest.class));
                        } else {
                            if (task.getException() != null) {
                                task.getException().printStackTrace();
                            }
                        }
                    });

        });


        thread.start();
    }

    @Override
    public void onConfirmFriendRequest(String requsetId, String senderId, String receiveId, String senderName) {
        reciveAdapter.removeItem(requsetId);
        FirebaseUtils.allUserCollectionReference().document(senderId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User user = task.getResult().toObject(User.class);
                friendAdapter.addItem(user);
            }
        });

        if (senderName.contains(searchTerm)) getFilter();
        FirebaseUtils.InviteReference().document(requsetId).delete();
        FirebaseUtils.InviteReference()
                .whereEqualTo("senderId", receiveId)
                .whereEqualTo("receiverId", senderId)
                .get()
                .addOnSuccessListener(
                        queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                    documentSnapshot.getReference().delete();
                                }
                            }
                        }
                );
        FirebaseUtils.currentUserDetail().update("friends", FieldValue.arrayUnion(senderId));
        FirebaseUtils.allUserCollectionReference().document(senderId).update("friends", FieldValue.arrayUnion(receiveId));
    }

    @Override
    public void onCancelFriendRequest(String requsetId, String reciveName) {
        sendAdapter.removeItem(requsetId);
        if (reciveName.contains(searchTerm)) getFilter();
        FirebaseUtils.InviteReference().document(requsetId).delete();
    }


    @Override
    public void onAddFriend(String userId, String username) {
        sendRequest(userId, username);
    }

    @Override
    public void onClick(User user) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("username", user.getUsername());
        intent.putExtra("userId", user.getUserId());
        intent.putExtra("phone", user.getPhone());
        startActivity(intent);
    }

    private void sendRequest(String userId, String username) {
        FirebaseUtils.currentUserDetail().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User user = task.getResult().toObject(User.class);
                FriendRequest friendRequest = new FriendRequest(userId, username, user.getUsername());
                sendAdapter.addItem(friendRequest);
                addFriendAdapter.removeItem(userId);
                FirebaseUtils.InviteReference().document(friendRequest.getId()).set(friendRequest);
            } else {
                if (task.getException() != null) {
                    Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("searchTerm", searchTerm);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        searchTerm = savedInstanceState.getString("searchTerm");
        if (searchTerm != null && !searchTerm.isEmpty()) {
            getFilter();
        }
    }
}
