package com.akw.crimson.Chat;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Adapters.Chat_RecyclerAdapter;
import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Communications.Communicator;
import com.akw.crimson.Backend.Communications.Messaging;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.SharedPrefManager;
import com.akw.crimson.Backend.Database.TheViewModel;
import com.akw.crimson.BaseActivity;
import com.akw.crimson.R;
import com.google.android.gms.tasks.OnSuccessListener;
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
    private ImageButton ib_send, ib_attach, ib_camera;
    private EditText et_message;

    private TheViewModel dbViewModel;
    private Cursor chatCursor;
    private Thread chatThread;
    private ActionBar ab;

    public static volatile User user;
    public static volatile boolean updated = false;
    public static volatile String userID, updateID;
    private Boolean isOnline = false;
    private int searchPosition;


    @Override
    protected void onStop() {
        super.onStop();
        if (user != null) {
            user.setUnread_count(0);
            user.setUnread(false);
        }
        dbViewModel.updateUser(user);
        chatThread.interrupt();
        stopService(new Intent(this, Communicator.class));
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(new Intent(this, Communicator.class));
        startService(intent);
        listenForOnline();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.chat_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.chat_menu_search);

        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length() != 0) {
                    searchPosition=0;
                    searchInChat(query, chatCursor);
                }else{
                    searchPosition=0;
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        ViewGroup.LayoutParams navButtonsParams = new ViewGroup.LayoutParams(ab.getHeight() * 2 / 3, ab.getHeight() * 2 / 3);

        Button btnNext = new Button(this);
        btnNext.setBackground(getResources().getDrawable(R.drawable.));

        Button btnPrev = new Button(this);
        btnPrev.setBackground(getResources().getDrawable(R.drawable.));

        searchStats = new TextView(this);

        ((LinearLayout) searchView.getChildAt(0)).addView(searchStats);
        ((LinearLayout) searchView.getChildAt(0)).addView(btnPrev, navButtonsParams);
        ((LinearLayout) searchView.getChildAt(0)).addView(btnNext, navButtonsParams);

        ((LinearLayout) searchView.getChildAt(0)).setGravity(Gravity.BOTTOM);


        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!searchQuery.equals("")) {
                    lawTextWebView.findNext(true);
                }
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!searchQuery.equals("")) {
                    lawTextWebView.findNext(false);
                }
            }
        });

        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        attachViews();

        dbViewModel = Communicator.localDB;
        user = dbViewModel.getUser(getIntent().getStringExtra(Constants.KEY_INTENT_USERID));
        userID = user.getUser_id();

        //Log.i("USER_ID", user.get_id());
        setMyActionBar();
        setClicks();

        GetChatMessagesThread gcmt = new GetChatMessagesThread(user.getUser_id());
        chatThread = new Thread(gcmt);
        chatThread.start();

    }

    private void searchInChat(String query, Cursor chatCursor) {
        Cursor cursor=chatCursor;
        int i=0;
        while(cursor.moveToNext()){
            if (cursor.getString(cursor.getColumnIndexOrThrow("msg")).contains(query)) {
                chatRecyclerView.scrollToPosition(i);
            }
            i++;
        }
    }

    private void setClicks() {
        ib_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!et_message.getText().toString().trim().equals("")) {
                    Message message = new Message(SharedPrefManager.getLocalUserID() + Calendar.getInstance().getTime().getTime(), userID, "0", et_message.getText().toString().trim(), true, false, null, 0);
                    dbViewModel.insertMessage(message);
                    //send(message);
                    user.setConnected(true);
                    et_message.setText("");
                    if (!isOnline) {
                        Log.i("NOT ONLINE", "SENDING MSG");
                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                        firestore.collection(Constants.KEY_FIRESTORE_USERS).document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                String userToken = documentSnapshot.getString(Constants.KEY_FIRESTORE_USER_TOKEN);
                                Messaging.sendMessageNotification(SharedPrefManager.getLocalUserID(), userToken, message.getTag(), message.getMsg_ID(), SharedPrefManager.getLocalUser().getName(), message.getMsg());
//                                Messaging.sendMessageRetroNotification(new SharedPrefManager(getApplicationContext()).getLocalUserID(), userToken, message.getTag(), message.getMsg_ID(),userID);
                            }
                        });

                    }
                }
            }
        });


    }

    private void listenForOnline() {
        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        firestoreDB.collection(Constants.KEY_FIRESTORE_USERS).document(userID).addSnapshotListener(ChatActivity.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }
                if (value != null) {
                    if (value.get(Constants.KEY_FIRESTORE_USER_ONLINE) != null) {
                        int online = Integer.parseInt(String.valueOf(Objects.requireNonNull(value.get(Constants.KEY_FIRESTORE_USER_ONLINE))));
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
                if (updated && chatAdapter != null && updateID.equals(userID)) {
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
        new SharedPrefManager(getApplicationContext());
        chatRecyclerView = findViewById(R.id.Chat_RecyclerView);
        ib_send = findViewById(R.id.Chat_Button_Send);
        ib_attach = findViewById(R.id.Chat_Button_Attachment);
        et_message = findViewById(R.id.Chat_EditText_Message);
        ib_camera = findViewById(R.id.Chat_Button_Camera);

        et_message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() != 0) {
                    ib_attach.setVisibility(View.GONE);
                    ib_camera.setVisibility(View.GONE);
                } else {
                    ib_attach.setVisibility(View.VISIBLE);
                    ib_camera.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }


}
