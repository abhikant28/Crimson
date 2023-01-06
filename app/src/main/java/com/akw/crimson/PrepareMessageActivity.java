package com.akw.crimson;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Adapters.PrepareMessage_List_RecyclerListAdapter;
import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.AppObjects.PreparedMessage;
import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.AlertReceiver;
import com.akw.crimson.Backend.Communications.Communicator;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.SharedPrefManager;
import com.akw.crimson.Backend.Database.TheViewModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class PrepareMessageActivity extends BaseActivity {

    RecyclerView rv_preparedMessages;
    TextView tv_selectContact, tv_selectDate, tv_selectTime;
    EditText et_messageText;
    ImageButton b_saveMsg;
    ActionBar ab;

    PrepareMessage_List_RecyclerListAdapter recyclerListAdapter = new PrepareMessage_List_RecyclerListAdapter();
    ArrayList<PreparedMessage> messageArrayList = new ArrayList<>();
    User msgForUser = null;
    Calendar msgDate;
    TheViewModel db;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.KEY_ACTIVITY_RESULT_CONTACT_SELECT) {
            if (resultCode == RESULT_OK && data != null) {
                msgForUser = Communicator.localDB.getUser(data.getStringExtra(Constants.KEY_INTENT_USERID));
                tv_selectContact.setText(msgForUser.getDisplayName());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prepare_message);

        messageArrayList = SharedPrefManager.getPreparedMessages();
        db=Communicator.localDB;
        Log.i("ONCREATE:::::",(db==null)+"");
        inti();

        clicks();


    }

    private void clicks() {
        recyclerListAdapter.setOnItemCLickListener(new PrepareMessage_List_RecyclerListAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(PreparedMessage preparedMessage, int position, View view) {
                switch (view.getId()) {
                    case R.id.PrepareMessage_Item_ib_edit:
                        msgDate = preparedMessage.getDate();
                        msgForUser=db.getUser(preparedMessage.getToID());
                        Log.i("EDIT::::::::", (msgForUser==null)+"");
                        et_messageText.setText(preparedMessage.getMessage().getMsg());
                        tv_selectContact.setText(preparedMessage.getToName());
                        SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
                        tv_selectDate.setText(preparedMessage.getDate().get(Calendar.DAY_OF_MONTH) + " " + month_date.format(msgDate.get(Calendar.MONTH)).substring(0,3));
                        tv_selectTime.setText("At: " + DateFormat.getTimeInstance(DateFormat.SHORT).format(preparedMessage.getDate().getTime()));
                        messageArrayList.remove(position);
                        SharedPrefManager.putPreparedMessages(messageArrayList);
                        recyclerListAdapter.submitList(messageArrayList);

                        break;
                    case R.id.PrepareMessage_Item_ib_delete:
                        createDeleteDialog(position);
                        Log.i("DELETED::::", messageArrayList.size()+"");
                        break;
                }
            }
        });

        b_saveMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Log.i("SAVING::::::::",(!et_messageText.getText().toString().equals("")) +"_"+ (msgDate != null) +"_"+ (msgForUser != null));
                if (!et_messageText.getText().toString().equals("") && msgDate != null && msgForUser != null) {
                    Message message = new Message(SharedPrefManager.getLocalUserID() + Calendar.getInstance().getTime().getTime(), msgForUser.getUser_id(), null, et_messageText.getText().toString().trim(), true, false, null, 0, msgDate);
                    PreparedMessage prep=new PreparedMessage(message, msgDate, msgForUser.getDisplayName(), msgForUser.getUser_id());
                    messageArrayList.add(0, prep);
                    setAlarm(prep);
                    SharedPrefManager.putPreparedMessages(messageArrayList);
                    recyclerListAdapter.submitList(SharedPrefManager.getPreparedMessages());
                    msgDate = null;
                    et_messageText.setText("");
                    tv_selectContact.setText("Select Contact");
                    tv_selectTime.setText("Select Time");
                    tv_selectDate.setText("Select Day");
                    msgForUser = null;
                } else {
                    Toast.makeText(getBaseContext(), "Enter Message", Toast.LENGTH_SHORT).show();
                    tv_selectDate.setText("Select Day");
                    tv_selectTime.setText("Select Time");
                }
            }
        });

        tv_selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popUpDate(view);
            }
        });

        tv_selectTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (msgDate != null) {
                    pickTime();
                } else {
                    Toast.makeText(getApplicationContext(), "Select a Day First", Toast.LENGTH_LONG).show();
                }
            }
        });

        tv_selectContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getApplicationContext(), SelectContact.class), Constants.KEY_ACTIVITY_RESULT_CONTACT_SELECT);
            }
        });
    }

    private void setAlarm(PreparedMessage preparedMessage) {
        AlarmManager alarmManager= (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent=new Intent(this, AlertReceiver.class);
        intent.putExtra(Constants.KEY_INTENT_PREP_MSG_ID,preparedMessage.getId());
        intent.putExtra(Constants.KEY_INTENT_USERNAME, preparedMessage.getToName());
        PendingIntent pi=PendingIntent.getBroadcast(this,preparedMessage.getId(), intent, PendingIntent.FLAG_MUTABLE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP,preparedMessage.getDate().getTimeInMillis(),pi);
    }

    private void pickTime() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                PrepareMessageActivity.this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        Calendar cal = msgDate;
                        cal.set(Calendar.HOUR_OF_DAY, selectedHour);
                        cal.set(Calendar.MINUTE, selectedMinute);
                        if (Calendar.getInstance().before(cal)) {
                            msgDate.set(Calendar.HOUR_OF_DAY, selectedHour);
                            msgDate.set(Calendar.MINUTE, selectedMinute);
                            msgDate.set(Calendar.SECOND, 0);
                            tv_selectTime.setText("At: " + DateFormat.getTimeInstance(DateFormat.SHORT).format(msgDate.getTime()));
                        } else {
                            Toast.makeText(getApplicationContext(), "Select a date and time in future", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                hour,
                minute,
                true
        );
        timePickerDialog.show();
    }

    private void popUpDate(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        int i = 0;
        popupMenu.getMenu().add(Menu.NONE, i, i++, "Today");
        popupMenu.getMenu().add(Menu.NONE, i, i, "Tomorrow");
        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                msgDate = null;
                msgDate = Calendar.getInstance();
                msgDate.add(Calendar.DAY_OF_MONTH, item.getItemId());
                tv_selectDate.setText(item.getItemId() == 0 ? "Today" : "Tomorrow");
                tv_selectTime.setText("Set Time");
                return false;
            }
        });
    }

    private void createDeleteDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PrepareMessageActivity.this);

        builder.setMessage("Are you sure you wish to delete this message?)")
                .setTitle("Confirm");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteSendIntent(messageArrayList.get(position));
                messageArrayList.remove(position);
                SharedPrefManager.putPreparedMessages(messageArrayList);
                recyclerListAdapter.submitList(messageArrayList);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        AlertDialog alertDialog = builder.create();
        // Show the Alert Dialog box
        alertDialog.show();
    }

    private void deleteSendIntent(PreparedMessage prepMsg) {
        AlarmManager alarmManager= (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent=new Intent(this, AlertReceiver.class);
        PendingIntent pi=PendingIntent.getBroadcast(this,prepMsg.getId(), intent, PendingIntent.FLAG_MUTABLE);
        alarmManager.cancel(pi);
    }

    private void inti() {
        rv_preparedMessages = findViewById(R.id.PrepareMessage_rv_preparedMessages);
        tv_selectContact = findViewById(R.id.PrepareMessage_tv_selectContact);
        tv_selectDate = findViewById(R.id.PrepareMessage_tv_setDate);
        tv_selectTime = findViewById(R.id.PrepareMessage_tv_setTime);
        et_messageText = findViewById(R.id.PrepareMessage_EditText_Message);
        b_saveMsg = findViewById(R.id.PrepareMessage_Button_Send);
        ab=getSupportActionBar();
        ab.setTitle("Prepare Message");

        new SharedPrefManager(this);
        rv_preparedMessages.setLayoutManager(new LinearLayoutManager(this));
        rv_preparedMessages.setAdapter(recyclerListAdapter);
        recyclerListAdapter.submitList(SharedPrefManager.getPreparedMessages());
    }
}








