package com.akw.crimson.Backend.Database.DAOs;

import androidx.room.TypeConverter;

import java.util.Calendar;

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
    }
