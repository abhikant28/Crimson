package com.akw.crimson.Backend.AppObjects;

import java.util.Calendar;

public class PreparedMessage {

    Message message;
    Calendar date;
    String toName;
    String toID;
    int id;

    public PreparedMessage(Message message, Calendar date, String to, String toID) {
        Calendar c = Calendar.getInstance();
        this.id = c.get(Calendar.DAY_OF_MONTH) + c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.MINUTE) + c.get(Calendar.SECOND);
        this.message = message;
        this.date = date;
        this.toName = to;
        this.toID = toID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public String getToName() {
        return toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public String getToID() {
        return toID;
    }

    public void setToID(String toID) {
        this.toID = toID;
    }
}
