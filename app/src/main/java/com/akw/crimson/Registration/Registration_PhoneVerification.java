package com.akw.crimson.Registration;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.SharedPrefManager;
import com.akw.crimson.MainActivity;
import com.akw.crimson.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class Registration_PhoneVerification extends AppCompatActivity {

    private String number = "INVALID";

    private TextView tv_phoneNumber;
    private TextView tv_changeNumber;
    private EditText et_OTP;
    private Button b_verify;
    String otpId = "";
    private FirebaseAuth mAuth;

    @Override
    public void onBackPressed() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_phone_verification);


        if (getIntent() != null) {
            number = getIntent().getStringExtra(Constants.KEY_INTENT_PHONE);
        }
        if (number.equals("INVALID")) {
            finish();
        }

        mAuth = FirebaseAuth.getInstance();
        generateOTP(number, this);

        setViews();

    }

    public void generateOTP(String num, Activity cxt) {

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(num.replaceAll(" ", ""))       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(cxt)                 // Activity (for callback binding)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                otpId = s;
                                Toast.makeText(getApplicationContext(), "Code Sent", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                signInWithPhoneAuthCredential(phoneAuthCredential);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                et_OTP.setError("Incorrect OTP");
                                et_OTP.setText("");
                            }
                        })          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = task.getResult().getUser();
                            if (user != null) {
                                Log.i("USERID::::", user.getUid());
                                new SharedPrefManager(getApplicationContext()).storeUserNumber(number,user.getUid());
                            }

                            // Update UI

                            Toast.makeText(getApplicationContext(), "Verified!", Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(getApplicationContext(), Registration_Profile.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();

                        } else {
                            // Sign in failed, display a message and update the UI
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                et_OTP.setError("Incorrect OTP");
                                et_OTP.setText("");
                            }
                        }
                    }
                });
    }


    private void setViews() {
        tv_phoneNumber = findViewById(R.id.Registration_PhoneVerification_TextView_Number);
        tv_changeNumber = findViewById(R.id.Registration_PhoneVerification_TextView_Change);
        et_OTP = findViewById(R.id.Registration_PhoneVerification_EditText_OTP);
        b_verify = findViewById(R.id.Registration_PhoneVerification_Button_Verify);


        tv_phoneNumber.setText("Verify " + number);

        b_verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (et_OTP.getText().toString().trim().isEmpty()) {
                    et_OTP.setError("Field Cannot be Empty");
                } else if (et_OTP.getText().toString().trim().length() != 6) {
                    et_OTP.setError("Invalid OTP");
                } else {
                    String otp = et_OTP.getText().toString().trim();
                    signInWithPhoneAuthCredential(PhoneAuthProvider.getCredential(otpId, otp));
                }
            }
        });

        tv_changeNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

                builder.setMessage("An OTP has been sent to the number: +91"+number
                                +". Do you wish to change the number you have provided for verification? " +
                                "(This can only be done a few times)")
                        .setTitle("Change number?");

                builder.setPositiveButton("Change number", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        number=null;
                        Intent intent = new Intent(getApplicationContext(), Registration_Phone.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                AlertDialog alertDialog = builder.create();
                // Show the Alert Dialog box
                alertDialog.show();
            }
        });
    }

}