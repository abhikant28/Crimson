package com.example.crimson;

import static java.security.AccessController.getContext;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.example.crimson.AppObjects.Profile;
import com.example.crimson.Chat_RecyclerView_Adapter;
import com.example.crimson.R;

public class Chat extends AppCompatActivity {

    Profile profile;
    Chat_RecyclerView_Adapter chatAdapter;
    RecyclerView chatRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatRecyclerView= findViewById(R.id.Chat_RecyclerView);
        loadChat(profile.getID());

    }

    private void loadChat(String profileID) {
            Log.i("SetAdapter:","Working");
            Chat_RecyclerView_Adapter adapter = new Chat_RecyclerView_Adapter(topicList,rListener);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            layoutManager.setStackFromEnd(true);
            layoutManager.setSmoothScrollbarEnabled(false);
            chatRecyclerView.setLayoutManager(layoutManager);
            chatRecyclerView.setItemAnimator(new DefaultItemAnimator());
            chatRecyclerView.setAdapter(chatAdapter);

            Log.i("Adapter", "Executed");
    }


}