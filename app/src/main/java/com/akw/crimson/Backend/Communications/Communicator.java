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

import com.akw.crimson.Backend.AppObjects.Box;
import com.akw.crimson.Backend.AppObjects.Group;
import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.SharedPrefManager;
import com.akw.crimson.Backend.Database.TheViewModel;
import com.akw.crimson.Backend.SerialJSONArray;
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.MainChatList;
import com.akw.crimson.R;
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
    private static final String GROUP_MAP = "groupMap";
    private static final String USER_MAP = "userMap";
    public static TheViewModel localDB;
    private static final Communicator instance = null;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl(Constants.FIREBASE_REALTIME_DATABASE_MSG_URL);
    int starter = 0;
    String senderUserID = "2";
    public static String thisUserID;
    public static HashSet<String> downloading = new HashSet<>(), uploading = new HashSet<>();

    public static boolean isCommunicatorRunning() {
        return instance != null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i("COMMUNICATOR:::", "STARTED ");

        super.onStartCommand(intent, flags, startId);
        if (intent != null && intent.getExtras() != null) {
            starter = intent.getExtras().getInt(Constants.Intent.KEY_INTENT_STARTED_BY, 0);
            senderUserID = intent.getExtras().getString(Constants.Intent.KEY_INTENT_USERID);
        }


        thisUserID = new SharedPrefManager(this).getLocalUserID();

        Log.i("COMMUNICATOR:::", "onStart Started");
        DatabaseReference userData = databaseReference.child(Constants.FIREBASE_REALTIME_DATABASE_CHILD_MSG).child(thisUserID);

        if (starter == Constants.Intent.KEY_INTENT_START_FCM) {
            Log.i("COMMUNICATOR:::", "Started by FCM");
            Log.i("USER ID::::", thisUserID);
            userData.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.i("COMMUNICATOR:::", "DATA CHANGE DETECTED from" + senderUserID);

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
                                Box box = new Box(UsefulFunctions.decodeText(element));
                                if (box.getType() == Constants.Message.MESSAGE_TYPE_TEXT) {
                                    Message msg = new Message(box.getData());
                                    if (localDB.getUser(key) == null) {
                                        fetchUserDetails(key, msg);
                                    } else {
                                        localDB.insertMessage(msg);
                                    }
                                } else if (box.getType() == Constants.Message.BOX_TYPE_GROUP_MESSAGE) {
                                    getGroupMessage(box.getData());
                                } else if (box.getType() == Constants.Message.BOX_TYPE_NEW_GROUP) {
                                    fetchGroupDetails(box.getData());
                                }
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
                                Box box = new Box(UsefulFunctions.decodeText(element));
                                Log.i("COMMUNICATOR:::", "Box type: " + box.getType());

                                if (box.getType() == Constants.Message.MESSAGE_TYPE_TEXT) {
                                    Message msg = new Message(box.getData());
                                    msg.setMsgType(Constants.Message.MESSAGE_STATUS_RECEIVED);
                                    Log.i("Communicator.Downloaded.Adding", msg.getMsg_ID());
                                    if (localDB.getUser(key) == null) {
                                        Log.i("Communicator.Downloaded.User Unknown", msg.getMsg_ID());
                                        fetchUserDetails(key, msg);
                                    } else {
                                        Log.i("Communicator.Downloaded.UserExists", msg.getMsg_ID());
                                        localDB.insertMessage(msg);
                                    }
                                } else if (box.getType() == Constants.Message.BOX_TYPE_GROUP_MESSAGE) {

                                    getGroupMessage(box.getData());

                                } else if (box.getType() == Constants.Message.BOX_TYPE_NEW_GROUP) {
                                    fetchGroupDetails(box.getData());
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
        return START_STICKY;
    }



    @Override
    public void onCreate() {
        Log.i("COMMUNICATOR:::", "CREATED ");


        //UPLOADING MESSAGES
        localDB = new TheViewModel(getApplication());

        localDB.getUploadPendingMessagesList().observe(this, messages -> {
            Log.i("COMMUNICATOR:::", "1.Pending Messages Found " + messages.size());
            HashMap<String, HashMap<String, HashSet<Message>>> map = makeUserIdMap(messages);
            HashMap<String, HashSet<Message>> userMap = map.get(USER_MAP);
            String[] userKeys = userMap.keySet().toArray(new String[0]);
            Log.i("COMMUNICATOR:::", "1.Messages For > " + Arrays.deepToString(userKeys));

            for (String key : userKeys) {
                Log.i("COMMUNICATOR:::", "Messages For > " + key);
                putUserMessage(key,userMap);
//                databaseReference.child(Constants.FIREBASE_REALTIME_DATABASE_CHILD_MSG).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        Log.i("COMMUNICATOR:::", "Pending Messages: Adding message");
//                        // Check if the user ID node exists
//                        if (dataSnapshot.hasChild(thisUserID)) {
//                            Log.i("COMMUNICATOR:::", "Pending Messages: Previous Messages Exist");
//                            // Get the array from the snapshot
//                            JSONArray array = null;
//                            try {
//                                array = new JSONArray(dataSnapshot.child(thisUserID).getValue(String.class));
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//
//                            // Add the new values to the array
//                            for (Message msg : userMap.get(key)) {
//                                Log.i("COMMUNICATOR:::", "Pending Messages: Add Message to Array");
//                                array.put(UsefulFunctions.encodeText(msg.asString(thisUserID)));
//                            }
//
//                            SerialJSONArray serialJSONArray = new SerialJSONArray(array);
//                            // Update the array in the database
//                            dataSnapshot.child(thisUserID).getRef().setValue(array.toString());
//                        } else {
//                            Log.i("COMMUNICATOR:::", "Pending Messages: No Previous Messages");
//                            // Create a new JSONArray
//                            JSONArray array = new JSONArray();
//                            for (Message msg : userMap.get(key)) {
//                                Log.i("COMMUNICATOR:::", "Pending Messages: Add Message to Array");
//                                array.put(UsefulFunctions.encodeText(msg.asString(thisUserID)));
//                            }
//                            // Add the array to the database
//                            dataSnapshot.child(thisUserID).getRef().setValue(array.toString());
//                        }
//                        Log.i("COMMUNICATOR:::", "Pending Messages: Message Sent");
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//                        Log.i("COMMUNICATOR:::", "Cancelled 3: " + databaseError);
//
//                    }
//                });
            }


            //For group Messages

            HashMap<String, HashSet<Message>> groupMap = map.get(GROUP_MAP);
            String[] groupKeys = groupMap.keySet().toArray(new String[0]);
            DatabaseReference groupDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_REALTIME_DATABASE_CHILD_GRP_MSG);
            Log.i("COMMUNICATOR.Observer:::", "GROUP MESSAGESFor > " + groupKeys.length);

            for (String groupID : groupKeys) {
                Log.i("COMMUNICATOR.Observer:::", "GROUP ID > "+ groupID );
                Group group = localDB.getUser(groupID).getGroup();
                ArrayList<String> groupUsersList = group.getUsers();
                groupUsersList.remove(thisUserID);

                //Put message in group_message
                for (Message msg : groupMap.get(groupID)) {
                    Log.i("COMMUNICATOR.UPLOADING MESSAGES:::", "Putting Messages ::::: For group");
                    if(msg.getMsgType()==Constants.Message.BOX_TYPE_NEW_GROUP) continue;
                    DatabaseReference msgRef = groupDatabaseReference.child(msg.getMsg_ID());
                    Box box = new Box(Constants.Message.MESSAGE_TYPE_TEXT, msg.asString(thisUserID));
                    msgRef.child(Constants.KEY_FIRESTORE_GROUP_MESSAGE_DATA).setValue(UsefulFunctions.encodeText(box.asString()));

                    for (String user : groupUsersList) {
                        msgRef.child(user).setValue("");
                    }
                }

                //Send Message Box to Users
                for (String key : groupUsersList) {
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
                                for (Message msg : groupMap.get(groupID)) {
                                    Log.i("COMMUNICATOR:::", "Pending Messages: Add Message to Array");
                                    if (msg.getMsgType() == Constants.Message.BOX_TYPE_NEW_GROUP) {
                                        Box newBox = new Box(Constants.Message.BOX_TYPE_NEW_GROUP,msg.getUser_id());
                                        array.put(UsefulFunctions.encodeText(newBox.asString()));
                                        msg.setMsgType(Constants.Message.MESSAGE_TYPE_INFO);
                                    }
                                    Box box = new Box(Constants.Message.BOX_TYPE_GROUP_MESSAGE, msg.getMsg_ID());
                                    array.put(UsefulFunctions.encodeText(box.asString()));
                                }

                                SerialJSONArray serialJSONArray = new SerialJSONArray(array);
                                // Update the array in the database
                                dataSnapshot.child(thisUserID).getRef().setValue(array.toString());
                            } else {
                                Log.i("COMMUNICATOR:::", "Pending Messages: No Previous Messages");
                                // Create a new JSONArray
                                JSONArray array = new JSONArray();
                                for (Message msg : groupMap.get(groupID)) {
                                    Log.i("COMMUNICATOR:::", "Pending Messages: Add Message to Array");
                                    if (msg.getMsgType() == Constants.Message.BOX_TYPE_NEW_GROUP) {
                                        Box newBox = new Box(Constants.Message.BOX_TYPE_NEW_GROUP,msg.getUser_id());
                                        array.put(UsefulFunctions.encodeText(newBox.asString()));
                                        msg.setMsgType(Constants.Message.MESSAGE_TYPE_INFO);
                                    }
                                    Box box = new Box(Constants.Message.BOX_TYPE_GROUP_MESSAGE, msg.getMsg_ID());
                                    array.put(UsefulFunctions.encodeText(box.asString()));
                                }
                                // Add the array to the database
                                dataSnapshot.child(thisUserID).getRef().setValue(array.toString());
                            }
                            Log.i("COMMUNICATOR:::", "Pending Messages: Message Sent");
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.i("COMMUNICATOR:::", "Cancelled 3: " + databaseError);

                        }
                    });
                }

            }


            //Update Message Status
            if (!messages.isEmpty()) {
                for (Message msg : messages) {
                    msg.setStatus(Constants.Message.MESSAGE_STATUS_SENT);
                }
                localDB.updateAllMessage(messages);
            }

        });

        localDB.getReceivedMessagesList().observe(this, messages -> {
            Log.i("COMMUNICATOR:::", "New Messages Found - " + messages.size());
            List<Message> msgs = localDB.getReceivedMessagesList().getValue();
            if (msgs != null) msgs.retainAll(messages);
            HashMap<String, HashSet<Message>> map = makeUsernameMap(localDB, msgs);
            notifyUser(map);
        });

        super.onCreate();
    }

    private void putUserMessage(String key, HashMap<String, HashSet<Message>> userMap){
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
                    for (Message msg : userMap.get(key)) {
                        Log.i("COMMUNICATOR:::", "Pending Messages: Add Message to Array");
                        Box box = new Box(Constants.Message.BOX_TYPE_TEXT_MESSAGE,msg.asString(thisUserID));
                        array.put(UsefulFunctions.encodeText(box.asString()));
                    }

                    SerialJSONArray serialJSONArray = new SerialJSONArray(array);
                    // Update the array in the database
                    dataSnapshot.child(thisUserID).getRef().setValue(array.toString());
                } else {
                    Log.i("COMMUNICATOR:::", "Pending Messages: No Previous Messages");
                    // Create a new JSONArray
                    JSONArray array = new JSONArray();
                    for (Message msg : userMap.get(key)) {
                        Log.i("COMMUNICATOR:::", "Pending Messages: Add Message to Array");
                        Box box = new Box(Constants.Message.BOX_TYPE_TEXT_MESSAGE,msg.asString(thisUserID));
                        array.put(UsefulFunctions.encodeText(box.asString()));                    }
                    // Add the array to the database
                    dataSnapshot.child(thisUserID).getRef().setValue(array.toString());
                }
                Log.i("COMMUNICATOR:::", "Pending Messages: Message Sent");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("COMMUNICATOR:::", "Cancelled 3: " + databaseError);

            }
        });

    }


    private void getGroupMessage(String msgID) {
        //group_messages > groupID > msgID > data > delete if single
        Log.i("COMMUNICATOR.getGroupMessage:::", "GETTING GROUP MESSAGE: " + msgID);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_REALTIME_DATABASE_CHILD_GRP_MSG).child(msgID);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String data = dataSnapshot.child("data").getValue(String.class);
                // do something with data
                if (data != null) {
                    Log.i("COMMUNICATOR.getGroupMessage:::", "Data Found ");
                    Box box= new Box(UsefulFunctions.decodeText(data));
                    Message msg = new Message(box.getData());
                    msg.setStatus(Constants.Message.MESSAGE_STATUS_RECEIVED);
                    localDB.insertMessage(msg);
                }
                if (dataSnapshot.hasChild(SharedPrefManager.getLocalUserID())) {
                    ref.child(SharedPrefManager.getLocalUserID()).removeValue().addOnCompleteListener(unused -> {
                        Log.i("COMMUNICATOR.getGroupMessage:::", "UserID Deleted");
                        if (dataSnapshot.getChildrenCount() == 2) {
                            Log.i("COMMUNICATOR.getGroupMessage:::", "Message Deleted");
                            ref.removeValue();
                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // handle error
            }
        });

    }

    private void fetchGroupDetails(String grpID) {
        Log.i("COMMUNICATOR.fetchGroupDetails:::", "Fetching group: "+ grpID);
        FirebaseFirestore fireStore = FirebaseFirestore.getInstance();
        Task<DocumentSnapshot> found = fireStore.collection(Constants.KEY_FIRESTORE_GROUPS).document(grpID).get();
        found.addOnSuccessListener(doc -> {
            String users = doc.getString(Constants.KEY_FIRESTORE_GROUP_USERS);
            ArrayList<String> userList = new ArrayList<>(Arrays.asList(users.substring(1, users.length() - 1).split(", ")));
            ArrayList<String> adminList = (ArrayList<String>) doc.get(Constants.KEY_FIRESTORE_GROUP_ADMINS);
//            ArrayList<String> adminList = new ArrayList<>(Arrays.asList(adminsss));
            Group group = new Group(doc.getString(Constants.KEY_FIRESTORE_GROUP_ID)
                    , doc.getString(Constants.KEY_FIRESTORE_GROUP_NAME)
                    , doc.getString(Constants.KEY_FIRESTORE_GROUP_CREATED_BY)
                    , doc.getString(Constants.KEY_FIRESTORE_GROUP_CREATED_TIME)
                    , userList, adminList);
            Log.i("FETCH USER::::", doc.getString(Constants.KEY_FIRESTORE_GROUP_NAME));
            User user = new User(doc.getString(Constants.KEY_FIRESTORE_GROUP_ID), doc.getString(Constants.KEY_FIRESTORE_GROUP_NAME)
                    , doc.getString(Constants.KEY_FIRESTORE_USER_PIC)
                    , doc.getString(Constants.KEY_FIRESTORE_GROUP_ID)
                    , Constants.User.USER_TYPE_GROUP
                    , group);
            localDB.insertUser(user);
            for (String userID : users.split(",")) {
                fetchUserDetails(userID, null);
            }
//            Add Info Message "User added you"
//            Message message= new Message("");
//            localDB.insertMessage(msg);
        });
    }

    private void fetchUserDetails(String user_id, Message msg) {
        Log.i("COMMUNICATOR:::", "Fetching UserDetails");
        Log.i("FETCH USER::::", user_id);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        Task<DocumentSnapshot> found = firestore.collection(Constants.KEY_FIRESTORE_USERS).document(user_id).get();
        found.addOnSuccessListener(doc -> {
             User user = new User(user_id, doc.getString(Constants.KEY_FIRESTORE_USER_NAME)
                    , doc.getString(Constants.KEY_FIRESTORE_USER_NAME)
                    , doc.getString(Constants.KEY_FIRESTORE_USER_PHONE)
                    , doc.getString(Constants.KEY_FIRESTORE_USER_PIC)
                    , doc.getString(Constants.KEY_FIRESTORE_USER_PHONE), false);
            localDB.insertUser(user);
            if (msg != null) localDB.insertMessage(msg);
        });
    }


    private HashMap<String, HashMap<String, HashSet<Message>>> makeUserIdMap(List<Message> messages) {
//        Log.i("COMMUNICATOR:::", "New Messages Found: Making ID List");
        HashMap<String, HashSet<Message>> userMap = new HashMap<>();
        HashMap<String, HashSet<Message>> groupMap = new HashMap<>();
        for (Message msg : messages) {
            if (msg.getGroupUserID() == null) {
                if (userMap.containsKey(msg.getUser_id())) {
                    userMap.put(msg.getUser_id(), new HashSet<Message>() {
                        {
                            addAll(userMap.get(msg.getUser_id()));
                            add(msg);
                        }
                    });
                } else {
                    userMap.put(msg.getUser_id(), new HashSet<Message>() {{
                        add(msg);
                    }});
                }
            } else if (msg.getGroupUserID() != null) {
                if (groupMap.containsKey(msg.getUser_id())) {
                    groupMap.put(msg.getUser_id(), new HashSet<Message>() {
                        {
                            addAll(groupMap.get(msg.getUser_id()));
                            add(msg);
                        }
                    });
                } else {
                    groupMap.put(msg.getUser_id(), new HashSet<Message>() {{
                        add(msg);
                    }});
                }
            }
        }
        HashMap<String, HashMap<String, HashSet<Message>>> map = new HashMap<>();
        map.put(USER_MAP, userMap);
        map.put(GROUP_MAP, groupMap);
        return map;
    }


    //Notifications
    private HashMap<String, HashSet<Message>> makeUsernameMap(TheViewModel localDB, List<Message> messages) {
        Log.i("COMMUNICATOR:::", "New Messages Found: Making Name List");

        HashMap<String, HashMap<String, HashSet<Message>>> m = makeUserIdMap(messages);
        HashMap<String, HashSet<Message>> map = m.get(USER_MAP);

        String[] keys = map.keySet().toArray(new String[0]);
        for (String key : keys) {
            map.put(localDB.getUser(key).getDisplayName(), map.get(key));
            map.remove(key);
        }
        return map;
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
            if (messages.get(0).getMsg() != null) {
                String msg = messages.get(0).getMsg();
                builder.setContentText(msg.substring(0, Math.min(msg.length(), 8)) + "...");
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText(msg.substring(0, Math.min(msg.length(), 8)) + "..."));
            } else {
                builder.setContentText("Media");
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText("Image"));
            }
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
            builder.setContentIntent(pendingIntent);
            builder.setAutoCancel(true);


            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
            notificationManagerCompat.notify(notificationID++, builder.build());

        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("SERVICE BINDING::::", "DONE");
        super.onBind(intent);

        return null;
    }
}
