package com.akw.crimson.Registration;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.SharedPrefManager;
import com.akw.crimson.MainActivity;
import com.akw.crimson.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Hashtable;

public class FinalRegister extends AppCompatActivity {

    DatabaseReference databaseReference = FirebaseDatabase.getInstance()
            .getReferenceFromUrl(Constants.FIREBASE_REALTIME_DATABASE_MSG_URL);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_final);
        new SharedPrefManager(getApplicationContext());
        String name = getIntent().getExtras().getString(Constants.KEY_INTENT_USERNAME);
        String email = getIntent().getExtras().getString(Constants.KEY_INTENT_EMAIL);
        String profilePic = getIntent().getExtras().getString(Constants.KEY_INTENT_PIC);

        Log.i("USERNAME_::::",name);
        makeCall(profilePic, name, email);


    }

    private void makeCall(String profilePic, String userName, String email) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        String userID = SharedPrefManager.getLocalUserID();
        String phone = SharedPrefManager.getLocalPhoneNumber();

        Hashtable<String, Object> data = new Hashtable<>();
        data.put(Constants.KEY_FIRESTORE_USER_NAME, userName);
        if(profilePic!=null){
            data.put(Constants.KEY_FIRESTORE_USER_PIC, profilePic);
        }
        data.put(Constants.KEY_FIRESTORE_USER_EMAIL, email);
        data.put(Constants.KEY_FIRESTORE_USER_PHONE, phone.replaceAll(" ", ""));

        firebaseFirestore.collection(Constants.KEY_FIRESTORE_USERS)
                .document(userID).set(data, SetOptions.merge())
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getApplicationContext(), "User Details Registered", Toast.LENGTH_SHORT).show();
                    SharedPrefManager.storeUserProfile(profilePic, userName, email);
                    Log.i("USERNAME::::",userName);
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(documentReference -> {
                    Toast.makeText(getApplicationContext(), "Data Insert Failed", Toast.LENGTH_SHORT).show();
                });
//        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
//                    @Override
//                    public void onSuccess(String token) {
//                        new SharedPrefManager(getApplicationContext()).storeUserToken(token);
//                    }
//                });

        makefireReatimeDbCall(profilePic,userName,email);
    }

    private void makefireReatimeDbCall(String profilePic, String userName, String email) {

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userID = SharedPrefManager.getLocalUserID();
                databaseReference.child(Constants.FIREBASE_REALTIME_DATABASE_CHILD_MSG).child(userID).setValue(userID);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }

        });
    }
}