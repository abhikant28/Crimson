package com.akw.crimson;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.akw.crimson.Database.SharedPrefManager;
import com.akw.crimson.Registration.Registration_Main;

public class MainActivity extends AppCompatActivity {

    public static final String TEXT= "text";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        SharedPreferences loginPreferences = getSharedPreferences(SharedPrefManager.SHARED_PREF_NAME, MODE_PRIVATE);
        if (loginPreferences.getBoolean("LOGGED_IN",false)) { //How can I ask here?
            this.startActivity(new Intent(this, MainChatList.class));
        } else {
            this.startActivity(new Intent(this, Registration_Main.class));
        }
        finish();
    }

}