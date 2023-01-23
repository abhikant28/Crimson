package com.akw.crimson.Backend.Adapters;


import android.content.Context;
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
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.ProfileImageView;
import com.akw.crimson.R;

public class ChatList_RecyclerListAdapter extends ListAdapter<User, ChatList_RecyclerListAdapter.MyViewHolder> {
    private OnItemClickListener listener;
    private Context cxt;

    private static final DiffUtil.ItemCallback<User> DIFF_CALLBACK_User = new DiffUtil.ItemCallback<User>() {
        @Override
        public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getUser_id().equals(newItem.getUser_id());
        }

        @Override
        public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getUnread_count() == newItem.getUnread_count() && oldItem.getDisplayName().equals(newItem.getDisplayName()) && oldItem.getPic().equals(newItem.getPic()) && oldItem.getTime().equals(newItem.getTime());
        }
    };

    public ChatList_RecyclerListAdapter() {
        super(DIFF_CALLBACK_User);
    }
    public ChatList_RecyclerListAdapter(Context cxt) {
        super(DIFF_CALLBACK_User);
        this.cxt=cxt;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_name,tv_lastMsg,tv_unreadCount;
        private ImageView iv_profilePic;
        private TextView tv_time;

        public MyViewHolder(@NonNull View view) {
            super(view);

            tv_name = view.findViewById(R.id.MainChatList_Item_TextView_UserName);
            tv_lastMsg = view.findViewById(R.id.MainChatList_Item_TextView_UserMsg);
            tv_unreadCount = view.findViewById(R.id.MainChatList_Item_TextView_UnreadCount);
            iv_profilePic = view.findViewById(R.id.MainChatList_Item_ImageView_UserPic);
            tv_time=view.findViewById(R.id.MainChatList_Item_TextView_Time);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int p = getAdapterPosition();
                    if (listener != null && p != -1) listener.OnItemClick(getUser(p));
                }
            });
            iv_profilePic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AppCompatActivity activity = (AppCompatActivity) view.getContext();
                    ProfileImageView update= new ProfileImageView();
                    Bundle bundle=new Bundle();
                    bundle.putString(Constants.KEY_INTENT_USERID,getUser(getLayoutPosition()).getUser_id());
                    bundle.putString(Constants.KEY_INTENT_PIC,getUser(getLayoutPosition()).getPic());
                    update.setArguments(bundle);
                    update.show(activity.getSupportFragmentManager().beginTransaction(), "EXAMPLE");
//                    DialogFragment fragment = ProfileImageView.newInstance(getUser(getAdapterPosition()).getUser_id(), getUser(getLayoutPosition()).getPic());
//                    ViewCompat.setTransitionName(iv_profilePic, "item_image");
//                    activity.getSupportFragmentManager().beginTransaction()
//                            .addSharedElement(iv_profilePic, "max")
//                            .replace(R.id.MainChat_frame_profilePic, fragment)
//                            .addToBackStack(null)
//                            .commit();
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
        holder.tv_lastMsg.setText(user.getLast_msg());
        if(user.getPic()!=null){
            holder.iv_profilePic.setImageBitmap(UsefulFunctions.decodeImage(user.getPic()));
        }else{
            holder.iv_profilePic.setImageResource(R.drawable.ic_baseline_person_24);
        }
        holder.tv_unreadCount.setText(String.valueOf((user.getUnread_count())!=0?user.getUnread_count():""));
        holder.tv_time.setText((user.getTime()==null?"12:00":String.valueOf(user.getTime().substring(0,5))));
    }

    public User getUser(int position) {
        return getItem(position);
    }

    public interface OnItemClickListener {
        void OnItemClick(User User);
    }

    public void setOnItemCLickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

//    public void setNextAlert(User User, Context cxt) {
//        Calendar date = User.getDate();
//
//        switch (User.getType()) {
//            case "monthly":
//                date.add(Calendar.MONTH, 1);
//                break;
//            case "daily":
//                date.add(Calendar.DATE, 1);
//                break;
//            case "yearly":
//                date.add(Calendar.YEAR, 1);
//                break;
//            case "weekly":
//                date.add(Calendar.DATE, 7);
//        }
//
//        User.setDate(date);
//        Intent intent = new Intent(cxt.getApplicationContext(), AlertReceiver.class);
//        intent.putExtra("title", "Reminder : ");
//        intent.putExtra("body", User.getTitle());
//        viewModel.update(User);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(cxt.getApplicationContext(), User.getT_id(), intent, 0);
//        AlarmManager alarmManager = (AlarmManager) cxt.getSystemService(Context.ALARM_SERVICE);
//        alarmManager.setExact(AlarmManager.RTC_WAKEUP, date.getTimeInMillis(), pendingIntent);
//
//    }

//    public void showPopup(View v, int pos){
//        Log.i("LONG:::","CLICKED");
//        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
//        popupMenu.inflate(R.menu.long_press_menu_User);
//        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem menuItem) {
//                switch (menuItem.getItemId()){
//                    case R.id.User_MenuOption_Delete:
//
//                        deleteDialogBox(getItem(pos),v.getContext());
//                        return true;
//
//                    default:
//                        return false;
//                }
//            }
//        });        popupMenu.show();
//    }
//    private void deleteDialogBox(User User, Context cxt) {
//        User_Delete_Dialog deleteDialog = new User_Delete_Dialog(User);
//
//        deleteDialog.show(((FragmentActivity)cxt).getSupportFragmentManager(), "New Group");
//    }

}
