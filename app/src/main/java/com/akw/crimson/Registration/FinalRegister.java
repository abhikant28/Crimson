package com.akw.crimson.Registration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Database;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.akw.crimson.Database.SharedPrefManager;
import com.akw.crimson.MainActivity;
import com.akw.crimson.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FinalRegister extends AppCompatActivity {

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://crimson-ims-default-rtdb.asia-southeast1.firebasedatabase.app/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        String name=getIntent().getExtras().getString("name");
        String email=getIntent().getExtras().getString("email");
        String profilePic=getIntent().getExtras().getString("profilePic");

        makeCall(profilePic,name,email);

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void makeCall(String profilePic,String userName,String email) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String userID= new SharedPrefManager(getApplicationContext()).getLocalUserID();
                if(snapshot.child("users").hasChild(userID)){
                    Toast.makeText(FinalRegister.this, "Welcome Back!", Toast.LENGTH_SHORT).show();
                }else{
                    String phone=new SharedPrefManager(getApplicationContext()).getLocalPhoneNumber();
                    databaseReference.child("users").child(userID).child("phone").setValue(phone);
                    databaseReference.child("users").child(userID).child("email").setValue(email);
                    databaseReference.child("users").child(userID).child("name").setValue(userName);
                    databaseReference.child("users").child(userID).child("pic").setValue(profilePic);
                    Toast.makeText(FinalRegister.this, "Welcome to Crimson!", Toast.LENGTH_SHORT).show();
                }
                new SharedPrefManager(getApplicationContext()).storeUserProfile(profilePic, userName, email);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


}