package com.akw.crimson.Backend;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Database.SharedPrefManager;
import com.akw.crimson.Backend.Database.TheViewModel;
import com.akw.crimson.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImportChatService extends Service {

    private static final String CHANNEL_ID = "ForegroundServiceChannel";
    private TheViewModel db;
    File file;
    User user;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        Notification notification = createNotification();
        startForeground(1, notification);

        db = new TheViewModel(getApplication());

//        Log.i("ImportService.onStartCommand:::::::", "STARTED");
        String uri = intent.getStringExtra(Constants.Intent.KEY_INTENT_FILE_PATH);
        String userName = intent.getStringExtra(Constants.Intent.KEY_INTENT_USERNAME);
        user = db.getUser(intent.getStringExtra(Constants.Intent.KEY_INTENT_USERID));
        // Perform your long-running operation or background task here
        file = new File(uri);
        importMessages(userName);
        // Return START_STICKY to ensure that the service keeps running even if it is killed by the system
        return START_STICKY;
    }


    private void importMessages(String selectedUser) {
//        Log.i("ImportService.importMessages:::::::", "STARTED");

        int messageCount = 0;

//        Log.i("Import.checkForUser::::", filePath);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            String regex = "^(\\d{2}/\\d{2}/\\d{4}), (\\d{2}:\\d{2}) - (.*?): (.*)$";
            Pattern pattern = Pattern.compile(regex);
//            Log.i("ImportService.importMessages:::::::", "Reading File");

            Message msg = null;
            while ((line = br.readLine()) != null) {
//                Log.i("ImportChat.checkForUsers ::::::", line);
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    messageCount++;
                    String date = matcher.group(1);
                    String time = matcher.group(2);
                    String name = matcher.group(3);
                    String data = matcher.group(4);
                    msg = parseMessage(date + ", " + time + ":00", data, name, selectedUser, messageCount);
                    updateNotificationCount(messageCount);


//                    Log.i("ImportChat.VALUES::::::::", "Date: " + date + ", Time: " + time + ", User: " + name + ", Message: " + data);
                    db.insertMessage(msg);
                } else {
                    if (msg != null) {
                        msg.setMsg(msg.getMsg() + "\n" + line);
                    }
                    db.updateMessage(msg);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            updateNotificationCount(-1);
            file.delete();
            stopSelf();
        }
    }


    private Message parseMessage(String timestamp, String data, String author, String userName, int count) {

        Message msg = new Message(SharedPrefManager.getLocalUserID(), user.getUser_id(), data, false, null, SharedPrefManager.getLocalUserID());
//        Log.i("Import.parseMessage::::::", count + "_" + timestamp + "::" + author + " ::" + data);
        if (author.equals(userName)) {
            msg.setSelf(false);
            msg.setReceivedTime(UsefulFunctions.convertToTimestamp(timestamp));
        } else {
            msg.setSelf(true);
            msg.setSentTime(UsefulFunctions.convertToTimestamp(timestamp));
        }
        msg.setStatus(Constants.Message.MESSAGE_STATUS_READ);
        return msg;
    }


    private void updateNotificationCount(int newCount) {

//        Log.i("ImportService.UpdateNotificationCount:::::::", "Count::" + newCount);

        // Update the notification with the new count
        NotificationManager manager = getSystemService(NotificationManager.class);
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            // For older versions without channels
            builder = new Notification.Builder(this);
        }

        if (newCount != -1) {
            builder.setContentTitle("Importing Messages (" + user.getDisplayName() + ")")
                    .setContentText(newCount + " Messages Imported")
                    .setSmallIcon(R.drawable.ic_baseline_message_24);
        } else {
            builder.setContentTitle("Import Completed (" + user.getDisplayName() + ")")
                    .setContentText(newCount + " Messages Imported")
                    .setSmallIcon(R.drawable.ic_baseline_check_24);
        }

        Notification notification = builder.build();

        manager.notify(1, notification);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop any ongoing tasks or cleanup resources here
    }

    @Override
    public IBinder onBind(Intent intent) {
        // This service does not support binding, so return null
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Import Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            // For older versions without channels
            builder = new Notification.Builder(this);
        }

        builder.setContentTitle("Importing Messages")
                .setContentText("Service is running...")
                .setSmallIcon(R.drawable.ic_baseline_message_24);

        return builder.build();
    }
}
