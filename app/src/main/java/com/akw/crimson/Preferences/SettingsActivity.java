package com.akw.crimson.Preferences;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.akw.crimson.Backend.Database.SharedPrefManager;
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.R;

public class SettingsActivity extends AppCompatActivity {

    LinearLayout ll_profile;
    ImageView iv_profilePic;
    public TextView tv_userName,tv_status;
    ActionBar ab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        new SharedPrefManager(this);
        init();
        clicks();
    }

    private void clicks() {
        ll_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), EditProfile.class));
            }
        });
    }

    private void init() {
        ll_profile=findViewById(R.id.settings_ll_profile);
        ab=getSupportActionBar();
        iv_profilePic=findViewById(R.id.settings_ImageView_UserPic);
        tv_status=findViewById(R.id.settings_tv_status);
        tv_userName=findViewById(R.id.settings_tv_userName);

        tv_userName.setText(SharedPrefManager.getLocalUser().getUserName());
        if(SharedPrefManager.getLocalUser().getPic()!=null) {
            iv_profilePic.setImageBitmap(UsefulFunctions.decodeImage(SharedPrefManager.getLocalUser().getPic()));
        }else{
            iv_profilePic.setImageResource(R.drawable.ic_baseline_person_24);
        }

        ab.setTitle("Settings");
    }
}