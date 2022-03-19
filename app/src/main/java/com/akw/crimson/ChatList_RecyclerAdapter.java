package com.akw.crimson;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.AppObjects.User;


public class ChatList_RecyclerAdapter extends ListAdapter<User, ChatList_RecyclerAdapter.UserHolder> {
    private OnItemClickListener listener;

    public ChatList_RecyclerAdapter() {
        super(DIFF_CALLBACK_CHATLIST);
    }

    private static final DiffUtil.ItemCallback<User> DIFF_CALLBACK_CHATLIST = new DiffUtil.ItemCallback<User>() {
            @Override
            public boolean areItemsTheSame(User oldItem, User newItem) {
                return oldItem.getUser_id().equals(newItem.getUser_id());
            }

            @Override
            public boolean areContentsTheSame(User oldItem, User newItem) {
                return oldItem.getLast_msg().equals(newItem.getLast_msg());
            }
        };


    @NonNull
        @Override
        public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.main_chat_list_item, parent, false);
            return new UserHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull UserHolder holder, int position) {
            User user = getItem(position);
            holder.tv_userName.setText(user.getName());
            holder.tv_lastMsg.setText(user.getLast_msg());
            holder.tv_time.setText(user.getTime());
            holder.tv_unreadCount.setText(user.getUnread_count());
            if(user.getUnread_count()==0){
                holder.tv_unreadCount.setText("");
            }

        }


        class UserHolder extends RecyclerView.ViewHolder {
            private TextView tv_userName;
            private TextView tv_lastMsg;
            private TextView tv_time;
            private TextView tv_unreadCount;
            private ImageView iv_userPic;

            public UserHolder(View itemView) {
                super(itemView);
                tv_userName = itemView.findViewById(R.id.MainChatList_Item_TextView_UserName);
                tv_lastMsg = itemView.findViewById(R.id.MainChatList_Item_TextView_UserMsg);
                tv_time = itemView.findViewById(R.id.MainChatList_Item_TextView_Time);
                tv_unreadCount = itemView.findViewById(R.id.MainChatList_Item_TextView_UnreadCount);
                iv_userPic= itemView.findViewById(R.id.MainChatList_Item_ImageView_UserPic);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                        if (listener != null && position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(getItem(position));
                        }
                    }
                });
            }
        }
        public User getUserAt(int position) {
            return getItem(position);
        }
        public interface OnItemClickListener {
            void onItemClick(User user);
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }
    }