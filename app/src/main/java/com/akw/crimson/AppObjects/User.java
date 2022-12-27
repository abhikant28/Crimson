package com.akw.crimson.AppObjects;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_table")
public class User {

    @PrimaryKey
    @NonNull
    private String user_id;
    private String _id;
    private String last_msg, name, userName, displayName, time, pic = "", phoneNumber, date;
    private boolean unread, connected;
    private int unread_count = 0;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        Log.i("UpDATING Connected:::", connected+"_"+displayName);
        this.connected = connected;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

//
//
//    @Ignore
//    public User(String user_id, String last_msg, String name, String pic, String phoneNumber,boolean connected) {
//        this.user_id = user_id;
//        this.last_msg = last_msg;
//        this.name = name;
//        this.pic=pic;
//        this.phoneNumber=phoneNumber;
//        this.connected=connected;
//    }

    public User(String user_id, String name, String displayName, String pic, String phoneNumber, boolean connected) {
        this.user_id = user_id;
        this.name = name;
        this.displayName = displayName;
        this.pic = pic;
        this.phoneNumber = phoneNumber;
        this.connected = connected;
    }

    public String getPic() {
        return pic;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getLast_msg() {
        return last_msg;
    }

    public void setLast_msg(String last_msg) {
        this.last_msg = last_msg;
    }

    public int getUnread_count() {
        return unread_count;
    }

    public void setUnread_count(int unread_count) {
        this.unread_count = unread_count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }


    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
