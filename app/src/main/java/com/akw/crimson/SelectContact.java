package com.akw.crimson;

import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.akw.crimson.Backend.Adapters.AllUserList_RecyclerListAdapter;
import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Communications.Communicator;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.TheViewModel;

import java.util.List;

public class SelectContact extends BaseActivity {

    RecyclerView rv_contacts;
    TheViewModel db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contact);

        rv_contacts=findViewById(R.id.SelectContact_rv_contactList);
        db= Communicator.localDB;
        AllUserList_RecyclerListAdapter adapter= new AllUserList_RecyclerListAdapter();
        adapter.submitList(db.getAllUsersList().getValue());
        db.getAllUsersList().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                adapter.submitList(users);
            }
        });
        rv_contacts.setLayoutManager(new LinearLayoutManager(this));
        rv_contacts.setAdapter(adapter);
        adapter.setOnItemCLickListener(new AllUserList_RecyclerListAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(User user) {
                Intent intent=new Intent(getApplicationContext(),PrepareMessageActivity.class);
                intent.putExtra(Constants.KEY_INTENT_USERID,user.getUser_id());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}