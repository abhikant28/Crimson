package com.akw.crimson;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Adapters.ChatList_RecyclerListAdapter;
import com.akw.crimson.AppObjects.Message;
import com.akw.crimson.AppObjects.User;
import com.akw.crimson.Chat.Chat;
import com.akw.crimson.Database.SharedPrefManager;
import com.akw.crimson.Database.TheViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class MainChatList extends AppCompatActivity {

    RecyclerView rv_chatList;
    //    ChatList_RecyclerCursorAdapter chatList_CursorAdapter;
    ChatList_RecyclerListAdapter chatList_recyclerListAdapter = new ChatList_RecyclerListAdapter();
    TheViewModel dbViewModel;
    ArrayList<Message> msg;
    public static User user;
//    Cursor c;

    FloatingActionButton fab_startNew;


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

        setView();
        connectLocalDatabase();

        setChatList(false);

//        makeMsgs();
//        InsertMessage im = new InsertMessage(3, msg);
//        new Thread(im).start();

        fireStore();

        sendToken();
    }

    private void connectLocalDatabase() {
        dbViewModel = ViewModelProviders.of(this).get(TheViewModel.class);
//        c = dbViewModel.getChatList();

        dbViewModel.getChatListUsers().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                chatList_recyclerListAdapter.submitList(users);
            }
        });
    }

    private void fireStore() {
//        FirebaseFirestore firebaseFirestore= FirebaseFirestore.getInstance();
//        Hashtable<String, Object> data= new Hashtable<>();
//
//        firebaseFirestore.collection("users")
//                .document(new SharedPrefManager(this).getLocalUserID()).set(data, SetOptions.merge())
//                .addOnSuccessListener(documentReference -> {
//                    Toast.makeText(getApplicationContext(), "Data Inserted with Success", Toast.LENGTH_SHORT).show();
//                })
//                .addOnFailureListener(documentReference -> {
//                    Toast.makeText(getApplicationContext(), "Data Insert Failed", Toast.LENGTH_SHORT).show();
//                });
    }


    private void sendToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        Hashtable<String, Object> data = new Hashtable<>();
        data.put("token", token);

        firebaseFirestore.collection("users")
                .document(new SharedPrefManager(this).getLocalUserID()).set(data, SetOptions.merge())
                .addOnSuccessListener(documentReference -> {
                })
                .addOnFailureListener(documentReference -> {
                });

        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReferenceFromUrl("https://crimson-ims-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userID = new SharedPrefManager(getApplicationContext()).getLocalUserID();
                databaseReference.child("users").child(userID).child("firebase_token").setValue(token);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void setChatList(boolean type) {
        rv_chatList.setLayoutManager(new LinearLayoutManager(this));
//        if (type) {
//            rv_chatList.setAdapter(chatList_CursorAdapter);
//            chatList_CursorAdapter.setOnItemClickListener(new ChatList_RecyclerCursorAdapter.OnListItemClickListener() {
//                @Override
//                public void onListItemClick(int position) {
//                    Intent intent = new Intent(getApplicationContext(), Chat.class);
//                    c.moveToPosition(position);
//                    user = dbViewModel.getUser(c.getString(c.getColumnIndexOrThrow("user_id")));
//                    intent.putExtra("USER_ID", user.getUser_id());
//                    Log.i("USER ID::::::", user.getName());
//                    startActivity(intent);
//                }
//
//            });
        //        chatList_CursorAdapter = new ChatList_RecyclerCursorAdapter(this, c
        //                , new ChatList_RecyclerCursorAdapter.OnListItemClickListener() {
        //            @Override
        //            public void onListItemClick(int position) {
        //
        //            }
        //        });
//        } else {
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
//        }
    }

    private void setView() {
        ActionBar ab = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#DC143C"));
        ab.setBackgroundDrawable(colorDrawable);

        rv_chatList = findViewById(R.id.MainChat_List_RecyclerView);
        fab_startNew = findViewById(R.id.MainChat_floatButton_newMessage);

        fab_startNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), StartNew.class));
            }
        });

    }

//    private void makeMsgs() {
//        msg = new ArrayList<>();
//        boolean sel = true;
//        Calendar cal = Calendar.getInstance();
//        for (int i = 5; i < 18; i++) {
//            sel = (!sel);
//            Message message = new Message(i + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)
//                    + cal.get(Calendar.MINUTE))
//                    + cal.get(Calendar.SECOND) + cal.get(Calendar.MILLISECOND),
//                    "1", "", Dataset.messagesDataSet[i], sel, false
//                    , "_", 0);
//            msg.add(message);
//        }
//        for (int i = 15; i < 30; i++) {
//            sel = (!sel);
//            msg.add(new Message(i + String.valueOf(cal.get(Calendar.HOUR_OF_DAY)
//                    + cal.get(Calendar.MINUTE)) + cal.get(Calendar.SECOND) + cal.get(Calendar.MILLISECOND)
//                    , "4", "", Dataset.messagesDataSet[i], sel, false
//                    , "_", 0));
//        }
//    }
//
//    class InsertMessage implements Runnable {
//        int time;
//        ArrayList<Message> msg;
//
//        InsertMessage(int time, ArrayList<Message> msg) {
//            this.time = time * 1000;
//            this.msg = msg;
//            Log.i("INSERT MSG THREAD::::::::", "Starting...");
//        }
//
//        @Override
//        public void run() {
//            Log.i("INSERT MSG THREAD::::::::", "Running...");
//            HTTPRequest.postMessage(msg.get(0));
//            for (int i = 0; i < msg.size(); i++) {
//                int finalI = i;
//                try {
//                    Thread.sleep(time);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                int[] ar = new int[]{1, 2, 3};
//
//                //Log.i("::::::::Message:::", String.valueOf(msg.get(0)));
//                dbViewModel.insertMessage(msg.get(finalI));
//            }
//            Log.i("INSERT MSG THREAD::::::::", "Inserted.");
//        }
//    }
}

