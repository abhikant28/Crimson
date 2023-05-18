package com.akw.crimson.Backend.Adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.Database.TheViewModel;
import com.akw.crimson.R;

public class MessageSearch_RecyclerListAdapter extends ListAdapter<Message, MessageSearch_RecyclerListAdapter.MyViewHolder> {
    private static final DiffUtil.ItemCallback<Message> DIFF_CALLBACK_Message = new DiffUtil.ItemCallback<Message>() {
        @Override
        public boolean areItemsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
            return oldItem.getMsg_ID().equals(newItem.getMsg_ID());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
            return oldItem.getStatus() == newItem.getStatus();
        }
    };
    TheViewModel db;
    private OnItemClickListener listener;

    public MessageSearch_RecyclerListAdapter() {
        super(DIFF_CALLBACK_Message);
    }

    public MessageSearch_RecyclerListAdapter(TheViewModel db) {
        super(DIFF_CALLBACK_Message);
        this.db = db;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_chatlist_search_message_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Message message = getItem(position);

        String name = db.getUser(message.getUser_id()).getDisplayName();
        holder.tv_name.setText(name);
        holder.tv_lastMsg.setText(message.getMsg());
        holder.tv_time.setText((message.getTime() == null ? "12:00" : message.getTime().substring(0, 5)));
    }

    public Message getMessage(int position) {
        return getItem(position);
    }

    public void setOnItemCLickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void OnItemClick(Message Message);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView tv_name;
        private final TextView tv_lastMsg;
        private final TextView tv_time;

        public MyViewHolder(@NonNull View view) {
            super(view);

            tv_name = view.findViewById(R.id.MainSearch_Item_TextView_UserName);
            tv_lastMsg = view.findViewById(R.id.MainSearch_Item_TextView_UserMsg);
            tv_time = view.findViewById(R.id.MainSearch_Item_TextView_Time);

            itemView.setOnClickListener(view1 -> {
                int p = getAdapterPosition();
                if (listener != null && p != -1) listener.OnItemClick(getMessage(p));
            });
        }
    }

}
