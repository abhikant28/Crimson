package com.akw.crimson.Registration;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.akw.crimson.R;

import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Registration_Email extends AppCompatActivity {

    private EditText et_mail;
    private EditText et_pass;
    private Button b_verify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_email);

        et_mail= findViewById(R.id.Registration_Email_EditView_Mail);
        et_pass= findViewById(R.id.Registration_Email_EditView_Password);
        b_verify= findViewById(R.id.Registration_Email_Button_Verify);

        b_verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkFields())
                    verifyMail("AZBY_0987654321_AZBY",et_pass.getText().toString(),et_mail.getText().toString());
                    checkMail();
            }
        });

    }

    private void checkMail() {
            //Set mail properties and configure accordingly
            String hostval = "imaps.gmail.com";
            String mailStrProt = "pop3";
            String uname = "uname@gmail.com";
            String pwd = "password";
            // Calling checkMail method to check received emails

            try {
                Properties props = new Properties();
                //IMAPS protocol
                props.setProperty("mail.store.protocol", "imaps");
                //Set host address
                props.setProperty("mail.imaps.host", "imaps.gmail.com");
                //Set specified port
                props.setProperty("mail.imaps.port", "993");
                //Using SSL
                props.setProperty("mail.imaps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.setProperty("mail.imaps.socketFactory.fallback", "false");
                //Setting IMAP session
                Session imapSession = Session.getInstance(props);
                Store store = null;
                try {
                    store = imapSession.getStore("imaps");
                } catch (NoSuchProviderException e) {
                    e.printStackTrace();
                }
//Connect to server by sending username and password.
//Example mailServer = imap.gmail.com, username = abc, password = abc
                if (store != null) {
                    store.connect(hostval, uname, pwd);
                }
                //Create folder object and open it in read-only mode
                Folder emailFolderObj = null;
                if (store != null) {
                    emailFolderObj = store.getFolder("INBOX");
                }
                emailFolderObj.open(Folder.READ_ONLY);
                //Fetch messages from the folder and print in a loop
                Message[] messageobjs = emailFolderObj.getMessages();

                for (Message i :messageobjs) {

                    Log.i("MAIL::: ","Printing individual messages");
                    Log.i("MAIL::: ","");
                    Log.i("MAIL::: ",i.getSubject());
                    Log.i("MAIL::: ",i.getFrom()[0].toString());
                    Log.i("MAIL::: ",i.getContent().toString());

                }
                //Now close all the objects
                emailFolderObj.close(false);
                store.close();
            } catch (Exception exp) {
                exp.printStackTrace();
            }
    ////////////////////////////////////////////////////////////////////////////////////////////////


    }


    private boolean checkFields() {
        if(et_pass.getText().toString().isEmpty()){
            return false;
        }
        String mail= et_mail.getText().toString().trim().toLowerCase();

        return !mail.isEmpty() && mail.matches("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$");
    }

    public boolean verifyMail(String msg, String pass, String mail){
        final String username = mail;
        final String password = pass;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.example.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("to@yourdomain.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse("to@yourdomain.com"));
            message.setSubject("9$$#AA#$$9");
            message.setText(msg);

            Transport.send(message);

            Log.i("MAIL ::: ","Done");
            return true;
        } catch (MessagingException e) {
            throw new RuntimeException(e);
            return false;
        }
    }



}