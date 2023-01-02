package com.akw.crimson.Backend;

import java.util.HashMap;

public class Constants {
    public static final String KEY_INTENT_STARTED_BY = "intentBy";
    public static final String KEY_INTENT_STARTED_BY_SEARCH = "search";
    public static final String KEY_INTENT_LIST_POSITION = "listPosition";
    public static final String KEY_INTENT_USERID = "userID";
    public static final String KEY_INTENT_MESSAGEID = "messageID";
    public static final String KEY_INTENT_USERNAME = "username";
    public static final String KEY_INTENT_PIC = "userPic";
    public static final String KEY_INTENT_EMAIL = "userEmail";
    public static final String KEY_INTENT_PHONE = "userPhone";
    public static final int KEY_INTENT_START_FCM = 2;
    public static final int KEY_INTENT_START_APP = 0;

    public static final String FIREBASE_REALTIME_DATABASE_MSG_URL="https://crimson-ims-default-rtdb.asia-southeast1.firebasedatabase.app/";
    public static final String FIREBASE_REALTIME_DATABASE_CHILD_MSG="messages";

    public static final String KEY_FCM_DATA = "data";
    public static final String KEY_FCM_TOKENS = "tokens";
    public static final String KEY_FCM_FROM = "from";
    public static final String KEY_FCM_MSG_ID = "msgID";
    public static final String KEY_FCM_MSG_TAG_GROUP = "2";
    public static final String KEY_FCM_TYPE = "type";
    public static final String KEY_FCM_TYPE_MSG = "1";
    public static final String KEY_FCM_SERVER_KEY = "AAAAwAiXfmQ:APA91bGtlDupCsKN4bih2StMtYr13wOzVnT0r9SaiIIUvN3l7gamK8L02s2OsbPewCVNGnBvM7s7fiufIwj0MhyAHgjgVuF_AG-fq0tZWm2AhBh4Kj6RagrnW8YjJD1SpEwJuCvWFVjA";



    public static final String KEY_FIRESTORE_USERS = "users";
    public static final String KEY_FIRESTORE_USER_ONLINE = "online";
    public static final String KEY_FIRESTORE_USER_NAME = "name";
    public static final String KEY_FIRESTORE_USER_ID = "userID";
    public static final String KEY_FIRESTORE_USER_PIC = "pic";
    public static final String KEY_FIRESTORE_USER_PHONE = "phone";
    public static final String KEY_FIRESTORE_USER_EMAIL = "email";
    public static final String KEY_FIRESTORE_USER_TOKEN = "token";


    public static final String KEY_AVAILABILITY = "availability";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    public static HashMap<String,String> remoteMsgHeaders=null;

    public static HashMap<String,String> getRemoteMsgHeaderes(){
        if(remoteMsgHeaders==null){
            remoteMsgHeaders=new HashMap<>();
            remoteMsgHeaders.put(REMOTE_MSG_AUTHORIZATION,
                    "key=AAAAwAiXfmQ:APA91bGtlDupCsKN4bih2StMtYr13wOzVnT0r9SaiIIUvN3l7gamK8L02s2OsbPewCVNGnBvM7s7fiufIwj0MhyAHgjgVuF_AG-fq0tZWm2AhBh4Kj6RagrnW8YjJD1SpEwJuCvWFVjA"
            );
        }
        remoteMsgHeaders.put(REMOTE_MSG_CONTENT_TYPE,
                "application/json");
        return remoteMsgHeaders;
    }
}
