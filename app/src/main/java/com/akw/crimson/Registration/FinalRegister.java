package com.akw.crimson.Registration;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.akw.crimson.Backend.Communications.Communicator;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.SharedPrefManager;
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.BaseActivity;
import com.akw.crimson.MainActivity;
import com.akw.crimson.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.File;
import java.util.Hashtable;

public class FinalRegister extends BaseActivity {

    DatabaseReference databaseReference = FirebaseDatabase.getInstance()
            .getReferenceFromUrl(Constants.FIREBASE_REALTIME_DATABASE_MSG_URL);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_final);
        new SharedPrefManager(getApplicationContext());
        String name = getIntent().getExtras().getString(Constants.Intent.KEY_INTENT_USERNAME);
        String email = getIntent().getExtras().getString(Constants.Intent.KEY_INTENT_EMAIL);
        String profilePic = getIntent().getExtras().getString(Constants.Intent.KEY_INTENT_PIC);
        String about = getIntent().getExtras().getString(Constants.Intent.KEY_INTENT_ABOUT);
        String status = getIntent().getExtras().getString(Constants.Intent.KEY_INTENT_RESULT_STATUS);

        Log.i("USERNAME_::::", name);
        makeCall(profilePic, name, email, about);

        executeEssentials();

    }

    private void executeEssentials() {


        makeDefaultPic();


    }

    private void makeDefaultPic() {
        // Get the Drawable object for your vector drawable
        Drawable vectorDrawable = getResources().getDrawable(R.drawable.ic_baseline_person_24);

// Calculate the desired width and height
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int originalWidth = vectorDrawable.getIntrinsicWidth();
        int originalHeight = vectorDrawable.getIntrinsicHeight();
        float aspectRatio = (float) originalHeight / originalWidth;
        int width = screenWidth;
        int height = Math.round(width * aspectRatio);

// Create a new Bitmap object
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

// Create a Canvas object with the bitmap
        Canvas canvas = new Canvas(bitmap);

// Draw the vector drawable onto the canvas
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);

// Now, the bitmap contains the vector drawable as a raster image
        File file = UsefulFunctions.FileUtil.makeOutputMediaFile(this, false, Constants.Media.KEY_MESSAGE_MEDIA_TYPE_PROFILE, Constants.Media.DEFAULT_PROFILE_PIC_NAME);
        UsefulFunctions.FileUtil.saveImage(bitmap, false, file);
    }

    private void makeCall(String profilePic, String userName, String email, String about) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        String userID = SharedPrefManager.getLocalUserID();
        String phone = SharedPrefManager.getLocalPhoneNumber();

        Hashtable<String, Object> data = new Hashtable<>();
        data.put(Constants.KEY_FIRESTORE_USER_NAME, userName);
        if (profilePic != null) {
            data.put(Constants.KEY_FIRESTORE_USER_PIC, profilePic);
        }
        data.put(Constants.KEY_FIRESTORE_USER_EMAIL, email);
        data.put(Constants.KEY_FIRESTORE_USER_ABOUT, about);
        data.put(Constants.KEY_FIRESTORE_USER_PHONE, phone.replaceAll(" ", ""));

        firebaseFirestore.collection(Constants.KEY_FIRESTORE_USERS)
                .document(userID).set(data, SetOptions.merge())
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getApplicationContext(), "User Registered on Crimson!", Toast.LENGTH_SHORT).show();
                    SharedPrefManager.storeUserProfile(profilePic, userName, email, about);
                    Log.i("USERNAME::::", userName);
                    Communicator.localDB.insertUser(SharedPrefManager.getLocalUser());

                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    finish();
                })
                .addOnFailureListener(documentReference -> {
                    Toast.makeText(getApplicationContext(), "User Registration Failed", Toast.LENGTH_SHORT).show();
                });

        makeFireRealtimeDBCall();
    }

    private void makeFireRealtimeDBCall() {

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