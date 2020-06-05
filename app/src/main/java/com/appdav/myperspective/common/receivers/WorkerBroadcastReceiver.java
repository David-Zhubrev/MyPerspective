package com.appdav.myperspective.common.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.appdav.myperspective.common.Constants;
import com.appdav.myperspective.common.services.DailyWorker;

import java.util.concurrent.TimeUnit;

public class WorkerBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(DailyWorker.class, 24, TimeUnit.HOURS).build();
        WorkManager manager = WorkManager.getInstance(context);
        manager.cancelAllWorkByTag(Constants.UNIQUE_PERIODIC_WORK_NAME);
        manager.enqueueUniquePeriodicWork(Constants.UNIQUE_PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE, request);
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(Constants.UNIQUE_PERIODIC_WORK_NAME, ExistingPeriodicWorkPolicy.REPLACE, request);
    }
}
