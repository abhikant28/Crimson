package com.akw.crimson;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Communications.Communicator;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.TheViewModel;
import com.akw.crimson.Backend.ImportChatService;
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.Utilities.SelectContact;
import com.akw.crimson.databinding.ActivityImportChatBinding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Calendar;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImportChat extends AppCompatActivity {
    ActivityImportChatBinding binding;
    String filePath;
    User user;
    String[] userList;
    TheViewModel db;
    File file;


    int checkCount = 50;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("CreateGroup.USERS ::::::", requestCode + "_" + resultCode);

        if (requestCode == Constants.Intent.KEY_INTENT_REQUEST_CODE_CONTACT) {
            user = db.getUser(data.getStringExtra(Constants.Intent.KEY_INTENT_USERID));

            binding.importChatTvUserName.setText("User : " + user.getDisplayName());

        }
        if (requestCode == Constants.Intent.KEY_INTENT_REQUEST_CODE_DOCUMENT) {

            String f = UsefulFunctions.FileUtil.getFileName(this, data.getData(), true);

            file = UsefulFunctions.FileUtil.saveFileInternalStorage(this, data.getData(), Calendar.getInstance().getTime().getTime() + "");
            assert file != null;
            filePath = file.getAbsolutePath();

            binding.importChatTvFileName.setText(f);
            checkForUsers();
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

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            String regex = "^(\\d{2}/\\d{2}/\\d{4}), (\\d{2}:\\d{2}) - (.*?): (.*)$";
            Pattern pattern = Pattern.compile(regex);

            while ((line = br.readLine()) != null && messageCount < checkCount) {
                messageCount++;
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String name = matcher.group(3);
                    users.add(name);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        userList = users.toArray(new String[0]);

        binding.importChatRgUsersFound.removeAllViews();
        for (int i = 0; i < userList.length; i++) {
            RadioButton rb = new RadioButton(this);
            rb.setText(userList[i]);
            rb.setId(i);
            rb.setTextColor(Color.WHITE);
            rb.setTextSize(20);
            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{
                            new int[]{-android.R.attr.state_checked},
                            new int[]{android.R.attr.state_checked}
                    },
                    new int[]{

                            Color.WHITE
                            , Color.YELLOW,
                    }
            );

            binding.importChatRgUsersFound.addView(rb);
        }
        checkCount += checkCount;
        binding.importChatTvReCheck.setVisibility(View.VISIBLE);
        binding.importChatTvReCheck.setOnClickListener(view -> {
            checkForUsers();
        });
    }


//    private void importMessages(int selectedUser) {
//
//        int messageCount = 0;
//
//        Log.i("Import.checkForUser::::", filePath);
//        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
//            String line;
//            String regex = "^(\\d{2}/\\d{2}/\\d{4}), (\\d{2}:\\d{2}) - (.*?): (.*)$";
//            Pattern pattern = Pattern.compile(regex);
//
//            Message msg = null;
//            while ((line = br.readLine()) != null && messageCount < 5) {
//                Log.i("ImportChat.checkForUsers ::::::", line);
//                Matcher matcher = pattern.matcher(line);
//                if (matcher.find()) {
//                    messageCount++;
//                    String date = matcher.group(1);
//                    String time = matcher.group(2);
//                    String name = matcher.group(3);
//                    String data = matcher.group(4);
//                    msg = parseMessage(date + ", " + time + ":00", data, name, userList[selectedUser], messageCount);
//
////                    Log.i("ImportChat.VALUES::::::::", "Date: " + date + ", Time: " + time + ", User: " + name + ", Message: " + data);
//                    db.insertMessage(msg);
//                } else {
//                    if (msg != null) {
//                        msg.setMsg(msg.getMsg() + "\n" + line);
//                    }
//                    db.updateMessage(msg);
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
////        Log.i("checkForUsers ::::::", Arrays.deepToString(userList));
//    }


//    private Message parseMessage(String timestamp, String data, String author, String userName, int count) {
//
//        Message msg = new Message(SharedPrefManager.getLocalUserID(), user.getUser_id(), data, false, null, SharedPrefManager.getLocalUserID());
//        Log.i("Import.parseMessage::::::", count + "_" + timestamp + ":" + author + " :");
//        if (author.equals(userName)) {
//            msg.setStatus(Constants.Message.MESSAGE_STATUS_READ);
//            msg.setSelf(false);
//        } else {
//            msg.setSelf(true);
//            msg.setSentTime(UsefulFunctions.convertToTimestamp(timestamp));
//        }
//        return msg;
//    }

    private void selectFile() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, Constants.Intent.KEY_INTENT_REQUEST_CODE_DOCUMENT);
    }


    private void selectContact() {
        Intent intent = new Intent(getApplicationContext(), SelectContact.class);
        intent.putExtra(Constants.Intent.KEY_INTENT_TYPE, Constants.Intent.KEY_INTENT_TYPE_SINGLE_SELECT);
        startActivityForResult(intent, Constants.Intent.KEY_INTENT_REQUEST_CODE_CONTACT);
    }


    private void initialize() {

        db = Communicator.localDB;
        binding.importChatBtnImportMsg.setOnClickListener(view -> {

            if (binding.importChatRgUsersFound.getCheckedRadioButtonId() != -1) {
                int selectedUser = binding.importChatRgUsersFound.getCheckedRadioButtonId();


                Intent serviceIntent = new Intent(this, ImportChatService.class);
                serviceIntent.putExtra(Constants.Intent.KEY_INTENT_USERNAME, userList[selectedUser]);
                serviceIntent.putExtra(Constants.Intent.KEY_INTENT_FILE_PATH, filePath);
                serviceIntent.putExtra(Constants.Intent.KEY_INTENT_USERID, user.getUser_id());

                ContextCompat.startForegroundService(this, serviceIntent);
                finish();

                // Use the selectedUser value as needed
            } else {
                Toast.makeText(this, "Select the user name as found in the import file", Toast.LENGTH_SHORT).show();
                // No radio button is selected
            }
        });
        binding.importChatTvFileName.setOnClickListener(view -> {
            selectFile();
        });
        binding.importChatTvUserName.setOnClickListener(view -> {
            selectContact();
        });
    }

}