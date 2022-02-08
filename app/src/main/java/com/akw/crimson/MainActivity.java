package com.akw.crimson;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    public static final String SHARED_PREFS= "SharedPrefs";
    public static final String TEXT= "text";
    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



    }

    public void saveData(String token,String phoneNo,String profilePic,String userName,String userID,String address){
        SharedPreferences sharedPreferences= getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("TOKEN", token);
        editor.putBoolean("LOGGED_IN",true);
        editor.putString("PHONE_NUMBER", phoneNo);
        editor.putString("PROFILE_PICTURE", profilePic);
        editor.putString("USER_NAME", userName);
        editor.putString("USER_ID", userID);
        editor.putString("ADDRESS", address);
        editor.apply();
    }

    public void getData(){
        SharedPreferences sharedPreferences= getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        text= sharedPreferences.getString(TEXT, "VALUE NOT FOUND!");

    }

}