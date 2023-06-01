package com.akw.crimson.Backend.Communications;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.annotation.Nullable;

import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.SharedPrefManager;
import com.akw.crimson.Backend.Database.TheViewModel;
import com.akw.crimson.Backend.UsefulFunctions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class UploadFileService extends IntentService {
    public static final int RESULT_SUCCESS = 1;
    public static final int RESULT_FAIL = 0;
    public static final String EXTRA_RECEIVER = "extra_receiver";
    private StorageReference storageRef;
    private TheViewModel db;

    FirebaseFirestore fireDB = FirebaseFirestore.getInstance();
    DocumentReference mediaDocRef;

    public UploadFileService() {
        super("UploadFileService");
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

        ResultReceiver receiver = intent.getParcelableExtra(EXTRA_RECEIVER);
        Bundle resultData = new Bundle();
        String id = intent.getStringExtra(Constants.Intent.KEY_INTENT_MESSAGE_ID);
        Message msg = db.getMessage(id);
        Communicator.uploading.add(msg.getMsg_ID());

        Messenger messenger = intent.getParcelableExtra(Constants.Intent.KEY_INTENT_MESSENGER);


        Log.i(this.getClass().getSimpleName() + ".UploadFileService.UPLOAD::::", "onHandleIntent");

        checkDoc(msg, receiver, resultData);

    }

    public void checkDoc(Message msg, ResultReceiver receiver, Bundle resultData) {
        String mediaID = SharedPrefManager.getLocalUserID() + "_" + msg.getMediaID();
        String docID = SharedPrefManager.getLocalUserID() + "_" + msg.getMediaID().substring(0, msg.getMediaID().lastIndexOf("."));
        ArrayList<String> userID = (msg.getGroupUserID()==null)?new ArrayList<>(Arrays.asList(msg.getUser_id())):db.getUser(msg.getUser_id()).getGroup().getUsers();
        Log.i(this.getClass().getSimpleName() + ".checkDoc::::", docID + "_" + userID);

        Log.i(this.getClass().getSimpleName() + ".UPLOAD MEDIA ID:::::", mediaID);
        DocumentReference documentRef = fireDB.collection(Constants.KEY_FCM_ATTACHMENTS_REFERENCE).document(docID);

        documentRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Document with mediaID exists
                        // Add empty field named userID
                        for(String id: userID){
                            documentRef.update(id, "");
                        }
                        Log.i(this.getClass().getSimpleName() + ".documentRef.addOnSuccessListener::::", "EXISTS");

                        msg.setStatus(Constants.Message.MESSAGE_STATUS_PENDING_UPLOAD);
                        db.updateMessage(msg);
                    } else {
                        Log.i(this.getClass().getSimpleName() + ".documentRef.addOnSuccessListener::::", "NOT EXISTS");
                        uploadFile(msg, mediaID, receiver, resultData);
                        // Document with mediaID does not exist
                        Map<String, Object> data = new HashMap<>();
                        data.put("id", mediaID);

                        for(String id: userID){
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

    private void uploadFile(Message msg, String fileName, ResultReceiver receiver, Bundle resultData) {
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

                    resultData.putInt("result", RESULT_SUCCESS);
                    receiver.send(RESULT_SUCCESS, resultData);
                    Communicator.uploading.remove(msg.getMsg_ID());
                    stopSelf();
                }).addOnFailureListener(e -> {
                    Log.e("UploadFileService.FileUploadService:::::::::", "Failed to upload file.", e);
                    resultData.putInt("result", RESULT_FAIL);
                    receiver.send(RESULT_FAIL, resultData);
                    Communicator.uploading.remove(msg.getMsg_ID());
                    stopSelf();
                }).addOnCanceledListener(() -> {
                    Log.e("UploadFileService.FileUploadService::::::", "Cancelled to upload file.");
                    Communicator.uploading.remove(msg.getMsg_ID());
                })
                .addOnProgressListener(snapshot ->
                        {
                            Log.i("UploadFileService.PROGRESS:::::::::", snapshot.getBytesTransferred() + "");
                            android.os.Message m = android.os.Message.obtain();
                            m.arg1 = (int) ((snapshot.getBytesTransferred() / file.length()) * 100);
                        }

                );

    }

}