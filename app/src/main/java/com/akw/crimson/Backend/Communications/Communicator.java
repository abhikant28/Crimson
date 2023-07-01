package com.akw.crimson.Backend.Communications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LifecycleService;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.akw.crimson.Backend.AppObjects.Box;
import com.akw.crimson.Backend.AppObjects.Group;
import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.SharedPrefManager;
import com.akw.crimson.Backend.Database.TheViewModel;
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.MainChatList;
import com.akw.crimson.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.LinkedBlockingDeque;

public class Communicator extends LifecycleService {

    public static TheViewModel localDB;
    private final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl(Constants.FIREBASE_REALTIME_DATABASE_MSG_URL);
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    static BroadcastReceiver profileUploadBroadcastReceiver;
    private final LinkedBlockingDeque<String> messageQueue = new LinkedBlockingDeque<>(), notificationQueue = new LinkedBlockingDeque<>();
    private final LinkedBlockingDeque<Message> receivedInfoMessageQueue = new LinkedBlockingDeque<>();

    private static Communicator instance = null;
    public static HashSet<String> mediaDownloading = new HashSet<>(), mediaUploading = new HashSet<>();
    private final HashMap<String, ArrayList<Message>> userMsgMap = new HashMap<>(), notificationMsgMap = new HashMap<>();
    private List<Message> prevMsgList, prevInfoMsgList, prevNotifiedList;
    public static String thisUserID;
    private boolean uploadingMessages = false, receivedInfoProcessing = false, makingNotification=false;
    private int serviceStarter = 0;
    private BroadcastReceiver profileDownloadBroadcastReceiver;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new SharedPrefManager(this);

        instance = this;

        Log.i("COM:::", "STARTED ");

        String fcmSenderUserID = null;

        super.onStartCommand(intent, flags, startId);
        if (intent != null && intent.getExtras() != null) {
            serviceStarter = intent.getExtras().getInt(Constants.Intent.KEY_INTENT_STARTED_BY, 0);
            fcmSenderUserID = intent.getExtras().getString(Constants.Intent.KEY_INTENT_USERID, "2");
        }

        thisUserID = SharedPrefManager.getLocalUserID();

        Log.i("COM:::", "93 onStart Started");
        DatabaseReference userData = databaseReference.child(Constants.FIREBASE_REALTIME_DATABASE_CHILD_MSG).child(thisUserID);

        if (serviceStarter == Constants.Intent.KEY_INTENT_START_FCM) {
            Log.i("COM:::", "97 Started by FCM");
            Log.i("USER ID::::", thisUserID);

            String finalFcmSenderUserID = fcmSenderUserID;
            userData.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.i("COM:::", "DATA CHANGE DETECTED from" + finalFcmSenderUserID);

                    assert finalFcmSenderUserID != null;
                    DataSnapshot child = snapshot.child(finalFcmSenderUserID);

                    if (child.exists()) {
                        Log.i("COM:::", "DATA FROM > " + child.getValue());
                        // Get the key value of the child
                        String key = child.getKey();
                        // Get the value of the child
                        JSONArray value = child.getValue(JSONArray.class);
                        // Iterate over the elements in the array
                        for (int i = 0; i < Objects.requireNonNull(value).length(); i++) {
                            // Get the string value of the element
                            String element = null;
                            try {
                                element = value.getString(i);
                                Box box = new Box(UsefulFunctions.decodeText(element));
                                if (box.getType() == Constants.Message.MESSAGE_TYPE_CHAT) {
                                    Message msg = new Message(box.getData());
                                    if (localDB.getUser(key) == null) {
                                        fetchUserDetails(key, msg);
                                    } else {
                                        localDB.insertMessage(msg);
                                    }
                                } else if (box.getType() == Constants.Box.BOX_TYPE_GROUP_MESSAGE) {
                                    getGroupMessage(box.getData());
                                } else if (box.getType() == Constants.Box.BOX_TYPE_NEW_GROUP) {
                                    fetchGroupDetails(box.getData());
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                        child.getRef().removeValue();
                    } else {
                        Log.i("COM:::", "141 DATA FROM > " + finalFcmSenderUserID + " Not Found");
                    }
                    stopSelf();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.i("COM:::", "148 Cancelled 1");
                }
            });
        } else {
            Log.i("COM:::", "152 Started in Default Mode");
            userData.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.i("COM:::", "156 Database Change Detected");

                    for (DataSnapshot child : snapshot.getChildren()) {

                        // Get the key value of the child
                        String key = child.getKey();
                        Log.i("COM:::", "162 Messages by: " + key);

                        // Get the value of the child
                        String str = child.getValue(String.class);
                        JSONArray value;
                        try {
                            value = new JSONArray(str);
                            // Iterate over the elements in the array
                                Log.i("COM:::", "170 Box COUNT: " + value.length());
                            for (int i = 0; i < Objects.requireNonNull(value).length(); i++) {

                                // Get the string value of the element
                                String element = null;
                                element = value.getString(i);
                                Box box = new Box(UsefulFunctions.decodeText(element));
                                Log.i("COM:::", "177 Box type: " + box.getType());

                                if (box.getType() == Constants.Box.BOX_TYPE_TEXT_MESSAGE) {

                                    Message msg = new Message(box.getData());
                                    msg.setStatus(Constants.Message.MESSAGE_STATUS_RECEIVED);
                                    msg.setMsgType(Constants.Message.MESSAGE_TYPE_CHAT);
                                    Log.i("COM.Downloaded.Adding","184 " + msg.getMsg_ID());

                                    if (localDB.getUser(key) == null) {
                                        Log.i("COM.Downloaded.User Unknown", "187 " +msg.getMsg_ID());

                                        fetchUserDetails(key, msg);

                                    } else {
                                        Log.i("COM.Downloaded.UserExists", "192 " +msg.getMsg_ID());

                                        localDB.insertMessage(msg);
                                    }

                                } else if (box.getType() == Constants.Box.BOX_TYPE_GROUP_MESSAGE) {

                                    getGroupMessage(box.getData());

                                } else if (box.getType() == Constants.Box.BOX_TYPE_INTERNAL_MESSAGE) {

                                    Message msg;

                                    switch (box.getData()) {
                                        case Constants.Box.BOX_TYPE_INTERNAL_PIC_UPDATE + "":

                                            Log.i("COM.ReceivedFromFirebase:::", "::208 Box Type::BOX_TYPE_INTERNAL_PIC_UPDATE:" + Constants.Box.BOX_TYPE_INTERNAL_PIC_UPDATE);

                                            msg = new Message(thisUserID, box.getUserID(), null, true, null
                                                    , false, Constants.Message.MESSAGE_STATUS_RECEIVED, Constants.Message.MESSAGE_TYPE_INTERNAL
                                                    , Constants.Media.KEY_MESSAGE_MEDIA_TYPE_PROFILE, null, box.getAppendix());
                                            localDB.insertMessage(msg);

                                            break;

                                        case Constants.Box.BOX_TYPE_INTERNAL_PROFILE_UPDATE + "":
                                            Log.i("COM.ReceivedFromFirebase:::", "::218 Box Type:BOX_TYPE_INTERNAL_PROFILE_UPDATE::" + Constants.Box.BOX_TYPE_INTERNAL_PROFILE_UPDATE);

                                            msg = new Message(thisUserID, box.getUserID(), null, false, null
                                                    , false, Constants.Message.MESSAGE_STATUS_RECEIVED, Constants.Message.MESSAGE_TYPE_INTERNAL
                                                    , Constants.Message.MESSAGE_TYPE_INTERNAL_UPDATE_PROFILE, null, null);
                                            localDB.insertMessage(msg);
                                            break;

                                        case Constants.Box.BOX_TYPE_NEW_GROUP + "":

                                            fetchGroupDetails(box.getAppendix());
                                            break;
                                    }
                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        child.getRef().setValue(null);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.i("COM:::", "243 Cancelled 2");

                }
            });
        }
        return START_STICKY;
    }


    @Override
    public void onCreate() {
        Log.i("COM:::", "254 CREATED ");


        //UPLOADING MESSAGES
        localDB = new TheViewModel(getApplication());

        localDB.getUploadPendingMessagesList().observe(this, messages -> {

            //For concurrency
            List<Message> newMessages = new ArrayList<>(messages);
            //Filter Messages
            if (prevMsgList == null) {

                prevMsgList = newMessages;
                addMsgToUploadQueue(newMessages);
                return;
            }

            for (Message m : messages) {

                if (prevMsgList.contains(m))
                    newMessages.remove(m);

            }
            prevMsgList = newMessages;

            addMsgToUploadQueue(newMessages);

        });

        localDB.getReceivedInfoMessagesList().observe(this, messages -> {

            if(messages.size()==0)
                return;
            //Filter New Messages
            Log.i("COM.getReceivedInfoMessagesList:::", "289 INFO MESSAGE FOUND:::" + messages);

            List<Message> newInfoMessages = new ArrayList<>(messages);
            if (prevInfoMsgList == null) {
                prevInfoMsgList = newInfoMessages;
                addMsgToReceivedInfoQueue(newInfoMessages);
                return;
            }
            Log.i("COM.getReceivedInfoMessagesList:::", "297 INFO MESSAGE FOUND:::" + messages);

            for (Message m : messages) {
                if (prevInfoMsgList.contains(m))
                    newInfoMessages.remove(m);
            }
            Log.i("COM.getReceivedInfoMessagesList:::", "303 INFO MESSAGE FOUND:::" + messages);
            prevInfoMsgList = newInfoMessages;
            addMsgToReceivedInfoQueue(newInfoMessages);
        });
//        localDB.getReceivedMessagesList().observe(this, messages -> {
//            if(messages.size()==0)
//                return;
//            //Filter New Messages
//            Log.i("COM.getNotificationList:::", "312 INFO MESSAGE FOUND:::" + messages);
//
//            List<Message> newNotificationMessages = new ArrayList<>(messages);
//            if (prevNotifiedList == null) {
//                prevNotifiedList = newNotificationMessages;
//                addMsgToNotificationQueue(newNotificationMessages);
//                return;
//            }
//            Log.i("COM.getNotificationList:::", "320 INFO MESSAGE FOUND:::" + messages);
//
//            for (Message m : messages) {
//                if (prevNotifiedList.contains(m))
//                    newNotificationMessages.remove(m);
//            }
//            Log.i("COM.getNotificationList:::", "326 INFO MESSAGE FOUND:::" + messages);
//            prevNotifiedList = newNotificationMessages;
//            notifyUser(notificationMsgMap);
//        });
        super.onCreate();
    }

//    private void addMsgToNotificationQueue(List<Message> newNotificationMessages) {
//
//        newNotificationMessages.addAll(messages);
//        Log.i("COM.getReceivedInfoMessagesList:::", "308 ADDING INFO MESSAGE FOUND::" + messages.size());
//
//        if (!makingNotification) {
//            Runnable myRunnable = () -> {
//
//                Log.i("COM.InfoMessageThread:::", "314 INFO ID QUEUE::::" + receivedInfoMessageQueue);
//
//                makingNotification = true;
//                Message msg;
//
//                while (!receivedInfoMessageQueue.isEmpty()) {
//
//                }
//
//                makingNotification = false;
//            };
//
//            Thread thread = new Thread(myRunnable);
//            thread.start();
//        }
//    }


    private void addMsgToReceivedInfoQueue(List<Message> messages) {

        receivedInfoMessageQueue.addAll(messages);
        Log.i("COM.getReceivedInfoMessagesList:::", "361 ADDING INFO MESSAGE FOUND::" + messages.size());

        if (!receivedInfoProcessing) {
            Runnable myRunnable = () -> {

                Log.i("COM.InfoMessageThread:::", "366 INFO ID QUEUE::::" + receivedInfoMessageQueue);

                receivedInfoProcessing = true;
                Message msg;

                while (!receivedInfoMessageQueue.isEmpty()) {
                    msg = receivedInfoMessageQueue.pop();
                    Log.i("COM.addMsgToInfoQueue:::", "373 while. ::::" + msg.getMediaType());

                    if (msg.getMediaType() == Constants.Media.KEY_MESSAGE_MEDIA_TYPE_PROFILE) {
                        downloadProfilePic(this, msg);
                        Log.i("COM.addMsgToReceivedInfoQueue.:::::::::", "377 UPDATE PROFILE INFO FOUND");

                    }
                }

                receivedInfoProcessing = false;
            };

            Thread thread = new Thread(myRunnable);
            thread.start();
        }
    }

    private void addMsgToUploadQueue(List<Message> messages) {


        boolean msgFound = false;
        ArrayList<Message> list = new ArrayList<>();
        for (Message m : messages) {
//            Log.i("COM.addMsgToQueue:::", "MSG USER ID::::" + m.getUser_id());
            if (m.getMsgType() != Constants.Message.MESSAGE_TYPE_INTERNAL) {
                msgFound = true;
            } else {
                Log.i("COM.addMsgToQueue:::", "400 ::INFO MESSAGE::" + m.getUser_id());

            }
            if (userMsgMap.containsKey(m.getUser_id())) {

                list = userMsgMap.get(m.getUser_id());
                list.add(m);
                userMsgMap.put(m.getUser_id(), list);
            } else {
                list = new ArrayList<>();
                list.add(m);
                userMsgMap.put(m.getUser_id(), list);
                if (m.getGroupUserID() != null) {
                    messageQueue.offerFirst(m.getUser_id());
                } else {
//                    Log.i("COM.addMessageToQueue:::::", "MsgType::"+m.getMsgType()+"_"+ Constants.Message.MESSAGE_TYPE_INTERNAL);

                    messageQueue.add(m.getUser_id());
                }
            }
//            Log.i("COM.addMsgToQueue:::", "352USER ID Queue::::" + messageQueue);

            if (m.getGroupUserID() != null) {
                ArrayList<String> grpUser = localDB.getUser(m.getUser_id()).group.getUsers();
                grpUser.remove(thisUserID);
                for (String s : grpUser) {
                    if (userMsgMap.containsKey(s)) {
                        list = userMsgMap.get(s);
                    } else {
                        list = new ArrayList<>();
                    }
                    list.add(m);
                    userMsgMap.put(s, list);
                }
            }


        }

//        Log.d("COMMUNICATIOR.addMessageToQueue:::::","THREAD VALUES::::"+msgFound+"_"+uploadingMessages+"_"+messageQueue.isEmpty());
        if (msgFound && !uploadingMessages && !messageQueue.isEmpty()) {
            startMessageUploadThread();
        }
    }

    private void startMessageUploadThread() {
        Runnable myRunnable = () -> {
            Log.i("COM.startUploadThread:::", "447 ID QUEUE::::" + messageQueue);

            uploadingMessages = true;
            String userID = "";

            //For User Messages
            while (!messageQueue.isEmpty()) {
                while (!messageQueue.isEmpty()) {

                    userID = messageQueue.pop();
                    ArrayList<Message> messageList = userMsgMap.get(userID);
                    userMsgMap.remove(userID);
                    Log.i("COM.startUploadThread:::", "459 USER ID::::" + userID);

                    User user = localDB.getUser(userID);
                    putUserMessage(user, messageList);
                }

                if (!userMsgMap.isEmpty())
                    messageQueue.addAll(userMsgMap.keySet());
            }

            uploadingMessages = false;
        };

        Thread thread = new Thread(myRunnable);
        thread.start();
    }

    private void putUserMessage(User user, ArrayList<Message> userMessages) {
        if (user.getType() == Constants.User.USER_TYPE_USER) {

            databaseReference.child(Constants.FIREBASE_REALTIME_DATABASE_CHILD_MSG).child(user.getUser_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    Log.i("COM:::", "483 Pending Messages: Adding message");
                    // Check if the user ID node exists
                    JSONArray array = null;

                    if (dataSnapshot.hasChild(thisUserID)) {
                        Log.i("COM:::", "488 Pending Messages: Previous Messages Exist");
                        // Get the array from the snapshot
                        try {

                            array = new JSONArray(dataSnapshot.child(thisUserID).getValue(String.class));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {

                        array = new JSONArray();

                    }

                    // Add the new values to the array
                    for (Message msg : userMessages) {

                        if (msg.getMsgType() == Constants.Message.MESSAGE_TYPE_CHAT) {

                            if (msg.getGroupUserID() == null) {

                                Box box = new Box(Constants.Box.BOX_TYPE_TEXT_MESSAGE, msg.encodeMessage(thisUserID));
                                Objects.requireNonNull(array).put(UsefulFunctions.encodeText(box.encodeBox()));

                            } else {

                                Box box = new Box(Constants.Box.BOX_TYPE_GROUP_MESSAGE, msg.getMsg_ID());
                                array.put(UsefulFunctions.encodeText(box.encodeBox()));

                                Log.i("COM:::", "518 Pending Messages: Add Message to Array");
                            }

                        } else if (msg.getMsgType() == Constants.Message.MESSAGE_TYPE_INTERNAL) {
                            if (msg.getMediaType() == Constants.Media.KEY_MESSAGE_MEDIA_TYPE_PROFILE) {

                                Box box = new Box(Constants.Box.BOX_TYPE_INTERNAL_MESSAGE, Constants.Box.BOX_TYPE_INTERNAL_PIC_UPDATE + "");
                                box.setUserID(thisUserID);
                                box.setAppendix(msg.getMediaUrl());

                                array.put(UsefulFunctions.encodeText(box.encodeBox()));
                            } else if (msg.getMediaType() == Constants.Box.BOX_TYPE_NEW_GROUP) {

                                Box newBox = new Box(Constants.Box.BOX_TYPE_INTERNAL_MESSAGE, Constants.Box.BOX_TYPE_NEW_GROUP + "");
                                newBox.setAppendix(msg.getUser_id());
                                array.put(UsefulFunctions.encodeText(newBox.encodeBox()));

                            }
                            Log.i("COM.putMessage:::", "537 INFO MSG Deleted::::" );

                            localDB.deleteMessage(msg);
                        }
                    }
                    // Update the array in the database
                    dataSnapshot.child(thisUserID).getRef().setValue(array.toString()).addOnSuccessListener(unused -> {
                        for (Message msg : userMessages) {

                            msg.setStatus(Constants.Message.MESSAGE_STATUS_SENT);
                        }

                        localDB.updateAllMessage(userMessages);

                    });
                    Log.i("COM:::", "552 Pending Messages: Message Sent");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.i("COM:::", "557 Cancelled 3: " + databaseError);

                }
            });
        } else if (user.getType() == Constants.User.USER_TYPE_GROUP) {
            DatabaseReference groupDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_REALTIME_DATABASE_CHILD_GRP_MSG);

            for (Message msg : userMessages) {
                Log.i("COM.UPLOADING MESSAGES:::", "565 Putting Messages ::::: For group");
                if (msg.getMsgType() == Constants.Box.BOX_TYPE_NEW_GROUP) continue;

                DatabaseReference msgRef = groupDatabaseReference.child(msg.getMsg_ID());
                Box box = new Box(Constants.Message.MESSAGE_TYPE_CHAT, msg.encodeMessage(thisUserID));

                msgRef.child(Constants.KEY_FIRESTORE_GROUP_MESSAGE_DATA).setValue(UsefulFunctions.encodeText(box.encodeBox()));

                ArrayList<String> groupUsersList = user.group.getUsers();
                groupUsersList.remove(thisUserID);

                for (String u : groupUsersList) {
                    msgRef.child(u).setValue("");
                }

                msgRef.child(groupUsersList.get(groupUsersList.size() - 1)).setValue("").addOnSuccessListener(unused -> {
                    for (Message m : userMessages) {

                        m.setStatus(Constants.Message.MESSAGE_STATUS_SENT);

                    }

                    localDB.updateAllMessage(userMessages);
                });
            }
        }
    }

    private void getGroupMessage(String msgID) {
        //group_messages > groupID > msgID > data > delete if single
        Log.i("COM.getGroupMessage:::", "595 GETTING GROUP MESSAGE: " + msgID);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_REALTIME_DATABASE_CHILD_GRP_MSG).child(msgID);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String data = dataSnapshot.child("data").getValue(String.class);
                // do something with data
                if (data != null) {
                    Log.i("COM.getGroupMessage:::", "605 Data Found ");
                    Box box = new Box(UsefulFunctions.decodeText(data));
                    Message msg = new Message(box.getData());
                    msg.setStatus(Constants.Message.MESSAGE_STATUS_RECEIVED);
                    localDB.insertMessage(msg);
                }
                if (dataSnapshot.hasChild(SharedPrefManager.getLocalUserID())) {
                    ref.child(SharedPrefManager.getLocalUserID()).removeValue().addOnCompleteListener(unused -> {
                        Log.i("COM.getGroupMessage:::", "613 UserID Deleted");
                        Log.i("COM.getGroupMessage:::", "614 Child KEYS:: " + ref.get().getResult().getKey());
                        if (dataSnapshot.getChildrenCount() == 1) {
                            Log.i("COM.getGroupMessage:::", "616 Message Deleted");
                            ref.removeValue();
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // handle error
            }
        });

    }

    private void fetchGroupDetails(String grpID) {
        Log.i("COM.fetchGroupDetails:::", "633 Fetching group: " + grpID);
        Task<DocumentSnapshot> found = firestore.collection(Constants.KEY_FIRESTORE_GROUPS).document(grpID).get();
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
        Log.i("COM:::", "662 Fetching UserDetails");
        Log.i("FETCH USER::::", user_id);

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


    private void notifyUser(HashMap<String,  ArrayList<Message>> map) {
        Log.i("COM:::", "679 New Messages Found: Making Notifying User");
        String[] keys = map.keySet().toArray(new String[0]);
        Intent intent;
        Log.i("COM:::", "682 New Messages Found: Making Notifying User Count > " + map.size());

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
            Log.i("COM:::", "702 New Messages Found: Making Notifications");
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
            builder.setSmallIcon(R.drawable.ic_launcher_foreground);
            ArrayList<Message> messages = new ArrayList<>(map.get(key));
            builder.setContentTitle(key);
            if (messages.get(0).getMsg() != null) {
                String msg = messages.get(0).getMsg();
                String text = msg.substring(0, Math.min(msg.length(), 8)) + "...";
                builder.setContentText(text);
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
            }
//            else {
//                builder.setContentText("Media");
//                builder.setStyle(new NotificationCompat.BigTextStyle().bigText("Image"));
//            }
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
            builder.setContentIntent(pendingIntent);
            builder.setAutoCancel(true);

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
            notificationManagerCompat.notify(notificationID++, builder.build());

        }

    }

    public static void updateProfilePic(Context cxt, String uri) {

        Log.i("COM.updateProfilePic:::", "730 UPDATING PROFILE PIC PVT ONLINE");
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        Task<DocumentSnapshot> found = firestore.collection(Constants.KEY_FIRESTORE_USERS).document(thisUserID).get();
        found.addOnSuccessListener(docSnap -> {
            if (docSnap.contains(Constants.KEY_FIRESTORE_USER_PROFILE_PIC) && docSnap.getString(Constants.KEY_FIRESTORE_USER_PROFILE_PIC) != null) {
                String profileValue = docSnap.getString(Constants.KEY_FIRESTORE_USER_PROFILE_PIC);
                //Value exists for profilePic
                deleteDocAndPic(profileValue);
            }

            //Upload the profilePic

            profileUploadBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getExtras().getInt(Constants.Intent.KEY_INTENT_RESULT_CODE, 0) == UploadFileService.RESULT_FAIL)
                        return;
                    // Handle the result received from the called Service
                    Log.i("COM.updateProfilePic.:::::::::", "748 onReceive.Received");
                    String mediaUrl = intent.getStringExtra(Constants.Intent.KEY_INTENT_URI);
                    // Process the result
                    ArrayList<User> users = (ArrayList<User>) localDB.getConnectedUsers();
                    if (users.size() != 0) {
                        ArrayList<Message> msgList = new ArrayList<>();
                        int i = 0;
                        for (User u : users) {
                            if (u.getUser_id().equals(thisUserID)) {
                                continue;
                            }
                            msgList.add(new Message(thisUserID + (i++), u.getUser_id(), null
                                    , true, null, true, Constants.Message.MESSAGE_STATUS_PENDING_UPLOAD
                                    , Constants.Message.MESSAGE_TYPE_INTERNAL, Constants.Media.KEY_MESSAGE_MEDIA_TYPE_PROFILE
                                    , thisUserID, mediaUrl));
                        }
                        Log.i("COM.updateProfilePic:::", "764 onReceive.:::: Adding Info msg to DB");

                        localDB.insertAllMessage(msgList);
                    }
                    unregisterUploadBroadcastService(context);
                }
            };
            LocalBroadcastManager.getInstance(instance.getApplicationContext()).registerReceiver(profileUploadBroadcastReceiver, new IntentFilter("profilePicUpdate"));
            Log.i("COM.updateProfilePic:::", "772 Starting UploadFileService");

            Intent intent = new Intent(cxt, UploadFileService.class);
            intent.putExtra(Constants.Intent.KEY_INTENT_FILE_PATH, uri);
            intent.putExtra(Constants.Intent.KEY_INTENT_UPLOAD_TYPE, Constants.Intent.KEY_INTENT_UPLOAD_TYPE_PROFILE);
            cxt.startService(intent);

        });
    }

    public void downloadProfilePic(Context cxt, Message msg) {
        Log.d("COM.downloadProfilePic:::::::","783 MsgID::"+msg.getMsg_ID()+"::"+msg.getStatus()+"::"+msg.getMsgType());

        //Download the profilePic

        profileDownloadBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("COM.downloadProfilePic.:::::::::", "790 onReceive.Received");
                if (intent.getExtras().getInt(Constants.Intent.KEY_INTENT_RESULT_CODE, 0) == DownloadFileService.RESULT_FAIL)
                    return;
                // Handle the result received from the called Service
                // Process the result
                Log.i("COM.downloadProfilePic.:::::::::", "795 onReceive. DELETING MESSAGE");
                localDB.deleteMessage(msg);
                unregisterDownloadBroadcastService(context);
            }
        };

        LocalBroadcastManager.getInstance(instance.getApplicationContext()).registerReceiver(profileDownloadBroadcastReceiver, new IntentFilter(msg.getMsg_ID()));

        Log.d("COM.downloadProPic:::::::","803 Starting Download::"+msg.getMsg_ID());

        Intent intent = new Intent(cxt, DownloadFileService.class);
        intent.putExtra(Constants.Intent.KEY_INTENT_MESSAGE_ID, msg.getMsg_ID());
        cxt.startService(intent);

    }

    private void unregisterDownloadBroadcastService(Context context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(profileDownloadBroadcastReceiver);

    }

    private static void deleteDocAndPic(String profileValue) {
        DocumentReference mediaDocRef = FirebaseFirestore.getInstance().collection(Constants.KEY_FCM_ATTACHMENTS_REFERENCE).document(profileValue);
        final StorageReference mediaRef = FirebaseStorage.getInstance().getReference();

        Log.i("COM.deleteDocAndPic:::::::::", "820 CHECKING");

        mediaDocRef.get().addOnSuccessListener(documentSnapshot -> {
            // Check if the document exists and contains the "data" field
            if (documentSnapshot.exists() && documentSnapshot.contains("data")) {
                String mediaData = documentSnapshot.getString("data");
                if (mediaData != null) {
                    StorageReference mediaToDeleteRef = mediaRef.child("profile" + "/" + mediaData);
                    // Delete the media file
                    mediaToDeleteRef.delete().addOnSuccessListener(unused ->
                                    // Delete the document after successful deletion of the media
                                    mediaDocRef.delete().addOnSuccessListener(unused2 ->
                                                    Log.d("COM.deleteDocAndPic:::::::::", "832 Media and document deleted successfully"))
                                            .addOnFailureListener(e ->
                                                    Log.e("COM.deleteDocAndPic:::::::::", "834 Failed to delete the document: " + e.getMessage())))
                            .addOnFailureListener(e ->
                                    Log.e("COM.deleteDocAndPic:::::::::", "836 Failed to delete the media: " + e.getMessage()));
                }
            }
        }).addOnFailureListener(e ->
                Log.e("COM.deleteDocAndPic:::::::::", "840 Failed to retrieve the document: " + e.getMessage()));
    }

    private static void unregisterUploadBroadcastService(Context context) {

        LocalBroadcastManager.getInstance(context).unregisterReceiver(profileUploadBroadcastReceiver);

    }

    @Nullable
    @Override
    public IBinder onBind(@NonNull Intent intent) {
        Log.i("SERVICE BINDING::::", "852 DONE");
        super.onBind(intent);

        return null;
    }
}
