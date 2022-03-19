package com.akw.crimson;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.akw.crimson.AppObjects.User;
import com.akw.crimson.Chat.Chat;

public class MainChatList extends AppCompatActivity {

    RecyclerView rv_chatList;
    ChatList_RecyclerAdapter chatListAdapter;
    User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_chat_list);

        rv_chatList= findViewById(R.id.MainChat_List_RecyclerView);

        chatListAdapter= new ChatList_RecyclerAdapter();

        getChatList();

    }

    private void getChatList() {
        rv_chatList.setLayoutManager(new LinearLayoutManager(this));
        rv_chatList.setAdapter(chatListAdapter);
        chatListAdapter.setOnItemClickListener(new ChatList_RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(User user) {
                Intent intent= new Intent(getApplicationContext(),Chat.class);
                intent.putExtra("USER_ID", user.getUser_id());
                startActivity(intent);
            }
        });
    }
}