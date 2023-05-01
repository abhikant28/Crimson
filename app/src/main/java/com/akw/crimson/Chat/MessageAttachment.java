package com.akw.crimson.Chat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.akw.crimson.Backend.Adapters.ChatAttachment_MediaListAdapter;
import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Communications.Communicator;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.SharedPrefManager;
import com.akw.crimson.Backend.Database.TheViewModel;
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.R;
import com.akw.crimson.Utilities.SelectContact;
import com.akw.crimson.databinding.ActivityMessageAttachmentBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class MessageAttachment extends AppCompatActivity {

    ActivityMessageAttachmentBinding viewBinding;

    public static ArrayList<String> mediaUris = new ArrayList<>(), userIDs = new ArrayList<>();
    public static HashMap<Integer, String> msgText = new HashMap<>();
    public static ArrayList<Integer> requestCodes = new ArrayList<>();
    ArrayList<User> users = new ArrayList<>();
    ChatAttachment_MediaListAdapter mediaAdapter = new ChatAttachment_MediaListAdapter(null, null, null, this);
    TheViewModel dbViewModel;
    boolean viewOnce = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i("CODE::::", requestCode + "");

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            if (requestCode == Constants.KEY_INTENT_REQUEST_CODE_DOCUMENT || requestCode == Constants.KEY_INTENT_REQUEST_CODE_MEDIA || requestCode == Constants.KEY_INTENT_REQUEST_CODE_CAMERA || requestCode == Constants.KEY_INTENT_REQUEST_CODE_AUDIO || requestCode == Constants.KEY_INTENT_REQUEST_CODE_CANVAS) {
                if (ContextCompat.checkSelfPermission(
                        this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            99);
                    return;

                }
            }
            Log.i("URIs:_::::", data.getData().getPath());

            if (data.getClipData() != null) { // Multiple images selected
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    mediaUris.add(data.getClipData().getItemAt(i).getUri().toString());
                    requestCodes.add(requestCode);
                    msgText.put(i, "");
                }
                Log.i("URIs:if:::::_:", mediaUris.toString());
            } else if (data.getData() != null) { // Single image selected
                mediaUris.add(data.getData().toString());
                requestCodes.add(requestCode);
                Log.i("URIs:else:::::_:", mediaUris.toString());
            }

            if (requestCode == Constants.KEY_INTENT_REQUEST_CODE_CAMERA) {
                File file = UsefulFunctions.makeOutputMediaFile(this, true, Constants.KEY_MESSAGE_MEDIA_TYPE_IMAGE);
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                UsefulFunctions.saveImage(imageBitmap, true, file);
                mediaUris.add(file.getPath());
            }

        } else if (data != null && requestCode == Constants.KEY_ACTIVITY_RESULT_CONTACT_SELECT) {
            String listGson = data.getStringExtra(Constants.KEY_INTENT_USER_LIST);
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<User>>() {
            }.getType();
            users = gson.fromJson(listGson, type);
            Log.i("USERSSSS::::::", users.toString());
        }
        Log.i("URIs::::::_:", mediaUris.toString());
        Log.i("CODEs::::::_:", requestCodes.toString());
        updateViews();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityMessageAttachmentBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        new SharedPrefManager(this);

        dbViewModel = Communicator.localDB;
        requestCodes = getIntent().getIntegerArrayListExtra(Constants.KEY_INTENT_REQUEST_CODE);
        mediaUris = getIntent().getStringArrayListExtra(Constants.KEY_INTENT_URI);
        userIDs = getIntent().getStringArrayListExtra(Constants.KEY_INTENT_USERID);

        users = getUsers();
        setViews();
        Log.i("codes::::::_:", requestCodes.toString());
    }

    public ArrayList<User> getUsers() {
        ArrayList<User> list = new ArrayList<>();
        for (String s : userIDs) {
            list.add(dbViewModel.getUser(s));
        }
        return list;
    }

    private void setViews() {

        viewBinding.messageAttachmentIvImage.setImageURI(Uri.parse(mediaUris.get(0)));
        Log.i("ATTACH URI::::::", mediaUris.get(0) + " _ " + Uri.parse(mediaUris.get(0)));

        viewBinding.messageAttachmentBtnAddMoreMedia.setOnClickListener(view -> {
            if (!viewBinding.messageAttachmentEtMsgText.getText().toString().isEmpty()) {
                msgText.put(mediaAdapter.prevPos, viewBinding.messageAttachmentEtMsgText.getText().toString());
            }
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/* video/*");
            if (intent.resolveActivity(getPackageManager()) != null) {
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.KEY_INTENT_REQUEST_CODE_MEDIA);
            }
        });

        viewBinding.messageAttachmentBtnAddText.setOnClickListener(view -> {

        });
        viewBinding.messageAttachmentBtnAddArt.setOnClickListener(view -> {

        });
        viewBinding.messageAttachmentBtnRemove.setOnClickListener(view -> {

        });
        viewBinding.messageAttachmentBtnClose.setOnClickListener(view -> finish());
        viewBinding.messageAttachmentBtnSend.setOnClickListener(view -> {
            if (users.size() == 0) {
                selectUsers();
            } else {
                sendMedia();
            }
        });
        viewBinding.messageAttachmentBtnViewOnce.setOnClickListener(view -> {
            viewOnce = !viewOnce;
            if (viewOnce) {
                Toast.makeText(this, "Media set to view once", Toast.LENGTH_SHORT).show();
                viewBinding.messageAttachmentBtnViewOnce.setBackground(getDrawable(R.drawable.ic_baseline_auto_delete_24));
            } else {
//                Toast.makeText(this, "Media set to view once", Toast.LENGTH_SHORT).show();
                viewBinding.messageAttachmentBtnViewOnce.setBackground(getDrawable(R.drawable.ic_outline_auto_delete_24));
            }
        });
        viewBinding.messageAttachmentBtnEmoji.setOnClickListener(view -> {

        });
        viewBinding.messageAttachmentBtnDraw.setOnClickListener(view -> {

        });

        viewBinding.messageAttachmentLlUsers.setOnClickListener(view -> {
            selectUsers();
        });

        updateViews();
    }

    private void selectUsers() {
        Intent intent = new Intent(this, SelectContact.class);
        Gson gson = new Gson();
        String json = gson.toJson(users);
        intent.putExtra(Constants.KEY_INTENT_USER_LIST, json);
        intent.putExtra(Constants.KEY_INTENT_TYPE, Constants.KEY_INTENT_TYPE_MULTI_SELECT);
        startActivityForResult(intent, Constants.KEY_ACTIVITY_RESULT_CONTACT_SELECT);
    }

    private void updateViews() {
        Log.i("MEDAIs:::::", mediaUris.toString());
        if (mediaUris.size() > 1) {
            mediaAdapter = new ChatAttachment_MediaListAdapter(viewBinding.messageAttachmentIvImage, viewBinding.messageAttachmentVvVideo, viewBinding.messageAttachmentEtMsgText, this);
            viewBinding.messageAttachmentRvMedia.setVisibility(View.VISIBLE);
            viewBinding.messageAttachmentRvMedia.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            viewBinding.messageAttachmentRvMedia.setAdapter(mediaAdapter);
        }

        viewBinding.messageAttachmentLlUsers.removeAllViews();
        for (User u : users) {
            viewBinding.messageAttachmentLlUsers.addView(userBox(u.getDisplayName()), 0);
        }


    }

    private TextView userBox(String userName) {
        ConstraintLayout.LayoutParams lParams = new ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lParams.setMargins(0, 0, 0, 0);
        TextView tv = new TextView(this.getApplicationContext());
        tv.setLayoutParams(lParams);
        tv.setText(userName);
        tv.setTextSize(15);
        tv.setTextColor(Color.WHITE);
        tv.setPadding(25, 5, 25, 5);
        tv.setBackgroundResource(R.drawable.round_box_chat_input);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        return tv;
    }

    public void sendMedia() {
        Message[] messages = new Message[requestCodes.size()];
        for (int i = 0; i < requestCodes.size(); i++) {
            int requestCode = requestCodes.get(i);
            Uri mediaUri = Uri.parse(mediaUris.get(i));
            if (requestCode == Constants.KEY_INTENT_REQUEST_CODE_MEDIA) {
                ContentResolver cR = this.getContentResolver();
                File file;
                if (cR.getType(mediaUri).startsWith("video/")) {
                    file = UsefulFunctions.makeOutputMediaFile(this, true, Constants.KEY_MESSAGE_MEDIA_TYPE_VIDEO);
                    UsefulFunctions.saveFile(this, mediaUri, file);

                    messages[i] = new Message(null, msgText.get(i), file.getName(), (file.length() / (1024)), true, false, true, -1, Constants.KEY_MESSAGE_MEDIA_TYPE_VIDEO);

                } else {
                    file = UsefulFunctions.makeOutputMediaFile(this, true, Constants.KEY_MESSAGE_MEDIA_TYPE_IMAGE);
                    Bitmap bitmap = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        bitmap = UsefulFunctions.resizeAndCompressImage(this, mediaUri);
                    }
                    UsefulFunctions.saveImage(bitmap, true, file);

                    messages[i] = new Message(null
                            , msgText.get(i), file.getName(), (file.length() / (1024)), true, false, true, -1, Constants.KEY_MESSAGE_MEDIA_TYPE_IMAGE);
                }


            } else if (requestCode == Constants.KEY_INTENT_REQUEST_CODE_AUDIO) {
                File file = UsefulFunctions.makeOutputMediaFile(this, true, Constants.KEY_MESSAGE_MEDIA_TYPE_AUDIO);
                UsefulFunctions.saveFile(this, mediaUri, file);
                messages[i] = new Message(null, msgText.get(i), file.getName(), (file.length() / (1024)), true, false, true, -1, Constants.KEY_MESSAGE_MEDIA_TYPE_AUDIO);

            }
        }
        String selfId = SharedPrefManager.getLocalUserID();
        for (User user : users) {
            for (Message msg : messages) {
                msg.setMsg_ID(selfId + Calendar.getInstance().getTime().getTime());
                msg.setUser_id(user.getUser_id());
                if (msg.getMediaType() == Constants.KEY_MESSAGE_MEDIA_TYPE_DOCUMENT) {
                    user.addDoc(msg.getMediaID());
                } else {
                    user.addMedia(msg.getMediaID());
                }
                user.setLast_msg(null);
                user.setLast_msg_type(msg.getMediaType());
                dbViewModel.insertMessage(msg);
            }
            dbViewModel.updateUser(user);
        }
        finish();
    }

}