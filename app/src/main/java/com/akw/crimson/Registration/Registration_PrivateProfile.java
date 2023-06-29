package com.akw.crimson.Registration;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Communications.Communicator;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.SharedPrefManager;
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.BaseActivity;
import com.akw.crimson.R;
import com.akw.crimson.databinding.ActivityRegistrationPrivateProfileBinding;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;


public class Registration_PrivateProfile extends BaseActivity {

    private Intent forwardIntent;
    private ActivityRegistrationPrivateProfileBinding binding;
    private boolean hasPic = false;
    private String imageUri = null;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("resultCode:::::",resultCode+"");
        if(resultCode==RESULT_OK ){
            User u=SharedPrefManager.getLocalUser();
            File file= UsefulFunctions.FileUtil.makeOutputMediaFile(this,true, Constants.Media.KEY_MESSAGE_MEDIA_TYPE_PROFILE,"self");

            u.setProfilePic(file.getName());
            SharedPrefManager.storeUser(u);
            Communicator.updateProfilePic(this,file.getPath());

            binding.registrationPvtIvProfilePic.setImageURI(Uri.fromFile(file));
            binding.registrationPvtCheckBox.setChecked(false);
            binding.registrationPvtTvAddPic.setVisibility(View.GONE);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistrationPrivateProfileBinding.inflate(getLayoutInflater());
        View v = binding.getRoot();
        setContentView(v);

        setView();
        forwardIntent = new Intent(this, FinalRegister.class);
        forwardIntent.putExtra(Constants.Intent.KEY_INTENT_USERNAME, getIntent().getExtras().getString(Constants.Intent.KEY_INTENT_USERNAME));
        forwardIntent.putExtra(Constants.Intent.KEY_INTENT_EMAIL, getIntent().getExtras().getString(Constants.Intent.KEY_INTENT_EMAIL));
        forwardIntent.putExtra(Constants.Intent.KEY_INTENT_PIC, getIntent().getExtras().getString(Constants.Intent.KEY_INTENT_PIC));
        forwardIntent.putExtra(Constants.Intent.KEY_INTENT_ABOUT, getIntent().getExtras().getString(Constants.Intent.KEY_INTENT_ABOUT));
        forwardIntent.putExtra(Constants.Intent.KEY_INTENT_PIC, getIntent().getExtras().getString(Constants.Intent.KEY_INTENT_PIC));
        forwardIntent.putExtra(Constants.Intent.KEY_INTENT_FILE_PATH, getIntent().getExtras().getString(Constants.Intent.KEY_INTENT_FILE_PATH));
        forwardIntent.putExtra("isUser", getIntent().getExtras().getBoolean("isUser", false));

        checkFireStoreForProfile();

    }

    private void checkFireStoreForProfile() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    99);
        }
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        String userID = SharedPrefManager.getLocalUserID();

        DocumentReference ref = firebaseFirestore.collection(Constants.KEY_FIRESTORE_USERS).document(userID);
        ref.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String status = document.getString(Constants.KEY_FIRESTORE_USER_STATUS);
                    binding.registrationPvtEditViewStatus.setText(status);
                }
            } else {
                Log.d("TAG", "get failed with ", task.getException());
            }
        });
    }


    private void setView() {

        ActionBar ab = getSupportActionBar();
        ab.setTitle("Registration");
        binding.registrationPvtIvProfilePic.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent,99);
        });

        binding.registrationPvtCheckBox.setOnCheckedChangeListener((compoundButton, checked) -> {
            if (checked) {
                hasPic = true;
                imageUri = getIntent().getExtras().getString(Constants.Intent.KEY_INTENT_FILE_PATH);
                binding.registrationPvtIvProfilePic.setImageURI(Uri.parse(getIntent().getExtras().getString(Constants.Intent.KEY_INTENT_FILE_PATH)));
            } else {
                hasPic = false;
                binding.registrationPvtIvProfilePic.setImageResource(R.drawable.ic_baseline_person_24);
            }
        });
        binding.registrationPvtButtonSubmit.setOnClickListener(view -> {

            if (!binding.registrationPvtEditViewStatus.getText().toString().trim().isEmpty()) {
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                firestore.collection(Constants.KEY_FIRESTORE_USERS).document(SharedPrefManager.getLocalUserID())
                        .update(Constants.KEY_FIRESTORE_USER_STATUS, binding.registrationPvtEditViewStatus.getText().toString().trim())
                        .addOnSuccessListener(unused -> {
                            User user = SharedPrefManager.getLocalUser();
                            user.setStatus(binding.registrationPvtEditViewStatus.getText().toString().trim());
                            SharedPrefManager.storeUser(user);
                        });
                forwardIntent.putExtra(Constants.Intent.KEY_INTENT_STATUS, binding.registrationPvtEditViewStatus.getText().toString().trim());
            }
            if (hasPic && imageUri != null) {
                Communicator.updateProfilePic(this, imageUri);
            }
            startActivity(forwardIntent);
        });

    }


}