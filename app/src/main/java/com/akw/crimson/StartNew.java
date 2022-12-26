package com.akw.crimson;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Adapters.AllUserList_RecyclerListAdapter;
import com.akw.crimson.AppObjects.User;
import com.akw.crimson.Chat.Chat;
import com.akw.crimson.Database.TheViewModel;
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

public class StartNew extends AppCompatActivity {

    HashMap<String, String> allContacts = new HashMap<>();
    ArrayList<String> allNums = new ArrayList<>();
    ArrayList<User> allUsers=new ArrayList<>();

    TheViewModel dbViewModel;

    AllUserList_RecyclerListAdapter allUserList_recyclerListAdapter=new AllUserList_RecyclerListAdapter();
    RecyclerView rv_allUsers;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 22:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContacts();
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

        permissionCheck();

        setViews();

        dbViewModel = ViewModelProviders.of(this).get(TheViewModel.class);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                getContacts();
            }
        }, 0);

        connectToDb();
    }

    private void setViews() {
        rv_allUsers=findViewById(R.id.startNew_rv_allUsers);
        rv_allUsers.setLayoutManager(new LinearLayoutManager(this));
        rv_allUsers.setAdapter(allUserList_recyclerListAdapter);
        allUserList_recyclerListAdapter.setOnItemCLickListener(new AllUserList_RecyclerListAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(User user) {
                Intent intent = new Intent(getApplicationContext(), Chat.class);
                intent.putExtra("USER_ID", user.getUser_id());
                startActivity(intent);
            }
        });
    }

    private void connectToDb() {
        dbViewModel.getAllUsersList().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                allUserList_recyclerListAdapter.submitList(null);
                allUserList_recyclerListAdapter.submitList(users);
            }
        });
    }


    private void getContacts() {
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        int nInd = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int pInd = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        while (phones.moveToNext() && phones.getCount() > 0) {
            String name = phones.getString(nInd);
            String phoneNumber = countryCoding(phones.getString(pInd));
            allContacts.put(phoneNumber, name);
            allNums.add(phoneNumber);
        }
        phones.close();
        searchFireStoreForContacts();
    }


    private void searchFireStoreForContacts() {
        Log.i("CONTACTS LENGTH::::", allContacts.size()+"");
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference citiesRef = firebaseFirestore.collection("users");
        ArrayList<DocumentSnapshot> numsFound = new ArrayList<>();

        ArrayList<Task> tasks= new ArrayList<>();
        for (int i = 0; i * 10 < allNums.size(); i++) {
            Task found = citiesRef.whereIn("phone", allNums.subList(i * 10, Math.min((i + 1) * 10, allNums.size()))).get();
            found.addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (queryDocumentSnapshots.size() > 0) {
                        numsFound.addAll(queryDocumentSnapshots.getDocuments());
                        Log.i("QuerySnapshot::::", queryDocumentSnapshots.size() + "_" + queryDocumentSnapshots.getDocuments().get(0)+"_"+numsFound.size());
                    }
                }
            });
            tasks.add(found);
        }
        Tasks.whenAllSuccess(tasks).addOnSuccessListener((OnSuccessListener<? super List<Object>>) objects -> {
            Log.i("QuerySnapshot::::", "TASKS COMPLETED "+numsFound.size());
            makeUsers(numsFound);
        });
    }


    private void makeUsers(ArrayList<DocumentSnapshot> numsFound) {
        for(DocumentSnapshot doc: numsFound){
            User user=new User(doc.getId(), doc.getString("name"), allContacts.get(doc.getString("phone")).trim(), doc.getString("pic"), doc.getString("phone"),false);
            dbViewModel.insertUser(user);
        }
    }


    private void permissionCheck() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED) {
            getContacts();
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 22);
        }
    }


    private String countryCoding(String phoneNumber) {
        phoneNumber=phoneNumber.replace(" ", "").replace("-", "").replace("(", "").replace(")", "");
        Pattern pattern = Pattern.compile("^\\+(\\d+)[^\\d]*(.*)$");
        Matcher matcher = pattern.matcher(phoneNumber);
        if (matcher.matches()) {
            if(phoneNumber.startsWith("+")){
                return phoneNumber;
            }
            return "+"+phoneNumber;
        }
        return "+91" + phoneNumber;
    }

}
