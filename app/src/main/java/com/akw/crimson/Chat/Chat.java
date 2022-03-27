package com.akw.crimson.Chat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.akw.crimson.AppObjects.Message;
import com.akw.crimson.AppObjects.Profile;
import com.akw.crimson.Database.TheViewModel;
import com.akw.crimson.R;

import java.util.List;

public class Chat extends AppCompatActivity {

    Profile profile;
    Chat_RecyclerAdapter chatAdapter;
    RecyclerView chatRecyclerView;
    TheViewModel dbViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        String userID=getIntent().getStringExtra("USER_ID");
        chatRecyclerView= findViewById(R.id.Chat_RecyclerView);
        loadChat();
        dbViewModel = ViewModelProviders.of(this).get(TheViewModel.class);

        dbViewModel.getChatMessages(userID).observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(@Nullable List<Message> msgs) {
                chatAdapter.submitList(msgs);
            }
        });
    }

    private void loadChat() {
            Log.i("SetAdapter:","Working");
            LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            layoutManager.setStackFromEnd(true);
            layoutManager.setSmoothScrollbarEnabled(false);
            chatRecyclerView.setLayoutManager(layoutManager);
            chatRecyclerView.setItemAnimator(new DefaultItemAnimator());
            chatRecyclerView.setAdapter(chatAdapter);

            Log.i("Adapter", "Executed");
    }


}