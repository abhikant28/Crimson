package com.akw.crimson.Backend.Communications;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.annotation.NonNull;

import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.SharedPrefManager;
import com.akw.crimson.Backend.Database.TheViewModel;
import com.akw.crimson.Backend.UsefulFunctions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class DownloadFileService extends IntentService {

    public static final int RESULT_SUCCESS = 1;
    public static final int RESULT_FAIL = 0;
    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_RECEIVER = "extra_receiver";
    private StorageReference storageRef;
    private TheViewModel db;
    FirebaseFirestore fireDB = FirebaseFirestore.getInstance();
    DocumentReference mediaDocRef;


    public DownloadFileService() {
        super("DownloadFileService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        db = Communicator.localDB;
        String id = intent.getStringExtra(Constants.Intent.KEY_INTENT_MESSAGE_ID);
        Message msg = db.getMessage(id);

        Communicator.downloading.add(msg.getMsg_ID());

        FirebaseStorage fireStore = FirebaseStorage.getInstance();
        storageRef = fireStore.getReference();

        ResultReceiver receiver = intent.getParcelableExtra(EXTRA_RECEIVER);
        Bundle resultData = new Bundle();

        User user = db.getUser(msg.getUser_id());
        Log.i("DownloadFileService.DOWNLOAD::::", "Started");

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
        File outFile;
        if (msg.getMediaType() == Constants.Media.KEY_MESSAGE_MEDIA_TYPE_DOCUMENT) {
            String docName = msg.getMediaID().substring(Math.min(msg.getMediaID().length() - 1, msg.getMediaID().indexOf('_') + 1));
            outFile = UsefulFunctions.FileUtil.makeOutputMediaFile(getApplicationContext(), msg.isSelf(), msg.getMediaType(), docName);
            assert outFile != null;
            user.addDoc(outFile.getName());
        } else {
            outFile = UsefulFunctions.FileUtil.makeOutputMediaFile(getApplicationContext(), msg.isSelf(), msg.getMediaType());
            Log.i("DownloadFileService.FILENAME:::::::::", outFile.getName());
            user.addMedia(outFile.getName());
        }

        getReference(outFile, folder, user, msg, receiver, resultData);
    }

    public boolean getReference(File outFile, String folder, User user, Message msg, ResultReceiver receiver, Bundle resultData) {
        String docID = (msg.getGroupUserID()==null?msg.getUser_id():msg.getGroupUserID()) +"_"+ msg.getMediaID().substring(0, msg.getMediaID().lastIndexOf('.'));
        new SharedPrefManager(this);
        String currentUserID = SharedPrefManager.getLocalUserID();
        mediaDocRef = fireDB.collection(Constants.KEY_FCM_ATTACHMENTS_REFERENCE).document(docID);
        Log.i("DownloadFileService.getReference:::::::::", outFile.getName());

        mediaDocRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    Log.i("DownloadFileService.mediaDocRef.addOnSuccessListener:::::::::", " ___________________________");
                    if (documentSnapshot.exists()) {
                        Log.i("DownloadFileService.getReference:::::::::", "Doc FOUND");
                        // Check if 'id' field exists
                        if (documentSnapshot.contains("id")) {
                            String idValue = documentSnapshot.getString("id");
                            beginDownload(idValue, outFile, folder, user, msg, receiver, resultData);
                            Log.i("Document ID:", idValue);
                        }

                        // Check if only one field is present
                        // Remove the field with the current user's ID
                        mediaDocRef.update(FieldPath.of(currentUserID), FieldValue.delete())
                                .addOnSuccessListener(aVoid -> {
                                    Log.i("DownloadFileService.getReference:::::::::", "REMOVED SUCCESSFULLY");
                                    // Field removed successfully
                                })
                                .addOnFailureListener(e -> {
                                    Log.i("DownloadFileService.getReference:::::::::", "FAILED to REMOVE>"+e.getMessage());
                                    // Failed to remove the field
                                });

                    } else {
                        Log.i("DownloadFileService.getReference:::::::::", "Doc does not exist::"+docID);

                        // Document does not exist
                    }
                })
                .addOnFailureListener(e -> {
                    // Failed to retrieve the document
                    Log.i("DownloadFileService.getReference:::::::::", "FAILED>"+e.getMessage());
                });
        return true;

    }


    public void beginDownload(String mediaID, File outFile, String folder, User user, Message msg, ResultReceiver receiver, Bundle resultData) {
        final StorageReference fileRef = storageRef.child(folder + "/" + mediaID);
        fileRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
            Log.i("DownloadFileService.DOWNLOAD::::", "Success");
            String name = null;
            try {
                name = UsefulFunctions.FileUtil.saveFile(bytes, outFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (name != null) {
                msg.setMediaID(name);
                db.updateMessage(msg);
                user.incMediaSize(outFile.length());
                db.updateUser(user);
                resultData.putInt("result", RESULT_SUCCESS);
                receiver.send(RESULT_SUCCESS, resultData);
            } else {
                resultData.putInt("result", RESULT_FAIL);
                receiver.send(RESULT_FAIL, resultData);
                outFile.delete();
            }
            checkForLast(fileRef);
            Communicator.downloading.remove(msg.getMsg_ID());
            stopSelf();
        }).addOnFailureListener(e -> {
            Log.i("DownloadFileService.DOWNLOAD FAILED::::", msg.getUser_id() + "_" + msg.getMediaID() + "_FAILED_" + e);
            // Send the result
            resultData.putInt("result", RESULT_FAIL);
            receiver.send(RESULT_FAIL, resultData);
            Communicator.downloading.remove(msg.getMsg_ID());
            stopSelf();
        });
    }

    private void checkForLast(StorageReference fileRef) {
        Log.i("DownloadFileService.checkForLast:::::::::", "CHECKING");

        mediaDocRef.get()
                .addOnSuccessListener(documentSnapshot -> {

                    // Check if only one field is present
                    if (documentSnapshot.getData() != null && documentSnapshot.getData().size() == 1) {
                        // Delete the whole document
                        mediaDocRef.delete();
                        fileRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.i("DownloadFileService.checkForLast:::::::::", "Doc DELETED");


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i("DownloadFileService.checkForLast:::::::::", "Doc NOT DELETED");

                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    // Failed to retrieve the document
                });

    }
}