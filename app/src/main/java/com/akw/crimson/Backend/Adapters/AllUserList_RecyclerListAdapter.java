package com.akw.crimson.Backend.Adapters;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.ProfileImageView;
import com.akw.crimson.R;
import com.akw.crimson.Utilities.SelectContact;

public class AllUserList_RecyclerListAdapter extends ListAdapter<User, AllUserList_RecyclerListAdapter.MyViewHolder> {
    private static final DiffUtil.ItemCallback<User> DIFF_CALLBACK_User = new DiffUtil.ItemCallback<User>() {
        @Override
        public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getUser_id().equals(newItem.getUser_id());
        }

        @Override
        public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getUnread_count() == newItem.getUnread_count();
        }
    };
    Context cxt;
    private OnItemClickListener listener;
    private boolean multi = false;

    public AllUserList_RecyclerListAdapter() {
        super(DIFF_CALLBACK_User);
    }

    public AllUserList_RecyclerListAdapter(boolean multi, Context cxt) {
        super(DIFF_CALLBACK_User);
        this.multi = multi;
        this.cxt = cxt;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_chatlist_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.tv_time.setVisibility(View.GONE);
        holder.tv_unreadCount.setVisibility(View.GONE);
        User user = getItem(position);
        holder.tv_name.setText(user.getDisplayName());
        holder.tv_lastMsg.setText(user.getAbout());
        holder.iv_profilePic.setImageBitmap(user.getUserPic(cxt));
        if (SelectContact.selectedUsers.contains(user)) {
            holder.itemView.setBackgroundColor(Color.parseColor("#C6C5C5"));
            holder.tv_lastMsg.setTextColor(Color.BLACK);
            holder.tv_name.setTextColor(Color.BLACK);
        } else {
            holder.itemView.setBackgroundColor(Color.BLACK);
            holder.tv_lastMsg.setTextColor(Color.WHITE);
            holder.tv_name.setTextColor(Color.WHITE);
        }


        holder.iv_profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatActivity activity = (AppCompatActivity) holder.itemView.getContext();
                ProfileImageView update = new ProfileImageView();
                Bundle bundle = new Bundle();
                bundle.putString(Constants.Intent.KEY_INTENT_USERID, getUser(holder.getAbsoluteAdapterPosition()).getUser_id());
                bundle.putString(Constants.Intent.KEY_INTENT_PIC, getUser(holder.getAbsoluteAdapterPosition()).getPublicPic());
                update.setArguments(bundle);
                update.show(activity.getSupportFragmentManager().beginTransaction(), "EXAMPLE");
                holder.tv_unreadCount.setText("");
                holder.tv_time.setText("");
            }

        });

    }

    public User getUser(int position) {
        return getItem(position);
    }

    public void setOnItemCLickListener(AllUserList_RecyclerListAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void OnItemClick(User User, TextView tv_name, TextView tv_lastMsg, View view);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView tv_name;
        private final TextView tv_lastMsg;
        private final TextView tv_unreadCount;
        private final ImageView iv_profilePic;
        private final TextView tv_time;

        public MyViewHolder(@NonNull View view) {
            super(view);

            tv_name = view.findViewById(R.id.MainChatList_Item_TextView_UserName);
            tv_lastMsg = view.findViewById(R.id.MainChatList_Item_TextView_UserMsg);
            tv_unreadCount = view.findViewById(R.id.MainChatList_Item_TextView_UnreadCount);
            iv_profilePic = view.findViewById(R.id.MainChatList_Item_ImageView_UserPic);
            tv_time = view.findViewById(R.id.MainChatList_Item_TextView_Time);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int p = getAdapterPosition();
                    if (listener != null && p != -1)
                        listener.OnItemClick(getUser(p), tv_name, tv_lastMsg, view);
                }
            });
        }
    }
}
