package com.akw.crimson.Backend.AppObjects;

import com.akw.crimson.Backend.Constants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class Box {

    private int type = 0;
    private String data;

    public Box(int type, String data) {
        this.type = type;
        this.data = data;
    }

    public Box(String src) {
        Gson gson = new Gson();
        Type type = new TypeToken<Box>() {
        }.getType();
        Box box = gson.fromJson(src, type);
        this.data= box.data;
        this.type=box.type;
    }

    public String asString(){
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
}
