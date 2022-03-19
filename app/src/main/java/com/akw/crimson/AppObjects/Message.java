package com.akw.crimson.AppObjects;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "messages_Table")
public class Message {

    @PrimaryKey(autoGenerate = true)
    private String local_msg_ID;
    private String msg_ID;
    private String user_id;
    private String tag;
    private String msg;
    private String time;
    private boolean self;
    private boolean unread;

    private boolean media;
    private String mediaID;

    private int status;

    public Message( String user_id, String tag, String msg, String time, boolean self, boolean unread, boolean media, String mediaID, int status) {
        this.user_id = user_id;
        this.tag = tag;
        this.msg = msg;
        this.time = time;
        this.self = self;
        this.unread = unread;
        this.media = media;
        this.mediaID = mediaID;
        this.status = status;
        this.msg_ID=user_id+local_msg_ID;
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
