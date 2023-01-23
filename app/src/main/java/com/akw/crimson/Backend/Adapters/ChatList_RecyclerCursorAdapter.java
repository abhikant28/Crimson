package com.akw.crimson.Backend.Adapters;


import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cursoradapter.widget.CursorAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.R;


public class ChatList_RecyclerCursorAdapter extends RecyclerView.Adapter<ChatList_RecyclerCursorAdapter.ViewHolder> {

    CursorAdapter mCursorAdapter;
    OnListItemClickListener mOnListItemClickListener;
    Context mContext;


    public ChatList_RecyclerCursorAdapter(Context context, Cursor c, OnListItemClickListener onImageClickListener) {
        mOnListItemClickListener = onImageClickListener;
        mContext = context;
        mCursorAdapter = new CursorAdapter(mContext, c, 0) {

            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                View view = LayoutInflater.from(context).inflate(R.layout.main_chatlist_item, parent, false);
                return view;
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
            }
        };
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView tv_userName;
        private TextView tv_lastMsg;
        private TextView tv_time;
        private TextView tv_unreadCount;
        private ImageView iv_userPic;

        OnListItemClickListener onListItemClickListener;

        public ViewHolder(View itemView, OnListItemClickListener onListItemClickListener) {
            super(itemView);
            tv_userName = itemView.findViewById(R.id.MainChatList_Item_TextView_UserName);
            tv_lastMsg = itemView.findViewById(R.id.MainChatList_Item_TextView_UserMsg);
            tv_time = itemView.findViewById(R.id.MainChatList_Item_TextView_Time);
            tv_unreadCount = itemView.findViewById(R.id.MainChatList_Item_TextView_UnreadCount);
            iv_userPic = itemView.findViewById(R.id.MainChatList_Item_ImageView_UserPic);

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
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Passing the binding operation user_id cursor loader
        mCursorAdapter.getCursor().moveToPosition(position);
        mCursorAdapter.bindView(holder.itemView, mContext, mCursorAdapter.getCursor());

        Cursor cursor = (Cursor) mCursorAdapter.getItem(position);


        holder.tv_userName.setText(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        holder.tv_lastMsg.setText(cursor.getString(cursor.getColumnIndexOrThrow("last_msg")));
        holder.tv_time.setText(cursor.getString(cursor.getColumnIndexOrThrow("time")));
        holder.tv_unreadCount.setText(cursor.getString(cursor.getColumnIndexOrThrow("unread_count")));

        //Picasso.with(mContext).load(cursor.getString(cursor.getColumnIndexOrThrow("pic"))).into(holder.iv_userPic);
        if (cursor.getInt(cursor.getColumnIndexOrThrow("unread_count")) == 0) {
            holder.tv_unreadCount.setText("");
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Passing the inflater job user_id the cursor-adapter
        View v = mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent);
        return new ViewHolder(v, mOnListItemClickListener);
    }

    public interface OnListItemClickListener {
        void onListItemClick(int position);
    }

    public void setOnItemClickListener(OnListItemClickListener listener) {
        this.mOnListItemClickListener = listener;
    }
}