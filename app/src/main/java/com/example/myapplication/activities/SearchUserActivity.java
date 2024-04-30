package com.example.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.AddFriendAdapter;
import com.example.myapplication.adapter.FriendRequestAdapter;
import com.example.myapplication.interfaces.AddFriend;
import com.example.myapplication.interfaces.ConfirmFriendRequest;
import com.example.myapplication.models.FriendRequest;
import com.example.myapplication.models.User;
import com.example.myapplication.utils.FirebaseUtils;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchUserActivity extends AppCompatActivity implements ConfirmFriendRequest, AddFriend {
    EditText searchInput;
    ImageButton backButton;
    private RecyclerView recyclerView, rcvReceive, rcvSend, rcvUser;

    private FriendRequestAdapter reciveAdapter, sendAdapter;
    private AddFriendAdapter addFriendAdapter, friendAdapter;
    private String searchTerm = "";

    private Thread threadFilter;


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
                if (threadFilter != null) {
                    if (threadFilter.isAlive()) threadFilter.interrupt();
                    threadFilter = null;
                }
                if (TextUtils.isEmpty(s)) {
                    recyclerView.setVisibility(View.GONE);
                    rcvUser.setVisibility(View.VISIBLE);
                    return;
                }
                recyclerView.setVisibility(View.VISIBLE);
                rcvUser.setVisibility(View.GONE);
                searchTerm = s.toString().trim();
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
        threadFilter = new Thread(() -> {
            try {
                Thread.sleep(100L);
                FirebaseUtils.allUserCollectionReference()
                        .whereNotEqualTo("userId", FirebaseUtils.currentUserID())
                        .whereGreaterThanOrEqualTo("username", searchTerm.trim())
                        .whereLessThanOrEqualTo("username", searchTerm.trim() + "\uf8ff")
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (searchTerm.trim().isEmpty()) return;
                                List<User> list = task.getResult().toObjects(User.class);
                                List<User> list1 = sendAdapter.filterList(list);
                                List<User> list2 = reciveAdapter.filterList(list1);
                                List<User> list3 = filterFriends(list2);
                                runOnUiThread(() -> {
                                    addFriendAdapter.setData(list3);
                                });
                            } else {
                                if (task.getException() != null) {
                                    task.getException().printStackTrace();
                                }
                            }
                        });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });
        threadFilter.start();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận kết bạn với " + senderName + "?");
        builder.setMessage("Chọn đồng ý để thêm bạn");
        builder.setNegativeButton("Từ chối", (dialog, which) ->{
            reciveAdapter.removeItem(requsetId);
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
        } );
        builder.setPositiveButton("Đồng ý", (dialog, which) -> accept(requsetId, senderId, receiveId, senderName));
        builder.create().show();
    }

    private void accept(String requsetId, String senderId, String receiveId, String senderName) {
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
    public void onAddFriend(String userId, String username, int position) {
        sendRequest(userId, username, position);
    }

    @Override
    public void onClick(User user) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("username", user.getUsername());
        intent.putExtra("userId", user.getUserId());
        intent.putExtra("phone", user.getPhone());
        startActivity(intent);
    }

    @Override
    public void unFriend(String userId, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xóa bạn bè");
        builder.setMessage("Xác nhận xóa bạn bè");
        builder.setNegativeButton("Hủy", null);
        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            friendAdapter.removeItemAt(position);
            FirebaseUtils.currentUserDetail().update("friends", FieldValue.arrayRemove(userId));
            FirebaseUtils.allUserCollectionReference().document(userId).update("friends", FieldValue.arrayRemove(FirebaseUtils.currentUserID()));
            List<String> userIds = new ArrayList<>();
            userIds.add(FirebaseUtils.currentUserID());
            userIds.add(userId);
            FirebaseUtils.allChatroomCollectionReference()
                    .whereEqualTo("userIds", userIds)
                    .get().addOnSuccessListener(queryDocumentSnapshots -> {
                        queryDocumentSnapshots.getDocuments().forEach(documentSnapshot -> {
                            documentSnapshot.getReference().delete();
                        });
                    });
        });
        builder.create().show();
    }

    private void sendRequest(String userId, String username, int position) {
        FirebaseUtils.currentUserDetail().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User user = task.getResult().toObject(User.class);
                FriendRequest friendRequest = new FriendRequest(userId, username, user.getUsername());
                sendAdapter.addItem(friendRequest);
                addFriendAdapter.removeItemAt(position);
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
