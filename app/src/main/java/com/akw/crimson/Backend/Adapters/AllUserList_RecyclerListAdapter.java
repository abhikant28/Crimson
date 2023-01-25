package com.akw.crimson.Backend.Adapters;


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

public class AllUserList_RecyclerListAdapter extends ListAdapter<User, AllUserList_RecyclerListAdapter.MyViewHolder> {
    private OnItemClickListener listener;

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

    public AllUserList_RecyclerListAdapter() {
        super(DIFF_CALLBACK_User);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_name;
        private TextView tv_lastMsg;
        private TextView tv_unreadCount;
        private ImageView iv_profilePic;
        private TextView tv_time;

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
                    if (listener != null && p != -1) listener.OnItemClick(getUser(p));
                }
            });
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_chatlist_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        User user = getItem(position);

        holder.tv_name.setText(user.getDisplayName());
        holder.tv_time.setText("");
        holder.tv_unreadCount.setText("");
        holder.tv_lastMsg.setText("");
        holder.iv_profilePic.setImageBitmap(user.getPicBitmap());

        holder.iv_profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatActivity activity = (AppCompatActivity) holder.itemView.getContext();
                ProfileImageView update = new ProfileImageView();
                Bundle bundle = new Bundle();
                bundle.putString(Constants.KEY_INTENT_USERID, getUser(holder.getAbsoluteAdapterPosition()).getUser_id());
                bundle.putString(Constants.KEY_INTENT_PIC, getUser(holder.getAbsoluteAdapterPosition()).getPic());
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

    public interface OnItemClickListener {
        void OnItemClick(User User);
    }

    public void setOnItemCLickListener(AllUserList_RecyclerListAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
}
