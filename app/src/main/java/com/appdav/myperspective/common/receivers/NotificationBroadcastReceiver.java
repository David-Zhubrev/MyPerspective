package com.appdav.myperspective.common.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.JobIntentService;

import com.appdav.myperspective.common.services.NotificationService;

public class NotificationBroadcastReceiver extends BroadcastReceiver {

    private final static String LOG_TAG = "myLogs";

    public NotificationBroadcastReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "BroadcastReceiver onReceive invoked");
        Intent serviceIntent = new Intent(context, NotificationService.class);
        serviceIntent.putExtras(intent);
        JobIntentService.enqueueWork(context, NotificationService.class, 0, serviceIntent);
    }
}
