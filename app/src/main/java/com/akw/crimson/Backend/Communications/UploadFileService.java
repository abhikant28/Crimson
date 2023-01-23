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
    private Uri fileUri;
    public static final int RESULT_SUCCESS = 1;
    public static final int RESULT_FAIL = 0;
    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_RECEIVER = "extra_receiver";


    public UploadFileService() {
        super("UploadFileService");
    }


//    public UploadFileService(Context context, String msgId, ProgressBar pb, TextView tv, CardView cv) {
//        super("UploadFileService");
//        this.db = Communicator.localDB;
//        this.msg = db.getMessage(msgId);
//        this.cxt = context;
//        this.pb = pb;
//        this.outFile = UsefulFunctions.getOutputMediaFile(cxt, msg.isSelf(), msg.getMediaType());
//        this.tv = tv;
//        this.cv = cv;
//    }



    private byte[] readFileToByteArray(File file) {
        byte[] fileData = new byte[(int) file.length()];
        DataInputStream dis = null;
        try {
            dis = new DataInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            assert dis != null;
            dis.readFully(fileData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileData;
    }


//    private String getFileExtension(Uri uri) {
//        ContentResolver cR = getContentResolver();
//        MimeTypeMap mime = MimeTypeMap.getSingleton();
//        return mime.getExtensionFromMimeType(cR.getType(uri));
//    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        storageRef = FirebaseStorage.getInstance().getReference();
        db=Communicator.localDB;
        assert intent != null;

        ResultReceiver receiver = intent.getParcelableExtra(EXTRA_RECEIVER);
        Bundle resultData = new Bundle();
        String id = intent.getStringExtra(Constants.KEY_INTENT_MESSAGE_ID);

        Message msg= db.getMessage(id);
        Log.i("UPLOAD::::","onHandleIntent");


            String fileName = msg.getMediaID();
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
            }).addOnCanceledListener(new OnCanceledListener() {
                @Override
                public void onCanceled() {
                    Log.e("FileUploadService::::::", "Cancelled to upload file.");

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    Log.i("PROGRESSS:::::::::", snapshot.getBytesTransferred()+"");
                }
            });

//            Bitmap bitmap = null;
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            byte[] data;
//            if (msg.getMediaType() == Constants.KEY_MESSAGE_MEDIA_TYPE_IMAGE) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    bitmap = UsefulFunctions.resizeAndCompressImage(this, fileUri);
//                }
//                assert bitmap != null;
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//                data = baos.toByteArray();
//
//            } else {
//                data = readFileToByteArray(new File(String.valueOf(fileUri)));
//            }
//
//            File outFile;
//            if (msg.getMediaType()==Constants.KEY_MESSAGE_MEDIA_TYPE_DOCUMENT){
//                String docName = msg.getMediaID().substring(Math.min(msg.getMediaID().length() - 1, msg.getMediaID().indexOf('_') + 1));
//
//                outFile = UsefulFunctions.getOutputMediaFile(getApplicationContext(), msg.isSelf(), msg.getMediaType(), docName);
//            }else{
//                outFile=UsefulFunctions.getOutputMediaFile(getApplicationContext(), msg.isSelf(), msg.getMediaType());
//            }
//            Bitmap finalBitmap = bitmap;
//            fileRef.putBytes(data)
//                    .addOnSuccessListener(taskSnapshot -> {
//                        if (msg.getMediaType() == Constants.KEY_MESSAGE_MEDIA_TYPE_IMAGE && finalBitmap != null) {
//                            String name = UsefulFunctions.saveImage(getApplicationContext(), finalBitmap, true, outFile);
//                            if (name == null) {
//                                //permission Missing
//                                return;
//                            } else {
//                                msg.setMediaID(name);
//                                msg.setStatus(0);
//                            }
//                            db.updateMessage(msg);
//                        } else if (msg.getMediaType() == Constants.KEY_MESSAGE_MEDIA_TYPE_VIDEO) {
////                                        UsefulFunctions.saveVideo();
//                        } else {
////                                        UsefulFunctions.saveFile();
//                        }
//                        Log.d("FileUploadService", "File successfully uploaded: ");
//                        stopSelf();
//                    })
//                    .addOnFailureListener(e -> {
//                        resultData.putInt("result", RESULT_FAIL);
//                        receiver.send(RESULT_FAIL, resultData);
//                        Log.e("FileUploadService", "Failed to upload file.", e);
//                        stopSelf();
//                    });
        }


}