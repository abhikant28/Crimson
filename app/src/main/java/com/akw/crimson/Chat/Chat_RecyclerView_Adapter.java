//package com.akw.crimson.Chat;
//
//
//import android.view.LayoutInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.LinearLayout;
//import android.widget.PopupMenu;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.DiffUtil;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.akw.crimson.AppObjects.Message;
//import com.akw.crimson.R;
//
//
//public class Chat_RecyclerView_Adapter extends ListAdapter<Message,Chat_RecyclerView_Adapter.MyViewHolder> {
//    private AdapterView.OnItemClickListener listener;
//
//    private  static final DiffUtil.ItemCallback<Message> DIFF_CALLBACK_MESSAGE = new DiffUtil.ItemCallback<Message>() {
//        @Override
//        public boolean areItemsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
//            return oldItem.getMsg_ID().equals(newItem.getMediaID());
//        }
//
//        @Override
//        public boolean areContentsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
//            return oldItem.getStatus()==newItem.getStatus();
//        }
//    };
//
//    public Chat_RecyclerView_Adapter() {
//        super(DIFF_CALLBACK_MESSAGE);
//    }
//
//    @NonNull
//    @Override
//    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//
//        if(getself){
//            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_super_list_item_note, parent, false);
//        }else{
//            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_super_list_item_note, parent, false);
//        }
//        return new MyViewHolder(itemView);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull Chat_RecyclerView_Adapter.MyViewHolder holder, int position) {
//        String text = getItem(position).getMsg();
//        String date = getItem(position).getTime();
//
//
//        holder.tv_.setText("");
//        holder.tv_date.setText(date);
//    }
//
//
//    public class SentViewHolder extends RecyclerView.ViewHolder {
//        private TextView tv_text;
//        private TextView tv_date;
//        private LinearLayout ll_main;
//
//        public SentViewHolder(@NonNull View itemView) {
//            super(itemView);
//            ll_main = itemView.findViewById(R.id.Message_Sent_LL);
//            tv_date = itemView.findViewById(R.id.Message_Sent_time);
//            tv_text = itemView.findViewById(R.id.Message_Sent_msgBox);
//
//        }
//    }
//
//    public class ReceivedViewHolder extends RecyclerView.ViewHolder {
//        private TextView tv_text;
//        private TextView tv_date;
//        private LinearLayout ll_main;
//
//        public ReceivedViewHolder(@NonNull View itemView) {
//            super(itemView);
//            ll_main = itemView.findViewById(R.id.Message_Received_LL);
//            tv_date = itemView.findViewById(R.id.Message_Received_time);
//            tv_text = itemView.findViewById(R.id.Message_Received_msgBox);
//
//        }
//    }
//
////    public interface OnItemClickListener {
////        void OnItemClick(Message note);
////    }
//
////    public void setOnItemClickListener(OnItemClickListener listener) {
////        this.listener = listener;
////    }
//
////    public void showPopup(View v, int pos){
////        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
////        popupMenu.inflate(R.menu.long_press_menu_notes);
////        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
////            @Override
////            public boolean onMenuItemClick(MenuItem menuItem) {
////                switch (menuItem.getItemId()){
////                    case R.id.Notes_Option_Delete:
////                        return true;
////
////                    default:
////                        return false;
////                }
////            }
////        });
////        popupMenu.show();
////    }
//
//
//}
