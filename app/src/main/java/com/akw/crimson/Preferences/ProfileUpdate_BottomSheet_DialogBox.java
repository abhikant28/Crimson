package com.akw.crimson.Preferences;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.SharedPrefManager;
import com.akw.crimson.MainActivity;
import com.akw.crimson.Preferences.EditProfile;
import com.akw.crimson.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Hashtable;

public class ProfileUpdate_BottomSheet_DialogBox extends BottomSheetDialogFragment {

    Button b_save,b_cancel;
    TextView tv_title;
    EditText et_input;
    EditProfile act;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.activity_profile_update_bottom_sheet_dialog_box, container,false);

        new SharedPrefManager(v.getContext());
        act = (EditProfile) getActivity();
        b_cancel=v.findViewById(R.id.profileEdit_bottomDialog_b_cancel);
        b_save=v.findViewById(R.id.profileEdit_bottomDialog_b_save);
        tv_title=v.findViewById(R.id.profileEdit_bottomDialog_tv_title);
        et_input=v.findViewById(R.id.profileEdit_bottomDialog_et_input);

        setValue();


        b_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!et_input.getText().toString().trim().equals("")){
                    switch (getArguments().getString(Constants.KEY_FRAGMENT_TYPE)){
                        case Constants.KEY_FRAGMENT_TYPE_NAME:
                            User user=SharedPrefManager.getLocalUser();
                            user.setName(et_input.getText().toString().trim());
                            SharedPrefManager.storeUser(user);
                            FirebaseFirestore firebaseFirestore= FirebaseFirestore.getInstance();
                            Hashtable<String, Object> data = new Hashtable<>();
                            data.put(Constants.KEY_FIRESTORE_USER_NAME, et_input.getText().toString().trim());
                            firebaseFirestore.collection(Constants.KEY_FIRESTORE_USERS)
                                    .document(SharedPrefManager.getLocalUserID()).set(data, SetOptions.merge())
                                    .addOnSuccessListener(documentReference -> {
                                        if(act != null)act.tv_userName.setText(et_input.getText().toString());
                                        dismiss();
                                    })
                                    .addOnFailureListener(documentReference -> {
                                    });
                            break;
                        case Constants.KEY_FRAGMENT_TYPE_ABOUT:
                            tv_title.setText("Something about yourself");
                            break;
                    }
                }
            }
        });
        b_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });


        return v;
    }

    private void setValue() {
        switch (getArguments().getString(Constants.KEY_FRAGMENT_TYPE)){
            case Constants.KEY_FRAGMENT_TYPE_NAME:
                tv_title.setText("Enter your name");
                et_input.setText(act.tv_userName.getText().toString());
                break;
            case Constants.KEY_FRAGMENT_TYPE_ABOUT:
                tv_title.setText("Something about yourself");
                et_input.setText(act.tv_status.getText().toString());
                break;
        }
    }

}












