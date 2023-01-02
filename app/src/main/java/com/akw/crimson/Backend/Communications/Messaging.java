package com.akw.crimson.Backend.Communications;

import android.util.Log;

import androidx.annotation.NonNull;

import com.akw.crimson.Backend.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Messaging {

        public static void sendNotification(String messageBody){
        Log.i("sendNotification::::", "SENDING");
        APIClient.getClient().create(APIService.class).sendRetroMessage(
                Constants.getRemoteMsgHeaderes(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                Log.i("NOTIFICATION RESPONSE::::","RECEIVED");
                if(response.isSuccessful()){
                    try{
                        Log.i("NOTIFICATION RESPONSE::::","BODY");
                        if(response.body()!=null){
                            JSONObject responseJson= new JSONObject(response.body());
                            JSONArray results= responseJson.getJSONArray("results");
                            if(responseJson.getInt("failure")==1){
                                JSONObject error= (JSONObject) results.get(0);
                                Log.i("ERROR::::", error.getString("error"));
                                return;
                            }
                        }else{
                            Log.i("NOTIFICATION RESPONSE::::","BODY NULL");
                        }
                    }catch (JSONException e){
                        Log.i("NOTIFICATION ERROR::::",e.toString());
                        e.printStackTrace();
                    }
                    Log.i("NOTIFICATION::::","SENT SUCCESSFULLY");
                }else{
                    Log.i("NOTIFICATION ERROR::::","UNSUCCESSFUL");
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.i("NOTIFICATION::::","FAILED");
            }
        });
    }
    public static void sendNotificationToPatner(String token) {

        SendNotificationModel sendNotificationModel = new SendNotificationModel("check", "i know you");
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

    public static void sendMessageNotification(String localUserID, String userToken, String tag, String msg_id) {
//        sendNotificationToPatner(userToken);
        JSONArray tokens = new JSONArray();
        tokens.put(userToken);

        Log.i("sendMessageNotification::::", "SENDING");
        JSONObject data = new JSONObject();
        try{
            data.put(Constants.KEY_FCM_FROM, localUserID);
            data.put(Constants.KEY_FCM_TYPE, Constants.KEY_FCM_TYPE_MSG);
            data.put(Constants.KEY_FCM_TYPE_MSG, tag);
            data.put(Constants.KEY_FCM_MSG_ID, msg_id);

            JSONObject body = new JSONObject();
            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);
            sendNotification(body.toString());
        }catch (Exception e){

        }
    }

    public static void sendMessageRetroNotification(String localUserID, String userToken, String tag, String msg_id, String userID) {
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(userToken);
            JSONObject data = new JSONObject();
            data.put(Constants.KEY_FCM_FROM, localUserID);
            data.put(Constants.KEY_FCM_TYPE, Constants.KEY_FCM_TYPE_MSG);
            data.put(Constants.KEY_FCM_MSG_ID, msg_id);

            JSONObject body = new JSONObject();
            body.put(Constants.KEY_FCM_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);
            sendNotification(body.toString());
        } catch (Exception exception) {
            Log.i("JSoN EXCEPTION::::","sendMessageRetroNotification" );
        }
    }

}
