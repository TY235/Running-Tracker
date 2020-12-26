package com.example.runningtracker;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

public class TrackingNotification extends Application {

    public static final String CHANNEL_ID = "TrackingNotificationChannel";
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    /* Create a notification channel for tracking service */
    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Running Tracker Service",
                NotificationManager.IMPORTANCE_LOW
        );
        getSystemService(NotificationManager.class).createNotificationChannel(serviceChannel);
    }

}
