package com.akw.crimson.Backend.AppObjects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.UsefulFunctions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Calendar;

@Entity(tableName = "messages_Table")
public class Message {
    @PrimaryKey
    @NonNull
    private String msg_ID;

    private String user_id, _id, taggedMsgID, msg, sentTime, receivedTime, readTime, mediaID, source, reaction, author, groupUserID, groupData;
    private long mediaSize;
    private double latitude, longitude;
    private boolean self, unread, starred, media, forwarded, link;
    private int status, mediaType, msgType = Constants.Message.MESSAGE_TYPE_TEXT;

    public String asString(String selfID) {
        Gson gson = new Gson();
        Message msg = new Message(this);
        if (msg.groupUserID == null) {
            msg.user_id = selfID;
        }
        if (self && msg.author == null && !msg.isForwarded())
            msg.author = selfID;
        if (msg.mediaType == Constants.Media.KEY_MESSAGE_MEDIA_TYPE_CAMERA_IMAGE)
            msg.mediaType = Constants.Media.KEY_MESSAGE_MEDIA_TYPE_IMAGE;
        if (msg.mediaType == Constants.Media.KEY_MESSAGE_MEDIA_TYPE_CAMERA_VIDEO)
            msg.mediaType = Constants.Media.KEY_MESSAGE_MEDIA_TYPE_VIDEO;
        return gson.toJson(msg);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return this.msg_ID.equals(((Message) obj).getMsg_ID());
    }


    @Ignore
    public Message(@NonNull String msg_ID, String user_id, String taggedMsgID, String msg, boolean self,
                   boolean media, String mediaID, int status, String author) {
        this.msg_ID = msg_ID;
        this.user_id = user_id;
        this.taggedMsgID = taggedMsgID;
        this.msg = msg;
        this.sentTime = UsefulFunctions.getCurrentTimestamp();
        this.unread = true;
        this.media = media;
        this.mediaID = mediaID;
        this.status = status;
        this.self = self;
        this.msgType = Constants.Message.MESSAGE_TYPE_TEXT;
        this.author = author;
    }

    //For Info
    @Ignore
    public Message(@NonNull String msg_ID, String user_id, String msg
            , String groupUserID, boolean self, int status, int msgType) {
        Calendar sentTime = Calendar.getInstance();
        this.msg_ID = msg_ID + sentTime.getTime().getTime();
        this.user_id = user_id;
        this.sentTime = UsefulFunctions.getCurrentTimestamp();
        this.msg = msg;
        this.groupUserID = groupUserID;
        this.self = self;
        this.status = status;
        this.msgType = msgType;
    }

    @Ignore
    public Message(@NonNull String msg_ID, String user_id, String taggedMsgID, String msg, String mediaID
            , long mediaSize, boolean self, boolean unread, boolean media, int status, int mediaType, String author) {
        Calendar sentTime = Calendar.getInstance();
        this.msg_ID = msg_ID + sentTime.getTime().getTime();
        this.sentTime = UsefulFunctions.getCurrentTimestamp();
        this.user_id = user_id;
        this.taggedMsgID = taggedMsgID;
        this.msg = msg;
        this.mediaID = mediaID;
        this.mediaSize = mediaSize;
        this.self = self;
        this.unread = unread;
        this.media = media;
        this.status = status;
        this.mediaType = mediaType;
        this.msgType = Constants.Message.MESSAGE_TYPE_TEXT;
        this.author = author;
    }

    @Ignore
    public Message(String msg_ID, String taggedMsgID, String msg, String source, String reaction, int status, int msgType, String author) {
        Calendar sentTime = Calendar.getInstance();
        this.msg_ID = msg_ID + sentTime.getTime().getTime();
        this.sentTime = UsefulFunctions.getCurrentTimestamp();
        this.taggedMsgID = taggedMsgID;
        this.msg = msg;
        this.source = source;
        this.reaction = reaction;
        this.self = true;
        this.status = status;
        this.msgType = msgType;
        this.author = author;
    }


    @Ignore
    public Message(String taggedMsgID, String msg, String mediaID
            , long mediaSize, boolean self, boolean unread, boolean media, int status, int mediaType, String author) {
        Calendar sentTime = Calendar.getInstance();
        this.msg_ID = msg_ID + sentTime.getTime().getTime();
        this.taggedMsgID = taggedMsgID;
        this.msg = msg;
        this.sentTime = UsefulFunctions.getCurrentTimestamp();
        this.mediaID = mediaID;
        this.mediaSize = mediaSize;
        this.self = self;
        this.unread = unread;
        this.media = media;
        this.status = status;
        this.mediaType = mediaType;
        this.msgType = Constants.Message.MESSAGE_TYPE_TEXT;
        this.author = author;
    }


    //For Location Message
    @Ignore
    public Message(@NonNull String msg_ID, String user_id, String taggedMsgID, double latitude, double longitude, boolean self, String msg, int status, String author) {
        this.msg_ID = msg_ID;
        this.user_id = user_id;
        this.taggedMsgID = taggedMsgID;
        this.msg = msg;
        this.sentTime = UsefulFunctions.getCurrentTimestamp();
        ;
        this.source = user_id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.self = self;
        this.unread = false;
        this.status = status;
        this.mediaType = Constants.Media.KEY_MESSAGE_MEDIA_TYPE_LOCATION;
        this.author = author;
    }


    public Message(String s) {
        Gson gson = new Gson();
        Type type = new TypeToken<Message>() {
        }.getType();
        Message message = gson.fromJson(s, type);
        this.receivedTime = UsefulFunctions.getCurrentTimestamp();
        this.msg_ID = message.msg_ID;
        this.user_id = message.user_id;
        this.taggedMsgID = message.taggedMsgID;
        this.msg = message.msg;
        this.sentTime = message.sentTime;
        this.readTime = message.readTime;
        this.mediaID = message.mediaID;
        this.source = message.source;
        this.reaction = message.reaction;
        this.author = message.author;
        this.groupUserID = message.groupUserID;
        this.groupData = message.groupData;
        this.mediaSize = message.mediaSize;
        this.latitude = message.latitude;
        this.longitude = message.longitude;
        this.self = false;
        this.unread = message.unread;
        this.starred = message.starred;
        this.media = message.media;
        this.forwarded = message.forwarded;
        this.link = message.link;
        this.status = message.status;
        this.mediaType = message.mediaType;
        this.msgType = message.msgType;
    }

    public Message(@NonNull Message message) {
        this.msg_ID = message.msg_ID;
        this.user_id = message.user_id;
        this._id = message._id;
        this.taggedMsgID = message.taggedMsgID;
        this.msg = message.msg;
        this.sentTime = message.sentTime;
        this.receivedTime = message.receivedTime;
        this.readTime = message.readTime;
        this.mediaID = message.mediaID;
        this.source = message.source;
        this.reaction = message.reaction;
        this.author = message.author;
        this.groupUserID = message.groupUserID;
        this.groupData = message.groupData;
        this.mediaSize = message.mediaSize;
        this.latitude = message.latitude;
        this.longitude = message.longitude;
        this.self = message.self;
        this.unread = message.unread;
        this.starred = message.starred;
        this.media = message.media;
        this.forwarded = message.forwarded;
        this.link = message.link;
        this.status = message.status;
        this.mediaType = message.mediaType;
        this.msgType = message.msgType;
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

    public String getTaggedMsgID() {
        return taggedMsgID;
    }

    public void setTaggedMsgID(String taggedMsgID) {
        this.taggedMsgID = taggedMsgID;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTime() {
        if (self)
            return sentTime.substring(0, sentTime.lastIndexOf(","));
        return receivedTime.substring(0, receivedTime.lastIndexOf(","));
    }

    public String getDate() {
        if (self)
            return sentTime.substring(sentTime.lastIndexOf(",") + 2);
        return receivedTime.substring(sentTime.lastIndexOf(",") + 2);
    }

    public void setSentTime(String sentTime) {
        this.sentTime = sentTime;
    }

    public String getSentTime() {
        return sentTime;
    }

    public String getReceivedTime() {
        return receivedTime;
    }

    public void setReceivedTime(String receivedTime) {
        this.receivedTime = receivedTime;
    }

    public String getReadTime() {
        return readTime;
    }

    public void setReadTime(String readTime) {
        this.readTime = readTime;
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

    public int getMsgType() {
        if(msgType==0) return Constants.Message.MESSAGE_TYPE_TEXT;
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public boolean isLink() {
        return link;
    }

    public void setLink(boolean link) {
        this.link = link;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGroupUserID() {
        return groupUserID;
    }

    public void setGroupUserID(String groupUserID) {
        this.groupUserID = groupUserID;
    }

    public String getGroupData() {
        return groupData;
    }

    public void setGroupData(String groupData) {
        this.groupData = groupData;
    }


    public Message(@NonNull String msg_ID, String user_id, String taggedMsgID, String msg, String sentTime, String receivedTime, String readTime, String mediaID, String source, String reaction, String author, String groupUserID, String groupData, long mediaSize, double latitude, double longitude, boolean self, boolean unread, boolean starred, boolean media, boolean forwarded, boolean link, int status, int mediaType, int msgType) {
        this.msg_ID = msg_ID;
        this.user_id = user_id;
        this.taggedMsgID = taggedMsgID;
        this.msg = msg;
        this.sentTime = sentTime;
        this.receivedTime = receivedTime;
        this.readTime = readTime;
        this.mediaID = mediaID;
        this.source = source;
        this.reaction = reaction;
        this.author = author;
        this.groupUserID = groupUserID;
        this.groupData = groupData;
        this.mediaSize = mediaSize;
        this.latitude = latitude;
        this.longitude = longitude;
        this.self = self;
        this.unread = unread;
        this.starred = starred;
        this.media = media;
        this.forwarded = forwarded;
        this.link = link;
        this.status = status;
        this.mediaType = mediaType;
        this.msgType = msgType;
    }
}
