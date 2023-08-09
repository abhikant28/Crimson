package com.akw.crimson;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Backend.Adapters.AllUserList_RecyclerListAdapter;
import com.akw.crimson.Backend.Adapters.ChatList_RecyclerListAdapter;
import com.akw.crimson.Backend.Adapters.MessageSearch_RecyclerListAdapter;
import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Communications.Communicator;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.SharedPrefManager;
import com.akw.crimson.Backend.Database.TheViewModel;
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.Chat.ChatActivity;
import com.akw.crimson.Chat.GroupChatActivity;
import com.akw.crimson.Chat.MessageAttachment;
import com.akw.crimson.Gallery.MainGalleryActivity;
import com.akw.crimson.Preferences.SettingsActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class MainChatList extends BaseActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 123;
    private int currentMenuId = R.menu.chatlist_menu;

    ActionBar ab;
    TextView tv_noResults, tv_unreadCount, tv_convCount;
    RecyclerView rv_chatList;
    RecyclerView rv_searchUsers;
    RecyclerView rv_searchMessages;

    ChatList_RecyclerListAdapter chatList_recyclerListAdapter = new ChatList_RecyclerListAdapter();
    AllUserList_RecyclerListAdapter searchUserList_rvAdapter = new AllUserList_RecyclerListAdapter();
    MessageSearch_RecyclerListAdapter searchMessageList_rv_Adapter = new MessageSearch_RecyclerListAdapter();

    TheViewModel dbViewModel;
    public static User user;
    private String mCurrentPhotoPath;
    public static boolean selectingUsers =false;
    public static ArrayList<User> selectedUserList;
    public static ArrayList<String> selectedUserIDList;


    // Launch the camera intent


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i("onActivityResult.CAMERA IMAGE ::::::", (data == null) + "_" + resultCode + "_" + requestCode);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Image captured and saved to the file specified in the Intent
            Bitmap imageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);

            File file = UsefulFunctions.FileUtil.makeOutputMediaFile(this, true, Constants.Media.KEY_MESSAGE_MEDIA_TYPE_CAMERA_IMAGE);
            UsefulFunctions.FileUtil.saveImage(imageBitmap, true, file);
            // Save the image in the best quality
            Intent intent = new Intent(this, MessageAttachment.class);
            ArrayList<Integer> arr = new ArrayList<>();
            arr.add(Constants.Intent.KEY_INTENT_REQUEST_CODE_CAMERA);
            intent.putExtra(Constants.Intent.KEY_INTENT_REQUEST_CODE, arr);
            ArrayList<String> ar = new ArrayList<>();
            Uri contentUri = FileProvider.getUriForFile(
                    this,
                    "com.akw.crimson.fileprovider",
                    file
            );
            ar.add(contentUri.toString());
            intent.putExtra(Constants.Intent.KEY_INTENT_URI, ar);
            intent.putExtra(Constants.Intent.KEY_INTENT_USERID, new ArrayList<>());
            startActivity(intent);

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(currentMenuId, menu);
        MenuItem searchItem = menu.findItem(R.id.chatList_Menu_search);
        if (searchItem!=null){
            // getting search view of our item.
            SearchView searchView = (SearchView) searchItem.getActionView();

            // below line is to call set on query text listener method.
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    Log.i("QUERY CHANGE:::::", newText);
                    if (newText.length() == 0) {
                        rv_chatList.setVisibility(View.VISIBLE);
                        searchMessageList_rv_Adapter.submitList(null);
                        searchUserList_rvAdapter.submitList(null);
                        tv_noResults.setVisibility(View.GONE);
                    } else {
                        rv_chatList.setVisibility(View.GONE);
                        searchQuery(newText);
                    }
                    return false;
                }
            });
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.chatList_Menu_gallery:
                startActivity(new Intent(this, MainGalleryActivity.class));
                break;
            case R.id.chatList_Menu_Settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.chatList_Menu_NewGroup:
                startActivity(new Intent(this, CreateGroup.class));
                break;
            case R.id.chatList_Menu_autoSendMsg:
                startActivity(new Intent(this, PrepareMessageActivity.class));
                break;
            case R.id.chatList_Menu_camera:
                dispatchTakePictureIntent();
                break;
            case R.id.chatList_Menu_starredMessages:
                startActivity(new Intent(this, StarredMessages.class));
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_chat_list);
        setView();
        connectLocalDatabase();

        setChatList();

        sendToken();

    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Log.i("CAMERA.dispatchTakePictureIntent:::::::", "Started");
        // Create a file to save the image
        Log.i("CAMERA.dispatchTakePictureIntent:::::::", "takePictureIntent.resolveActivity(getPackageManager()) != null");
        File photoFile = null;
        try {
            Log.i("CAMERA.dispatchTakePictureIntent:::::::", "try");
            photoFile= UsefulFunctions.FileUtil.makeOutputMediaFile(this, true, Constants.Media.KEY_MESSAGE_MEDIA_TYPE_CAMERA_IMAGE);
            mCurrentPhotoPath = photoFile.getAbsolutePath();
        } catch (Exception ex) {
            // Handle file creation error
            Log.e("CAMERA.dispatchTakePictureIntent:::::::", "Failed::" + ex.getMessage());
            ex.printStackTrace();
        }

        // Continue only if the file was successfully created
        if (photoFile != null) {
            Log.i("CAMERA.dispatchTakePictureIntent:::::::", "File created successfully");
            Uri photoURI = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


    private void connectLocalDatabase() {

        dbViewModel = Communicator.localDB;

        dbViewModel.getChatListUsers().observe(this, users -> {
            chatList_recyclerListAdapter.submitList(users);
            updateCount(users);
        });
    }

    private void updateCount(List<User> users) {
        int c = 0;
        for (User u : users) {
            if (u.isUnreadUser())
                c++;
        }
        tv_unreadCount.setText((c == 0) ? "" : c + " Unread");
        tv_convCount.setText((users.size() == 0) ? "" : users.size() + " Conversation" + (users.size() == 1 ? "" : "s"));
        tv_unreadCount.setOnClickListener(view -> {
            int f = 0;
            for (User u : users) {
                f++;
                if (u.isUnreadUser()) {
                    rv_chatList.scrollToPosition(f);
                    return;
                }
            }
        });
    }

    private void searchQuery(String newText) {
        List<Message> messagesFound = dbViewModel.searchInMessages(newText);
        List<User> usersFound = dbViewModel.searchUserByText(newText);
        if (messagesFound.size() == 0 && usersFound.size() == 0) {
            tv_noResults.setVisibility(View.VISIBLE);
            searchMessageList_rv_Adapter.submitList(null);
            searchUserList_rvAdapter.submitList(null);
            return;
        }
        tv_noResults.setVisibility(View.GONE);
        searchMessageList_rv_Adapter.submitList(null);
        searchMessageList_rv_Adapter.submitList(messagesFound);
        searchUserList_rvAdapter.submitList(null);
        searchUserList_rvAdapter.submitList(usersFound);

    }

    private void sendToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        Hashtable<String, Object> data = new Hashtable<>();
        data.put(Constants.KEY_FIRESTORE_USER_TOKEN, token);

        firebaseFirestore.collection(Constants.KEY_FIRESTORE_USERS)
                .document(new SharedPrefManager(this).getLocalUserID()).set(data, SetOptions.merge())
                .addOnSuccessListener(documentReference -> {
                })
                .addOnFailureListener(documentReference -> {
                });
    }



    private void setChatList() {
        rv_chatList.setLayoutManager(new LinearLayoutManager(this));
        rv_chatList.setAdapter(chatList_recyclerListAdapter);
        chatList_recyclerListAdapter.setOnItemCLickListener((itemView, user) -> {
            if(selectingUsers){
                selectedUser();
                if(selectedUserIDList.contains(user.getUser_id())){
                    selectedUserIDList.remove(user.getUser_id());
                    selectedUserList.remove(user);
                    itemView.findViewById(R.id.MainChatList_Item_iv_selectedTick).setVisibility(View.GONE);
                    itemView.setBackgroundColor(Color.BLACK);
                    if(selectedUserIDList.size()==0){
                        selectingUsers=false;
                        currentMenuId=R.menu.chatlist_menu;
                        invalidateOptionsMenu();
                        baseActionBar.setDisplayHomeAsUpEnabled(false);
                    }
                }else {
                    selectedUserIDList.add(user.getUser_id());
                    selectedUserList.add(user);
                    itemView.findViewById(R.id.MainChatList_Item_iv_selectedTick).setVisibility(View.VISIBLE);
                    itemView.setBackgroundColor(Color.parseColor("#393838"));
                }

            }else{
                Intent intent;
                if (user.getType() == Constants.User.USER_TYPE_GROUP) {
                    intent = new Intent(getApplicationContext(), GroupChatActivity.class);
                } else {
                    intent = new Intent(getApplicationContext(), ChatActivity.class);
                }
                intent.putExtra(Constants.Intent.KEY_INTENT_USERID, user.getUser_id());
                Log.i("USER ID::::::", user.getDisplayName());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        chatList_recyclerListAdapter.setOnItemLongCLickListener((position, itemView, user1) -> {

            if(!selectingUsers){
                currentMenuId=R.menu.chatlist_single_selected_menu;
                invalidateOptionsMenu();
                selectingUsers =true;
                selectedUserIDList= new ArrayList<>();
                selectedUserIDList.add(user1.getUser_id());
                selectedUserList= new ArrayList<>();
                selectedUserList.add(user1);
                itemView.findViewById(R.id.MainChatList_Item_iv_selectedTick).setVisibility(View.VISIBLE);
                itemView.setBackgroundColor(Color.parseColor("#393838"));

            }

        });
        rv_searchUsers.setLayoutManager(new LinearLayoutManager(this));
        rv_searchUsers.setAdapter(searchUserList_rvAdapter);
        searchUserList_rvAdapter.setOnItemCLickListener((User, tv_name, tv_lastMsg, view) -> {
            Intent intent = new Intent(getApplicationContext(), ProfileView.class);
            intent.putExtra(Constants.Intent.KEY_INTENT_USERID, User.getUser_id());
            startActivity(intent);
        });

        searchMessageList_rv_Adapter = new MessageSearch_RecyclerListAdapter(dbViewModel);
        rv_searchMessages.setLayoutManager(new LinearLayoutManager(this));
        rv_searchMessages.setAdapter(searchMessageList_rv_Adapter);
        searchMessageList_rv_Adapter.setOnItemCLickListener(message -> {
            Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
            intent.putExtra(Constants.Intent.KEY_INTENT_USERID, message.getUser_id());
            intent.putExtra(Constants.Intent.KEY_INTENT_MESSAGE_ID, message.getMsg_ID());
            intent.putExtra(Constants.Intent.KEY_INTENT_STARTED_BY, Constants.Intent.KEY_INTENT_STARTED_BY_SEARCH);
            startActivity(intent);
        });
    }

    private void selectedUser() {

        baseActionBar.setTitle(""+selectedUserIDList.size());
        baseActionBar.setDisplayHomeAsUpEnabled(true);

        if(selectedUserIDList.size()>1 && selectedUserIDList.size()<3-ChatList_RecyclerListAdapter.pinnedUserCount){



        }else{

        }
    }

    private void setView() {
        ab = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.BLACK);
        ab.setBackgroundDrawable(colorDrawable);
        ab.setDisplayHomeAsUpEnabled(false);
        ab.setTitle(UsefulFunctions.getGreeting());

        tv_noResults = findViewById(R.id.MainChat_tv_Search_noResultsFound);
        tv_convCount = findViewById(R.id.MainChat_tv_chatCount);
        tv_unreadCount = findViewById(R.id.MainChat_tv_unreadCount);
        rv_chatList = findViewById(R.id.MainChat_List_RecyclerView);
        rv_searchMessages = findViewById(R.id.MainChat_MessageSearch_List_RecyclerView);
        rv_searchUsers = findViewById(R.id.MainChat_UserSearch_List_RecyclerView);
        FloatingActionButton fab_startNew = findViewById(R.id.MainChat_floatButton_newMessage);

        fab_startNew.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), StartNew.class)));

    }

}

