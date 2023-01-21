package com.akw.crimson.Backend.Communications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.Observer;

import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.SharedPrefManager;
import com.akw.crimson.Backend.Database.TheViewModel;
import com.akw.crimson.Backend.SerialJSONArray;
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.MainChatList;
import com.akw.crimson.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Communicator extends LifecycleService {
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl(Constants.FIREBASE_REALTIME_DATABASE_MSG_URL);

    public static TheViewModel localDB;
    int starter = 0;
    String senderUserID = "2";
    String thisUserID;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        super.onStartCommand(intent, flags, startId);
        if (intent != null && intent.getExtras() != null) {
            starter = intent.getExtras().getInt(Constants.KEY_INTENT_STARTED_BY, 0);
            senderUserID = intent.getExtras().getString(Constants.KEY_INTENT_USERID);
        }


        thisUserID = new SharedPrefManager(this).getLocalUserID();
        Log.i("COMMUNICATOR:::", "onStart Started");
        DatabaseReference userData = databaseReference.child(Constants.FIREBASE_REALTIME_DATABASE_CHILD_MSG).child(thisUserID);

        if (starter == Constants.KEY_INTENT_START_FCM) {
            Log.i("COMMUNICATOR:::", "Started by FCM");
            Log.i("USER ID::::", thisUserID);
            userData.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.i("COMMUNICATOR:::", "DATA CHANGE DETECTED from"+senderUserID);

                    DataSnapshot child = snapshot.child(senderUserID);

                    if (child.exists()) {
                        Log.i("COMMUNICATOR:::", "DATA FROM > " + child.getValue());
                        // Get the key value of the child
                        String key = child.getKey();
                        // Get the value of the child
                        JSONArray value = child.getValue(JSONArray.class);
                        // Iterate over the elements in the array
                        for (int i = 0; i < value.length(); i++) {
                            // Get the string value of the element
                            String element = null;
                            try {
                                element = value.getString(i);
                                Message msg = new Message(UsefulFunctions.decodeText(element));
                                if (localDB.getUser(key) == null) {
                                    fetchUserDetails(key,msg);
                                }else{
                                localDB.insertMessage(msg);}
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            // Log the element
                            Log.d("TAG", element);
                        }
                        child.getRef().removeValue();
                    } else {
                        Log.i("COMMUNICATOR:::", "DATA FROM > " + senderUserID + " Not Found");
                    }
                    stopSelf();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.i("COMMUNICATOR:::", "Cancelled 1");
                }
            });
        } else {
            Log.i("COMMUNICATOR:::", "Started in Default Mode");
            userData.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.i("COMMUNICATOR:::", "Database Change Detected");

                    for (DataSnapshot child : snapshot.getChildren()) {
                        // Get the key value of the child
                        String key = child.getKey();
                        Log.i("COMMUNICATOR:::", "Messages by: " + key);
                        // Get the value of the child
                        String str = child.getValue(String.class);
                        JSONArray value = null;
                        try {
                            value = new JSONArray(str);
                            // Iterate over the elements in the array
                            for (int i = 0; i < Objects.requireNonNull(value).length(); i++) {
                                Log.i("COMMUNICATOR:::", "Messages COUNT: " + value.length());
                                // Get the string value of the element
                                String element = null;
                                element = value.getString(i);
                                Message msg = new Message(UsefulFunctions.decodeText(element));
                                if (localDB.getUser(key) == null) {
                                    fetchUserDetails(key,msg);
                                }else
                                {
                                    localDB.insertMessage(msg);
                                }
                                // Log the element
                                Log.d("TAG", element);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        child.getRef().setValue(null);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.i("COMMUNICATOR:::", "Cancelled 2");

                }
            });
        }


        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {

        localDB = new TheViewModel(getApplication());

        localDB.getPendingMessagesList().observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages) {
                Log.i("COMMUNICATOR:::", "Pending Messages Found " + messages.size());
                HashMap<String, HashSet<Message>> messageMap = makeUserIdMap(messages);
                String[] keys = messageMap.keySet().toArray(new String[0]);
                Log.i("COMMUNICATOR:::", "Messages For > " + Arrays.deepToString(keys));
                for (String key : keys) {
                    Log.i("COMMUNICATOR:::", "Messages For > " + key);
                    databaseReference.child(Constants.FIREBASE_REALTIME_DATABASE_CHILD_MSG).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.i("COMMUNICATOR:::", "Pending Messages: Adding message");
                            // Check if the user ID node exists
                            if (dataSnapshot.hasChild(thisUserID)) {
                                Log.i("COMMUNICATOR:::", "Pending Messages: Previous Messages Exist");
                                // Get the array from the snapshot
                                JSONArray array = null;
                                try {
                                    array = new JSONArray(dataSnapshot.child(thisUserID).getValue(String.class));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                // Add the new values to the array
                                for (Message msg : messageMap.get(key)) {
                                    Log.i("COMMUNICATOR:::", "Pending Messages: Add Message to Array");
                                    array.put(UsefulFunctions.encodeText(msg.asString(thisUserID)));
                                }

                                SerialJSONArray serialJSONArray = new SerialJSONArray(array);
                                // Update the array in the database
                                dataSnapshot.child(thisUserID).getRef().setValue(array.toString());
                            } else {
                                Log.i("COMMUNICATOR:::", "Pending Messages: No Previous Messages");
                                // Create a new JSONArray
                                JSONArray array = new JSONArray();
                                for (Message msg : messageMap.get(key)) {
                                    Log.i("COMMUNICATOR:::", "Pending Messages: Add Message to Array");
                                    array.put(UsefulFunctions.encodeText(msg.asString(thisUserID)));
                                }
                                // Add the array to the database
                                dataSnapshot.child(thisUserID).getRef().setValue(array.toString());
                            }
                            Log.i("COMMUNICATOR:::", "Pending Messages: Message Sent");
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.i("COMMUNICATOR:::", "Cancelled 3: "+ databaseError);

                        }
                    });
                }

                if (!messages.isEmpty()) {
                    for (Message msg : messages) {
                        msg.setStatus(1);
                    }
                    localDB.updateAllMessage(messages);
                }

            }
        });

        localDB.getReceivedMessagesList().observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages) {
                Log.i("COMMUNICATOR:::", "New Messages Found - " + messages.size());
                List<Message> msgs = localDB.getReceivedMessagesList().getValue();
                if (msgs != null) msgs.retainAll(messages);
                HashMap<String, HashSet<Message>> map = makeUsernameMap(localDB, msgs);
                notifyUser(map);
            }
        });

        super.onCreate();
    }


    private void fetchUserDetails(String user_id, Message msg) {
        Log.i("COMMUNICATOR:::", "Fetching UserDetails");
        Log.i("FETCH USER::::",user_id);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        Task<DocumentSnapshot> found = firestore.collection(Constants.KEY_FIRESTORE_USERS).document(user_id).get();
        found.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot doc) {
                Log.i("FETCH USER::::", doc.getString(Constants.KEY_FIRESTORE_USER_NAME));
                User user = new User(user_id, doc.getString(Constants.KEY_FIRESTORE_USER_NAME),
                        doc.getString(Constants.KEY_FIRESTORE_USER_NAME)
                        , doc.getString(Constants.KEY_FIRESTORE_USER_PHONE), doc.getString(Constants.KEY_FIRESTORE_USER_PIC)
                        , doc.getString(Constants.KEY_FIRESTORE_USER_PHONE), false);
                localDB.insertUser(user);
                localDB.insertMessage(msg);
            }
        });
    }


    private void notifyUser(HashMap<String, HashSet<Message>> map) {
        Log.i("COMMUNICATOR:::", "New Messages Found: Making Notifying User");
        String[] keys = map.keySet().toArray(new String[0]);
        Intent intent;
        Log.i("COMMUNICATOR:::", "New Messages Found: Making Notifying User Count > " + map.size());

        intent = new Intent(getApplicationContext(), MainChatList.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        String channelId = "chat_messages";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Chat Message";
            String channelDescription = "This notification channel is used for chat message notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDescription);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        int notificationID = new Random().nextInt();

        for (String key : keys) {
            Log.i("COMMUNICATOR:::", "New Messages Found: Making Notifications");
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
            builder.setSmallIcon(R.drawable.ic_launcher_foreground);
            ArrayList<Message> messages = new ArrayList<>(map.get(key));
            builder.setContentTitle(key);
            String msg = messages.get(0).getMsg();
            builder.setContentText(msg.substring(0, Math.min(msg.length(), 8)) + "...");
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(msg.substring(0, Math.min(msg.length(), 8)) + "..."));
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
            builder.setContentIntent(pendingIntent);
            builder.setAutoCancel(true);


            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
            notificationManagerCompat.notify(notificationID++, builder.build());

        }

    }

    private HashMap<String, HashSet<Message>> makeUsernameMap(TheViewModel localDB, List<Message> messages) {
        Log.i("COMMUNICATOR:::", "New Messages Found: Making Name List");

        HashMap<String, HashSet<Message>> map = makeUserIdMap(messages);

        String[] keys = map.keySet().toArray(new String[0]);
        for (String key : keys) {
            map.put(localDB.getUser(key).getDisplayName(), map.get(key));
            map.remove(key);
        }
        return map;
    }

    private HashMap<String, HashSet<Message>> makeUserIdMap(List<Message> messages) {
        Log.i("COMMUNICATOR:::", "New Messages Found: Making ID List");
        HashMap<String, HashSet<Message>> map = new HashMap<>();
        for (Message msg : messages) {
            if (map.containsKey(msg.getUser_id())) {
                map.put(msg.getUser_id(), new HashSet<Message>() {
                    {
                        addAll(map.get(msg.getUser_id()));
                        add(msg);
                    }
                });
            } else {
                map.put(msg.getUser_id(), new HashSet<Message>() {{
                    add(msg);
                }});
            }
        }
        return map;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("SERVICE BINDING::::", "DONE");
        super.onBind(intent);

        return null;
    }
}
