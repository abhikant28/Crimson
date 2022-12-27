package com.akw.crimson;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.akw.crimson.Database.SharedPrefManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class BaseActivity extends AppCompatActivity {

    DocumentReference documentReference;

    @Override
    protected void onPause() {
        super.onPause();
        documentReference.update("online",0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        documentReference.update("online",1);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseFirestore firestore= FirebaseFirestore.getInstance();
        documentReference=firestore.collection("users").document(new SharedPrefManager(getApplicationContext()).getLocalUserID());
    }


}
