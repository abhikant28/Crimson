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

@Entity(tableName = "user_table")
public class User {

    @PrimaryKey
    @NonNull
    private String user_id;
    private String _id;
    private String last_msg, name, userName, displayName, time, pic, phoneNumber, date, about, wallpaper;
    private boolean unread, connected, blocked, mute;
    private int unread_count = 0;
    @TypeConverters(DataConverter.class)
    private ArrayList<String> groups, medias, links, docs;

    public User(@NonNull String user_id, String name, String displayName, String pic, String phoneNumber, boolean connected) {
        this.user_id = user_id;
        this.name = name;
        this.displayName = displayName;
        this.pic = pic;
        this.phoneNumber = phoneNumber;
        this.connected = connected;
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

    public int getGroupCount() {
        return getGroups().size();
    }

    public int getMediaCount() {
        return getMedias().size() + getDocs().size() + getLinks().size();
    }

    public int getDocCount() {
        return docs.size();
    }

    public int getLinksCount() {
        return links.size();
    }

    public int getPicMediaCount() {
        return links.size();
    }

    public boolean addMedia(String id) {
        return medias.add(id);
    }

    public boolean removeMedia(String id) {
        return medias.remove(id);
    }

    public boolean addDoc(String id) {
        return docs.add(id);
    }

    public boolean removeDoc(String id) {
        return docs.remove(id);
    }

    public boolean addLink(String id) {
        return links.add(id);
    }

    public boolean removeLink(String id) {
        return links.remove(id);
    }

    public boolean addGroup(String id) {
        return groups.add(id);
    }

    public boolean removeGroup(String id) {
        return groups.remove(id);
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
        if(groups==null)
            groups=new ArrayList<>();
        return groups;
    }

    public void setGroups(ArrayList<String> groups) {
        this.groups = groups;
    }

    public ArrayList<String> getMedias() {
        if(medias==null)
            medias= new ArrayList<>();
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
        if(links==null)
            links=new ArrayList<>();
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
        if(docs==null)
            docs=new ArrayList<>();
        return docs;
    }

    public void setDocs(ArrayList<String> docs) {
        this.docs = docs;
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

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }
}
