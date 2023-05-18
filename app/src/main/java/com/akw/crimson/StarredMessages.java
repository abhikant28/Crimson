package com.akw.crimson;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Backend.Adapters.MessageSearch_RecyclerListAdapter;
import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.Communications.Communicator;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.TheViewModel;
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.Chat.ChatActivity;

import java.util.ArrayList;

public class StarredMessages extends AppCompatActivity {
    MessageSearch_RecyclerListAdapter starredMsgs_rv_Adapter = new MessageSearch_RecyclerListAdapter();
    RecyclerView rv_starredMsgs;

    ArrayList<Message> starredMsgs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starred_messages);

        TheViewModel db = Communicator.localDB;

        String userID = getIntent().getStringExtra(Constants.KEY_INTENT_USERID);
        starredMsgs_rv_Adapter= new MessageSearch_RecyclerListAdapter(db);


        if(userID!=null){
            starredMsgs= (ArrayList<Message>) db.getStarredUserMessages(userID);
        }else{
            starredMsgs = (ArrayList<Message>) db.getStarredMessages();
        }



        initialize();


    }

    private void initialize() {
        rv_starredMsgs = findViewById(R.id.starredMessages_recyclerView_messages);

        ActionBar ab = getSupportActionBar();
        ab.setTitle("Starred Messages");
        ab.setSubtitle(starredMsgs.size()+" Starred Message"+(starredMsgs.size()==1?"":"s"));
        ab.setDisplayHomeAsUpEnabled(true);
        ImageView iv = new ImageView(getApplicationContext());
        iv.setPadding(50, 50, 50, 50);
        iv.setImageResource(R.drawable.ic_baseline_star_border_24);
        Drawable d = iv.getDrawable();
        getSupportActionBar().setIcon(d);
        rv_starredMsgs.setLayoutManager(new LinearLayoutManager(this));
        starredMsgs_rv_Adapter.submitList(starredMsgs);
        rv_starredMsgs.setAdapter(starredMsgs_rv_Adapter);
        starredMsgs_rv_Adapter.setOnItemCLickListener(message -> {
            Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
            intent.putExtra(Constants.KEY_INTENT_USERID, message.getUser_id());
            intent.putExtra(Constants.KEY_INTENT_MESSAGE_ID, message.getMsg_ID());
            intent.putExtra(Constants.KEY_INTENT_STARTED_BY, Constants.KEY_INTENT_STARTED_BY_SEARCH);
            startActivity(intent);
        });
    }
}