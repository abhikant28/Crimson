package com.example.crimson.AppObjects;

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
}
