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
    public static final String SHARED_PREF_NAME = "CRIMSON_SHARED_PREFERENCE", KEY_ACCESS_TOKEN = "token";
    public static String USERID = null;


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

    public static boolean storeUser(User user) {
        SharedPreferences sharedPreferences = mctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(user);
        editor.putString("USER", json);
//        editor.putString("USER_ID", user.getUser_id());
//        editor.putString("USER_ABOUT", user.getAbout());
//        editor.putString("USER_NAME", user.getName());
//        editor.putString("USER_PIC", user.getPublicPic());
//        editor.putString("USER_PHONE", user.getPhoneNumber());
//        editor.putString("USER_STATUS", user.getStatus());
//        editor.putString("USER_PROFILE_PIC", user.getProfilePic());
        editor.apply();

        return true;
    }

    public static boolean storeUserProfile(String publicPic, String userName, String address, String about) {
        SharedPreferences sharedPreferences = mctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        User user = getLocalUser();
        user.setUserName(userName);
        user.setAbout(about);
        Gson gson = new Gson();
        if (publicPic != null) {
            user.setPublicPic(publicPic);
        }
        String json = gson.toJson(user);
        editor.putBoolean("LOGGED_IN", true);
        editor.putString("USER", json);
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
        Gson gson = new Gson();
        SharedPreferences sharedPreferences = mctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("PHONE_VERIFIED", true);
        User user = getLocalUser();
        user.setPhoneNumber(num);
        user.setUser_id(userID);
        String json = gson.toJson(user);
        editor.putString("USER", json);
        editor.apply();
        return true;
    }

    public static User getLocalUser() {
        SharedPreferences sp = mctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String u = sp.getString("USER", null);
        User user;
        if (u == null) {
            user = new User();
        } else {
            Gson gson = new Gson();
            Type type = new TypeToken<User>() {
            }.getType();
            user = gson.fromJson(u, type);
        }
//        User lUser = new User(sp.getString("USER_ID", null), null, sp.getString("USER_NAME", null)
//                , sp.getString("USER_PIC", null), sp.getString("USER_PHONE", null)
//                , true, sp.getString("USER_ABOUT", null));
//        lUser.setProfilePic(sp.getString("USER_PROFILE_PIC", null));
//        lUser.setStatus(sp.getString("USER_STATUS", null));
//        lUser.setUserName(sp.getString("USER_NAME", null));
        return user;
    }

    public static String getLocalUserID() {
        if (USERID == null) {
            USERID = getLocalUser().getUser_id();
        }
        return USERID;
    }

    public static String getLocalPhoneNumber() {
        return getLocalUser().getPhoneNumber();
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
