package com.akw.crimson.Backend.Communications;

import android.util.Log;

import androidx.annotation.NonNull;

import com.akw.crimson.Backend.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Messaging {

    public static void sendNotification(String messageBody) {
        Log.i("sendNotification::::", "SENDING=> " + messageBody);
        APIClient.getRetroClient().create(APIService.class).sendRetroMessage(
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                Log.i("NOTIFICATION RESPONSE::::", "RECEIVED--");
                if (response.isSuccessful()) {
                    try {
                        Log.i("NOTIFICATION RESPONSE::::", "BODY");
                        if (response.body() != null) {
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if (responseJson.getInt("failure") == 1) {
                                JSONObject error = (JSONObject) results.get(0);
                                Log.i("ERROR::::", error.getString("error"));
                                return;
                            }
                        } else {
                            Log.i("NOTIFICATION RESPONSE::::", "BODY NULL");
                        }
                    } catch (JSONException e) {
                        Log.i("NOTIFICATION ERROR::::", e.toString());
                        e.printStackTrace();
                    }
                    Log.i("NOTIFICATION::::", "SENT SUCCESSFULLY");
                } else {
                    Log.i("NOTIFICATION ERROR::::", "UNSUCCESSFUL-" + response + "_" + response.code() + "_" + response.message() + "_" + response.errorBody() + "_" + response.raw());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.i("NOTIFICATION::::", "FAILED");
            }
        });
    }

    public static void sendNotificationToPatner(String token, String msg, String myName) {

        SendNotificationModel sendNotificationModel = new SendNotificationModel(msg.substring(0, Math.min(10, msg.length())),myName);
        RequestNotification requestNotification = new RequestNotification();
        requestNotification.setSendNotificationModel(sendNotificationModel);
        requestNotification.setToken(token);

        APIService apiService = APIClient.getClient().create(APIService.class);
        retrofit2.Call<ResponseBody> responseBodyCall = apiService.sendMessage(requestNotification);

        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                Log.d("kkkk", "done" + response.message() + " " + responseBodyCall + "_" + call + "_" + response.errorBody() + "-" + response.code());
            }

            @Override
            public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public static void sendMessageNotification(String localUserID, String userToken, String tag, String msg_id, String myName, String msg) {
        sendNotificationToPatner(userToken, msg, myName);
    }

    public static void sendPingMessageNotification( String userToken, String myName) {
        sendNotificationToPatner(userToken, "Pinged you!", myName);
        Log.i("Messaging.sendPingMessageNotification:::::::", "Ping SENT!!!!");
    }

    public static void sendMessageRetroNotification(String localUserID, String userToken, String tag, String msg_id, String userID) {
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(userToken);
            JSONObject data = new JSONObject();
            data.put(Constants.KEY_FCM_FROM, localUserID);
            data.put(Constants.KEY_FCM_TYPE, tag);
            data.put(Constants.KEY_FCM_MSG_ID, msg_id);

            JSONObject body = new JSONObject();
            body.put("to", tokens);
            body.put(Constants.KEY_FCM_DATA, data);
        } catch (Exception exception) {
            Log.i("JSoN EXCEPTION::::", "sendMessageRetroNotification");
        }
    }

}
