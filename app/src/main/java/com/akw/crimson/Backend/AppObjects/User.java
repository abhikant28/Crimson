package com.akw.crimson.Backend.AppObjects;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.DAOs.DataConverter;
import com.akw.crimson.Backend.UsefulFunctions;

import java.util.ArrayList;
import java.util.Objects;

@Entity(tableName = "user_table")
public class User {

    @PrimaryKey
    @NonNull
    private String user_id;
    private String _id;
    private String last_msg, name, userName, displayName, time, pic, phoneNumber, date, about, wallpaper;
    private boolean unread, connected, blocked, mute;
    private int unread_count, last_msg_type = 0, vidCount = 0, imgCount = 0, docCount = 0;
    private long mediaSize;
    @TypeConverters(DataConverter.class)
    private ArrayList<String> groups, medias, links, docs;

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


    public int getGroupCount() {
        return getGroups().size();
    }

    public int getTotalMediaCount() {
        return getMedias().size() + getDocs().size() + getLinks().size();
    }

    public void setDocCount(int docCount) {
        this.docCount = docCount;
    }

    public int getDocCount() {
        return getDocs().size();
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

    public void setMedias(ArrayList<String> medias) {
        this.medias = medias;
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

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPic() {
        if (pic == null)
            return Constants.DEFAULT_PROFILE_PIC;
        return pic;
    }

    public Bitmap getPicBitmap() {
        if (pic == null)
            return UsefulFunctions.decodeImage(Constants.DEFAULT_PROFILE_PIC);
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
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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

    public void setLast_msg(String last_msg) {
        this.last_msg = last_msg;
    }

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public long getMediaSize() {
        return mediaSize;
    }

    public long incMediaSize(long size) {
        mediaSize += size;
        return mediaSize;
    }

    public void setMediaSize(long mediaSize) {
        this.mediaSize = mediaSize;
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
}
