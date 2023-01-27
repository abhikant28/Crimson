package com.akw.crimson.Backend;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class Constants {
    public static final String KEY_INTENT_STARTED_BY = "intentBy";
    public static final String KEY_INTENT_STARTED_BY_SEARCH = "search";
    public static final String KEY_INTENT_LIST_POSITION = "listPosition";
    public static final String KEY_INTENT_USERID = "userID";
    public static final String KEY_INTENT_MSG_TYPE = "messageType";
    public static final String KEY_INTENT_MESSAGE_ID = "messageID";
    public static final String KEY_INTENT_USERNAME = "username";
    public static final String KEY_INTENT_PREP_MSG_ID = "preparedID";
    public static final String KEY_INTENT_PIC = "userPic";
    public static final String KEY_INTENT_EMAIL = "userEmail";
    public static final String KEY_INTENT_PHONE = "userPhone";
    public static final int KEY_INTENT_START_FCM = 2;
    public static final int KEY_INTENT_START_APP = 0;

    public static final String FIREBASE_REALTIME_DATABASE_MSG_URL = "https://crimson-ims-default-rtdb.asia-southeast1.firebasedatabase.app/";
    public static final String FIREBASE_REALTIME_DATABASE_CHILD_MSG = "messages";

    public static final String KEY_FCM_DATA = "data";
    public static final String KEY_FCM_TOKENS = "tokens";
    public static final String KEY_FCM_FROM = "from";
    public static final String KEY_FCM_MSG_ID = "msgID";
    public static final String KEY_FCM_MSG_TAG_GROUP = "2";
    public static final String KEY_FCM_TYPE = "type";
    public static final String KEY_FCM_TYPE_MSG = "1";
    public static final String KEY_FCM_SERVER_KEY = "AAAAwAiXfmQ:APA91bGtlDupCsKN4bih2StMtYr13wOzVnT0r9SaiIIUvN3l7gamK8L02s2OsbPewCVNGnBvM7s7fiufIwj0MhyAHgjgVuF_AG-fq0tZWm2AhBh4Kj6RagrnW8YjJD1SpEwJuCvWFVjA\t\n";


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
    public static final int KEY_ACTIVITY_RESULT_CONTACT_SELECT = 3;
    public static final String KEY_FRAGMENT_TYPE = "fragmentType";
    public static final String KEY_FRAGMENT_TYPE_NAME = "name";
    public static final String KEY_FRAGMENT_TYPE_ABOUT = "about";
    public static final String KEY_MESSAGE_MEDIA_TYPE = "mediaType";
    public static final int KEY_MESSAGE_MEDIA_TYPE_IMAGE = 1;
    public static final int KEY_MESSAGE_MEDIA_TYPE_VIDEO = 2;
    public static final int KEY_MESSAGE_MEDIA_TYPE_DOCUMENT = 3;
    public static final int KEY_MESSAGE_MEDIA_TYPE_LOCATION = 4;
    public static final int KEY_MESSAGE_MEDIA_TYPE_CONTACT = 5;
    public static final int KEY_MESSAGE_MEDIA_TYPE_CANVAS = 6;
    public static final int KEY_MESSAGE_MEDIA_TYPE_AUDIO = 7;
    public static final int KEY_MESSAGE_MEDIA_TYPE_WALLPAPER = 8;
    public static final String KEY_INTENT_LIST_SIZE = "listSize";
    public static final String KEY_FIRESTORE_USER_ABOUT = "about";
    public static final int KEY_MESSAGE_MEDIA_TYPE_NONE = 0;
    public static final String KEY_INTENT_ABOUT ="about" ;

    public static HashMap<String, String> remoteMsgHeaders = null;
    public static JSONObject FcmJsonObject = null;

    public static JSONObject getFCMjsonObject() {
        if (FcmJsonObject == null) {
            FcmJsonObject = new JSONObject();
            try {
                FcmJsonObject.put(REMOTE_MSG_CONTENT_TYPE, "application/json");
                FcmJsonObject.put(REMOTE_MSG_AUTHORIZATION, "key=" + KEY_FCM_SERVER_KEY);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return FcmJsonObject;
    }

    public static HashMap<String, String> getRemoteMsgHeaderes() {
        if (remoteMsgHeaders == null) {
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(REMOTE_MSG_AUTHORIZATION,
                    "key=AAAAwAiXfmQ:APA91bGtlDupCsKN4bih2StMtYr13wOzVnT0r9SaiIIUvN3l7gamK8L02s2OsbPewCVNGnBvM7s7fiufIwj0MhyAHgjgVuF_AG-fq0tZWm2AhBh4Kj6RagrnW8YjJD1SpEwJuCvWFVjA"
            );
        }
        remoteMsgHeaders.put(REMOTE_MSG_CONTENT_TYPE,
                "application/json");
        return remoteMsgHeaders;
    }

    public static final String DEFAULT_PROFILE_PIC = "/9j/4AAQSkZJRgABAQAAAQABAAD/4gIoSUNDX1BST0ZJTEUAAQEAAAIYA" +
            "AAAAAQwAABtbnRyUkdCIFhZWiAAAAAAAAAAAAAAAABhY3NwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQAA9tYAAQAAAADTLQ" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAlkZXNjAAAA8AAAAHRyWFlaAAABZAAAABRnWFl" +
            "aAAABeAAAABRiWFlaAAABjAAAABRyVFJDAAABoAAAAChnVFJDAAABoAAAAChiVFJDAAABoAAAACh3dHB0AAAByAAAABRjcHJ0AAAB3AAAADxtbHV" +
            "jAAAAAAAAAAEAAAAMZW5VUwAAAFgAAAAcAHMAUgBHAEIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFhZWiAAAAAAAABvogAAOPUAAAOQWFlaIAAAAAAAAGKZAAC3hQAAGNpYWVogAAAAAAAAJKAAAA+EAAC2z3BhcmEAAA" +
            "AAAAQAAAACZmYAAPKnAAANWQAAE9AAAApbAAAAAAAAAABYWVogAAAAAAAA9tYAAQAAAADTLW1sdWMAAAAAAAAAAQAAAAxlblVTAAAAIAAAABwARwBvAG8AZwBsAGUA" +
            "IABJAG4AYwAuACAAMgAwADEANv/bAEMAEAsMDgwKEA4NDhIREBMYKBoYFhYYMSMlHSg6Mz08OTM4N0BIXE5ARFdFNzhQbVFXX2JnaGc+TXF5cGR4XGVnY//bAEMBERISGBU" +
            "YLxoaL2NCOEJjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY2NjY//AABEIAJYAlgMBIgACEQEDEQH/xAAaAAEAAwEBAQAAAAAAAAAAAAA" +
            "AAwQFAgEH/8QAMhABAAIBAQQIBAUFAAAAAAAAAAECAxEEFVKRBRIhIjFBUdETMkKBFFNhobEjNHGS8P/EABQBAQAAAAAAAAAAAAAAAAAAAAD/xAAUEQEAAAAAAAAAAAAAAA" +
            "AAAAAA/9oADAMBAAIRAxEAPwD6AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACttG2Uw92O9f09P8AILIyMm2Zsn19WPSvYj+Pm/Nv/tINsZWLbs1Oy0" +
            "xeP18V/BtFM9dazpPnWfGATAAAAAAAAAAAAAAq7btHwcfVrPft+0MtNtd5vtN5nynSPshAAAe0valotWdJjzeANrBljNii8dmvjHpKRndGXnr3p5TGrRAAAAAAAA" +
            "AAAABh5YmMt4nx606uU+24/h7Tb0t3oQAAAAAsdHxP4qNPCInVrM/ozH23yeXyx/3JoAAAAAAAAAAAAAg2rBGfHprpaO2JZNqzS01tGkx4xLdQ59nx5478dseE" +
            "x4gxxbv0flrr1LVtHKUf4LaPy/3gEDvDhtmyRWv3n0WsfR1tf6l4iPSq9jx0xV6tI0gDHSuPHFK+EOwAAAAAAAAAAAAAEN9pw0+bJX7dqG3SOKInq1tM8gXBn" +
            "W6StMd3HET+s6ud45eGnKfcGmM+Okp88XKyWvSGG06T1q/rMAtiPHmx5PkvWdfLXtSAAAAAAAAAPJmIiZmdIjzJmIjWeyGVte1WzWmtZ0xx4R6gs59vrWeri" +
            "jrT6z4KWTaMuX57zMenhCMAAAAAAAT49szY/q60elu1AA1cG2Y83ZPct6T5rLBX9h2qbT8LLbWfpmf4BfAAAAABBtszXZckxOnZp+7Ia23f2l/t/MMkAAAA" +
            "AAAAAAArM1tFonSYnWABvAAAAAA4y44y45paZiJ9Fbd2Hivzj2ADd2Hivzj2N3YeK/OPYAN3YeK/OPY3dh4r849gA3dh4r849jd2Hivzj2ADd2Hivzj2N" +
            "3YeK/OPYAN3YeK/OPY3dh4r849gA3dh4r849jd2Hivzj2AFwAAAH//Z";
}
