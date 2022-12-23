package com.akw.crimson.AppObjects;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@Entity(tableName = "messages_Table")
public class Message {

    @PrimaryKey
    @NonNull private String local_msg_ID;
    private String msg_ID;
    private String user_id;
    private String _id;
    private String tag;
    private String msg;
    private String time;
    private String date;
    private boolean self;
    private boolean unread;
    private boolean media;
    private String mediaID;
    private int status;


    public String asString(String selfID) {
        //return "{"+msg_ID+","+selfID+","+tag+","+msg+","+time+","+date+","+media+","+mediaID+"}";
         return "["+msg_ID+","+selfID+","+tag+","+ "\""+msg.replaceAll("\"", "%q%").replaceAll(",", "%c%").replaceAll("_","%u%")+"\""+","+media+","+mediaID+"]";

    }

    public Message(String msg_ID, String user_id, String tag, String msg,boolean self, boolean media, String mediaID, int status) {
        Calendar time = Calendar.getInstance();
        this.local_msg_ID = msg_ID+"_"+user_id;
        this.msg_ID = msg_ID;
        this.user_id = user_id;
        this.tag = tag;
        this.msg = msg;
        this.time = (time==null)?"12:00 ":(String.format("%02d", time.get(Calendar.HOUR_OF_DAY))+":"+String.format("%02d",time.get(Calendar.MINUTE)));
        this.date = new SimpleDateFormat("dd/MM/yyyy").format(time.getTime());
        this.unread = true;
        this.media = media;
        this.mediaID = mediaID;
        this.status = status;
        this.self=self;
    }


    public Message(String[] s){
        Calendar time = Calendar.getInstance();
        this.local_msg_ID = msg_ID+"_"+user_id;
        this.msg_ID = s[0];
        this.user_id = s[1];
        this.tag = s[2];
        this.msg = s[3].replaceAll("%c%", ",").replaceAll("%q%", "\"").replaceAll("%u%", "_");
        this.time = (time==null)?"12:00 ":(String.format("%02d", time.get(Calendar.HOUR_OF_DAY))+":"+String.format("%02d",time.get(Calendar.MINUTE)));
        this.date = new SimpleDateFormat("dd/MM/yyyy").format(time.getTime());
        this.unread = false;
        this.media = Boolean.parseBoolean(s[4]);
        this.mediaID = s[5];
        this.status = 4;
        this.self=false;
    }

    public Message(String msg_id, String user_id, String tag, String msg, boolean self, boolean media, String mediaID, int status, String time, String date) {
        this.local_msg_ID = msg_ID+"_"+user_id;
        this.msg_ID = msg_id;
        this.user_id = user_id;
        this.tag = tag;
        this.msg = msg;
        this.time = time;
        this.date = date;
        this.unread = true;
        this.media = media;
        this.mediaID = mediaID;
        this.status = status;
        this.self=self;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getLocal_msg_ID() {
        return local_msg_ID;
    }

    public void setLocal_msg_ID(String local_msg_ID) {
        this.local_msg_ID = local_msg_ID;
    }

    public String getMsg_ID() {
        return msg_ID;
    }

    public void setMsg_ID(String msg_ID) {
        this.msg_ID = msg_ID;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isSelf() {
        return self;
    }

    public void setSelf(boolean self) {
        this.self = self;
    }

    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }

    public boolean isMedia() {
        return media;
    }

    public void setMedia(boolean media) {
        this.media = media;
    }

    public String getMediaID() {
        return mediaID;
    }

    public void setMediaID(String mediaID) {
        this.mediaID = mediaID;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


}
