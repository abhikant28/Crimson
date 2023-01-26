package com.akw.crimson.Backend.Communications;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.SharedPrefManager;
import com.akw.crimson.Backend.Database.TheViewModel;
import com.akw.crimson.Backend.UsefulFunctions;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class UploadFileService extends IntentService {
    private StorageReference storageRef;
    private TheViewModel db;
    public static final int RESULT_SUCCESS = 1;
    public static final int RESULT_FAIL = 0;
    public static final String EXTRA_RECEIVER = "extra_receiver";


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
        db=Communicator.localDB;
        assert intent != null;

        ResultReceiver receiver = intent.getParcelableExtra(EXTRA_RECEIVER);
        Bundle resultData = new Bundle();
        String id = intent.getStringExtra(Constants.KEY_INTENT_MESSAGE_ID);

        Message msg= db.getMessage(id);
        Log.i("UPLOAD::::","onHandleIntent");


            String fileName = SharedPrefManager.getLocalUserID() +"_"+msg.getMediaID();
            Log.i("UPLOAD MSG ID:::::", fileName);

            String folder="";
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
            }
            StorageReference fileRef = storageRef.child(folder+"/" + fileName);

            File file= UsefulFunctions.getFile(this,msg.getMediaID()
                    , msg.getMediaType(), msg.isSelf());

            Log.i("MEDIA TYE Upload:::::", msg.getMediaType()+"");
            Uri uri = Uri.fromFile(file);
            fileRef.putFile(uri).addOnSuccessListener(taskSnapshot -> {
                Log.d("FileUploadService:::::::", "File successfully uploaded: ");
                msg.setStatus(0);
                db.updateMessage(msg);
                resultData.putInt("result", RESULT_SUCCESS);
                receiver.send(RESULT_SUCCESS, resultData);
                stopSelf();
            }).addOnFailureListener(e -> {
                Log.e("FileUploadService:::::::::", "Failed to upload file.", e);
                resultData.putInt("result", RESULT_FAIL);
                receiver.send(RESULT_FAIL, resultData);
                stopSelf();
            }).addOnCanceledListener(() -> Log.e("FileUploadService::::::", "Cancelled to upload file."))
                    .addOnProgressListener(snapshot -> Log.i("PROGRESSS:::::::::", snapshot.getBytesTransferred()+""));
        }


}