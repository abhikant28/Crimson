package com.akw.crimson;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.akw.crimson.Database.SharedPrefManager;
import com.akw.crimson.Registration.Registration_Main;
import com.akw.crimson.Registration.Registration_Profile;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        Log.i("MAIN ACTIVITY:::::","ONCEEEEEEEE");
        SharedPreferences loginPreferences = getSharedPreferences(SharedPrefManager.SHARED_PREF_NAME, MODE_PRIVATE);
        if (loginPreferences.getBoolean("LOGGED_IN", false)) { //How can I ask here?
            Log.i("MAIN ACTIVITY:::::","LOGGED IN");
            Intent intent=new Intent(getApplicationContext(), MainChatList.class);
            this.startActivity(intent);
        } else {
            Log.i("MAIN ACTIVITY:::::","LOGGED OUT");
            this.startActivity(new Intent(this, Registration_Main.class));
        }
        finish();
    }
}