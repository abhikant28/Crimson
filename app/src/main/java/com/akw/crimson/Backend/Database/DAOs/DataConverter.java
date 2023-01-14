package com.akw.crimson.Backend.Database.DAOs;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class DataConverter {
        @TypeConverter
        public Calendar toCalendar(Long value) {
            Calendar c = Calendar.getInstance();
            if(value == null){
                return null;
            }else {
                c.setTimeInMillis(value);
                return c;
            }
        }

        @TypeConverter
        public Long fromCalendar(Calendar calendar) {
            if (calendar == null) {
                return null;
            } else {
                return calendar.getTime().getTime();
            }
        }

    @TypeConverter
    public static String[] stringToArray(String string) {
        if (string == null) {
            return null;
        }
        Gson gson = new Gson();
        Type type = new TypeToken<String[]>() {}.getType();
        return gson.fromJson(string, type);
    }

    @TypeConverter
    public static String arrayToString(String[] string) {
        if (string != null) {
            return new Gson().toJson(string);
        }else
            return null;
    }
}