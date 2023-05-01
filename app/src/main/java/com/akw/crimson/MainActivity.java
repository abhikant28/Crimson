package com.akw.crimson;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.akw.crimson.Backend.Communications.Communicator;
import com.akw.crimson.Backend.Database.SharedPrefManager;
import com.akw.crimson.Registration.Registration_Main;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onResume() {
        super.onResume();
        //Log.i("MAIN ACTIVITY:::::","ONCEEEEEEEE");
        SharedPreferences loginPreferences = getSharedPreferences(SharedPrefManager.SHARED_PREF_NAME, MODE_PRIVATE);
        if (loginPreferences.getBoolean("LOGGED_IN", false)) { //How can I ask here?
            Log.i("MAIN ACTIVITY:::::","LOGGED IN");
            startService(new Intent(this, Communicator.class));
            Intent intent=new Intent(getApplicationContext(), MainChatList.class);
            this.startActivity(intent);
        } else {
            Log.i("MAIN ACTIVITY:::::","LOGGED OUT");
            this.startActivity(new Intent(this, Registration_Main.class));
        }
        finish();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

    }
}