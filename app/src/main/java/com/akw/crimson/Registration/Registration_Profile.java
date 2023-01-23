package com.akw.crimson.Registration;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.SharedPrefManager;
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class Registration_Profile extends AppCompatActivity {

    private EditText et_mail;
    private EditText et_pass;
    private Button b_verify;
    private ImageView iv_profilePic;
    private TextView tv_ImageText;

    private String encodedImage;
    private boolean hasPic=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_profile);

        setViews();

//        checkForProfile();
        checkFireStoreForProfile();
    }


    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK) {
                    if(result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try{
                            InputStream inputStream= getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                            iv_profilePic.setImageBitmap(bitmap);
                            tv_ImageText.setVisibility(View.GONE);
                            hasPic=true;
                            encodedImage=UsefulFunctions.encodeImage(bitmap);
                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
            );

    private void checkFireStoreForProfile(){
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {

            Log.i("TAG ::::", "DONE");

        } else {
            Log.i("Permission ::::", "NOTTTTT");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    99);
        }
        FirebaseFirestore firebaseFirestore= FirebaseFirestore.getInstance();
        String userID= new SharedPrefManager(getApplicationContext()).getLocalUserID();

        DocumentReference ref=firebaseFirestore.collection(Constants.KEY_FIRESTORE_USERS).document(userID);
        ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String phone=document.getString(Constants.KEY_FIRESTORE_USER_PHONE);
                        String email=document.getString(Constants.KEY_FIRESTORE_USER_EMAIL);
                        String name=document.getString(Constants.KEY_FIRESTORE_USER_NAME);
                        String profilePic=document.getString(Constants.KEY_FIRESTORE_USER_PIC);
                        encodedImage=profilePic;
                        if(!encodedImage.isEmpty())hasPic=true;
                        et_mail.setText(email);
                        et_pass.setText(name);
                        Bitmap bitmap= UsefulFunctions.decodeImage(profilePic);
                        iv_profilePic.setImageBitmap(bitmap);
                        tv_ImageText.setVisibility(View.GONE);
//                        Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
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


    private void setViews() {
        et_mail = findViewById(R.id.Registration_Email_EditView_Mail);
        et_pass = findViewById(R.id.Registration_Email_EditView_name);
        b_verify = findViewById(R.id.Registration_Email_Button_Verify);
        iv_profilePic=findViewById(R.id.Registration_Email_iv_profilePic);
        tv_ImageText=findViewById(R.id.Registration_Email_tv_addPic);

        iv_profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                pickImage.launch(intent);
            }
        });
        tv_ImageText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                pickImage.launch(intent);
            }
        });

        b_verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkFields()) {
                    Intent intent = new Intent(getApplicationContext(), FinalRegister.class);
                    intent.putExtra(Constants.KEY_INTENT_EMAIL, et_mail.getText().toString().toLowerCase().trim());
                    intent.putExtra(Constants.KEY_INTENT_USERNAME, et_pass.getText().toString().trim());
                    if(hasPic){
                        Log.i("HAS PIC:::","TRUE");
                        intent.putExtra(Constants.KEY_INTENT_PIC, encodedImage);
                    }
                    startActivity(intent);
                }
            }
        });
    }
}