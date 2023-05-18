package com.akw.crimson.Backend.Communications;

import org.json.JSONObject;

public class SendNotificationModel {
    private String body, title;
    private JSONObject data;

    public SendNotificationModel(String body, String title) {
        this.body = body;
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
