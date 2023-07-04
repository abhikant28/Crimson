package com.akw.crimson.Registration;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.akw.crimson.R;

public class Registration_Main extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_main);

        Button acptCountinue= findViewById(R.id.Registration_Main_Button_Accept);

        acptCountinue.setOnClickListener(view ->
                startActivity(new Intent(view.getContext(),Registration_Phone.class)));

    }
}