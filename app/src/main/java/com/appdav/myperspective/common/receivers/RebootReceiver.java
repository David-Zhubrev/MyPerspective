package com.appdav.myperspective.common.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.JobIntentService;

import com.appdav.myperspective.common.services.RebootService;

public class RebootReceiver extends BroadcastReceiver {

    public RebootReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            JobIntentService.enqueueWork(context, RebootService.class, 0, new Intent(context, RebootService.class));
        }
    }
}
