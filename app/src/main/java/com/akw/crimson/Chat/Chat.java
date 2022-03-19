package com.akw.crimson.Chat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.akw.crimson.AppObjects.Profile;
import com.akw.crimson.R;

public class Chat extends AppCompatActivity {

    Profile profile;
    Chat_RecyclerAdapter chatAdapter;
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