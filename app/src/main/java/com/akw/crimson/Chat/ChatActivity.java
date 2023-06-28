package com.akw.crimson.Chat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Backend.Adapters.ChatView_RecyclerAdapter;
import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Communications.Communicator;
import com.akw.crimson.Backend.Communications.DownloadFileService;
import com.akw.crimson.Backend.Communications.Messaging;
import com.akw.crimson.Backend.Communications.UploadFileService;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.SharedPrefManager;
import com.akw.crimson.Backend.Database.TheViewModel;
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.BaseActivity;
import com.akw.crimson.ImportChat;
import com.akw.crimson.PrepareMessageActivity;
import com.akw.crimson.ProfileView;
import com.akw.crimson.R;
import com.akw.crimson.StarredMessages;
import com.akw.crimson.Utilities.SelectAudio;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ChatActivity extends BaseActivity {
    private static final int REQUEST_CALL_PERMISSION = 243;

    private ChatView_RecyclerAdapter chatViewAdapter;
    private RecyclerView chatRecyclerView;
    private ImageButton ib_send, ib_attach, ib_camera, ib_emoji;
    private EditText et_message;
    LinearLayout ll_full;

    private final FirebaseFirestore fireStoreDB = FirebaseFirestore.getInstance();
    private TheViewModel dbViewModel;
    private Cursor chatCursor;
    public static List<Message> mediaList;
    private Thread chatThread;
    private ActionBar ab;

    public static volatile User user;
//    public static volatile boolean updated = false;
    public static volatile String userID;
    private Boolean isOnline = false;
    GestureDetector sendButtonGestureDetector;

    @Override
    public void onBackPressed() {
        if (findViewById(R.id.attachment_popup_cl_attachmentOptions).getVisibility() == View.VISIBLE) {
            // The view is visible
            findViewById(R.id.attachment_popup_cl_attachmentOptions).setVisibility(View.GONE);
        } else {
            // The view is either invisible or gone
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (user != null) {
            user.setUnread_count(0);
            user.setUnreadUser(false);
        }
        dbViewModel.updateUser(user);
        chatThread.interrupt();
        stopService(new Intent(this, Communicator.class));
    }

    @Override
    public void finish() {
        chatCursor.close();
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(new Intent(this, Communicator.class));
        startService(intent);
        listenForOnline();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.chat_menu, menu);
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null)
            return;
        Message message = null;
        ArrayList<String> uri = new ArrayList<>();
        if (data.getClipData() != null) { // Multiple images selected
            int count = data.getClipData().getItemCount();
            for (int i = 0; i < count; i++) {
                uri.add(data.getClipData().getItemAt(i).getUri().toString());
            }
        }
        Log.i("URIs:_:_:::", data.getData() + "00");
        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.Intent.KEY_INTENT_REQUEST_CODE_DOCUMENT || requestCode == Constants.Intent.KEY_INTENT_REQUEST_CODE_MEDIA || requestCode == Constants.Intent.KEY_INTENT_REQUEST_CODE_CAMERA || requestCode == Constants.Intent.KEY_INTENT_REQUEST_CODE_AUDIO || requestCode == Constants.Intent.KEY_INTENT_REQUEST_CODE_CANVAS) {
                if (ContextCompat.checkSelfPermission(
                        this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            99);
                    return;

                }
            }

            if (data.getClipData() != null) { // Multiple images selected
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    uri.add(data.getClipData().getItemAt(i).getUri().toString());
                }
            } else if (data.getData() != null) { // Single image selected
                uri.add(data.getData().toString());
            } else if (requestCode == Constants.Intent.KEY_INTENT_REQUEST_CODE_AUDIO) {
                uri.add(UsefulFunctions.getAudioContentUri(this, data.getExtras().getString(Constants.Intent.KEY_INTENT_RESULT_AUDIO_PATH)).toString());
            }
            if (requestCode == Constants.Intent.KEY_INTENT_REQUEST_CODE_CAMERA) {
                Log.i("CAMERA URI CODE::::::", "....FOUND....");
                File file = UsefulFunctions.FileUtil.makeOutputMediaFile(this, true, Constants.Media.KEY_MESSAGE_MEDIA_TYPE_IMAGE);
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                UsefulFunctions.FileUtil.saveImage(imageBitmap, true, file);
                Log.i("CAMERA:::::", data.getData() + "");
                uri.add(file.getPath());
            }

            if (requestCode == Constants.Intent.KEY_INTENT_REQUEST_CODE_MEDIA || requestCode == Constants.Intent.KEY_INTENT_REQUEST_CODE_AUDIO) {

                Log.i("MEDIA URI CODE::::::", "....FOUND....");

                Intent intent = new Intent(this, MessageAttachment.class);
                intent.putExtra(Constants.Intent.KEY_INTENT_URI, uri);
                ArrayList<Integer> codes = new ArrayList<>();
                codes.add(requestCode);
                intent.putExtra(Constants.Intent.KEY_INTENT_REQUEST_CODE, codes);
                intent.putExtra(Constants.Intent.KEY_INTENT_USERID, new ArrayList<String>(Collections.singleton(userID)));
                startActivity(intent);

            } else if (requestCode == Constants.Intent.KEY_INTENT_REQUEST_CODE_DOCUMENT) {
                String f = UsefulFunctions.FileUtil.getFileName(this, data.getData(), true);
                Log.i("DOCUMENT URI CODE::::::", "....FOUND...." + f);

                File file = UsefulFunctions.FileUtil.makeOutputMediaFile(this, true, Constants.Media.KEY_MESSAGE_MEDIA_TYPE_DOCUMENT, f);
                UsefulFunctions.FileUtil.saveFile(this, data.getData(), file);
                message = new Message(SharedPrefManager.getLocalUserID() + Calendar.getInstance().getTime().getTime(), userID, null
                        , null, file.getName(), (file.length() / (1024)), true, false, true, Constants.Message.MESSAGE_STATUS_MEDIA_TRANSFER_PENDING , Constants.Media.KEY_MESSAGE_MEDIA_TYPE_DOCUMENT, SharedPrefManager.getLocalUserID());

            }

            if (message != null) {
                if (message.getMediaType() == Constants.Media.KEY_MESSAGE_MEDIA_TYPE_DOCUMENT) {
                    user.addDoc(message.getMediaID());
                } else {
                    user.addMedia(message.getMediaID());
                }
                user.setLast_msg(null);
                user.setLast_msg_media_type(message.getMediaType());
                dbViewModel.updateUser(user);
                dbViewModel.insertMessage(message);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.chat_menu_importChat:
                startActivity(new Intent(this, ImportChat.class));
                break;
            case R.id.chat_menu_search:
                Log.i("SEARCH:::::", "MENU CLICK");
                searchChat();
                break;
            case R.id.chat_menu_prepareMessage:
                startActivity(new Intent(this, PrepareMessageActivity.class));
                break;
            case R.id.chat_menu_profile:
                Intent i = new Intent(this, ProfileView.class);
                i.putExtra(Constants.Intent.KEY_INTENT_USERID, userID);
                startActivity(i);
                break;
            case R.id.chat_menu_starredUserMessages:
                Intent ii = new Intent(this, StarredMessages.class);
                ii.putExtra(Constants.Intent.KEY_INTENT_USERID, userID);
                startActivity(ii);
                break;
            case R.id.chat_menu_call:
                requestCallPermission();
                break;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            } else {
                // Handle the case when permission is denied
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        attachViews();

        new SharedPrefManager(this);
        dbViewModel = Communicator.localDB;
        user = dbViewModel.getUser(getIntent().getStringExtra(Constants.Intent.KEY_INTENT_USERID));
        userID = user.getUser_id();

        setMyActionBar();
        setClicks();

        sendButtonGestureDetector = new GestureDetector(ChatActivity.this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                // Handle double-click event here
                Log.i("ChatActivity.GestureDetect:::::::", "Double Tap!!!!");
                fireStoreDB.collection(Constants.KEY_FIRESTORE_USERS).document(userID).get().addOnSuccessListener(documentSnapshot -> {
                    String userToken = documentSnapshot.getString(Constants.KEY_FIRESTORE_USER_TOKEN);
                    Messaging.sendPingMessageNotification(userToken, SharedPrefManager.getLocalUser().getName());
                });

                return super.onDoubleTap(e);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {


                if (!et_message.getText().toString().trim().equals("")) {
                    Message message = new Message(userID, null, et_message.getText().toString().trim(), true, false
                            , null, Constants.Message.MESSAGE_STATUS_PENDING_UPLOAD, SharedPrefManager.getLocalUserID());
                    if (user.getType() == Constants.User.USER_TYPE_GROUP) {
                        message.setGroupUserID(SharedPrefManager.getLocalUserID());
                        message.setMsgType(Constants.Box.BOX_TYPE_GROUP_MESSAGE);
                    }

                    dbViewModel.insertMessage(message);
                    //send(message);
                    user.setConnected(true);
                    et_message.setText("");
                    if (!isOnline) {
                        Log.i("NOT ONLINE", "SENDING MSG");
                        fireStoreDB.collection(Constants.KEY_FIRESTORE_USERS).document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                String userToken = documentSnapshot.getString(Constants.KEY_FIRESTORE_USER_TOKEN);
                                Messaging.sendMessageNotification(SharedPrefManager.getLocalUserID(), userToken, message.getTaggedMsg(), message.getMsg_ID(), SharedPrefManager.getLocalUser().getName(), message.getMsg());
                            }
                        });

                    }
                }
                return super.onSingleTapConfirmed(e);
            }
        });

        dbViewModel.getLiveMessagesList(userID).observe(this, messages -> {
            GetChatLiveMessagesThread gcmt = new GetChatLiveMessagesThread(user.getUser_id());
            chatThread = new Thread(gcmt);
            chatThread.start();
        });

    }


    private void requestCallPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE},
                        REQUEST_CALL_PERMISSION);
            } else {
                makePhoneCall();
            }
        } else {
            makePhoneCall();
        }
    }

    private void makePhoneCall() {
        if (user.getType() == Constants.User.USER_TYPE_GROUP)
            return;
        String phoneNumber = user.getPhoneNumber(); // Replace with the desired phone number
        Uri uri = Uri.parse("tel:" + phoneNumber);
        Intent intent = new Intent(Intent.ACTION_CALL, uri);
        startActivity(intent);
    }

    private final ResultReceiver resultReceiver = new ResultReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == DownloadFileService.RESULT_SUCCESS) {
                Log.i("RESULT RECEIVER::::::", "_________");
            } else {
                // Download failed
            }
        }
    };

    private void searchChat() {
        Log.i("2.SEARCH ::::", "searchChat");
        EditText et_input = findViewById(R.id.Chat_et_search_searchInput);
        et_input.setText("");
        findViewById(R.id.Chat_ib_search_close).setOnClickListener(view -> {
            findViewById(R.id.Chat_ll_search).setVisibility(View.GONE);
        });
        findViewById(R.id.Chat_ll_search).setVisibility(View.VISIBLE);
        findViewById(R.id.Chat_ib_search_search).setOnClickListener(view -> {
            if (!et_input.getText().toString().isEmpty()) {
                Log.i("2.SEARCH ::::", "searchChat:::: " + et_input.getText().toString());
                makeSearch(et_input.getText().toString().trim());
            }

        });
    }


    private void makeSearch(String query) {
        Log.i("3.SEARCH ::::", "makeSearch:::::" + query);

        ImageButton btnNext = findViewById(R.id.Chat_ib_search_down);
        ImageButton btnPrev = findViewById(R.id.Chat_ib_search_up);
        btnNext.setVisibility(View.VISIBLE);
        btnPrev.setVisibility(View.VISIBLE);

        final int[] pos = {0};
        pos[0] = searchCursor(chatCursor, query, false);
        if (pos[0] != -1) {
            chatRecyclerView.scrollToPosition(pos[0]);
        } else {
            Toast.makeText(getApplicationContext(), "No more results", Toast.LENGTH_SHORT).show();
        }
        btnNext.setOnClickListener(view -> {

                    pos[0] = searchCursor(chatCursor, query, true);
                    if (pos[0] != -1) {
                        chatRecyclerView.scrollToPosition(pos[0]);
                    } else {
                        Toast.makeText(getApplicationContext(), "No more results", Toast.LENGTH_SHORT).show();
                    }
                    Log.i("POSITION::::", pos[0] + "");
                }
        );

        btnPrev.setOnClickListener(v -> {
            pos[0] = searchCursor(chatCursor, query, false);
            if (pos[0] != -1) {
                chatRecyclerView.scrollToPosition(pos[0]);
            } else {
                Toast.makeText(getApplicationContext(), "No more results", Toast.LENGTH_SHORT).show();
            }
            Log.i("POSITION::::", pos[0] + "");

        });
        findViewById(R.id.Chat_ib_search_close).setOnClickListener(view -> {
            btnNext.setVisibility(View.GONE);
            btnPrev.setVisibility(View.GONE);
            findViewById(R.id.Chat_ll_search).setVisibility(View.GONE);
        });
    }


    public int searchCursor(Cursor cursor, String searchValue, boolean forward) {
        Log.i("1.SEARCH:::::::", searchValue);
        int pos = cursor.getColumnIndex("msg");
        int position = cursor.getPosition();
        if (forward) {
            while (cursor.moveToNext()) {
                String value = cursor.getString(pos);
                Log.i("SEARCH 1 :::::::::", searchValue + "__" + value);
                if (value != null && value.contains(searchValue)) {
                    return position;
                }
                position++;
            }
        } else {
            while (cursor.moveToPrevious()) {
                String value = cursor.getString(pos);
                if (value != null && value.contains(searchValue)) {
                    return position;
                }
                position--;
            }
        }
        // If the value was not found, return -1
        return -1;
    }


    private void attachmentPopUp() {


        if(findViewById(R.id.attachment_popup_cl_attachmentOptions).getVisibility()==View.VISIBLE){
            findViewById(R.id.attachment_popup_cl_attachmentOptions).setVisibility(View.GONE);

        }else {
            findViewById(R.id.attachment_popup_cl_attachmentOptions).setVisibility(View.VISIBLE);
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);


            findViewById(R.id.attachment_popup_ll_gallery).setOnClickListener(view -> {
                Log.i("ChatActivity.CLICKED::::", "GALLERY");
                Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
                intent1.setType("image/* video/*");

                if (intent1.resolveActivity(getPackageManager()) != null) {
                    Log.i("ChatActivity.CLICKED::::", "GALLERY_not null");
                    startActivityForResult(Intent.createChooser(intent1, "Select Picture"), Constants.Intent.KEY_INTENT_REQUEST_CODE_MEDIA);
                } else {
                    Intent intentOther = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intentOther.addCategory(Intent.CATEGORY_OPENABLE);
                    intentOther.setType("*/*");
                    String[] mimeTypes = {"image/*", "video/*"};
                    intentOther.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

                    startActivityForResult(intentOther, Constants.Intent.KEY_INTENT_REQUEST_CODE_MEDIA);

                    // No activities found to handle the intent
                    Toast.makeText(this, "No app found to handle this action", Toast.LENGTH_SHORT).show();
                }
            });
            findViewById(R.id.attachment_popup_ll_audio).setOnClickListener(view -> {

                Intent audioIntent = new Intent(this, SelectAudio.class);
                if (audioIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(audioIntent, Constants.Intent.KEY_INTENT_REQUEST_CODE_AUDIO);
                }
            });
            findViewById(R.id.attachment_popup_ll_camera).setOnClickListener(view -> {
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, Constants.Intent.KEY_INTENT_REQUEST_CODE_CAMERA);
                }
            });
            findViewById(R.id.attachment_popup_ll_poll).setOnClickListener(view -> {
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, Constants.Intent.KEY_INTENT_REQUEST_CODE_POLL);
                }
            });
            findViewById(R.id.attachment_popup_ll_payment).setOnClickListener(view -> {
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, Constants.Intent.KEY_INTENT_REQUEST_CODE_PAYMENT);
                }
            });
            findViewById(R.id.attachment_popup_ll_location).setOnClickListener(view -> {
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, Constants.Intent.KEY_INTENT_REQUEST_CODE_LOCATION);
                }
            });
            findViewById(R.id.attachment_popup_ll_canvas).setOnClickListener(view -> {
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, Constants.Intent.KEY_INTENT_REQUEST_CODE_CANVAS);
                }
            });
            findViewById(R.id.attachment_popup_ll_document).setOnClickListener(view -> {
                intent.setType("*/*");

//                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, Constants.Intent.KEY_INTENT_REQUEST_CODE_DOCUMENT);
//                }
            });
            findViewById(R.id.attachment_popup_ll_contact).setOnClickListener(view -> {
                Log.i("CLICKED::::", "GALLERY");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, Constants.Intent.KEY_INTENT_REQUEST_CODE_CONTACT);
                }
            });

        }

    }

//    GestureDetector sendButtonGestureDetector = new GestureDetector(ChatActivity.this, new GestureDetector.SimpleOnGestureListener() {
//        @Override
//        public boolean onDoubleTap(MotionEvent e) {
//            // Handle double-click event here
//            Log.i("ChatActivity.GestureDetect:::::::", "Double Tap!!!!");
//            FirebaseFirestore fireStore = FirebaseFirestore.getInstance();
//            fireStore.collection(Constants.KEY_FIRESTORE_USERS).document(userID).get().addOnSuccessListener(documentSnapshot -> {
//                String userToken = documentSnapshot.getString(Constants.KEY_FIRESTORE_USER_TOKEN);
//                Messaging.sendPingMessageNotification(userToken, SharedPrefManager.getLocalUser().getName());
//            });
//
//            return super.onDoubleTap(e);
//        }
//
//        @Override
//        public boolean onSingleTapConfirmed(MotionEvent e) {
//            Log.i("ChatActivity.GestureDetect 1 :::::::", "Single Tap!!!!");
//
//            if (!et_message.getText().toString().trim().equals("")) {
//                Message message = new Message(SharedPrefManager.getLocalUserID() + Calendar.getInstance().getSentTime().getSentTime(), userID, "0", et_message.getText().toString().trim(), true, false, null, 0);
//                dbViewModel.insertMessage(message);
//                //send(message);
//                user.setConnected(true);
//                et_message.setText("");
//                if (!isOnline) {
//                    Log.i("NOT ONLINE", "SENDING MSG");
//                    FirebaseFirestore fireStore = FirebaseFirestore.getInstance();
//                    fireStore.collection(Constants.KEY_FIRESTORE_USERS).document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                        @Override
//                        public void onSuccess(DocumentSnapshot documentSnapshot) {
//                            String userToken = documentSnapshot.getString(Constants.KEY_FIRESTORE_USER_TOKEN);
//                            Messaging.sendMessageNotification(SharedPrefManager.getLocalUserID(), userToken, message.getTaggedMsg(), message.getMsg_ID(), SharedPrefManager.getLocalUser().getName(), message.getMsg());
//                        }
//                    });
//
//                }
//            }
//            return super.onSingleTapConfirmed(e);
//        }
//    });

    private void setClicks() {

        ib_send.setOnTouchListener((v, event) -> sendButtonGestureDetector.onTouchEvent(event));

        ib_emoji.setOnClickListener(view -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                if (imm.isActive(et_message)) {
                    // Hide the keyboard
//                        imm.hideSoftInputFromWindow(et_message.getWindowToken(), 0);
                    // Switch to emoji keyboard
                    et_message.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
                    ib_emoji.setBackground(AppCompatResources.getDrawable(view.getContext(), R.drawable.ic_baseline_keyboard_24));
                } else {
                    // Show the keyboard
                    imm.showSoftInput(et_message, InputMethodManager.SHOW_FORCED);
                    // Switch to text keyboard
                    et_message.setInputType(InputType.TYPE_CLASS_TEXT);
                    ib_emoji.setBackground(AppCompatResources.getDrawable(view.getContext(), R.drawable.ic_baseline_emoji_emotions_24));
                }
            }
        });

    }


    private void loadChat() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setItemAnimator(new DefaultItemAnimator());
        chatRecyclerView.setAdapter(chatViewAdapter);
        chatViewAdapter.setOnItemClickListener((msgID, fileName, cvSize, tvSize, progressBar, ivCancel, view, upload) -> {
            Intent intent;
            Log.i("FIRESTORAGE :::::::", "STARTING");
            if (upload) {
                intent = new Intent(getApplicationContext(), UploadFileService.class);
                intent.putExtra(UploadFileService.EXTRA_RECEIVER, resultReceiver);
            } else {
                intent = new Intent(getApplicationContext(), DownloadFileService.class);
                intent.putExtra(DownloadFileService.EXTRA_RECEIVER, resultReceiver);
            }
            intent.putExtra(Constants.Intent.KEY_INTENT_MESSAGE_ID, msgID);
            startService(intent);
        });
        chatRecyclerView.smoothScrollToPosition(chatViewAdapter.getItemCount());
    }


    private void listenForOnline() {
        fireStoreDB.collection(Constants.KEY_FIRESTORE_USERS).document(userID).addSnapshotListener(ChatActivity.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }
                if (value != null) {
                    if (value.get(Constants.KEY_FIRESTORE_USER_ONLINE) != null) {
                        int online = Integer.parseInt(String.valueOf(Objects.requireNonNull(value.get(Constants.KEY_FIRESTORE_USER_ONLINE))));
                        isOnline = online == 1;
                    }
                    if (value.get(Constants.KEY_FIRESTORE_USER_PIC) != null) {
                        Log.i("User:::::", user.getDisplayName());
                        user.setPublicPic(String.valueOf(value.get(Constants.KEY_FIRESTORE_USER_PIC)));
                    }
                    if (value.get(Constants.KEY_FIRESTORE_USER_NAME) != null) {
                        user.setUserName(String.valueOf(value.get(Constants.KEY_FIRESTORE_USER_NAME)));
                    }
                    if (value.get(Constants.KEY_FIRESTORE_USER_ABOUT) != null) {
                        user.setAbout(String.valueOf(value.get(Constants.KEY_FIRESTORE_USER_ABOUT)));
                    }
                }
                if (isOnline) {
                    ab.setSubtitle("Online");
                } else {
                    ab.setSubtitle(user.getAbout());
                }
            }
        });
    }


    private void setMyActionBar() {
        ab = getSupportActionBar();
        ab.setTitle(user.getDisplayName());
        ab.setSubtitle(user.getAbout());
        ab.setDisplayHomeAsUpEnabled(true);
        ImageView iv = new ImageView(getApplicationContext());
        iv.setPadding(50, 50, 50, 50);
        iv.setImageBitmap(UsefulFunctions.getCircularBitmap(UsefulFunctions.decodeImage(user.getPublicPic())));

        Drawable d = iv.getDrawable();
        getSupportActionBar().setIcon(d);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.BLACK);

        ab.setBackgroundDrawable(colorDrawable);
        findViewById(R.id.action_bar).setOnClickListener(v -> {
             Intent i = new Intent(getApplicationContext(), ProfileView.class);
            i.putExtra(Constants.Intent.KEY_INTENT_USERID, userID);
            startActivity(i);
        });

    }


    private void attachViews() {
        new SharedPrefManager(getApplicationContext());
        chatRecyclerView = findViewById(R.id.Chat_RecyclerView);
        ib_send = findViewById(R.id.Chat_Button_Send);
        ib_emoji = findViewById(R.id.Chat_Button_Emoji);
        ib_attach = findViewById(R.id.Chat_Button_Attachment);
        et_message = findViewById(R.id.Chat_EditText_Message);
        ib_camera = findViewById(R.id.Chat_Button_Camera);
        ll_full = findViewById(R.id.Chat_ll_Full);


        et_message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() != 0) {
                    ib_attach.setVisibility(View.GONE);
                    ib_camera.setVisibility(View.GONE);
                    ib_send.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_send_24));
                    ib_attach.setVisibility(View.GONE);
                    ib_attach = findViewById(R.id.Chat_Button_Attachment2);
                    ib_attach.setVisibility(View.VISIBLE);
                    ib_attach.setOnClickListener(view -> attachmentPopUp());
                } else {
                    ib_attach.setVisibility(View.GONE);
                    ib_attach = findViewById(R.id.Chat_Button_Attachment);
                    ib_attach.setVisibility(View.VISIBLE);
                    ib_attach.setOnClickListener(view -> attachmentPopUp());
                    ib_camera.setVisibility(View.VISIBLE);
                    ib_send.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_mic_24));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        ib_attach.setOnClickListener(view -> attachmentPopUp());
        ib_camera.setOnClickListener(view -> {
            Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(camera_intent, Constants.Intent.KEY_INTENT_REQUEST_CODE_CAMERA);

        });
    }


    public void updateChat(String inp) {
        chatCursor = dbViewModel.getChatMessages(inp);
        mediaList = dbViewModel.getUserMedia(userID);
        if (mediaList != null && mediaList.size() != 0) {
            for (Message msg : mediaList) {
                Log.i("MEDIA LIST:::::::", msg.getMediaType() + "_" + msg.getMediaID());
            }
        }
        runOnUiThread(() -> {
            chatViewAdapter = new ChatView_RecyclerAdapter(getApplicationContext(), chatCursor
                    , (msgID, fileName, cvSize, tvSize, progressBar, ivCancel, view, upload) -> {
                Intent intent;
                if (upload) {
                    intent = new Intent(getApplicationContext(), UploadFileService.class);
                    intent.putExtra(UploadFileService.EXTRA_RECEIVER, resultReceiver);
                } else {
                    intent = new Intent(getApplicationContext(), DownloadFileService.class);
                    intent.putExtra(DownloadFileService.EXTRA_RECEIVER, resultReceiver);
                }
                intent.putExtra(Constants.Intent.KEY_INTENT_MESSAGE_ID, msgID);
                startService(intent);
            }, dbViewModel, false);
            chatRecyclerView.setAdapter(null);
            loadChat();
            chatRecyclerView.smoothScrollToPosition(chatViewAdapter.getItemCount());
        });
//        updated = false;
        user = dbViewModel.getUser(userID);
    }

    class GetChatLiveMessagesThread implements Runnable {
        String inp;

        GetChatLiveMessagesThread(String inp) {
            this.inp = inp;
        }

        @Override
        public void run() {
            updateChat(inp);
            chatThread.interrupt();
        }
    }

//    class GetChatMessagesThread implements Runnable {
//        String inp;
//
//        GetChatMessagesThread(String inp) {
//            this.inp = inp;
//        }
//
//        @Override
//        public void run() {
//            chatCursor = dbViewModel.getChatMessages(inp);
//            mediaList = dbViewModel.getUserMedia(userID);
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Log.i(
//                            "MAKING ADAPTER CHAT:::::", "MAking.." + chatCursor.getCount() + "..");
//
//                    chatViewAdapter = new ChatView_RecyclerAdapter(getApplicationContext(), chatCursor
//                            , new ChatView_RecyclerAdapter.OnItemClickListener() {
//                        @Override
//                        public void OnItemClick(String msgID, String fileName, CardView cvSize, TextView tvSize, ProgressBar progressBar, ImageView ivCancel, View view, boolean upload) {
//                            Intent intent;
//                            Log.i("FIRESTORAGE :::::::", "STARTING");
//                            if (upload) {
//                                intent = new Intent(getApplicationContext(), UploadFileService.class);
//                                intent.putExtra(UploadFileService.EXTRA_RECEIVER, resultReceiver);
//                            } else {
//                                intent = new Intent(getApplicationContext(), DownloadFileService.class);
//                                intent.putExtra(DownloadFileService.EXTRA_RECEIVER, resultReceiver);
//                            }
//                            intent.putExtra(Constants.KEY_INTENT_MESSAGE_ID, msgID);
//                            startService(intent);
//                        }
//                    }, dbViewModel, true);
//                    loadChat();
//                    chatRecyclerView.scrollToPosition(chatViewAdapter.getItemCount());
//                }
//            });
//
//            while (true) {
//                if (updated && chatViewAdapter != null && updateID.equals(userID)) {
//                    int l = chatCursor.getCount();
//                    //chatCursor.close();
//                    chatCursor = dbViewModel.getChatMessages(inp);
//                    mediaList = dbViewModel.getUserMedia(userID);
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            chatViewAdapter = new ChatView_RecyclerAdapter(getApplicationContext(), chatCursor
//                                    , new ChatView_RecyclerAdapter.OnItemClickListener() {
//                                @Override
//                                public void OnItemClick(String msgID, String fileName, CardView cvSize, TextView tvSize, ProgressBar progressBar, ImageView ivCancel, View view, boolean upload) {
//                                    Intent intent;
//                                    Log.i("FIRESTORAGE :::::::", "STARTING");
//                                    if (upload) {
//                                        intent = new Intent(getApplicationContext(), UploadFileService.class);
//                                        intent.putExtra(UploadFileService.EXTRA_RECEIVER, resultReceiver);
//                                    } else {
//                                        intent = new Intent(getApplicationContext(), DownloadFileService.class);
//                                        intent.putExtra(DownloadFileService.EXTRA_RECEIVER, resultReceiver);
//                                    }
//                                    intent.putExtra(Constants.KEY_INTENT_MESSAGE_ID, msgID);
//                                    startService(intent);
//                                }
//                            }, dbViewModel, false);
//                            chatRecyclerView.setAdapter(null);
//                            loadChat();
//                            chatRecyclerView.smoothScrollToPosition(chatViewAdapter.getItemCount());
//                        }
//                    });
//                    updated = false;
//                    user = dbViewModel.getUser(userID);
//                }
//            }
//
//        }
//    }
}