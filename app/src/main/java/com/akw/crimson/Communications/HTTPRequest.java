package com.akw.crimson.Communications;

import android.util.Log;

import com.akw.crimson.AppObjects.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class HTTPRequest {
    //https://script.google.com/macros/s/AKfycbwCT0z3qG8hrswM_7Yp4ESlKaN41xsXw_eTxeEYHqutDRN_76UhEka2qvQ46-g4FqU/exec
    //https://script.google.com/macros/s/AKfycbx1rb2_XaIbHEyP83qFdIXAm0LWpeo8F2PO2qj6O4bxu97zHoPXzPgwsHyFYTNncK0/exec

    private static String MESSAGE_REQUEST_URL = "https://script.google.com/macros/s/AKfycbwCT0z3qG8hrswM_7Yp4ESlKaN41xsXw_eTxeEYHqutDRN_76UhEka2qvQ46-g4FqU/exec";
    private String USER_REQUEST_URL = "";
    public static String PREV_REQUEST_TIME = "2022/10/10,12:12:12";
    public static String USER_ID = "1001";
    private static boolean inProgress = false;

    private static JSONObject outQueue=new JSONObject();


    public static void postMessage(Message message) {
        try {
            outQueue.put(message.getUser_id(), outQueue.get(message.getUser_id())+","+message.asString(USER_ID).replaceAll(",,", ",_,"));
        } catch (JSONException e) {
            try {
                outQueue.put("1002", message.asString(USER_ID));
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
//        Log.i("MESSAGE ARRAY::::::",outQueue.toString());
        if (!inProgress) {
            Log.i("Starting API Request:::", "");
            inProgress = true;
            POSTrequestRun();
        }
    }

    private static boolean POSTrequestRun() {

        final boolean[] json = new boolean[1];
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    json[0] = POSTrequest();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i("POSTrequestRun:::::", "IOException");
                }
            }
        };
        new Thread(runnable).start();
        inProgress = false;
        return true;
    }

    private static boolean POSTrequest() throws MalformedURLException {
        String q="";

        URL url = null;
        try {
            url = new URL(MESSAGE_REQUEST_URL + "?id=" + USER_ID+"&time="+ PREV_REQUEST_TIME +  "&data=" + outQueue.toString() );
        } catch (Exception e) {
            e.printStackTrace();
            url = new URL(MESSAGE_REQUEST_URL + "?id=" + USER_ID+ "&time=" + PREV_REQUEST_TIME + "&data=" + outQueue.toString() );
        }

        String result = "";
        Log.i("URL:::::::::::::;", url.toString());
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setReadTimeout(30000);
            urlConnection.setConnectTimeout(60000);
            InputStream in = urlConnection.getInputStream();
            if (urlConnection.getResponseCode() != 200) {
                Log.i("API RESPONSE Fail::::", "Code::" + urlConnection.getResponseCode());
                return false;
            }
            PREV_REQUEST_TIME = new SimpleDateFormat("yyyy/MM/dd,hh:mm:ss").format(Calendar.getInstance().getTime());
            outQueue = new JSONObject();
            InputStreamReader reader = new InputStreamReader(in);

            int streamData = reader.read();
            while (streamData != -1) {
                char current = (char) streamData;
                result += current;
                streamData = reader.read();
            }
        } catch (Exception e) {
            Log.i(" API: ", " Data parsing error");
            e.printStackTrace();
        }
        if (urlConnection != null) urlConnection.disconnect();
        inProgress=false;
        try {
            extractMessages(new JSONObject(result));
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i("Bad JSON::", "JSON Bad");
        }finally{
            Log.i("API Response:::::", result);
            return true;
        }
    }


    public boolean getMessages() {
        if(!inProgress ){
            try {
                GETrequestRun(USER_ID, PREV_REQUEST_TIME);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private JSONObject GETrequestRun(String fromID, String time) {
        inProgress = true;
        final JSONObject[] json = {null};
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    json[0] = GETrequest(fromID, time);
                    extractMessages(json[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("GETrequestRun:::::", "IOException");
                }
            }
        };
        new Thread(runnable).start();
        inProgress = false;
        return json[0];
    }

    private JSONObject GETrequest(String fromID, String time) throws Exception {
        URL url = new URL(MESSAGE_REQUEST_URL + "?time=" + time + "&id=" + fromID);
        String result = "";
        HttpURLConnection urlConnection = null;
        urlConnection = (HttpURLConnection) url.openConnection();

        InputStream in = urlConnection.getInputStream();
        InputStreamReader reader = new InputStreamReader(in);

        int streamData = reader.read();

        while (streamData != -1) {
            char current = (char) streamData;
            result += current;
            streamData = reader.read();
        }
        urlConnection.disconnect();
        extractMessages(new JSONObject(result));
        try {
            Log.i(" API: ", " Creation");
            Log.i("API RESponse:::::", result);
            return new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i(" JSON Object: ", "Malformed JSON");
            return null;
        }
    }


    private static ArrayList<Message> extractMessages(JSONObject jsonObject) {

        ArrayList<Message> msgs = new ArrayList<>();
        try {
            JSONArray jsonArray = jsonObject.getJSONObject("messages").getJSONArray("data");
            for (int i = 0; i < jsonArray.length(); i++) {
                String[] s = jsonArray.getString(i).replaceAll(",_,",",," ).split(",");
                msgs.add(new Message(s));
                Log.i(":::::Message Received::::::", Arrays.toString(s));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return msgs;
    }

}