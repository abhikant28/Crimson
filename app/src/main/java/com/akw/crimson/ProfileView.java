package com.akw.crimson;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Backend.Adapters.Chat_RecyclerAdapter;
import com.akw.crimson.Backend.Adapters.MediaListAdapter;
import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Communications.Communicator;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.TheViewModel;
import com.akw.crimson.Chat.ChatActivity;
import com.akw.crimson.databinding.ActivityProfileViewBinding;

import java.util.ArrayList;
import java.util.List;

public class ProfileView extends AppCompatActivity {

    ActivityProfileViewBinding binding;
    TheViewModel db;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);
        binding = ActivityProfileViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = Communicator.localDB;
        user = db.getUser(getIntent().getStringExtra(Constants.KEY_INTENT_USERID));

        setValues();
        setClicks();

        Log.i("USER::::::",user.getDisplayName());

        setTitle(user.getDisplayName());

    }

    private void setClicks() {
        binding.profileViewIbMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra(Constants.KEY_INTENT_USERID, user.getUser_id());
                startActivity(intent);
                finish();
            }
        });
        binding.profileViewIbSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra(Constants.KEY_INTENT_USERID, user.getUser_id());
                startActivity(intent);
                finish();
            }
        });
    }

    private void setValues() {
        binding.profileViewIvProfilePic.setImageBitmap(user.getPicBitmap());
        binding.profileViewTvName.setText(user.getDisplayName());
        binding.profileViewTvAbout.setText(user.getAbout());
        binding.profileViewTvNumber.setText(user.getPhoneNumber());
        binding.profileViewTvMediaCount.setText(String.valueOf(user.getTotalMediaCount()));
        if (user.getTotalMediaCount() != 0) {
            List<Message> mediaList=db.getUserMedia(user.getUser_id());
            binding.profileViewClMedia.setVisibility(View.VISIBLE);
            binding.profileViewTvMediaCount.setText(String.valueOf(user.getTotalMediaCount()));
            binding.profileViewRvMediaList.setVisibility(View.VISIBLE);
            MediaListAdapter mediaAdapter = new MediaListAdapter(user,mediaList);
            binding.profileViewRvMediaList.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
            binding.profileViewRvMediaList.setAdapter(mediaAdapter);
        }
        if (user.getGroupCount() != 0) {
            binding.profileViewTvGroups.setText(user.getGroupCount() + " groups");

            if (user.getGroupCount() > 3) {
                binding.profileViewTvMoreGroups.setVisibility(View.VISIBLE);
            }
        }
    }
}