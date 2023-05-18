package com.akw.crimson.Backend;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.akw.crimson.R;

public class NotificationHelper extends ContextWrapper {
    public static final String channel1ID = "channel1111";
    public static final String channel1Name = "Channel 1";
    public static final String channel2ID = "channel1210";
    public static final String channel2Name = "Channel 2";

    private NotificationManager mManager;

    public NotificationHelper(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            createChannels();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void createChannels() {
        NotificationChannel channel1 = new NotificationChannel(channel1ID, channel1Name, NotificationManager.IMPORTANCE_DEFAULT);
        channel1.enableLights(true);
        channel1.enableVibration(true);
        channel1.setLightColor(R.color.design_default_color_primary);
        channel1.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationChannel channel2 = new NotificationChannel(channel2ID, channel2Name, NotificationManager.IMPORTANCE_HIGH);
        channel2.enableLights(true);
        channel2.enableVibration(true);
        channel2.setLightColor(R.color.design_default_color_primary);
        channel2.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
    }

    public NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    public NotificationCompat.Builder getChannel1Notification(String to) {
        return new NotificationCompat.Builder(getApplicationContext(), channel1ID)
                .setContentTitle("Prepared Message")
                .setContentText("Message sent to " + to)
                .setSmallIcon(R.drawable.ic_launcher_foreground);
    }

    public NotificationCompat.Builder getChannel2Notification(String title, String message) {
        return new NotificationCompat.Builder(getApplicationContext(), channel2ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher_foreground);
    }
}
