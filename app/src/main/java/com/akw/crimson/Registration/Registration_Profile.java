package com.akw.crimson.Registration;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.akw.crimson.Database.SharedPrefManager;
import com.akw.crimson.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
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
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://crimson-ims-default-rtdb.asia-southeast1.firebasedatabase.app/");

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
                            int dimension=Math.min(bitmap.getHeight(),bitmap.getWidth());
                            bitmap = ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension);
                            iv_profilePic.setImageBitmap(bitmap);
                            tv_ImageText.setVisibility(View.GONE);
                            hasPic=true;
                            encodedImage=encodeImage(bitmap);
                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
            );

    private void checkFireStoreForProfile(){
        FirebaseFirestore firebaseFirestore= FirebaseFirestore.getInstance();
        String userID= new SharedPrefManager(getApplicationContext()).getLocalUserID();

        DocumentReference ref=firebaseFirestore.collection("users").document(userID);
        ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String phone=document.getString("phone");
                        String email=document.getString("email");
                        String name=document.getString("name");
                        String profilePic=document.getString("pic");
                        encodedImage=profilePic;
                        if(!encodedImage.isEmpty())hasPic=true;
                        et_mail.setText(email);
                        et_pass.setText(name);
                        Bitmap bitmap= decodeImage(profilePic);
                        iv_profilePic.setImageBitmap(bitmap);
//                        Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                    } else {
//                        Log.d("TAG", "No such document");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
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
                    String name= String.valueOf(snapshot.child("users").child(userID).child("name").getValue());
                    String email=String.valueOf(snapshot.child("users").child(userID).child("email").getValue());
                    String profilePic=String.valueOf(snapshot.child("users").child(userID).child("pic").getValue());
                    et_mail.setText(email);
                    et_pass.setText(name);
                    Bitmap bitmap= decodeImage(profilePic);
                    iv_profilePic.setImageBitmap(bitmap);

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

    private String encodeImage(Bitmap bitmap) {
        int previewWidth=150;
        int previewHeight=bitmap.getHeight()*previewWidth/bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] bytes= byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private Bitmap decodeImage(String input){
        byte[] decoded = Base64.decode(input, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
    }

    private void setViews() {
        et_mail = findViewById(R.id.Registration_Email_EditView_Mail);
        et_pass = findViewById(R.id.Registration_Email_EditView_Password);
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

        b_verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkFields()) {
                    //verifyMail("AZBY_0987654321_AZBY",et_pass.getText().toString(),et_mail.getText().toString());
                    //checkMail();
                    Intent intent = new Intent(getApplicationContext(), FinalRegister.class);
                    intent.putExtra("email", et_mail.getText().toString().toLowerCase().trim());
                    intent.putExtra("name", et_pass.getText().toString().trim());
                    if(hasPic){
                        Log.i("HAS PIC:::","TRUE");
                        intent.putExtra("encodedImg", encodedImage);
                    }else{
                        Log.i("HAS PIC:::","FALSE");

                    }
                    startActivity(intent);
                }

            }
        });
    }

}
