package com.victorman.memorycards;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {
    public static final String NOTIFICATION_CHANNEL_ID = "notification_channel";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChanel();
    }


    private void createNotificationChanel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return;

        getSystemService(NotificationManager.class).createNotificationChannel(
                new NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        "Channel",
                        NotificationManager.IMPORTANCE_DEFAULT
                )
        );
    }
}
