package com.akw.crimson.Backend.Communications;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.TheViewModel;
import com.akw.crimson.Backend.UsefulFunctions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class DownloadFileService extends IntentService {
    private StorageReference storageRef;
    private TheViewModel db;
    public static final int RESULT_SUCCESS = 1;
    public static final int RESULT_FAIL = 0;
    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_RECEIVER = "extra_receiver";

    public DownloadFileService() {
        super("DownloadFileService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        db = Communicator.localDB;
        FirebaseStorage fireStore = FirebaseStorage.getInstance();
        storageRef = fireStore.getReference();
        ResultReceiver receiver = intent.getParcelableExtra(EXTRA_RECEIVER);
        Bundle resultData = new Bundle();

        String id = intent.getStringExtra(Constants.KEY_INTENT_MESSAGE_ID);
        Message msg = db.getMessage(id);
        User user = db.getUser(msg.getUser_id());
        Log.i("DOWNLOAD::::", "Started");

        String folder = "";
        switch (msg.getMediaType()) {
            case Constants.KEY_MESSAGE_MEDIA_TYPE_IMAGE:
                folder = "images";
                break;
            case Constants.KEY_MESSAGE_MEDIA_TYPE_VIDEO:
                folder = "videos";
                break;
            case Constants.KEY_MESSAGE_MEDIA_TYPE_DOCUMENT:
                folder = "documents";
                break;
            case Constants.KEY_MESSAGE_MEDIA_TYPE_AUDIO:
                folder = "audios";
                break;
            case Constants.KEY_MESSAGE_MEDIA_TYPE_PROFILE:
                folder= "profile";
                break;
            case Constants.KEY_MESSAGE_MEDIA_TYPE_STATUS:
                folder="status";
                break;
        }
        File outFile;
        final StorageReference fileRef = storageRef.child(folder + "/" + msg.getUser_id() + "_" + msg.getMediaID());
        if (msg.getMediaType() == Constants.KEY_MESSAGE_MEDIA_TYPE_DOCUMENT) {
            String docName = msg.getMediaID().substring(Math.min(msg.getMediaID().length() - 1, msg.getMediaID().indexOf('_') + 1));
            outFile = UsefulFunctions.makeOutputMediaFile(getApplicationContext(), msg.isSelf(), msg.getMediaType(), docName);
            assert outFile != null;
            user.addDoc(outFile.getName());
        } else {
            outFile = UsefulFunctions.makeOutputMediaFile(getApplicationContext(), msg.isSelf(), msg.getMediaType());
            user.addMedia(outFile.getName());
        }

        fileRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
            Log.i("DOWNLOAD::::", "Success");
            String name = null;
            try {
                name = UsefulFunctions.saveFile(bytes, outFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (name != null) {
                msg.setMediaID(name);
                db.updateMessage(msg);
                db.updateUser(user);
                fileRef.delete();
                user.incMediaSize(outFile.length());
                resultData.putInt("result", RESULT_SUCCESS);
                receiver.send(RESULT_SUCCESS, resultData);
            } else {
                resultData.putInt("result", RESULT_FAIL);
                receiver.send(RESULT_FAIL, resultData);
                outFile.delete();
            }
            fileRef.delete();
            stopSelf();
        }).addOnFailureListener(e -> {
            Log.i("DOWNLOAD::::", msg.getUser_id() + "_" + msg.getMediaID() + "FAILED- " + e);
            // Send the result
            resultData.putInt("result", RESULT_FAIL);
            receiver.send(RESULT_FAIL, resultData);
            stopSelf();
        });
    }
}