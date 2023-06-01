package com.akw.crimson.Backend.AppObjects;

import com.akw.crimson.Backend.Database.SharedPrefManager;
import com.akw.crimson.Backend.UsefulFunctions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class Group {
    private String groupId, description, displayName, createdBy, createdTime;
    private ArrayList<String> users, admins;

    public Group(String groupId, String displayName
            , String createdBy, String createdTime, ArrayList<String> users
            , ArrayList<String> admins) {
        this.groupId = groupId;
        this.displayName = displayName;
        this.createdBy = createdBy;
        this.createdTime = createdTime;
        this.users = users;
        this.admins = admins;
    }

    public Group(String displayName, String createdBy, ArrayList<String> users, ArrayList<String> admins) {
        this.displayName = displayName;
        this.createdBy = createdBy;
        this.users = users;
        this.createdTime = UsefulFunctions.getCurrentTimestamp();
        this.admins=admins;
    }

    
    public Group(String g){
        Gson gson = new Gson();
        Type type = new TypeToken<Group>() {
        }.getType();
        Group group = gson.fromJson(g, type);
        this.groupId = group.groupId;
        this.description = group.description;
        this.displayName = group.displayName;
        this.createdBy = group.createdBy;
        this.createdTime = group.createdTime;
        this.users = group.users;
        this.admins = group.admins;
    }

    public String asString() {
        Gson gson = new Gson();
        Group grp=this;
        return gson.toJson(grp);
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }


    public boolean addUser(User user) throws Exception {
        if (amAdmin()) {
            if (users.contains(user.getUser_id())) {
                users.add(user.getUser_id());
                return true;
            }
        }
        return false;
    }

    public boolean amAdmin() {
        return admins.contains(SharedPrefManager.getLocalUserID());
    }

    public boolean removeUser(User user) {
        if (amAdmin()) {
            if (users.contains(user.getUser_id())) {
                users.remove(user.getUser_id());
                return true;
            }
        }
        return false;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public ArrayList<String> getUsers() {
        if (users == null)
            return new ArrayList<>();
        return users;
    }

    public void setUsers(ArrayList<String> users) {
        this.users = users;
    }

    public boolean addAdmin(User user) {
        if (amAdmin()) {
            admins.add(user.getUser_id());
            return true;
        }
        return false;
    }

    public boolean removeAdmin(User user) {
        if (amAdmin() && user.getUser_id().equals(createdBy)) {
            admins.remove(user.getUser_id());
            return true;
        }
        return false;
    }

    public ArrayList<String> getAdmins() {
        return admins;
    }

    public void setAdmins(ArrayList<String> admins) {
        this.admins = admins;
    }

    public int getUserCount() {
        if(users==null)
            return 0;
        return users.size();
    }

}
