package com.akw.crimson.Adapters;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.Database.TheViewModel;
import com.akw.crimson.R;


public class Chat_RecyclerAdapter extends RecyclerView.Adapter<Chat_RecyclerAdapter.ViewHolder> {

    CursorAdapter mCursorAdapter;
    Chat_RecyclerAdapter.OnListItemClickListener mOnListItemClickListener;
    Context mContext;
    TheViewModel dbview;
    boolean active,unreadFound=false;
    public int unreadPosition;



    public Chat_RecyclerAdapter(Context context, Cursor c, Chat_RecyclerAdapter.OnListItemClickListener onImageClickListener, TheViewModel db, boolean active) {
        mOnListItemClickListener=onImageClickListener;
        mContext = context;
        dbview=db;
        this.active=active;

        //Log.i("Chat_RecyclerAdapter::::", "Constructor "+c.getCount());

        mCursorAdapter = new CursorAdapter(mContext, c, 0) {

            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                // Inflate the view here
                View view = LayoutInflater.from(context).inflate(R.layout.message_layout, parent, false);
                return view;
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                // Binding operations
                //ImageView img=view.findViewById(R.id.MediaListItem_ImageLayout);
            }
        };
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView tv_received_msg,tv_received_time,tv_sent_msg,tv_sent_time;
        //private TextView tv_unreadMsgs;
        private LinearLayout ll_message,ll_received,ll_sent;
//        private TextView tv_unreadCount;
//        private ImageView iv_userPic;

        Chat_RecyclerAdapter.OnListItemClickListener onListItemClickListener;

        public ViewHolder(View itemView, Chat_RecyclerAdapter.OnListItemClickListener onListItemClickListener) {
            super(itemView);
//            ll_message=itemView.findViewById(R.id.Message_Layout_Unread);
            tv_received_time = itemView.findViewById(R.id.Message_Received_time);
            tv_received_msg = itemView.findViewById(R.id.Message_Received_msgBox);
            tv_sent_msg=itemView.findViewById(R.id.Message_Sent_msgBox);
            tv_sent_time=itemView.findViewById(R.id.Message_Sent_time);
            ll_message=itemView.findViewById(R.id.Message_LinearLayout);
            ll_received=itemView.findViewById(R.id.Message_Received_LL);
            ll_sent=itemView.findViewById(R.id.Message_Sent_LL);
            this.onListItemClickListener=onListItemClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onListItemClickListener.onListItemClick(getAdapterPosition());
        }
    }


    @Override
    public int getItemCount() {
        return mCursorAdapter.getCount();
    }

    @Override
    public void onBindViewHolder(Chat_RecyclerAdapter.ViewHolder holder, int position) {
        // Passing the binding operation user_id cursor loader
        mCursorAdapter.getCursor().moveToPosition(position);
        mCursorAdapter.bindView(holder.itemView, mContext, mCursorAdapter.getCursor());

        Cursor cursor = (Cursor)mCursorAdapter.getItem(position);


        if (active && cursor.getInt(cursor.getColumnIndexOrThrow("unread"))==1 && !unreadFound){
            //unreadPosition=position;
            ConstraintLayout.LayoutParams lparams = new ConstraintLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lparams.setMargins(0, 0, 0, 15);
            TextView tv=new TextView(mContext.getApplicationContext());

            tv.setLayoutParams(lparams);
            tv.setText("Unread Messages");
            tv.setTextColor(Color.WHITE);
            tv.setPadding(25, 5, 25, 5);
            tv.setBackgroundResource(R.drawable.round_box_chat_input);
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            holder.ll_message.addView(tv,0);
            unreadFound=true;
        }
        if (cursor.getInt(cursor.getColumnIndexOrThrow("self"))==0) {
            holder.ll_received.setVisibility(View.VISIBLE);
            holder.tv_received_msg.setText(cursor.getString(cursor.getColumnIndexOrThrow("msg")));
            holder.tv_received_msg.setPadding(25, 1, 25, 1);
            holder.tv_received_time.setText(cursor.getString(cursor.getColumnIndexOrThrow("time")));
            holder.tv_received_time.setPadding(25, 1, 25, 1);
            holder.tv_sent_msg.setText("");
            holder.tv_sent_time.setText("");
            holder.ll_sent.setVisibility(View.GONE);
        } else {
            holder.tv_received_msg.setText("");
            holder.tv_received_time.setText("");
            holder.ll_received.setVisibility(View.GONE);
            holder.ll_sent.setVisibility(View.VISIBLE);
            holder.tv_sent_msg.setText(cursor.getString(cursor.getColumnIndexOrThrow("msg")));
            holder.tv_sent_msg.setPadding(25, 1, 25, 1);
            holder.tv_sent_time.setPadding(25, 1, 25, 1);
            holder.tv_sent_time.setText(cursor.getString(cursor.getColumnIndexOrThrow("time")));

        }
        Message msg=dbview.getMessage(cursor.getString(cursor.getColumnIndexOrThrow("local_msg_ID")));
        msg.setUnread(false);
        if(!msg.isSelf())msg.setStatus(3);
        dbview.updateMessage(msg);
        //Log.i("ALL ::::", mCursorAdapter.getCursor().getString(mCursorAdapter.getCursor().getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
        //holder.v1.setImageURI(Uri.parse(mCursorAdapter.getCursor().getString(mCursorAdapter.getCursor().getColumnIndexOrThrow(MediaStore.Images.Media.DATA))));

    }

    @Override
    public Chat_RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent);
        return new Chat_RecyclerAdapter.ViewHolder(v,mOnListItemClickListener);
    }
    public interface OnListItemClickListener {
        void onListItemClick(int position);
    }
    public void setOnItemClickListener(OnListItemClickListener listener) {
        this.mOnListItemClickListener = listener;
    }
}
//
//public class Chat_RecyclerAdapter extends ListAdapter<Message, Chat_RecyclerAdapter.MessageHolder> {
//    private OnItemClickListener listener;
//
//    public Chat_RecyclerAdapter() {
//        super(DIFF_CALLBACK_CHAT);
//    }
//
//    private static final DiffUtil.ItemCallback<Message> DIFF_CALLBACK_CHAT = new DiffUtil.ItemCallback<Message>() {
//            @Override
//            public boolean areItemsTheSame(Message oldItem, Message newItem) {
//                return oldItem.getLocal_msg_ID().equals(newItem.getLocal_msg_ID());
//            }
//
//            @Override
//            public boolean areContentsTheSame(Message oldItem, Message newItem) {
//                return oldItem.getStatus()==newItem.getStatus();
//            }
//        };
//
//    @NonNull
//        @Override
//        public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View itemView = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.message_received_layout, parent, false);
//        if(viewType==0){
//            itemView = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.message_sent_layout, parent, false);
//        }
//            return new MessageHolder(itemView);
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull MessageHolder holder, int position) {
//            Message user = getItem(position);
//            holder.tv_date.setText(user.getTime());
//            holder.tv_text.setText(user.getMsg());
//
//        }
//
//    @Override
//    public int getItemViewType(int position) {
//        if(getItem(position).isSelf()){return 0;}
//        return 1;
//    }
//
//    class MessageHolder extends RecyclerView.ViewHolder {
//        private TextView tv_text;
//        private TextView tv_date;
//        private LinearLayout ll_main;
//
//            public MessageHolder(View itemView) {
//                super(itemView);
//                if(getItemViewType()==0) {
//                    ll_main = itemView.findViewById(R.id.Message_Sent_LL);
//                    tv_date = itemView.findViewById(R.id.Message_Sent_time);
//                    tv_text = itemView.findViewById(R.id.Message_Sent_msgBox);
//                }else{
//                    ll_main = itemView.findViewById(R.id.Message_Received_LL);
//                    tv_date = itemView.findViewById(R.id.Message_Received_time);
//                    tv_text = itemView.findViewById(R.id.Message_Received_msgBox);
//                }
//
//                itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        int position = getAdapterPosition();
//                        if (listener != null && position != RecyclerView.NO_POSITION) {
//                            listener.onItemClick(getItem(position));
//                        }
//                    }
//                });
//            }
//        }
//
//        public Message getUserAt(int position) {
//            return getItem(position);
//        }
//
//        public interface OnItemClickListener {
//            void onItemClick(Message user);
//        }
//
//        public void setOnItemClickListener(OnItemClickListener listener) {
//            this.listener = listener;
//        }
//    }