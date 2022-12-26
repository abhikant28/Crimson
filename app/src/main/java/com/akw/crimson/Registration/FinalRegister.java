package com.akw.crimson.Registration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.akw.crimson.Database.SharedPrefManager;
import com.akw.crimson.MainActivity;
import com.akw.crimson.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Hashtable;

public class FinalRegister extends AppCompatActivity {

    DatabaseReference databaseReference = FirebaseDatabase.getInstance()
            .getReferenceFromUrl("https://crimson-ims-default-rtdb.asia-southeast1.firebasedatabase.app/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        String name=getIntent().getExtras().getString("name");
        String email=getIntent().getExtras().getString("email");
        String profilePic=getIntent().getExtras().getString("encodedImg");

        makeCall(profilePic,name,email);

        Intent intent =new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void makeCall(String profilePic,String userName,String email) {
        FirebaseFirestore firebaseFirestore= FirebaseFirestore.getInstance();
        String userID= new SharedPrefManager(getApplicationContext()).getLocalUserID();
        String phone=new SharedPrefManager(getApplicationContext()).getLocalPhoneNumber();

        Hashtable<String, Object> data= new Hashtable<>();
        data.put("name", userName);
        data.put("pic", profilePic);
        data.put("email", email);
        data.put("phone", phone);

        firebaseFirestore.collection("users")
                .document(userID).set(data, SetOptions.merge())
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getApplicationContext(), "Data Inserted with Success", Toast.LENGTH_SHORT).show();
                    new SharedPrefManager(getApplicationContext()).storeUserProfile(profilePic, userName, email);
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

    }

    private void makefireReatimeDbCall(String profilePic,String userName,String email) {

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String userID= new SharedPrefManager(getApplicationContext()).getLocalUserID();
                String phone=new SharedPrefManager(getApplicationContext()).getLocalPhoneNumber();
                databaseReference.child("users").child(userID).child("phone").setValue(phone);
                databaseReference.child("users").child(userID).child("email").setValue(email);
                databaseReference.child("users").child(userID).child("name").setValue(userName);
                databaseReference.child("users").child(userID).child("pic").setValue(profilePic);
                FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String token) {
                          new SharedPrefManager(getApplicationContext()).storeUserToken(token);
                    }
                });

                new SharedPrefManager(getApplicationContext()).storeUserProfile(profilePic, userName, email);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

}