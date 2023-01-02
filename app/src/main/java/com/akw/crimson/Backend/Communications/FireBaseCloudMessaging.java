package com.akw.crimson.Backend.Communications;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.akw.crimson.Backend.Constants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;


public class FireBaseCloudMessaging extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.i("FCM MESSAGE NOTIFICATION::::", remoteMessage.getSenderId());
        Map<String, String> data = remoteMessage.getData();
        String serviceData = data.get("serviceData");

        Log.i("FCM DATA::::", data.toString());
        // Create an intent for the service
        Intent serviceIntent = new Intent(this, Communicator.class);
        serviceIntent.putExtra("serviceData", serviceData);
        serviceIntent.putExtra(Constants.KEY_INTENT_STARTED_BY, Constants.KEY_INTENT_START_FCM);
        serviceIntent.putExtra(Constants.KEY_INTENT_USERID, remoteMessage.getSenderId());

        // Start the service
        startService(serviceIntent);

    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }
}
