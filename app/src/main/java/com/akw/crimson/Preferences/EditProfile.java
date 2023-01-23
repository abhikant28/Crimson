package com.akw.crimson.Preferences;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.SharedPrefManager;
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class EditProfile extends AppCompatActivity {

    ImageView iv_profilePic;
    CardView cv_profilePic;
    public TextView tv_userName,tv_status;
    TextView tv_phone;
    LinearLayout ll_about,ll_name,ll_phone;
    ActionBar ab;
    FirebaseFirestore fireStore=FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        new SharedPrefManager(this);

        init();
        clicks();
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
                            String encodedImage = UsefulFunctions.encodeImage(bitmap);
                            updateProfilePic(encodedImage,bitmap);
                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private void updateProfilePic(String encodedImage, Bitmap bitmap) {
        FirebaseFirestore firestore=FirebaseFirestore.getInstance();
        firestore.collection(Constants.KEY_FIRESTORE_USERS).document(SharedPrefManager.getLocalUserID())
                .update(Constants.KEY_FIRESTORE_USER_PIC,encodedImage)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        User user=SharedPrefManager.getLocalUser();
                        user.setPic(encodedImage);
                        SharedPrefManager.storeUser(user);
                        iv_profilePic.setImageBitmap(bitmap);
                    }
                });
    }

    private void clicks() {
        cv_profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                pickImage.launch(intent);
            }
        });
        ll_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProfileUpdate_BottomSheet_DialogBox update= new ProfileUpdate_BottomSheet_DialogBox();
                Bundle bundle=new Bundle();
                bundle.putString(Constants.KEY_FRAGMENT_TYPE,Constants.KEY_FRAGMENT_TYPE_NAME );
                update.setArguments(bundle);
                update.show(getSupportFragmentManager(), "EXAMPLE");
            }
        });
        ll_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProfileUpdate_BottomSheet_DialogBox update= new ProfileUpdate_BottomSheet_DialogBox();
                Bundle bundle=new Bundle();
                bundle.putString(Constants.KEY_FRAGMENT_TYPE,Constants.KEY_FRAGMENT_TYPE_ABOUT );
                update.setArguments(bundle);
                update.show(getSupportFragmentManager(), "EXAMPLE");
            }
        });
    }

    private void init(){
        tv_phone=findViewById(R.id.editProfile_tv_phoneNum);
        tv_status=findViewById(R.id.editProfile_tv_status);
        tv_userName=findViewById(R.id.editProfile_tv_name);
        iv_profilePic=findViewById(R.id.editProfile_ImageView_UserPic);
        cv_profilePic=findViewById(R.id.editProfile_cv_UserPic);
        ll_about=findViewById(R.id.editProfile_ll_about);
        ll_name=findViewById(R.id.editProfile_ll_name);
        ll_phone=findViewById(R.id.editProfile_ll_phone);
        ab=getSupportActionBar();

        User user = SharedPrefManager.getLocalUser();
        if(user.getPic()!=null){
            iv_profilePic.setImageBitmap(UsefulFunctions.decodeImage(user.getPic()));
        }else{
            iv_profilePic.setImageResource(R.drawable.ic_baseline_person_24);
        }
        tv_userName.setText(SharedPrefManager.getLocalUser().getUserName());
        tv_phone.setText(SharedPrefManager.getLocalUser().getPhoneNumber());
        ab.setTitle("Profile");
    }
}


















