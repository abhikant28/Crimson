package com.akw.crimson.Backend.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.R;

public class UserDisplay_RecyclerViewAdapter extends ListAdapter<User, UserDisplay_RecyclerViewAdapter.MyViewHolder> {
    private OnItemClickListener listener;
    private boolean firstDifferent=false;

    private static final DiffUtil.ItemCallback<User> DIFF_CALLBACK_User = new DiffUtil.ItemCallback<User>() {
        @Override
        public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getUser_id().equals(newItem.getUser_id());
        }

        @Override
        public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return false;
        }
    };

    public UserDisplay_RecyclerViewAdapter() {
        super(DIFF_CALLBACK_User);
    }

    public UserDisplay_RecyclerViewAdapter(boolean addUser) {
        super(DIFF_CALLBACK_User);
        this.firstDifferent=addUser;
    }
    @NonNull
    @Override
    public UserDisplay_RecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_list_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UserDisplay_RecyclerViewAdapter.MyViewHolder holder, int i) {
        if(firstDifferent && i==0){
            holder.iv_profile.setImageResource(R.drawable.ic_baseline_add_24);
            holder.iv_profile.setOnClickListener(view->{
                Log.i("First    Element :::::", "Found"+i+"_"+(listener != null));
                if (listener != null) listener.OnItemClick(null,true);
            });
            holder.tv_userName.setVisibility(View.GONE);
            holder.iv_close.setVisibility(View.GONE);
            return;
        }
        int position = firstDifferent?i-1:i;
        User user = getItem(position);
        Log.i(" Element :::::", position+"_");
//        holder.iv_profile.setImageResource(R.drawable.ic_baseline_download_24);
        holder.iv_profile.setImageBitmap(UsefulFunctions.getCircularBitmap(UsefulFunctions.decodeImage(user.getPublicPic())));
        holder.iv_close.setOnClickListener(view -> {
            if (listener != null && position != -1) listener.OnItemClick(getItem(position), false);
        });
        holder.tv_userName.setText(user.getDisplayName());


    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView tv_userName;
        private final ImageView iv_profile, iv_close;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_userName = itemView.findViewById(R.id.UserDisplay_listItem_tv_userName);
            iv_close = itemView.findViewById(R.id.UserDisplay_listItem_ib_close);
            iv_profile = itemView.findViewById(R.id.UserDisplay_listItem_ib_img);
        }
    }

    public void setOnItemCLickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void OnItemClick(User User, boolean b);
    }

    @Override
    public int getItemCount() {
        if(firstDifferent)
            return super.getItemCount()+1;
        return super.getItemCount();
    }
}
