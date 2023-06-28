package com.akw.crimson.Backend.AppObjects;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class Box {

    private int type;
    private String data,userID,appendix;

    public Box(int type, String data) {
        this.type = type;
        this.data = data;
    }

    public Box(int type, String data, String userID, String appendix) {
        this.type = type;
        this.data = data;
        this.userID = userID;
        this.appendix = appendix;
    }

    public Box(String src) {
        Gson gson = new Gson();
        Type type = new TypeToken<Box>() {
        }.getType();
        Box box = gson.fromJson(src, type);
        this.data= box.data;
        this.type=box.type;
        this.userID = box.userID;
        this.appendix = box.appendix;   }

    public String encodeBox(){
            Gson gson = new Gson();
            return gson.toJson(this);

    }
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getAppendix() {
        return appendix;
    }

    public void setAppendix(String appendix) {
        this.appendix = appendix;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
