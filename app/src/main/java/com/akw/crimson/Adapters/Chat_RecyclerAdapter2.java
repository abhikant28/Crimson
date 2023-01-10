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

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.Database.TheViewModel;
import com.akw.crimson.R;


public class Chat_RecyclerAdapter2 {

    static CursorAdapter mCursorAdapter;
    //    Chat_RecyclerAdapter2.OnListItemClickListener mOnListItemClickListener;
    Context mContext;
    TheViewModel dbview;
    boolean active, unreadFound = false;
    public int unreadPosition;

    static class SentMessageViewHolder extends RecyclerView.Adapter{

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            return null;

        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            TextView tv_sent_msg,tv_sent_time;

        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }

    public static class ReceivedMessageViewHolder extends RecyclerView.Adapter<ReceivedMessageViewHolder.ViewHolder> {

        CursorAdapter mCursorAdapter;
        ReceivedMessageViewHolder.OnListItemClickListener mOnListItemClickListener;
        Context mContext;
        TheViewModel dbview;
        boolean active, unreadFound = false;


        public ReceivedMessageViewHolder(Context context, Cursor c, ReceivedMessageViewHolder.OnListItemClickListener onImageClickListener, TheViewModel db, boolean active) {
            mOnListItemClickListener = onImageClickListener;
            mContext = context;
            dbview = db;
            this.active = active;

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

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            final private TextView tv_received_msg, tv_received_time;
            final private LinearLayout ll_message;

            ReceivedMessageViewHolder.OnListItemClickListener onListItemClickListener;

            public ViewHolder(View itemView, ReceivedMessageViewHolder.OnListItemClickListener onListItemClickListener) {
                super(itemView);
                tv_received_time = itemView.findViewById(R.id.Message_Received_time);
                tv_received_msg = itemView.findViewById(R.id.Message_Received_msgBox);
                ll_message = itemView.findViewById(R.id.Message_LinearLayout);
                this.onListItemClickListener = onListItemClickListener;
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
        public void onBindViewHolder(ReceivedMessageViewHolder.ViewHolder holder, int position) {
            // Passing the binding operation user_id cursor loader
            mCursorAdapter.getCursor().moveToPosition(position);
            mCursorAdapter.bindView(holder.itemView, mContext, mCursorAdapter.getCursor());

            Cursor cursor = (Cursor) mCursorAdapter.getItem(position);


            if (active && cursor.getInt(cursor.getColumnIndexOrThrow("unread")) == 1 && !unreadFound) {
                //unreadPosition=position;
                ConstraintLayout.LayoutParams lparams = new ConstraintLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lparams.setMargins(0, 0, 0, 15);
                TextView tv = new TextView(mContext.getApplicationContext());

                tv.setLayoutParams(lparams);
                tv.setText("Unread Messages");
                tv.setTextColor(Color.WHITE);
                tv.setPadding(25, 5, 25, 5);
                tv.setBackgroundResource(R.drawable.round_box_chat_input);
                tv.setGravity(Gravity.CENTER_HORIZONTAL);
                holder.ll_message.addView(tv, 0);
                unreadFound = true;
            }
            holder.tv_received_msg.setText(cursor.getString(cursor.getColumnIndexOrThrow("msg")));
            holder.tv_received_msg.setPadding(25, 1, 25, 1);
            holder.tv_received_time.setText(cursor.getString(cursor.getColumnIndexOrThrow("time")));
            holder.tv_received_time.setPadding(25, 1, 25, 1);

            Message msg = dbview.getMessage(cursor.getString(cursor.getColumnIndexOrThrow("local_msg_ID")));
            msg.setUnread(false);
            if (!msg.isSelf()) msg.setStatus(3);
            dbview.updateMessage(msg);
            //Log.i("ALL ::::", mCursorAdapter.getCursor().getString(mCursorAdapter.getCursor().getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
            //holder.v1.setImageURI(Uri.parse(mCursorAdapter.getCursor().getString(mCursorAdapter.getCursor().getColumnIndexOrThrow(MediaStore.Images.Media.DATA))));

        }

        @Override
        public ReceivedMessageViewHolder.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent);
            return new ReceivedMessageViewHolder.ViewHolder(v, mOnListItemClickListener);
        }

        public interface OnListItemClickListener {
            void onListItemClick(int position);
        }

        public void setOnItemClickListener(ReceivedMessageViewHolder.OnListItemClickListener listener) {
            this.mOnListItemClickListener = listener;
        }
    }


}
