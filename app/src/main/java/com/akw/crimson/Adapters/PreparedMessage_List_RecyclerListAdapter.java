package com.akw.crimson.Adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Backend.AppObjects.PreparedMessage;
import com.akw.crimson.Backend.Database.TheViewModel;
import com.akw.crimson.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PreparedMessage_List_RecyclerListAdapter extends ListAdapter<PreparedMessage, PreparedMessage_List_RecyclerListAdapter.MyViewHolder> {
    private OnItemClickListener listener;
    TheViewModel db;

    private static final DiffUtil.ItemCallback<PreparedMessage> DIFF_CALLBACK_PreparedMessage = new DiffUtil.ItemCallback<PreparedMessage>() {
        @Override
        public boolean areItemsTheSame(@NonNull PreparedMessage oldItem, @NonNull PreparedMessage newItem) {
            return oldItem.getMessage().getMsg_ID().equals(newItem.getMessage().getMsg_ID());
        }

        @Override
        public boolean areContentsTheSame( PreparedMessage oldItem, PreparedMessage newItem) {
            return false;
        }
    };

    public PreparedMessage_List_RecyclerListAdapter() {
        super(DIFF_CALLBACK_PreparedMessage);
    }

    public PreparedMessage_List_RecyclerListAdapter(TheViewModel db) {
        super(DIFF_CALLBACK_PreparedMessage);
        this.db = db;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView tv_name,tv_msg,tv_time;
        private final ImageButton ib_delete,ib_edit;

        public MyViewHolder(@NonNull View view) {
            super(view);

            tv_name = view.findViewById(R.id.PrepareMessage_Item_TextView_UserName);
            tv_msg = view.findViewById(R.id.PrepareMessage_Item_TextView_UserMsg);
            tv_time = view.findViewById(R.id.PrepareMessage_Item_TextView_Time);
            ib_delete = view.findViewById(R.id.PrepareMessage_Item_ib_delete);
            ib_edit=view.findViewById(R.id.PrepareMessage_Item_ib_edit);


            ib_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int p = getAdapterPosition();
                    if (listener != null && p != -1) listener.OnItemClick(getPreparedMessage(p),p,view);
                }
            });
            ib_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int p = getAdapterPosition();
                    if (listener != null && p != -1) listener.OnItemClick(getPreparedMessage(p),p,view);
                }
            });
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.prepare_message_list_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        PreparedMessage preparedMessage = getItem(position);

        holder.tv_name.setText(preparedMessage.getToName());
        holder.tv_msg.setText(preparedMessage.getMessage().getMsg().substring(0,Math.min(preparedMessage.getMessage().getMsg().length(), 8)));
        SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
        holder.tv_time.setText("On "+preparedMessage.getDate().get(Calendar.DATE) +","+month_date.format(preparedMessage.getDate().get(Calendar.MONTH)).substring(0,3)+" at "+preparedMessage.getDate().get(Calendar.HOUR_OF_DAY)+":"+preparedMessage.getDate().get(Calendar.MINUTE));
    }

    public PreparedMessage getPreparedMessage(int position) {
        return getItem(position);
    }

    public interface OnItemClickListener {
        void OnItemClick(PreparedMessage PreparedMessage, int position, View view);
    }

    public void setOnItemCLickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

}
