package com.akw.crimson.Backend;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.akw.crimson.Backend.AppObjects.PreparedMessage;
import com.akw.crimson.Backend.Database.SharedPrefManager;
import com.akw.crimson.Backend.Database.TheViewModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class AlertReceiver extends BroadcastReceiver {
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl(Constants.FIREBASE_REALTIME_DATABASE_MSG_URL);

    @Override
    public void onReceive(Context context, Intent intent) {

        new SharedPrefManager(context);
        ArrayList<PreparedMessage> list = SharedPrefManager.getPreparedMessages();
        Log.i("ALERT RECEIVER::::","Started");

        int id = intent.getIntExtra(Constants.KEY_INTENT_PREP_MSG_ID, 0);
        PreparedMessage prepMsg = null;
        if (id != 0) {
            Log.i("ALERT RECEIVER::::","ID EXISTS");
            int i=0;
            for (PreparedMessage msg : list) {
                if (msg.getId() == id) {
                    Log.i("ALERT RECEIVER::::","FOUND");
                    prepMsg = msg;
                    postMessage(prepMsg, (Application) context.getApplicationContext());
                    NotificationHelper notificationHelper = new NotificationHelper(context);
                    NotificationCompat.Builder nb = notificationHelper.getChannel1Notification(prepMsg.getToName());
                    notificationHelper.getManager().notify(1, nb.build());
                    list.remove(i);
                    SharedPrefManager.putPreparedMessages(list);
                    break;
                }
                i++;
            }

        }

    }

    private void postMessage(PreparedMessage prepMsg, Application act) {
        TheViewModel localDB = new TheViewModel(act);
        localDB.insertMessage(prepMsg.getMessage());
        String thisUserID = SharedPrefManager.getLocalUserID();
        Log.i("ALERT RECEIVER::::","POSTING");

        databaseReference.child(Constants.FIREBASE_REALTIME_DATABASE_CHILD_MSG).child(prepMsg.getToID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("COMMUNICATOR::::", "Pending Messages: Adding message");
                // Check if the user ID node exists
                if (dataSnapshot.hasChild(thisUserID)) {
                    Log.i("COMMUNICATOR::::", "Pending Messages: Previous Messages Exist");
                    JSONArray array = null;
                    try {
                        array = new JSONArray(dataSnapshot.child(thisUserID).getValue(String.class));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.i("COMMUNICATOR::::", "Pending Messages: Add Message to Array");
                    array.put(UsefulFunctions.encodeText(prepMsg.getMessage().asString(thisUserID)));

                    dataSnapshot.child(thisUserID).getRef().setValue(array.toString());
                } else {
                    Log.i("COMMUNICATOR::::", "Pending Messages: No Previous Messages");
                    // Create a new JSONArray
                    JSONArray array = new JSONArray();
                    Log.i("COMMUNICATOR::::", "Pending Messages: Add Message to Array");
                    array.put(UsefulFunctions.encodeText(prepMsg.getMessage().asString(thisUserID)));

                    dataSnapshot.child(thisUserID).getRef().setValue(array.toString());
                }
                Log.i("COMMUNICATOR::::", "Pending Messages: Message Sent");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("COMMUNICATOR::::", "Cancelled 3");

            }
        });


    }
}








