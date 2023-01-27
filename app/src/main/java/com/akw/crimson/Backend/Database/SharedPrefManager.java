package com.akw.crimson.Backend.Database;

import android.content.Context;
import android.content.SharedPreferences;

import com.akw.crimson.Backend.AppObjects.PreparedMessage;
import com.akw.crimson.Backend.AppObjects.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class SharedPrefManager {
    public static final String SHARED_PREF_NAME = "CRIMSON_SHAREDPREFERENCE";
    public static final String KEY_ACCESS_TOKEN = "token";

    private static Context mctx;
    private static SharedPrefManager instance;


    public SharedPrefManager(Context cxt) {
        mctx = cxt;
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null)
            instance = new SharedPrefManager(context);
        return instance;
    }

    public static boolean storeToken(String token) {
        SharedPreferences sharedPreferences = mctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_ACCESS_TOKEN, token);
        editor.apply();
        return true;
    }

    public static String getToken() {
        SharedPreferences sharedPreferences = mctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
    }

    public static boolean storeUser(User user, String token) {
        SharedPreferences sharedPreferences = mctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("USER_ID", user.getUser_id());
        editor.putString("USER_NAME", user.getName());
        editor.putString("USER_PIC", user.getPic());
        editor.putString("USER_PHONE", user.getPhoneNumber());
        editor.putString("USER_TOKEN", token);
        editor.apply();
        return true;
    }

    public static boolean storeUser(User user) {
        SharedPreferences sharedPreferences = mctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("USER_ID", user.getUser_id());
        editor.putString("USER_NAME", user.getName());
        editor.putString("USER_PIC", user.getPic());
        editor.putString("USER_PHONE", user.getPhoneNumber());
        editor.apply();
        return true;
    }

    public static boolean storeUserProfile(String profilePic, String userName, String address, String about) {
        SharedPreferences sharedPreferences = mctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("USER_NAME", userName);
        if (profilePic != null) {
            editor.putString("USER_PIC", about);
        }
        editor.putString("USER_EMAIL", address);
        editor.putString("USER_ABOUT", address);
        editor.putBoolean("LOGGED_IN", true);
        editor.apply();
        return true;
    }

    public static void storeUserToken(String token) {
        SharedPreferences sharedPreferences = mctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("FIREBASE_TOKEN", token);
        editor.apply();
    }

    public static boolean storeUserNumber(String num, String userID) {
        SharedPreferences sharedPreferences = mctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("USER_PHONE", num);
        editor.putString("USER_ID", userID);
        editor.putBoolean("PHONE_VERIFIED", true);
        editor.apply();
        return true;
    }

    public static User getLocalUser() {
        SharedPreferences sp = mctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        User lUser = new User(sp.getString("USER_ID", null), null, sp.getString("USER_NAME", null)
                , sp.getString("USER_PIC", null), sp.getString("USER_PHONE", null), true);
        lUser.setUserName(sp.getString("USER_NAME", null));
        return lUser;
    }

    public static String getLocalUserID() {
        SharedPreferences sharedPreferences = mctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString("USER_ID", null);
    }

    public static String getLocalPhoneNumber() {
        SharedPreferences sharedPreferences = mctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString("USER_PHONE", null);
    }

    public static void putPreparedMessages(ArrayList<PreparedMessage> newMessages) {
        Gson gson = new Gson();
        String json = gson.toJson(newMessages);
        SharedPreferences sharedPreferences = mctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("PREPARED_MESSAGES", json);
        editor.apply();
    }

    public static ArrayList<PreparedMessage> getPreparedMessages() {
        SharedPreferences sharedPreferences = mctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String s = sharedPreferences.getString("PREPARED_MESSAGES", null);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<PreparedMessage>>() {
        }.getType();
        ArrayList<PreparedMessage> messages = gson.fromJson(s, type);
        if (messages == null) messages = new ArrayList<>();
        return messages;
    }

}
