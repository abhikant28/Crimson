package com.akw.crimson;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Messenger;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.akw.crimson.Backend.Adapters.UserDisplay_RecyclerViewAdapter;
import com.akw.crimson.Backend.AppObjects.Group;
import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Communications.Communicator;
import com.akw.crimson.Backend.Communications.UploadFileService;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.SharedPrefManager;
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.Utilities.SelectContact;
import com.akw.crimson.databinding.ActivityCreateGroupBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreateGroup extends BaseActivity {

    ActivityCreateGroupBinding binding;
    Uri imageUri;
    Group group;
    User groupUser;
    UserDisplay_RecyclerViewAdapter userDisplay_recyclerAdapter;

    String encodedImage;
    ArrayList<User> users = new ArrayList<>();
    ArrayList<String> admins = new ArrayList<>();
    private boolean hasPic = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && requestCode == Constants.KEY_ACTIVITY_RESULT_CONTACT_SELECT) {
            String listGson = data.getStringExtra(Constants.Intent.KEY_INTENT_USER_LIST);
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<User>>() {
            }.getType();
            users = gson.fromJson(listGson, type);
            Log.i("CreateGroup.USERS ::::::", users.toString());
        }
        if(users.size()!=0){
            binding.CreateGroupIvAddParticipant.setVisibility(View.GONE);
            GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
            binding.CreateGroupRvParticipants.setLayoutManager(layoutManager);
            userDisplay_recyclerAdapter = new UserDisplay_RecyclerViewAdapter(true);
            binding.CreateGroupRvParticipants.setAdapter(userDisplay_recyclerAdapter);
            userDisplay_recyclerAdapter.submitList(users);
            userDisplay_recyclerAdapter.setOnItemCLickListener((user, first) -> {
                if (first) {
                    Log.i("First Element :::::", "clicked");
                    Intent intent = new Intent(this, SelectContact.class);
                    Gson gson = new Gson();
                    String json = gson.toJson(users);
                    intent.putExtra(Constants.Intent.KEY_INTENT_USER_LIST, json);
                    intent.putExtra(Constants.Intent.KEY_INTENT_TYPE, Constants.Intent.KEY_INTENT_TYPE_MULTI_SELECT);
                    startActivityForResult(intent, Constants.KEY_ACTIVITY_RESULT_CONTACT_SELECT);
                } else {
                    Log.i("Close Element :::::", "clicked");
                    users.remove(user);
                    userDisplay_recyclerAdapter.submitList(users);
                }
            });
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateGroupBinding.inflate(getLayoutInflater());
        View v = binding.getRoot();
        setContentView(v);

        initialize();

    }



    private void initialize() {

        new SharedPrefManager(this);
        if(users.size()!=0){
            GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
            binding.CreateGroupRvParticipants.setLayoutManager(layoutManager);
            userDisplay_recyclerAdapter = new UserDisplay_RecyclerViewAdapter(true);
            binding.CreateGroupRvParticipants.setAdapter(userDisplay_recyclerAdapter);
            userDisplay_recyclerAdapter.submitList(users);
            userDisplay_recyclerAdapter.setOnItemCLickListener((user, first) -> {
                if (first) {
                    Log.i("First Element :::::", "clicked");
                    Intent intent = new Intent(this, SelectContact.class);
                    Gson gson = new Gson();
                    String json = gson.toJson(users);
                    intent.putExtra(Constants.Intent.KEY_INTENT_USER_LIST, json);
                    intent.putExtra(Constants.Intent.KEY_INTENT_TYPE, Constants.Intent.KEY_INTENT_TYPE_MULTI_SELECT);
                    startActivityForResult(intent, Constants.KEY_ACTIVITY_RESULT_CONTACT_SELECT);
                } else {
                    users.remove(user);
                    userDisplay_recyclerAdapter.submitList(null);
                    userDisplay_recyclerAdapter.submitList(users);
                }
            });
        }

        binding.CreateGroupIvAddParticipant.setOnClickListener(view -> {
            Intent intent = new Intent(this, SelectContact.class);
            Gson gson = new Gson();
            String json = gson.toJson(users);
            intent.putExtra(Constants.Intent.KEY_INTENT_USER_LIST, json);
            intent.putExtra(Constants.Intent.KEY_INTENT_TYPE, Constants.Intent.KEY_INTENT_TYPE_MULTI_SELECT);
            startActivityForResult(intent, Constants.KEY_ACTIVITY_RESULT_CONTACT_SELECT);
        });
        binding.CreateGroupFloatButtonSubmit.setOnClickListener(view -> {
            if (!binding.CreateGroupEtGroupSubject.getText().toString().trim().isEmpty() && users.size() != 0) {
                ArrayList<String> userIDs = new ArrayList<>();
                userIDs.add(SharedPrefManager.getLocalUserID());
                for (User u : users)
                    userIDs.add(u.getUser_id());
                admins.add(SharedPrefManager.getLocalUserID());
                group = new Group(binding.CreateGroupEtGroupSubject.getText().toString().trim(), SharedPrefManager.getLocalUserID(), userIDs, admins);

                if (UsefulFunctions.isInternetConnected(this)) {
                    createGroup();
                } else {
                    Toast.makeText(this, "Check Network ", Toast.LENGTH_SHORT).show();
                }
            }
        });


        binding.CreateGroupCvGroupIcon.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private void createDoc() {
        Log.i("COMMUNICATOR.createDoc:::", "Creating Doc: ");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = db.collection(Constants.KEY_FIRESTORE_GROUPS);

// Create a new document with a random ID
        DocumentReference documentRef = collectionRef.document();

// Set the data for the document
        Map<String, Object> data = new HashMap<>();
        data.put(Constants.KEY_FIRESTORE_GROUP_NAME, group.getDisplayName());

        documentRef.set(data)
                .addOnSuccessListener(aVoid -> {
                    // Document creation successful
                    Log.d("CreateGroup.FireStore", "Document created with ID: " + documentRef.getId());
                    String userID = documentRef.getId();
                    String name = binding.CreateGroupEtGroupSubject.getText().toString().trim();

                    group.setGroupId(userID);
                    groupUser = new User(userID, name, encodedImage, group.getGroupId(), Constants.User.USER_TYPE_GROUP, group);
                    if (hasPic) {
                        groupUser.setPublicPic(imageUri.getPath());
                        groupUser.setPublicPic(encodedImage);
                    }
                    documentRef.update(Constants.KEY_FIRESTORE_GROUP_ID, group.getGroupId());
                    documentRef.update(Constants.KEY_FIRESTORE_GROUP_ADMINS, group.getAdmins());
                    documentRef.update(Constants.KEY_FIRESTORE_GROUP_USERS, group.getUsers().toString());
                    documentRef.update(Constants.KEY_FIRESTORE_GROUP_CREATED_BY, group.getCreatedBy());
                    documentRef.update(Constants.KEY_FIRESTORE_GROUP_CREATED_TIME, group.getCreatedTime());
                    documentRef.update(Constants.KEY_FIRESTORE_GROUP_USERS, group.getUsers().toString());
                    Message msg = new Message(SharedPrefManager.getLocalUserID(), group.getGroupId(), "Added you", false, Communicator.thisUserID
                            , true, Constants.Message.MESSAGE_STATUS_PENDING_UPLOAD, Constants.Message.MESSAGE_TYPE_INFO, Constants.Box.BOX_TYPE_NEW_GROUP, null,null);
                    Communicator.localDB.insertUser(groupUser);
                    Communicator.localDB.insertMessage(msg);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Document creation failed
                    Log.e("FireStore", "Error creating document: " + e.getMessage());
                });

    }


    private void createGroup() {

        Log.i("CreateGroup.INTENT MSG ID:::::", "cursor.getString(cursor.getColumnIndexOrThrow(msg_ID))");
        if (hasPic) {
            Intent intent;
            intent = new Intent(this.getApplicationContext(), UploadFileService.class);
            intent.putExtra(UploadFileService.EXTRA_RECEIVER, "resultReceiver");
            Messenger messenger = new Messenger(new Handler() {
                @Override
                public void handleMessage(android.os.Message msg) {
                }
            });
            intent.putExtra(Constants.Intent.KEY_INTENT_MESSENGER, messenger);
            intent.putExtra(Constants.Intent.KEY_INTENT_MESSAGE_ID, "");
            this.startService(intent);
        }
        createDoc();
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        imageUri = result.getData().getData();
                        Bitmap bitmap = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            bitmap = UsefulFunctions.resizeAndCompressImage(this, imageUri);
                        } else {
                            try {
                                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                                encodedImage = UsefulFunctions.encodeImage(bitmap);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                        binding.CreateGroupIvGroupIcon.setImageBitmap(bitmap);
                        hasPic = true;
                    }
                }
            }
    );
}