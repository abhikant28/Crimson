package com.akw.crimson.Backend.Adapters;


import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
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
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.ProfileImageView;
import com.akw.crimson.R;

public class ChatList_RecyclerListAdapter extends ListAdapter<User, ChatList_RecyclerListAdapter.MyViewHolder> {
    private static final DiffUtil.ItemCallback<User> DIFF_CALLBACK_User = new DiffUtil.ItemCallback<User>() {
        @Override
        public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getUser_id().equals(newItem.getUser_id());
        }

        @Override
        public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getUnread_count() == newItem.getUnread_count() && oldItem.getTime().equals(newItem.getTime()) && oldItem.getDisplayName().equals(newItem.getDisplayName());
        }
    };
    private OnItemClickListener listener;

    public ChatList_RecyclerListAdapter() {
        super(DIFF_CALLBACK_User);
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
        holder.tv_lastMsg.setText(user.getLast_msg());
        holder.iv_profilePic.setImageBitmap(user.getUserPicBitmap(holder.itemView.getContext()));

        if (user.getUnread_count() > 0) {
            holder.tv_unreadCount.setVisibility(View.VISIBLE);
            holder.tv_unreadCount.setText(String.valueOf(user.getUnread_count()));
        }
        if(user.getLast_msg()==null) holder.tv_lastMsg.setTypeface(null, Typeface.ITALIC);
        holder.tv_time.setText(UsefulFunctions.getTimeHhMm(user.getTime()));
        if (user.getLast_msg_media_type() != Constants.Media.KEY_MESSAGE_MEDIA_TYPE_NONE) {
            switch (user.getLast_msg_media_type()) {
                case Constants.Media.KEY_MESSAGE_MEDIA_TYPE_VIDEO:
                    Log.e("VID:::::", user.getLast_msg_media_type() + "");
                    holder.tv_lastMsg.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_twotone_videocam_24, 0, 0, 0);
                    if (user.getLast_msg() == null) holder.tv_lastMsg.setText(" Video");
                    break;
                case Constants.Media.KEY_MESSAGE_MEDIA_TYPE_IMAGE:
                    Log.e("IMG:::::", user.getLast_msg_media_type() + "");
                    holder.tv_lastMsg.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_twotone_insert_photo_24, 0, 0, 0);
                    if (user.getLast_msg() == null) holder.tv_lastMsg.setText(" Photo");
                    break;
                case Constants.Media.KEY_MESSAGE_MEDIA_TYPE_AUDIO:
                    Log.i("AUD:::::", user.getLast_msg_media_type() + "");
                    holder.tv_lastMsg.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_headphones_24, 0, 0, 0);
                    if (user.getLast_msg() == null) holder.tv_lastMsg.setText(" Audio");
                    break;
                case Constants.Media.KEY_MESSAGE_MEDIA_TYPE_DOCUMENT:
                    Log.i("DOC:::::", user.getLast_msg_media_type() + "");
                    holder.tv_lastMsg.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_twotone_insert_drive_file_24, 0, 0, 0);
                    if (user.getLast_msg() == null) holder.tv_lastMsg.setText(" Document");
                    break;
            }
        } else {
            holder.tv_lastMsg.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
    }

    public User getUser(int position) {
        return getItem(position);
    }

    public void setOnItemCLickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void OnItemClick(User User);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView tv_name, tv_lastMsg, tv_unreadCount, tv_time;
        private final ImageView iv_profilePic;

        public MyViewHolder(@NonNull View view) {
            super(view);

            tv_name = view.findViewById(R.id.MainChatList_Item_TextView_UserName);
            tv_lastMsg = view.findViewById(R.id.MainChatList_Item_TextView_UserMsg);
            tv_unreadCount = view.findViewById(R.id.MainChatList_Item_TextView_UnreadCount);
            iv_profilePic = view.findViewById(R.id.MainChatList_Item_ImageView_UserPic);
            tv_time = view.findViewById(R.id.MainChatList_Item_TextView_Time);

            itemView.setOnClickListener(view12 -> {
                int p = getAdapterPosition();
                if (listener != null && p != -1) listener.OnItemClick(getUser(p));
            });
            iv_profilePic.setOnClickListener(view1 -> {
                AppCompatActivity activity = (AppCompatActivity) view1.getContext();
                ProfileImageView update = new ProfileImageView();
                Bundle bundle = new Bundle();
                bundle.putString(Constants.Intent.KEY_INTENT_USERID, getUser(getLayoutPosition()).getUser_id());
                bundle.putString(Constants.Intent.KEY_INTENT_PIC,getUser(getLayoutPosition()).getUserPic());
                update.setArguments(bundle);
                update.show(activity.getSupportFragmentManager().beginTransaction(), "EXAMPLE");

            });
        }
    }

}
