package com.akw.crimson.Preferences;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.cardview.widget.CardView;

import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Communications.Communicator;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.SharedPrefManager;
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.BaseActivity;
import com.akw.crimson.R;
import com.akw.crimson.databinding.ActivityEditProfileBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;

@RequiresApi(api = Build.VERSION_CODES.N)
public class EditProfile extends BaseActivity {

    private static final int PRIVATE_PROFILE_PIC = 1;
    private static final int PUBLIC_PROFILE_PIC = 2;
    ImageView iv_profilePic;
    CardView cv_profilePic;
    public TextView tv_userName, tv_status;
    TextView tv_phone;
    LinearLayout ll_about, ll_name, ll_phone;
    ActionBar ab;
    ActivityEditProfileBinding layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layout = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(layout.getRoot());

        init();
        clicks();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent result) {
        super.onActivityResult(requestCode, resultCode, result);

        if (resultCode == RESULT_OK && requestCode == PUBLIC_PROFILE_PIC) {
            if (result.getData() != null) {
                Uri imageUri = result.getData();
                Bitmap bitmap = UsefulFunctions.FileUtil.getImageFromUri(this, imageUri);
                assert bitmap != null;
                String encodedImage = UsefulFunctions.encodeImage(bitmap);
                updatePublicProfilePic(encodedImage, bitmap);
            }
        } else if (resultCode == RESULT_OK && requestCode == PRIVATE_PROFILE_PIC) {
            User u = SharedPrefManager.getLocalUser();
            File file = UsefulFunctions.FileUtil.makeOutputMediaFile(this, true, Constants.Media.KEY_MESSAGE_MEDIA_TYPE_PROFILE, "self");
            UsefulFunctions.FileUtil.saveFile(this, result.getData(), file);
            u.setProfilePic(file.getName());
            Log.i("ActivityResult::::",file.getName()+"____"+result.getData() );
            SharedPrefManager.storeUser(u);
            layout.editProfileImageViewPrivateUserPic.setImageBitmap(u.getUserPicBitmap(this));
            Communicator.updateProfilePic(this, file.getPath());
        }

    }

    private void updatePublicProfilePic(String encodedImage, Bitmap bitmap) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(Constants.KEY_FIRESTORE_USERS).document(SharedPrefManager.getLocalUserID())
                .update(Constants.KEY_FIRESTORE_USER_PIC, encodedImage)
                .addOnSuccessListener(unused -> {
                    User user = SharedPrefManager.getLocalUser();
                    user.setPublicPic(encodedImage);
                    SharedPrefManager.storeUser(user);
                    layout.editProfileIvPublicUserPic.setImageBitmap(bitmap);
                });
    }

    private void clicks() {

        ll_name.setOnClickListener(view -> {
            ProfileUpdate_BottomSheet_DialogBox update = new ProfileUpdate_BottomSheet_DialogBox();
            Bundle bundle = new Bundle();
            bundle.putString(Constants.KEY_FRAGMENT_TYPE, Constants.KEY_FRAGMENT_TYPE_NAME);
            update.setArguments(bundle);
            update.show(getSupportFragmentManager(), "EXAMPLE");
        });
        layout.editProfileLlStatus.setOnClickListener(view -> {
            ProfileUpdate_BottomSheet_DialogBox update = new ProfileUpdate_BottomSheet_DialogBox();
            Bundle bundle = new Bundle();
            bundle.putString(Constants.KEY_FRAGMENT_TYPE, Constants.KEY_FRAGMENT_TYPE_STATUS);
            update.setArguments(bundle);
            update.show(getSupportFragmentManager(), "EXAMPLE");
        });
        layout.editProfileLlAbout.setOnClickListener(view -> {
            ProfileUpdate_BottomSheet_DialogBox update = new ProfileUpdate_BottomSheet_DialogBox();
            Bundle bundle = new Bundle();
            bundle.putString(Constants.KEY_FRAGMENT_TYPE, Constants.KEY_FRAGMENT_TYPE_ABOUT);
            update.setArguments(bundle);
            update.show(getSupportFragmentManager(), "EXAMPLE");
        });

        layout.editProfileIvPublicUserPic.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, PUBLIC_PROFILE_PIC);
        });
        layout.editProfileImageViewPrivateUserPic.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, PRIVATE_PROFILE_PIC);
        });
    }

    private void init() {
        tv_phone = findViewById(R.id.editProfile_tv_phoneNum);
        tv_status = findViewById(R.id.editProfile_tv_status);
        tv_userName = findViewById(R.id.editProfile_tv_name);
        iv_profilePic = findViewById(R.id.editProfile_iv_publicUserPic);
        cv_profilePic = layout.editProfileCvPublicUserPic;
        ll_about = findViewById(R.id.editProfile_ll_about);
        ll_name = findViewById(R.id.editProfile_ll_name);
        ll_phone = findViewById(R.id.editProfile_ll_phone);
        ab = getSupportActionBar();

        User user = SharedPrefManager.getLocalUser();
        User u = SharedPrefManager.getLocalUser();
        layout.editProfileIvPublicUserPic.setImageBitmap(UsefulFunctions.decodeImage(u.getPublicPic()));
        Log.i(getClass() + ":::::", user.getProfilePic() + "");
        layout.editProfileImageViewPrivateUserPic.setImageBitmap(user.getUserPicBitmap(this));

        layout.editProfileTvStatus.setText(u.getStatus());
        layout.editProfileTvAbout.setText(u.getAbout());

        layout.editProfileTvName.setText(SharedPrefManager.getLocalUser().getUserName());
        tv_phone.setText(SharedPrefManager.getLocalUser().getPhoneNumber());
        ab.setTitle("Profile");

    }
}