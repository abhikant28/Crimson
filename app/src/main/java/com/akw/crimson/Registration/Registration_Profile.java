package com.akw.crimson.Registration;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.akw.crimson.Database.SharedPrefManager;
import com.akw.crimson.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Registration_Profile extends AppCompatActivity {

    private EditText et_mail;
    private EditText et_pass;
    private Button b_verify;

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://crimson-ims-default-rtdb.asia-southeast1.firebasedatabase.app/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_email);


        setViews();

    }

    private void setViews() {
        et_mail = findViewById(R.id.Registration_Email_EditView_Mail);
        et_pass = findViewById(R.id.Registration_Email_EditView_Password);
        b_verify = findViewById(R.id.Registration_Email_Button_Verify);

        b_verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkFields()) {
                    //verifyMail("AZBY_0987654321_AZBY",et_pass.getText().toString(),et_mail.getText().toString());
                    //checkMail();
                    Intent intent = new Intent(getApplicationContext(), FinalRegister.class);
                    intent.putExtra("email", et_mail.getText().toString().toLowerCase().trim());
                    intent.putExtra("profilePic", et_mail.getText().toString().toLowerCase().trim());
                    intent.putExtra("name", et_pass.getText().toString().toLowerCase().trim());
                    startActivity(intent);
                }

            }
        });
    }

    private void checkForProfile(){
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String userID= new SharedPrefManager(getApplicationContext()).getLocalUserID();
                if(snapshot.child("users").hasChild(userID)){
                    Toast.makeText(getApplicationContext(), "Welcome Back!", Toast.LENGTH_SHORT).show();
                    String name=snapshot.child("users").child(userID).child("name").toString();
                    String email=snapshot.child("users").child(userID).child("email").toString();
                    String profilePic=snapshot.child("users").child(userID).child("pic").toString();
                    et_mail.setText(email);
                    et_pass.setText(name);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private boolean checkFields() {
        if (et_pass.getText().toString().isEmpty()) {
            return false;
        }
        String mail = et_mail.getText().toString().trim().toLowerCase();

        return !mail.isEmpty() && mail.matches("^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$");
    }

}