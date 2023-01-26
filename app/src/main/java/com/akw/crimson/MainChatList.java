package com.akw.crimson;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Backend.Adapters.AllUserList_RecyclerListAdapter;
import com.akw.crimson.Backend.Adapters.ChatList_MessageSearch_RecyclerListAdapter;
import com.akw.crimson.Backend.Adapters.ChatList_RecyclerListAdapter;
import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Communications.Communicator;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.SharedPrefManager;
import com.akw.crimson.Backend.Database.TheViewModel;
import com.akw.crimson.Chat.ChatActivity;
import com.akw.crimson.Preferences.SettingsActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Hashtable;
import java.util.List;

public class MainChatList extends AppCompatActivity {

    ActionBar ab;
    TextView tv_noResults;
    RecyclerView rv_chatList;
    RecyclerView rv_searchUsers;
    RecyclerView rv_searchMessages;

    ChatList_RecyclerListAdapter chatList_recyclerListAdapter = new ChatList_RecyclerListAdapter();
    AllUserList_RecyclerListAdapter searchUserList_rvAdapter = new AllUserList_RecyclerListAdapter();
    ChatList_MessageSearch_RecyclerListAdapter searchMessageList_rv_Adapter = new ChatList_MessageSearch_RecyclerListAdapter();

    TheViewModel dbViewModel;
    public static User user;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.chatlist_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.chatList_Menu_search);

        // getting search view of our item.
        SearchView searchView = (SearchView) searchItem.getActionView();

        // below line is to call set on query text listener method.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.i("QUERY CHANGE:::::", newText);
                if (newText.length() == 0) {
                    rv_chatList.setVisibility(View.VISIBLE);
                    searchMessageList_rv_Adapter.submitList(null);
                    searchUserList_rvAdapter.submitList(null);
                    tv_noResults.setVisibility(View.GONE);
                } else {
                    rv_chatList.setVisibility(View.GONE);
                    searchQuery(newText);
                }
                return false;
            }
        });
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.chatList_Menu_Settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.chatList_Menu_NewGroup:
                break;
            case R.id.chatList_Menu_autoSendMsg:
                startActivity(new Intent(this, PrepareMessageActivity.class));
                break;
            case R.id.chatList_Menu_camera:
                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(camera_intent, 77);
                break;
            case R.id.chatList_Menu_payments:
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

        setChatList();

        sendToken();


    }

    private void connectLocalDatabase() {
        dbViewModel = Communicator.localDB;

        dbViewModel.getChatListUsers().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                chatList_recyclerListAdapter.submitList(users);
            }
        });
    }

    private void searchQuery(String newText) {
        List<Message> messagesFound = dbViewModel.searchInMessages(newText);
        List<User> usersFound = dbViewModel.searchUserByText(newText);
        if (messagesFound.size() == 0 && usersFound.size() == 0) {
            tv_noResults.setVisibility(View.VISIBLE);
            searchMessageList_rv_Adapter.submitList(null);
            searchUserList_rvAdapter.submitList(null);
            return;
        }
        tv_noResults.setVisibility(View.GONE);
        searchMessageList_rv_Adapter.submitList(null);
        searchMessageList_rv_Adapter.submitList(messagesFound);
        searchUserList_rvAdapter.submitList(null);
        searchUserList_rvAdapter.submitList(usersFound);

    }

    private void sendToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        Hashtable<String, Object> data = new Hashtable<>();
        data.put(Constants.KEY_FIRESTORE_USER_TOKEN, token);

        firebaseFirestore.collection(Constants.KEY_FIRESTORE_USERS)
                .document(new SharedPrefManager(this).getLocalUserID()).set(data, SetOptions.merge())
                .addOnSuccessListener(documentReference -> {
                })
                .addOnFailureListener(documentReference -> {
                });
    }


    private void setChatList() {
        rv_chatList.setLayoutManager(new LinearLayoutManager(this));
        rv_chatList.setAdapter(chatList_recyclerListAdapter);
        chatList_recyclerListAdapter.setOnItemCLickListener(new ChatList_RecyclerListAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(User User) {
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra(Constants.KEY_INTENT_USERID, User.getUser_id());
                Log.i("USER ID::::::", User.getName());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        rv_searchUsers.setLayoutManager(new LinearLayoutManager(this));
        rv_searchUsers.setAdapter(searchUserList_rvAdapter);
        searchUserList_rvAdapter.setOnItemCLickListener(new AllUserList_RecyclerListAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(User User) {
                Intent intent = new Intent(getApplicationContext(), ProfileView.class);
                intent.putExtra(Constants.KEY_INTENT_USERID, User.getUser_id());
                startActivity(intent);
            }
        });

        searchMessageList_rv_Adapter = new ChatList_MessageSearch_RecyclerListAdapter(dbViewModel);
        rv_searchMessages.setLayoutManager(new LinearLayoutManager(this));
        rv_searchMessages.setAdapter(searchMessageList_rv_Adapter);
        searchMessageList_rv_Adapter.setOnItemCLickListener(new ChatList_MessageSearch_RecyclerListAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(Message message) {
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra(Constants.KEY_INTENT_USERID, message.getUser_id());
                intent.putExtra(Constants.KEY_INTENT_MESSAGE_ID, message.getMsg_ID());
                intent.putExtra(Constants.KEY_INTENT_STARTED_BY, Constants.KEY_INTENT_STARTED_BY_SEARCH);
                startActivity(intent);
            }
        });
    }

    private void setView() {
        ab = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.BLACK);
        ab.setBackgroundDrawable(colorDrawable);

        tv_noResults = findViewById(R.id.MainChat_tv_Search_noResultsFound);
        rv_chatList = findViewById(R.id.MainChat_List_RecyclerView);
        rv_searchMessages = findViewById(R.id.MainChat_MessageSearch_List_RecyclerView);
        rv_searchUsers = findViewById(R.id.MainChat_UserSearch_List_RecyclerView);
        FloatingActionButton fab_startNew = findViewById(R.id.MainChat_floatButton_newMessage);

        fab_startNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), StartNew.class));
            }
        });

    }
}

