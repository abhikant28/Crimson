package com.akw.crimson;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Backend.Adapters.AllUserList_RecyclerListAdapter;
import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.TheViewModel;
import com.akw.crimson.Chat.ChatActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StartNew extends BaseActivity {

    HashMap<String, String> allContacts = new HashMap<>();
    ArrayList<String> allNums = new ArrayList<>();

    TheViewModel dbViewModel;

    AllUserList_RecyclerListAdapter allUserList_recyclerListAdapter = new AllUserList_RecyclerListAdapter();
    RecyclerView rv_allUsers;
    ActionBar ab;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 22:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            getContacts();
                        }
                    }, 0);
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_new);

        setViews();
        connectToDb();

    }


    private void connectToDb() {
        dbViewModel = ViewModelProviders.of(this).get(TheViewModel.class);
        Log.i("DB:::::", "Acha");
        allUserList_recyclerListAdapter.submitList(dbViewModel.getAllUsersList().getValue());
        Log.i("DB:::::", "Achaaaa");
        dbViewModel.getAllUsersList().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                allUserList_recyclerListAdapter.submitList(null);
                allUserList_recyclerListAdapter.submitList(users);
                ab.setSubtitle(users.size() + " Contacts");
            }
        });
        permissionCheck();
    }


    private void getContacts() {
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        int nInd = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int pInd = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        while (phones.moveToNext() && phones.getCount() > 0) {
            String name = phones.getString(nInd);
            String phoneNumber = countryCoding(phones.getString(pInd));
            if (phoneNumber.length() > 6) {
                allContacts.put(phoneNumber, name);
                allNums.add(phoneNumber);
            }
        }
        phones.close();
        filterNums();
        searchFireStoreForContacts();
    }

    private void filterNums() {
        ArrayList<String> numsToCheck = new ArrayList<>();
        for (String s : allNums) {
            if (dbViewModel != null && !dbViewModel.checkForUserNum(s)) {
                numsToCheck.add(s);
            } else if (dbViewModel != null && dbViewModel.getUserByNum(s) != null) {
                User user = dbViewModel.getUserByNum(s);
                if (user.getDisplayName().equals(user.getPhoneNumber())) {
                    user.setDisplayName(allContacts.get(s));
                    dbViewModel.updateUser(user);
                }
            }
        }
        allNums.clear();
        allNums.addAll(numsToCheck);
    }


    private void searchFireStoreForContacts() {
        Log.i("CONTACTS LENGTH::::", allContacts.size() + "");
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference citiesRef = firebaseFirestore.collection(Constants.KEY_FIRESTORE_USERS);
        ArrayList<DocumentSnapshot> numsFound = new ArrayList<>();

        ArrayList<Task> tasks = new ArrayList<>();
        Log.i("All NUMS", allNums.size() + "");
        for (int i = 0; i * 10 < allNums.size(); i++) {
            Task<QuerySnapshot> found = citiesRef.whereIn("phone", allNums.subList(i * 10, Math.min((i + 1) * 10, allNums.size()))).get();
            //Log.i("USERS::::", String.valueOf(allNums.subList(i * 10, Math.min((i + 1) * 10, allNums.size()))));
            found.addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (queryDocumentSnapshots.size() > 0) {
                        numsFound.addAll(queryDocumentSnapshots.getDocuments());
                        Log.i("QuerySnapshot::::", queryDocumentSnapshots.size() + "_" + queryDocumentSnapshots.getDocuments().get(0) + "_" + numsFound.size());
                    }
                }
            });
            tasks.add(found);
        }
        Tasks.whenAllSuccess(tasks).addOnSuccessListener((OnSuccessListener<? super List<Object>>) objects -> {
            Log.i("QuerySnapshot::::", "TASKS COMPLETED " + numsFound.size());
            if (!numsFound.isEmpty()) {
                makeUsers(numsFound);
            }
        });
    }


    private void makeUsers(ArrayList<DocumentSnapshot> numsFound) {
//        Log.i("Created Users::::::", "CREATED" + numsFound);
        for (DocumentSnapshot doc : numsFound) {
//            Log.i("Created User::::::", doc.getId());
            User user = new User(doc.getId(), doc.getString("name"), doc.getString("name")
                    , allContacts.get(doc.getString("phone")).trim(), doc.getString("pic")
                    , doc.getString("phone"), false,doc.getString("about"));
            dbViewModel.insertUser(user);
        }
    }


    private String countryCoding(String phoneNumber) {
        phoneNumber = phoneNumber.charAt(0) == '0' ? phoneNumber.substring(1) : phoneNumber;
        phoneNumber = phoneNumber.replaceAll(" ", "").replaceAll("-", "").replaceAll("\\)", "");
        phoneNumber = phoneNumber.replaceAll("\\(", "");
        Pattern pattern = Pattern.compile("^\\+(\\d+)[^\\d]*(.*)$");
        Matcher matcher = pattern.matcher(phoneNumber);
        if (matcher.matches()) {
            if (phoneNumber.startsWith("+")) {
                return phoneNumber;
            }
            return "+" + phoneNumber;
        }
        return "+91" + phoneNumber;
    }


    private void permissionCheck() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED) {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    getContacts();
                }
            }, 0);
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 22);
        }
    }


    private void setViews() {
        rv_allUsers = findViewById(R.id.startNew_rv_allUsers);
        rv_allUsers.setLayoutManager(new LinearLayoutManager(this));
        rv_allUsers.setAdapter(allUserList_recyclerListAdapter);
        allUserList_recyclerListAdapter.setOnItemCLickListener(new AllUserList_RecyclerListAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(User user, TextView tv_name, TextView tv_lastMsg, View view) {
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra(Constants.KEY_INTENT_USERID, user.getUser_id());
                startActivity(intent);
            }
        });
        ab = getSupportActionBar();
        ab.setTitle("Select Contact");
        ColorDrawable colorDrawable = new ColorDrawable(Color.BLACK);
        ab.setBackgroundDrawable(colorDrawable);
    }
}
