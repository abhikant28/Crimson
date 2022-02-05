package com.example.crimson.Registration;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.crimson.R;

public class Registration_PhoneVerification extends AppCompatActivity {

    private String number="INVALID";
    private RequestQueue queue;

    private TextView tv_phoneNumber;
    private TextView tv_changeNumber;
    private EditText et_OTP;
    private Button b_verify;
    private int kon =0;
    private String docen;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_phone_verification);

        if(getIntent()!=null){
            number = getIntent().getStringExtra("NUMBER");
        }
        if(number.equals("INVALID")){
            finish();
        }

        tv_phoneNumber=findViewById(R.id.Registration_PhoneVerification_TextView_Number);
        tv_changeNumber= findViewById(R.id.Registration_PhoneVerification_TextView_Change);
        et_OTP=findViewById(R.id.Registration_PhoneVerification_EditText_OTP);
        b_verify= findViewById(R.id.Registration_PhoneVerification_Button_Verify);

        b_verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(et_OTP.getText().toString().matches("\\d+")){
                    String otp = et_OTP.getText().toString();
                    int check = makeTheCall(otp);
                    if(check==5785){
                        SharedPreferences sharedPreferences= getSharedPreferences()
                        startActivity(new Intent(getApplicationContext(),Registration_Email.class));
                    }else if(check == 6118){
                        Toast.makeText(getApplicationContext(),"Network Unavailable",Toast.LENGTH_SHORT).show();
                    }else{
                        kon++;
                        Toast.makeText(getApplicationContext(), "Incorrect Code, "+(3- kon)+" attempts left.", Toast.LENGTH_SHORT).show();
                        if(kon >2){
                            finish();
                        }
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Invalid Code", Toast.LENGTH_SHORT).show();
                    et_OTP.setText("");
                }

            }
        });
        tv_changeNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        tv_phoneNumber.setText("Verify "+number);
    }
    public int makeTheCall(String otp){
        queue = Volley.newRequestQueue(getApplicationContext());
        String url =""+otp;
        final int[] val = new int[1];
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {



                        if(){
                            docen="TOKEN";
                            val[0] = 5785;
                        }else{
                            docen=null;
                            val[0] =-1;
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                val[0]=6118;
                error.printStackTrace();
            }
        });

        stringRequest.setTag("TAG");
        queue.add(stringRequest);
        return val[0];
    }

}