package com.akw.crimson;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Communications.Communicator;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.Utilities.SelectContact;
import com.akw.crimson.databinding.ActivityImportChatBinding;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImportChat extends AppCompatActivity {
    ActivityImportChatBinding binding;
    String filePath;
    User user;
    String[] userList;

    private static final String MESSAGE_REGEX = "(\\d{2}/\\d{2}/\\d{4}, \\d{2}:\\d{2}) - \\[([^\\]]+)\\]: (.*)";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && requestCode == Constants.Intent.KEY_INTENT_REQUEST_CODE_CONTACT) {
            user = Communicator.localDB.getUser(data.getStringExtra(Constants.Intent.KEY_INTENT_USERID));

            binding.importChatTvUserName.setText("User : " + user.getDisplayName());
            Log.i("CreateGroup.USERS ::::::", user.toString());
            checkForUsers();
        }
        if (requestCode == Constants.Intent.KEY_INTENT_REQUEST_CODE_DOCUMENT) {

            filePath = data.getData().getPath();
            String f = UsefulFunctions.FileUtil.getFileName(this, data.getData(), true);
            Log.i("DOCUMENT URI CODE::::::", "....FOUND...." + f);
            binding.importChatTvFileName.setText("File : " + f);
            selectContact();
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImportChatBinding.inflate(getLayoutInflater());
        View v = binding.getRoot();
        setContentView(v);
        initialize();

        Toast.makeText(this, "Select File to import from messages", Toast.LENGTH_LONG).show();

        selectFile();

    }

    private void checkForUsers() {

        int messageCount = 0;
        HashSet<String> users = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null && messageCount < 50) {
                if (line.matches("\\d{2}/\\d{2}/\\d{4}, \\d{2}:\\d{2} - \\[([^\\]]+)]: .*")) {
                    String[] parts = line.split(" - \\[");
                    String author = parts[1].substring(0, parts[1].length() - 1);
                    users.add(author);
                    messageCount++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        userList = (String[]) users.toArray();
        for (int i = 0; i < userList.length; i++) {
            RadioButton rb = new RadioButton(this);
            rb.setText(userList[i]);
            rb.setId(i);
            binding.importChatRgUsersFound.addView(rb);
        }
    }

    private void initialize() {
        binding.importChatBtnImportMsg.setOnClickListener(view -> {

            if (binding.importChatRgUsersFound.getCheckedRadioButtonId() != -1) {
                int selectedUser = binding.importChatRgUsersFound.getCheckedRadioButtonId();

                startImport(filePath, user, selectedUser);
                // Use the selectedUser value as needed
            } else {
                Toast.makeText(this, "Select the user name as found in the import file", Toast.LENGTH_SHORT).show();
                // No radio button is selected
            }
        });
    }

    private void selectFile() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, Constants.Intent.KEY_INTENT_REQUEST_CODE_DOCUMENT);
    }

    private void selectContact() {
        Intent intent = new Intent(getApplicationContext(), SelectContact.class);
        intent.putExtra(Constants.Intent.KEY_INTENT_TYPE, Constants.Intent.KEY_INTENT_TYPE_SINGLE_SELECT);
        startActivityForResult(intent, Constants.KEY_ACTIVITY_RESULT_CONTACT_SELECT);
    }


    public void startImport(String path, User user, int selectedUser) {

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                parseMessage(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    private static void parseMessage(String line) {
        Pattern pattern = Pattern.compile(MESSAGE_REGEX);
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) {
            String timestamp = matcher.group(1);
            String author = matcher.group(2);
            String message = matcher.group(3);

            System.out.println("Timestamp: " + timestamp);
            System.out.println("Author: " + author);
            System.out.println("Message: " + message);
            System.out.println("--------------------");
        }
    }
}