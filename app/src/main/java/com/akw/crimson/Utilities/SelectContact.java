package com.akw.crimson.Utilities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Backend.Adapters.AllUserList_RecyclerListAdapter;
import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Communications.Communicator;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.TheViewModel;
import com.akw.crimson.BaseActivity;
import com.akw.crimson.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;

public class SelectContact extends BaseActivity {

    RecyclerView rv_contacts;
    FloatingActionButton fb_submit;

    TheViewModel db;
    String userList;
    AllUserList_RecyclerListAdapter adapter = new AllUserList_RecyclerListAdapter();
    public static HashSet<User> selectedUsers = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contact);
        if (getIntent().getExtras() != null) {
            int type = getIntent().getExtras().getInt(Constants.KEY_INTENT_TYPE);
            if (type == Constants.KEY_INTENT_TYPE_SINGLE_SELECT) {
                adapter = new AllUserList_RecyclerListAdapter(false, this);
            } else if (type == Constants.KEY_INTENT_TYPE_MULTI_SELECT) {
                adapter = new AllUserList_RecyclerListAdapter(true, this);
            }
        }
        if (getIntent().getExtras() != null && getIntent().getExtras().getString(Constants.KEY_INTENT_USER_LIST) != null) {
            Gson gson = new Gson();
            Type gType = new TypeToken<ArrayList<User>>() {
            }.getType();
            ArrayList<User> users = gson.fromJson(getIntent().getExtras().getString(Constants.KEY_INTENT_USER_LIST), gType);
            selectedUsers.addAll(users);
        }
        Log.i("SELECTED USERS:::::", selectedUsers.toString());

        db = Communicator.localDB;
        setViews();


        adapter.setOnItemCLickListener((user, tv_name, tv_lastMsg, itemView) -> {
            if (getIntent().getExtras().getInt(Constants.KEY_INTENT_TYPE) == Constants.KEY_INTENT_TYPE_MULTI_SELECT) {
                if (!selectedUsers.contains(user)) {
                    selectedUsers.add(user);
                    itemView.setBackgroundColor(Color.parseColor("#C6C5C5"));
                    tv_lastMsg.setTextColor(Color.BLACK);
                    tv_name.setTextColor(Color.BLACK);
                } else {
                    itemView.setBackgroundColor(Color.BLACK);
                    tv_lastMsg.setTextColor(Color.WHITE);
                    tv_name.setTextColor(Color.WHITE);
                    selectedUsers.remove(user);
                }
                updateViews();
            } else if (getIntent().getExtras().getInt(Constants.KEY_INTENT_TYPE) == Constants.KEY_INTENT_TYPE_SINGLE_SELECT) {
                Intent intent = new Intent();
                intent.putExtra(Constants.KEY_INTENT_USERID, user.getUser_id());
                setResult(RESULT_OK, intent);
                finish();
            }
        });

    }

    public void updateViews() {
        if (selectedUsers.size() == 0) {
            findViewById(R.id.SelectContact_ll_userList).setVisibility(View.GONE);
            fb_submit.setVisibility(View.GONE);
            return;
        } else {
            findViewById(R.id.SelectContact_ll_userList).setVisibility(View.VISIBLE);
            fb_submit.setVisibility(View.VISIBLE);

        }
        userList = "";
        for (User u : selectedUsers)
            userList += u.getDisplayName() + ", ";
        userList = userList.substring(0, userList.length() - 2);
        TextView tv_usersList = findViewById(R.id.SelectContact_tv_userList);
        tv_usersList.setText(userList);
    }

    private void setViews() {
        rv_contacts = findViewById(R.id.SelectContact_rv_contactList);
        fb_submit = findViewById(R.id.SelectContact_fb_submit);
        rv_contacts.setLayoutManager(new LinearLayoutManager(this));

        adapter.submitList(db.getAllUsersList().getValue());
        db.getAllUsersList().observe(this, users -> adapter.submitList(users));

        rv_contacts.setAdapter(adapter);

        fb_submit.setOnClickListener(view -> {
            Intent intent = new Intent();
            Gson gson = new Gson();
            String json = gson.toJson(selectedUsers);
            intent.putExtra(Constants.KEY_INTENT_USER_LIST, json);
            setResult(RESULT_OK, intent);
            finish();
        });
    }
}