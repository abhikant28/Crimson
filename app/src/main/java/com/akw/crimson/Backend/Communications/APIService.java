package com.akw.crimson.Backend.Communications;

import com.akw.crimson.Backend.Constants;

import org.json.JSONObject;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({"Authorization: key="+ Constants.KEY_FCM_SERVER_KEY,
            "Content-Type:application/json"})
    @POST("fcm/send")
    Call<ResponseBody> sendMessage(@Body RequestNotification requestNotification);


    @POST("send")
    @Headers({"Authorization: key="+ Constants.KEY_FCM_SERVER_KEY,
            "Content-Type:application/json"})
    Call<String> sendRetroMessage(
            @Body String messageBody
    );
}
