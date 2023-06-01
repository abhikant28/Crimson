package com.akw.crimson.Backend.AppObjects;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.DAOs.DataConverter;
import com.akw.crimson.Backend.UsefulFunctions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;

@Entity(tableName = "user_table")
public class User {

    @PrimaryKey
    @NonNull
    private String user_id;
    private String _id;
    private String last_msg, name, userName, displayName, time, pic, phoneNumber, date, about, wallpaper, status, groupData;
    private boolean unread, connected, blocked, mute, known, pinned;
    private int unread_count, last_msg_type = 0, vidCount = 0, imgCount = 0, docCount = 0, msg_count = 0, type = 0;
    private long mediaSize;
    @TypeConverters(DataConverter.class)
    private ArrayList<String> groups, medias, links, docs;
    @TypeConverters(DataConverter.class)
    public Group group;

    //Used For Groups
    @Ignore
    public User(@NonNull String user_id, String displayName, String pic, String groupData, int type, Group group) {
        this.user_id = user_id;
        this.displayName = displayName;
        this.pic = pic;
        this.groupData = groupData;
        this.connected=true;
        this.known=true;
        this.group=group;
        this.type = type;
    }

    public User(@NonNull String user_id, String name, String displayName, String pic, String phoneNumber, boolean connected, String about) {
        this.user_id = user_id;
        this.name = name;
        this.displayName = displayName;
        this.pic = pic;
        this.phoneNumber = phoneNumber;
        this.connected = connected;
        this.about = about;
    }

    public User(@NonNull String user_id, String username, String name, String displayName, String pic, String phoneNumber, boolean connected) {
        this.user_id = user_id;
        this.name = name;
        this.userName = username;
        this.displayName = displayName;
        this.pic = pic;
        this.phoneNumber = phoneNumber;
        this.connected = connected;
    }

    public User(@NonNull String user_id, String username, String name, String displayName, String pic, String phoneNumber, boolean connected, String about) {
        this.user_id = user_id;
        this.name = name;
        this.userName = username;
        this.displayName = displayName;
        this.pic = pic;
        this.phoneNumber = phoneNumber;
        this.connected = connected;
        this.about = about;
    }

    public User(String u) {
        Gson gson = new Gson();
        Type type = new TypeToken<User>() {
        }.getType();
        User user = gson.fromJson(u, type);
        this.user_id = user.user_id;
        this._id = user._id;
        this.last_msg = user.last_msg;
        this.name = user.name;
        this.userName = user.userName;
        this.displayName = user.displayName;
        this.time = user.time;
        this.pic = user.pic;
        this.phoneNumber = user.phoneNumber;
        this.date = user.date;
        this.about = user.about;
        this.wallpaper = user.wallpaper;
        this.status = user.status;
        this.groupData = user.groupData;
        this.unread = user.unread;
        this.connected = user.connected;
        this.blocked = user.blocked;
        this.mute = user.mute;
        this.known = user.known;
        this.pinned = user.pinned;
        this.unread_count = user.unread_count;
        this.last_msg_type = user.last_msg_type;
        this.vidCount = user.vidCount;
        this.imgCount = user.imgCount;
        this.docCount = user.docCount;
        this.msg_count = user.msg_count;
        this.type = user.type;
        this.mediaSize = user.mediaSize;
        this.groups = user.groups;
        this.medias = user.medias;
        this.links = user.links;
        this.docs = user.docs;
        this.group = user.group;
    }

    public int getGroupCount() {
        return getGroups().size();
    }

    public int getTotalMediaCount() {
        return getMedias().size() + getDocs().size() + getLinks().size();
    }

    public int getDocCount() {
        return getDocs().size();
    }

    public void setDocCount(int docCount) {
        this.docCount = docCount;
    }

    public int getLinksCount() {
        return getLinks().size();
    }

    public int getPicMediaCount() {
        return getMedias().size();
    }

    public boolean addMedia(String id) {
        if (id.startsWith("IMG_")) imgCount++;
        else vidCount++;
        return getMedias().add(id);
    }

    public boolean removeMedia(String id) {
        if (id.startsWith("IMG_")) imgCount--;
        else vidCount--;
        return getMedias().remove(id);
    }

    public boolean addDoc(String id) {
        docCount++;
        return getDocs().add(id);
    }

    public boolean removeDoc(String id) {
        docCount--;
        return getDocs().remove(id);
    }

    public boolean addLink(String id) {
        return getLinks().add(id);
    }

    public boolean removeLink(String id) {
        return getLinks().remove(id);
    }

    public boolean addGroup(String id) {
        return getGroups().add(id);
    }

    public boolean removeGroup(String id) {
        return getGroups().remove(id);
    }

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

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public ArrayList<String> getGroups() {
        if (groups == null)
            groups = new ArrayList<>();
        return groups;
    }

    public void setGroups(ArrayList<String> groups) {
        this.groups = groups;
    }

    public ArrayList<String> getMedias() {
        if (medias == null)
            medias = new ArrayList<>();
        return medias;
    }

    public void setMedias(ArrayList<String> medias) {
        this.medias = medias;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPic() {
        if (pic == null)
            return Constants.Media.DEFAULT_PROFILE_PIC;
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public Bitmap getPicBitmap() {
        if (pic == null)
            return UsefulFunctions.decodeImage(Constants.Media.DEFAULT_PROFILE_PIC);
        return UsefulFunctions.decodeImage(pic);
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public ArrayList<String> getLinks() {
        if (links == null)
            links = new ArrayList<>();
        return links;
    }

    public void setLinks(ArrayList<String> links) {
        this.links = links;
    }

    public String getLast_msg() {
        return last_msg;
    }

    public void setLast_msg(String last_msg) {
        this.last_msg = last_msg;
    }

    public void setLast_msg(String last_msg, int last_msg_type) {
        this.last_msg_type = last_msg_type;
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
        if(time==null)
            return "00:00";
        return time;
    }

    public void setTime(String sentTime) {
        this.time = sentTime;
    }

    public ArrayList<String> getDocs() {
        if (docs == null)
            docs = new ArrayList<>();
        return docs;
    }

    public void setDocs(ArrayList<String> docs) {
        this.docs = docs;
    }

    public int getVidCount() {
        return vidCount;
    }

    public void setVidCount(int vidCount) {
        this.vidCount = vidCount;
    }

    public int getImgCount() {
        return imgCount;
    }

    public void setImgCount(int imgCount) {
        this.imgCount = imgCount;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getWallpaper() {
        return wallpaper;
    }

    public void setWallpaper(String wallpaper) {
        this.wallpaper = wallpaper;
    }

    public int getLast_msg_type() {
        return last_msg_type;
    }

    public void setLast_msg_type(int last_msg_type) {
        this.last_msg_type = last_msg_type;
    }

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public boolean isKnown() {
        return known;
    }

    public void setKnown(boolean known) {
        this.known = known;
    }

    public void incMsgCount() {
        msg_count++;
    }

    public int getMsg_count() {
        return msg_count;
    }

    public void setMsg_count(int msg_count) {
        this.msg_count = msg_count;
    }

    public String getStatus() {
        if (status == null)
            return getAbout();
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public long getMediaSize() {
        return mediaSize;
    }

    public void setMediaSize(long mediaSize) {
        this.mediaSize = mediaSize;
    }

    public long incMediaSize(long size) {
        mediaSize += size;
        return mediaSize;
    }

    @Override
    public int hashCode() {
        return Objects.hash(user_id, displayName);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof User)) {
            return false;
        }

        User other = (User) obj;
        return this.user_id.equals(other.user_id);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getGroupData() {
        return groupData;
    }

    public void setGroupData(String groupData) {
        this.groupData = groupData;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}
