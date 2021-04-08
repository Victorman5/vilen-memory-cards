package com.victorman.memorycards.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.victorman.memorycards.R;
import com.victorman.memorycards.activities.MainActivity;

import static com.victorman.memorycards.App.NOTIFICATION_CHANNEL_ID;

public class NotificationSenderService extends Service {
    private NotificationManagerCompat notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = NotificationManagerCompat.from(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String[] terms = intent.getStringArrayExtra(MainActivity.EXTRA_CARDS_LIST_TERMS);
        final String[] definitions = intent.getStringArrayExtra(MainActivity.EXTRA_CARDS_LIST_DEFINITIONS);
        final int sendingInterval = intent.getIntExtra(MainActivity.EXTRA_NOTIFY_SENDING_INTERVAL_MINUTES, 5)
                                        * 60 * 60;

        (new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < terms.length; i++) {
                    createTermNotification(terms[i], definitions[i]);
                    try {
                        Thread.sleep(sendingInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        })).start();

        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void createTermNotification(String term, String definition) {
        notificationManager.notify(1, new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_human_memory_24dp)
                .setContentTitle(term)
                .setContentText(definition)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build());
    }
}
