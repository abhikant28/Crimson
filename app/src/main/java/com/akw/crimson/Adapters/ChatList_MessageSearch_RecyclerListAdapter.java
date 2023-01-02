package com.akw.crimson.Adapters;


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

import com.akw.crimson.AppObjects.Message;
import com.akw.crimson.Backend.Database.TheViewModel;
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.R;

public class ChatList_MessageSearch_RecyclerListAdapter extends ListAdapter<Message, ChatList_MessageSearch_RecyclerListAdapter.MyViewHolder> {
    private OnItemClickListener listener;
    TheViewModel db;

    private static final DiffUtil.ItemCallback<Message> DIFF_CALLBACK_Message = new DiffUtil.ItemCallback<Message>() {
        @Override
        public boolean areItemsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
            return oldItem.getLocal_msg_ID().equals(newItem.getLocal_msg_ID());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
            return oldItem.getStatus() == newItem.getStatus() ;
        }
    };

    public ChatList_MessageSearch_RecyclerListAdapter() {
        super(DIFF_CALLBACK_Message);
    }
    public ChatList_MessageSearch_RecyclerListAdapter(TheViewModel db) {
        super(DIFF_CALLBACK_Message);
        this.db=db;
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView tv_name;
        private final TextView tv_lastMsg;
        private final TextView tv_time;

        public MyViewHolder(@NonNull View view) {
            super(view);

            tv_name = view.findViewById(R.id.MainSearch_Item_TextView_UserName);
            tv_lastMsg = view.findViewById(R.id.MainSearch_Item_TextView_UserMsg);
            tv_time=view.findViewById(R.id.MainSearch_Item_TextView_Time);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int p = getAdapterPosition();
                    if (listener != null && p != -1) listener.OnItemClick(getMessage(p));
                }
            });
        }
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

        String name=db.getUser(message.getUser_id()).getDisplayName();
        holder.tv_name.setText(name);
        holder.tv_lastMsg.setText(message.getMsg());
        holder.tv_time.setText((message.getTime()==null?"12:00":String.valueOf(message.getTime().substring(0,5))));
    }

    public Message getMessage(int position) {
        return getItem(position);
    }

    public interface OnItemClickListener {
        void OnItemClick(Message Message);
    }

    public void setOnItemCLickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

//    public void setNextAlert(Message Message, Context cxt) {
//        Calendar date = Message.getDate();
//
//        switch (Message.getType()) {
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
//        Message.setDate(date);
//        Intent intent = new Intent(cxt.getApplicationContext(), AlertReceiver.class);
//        intent.putExtra("title", "Reminder : ");
//        intent.putExtra("body", Message.getTitle());
//        viewModel.update(Message);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(cxt.getApplicationContext(), Message.getT_id(), intent, 0);
//        AlarmManager alarmManager = (AlarmManager) cxt.getSystemService(Context.ALARM_SERVICE);
//        alarmManager.setExact(AlarmManager.RTC_WAKEUP, date.getTimeInMillis(), pendingIntent);
//
//    }

//    public void showPopup(View v, int pos){
//        Log.i("LONG:::","CLICKED");
//        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
//        popupMenu.inflate(R.menu.long_press_menu_Message);
//        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem menuItem) {
//                switch (menuItem.getItemId()){
//                    case R.id.Message_MenuOption_Delete:
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
//    private void deleteDialogBox(Message Message, Context cxt) {
//        Message_Delete_Dialog deleteDialog = new Message_Delete_Dialog(Message);
//
//        deleteDialog.show(((FragmentActivity)cxt).getSupportFragmentManager(), "New Group");
//    }

}
