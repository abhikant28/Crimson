package com.akw.crimson;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Communications.Communicator;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.TheViewModel;
import com.akw.crimson.Chat.ChatActivity;
import com.akw.crimson.databinding.ActivityProfileViewBinding;

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
            }
        });
        binding.profileViewIbSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra(Constants.KEY_INTENT_USERID, user.getUser_id());
                startActivity(intent);
            }
        });
    }

    private void setValues() {
        binding.profileViewIvProfilePic.setImageBitmap(user.getPicBitmap());
        binding.profileViewTvName.setText(user.getDisplayName());
        binding.profileViewTvAbout.setText(user.getAbout());
        binding.profileViewTvNumber.setText(user.getPhoneNumber());
        binding.profileViewTvMediaCount.setText(String.valueOf(user.getMediaCount()));
        if (user.getMediaCount() != 0) {
            binding.profileViewClGroups.setVisibility(View.VISIBLE);
            binding.profileViewTvMediaCount.setText(String.valueOf(user.getMediaCount()));
            binding.profileViewRvMediaList.setAdapter(null);
        }
        if (user.getGroupCount() != 0) {
            binding.profileViewTvGroups.setText(user.getGroupCount() + " groups");

            if (user.getGroupCount() > 3) {
                binding.profileViewTvMoreGroups.setVisibility(View.VISIBLE);
            }
        }
    }
}