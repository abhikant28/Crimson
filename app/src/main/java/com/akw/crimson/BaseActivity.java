package com.akw.crimson;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.ActionBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.akw.crimson.Backend.Communications.Communicator;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.SharedPrefManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class BaseActivity extends AppCompatActivity {

    DocumentReference documentReference;

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("Stopping::::::::::","Communicator");
        documentReference.update(Constants.KEY_FIRESTORE_USER_ONLINE,0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        documentReference.update(Constants.KEY_FIRESTORE_USER_ONLINE,0);
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : runningServices) {
            if (runningServiceInfo.service.getClassName().equals(Communicator.class.getName())) {
                stopService(new Intent(new Intent(this, Communicator.class)));
                Log.i("Stopping::::::::::","Communicator");
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        documentReference.update(Constants.KEY_FIRESTORE_USER_ONLINE,1);
//        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(Integer.MAX_VALUE);
//
//        boolean found = false;
//        for (ActivityManager.RunningServiceInfo runningServiceInfo : runningServices) {
//            if (runningServiceInfo.service.getClassName().equals(Communicator.class.getName())) {
//                found=true;
//                break;
//            }
//        }
//        if(!found){
//            Intent intent= new Intent(new Intent(this, Communicator.class));
//            Log.i("STARTING::::::::::","Communicator");
//            startService(intent);
//        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseFirestore firestore= FirebaseFirestore.getInstance();
        documentReference=firestore.collection(Constants.KEY_FIRESTORE_USERS).document(new SharedPrefManager(getApplicationContext()).getLocalUserID());

        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(Integer.MAX_VALUE);

        new SharedPrefManager(this);

        ActionBar ab = getSupportActionBar();

        if (ab!=null) ab.setDisplayHomeAsUpEnabled(true);

        boolean found = false;
        for (ActivityManager.RunningServiceInfo runningServiceInfo : runningServices) {
            if (runningServiceInfo.service.getClassName().equals(Communicator.class.getName())) {
                found=true;
                break;
            }
        }
        if(!found){
            Intent intent= new Intent(new Intent(this, Communicator.class));
            Log.i("STARTING::::::::::","Communicator");
            startService(intent);
        }

    }


}
