package com.akw.crimson;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.AppObjects.Message;
import com.akw.crimson.AppObjects.User;
import com.akw.crimson.Chat.Chat;
import com.akw.crimson.Database.TheViewModel;
import com.akw.crimson.Communications.HTTPRequest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainChatList extends AppCompatActivity {

    RecyclerView rv_chatList;
    ChatList_RecyclerCursorAdapter chatList_CursorAdapter;
    ChatList_RecyclerListAdapter chatList_recyclerListAdapter = new ChatList_RecyclerListAdapter();
    TheViewModel dbViewModel;
    ArrayList<Message> msg;
    public static User user;
    Cursor c;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.chatlist_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.chatList_Menu_search:
                break;
            case R.id.chatList_Menu_Settings:
                break;
            case R.id.chatList_Menu_NewGroup:
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_chat_list);

        ActionBar ab = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#DC143C"));
        ab.setBackgroundDrawable(colorDrawable);

        rv_chatList = findViewById(R.id.MainChat_List_RecyclerView);

        dbViewModel = ViewModelProviders.of(this).get(TheViewModel.class);
        c = dbViewModel.getChatList();
        chatList_CursorAdapter = new ChatList_RecyclerCursorAdapter(this, c
                , new ChatList_RecyclerCursorAdapter.OnListItemClickListener() {
            @Override
            public void onListItemClick(int position) {

            }
        });
        dbViewModel.getAllUsersList().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                chatList_recyclerListAdapter.submitList(users);
            }
        });
        setChatList(false);
        makeMsgs();
        InsertMessage im = new InsertMessage(3, msg);
        new Thread(im).start();

        //new HTTPRequest().postMessage(new Message());
        //new HTTPRequest().getMessages();

    }


    private void setChatList(boolean type) {
        rv_chatList.setLayoutManager(new LinearLayoutManager(this));
        if (type) {
            rv_chatList.setAdapter(chatList_CursorAdapter);
            chatList_CursorAdapter.setOnItemClickListener(new ChatList_RecyclerCursorAdapter.OnListItemClickListener() {
                @Override
                public void onListItemClick(int position) {
                    Intent intent = new Intent(getApplicationContext(), Chat.class);
                    c.moveToPosition(position);
                    user = dbViewModel.getUser(c.getString(c.getColumnIndexOrThrow("user_id")));
                    intent.putExtra("USER_ID", user.getUser_id());
                    Log.i("USER ID::::::", user.getName());
                    startActivity(intent);
                }

            });
        } else {
            rv_chatList.setAdapter(chatList_recyclerListAdapter);
            chatList_recyclerListAdapter.setOnItemCLickListener(new ChatList_RecyclerListAdapter.OnItemClickListener() {
                @Override
                public void OnItemClick(User User) {
                    Intent intent = new Intent(getApplicationContext(), Chat.class);
                    intent.putExtra("USER_ID", User.getUser_id());
                    Log.i("USER ID::::::", User.getName());
                    startActivity(intent);
                }
            });
        }
    }


    private void makeMsgs() {
        msg = new ArrayList<>();
        boolean sel = true;
        Calendar cal = Calendar.getInstance();
        for (int i = 5; i < 18; i++) {
            sel = (!sel);
            Message message = new Message(i + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)
                    + cal.get(Calendar.MINUTE))
                    + cal.get(Calendar.SECOND) + cal.get(Calendar.MILLISECOND),
                    "1", "", Dataset.messagesDataSet[i], sel, false
                    , "_", 0);
            msg.add(message);
        }
        for (int i = 15; i < 30; i++) {
            sel = (!sel);
            msg.add(new Message(i + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)
                    + cal.get(Calendar.MINUTE)) + cal.get(Calendar.SECOND) + cal.get(Calendar.MILLISECOND)
                    , "4", "", Dataset.messagesDataSet[i], sel, false
                    , "_", 0));
        }
    }

    class InsertMessage implements Runnable {
        int time;
        ArrayList<Message> msg;

        InsertMessage(int time, ArrayList<Message> msg) {
            this.time = time * 1000;
            this.msg = msg;
            Log.i("INSERT MSG THREAD::::::::", "Starting...");
        }

        @Override
        public void run() {
            Log.i("INSERT MSG THREAD::::::::", "Running...");
            HTTPRequest.postMessage(msg.get(0));
            for (int i = 0; i < msg.size(); i++) {
                int finalI = i;
                try {
                    Thread.sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int[] ar = new int[]{1, 2, 3};

                //Log.i("::::::::Message:::", String.valueOf(msg.get(0)));
                dbViewModel.insertMessage(msg.get(finalI));
            }
            Log.i("INSERT MSG THREAD::::::::", "Inserted.");
        }
    }
}

