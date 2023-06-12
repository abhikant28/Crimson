package com.akw.crimson.Backend.Communications;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.SharedPrefManager;
import com.akw.crimson.Backend.Database.TheViewModel;
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.Preferences.SettingsActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadFileService extends IntentService {
    public static final int RESULT_SUCCESS = 1;
    public static final int RESULT_FAIL = 0;
    public static final String EXTRA_RECEIVER = "extra_receiver";
    private StorageReference storageRef;
    private TheViewModel db;
    LifecycleOwner lifecycleOwner;
    FirebaseFirestore fireDB = FirebaseFirestore.getInstance();
    DocumentReference mediaDocRef;

    public UploadFileService() {
        super("UploadFileService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        new SharedPrefManager(getApplicationContext());
        storageRef = FirebaseStorage.getInstance().getReference();
        db = Communicator.localDB;
        assert intent != null;
        Log.i(this.getClass().getSimpleName() + ".UploadFileService.UPLOAD::::", "onHandleIntent");

        int uploadType = intent.getIntExtra(Constants.Intent.KEY_INTENT_UPLOAD_TYPE, Constants.Intent.KEY_INTENT_UPLOAD_TYPE_MEDIA);


        if(uploadType==Constants.Intent.KEY_INTENT_UPLOAD_TYPE_PROFILE) {
            profilePicUpload(intent.getStringExtra(Constants.Intent.KEY_INTENT_FILE_PATH));
        }else{
            String id = intent.getStringExtra(Constants.Intent.KEY_INTENT_MESSAGE_ID);
            Message msg = db.getMessage(id);

            Communicator.uploading.add(msg.getMsg_ID());

            checkForDoc(msg);
        }

    }

    public void profilePicUpload(String fileName) {

        DocumentReference documentRef = fireDB.collection(Constants.KEY_FCM_ATTACHMENTS_REFERENCE).document();

        List<User> users = db.getConnectedUsers().getValue();


        if (users.size() == 0)
            return;

        documentRef.get()
                .addOnSuccessListener(documentSnapshot -> {

                    Log.i(this.getClass().getSimpleName() + ".profilePicUpload.addOnSuccessListener::::", "NOT EXISTS");

                    //Upload Profile
                    StorageReference fileRef = storageRef.child("profile" + "/" + documentRef.getId() + ".jpg");

                    File file = UsefulFunctions.FileUtil.getFile(getApplicationContext(), fileName
                            , Constants.Media.KEY_MESSAGE_MEDIA_TYPE_PROFILE, true);

                    Log.i("UploadFileService.MEDIA TYPE Upload:::::", Constants.Media.KEY_MESSAGE_MEDIA_TYPE_PROFILE + "");
                    Uri uri = Uri.fromFile(file);

                    fileRef.putFile(uri).addOnSuccessListener(taskSnapshot -> {
                        Log.i("UploadFileService.FileUploadService:::::::", "Profile Pic successfully uploaded: " + fileRef.getName());
                        stopSelf();
                    }).addOnFailureListener(e -> {
                        Log.e("UploadFileService.FileUploadService:::::::::", "Failed to upload file.", e);
                        stopSelf();
                    }).addOnCanceledListener(() -> {
                        Log.e("UploadFileService.FileUploadService::::::", "Cancelled to upload file.");
                        stopSelf();
                    });


                    // Document with mediaID does not exist
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", fileRef.getName());

                    for (User u : users) {
                        data.put(u.getUser_id(), "");
                    }

                    documentRef.set(data)
                            .addOnSuccessListener(aVoid -> {
                                // Document created successfully
                                Intent resultIntent = new Intent("profilePicUpdate");
                                resultIntent.putExtra(Constants.Intent.KEY_INTENT_URI, documentRef.getId());
                                resultIntent.putExtra(Constants.Intent.KEY_INTENT_RESULT_CODE, RESULT_SUCCESS);
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(resultIntent);

                            })
                            .addOnFailureListener(e -> {
                                // Failed to create document
                            });

                })
                .addOnFailureListener(e -> {
                    Log.i(this.getClass().getSimpleName() + ".profilePicUpload.addOnFailureListener::::", documentRef.getPath());
                    // Failed to get the document
                });

    }

    public void checkForDoc(Message msg){

        String mediaID = SharedPrefManager.getLocalUserID() + msg.getMediaID().replaceAll("_", "").replaceAll("IM", "")
                .replaceAll("VI", "");
        msg.setMediaUrl(SharedPrefManager.getLocalUserID() + msg.getMediaID().substring(0, msg.getMediaID().lastIndexOf(".")));
        ArrayList<String> userID = null;
        userID = (msg.getGroupUserID() == null) ? new ArrayList<>(Arrays.asList(msg.getUser_id())) : db.getUser(msg.getUser_id()).getGroup().getUsers();

        Log.i(this.getClass().getSimpleName() + ".checkDoc::::", msg.getMediaUrl() + "_" + userID);

        Log.i(this.getClass().getSimpleName() + ".UPLOAD MEDIA ID:::::", mediaID);
        DocumentReference documentRef = fireDB.collection(Constants.KEY_FCM_ATTACHMENTS_REFERENCE).document(msg.getMediaUrl());

        ArrayList<String> finalUserID = userID;

        documentRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Document with mediaID exists
                        // Add empty field named userID
                        for (String id : finalUserID) {
                            documentRef.update(id, "");
                        }
                        Log.i(this.getClass().getSimpleName() + ".documentRef.addOnSuccessListener::::", "EXISTS");

                        msg.setStatus(Constants.Message.MESSAGE_STATUS_PENDING_UPLOAD);
                        db.updateMessage(msg);
                    } else {
                        Log.i(this.getClass().getSimpleName() + ".documentRef.addOnSuccessListener::::", "NOT EXISTS");
                        uploadFile(msg, mediaID);
                        // Document with mediaID does not exist
                        Map<String, Object> data = new HashMap<>();
                        data.put("id", mediaID);

                        for (String id : finalUserID) {
                            data.put(id, "");
                        }

                        documentRef.set(data)
                                .addOnSuccessListener(aVoid -> {
                                    // Document created successfully
                                })
                                .addOnFailureListener(e -> {
                                    // Failed to create document
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.i(this.getClass().getSimpleName() + ".documentRef.addOnFailureListener::::", documentRef.getPath());
                    // Failed to get the document
                });


    }

    private void uploadFile(Message msg, String fileName) {
        String folder = "";
        switch (msg.getMediaType()) {
            case Constants.Media.KEY_MESSAGE_MEDIA_TYPE_IMAGE:
                folder = "images";
                break;
            case Constants.Media.KEY_MESSAGE_MEDIA_TYPE_VIDEO:
                folder = "videos";
                break;
            case Constants.Media.KEY_MESSAGE_MEDIA_TYPE_DOCUMENT:
                folder = "documents";
                break;
            case Constants.Media.KEY_MESSAGE_MEDIA_TYPE_AUDIO:
                folder = "audios";
                break;
            case Constants.Media.KEY_MESSAGE_MEDIA_TYPE_STATUS:
                folder = "status";
                break;
//            case Constants.Media.KEY_MESSAGE_MEDIA_TYPE_PROFILE:
//                folder = "profile";
//                break;
        }
        StorageReference fileRef = storageRef.child(folder + "/" + fileName);

        File file = UsefulFunctions.FileUtil.getFile(this, msg.getMediaID()
                , msg.getMediaType(), msg.isSelf());

        Log.i("UploadFileService.MEDIA TYPE Upload:::::", msg.getMediaType() + "");
        Uri uri = Uri.fromFile(file);

        fileRef.putFile(uri).addOnSuccessListener(taskSnapshot -> {
                    Log.d("UploadFileService.FileUploadService:::::::", "File successfully uploaded: ");
                    msg.setStatus(Constants.Message.MESSAGE_STATUS_PENDING_UPLOAD);
                    db.updateMessage(msg);
                    Intent resultIntent = new Intent(msg.getMsg_ID());
                    resultIntent.putExtra(Constants.Intent.KEY_INTENT_RESULT_CODE, RESULT_SUCCESS);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(resultIntent);

                    Communicator.uploading.remove(msg.getMsg_ID());
                    stopSelf();
                }).addOnFailureListener(e -> {
                    Log.e("UploadFileService.FileUploadService:::::::::", "Failed to upload file.", e);
                    Communicator.uploading.remove(msg.getMsg_ID());
                    stopSelf();
                }).addOnCanceledListener(() -> {
                    Log.e("UploadFileService.FileUploadService::::::", "Cancelled to upload file.");
                    Intent resultIntent = new Intent("profilePicUpdate");
                    resultIntent.putExtra(Constants.Intent.KEY_INTENT_RESULT_CODE, RESULT_FAIL);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(resultIntent);
                    Communicator.uploading.remove(msg.getMsg_ID());
                    stopSelf();
                })
                .addOnProgressListener(snapshot ->{
//                            Intent resultIntent = new Intent(msg.getMsg_ID());
//                            resultIntent.putExtra(Constants.Intent.KEY_INTENT_RESULT_CODE, RESULT_SUCCESS);
//                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(resultIntent);
                    Log.i("UploadFileService.PROGRESS:::::::::", snapshot.getBytesTransferred() + "");

                }

                );

    }


}