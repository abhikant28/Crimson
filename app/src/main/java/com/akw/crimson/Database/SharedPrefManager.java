package com.akw.crimson.Database;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.akw.crimson.AppObjects.User;

public class SharedPrefManager {
    public static final String SHARED_PREF_NAME="CRIMSON_SHAREDPREFERENCE";
    public static final String KEY_ACCESS_TOKEN="token";

    private static Context mctx;
    private static SharedPrefManager instance;


    public SharedPrefManager(Context cxt){
        mctx=cxt;
    }

    public static synchronized  SharedPrefManager getInstance(Context context){
        if(instance==null)
            instance= new SharedPrefManager(context);
        return instance;
    }

    public boolean storeToken(String token){
        SharedPreferences sharedPreferences= mctx.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor= sharedPreferences.edit();
        editor.putString(KEY_ACCESS_TOKEN,token);
        editor.apply();
        return true;
    }
    public String getToken(){
        SharedPreferences sharedPreferences= mctx.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_ACCESS_TOKEN,null);
    }
    public boolean storeUser(User user,String token){
        SharedPreferences sharedPreferences= mctx.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor= sharedPreferences.edit();
        editor.putString("USER_ID", user.get_id());
        editor.putString("USER_NAME", user.getName());
        editor.putString("USER_PIC", user.getPic());
        editor.putString("USER_PHONE",user.getPhoneNumber());
        editor.putString("USER_TOKEN", token);
        editor.apply();
        return true;
    }
    public boolean storeUserProfile(String profilePic,String userName,String address){
        SharedPreferences sharedPreferences= mctx.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor= sharedPreferences.edit();
        editor.putString("USER_NAME", userName);
        editor.putString("USER_PIC", profilePic);
        editor.putString("USER_EMAIL", address);
        editor.putBoolean("LOGGED_IN", true);
        editor.apply();
        Log.i("SHARED PREFS::::", "APPLIED");
        return true;
    }
    public void storeUserToken(String token){
        SharedPreferences sharedPreferences= mctx.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor= sharedPreferences.edit();
        editor.putString("FIREBASE_TOKEN", token);
        editor.apply();
        return;
    }
    public boolean storeUserNumber(String num,String userID){
        SharedPreferences sharedPreferences= mctx.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor= sharedPreferences.edit();
        editor.putString("USER_PHONE",num);
        editor.putString("USER_ID", userID);
        editor.putBoolean("PHONE_VERIFIED", true);
        editor.apply();
        return true;
    }
    public User getLocalUser(){
        SharedPreferences sp= mctx.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        User lUser=new User(sp.getString("USER_ID",null ),null,sp.getString("USER_NAME",null )
        ,sp.getString("USER_PIC", null), sp.getString("USER_PHONE", null),true);
        return lUser;
    }
    public String getLocalUserID(){
        SharedPreferences sharedPreferences= mctx.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        return sharedPreferences.getString("USER_ID", null);
    }
    public String getLocalPhoneNumber(){
        SharedPreferences sharedPreferences= mctx.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        return sharedPreferences.getString("USER_PHONE", null);
    }

}
