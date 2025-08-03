package com.example.autoalarmcoffeeapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

//This class is specifically meant to check for if the alarm command for changing was
//able to processed and recieved when either of the buttons of remove or set where made
public class AlarmReceiver extends BroadcastReceiver
{
    private static final String TAG = "AlarmReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("com.example.GENERIC_ACTION".equals(intent.getAction()))
        {
            Log.d(TAG, "Alarm triggered!");
            Toast.makeText(context, "Time to brew coffee!", Toast.LENGTH_SHORT).show();
        }
    }
}
