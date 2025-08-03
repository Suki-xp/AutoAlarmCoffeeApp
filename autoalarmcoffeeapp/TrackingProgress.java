package com.example.autoalarmcoffeeapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

//This is class is meant to display the live on screen tracking update of the coffee pot
//so that the user can track the current status of their cup of coffee
public class TrackingProgress extends Service
{
    private static final String DISPLAY_ID = "coffee_pathway_track";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        String channelID = "Tracking Channel";
        NotificationManager notificationManager;

        //Now we want to reference the method call to update the progress of the brewing
        //within the notification itself
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            notificationManager = getSystemService(NotificationManager.class);
            NotificationChannel channel = new NotificationChannel(channelID, "Tracking Progress", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        Notification notification = new NotificationCompat.Builder(this, channelID)
                .setContentTitle("Coffee Brewing")
                .setContentText("Tracking progress...")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
        startForeground(1, notification); // Start as foreground service
        return START_STICKY;
    }

    //The updateNotification command which should parse the progress of the bar
    //throughout the brewing
    private void updatingNotifications(int tracks)
    {
        NotificationCompat.Builder updateBuilder = new NotificationCompat.Builder(this, DISPLAY_ID)
                .setContentTitle("Coffee brewing in progress")
                .setContentText(tracks + "%")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setProgress(100, 0, false)
                .setOngoing(true);
        try
        {
            NotificationManagerCompat.from(this).notify(1, updateBuilder.build());
        }
        catch (SecurityException e)
        {
            Log.e("Notifications poop ", String.valueOf(e));
        }
    }

    //The method that should display the changes happening to the notification itself
    private void displayNotifications()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
                NotificationChannel newChannel = new NotificationChannel(DISPLAY_ID, "Coffee Status", NotificationManager.IMPORTANCE_LOW);
                NotificationManager newManager = getSystemService(NotificationManager.class);
                newManager.createNotificationChannel(newChannel);
        }

    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}
