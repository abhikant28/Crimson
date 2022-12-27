package com.akw.crimson.Chat;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Adapters.Chat_RecyclerAdapter;
import com.akw.crimson.AppObjects.Message;
import com.akw.crimson.AppObjects.User;
import com.akw.crimson.BaseActivity;
import com.akw.crimson.Database.TheViewModel;
import com.akw.crimson.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Objects;

public class ChatActivity extends BaseActivity {

    private Chat_RecyclerAdapter chatAdapter;
    private RecyclerView chatRecyclerView;
    private ImageButton ib_send, ib_attach;
    private EditText et_message;

    private TheViewModel dbViewModel;
    private Cursor chatCursor;
    private Thread chatThread;
    private ActionBar ab;

    public static volatile User user;
    public static volatile boolean updated = false;
    public static volatile String userID, updateID;
    private Boolean isOnline = false;


    @Override
    protected void onStop() {
        super.onStop();
        user.setUnread_count(0);
        user.setUnread(false);
        dbViewModel.updateUser(user);
        chatThread.interrupt();
    }

    @Override
    protected void onResume() {
        super.onResume();
        listenForOnline();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        attachViews();

        dbViewModel = ViewModelProviders.of(this).get(TheViewModel.class);
        user = dbViewModel.getUser(getIntent().getStringExtra("USER_ID"));
        userID = user.getUser_id();

        //Log.i("USER_ID", user.get_id());
        setMyActionBar();
        setClicks();

        GetChatMessagesThread gcmt = new GetChatMessagesThread(user.getUser_id());
        chatThread = new Thread(gcmt);
        chatThread.start();

    }


    private void setClicks() {
        ib_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!et_message.getText().toString().trim().equals("")) {
                    Message message = new Message("1" + Calendar.getInstance().getTime().getTime(), userID, "0", et_message.getText().toString().trim(), true, false, null, 0);
                    dbViewModel.insertMessage(message);
                    //send(message);
                    user.setConnected(true);
                    et_message.setText("");
                }
            }
        });


    }

    private void listenForOnline() {
        FirebaseFirestore firestorDB = FirebaseFirestore.getInstance();
        firestorDB.collection("users").document(userID).addSnapshotListener(ChatActivity.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }
                if (value != null) {
                    if (value.getLong("online") != null) {
                        int online = Objects.requireNonNull(value.getLong("online")).intValue();
                        isOnline = online == 1;
                    }
                }
                if (isOnline) {
                    ab.setSubtitle("Online");
                } else {
                    ab.setSubtitle("");
                }
            }
        });
    }

    private void setMyActionBar() {
        ab = getSupportActionBar();
        ab.setTitle(user.getDisplayName());
        ab.setSubtitle("Status");
        ab.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        ImageView iv = new ImageView(getApplicationContext());
        iv.setPadding(50, 50, 50, 50);
        Picasso.get().load(user.getPic()).into(iv);
        Drawable d = iv.getDrawable();
        getSupportActionBar().setIcon(d);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#DC143C"));
        ab.setBackgroundDrawable(colorDrawable);
    }


    private void attachViews() {
        chatRecyclerView = findViewById(R.id.Chat_RecyclerView);
        ib_send = findViewById(R.id.Chat_Button_Send);
        ib_attach = findViewById(R.id.Chat_Button_Attachment);
        et_message = findViewById(R.id.Chat_EditText_Message);
    }


    private void loadChat() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setItemAnimator(new DefaultItemAnimator());
        chatRecyclerView.setAdapter(chatAdapter);
        chatAdapter.setOnItemClickListener(new Chat_RecyclerAdapter.OnListItemClickListener() {
            @Override
            public void onListItemClick(int position) {

            }
        });
        chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount());
    }


    class GetChatMessagesThread implements Runnable {
        String inp;

        GetChatMessagesThread(String inp) {
            this.inp = inp;
        }

        @Override
        public void run() {
            chatCursor = dbViewModel.getChatMessages(inp);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(
                            "MAKING ADAPTER CHAT:::::", "MAking.." + chatCursor.getCount() + "..");

                    chatAdapter = new Chat_RecyclerAdapter(getApplicationContext(), chatCursor
                            , new Chat_RecyclerAdapter.OnListItemClickListener() {
                        @Override
                        public void onListItemClick(int position) {

                        }
                    }, dbViewModel, true);
                    loadChat();
                    chatRecyclerView.scrollToPosition(chatAdapter.getItemCount());
                }
            });
            while (true) {
                //Log.i("CHAT UPDATE:::::","Watching..."+updateID+":"+user.get_id()+":"+updated);
                if (updated && chatAdapter != null && updateID.equals(userID)) {
                    //Log.i("CHAT UPDATE::::", "Message received");
                    int l = chatCursor.getCount();
                    chatCursor = dbViewModel.getChatMessages(inp);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            chatAdapter = new Chat_RecyclerAdapter(getApplicationContext(), chatCursor
                                    , new Chat_RecyclerAdapter.OnListItemClickListener() {
                                @Override
                                public void onListItemClick(int position) {

                                }
                            }, dbViewModel, false);
                            chatRecyclerView.setAdapter(null);
                            loadChat();
                            chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount());
                        }
                    });
                    updated = false;
                    user = dbViewModel.getUser(userID);
                }
            }

        }
    }


}