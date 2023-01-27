package com.akw.crimson.Chat;

import android.Manifest;
import android.content.ContentResolver;
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
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
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
import com.akw.crimson.PrepareMessageActivity;
import com.akw.crimson.ProfileView;
import com.akw.crimson.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.File;
import java.util.Calendar;
import java.util.Objects;

public class ChatActivity extends BaseActivity {

    private ChatView_RecyclerAdapter chatViewAdapter;
    private RecyclerView chatRecyclerView;
    private ImageButton ib_send, ib_attach, ib_camera, ib_emoji;
    private EditText et_message;
    LinearLayout ll_full;
    Button btnPrev, btnNext;

    private TheViewModel dbViewModel;
    private Cursor chatCursor;
    private Thread chatThread;
    private ActionBar ab;

    public static volatile User user;
    public static volatile boolean updated = false;
    public static volatile String userID, updateID;
    private Boolean isOnline = false;
    private int searchPosition;


    @Override
    protected void onStop() {
        super.onStop();
        if (user != null) {
            user.setUnread_count(0);
            user.setUnread(false);
        }
        dbViewModel.updateUser(user);
        chatThread.interrupt();
        stopService(new Intent(this, Communicator.class));
    }

    @Override
    public void finish() {
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
        MenuItem searchItem = menu.findItem(R.id.chat_menu_search);

        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length() != 0) {
                    searchPosition = 0;
                    makeSearch(searchView, chatCursor, query);
                } else {
                    searchPosition = 0;
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final int[] code = {55, 66, 77, 88, 99, 44, 33, 22, 11};
        Message message = null;
        if (resultCode == RESULT_OK && data.getData() != null) {

            if (requestCode == code[0] || requestCode == code[1] || requestCode == code[2] || requestCode == code[3] || requestCode == code[8]) {
                if (ContextCompat.checkSelfPermission(
                        this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            99);
                    return;

                }
            }
            if (requestCode == code[1]) {
                Uri uri = data.getData();
                ContentResolver cR = this.getContentResolver();
                File file;
                if (cR.getType(uri).startsWith("video/")) {
                    file = UsefulFunctions.getOutputMediaFile(this, true, Constants.KEY_MESSAGE_MEDIA_TYPE_VIDEO);
                    UsefulFunctions.saveFile(this, data.getData(), file);

                    message = new Message(SharedPrefManager.getLocalUserID() + Calendar.getInstance().getTime().getTime(), userID, null
                            , null, file.getName(), (file.length() / (1024)), true, false, true, -1, Constants.KEY_MESSAGE_MEDIA_TYPE_VIDEO);

                } else {
                    file = UsefulFunctions.getOutputMediaFile(this, true, Constants.KEY_MESSAGE_MEDIA_TYPE_IMAGE);
                    Bitmap bitmap = UsefulFunctions.resizeAndCompressImage(this, data.getData());
                    UsefulFunctions.saveImage(bitmap, true, file);

                    message = new Message(SharedPrefManager.getLocalUserID(), userID, null
                            , null, file.getName(), (file.length() / (1024)), true, false, true, -1, Constants.KEY_MESSAGE_MEDIA_TYPE_IMAGE);
                }


            } else if (requestCode == code[0]) {
                String f = UsefulFunctions.getFileName(this, data.getData());

                File file = UsefulFunctions.getOutputMediaFile(this, true, Constants.KEY_MESSAGE_MEDIA_TYPE_DOCUMENT, f);
                UsefulFunctions.saveFile(this, data.getData(), file);
                message = new Message(SharedPrefManager.getLocalUserID() + Calendar.getInstance().getTime().getTime(), userID, null
                        , null, file.getName(), (file.length() / (1024)), true, false, true, -1, Constants.KEY_MESSAGE_MEDIA_TYPE_DOCUMENT);

            } else if (requestCode == code[3]) {
                File file = UsefulFunctions.getOutputMediaFile(this, true, Constants.KEY_MESSAGE_MEDIA_TYPE_AUDIO);
                UsefulFunctions.saveFile(this, data.getData(), file);
                message = new Message(SharedPrefManager.getLocalUserID() + Calendar.getInstance().getTime().getTime(), userID, null
                        , null, file.getName(), (file.length() / (1024)), true, false, true, -1, Constants.KEY_MESSAGE_MEDIA_TYPE_AUDIO);

            } else if (requestCode == code[2]) {
                File file = UsefulFunctions.getOutputMediaFile(this, true, Constants.KEY_MESSAGE_MEDIA_TYPE_IMAGE);
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                UsefulFunctions.saveImage(imageBitmap, true, file);
                Log.i("CAMERA:::::", data.getData() + "");
                message = new Message(SharedPrefManager.getLocalUserID() + Calendar.getInstance().getTime().getTime(), userID, null
                        , null, file.getName(), (file.length() / (1024)), true, false, true, -1, Constants.KEY_MESSAGE_MEDIA_TYPE_IMAGE);

            }
            if (message != null) {
                if (message.getMediaType() == Constants.KEY_MESSAGE_MEDIA_TYPE_DOCUMENT) {
                    user.addDoc(message.getMediaID());
                } else {
                    user.addMedia(message.getMediaID());
                }
                user.setLast_msg(null);
                user.setLast_msg_type(message.getMediaType());
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
                break;
            case R.id.chat_menu_prepareMessage:
                startActivity(new Intent(this, PrepareMessageActivity.class));
                break;
            case R.id.chat_menu_profile:
                Intent i = new Intent(this, ProfileView.class);
                i.putExtra(Constants.KEY_INTENT_USERID, userID);
                startActivity(i);

        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        attachViews();

        new SharedPrefManager(this);
        dbViewModel = Communicator.localDB;
        user = dbViewModel.getUser(getIntent().getStringExtra(Constants.KEY_INTENT_USERID));
        userID = user.getUser_id();

        setMyActionBar();
        setClicks();

        GetChatMessagesThread gcmt = new GetChatMessagesThread(user.getUser_id());
        chatThread = new Thread(gcmt);
        chatThread.start();

    }


    private ResultReceiver resultReceiver = new ResultReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == DownloadFileService.RESULT_SUCCESS) {
                Log.i("RESULT RECEIVER::::::", "_________");
            } else {
                // Download failed
            }
        }
    };


    private void attachmentPopUp() {
        View popupView = getLayoutInflater().inflate(R.layout.chat_attachment_popup, null);
// Create the PopupWindow
        final PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        final int[] code = {55, 66, 77, 88, 99, 44, 33, 22, 11};
        popupView.findViewById(R.id.attachment_popup_ll_gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("CLICKED::::", "GALLERY");
                intent.setType("image/* video/*");
                code[0] = 55;
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, code[1]);
                }
                popupWindow.dismiss();
            }
        });
        popupView.findViewById(R.id.attachment_popup_ll_audio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] mimetypes = {"audio/3gp", "audio/AMR", "audio/mp3"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                intent.setType("audio/*");

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, code[3]);
                }
                popupWindow.dismiss();
            }
        });
        popupView.findViewById(R.id.attachment_popup_ll_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, code[2]);
                }
                popupWindow.dismiss();
            }
        });
        popupView.findViewById(R.id.attachment_popup_ll_poll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, code[7]);
                }
                popupWindow.dismiss();
            }
        });
        popupView.findViewById(R.id.attachment_popup_ll_payment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, code[4]);
                }
                popupWindow.dismiss();
            }
        });
        popupView.findViewById(R.id.attachment_popup_ll_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, code[5]);
                }
                popupWindow.dismiss();
            }
        });
        popupView.findViewById(R.id.attachment_popup_ll_canvas).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, code[8]);
                }
                popupWindow.dismiss();
            }
        });
        popupView.findViewById(R.id.attachment_popup_ll_document).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.setType("*/*");

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, code[0]);
                }
                popupWindow.dismiss();
            }
        });
        popupView.findViewById(R.id.attachment_popup_ll_contact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("CLICKED::::", "GALLERY");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, code[6]);
                }
                popupWindow.dismiss();
            }
        });

// Set an OnClickListener for the camera option

        popupWindow.showAtLocation(ll_full, Gravity.BOTTOM, et_message.getWidth() / 50, et_message.getHeight() * 2 + 10);

    }


    private void makeSearch(SearchView searchView, Cursor cursor, String query) {

        ViewGroup.LayoutParams navButtonsParams = new ViewGroup.LayoutParams(ab.getHeight() * 2 / 3, ab.getHeight() * 2 / 3);

        btnNext = new Button(this);
        btnNext.setBackground(getDrawable(R.drawable.ic_baseline_arrow_up_24));

        btnPrev = new Button(this);
        btnPrev.setBackground(getDrawable(R.drawable.ic_baseline_arrow_down_24));

        ((LinearLayout) searchView.getChildAt(0)).addView(btnNext, navButtonsParams);
        ((LinearLayout) searchView.getChildAt(0)).addView(btnPrev, navButtonsParams);
        ((LinearLayout) searchView.getChildAt(0)).setGravity(Gravity.BOTTOM);


        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = searchCursor(chatCursor, query, true);
                if (pos != -1) {
                    chatRecyclerView.scrollToPosition(pos);
                } else {
                    Toast.makeText(getApplicationContext(), "No more results", Toast.LENGTH_SHORT).show();
                }
                Log.i("POSITION::::", pos + "");
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = searchCursor(chatCursor, query, false);
                if (pos != -1) {
                    chatRecyclerView.scrollToPosition(pos);
                } else {
                    Toast.makeText(getApplicationContext(), "No more results", Toast.LENGTH_SHORT).show();
                }
                Log.i("POSITION::::", pos + "");
            }
        });
    }


    public int searchCursor(Cursor cursor, String searchValue, boolean forward) {
        int pos = cursor.getColumnIndex("msg");
        int position = cursor.getPosition();
        if (forward) {
            while (cursor.moveToNext()) {
                String value = cursor.getString(pos);
                if (value.contains(searchValue)) {
                    return position;
                }
                position++;
            }
        } else {
            while (cursor.moveToPrevious()) {
                String value = cursor.getString(pos);
                if (value.contains(searchValue)) {
                    return position;
                }
                position--;
            }
        }
        // If the value was not found, return -1
        return -1;
    }


    private void setClicks() {
        ib_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!et_message.getText().toString().trim().equals("")) {
                    Message message = new Message(SharedPrefManager.getLocalUserID() + Calendar.getInstance().getTime().getTime(), userID, "0", et_message.getText().toString().trim(), true, false, null, 0);
                    dbViewModel.insertMessage(message);
                    //send(message);
                    user.setConnected(true);
                    et_message.setText("");
                    if (!isOnline) {
                        Log.i("NOT ONLINE", "SENDING MSG");
                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                        firestore.collection(Constants.KEY_FIRESTORE_USERS).document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                String userToken = documentSnapshot.getString(Constants.KEY_FIRESTORE_USER_TOKEN);
                                Messaging.sendMessageNotification(SharedPrefManager.getLocalUserID(), userToken, message.getTag(), message.getMsg_ID(), SharedPrefManager.getLocalUser().getName(), message.getMsg());
//                                Messaging.sendMessageRetroNotification(new SharedPrefManager(getApplicationContext()).getLocalUserID(), userToken, message.getTag(), message.getMsg_ID(),userID);
                            }
                        });

                    }
                }
            }
        });

        ib_emoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        chatViewAdapter.setOnItemClickListener(new ChatView_RecyclerAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(String msgID, String fileName, CardView cvSize, TextView tvSize, ProgressBar progressBar, ImageView ivCancel, View view, boolean upload) {
                Intent intent;
                Log.i("FIRESTORAGE :::::::", "STARTING");
                if (upload) {
                    intent = new Intent(getApplicationContext(), UploadFileService.class);
                    intent.putExtra(UploadFileService.EXTRA_RECEIVER, resultReceiver);
                } else {
                    intent = new Intent(getApplicationContext(), DownloadFileService.class);
                    intent.putExtra(DownloadFileService.EXTRA_RECEIVER, resultReceiver);
                }
                intent.putExtra(Constants.KEY_INTENT_MESSAGE_ID, msgID);
                startService(intent);
            }
        });
        chatRecyclerView.smoothScrollToPosition(chatViewAdapter.getItemCount());
    }


    private void listenForOnline() {
        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        firestoreDB.collection(Constants.KEY_FIRESTORE_USERS).document(userID).addSnapshotListener(ChatActivity.this, new EventListener<DocumentSnapshot>() {
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
                        user.setPic(String.valueOf(value.get(Constants.KEY_FIRESTORE_USER_PIC)));
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
                    ab.setSubtitle("");
                }
            }
        });
    }


    private void setMyActionBar() {
        ab = getSupportActionBar();
        ab.setTitle(user.getDisplayName());
        ab.setSubtitle("Status");
        ab.setDisplayHomeAsUpEnabled(true);
        ImageView iv = new ImageView(getApplicationContext());
        iv.setPadding(50, 50, 50, 50);
        iv.setImageBitmap(UsefulFunctions.getCircularBitmap(user.getPicBitmap()));

        Drawable d = iv.getDrawable();
        getSupportActionBar().setIcon(d);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.BLACK);

        ab.setBackgroundDrawable(colorDrawable);
        findViewById(R.id.action_bar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ProfileView.class);
                i.putExtra(Constants.KEY_INTENT_USERID, userID);
                startActivity(i);
            }
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
                } else {
                    ib_attach.setVisibility(View.VISIBLE);
                    ib_camera.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        ib_attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attachmentPopUp();
            }
        });
        ib_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(camera_intent, 77);
            }
        });
    }


    class GetChatMessagesThread implements Runnable {
        String inp;

        GetChatMessagesThread(String inp) {
            this.inp = inp;
        }

        @Override
        public void run() {
            chatCursor = dbViewModel.getChatMessages(inp);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(
                            "MAKING ADAPTER CHAT:::::", "MAking.." + chatCursor.getCount() + "..");

                    chatViewAdapter = new ChatView_RecyclerAdapter(getApplicationContext(), chatCursor
                            , new ChatView_RecyclerAdapter.OnItemClickListener() {
                        @Override
                        public void OnItemClick(String msgID, String fileName, CardView cvSize, TextView tvSize, ProgressBar progressBar, ImageView ivCancel, View view, boolean upload) {
                            Intent intent;
                            Log.i("FIRESTORAGE :::::::", "STARTING");
                            if (upload) {
                                intent = new Intent(getApplicationContext(), UploadFileService.class);
                                intent.putExtra(UploadFileService.EXTRA_RECEIVER, resultReceiver);
                            } else {
                                intent = new Intent(getApplicationContext(), DownloadFileService.class);
                                intent.putExtra(DownloadFileService.EXTRA_RECEIVER, resultReceiver);
                            }
                            intent.putExtra(Constants.KEY_INTENT_MESSAGE_ID, msgID);
                            startService(intent);
                        }
                    }, dbViewModel, true);
                    loadChat();
                    chatRecyclerView.scrollToPosition(chatViewAdapter.getItemCount());
                }
            });

            while (true) {
                if (updated && chatViewAdapter != null && updateID.equals(userID)) {
                    int l = chatCursor.getCount();
                    chatCursor.close();
                    chatCursor = dbViewModel.getChatMessages(inp);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            chatViewAdapter = new ChatView_RecyclerAdapter(getApplicationContext(), chatCursor
                                    , new ChatView_RecyclerAdapter.OnItemClickListener() {
                                @Override
                                public void OnItemClick(String msgID, String fileName, CardView cvSize, TextView tvSize, ProgressBar progressBar, ImageView ivCancel, View view, boolean upload) {
                                    Intent intent;
                                    Log.i("FIRESTORAGE :::::::", "STARTING");
                                    if (upload) {
                                        intent = new Intent(getApplicationContext(), UploadFileService.class);
                                        intent.putExtra(UploadFileService.EXTRA_RECEIVER, resultReceiver);
                                    } else {
                                        intent = new Intent(getApplicationContext(), DownloadFileService.class);
                                        intent.putExtra(DownloadFileService.EXTRA_RECEIVER, resultReceiver);
                                    }
                                    intent.putExtra(Constants.KEY_INTENT_MESSAGE_ID, msgID);
                                    startService(intent);
                                }
                            }, dbViewModel, false);
                            chatRecyclerView.setAdapter(null);
                            loadChat();
                            chatRecyclerView.smoothScrollToPosition(chatViewAdapter.getItemCount());
                        }
                    });
                    updated = false;
                    user = dbViewModel.getUser(userID);
                }
            }

        }
    }
}