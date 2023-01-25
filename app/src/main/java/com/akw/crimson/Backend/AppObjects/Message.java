package com.akw.crimson.Backend.AppObjects;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.akw.crimson.Backend.Constants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@Entity(tableName = "messages_Table")
public class Message {

    @PrimaryKey
    @NonNull
    private String msg_ID;
    private String user_id, _id, tag, msg, time, date, mediaID, source, reaction;
    private long mediaSize;
    private double latitude, longitude;
    private boolean self, unread, starred;
    private boolean media;

    private boolean forwarded;
    private int status, mediaType;


    public Message(@NonNull String msg_ID, String user_id, String tag, String msg, boolean self,
                   boolean media, String mediaID, int status, Calendar time) {
        this.msg_ID = msg_ID;
        this.user_id = user_id;
        this.tag = tag;
        this.msg = msg;
        this.time = (time == null) ? "12:00 " : (String.format("%02d", time.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", time.get(Calendar.MINUTE)));
        this.date = new SimpleDateFormat("dd/MM/yyyy").format(time.getTime());
        this.unread = true;
        this.media = media;
        this.mediaID = mediaID;
        this.status = status;
        this.self = self;
    }

    @Ignore
    public Message(@NonNull String msg_ID, String user_id, String tag, String msg, String mediaID
            , long mediaSize, boolean self, boolean unread, boolean media, int status, int mediaType) {
        Calendar time = Calendar.getInstance();
        this.msg_ID = msg_ID + time.getTime().getTime();
        this.user_id = user_id;
        this.tag = tag;
        this.msg = msg;
        this.time = String.format("%02d", time.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", time.get(Calendar.MINUTE));
        this.date = new SimpleDateFormat("dd/MM/yyyy").format(time.getTime());
        this.mediaID = mediaID;
        this.mediaSize = mediaSize;
        this.self = self;
        this.unread = unread;
        this.media = media;
        this.status = status;
        this.mediaType = mediaType;
    }

    @Ignore
    public Message(@NonNull String msg_ID, String user_id, String tag, String msg, boolean self,
                   boolean media, String mediaID, int status) {
        Calendar time = Calendar.getInstance();
        this.msg_ID = msg_ID;
        this.user_id = user_id;
        this.tag = tag;
        this.msg = msg;
        this.time = (String.format("%02d", time.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", time.get(Calendar.MINUTE)));
        this.date = new SimpleDateFormat("dd/MM/yyyy").format(time.getTime());
        this.unread = true;
        this.media = media;
        this.mediaID = mediaID;
        this.status = status;
        this.self = self;
    }

    public Message(String[] s) {
        Calendar time = Calendar.getInstance();
        this.msg_ID = s[0].substring(1);
        this.user_id = s[1];
        this.tag = s[2].equals("NULL") ? null : s[2];
        String msg = s[3].replaceAll("%c%", ",").replaceAll("%q%", "\"").replaceAll("%u%", "_");
        this.msg = msg.substring(1, msg.length() - 1);
        this.time = String.format("%02d", time.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", time.get(Calendar.MINUTE));
        this.date = new SimpleDateFormat("dd/MM/yyyy").format(time.getTime());
        this.unread = false;
        this.media = Boolean.parseBoolean(s[4]);
        this.mediaID = s[5].substring(0, s[5].length() - 1).equals("NULL") ? null : s[5].substring(0, s[5].length() - 1);
        this.status = 2;
        this.self = false;
    }

    public Message(String s) {
        Gson gson = new Gson();
        Type type = new TypeToken<Message>() {
        }.getType();
        Message message = gson.fromJson(s, type);
        this.msg_ID = message.msg_ID;
        this.user_id = message.user_id;
        this.tag = message.tag;
        this.msg = message.msg;
        Calendar time = Calendar.getInstance();
        this.time = String.format("%02d", time.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", time.get(Calendar.MINUTE));
        this.date = new SimpleDateFormat("dd/MM/yyyy").format(time.getTime());
        this.mediaID = message.mediaID;
        this.mediaSize = message.mediaSize;
        this.latitude = message.latitude;
        this.longitude = message.longitude;
        this.self = false;
        this.unread = true;
        this.media = message.media;
        this.status = 2;
        this.mediaType = message.mediaType;
    }

    public Message(@NonNull Message message) {
        this.msg_ID = message.msg_ID;
        this.user_id = message.user_id;
        this.tag = message.tag;
        this.msg = message.msg;
        this.time = message.time;
        this.date = message.date;
        this.mediaID = message.mediaID;
        this.mediaSize = message.mediaSize;
        this.latitude = message.latitude;
        this.longitude = message.longitude;
        this.self = message.self;
        this.unread = message.unread;
        this.media = message.media;
        this.status = message.status;
        this.mediaType = message.mediaType;
    }


    public String asString(String selfID) {
        Gson gson = new Gson();
        Message msg = new Message(this);
        msg.user_id = selfID;
//        msg.setMediaID(msg_ID+"_"+mediaID);
        return gson.toJson(msg);
//        return "[" + msg_ID + "," + selfID + "," + tag + "," + "\"" + msg.replaceAll("\"", "%q%").replaceAll(",", "%c%").replaceAll("_", "%u%") + "\"" + "," + media + "," + (mediaID == null ? "NULL" : mediaID) + "]";
    }

    public Message(@NonNull String msg_ID, String user_id, String tag, double latitude, double longitude, boolean self, String msg, int status) {
        this.msg_ID = msg_ID;
        this.user_id = user_id;
        this.tag = tag;
        this.msg = msg;
        Calendar c = Calendar.getInstance();
        this.time = String.format("%02d", c.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", c.get(Calendar.MINUTE));
        this.date = new SimpleDateFormat("dd/MM/yyyy").format(c.getTime());
        this.source = user_id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.self = self;
        this.unread = false;
        this.status = status;
        this.mediaType = Constants.KEY_MESSAGE_MEDIA_TYPE_LOCATION;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getMediaSize() {
        return mediaSize;
    }

    public void setMediaSize(long mediaSize) {
        this.mediaSize = mediaSize;
    }

    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
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
        if (tag == null) tag = Constants.KEY_FCM_TYPE_MSG;
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

    public boolean isForwarded() {
        return forwarded;
    }

    public void setForwarded(boolean forwarded) {
        this.forwarded = forwarded;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getReaction() {
        return reaction;
    }

    public void setReaction(String reaction) {
        this.reaction = reaction;
    }

    public boolean isStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }
}
