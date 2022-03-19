package com.akw.crimson.Chat;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.AppObjects.Message;
import com.akw.crimson.AppObjects.Message;
import com.akw.crimson.R;


public class Chat_RecyclerAdapter extends ListAdapter<Message, Chat_RecyclerAdapter.MessageHolder> {
    private OnItemClickListener listener;

    public Chat_RecyclerAdapter() {
        super(DIFF_CALLBACK_CHAT);
    }

    private static final DiffUtil.ItemCallback<Message> DIFF_CALLBACK_CHAT = new DiffUtil.ItemCallback<Message>() {
            @Override
            public boolean areItemsTheSame(Message oldItem, Message newItem) {
                return oldItem.getLocal_msg_ID().equals(newItem.getLocal_msg_ID());
            }

            @Override
            public boolean areContentsTheSame(Message oldItem, Message newItem) {
                return oldItem.getStatus()==newItem.getStatus();
            }
        };

    @NonNull
        @Override
        public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_received_layout, parent, false);
        if(viewType==0){
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_sent_layout, parent, false);
        }
            return new MessageHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageHolder holder, int position) {
            Message user = getItem(position);
            holder.tv_date.setText(user.getTime());
            holder.tv_text.setText(user.getMsg());

        }

    @Override
    public int getItemViewType(int position) {
        if(getItem(position).isSelf()){return 0;}
        return 1;
    }

    class MessageHolder extends RecyclerView.ViewHolder {
        private TextView tv_text;
        private TextView tv_date;
        private LinearLayout ll_main;

            public MessageHolder(View itemView) {
                super(itemView);
                if(getItemViewType()==0) {
                    ll_main = itemView.findViewById(R.id.Message_Sent_LL);
                    tv_date = itemView.findViewById(R.id.Message_Sent_time);
                    tv_text = itemView.findViewById(R.id.Message_Sent_msgBox);
                }else{
                    ll_main = itemView.findViewById(R.id.Message_Received_LL);
                    tv_date = itemView.findViewById(R.id.Message_Received_time);
                    tv_text = itemView.findViewById(R.id.Message_Received_msgBox);
                }

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

    public Message getUserAt(int position) {
            return getItem(position);
        }
        public interface OnItemClickListener {
            void onItemClick(Message user);
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }
    }