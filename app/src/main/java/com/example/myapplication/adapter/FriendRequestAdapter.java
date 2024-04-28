package com.example.myapplication.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.interfaces.ConfirmFriendRequest;
import com.example.myapplication.models.FriendRequest;
import com.example.myapplication.models.User;
import com.example.myapplication.utils.Convert;
import com.example.myapplication.utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FriendRequestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<FriendRequest> list = new ArrayList<>();
    private ConfirmFriendRequest confirmFriendRequest;

    public List<User> filterList(List<User> users) {
        if (list.isEmpty() || users.isEmpty()) {
            return users;
        }
        List<User> newList = new ArrayList<>(users);
        for (User user : users) {
            for (FriendRequest friendRequest : list) {
                if (user.getUserId().equals(friendRequest.getReceiverId()) || user.getUserId().equals(friendRequest.getSenderId())) {
                    newList.remove(user);
                    break;
                }
            }
        }
        return newList;
    }

    public FriendRequestAdapter(ConfirmFriendRequest confirmFriendRequest) {
        this.confirmFriendRequest = confirmFriendRequest;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<FriendRequest> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (Objects.equals(list.get(position).getReceiverId(), FirebaseUtils.currentUserID())) {
            return 0;
        }
        return 1;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_request_layout, parent, false);
            return new FriendRequestViewHolder(view);
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_sender_layout, parent, false);
        return new SenderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FriendRequest friendRequest = list.get(position);
        if (holder instanceof FriendRequestViewHolder) {
            ((FriendRequestViewHolder) holder).tvAvatar.setText(Convert.convertName(friendRequest.getSenderName()));
            ((FriendRequestViewHolder) holder).tvName.setText(friendRequest.getSenderName());
            ((FriendRequestViewHolder) holder).btnConfirm.setOnClickListener(v -> {
                confirmFriendRequest.onConfirmFriendRequest(friendRequest.getId(), friendRequest.getSenderId(), friendRequest.getReceiverId(), friendRequest.getSenderName());
            });
        } else {
            ((SenderViewHolder) holder).tvAvatar.setText(Convert.convertName(friendRequest.getReceiverName()));
            ((SenderViewHolder) holder).tvName.setText(friendRequest.getReceiverName());
            ((SenderViewHolder) holder).btnDenied.setOnClickListener(v -> {
                confirmFriendRequest.onCancelFriendRequest(friendRequest.getId(), friendRequest.getReceiverName());
            });
        }
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void removeItem(String inviteId) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(inviteId)) {
                list.remove(i);
                notifyItemRemoved(i);
                notifyItemRangeChanged(i, list.size());
                break;
            }
        }
    }

    public void addItem(FriendRequest friendRequest) {
        list.add(friendRequest);
        notifyItemInserted(list.size() - 1);
    }

    public class FriendRequestViewHolder extends RecyclerView.ViewHolder {
        private TextView tvAvatar, tvName;
        private Button btnConfirm;

        public FriendRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tv_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            btnConfirm = itemView.findViewById(R.id.btn_confirm);
        }
    }

    public static class SenderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvAvatar, tvName;
        private ImageButton btnDenied;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tv_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            btnDenied = itemView.findViewById(R.id.btn_denied);
        }
    }
}
