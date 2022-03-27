package com.akw.crimson.Registration;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.akw.crimson.R;

import java.net.PasswordAuthentication;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Registration_Email extends AppCompatActivity {

    public boolean sendMail(String msg){
        final String username = "yourname@yourcompany.com";
        final String password = "yourpassword";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.example.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password.toCharArray());
            }
        });

        try {

            Message message = new MimeMessage(session);
            ((MimeMessage) message).setFrom(new InternetAddress("to@yourdomain.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse("to@yourdomain.com"));
            message.setSubject("9$$#AA#$$9");
            message.setText(msg);

            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_email);



    }

}