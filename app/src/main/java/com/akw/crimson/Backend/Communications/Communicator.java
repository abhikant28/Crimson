package com.akw.crimson.Backend.Communications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.util.concurrent.LinkedBlockingDeque;

public class Communicator extends LifecycleService {

    public static TheViewModel localDB;
    private final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl(Constants.FIREBASE_REALTIME_DATABASE_MSG_URL);
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    static BroadcastReceiver profileUploadBroadcastReceiver;
    private final LinkedBlockingDeque<String> messageQueue = new LinkedBlockingDeque<>(), uploadInfoMessageQueue = new LinkedBlockingDeque<>();
    ;
    private final LinkedBlockingDeque<Message> receivedInfoMessageQueue = new LinkedBlockingDeque<>();

    private static Communicator instance = null;
    public static HashSet<String> mediaDownloading = new HashSet<>(), mediaUploading = new HashSet<>();
    private HashMap<String, ArrayList<Message>> userMsgMap = new HashMap<>();
    private List<Message> prevMsgList, prevInfoMsgList;
    public static String thisUserID;
    private boolean uploadingMessages = false, receivedInfoProcessing = false;
    private int serviceStarter = 0;
    private BroadcastReceiver profileDownloadBroadcastReceiver;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new SharedPrefManager(this);

        instance = this;

        Log.i("COMMUNICATOR:::", "STARTED ");

        String fcmSenderUserID = null;

        super.onStartCommand(intent, flags, startId);
        if (intent != null && intent.getExtras() != null) {
            serviceStarter = intent.getExtras().getInt(Constants.Intent.KEY_INTENT_STARTED_BY, 0);
            fcmSenderUserID = intent.getExtras().getString(Constants.Intent.KEY_INTENT_USERID, "2");
        }

        thisUserID = SharedPrefManager.getLocalUserID();

        Log.i("COMMUNICATOR:::", "onStart Started");
        DatabaseReference userData = databaseReference.child(Constants.FIREBASE_REALTIME_DATABASE_CHILD_MSG).child(thisUserID);

        if (serviceStarter == Constants.Intent.KEY_INTENT_START_FCM) {
            Log.i("COMMUNICATOR:::", "Started by FCM");
            Log.i("USER ID::::", thisUserID);

            String finalFcmSenderUserID = fcmSenderUserID;
            userData.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.i("COMMUNICATOR:::", "DATA CHANGE DETECTED from" + finalFcmSenderUserID);

                    DataSnapshot child = snapshot.child(finalFcmSenderUserID);

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
                                } else if (box.getType() == Constants.Box.BOX_TYPE_GROUP_MESSAGE) {
                                    getGroupMessage(box.getData());
                                } else if (box.getType() == Constants.Box.BOX_TYPE_NEW_GROUP) {
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
                        Log.i("COMMUNICATOR:::", "DATA FROM > " + finalFcmSenderUserID + " Not Found");
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
                        JSONArray value;
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

                                if (box.getType() == Constants.Box.BOX_TYPE_TEXT_MESSAGE) {

                                    Message msg = new Message(box.getData());
                                    msg.setStatus(Constants.Message.MESSAGE_STATUS_RECEIVED);
                                    msg.setMsgType(Constants.Message.MESSAGE_TYPE_TEXT);
                                    Log.i("Communicator.Downloaded.Adding", msg.getMsg_ID());

                                    if (localDB.getUser(key) == null) {
                                        Log.i("Communicator.Downloaded.User Unknown", msg.getMsg_ID());

                                        fetchUserDetails(key, msg);

                                    } else {
                                        Log.i("Communicator.Downloaded.UserExists", msg.getMsg_ID());

                                        localDB.insertMessage(msg);
                                    }

                                } else if (box.getType() == Constants.Box.BOX_TYPE_GROUP_MESSAGE) {

                                    getGroupMessage(box.getData());

                                } else if (box.getType() == Constants.Box.BOX_TYPE_INFO_MESSAGE) {

                                    Message msg;

                                    switch (box.getData()) {
                                        case Constants.Box.BOX_TYPE_INFO_PIC_UPDATE + "":

                                            msg = new Message(thisUserID, box.getUserID(), null, true, null
                                                    , false, Constants.Message.MESSAGE_STATUS_RECEIVED, Constants.Message.MESSAGE_TYPE_INFO
                                                    , Constants.Media.KEY_MESSAGE_MEDIA_TYPE_PROFILE, null, box.getAppendix());
                                            localDB.insertMessage(msg);

                                            break;

                                        case Constants.Box.BOX_TYPE_INFO_PROFILE_UPDATE + "":

                                            msg = new Message(thisUserID, box.getUserID(), null, false, null
                                                    , false, Constants.Message.MESSAGE_STATUS_RECEIVED, Constants.Message.MESSAGE_TYPE_INFO
                                                    , Constants.Message.MESSAGE_TYPE_INFO_INTERNAL_UPDATE_PROFILE, null, null);
                                            localDB.insertMessage(msg);
                                            break;

                                        case Constants.Box.BOX_TYPE_NEW_GROUP + "":

                                            fetchGroupDetails(box.getAppendix());
                                            break;
                                    }
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

            //For concurrency
            List<Message> newMessages= new ArrayList<>(messages);
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

            //Filter New Messages
            Log.i("Communicator.getReceivedInfoMessagesList:::", "274 INFO MESSAGE FOUND:::"+messages);

            List<Message> newInfoMessages=new ArrayList<>(messages);
            if (prevInfoMsgList == null) {
                prevInfoMsgList = newInfoMessages;
                addMsgToReceivedInfoQueue(newInfoMessages);
                return;
            }

            for (Message m : messages) {
                if (prevInfoMsgList.contains(m))
                    newInfoMessages.remove(m);
            }
            prevInfoMsgList = newInfoMessages;
            addMsgToReceivedInfoQueue(newInfoMessages);
        });
//        localDB.getReceivedMessagesList().observe(this, messages -> {
//            Log.i("COMMUNICATOR:::", "New Messages Found - " + messages.size());
//            List<Message> msgs = localDB.getReceivedMessagesList().getValue();
//            if (msgs != null) msgs.retainAll(messages);
//            HashMap<String, HashSet<Message>> map = makeUsernameMap(localDB, msgs);
//            notifyUser(map);
//        });
        super.onCreate();
    }


    private void addMsgToReceivedInfoQueue(List<Message> messages) {

        receivedInfoMessageQueue.addAll(messages);
        Log.i("Communicator.getReceivedInfoMessagesList:::", "304 ADDING INFO MESSAGE FOUND");

        if (!receivedInfoProcessing) {
            Runnable myRunnable = () -> {
                Log.i("Communicator.InfoMessageThread:::", "INFO ID QUEUE::::" + receivedInfoMessageQueue);

                receivedInfoProcessing = true;
                Message msg;

                while (!receivedInfoMessageQueue.isEmpty()) {
                    msg = receivedInfoMessageQueue.pop();

                    if (msg.getMediaType() == Constants.Message.MESSAGE_TYPE_INFO_INTERNAL_UPDATE_PROFILE_PICTURE) {
                        downloadProfilePic(this, msg);
                        Log.i("Communicator.addMsgToReceivedInfoQueue.:::::::::", "UPDATE PROFILE INFO FOUND");

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
//            Log.i("Communicator.addMsgToQueue:::", "MSG USER ID::::" + m.getUser_id());
            if (m.getMsgType() != Constants.Message.MESSAGE_TYPE_INFO) {
                msgFound = true;
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
//                    Log.i("Communicator.addMessageToQueue:::::", "MsgType::"+m.getMsgType()+"_"+ Constants.Message.MESSAGE_TYPE_INFO);

                    messageQueue.add(m.getUser_id());
                }
            }
//            Log.i("Communicator.addMsgToQueue:::", "352USER ID Queue::::" + messageQueue);

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
            Log.i("Communicator.startUploadThread:::", "378 ID QUEUE::::" + messageQueue);

            uploadingMessages = true;
            String userID = "";
            //For User Messages
            while (!messageQueue.isEmpty()) {
                userID = messageQueue.pop();
                ArrayList<Message> messageList = userMsgMap.get(userID);
                userMsgMap.remove(userID);
                Log.i("Communicator.startUploadThread:::", "387 USER ID::::" + userID);
                User user = localDB.getUser(userID);
                putUserMessage(user, messageList);
            }

            uploadingMessages = false;
        };

        Thread thread = new Thread(myRunnable);
        thread.start();
    }

    private void putUserMessage(User user, ArrayList<Message> userMessages)  {
        if (user.getType() == Constants.User.USER_TYPE_USER) {
            databaseReference.child(Constants.FIREBASE_REALTIME_DATABASE_CHILD_MSG).child(user.getUser_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    Log.i("COMMUNICATOR:::", "Pending Messages: Adding message");
                    // Check if the user ID node exists
                    JSONArray array = null;
                    if (dataSnapshot.hasChild(thisUserID)) {
                        Log.i("COMMUNICATOR:::", "Pending Messages: Previous Messages Exist");
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
                        if (msg.getMsgType() == Constants.Message.MESSAGE_TYPE_TEXT) {
                            if (msg.getGroupUserID() == null) {
                                Box box = new Box(Constants.Box.BOX_TYPE_TEXT_MESSAGE, msg.asString(thisUserID));
                                array.put(UsefulFunctions.encodeText(box.asString()));
                            } else {
                                Box box = new Box(Constants.Box.BOX_TYPE_GROUP_MESSAGE, msg.getMsg_ID());
                                array.put(UsefulFunctions.encodeText(box.asString()));

                                Log.i("COMMUNICATOR:::", "Pending Messages: Add Message to Array");
                            }

                        } else if (msg.getMsgType() == Constants.Message.MESSAGE_TYPE_INFO) {
                            if (msg.getMediaType() == Constants.Message.MESSAGE_TYPE_INFO_INTERNAL_UPDATE_PROFILE_PICTURE) {
                                Box box = new Box(Constants.Box.BOX_TYPE_INFO_MESSAGE, Constants.Box.BOX_TYPE_INFO_PIC_UPDATE + "");
                                box.setAppendix(msg.getMediaUrl());
                                box.setUserID(thisUserID);
                                array.put(UsefulFunctions.encodeText(box.asString()));
                            } else if (msg.getMediaType() == Constants.Box.BOX_TYPE_NEW_GROUP) {
                                Box newBox = new Box(Constants.Box.BOX_TYPE_INFO_MESSAGE, Constants.Box.BOX_TYPE_NEW_GROUP + "");
                                newBox.setAppendix(msg.getUser_id());
                                array.put(UsefulFunctions.encodeText(newBox.asString()));
                            }
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
                    Log.i("COMMUNICATOR:::", "Pending Messages: Message Sent");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.i("COMMUNICATOR:::", "Cancelled 3: " + databaseError);

                }
            });
        } else if (user.getType() == Constants.User.USER_TYPE_GROUP) {
            DatabaseReference groupDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_REALTIME_DATABASE_CHILD_GRP_MSG);

            for (Message msg : userMessages) {
                Log.i("COMMUNICATOR.UPLOADING MESSAGES:::", "Putting Messages ::::: For group");
                if (msg.getMsgType() == Constants.Box.BOX_TYPE_NEW_GROUP) continue;
                DatabaseReference msgRef = groupDatabaseReference.child(msg.getMsg_ID());
                Box box = new Box(Constants.Message.MESSAGE_TYPE_TEXT, msg.asString(thisUserID));
                msgRef.child(Constants.KEY_FIRESTORE_GROUP_MESSAGE_DATA).setValue(UsefulFunctions.encodeText(box.asString()));
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
        Log.i("COMMUNICATOR.getGroupMessage:::", "GETTING GROUP MESSAGE: " + msgID);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_REALTIME_DATABASE_CHILD_GRP_MSG).child(msgID);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String data = dataSnapshot.child("data").getValue(String.class);
                // do something with data
                if (data != null) {
                    Log.i("COMMUNICATOR.getGroupMessage:::", "Data Found ");
                    Box box = new Box(UsefulFunctions.decodeText(data));
                    Message msg = new Message(box.getData());
                    msg.setStatus(Constants.Message.MESSAGE_STATUS_RECEIVED);
                    localDB.insertMessage(msg);
                }
                if (dataSnapshot.hasChild(SharedPrefManager.getLocalUserID())) {
                    ref.child(SharedPrefManager.getLocalUserID()).removeValue().addOnCompleteListener(unused -> {
                        Log.i("COMMUNICATOR.getGroupMessage:::", "UserID Deleted");
                        Log.i("COMMUNICATOR.getGroupMessage:::", "Child KEYS:: " + ref.get().getResult().getKey());
                        if (dataSnapshot.getChildrenCount() == 1) {
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
        Log.i("COMMUNICATOR.fetchGroupDetails:::", "Fetching group: " + grpID);
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
        Log.i("COMMUNICATOR:::", "Fetching UserDetails");
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

    //Notifications
//    private HashMap<String, HashSet<Message>> makeUsernameMap(TheViewModel localDB, List<Message> messages) {
//        Log.i("COMMUNICATOR:::", "New Messages Found: Making Name List");
//
//        HashMap<String, HashMap<String, HashSet<Message>>> m = makeUserIdMap(messages);
//        HashMap<String, HashSet<Message>> map = m.get(USER_MAP);
//
//        String[] keys = map.keySet().toArray(new String[0]);
//        for (String key : keys) {
//            map.put(localDB.getUser(key).getDisplayName(), map.get(key));
//            map.remove(key);
//        }
//        return map;
//    }

//    private void notifyUser(HashMap<String, HashSet<Message>> map) {
//        Log.i("COMMUNICATOR:::", "New Messages Found: Making Notifying User");
//        String[] keys = map.keySet().toArray(new String[0]);
//        Intent intent;
//        Log.i("COMMUNICATOR:::", "New Messages Found: Making Notifying User Count > " + map.size());
//
//        intent = new Intent(getApplicationContext(), MainChatList.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
//
//        String channelId = "chat_messages";
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence channelName = "Chat Message";
//            String channelDescription = "This notification channel is used for chat message notifications";
//            int importance = NotificationManager.IMPORTANCE_DEFAULT;
//            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
//            channel.setDescription(channelDescription);
//
//            NotificationManager notificationManager = getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(channel);
//        }
//        int notificationID = new Random().nextInt();
//
//        for (String key : keys) {
//            Log.i("COMMUNICATOR:::", "New Messages Found: Making Notifications");
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
//            builder.setSmallIcon(R.drawable.ic_launcher_foreground);
//            ArrayList<Message> messages = new ArrayList<>(map.get(key));
//            builder.setContentTitle(key);
//            if (messages.get(0).getMsg() != null) {
//                String msg = messages.get(0).getMsg();
//                builder.setContentText(msg.substring(0, Math.min(msg.length(), 8)) + "...");
//                builder.setStyle(new NotificationCompat.BigTextStyle().bigText(msg.substring(0, Math.min(msg.length(), 8)) + "..."));
//            } else {
//                builder.setContentText("Media");
//                builder.setStyle(new NotificationCompat.BigTextStyle().bigText("Image"));
//            }
//            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
//            builder.setContentIntent(pendingIntent);
//            builder.setAutoCancel(true);
//
//
//            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
//            notificationManagerCompat.notify(notificationID++, builder.build());
//
//        }
//
//    }

    public static void updateProfilePic(Context cxt, String uri) {

        Log.i("Communicator.updateProfilePic:::", "UPDATING PROFILE PIC PVT ONLINE");
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
                    Log.i("Communicator.updateProfilePic.:::::::::", "onReceive.Received");
                    String mediaUrl = intent.getStringExtra(Constants.Intent.KEY_INTENT_URI);
                    // Process the result
                    ArrayList<User> users = (ArrayList<User>) localDB.getConnectedUsers();
                    if (users.size() != 0) {
                        ArrayList<Message> msgList = new ArrayList<>();
                        for (User u : users)
                            msgList.add(new Message(thisUserID, u.getUser_id(), null
                                    , true, null, true, Constants.Message.MESSAGE_STATUS_PENDING_UPLOAD
                                    , Constants.Message.MESSAGE_TYPE_INFO, Constants.Media.KEY_MESSAGE_MEDIA_TYPE_PROFILE
                                    , thisUserID, mediaUrl));
                        localDB.insertAllMessage(msgList);
                    }
                    unregisterUploadBroadcastService(context);
                }
            };
            LocalBroadcastManager.getInstance(instance.getApplicationContext()).registerReceiver(profileUploadBroadcastReceiver, new IntentFilter("profilePicUpdate"));

            Intent intent = new Intent(cxt, UploadFileService.class);
            intent.putExtra(Constants.Intent.KEY_INTENT_FILE_PATH, uri);
            intent.putExtra(Constants.Intent.KEY_INTENT_UPLOAD_TYPE, Constants.Intent.KEY_INTENT_UPLOAD_TYPE_PROFILE);
            cxt.startService(intent);

        });
    }


    public void downloadProfilePic(Context cxt, Message msg) {


        //Download the profilePic

        profileDownloadBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("Communicator.downloadProfilePic.:::::::::", "onReceive.Received");
                if (intent.getExtras().getInt(Constants.Intent.KEY_INTENT_RESULT_CODE, 0) == DownloadFileService.RESULT_FAIL)
                    return;
                // Handle the result received from the called Service
                // Process the result
                localDB.deleteMessage(msg);
                unregisterDownloadBroadcastService(context);
            }
        };

        LocalBroadcastManager.getInstance(instance.getApplicationContext()).registerReceiver(profileDownloadBroadcastReceiver, new IntentFilter(msg.getMsg_ID()));

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

        Log.i("Communicator.deleteDocAndPic:::::::::", "CHECKING");

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
                                                    Log.d("Communicator.deleteDocAndPic:::::::::", "Media and document deleted successfully"))
                                            .addOnFailureListener(e ->
                                                    Log.e("Communicator.deleteDocAndPic:::::::::", "Failed to delete the document: " + e.getMessage())))
                            .addOnFailureListener(e ->
                                    Log.e("Communicator.deleteDocAndPic:::::::::", "Failed to delete the media: " + e.getMessage()));
                }
            }
        }).addOnFailureListener(e ->
                Log.e("Communicator.deleteDocAndPic:::::::::", "Failed to retrieve the document: " + e.getMessage()));
    }

    private static void unregisterUploadBroadcastService(Context context) {

        LocalBroadcastManager.getInstance(context).unregisterReceiver(profileUploadBroadcastReceiver);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("SERVICE BINDING::::", "DONE");
        super.onBind(intent);

        return null;
    }
}
