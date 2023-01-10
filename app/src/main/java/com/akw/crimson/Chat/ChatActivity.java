package com.akw.crimson.Chat;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Adapters.Chat_RecyclerAdapter;
import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Communications.Communicator;
import com.akw.crimson.Backend.Communications.Messaging;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.SharedPrefManager;
import com.akw.crimson.Backend.Database.TheViewModel;
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.BaseActivity;
import com.akw.crimson.PrepareMessageActivity;
import com.akw.crimson.R;
import com.akw.crimson.ViewProfile;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Objects;

public class ChatActivity extends BaseActivity {

    private Chat_RecyclerAdapter chatAdapter;
    private RecyclerView chatRecyclerView;
    private ImageButton ib_send, ib_attach, ib_camera;
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
        if (requestCode == 55 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Log.i("RESULT:::::", data.getData()+"");

            Bitmap bitmap=UsefulFunctions.resizeAndCompressImage(this,data.getData());
//            ImageView iv= new ImageView(this);
//            ConstraintLayout.LayoutParams lparams = new ConstraintLayout.LayoutParams(
//                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            lparams.setMargins(0, 0, 0, 15);
//            iv.setLayoutParams(lparams);
//            iv.setImageBitmap(bitmap);
//            ll_full.addView(iv,0);
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {

                    if(bitmap!=null)UsefulFunctions.saveBitmapAsJpeg(this, bitmap);
                    Log.i("TAG"+"::::","DONE");

            } else {
                Log.i("Permission"+"::::","NOTTTTTTTT");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        99);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.chat_menu_importChat:
                break;
            case R.id.chat_menu_prepareMessage:
                startActivity(new Intent(this, PrepareMessageActivity.class));
                break;
            case R.id.chat_menu_profile:
                startActivity(new Intent(this, ViewProfile.class));

        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        attachViews();

        dbViewModel = Communicator.localDB;
        user = dbViewModel.getUser(getIntent().getStringExtra(Constants.KEY_INTENT_USERID));
        userID = user.getUser_id();

        //Log.i("USER_ID", user.get_id());
        setMyActionBar();
        setClicks();

        GetChatMessagesThread gcmt = new GetChatMessagesThread(user.getUser_id());
        chatThread = new Thread(gcmt);
        chatThread.start();

    }

    //    private void uploadImg(Uri imageUri) {
//        InputStream inputStream= null;
//        try {
//            inputStream = getContentResolver().openInputStream(imageUri);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        FirebaseStorage storage = FirebaseStorage.getInstance();
//        Path outputPath = android.os.Environment.getExternalStorageDirectory();;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            try {
//                Files.copy(inputStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//// Create a storage reference from our app
//        StorageReference storageRef = storage.getReference();
//
//// Create a child reference for the file you want to upload
//        StorageReference imagesRef = storageRef.child("media/my_image.jpg");
//
//// Create a file from the selected image
//        File file = new File(imagePath);
//
//// Convert the file to a byte[]
////        byte[] fileData = Files.readAllBytes(imagePath);
//        byte[] fileContent=null;
//        try (FileInputStream inputStream = new FileInputStream(imagePath)) {
//            Log.i("ERROR::::::::;;", "1");
//            fileContent = new byte[inputStream.available()];
//            Log.i("ERROR::::::::;;","2");
//            inputStream.read(fileContent);
//        }catch (Exception e){
//            Log.i("ERROR::::::::;;",e.toString()+ "");
//        }
//// Upload the file to Firebase Storage
//        UploadTask uploadTask = imagesRef.putBytes(fileContent);
//        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//            }
//        });
//
//    }
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        Log.i("URI::::", imageUri.getPath());
//                        uploadImg(imageUri);
                    }
                }
            }
    );

    private void attachmentPopUp() {
        View popupView = getLayoutInflater().inflate(R.layout.chat_attachment_popup, null);

// Create the PopupWindow
        final PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);


// Set an OnClickListener for the camera option
        popupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("CLICKED::::", view + "");
                String type = "";
                // Handle document option selection
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/* video/*");
                switch (view.getId()) {
                    case R.id.attachment_popup_ll_audio:
                        Log.i("CLICKED::::", "AUDIO");
                        intent.setType("audio/*");
                        break;
                    case R.id.attachment_popup_ll_gallery:
                        Log.i("CLICKED::::", "GALLERY");
                        intent.setType("image/* video/*");
                        break;
                    case R.id.attachment_popup_ll_payment:
                        type = "";
                        break;
                    case R.id.attachment_popup_ll_camera:
                        break;
                    case R.id.attachment_popup_ll_contact:
                        type = "";
                        break;
                    case R.id.attachment_popup_ll_document:
                        intent.setType("*/*");
                        break;
                    case R.id.attachment_popup_ll_location:
                        type = "";
                        break;
                    case R.id.attachment_popup_ll_poll:
                        return;
//                        break;

                }
                    startActivityForResult(intent, 55);
//                intent= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                pickImage.launch(intent);
                popupWindow.dismiss();
            }
        });

        popupWindow.showAtLocation(et_message, Gravity.BOTTOM, 0, 0);

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

    }


    private void loadChat() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setItemAnimator(new DefaultItemAnimator());
        chatRecyclerView.setAdapter(chatAdapter);
        chatAdapter.setOnItemClickListener(new Chat_RecyclerAdapter.OnListItemClickListener() {
            @Override
            public void onListItemClick(int position) {

            }
        });
        chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount());
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

                    chatAdapter = new Chat_RecyclerAdapter(getApplicationContext(), chatCursor
                            , new Chat_RecyclerAdapter.OnListItemClickListener() {
                        @Override
                        public void onListItemClick(int position) {

                        }
                    }, dbViewModel, true);
                    loadChat();
                    chatRecyclerView.scrollToPosition(chatAdapter.getItemCount());
                }
            });
            while (true) {
                if (updated && chatAdapter != null && updateID.equals(userID)) {
                    int l = chatCursor.getCount();
                    chatCursor = dbViewModel.getChatMessages(inp);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            chatAdapter = new Chat_RecyclerAdapter(getApplicationContext(), chatCursor
                                    , new Chat_RecyclerAdapter.OnListItemClickListener() {
                                @Override
                                public void onListItemClick(int position) {

                                }
                            }, dbViewModel, false);
                            chatRecyclerView.setAdapter(null);
                            loadChat();
                            chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount());
                        }
                    });
                    updated = false;
                    user = dbViewModel.getUser(userID);
                }
            }

        }
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
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        ImageView iv = new ImageView(getApplicationContext());
        iv.setPadding(50, 50, 50, 50);
        if (user.getPic() != null) {
            iv.setImageBitmap(UsefulFunctions.decodeImage(user.getPic()));
        } else {
            iv.setImageResource(R.drawable.ic_baseline_person_24);
        }
        Drawable d = iv.getDrawable();
        getSupportActionBar().setIcon(d);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#DC143C"));

        ab.setBackgroundDrawable(colorDrawable);
    }


    private void attachViews() {
        new SharedPrefManager(getApplicationContext());
        chatRecyclerView = findViewById(R.id.Chat_RecyclerView);
        ib_send = findViewById(R.id.Chat_Button_Send);
        ib_attach = findViewById(R.id.Chat_Button_Attachment);
        et_message = findViewById(R.id.Chat_EditText_Message);
        ib_camera = findViewById(R.id.Chat_Button_Camera);
        ll_full=findViewById(R.id.Chat_ll_Full);

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
    }

}
