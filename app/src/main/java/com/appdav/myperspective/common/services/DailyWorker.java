package com.appdav.myperspective.common.services;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.appdav.myperspective.common.Constants;
import com.appdav.myperspective.common.receivers.NotificationBroadcastReceiver;
import com.appdav.myperspective.common.daggerproviders.components.DaggerDataModelComponent;
import com.appdav.myperspective.common.daggerproviders.modules.DataModelModule;
import com.appdav.myperspective.common.data.EventData;
import com.appdav.myperspective.datamodels.EventDataModel;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DailyWorker extends Worker implements EventDataModel.EventDataModelCallback {

    private EventDataModel model;
    private Calendar currentDate;

    public DailyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        DataModelModule modelModule = new DataModelModule();
        modelModule.setEventDataModelCallback(this);
        model = DaggerDataModelComponent.builder().dataModelModule(modelModule).build().getEventDataModelInstance();
        currentDate = Calendar.getInstance(Locale.getDefault());
    }

    @NonNull
    @Override
    public Result doWork() {
        model.queryBirthdayEvents(birthdayDataArrayList -> {
            if (birthdayDataArrayList != null) {
                for (EventData birthday : birthdayDataArrayList) {
                    Calendar birthdayDate = Calendar.getInstance(Locale.getDefault());
                    birthdayDate.setTime(new Date(birthday.getDate()));
                    if (birthdayDate.get(Calendar.DAY_OF_MONTH) == currentDate.get(Calendar.DAY_OF_MONTH) &&
                            birthdayDate.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH)) {
                        sendBirthdayNotification(birthday);
                    }
                }
            }
        });
        return Result.success();
    }

    private final static String ACTION_BIRTHDAY_SUFFIX = "birthday";

    private void sendBirthdayNotification(EventData birthdayEvent) {
        Intent intent = new Intent(getApplicationContext(), NotificationBroadcastReceiver.class);
        intent.setAction(birthdayEvent.getEventId() + ACTION_BIRTHDAY_SUFFIX);
        intent.putExtra(Constants.NOTIFICATION_EVENT_TYPE_EXTRA, Constants.NOTIFICATION_TYPE_BIRTHDAY);
        intent.putExtra(Constants.NOTIFICATION_EVENT_NAME_INTENT_EXTRA, birthdayEvent.getName());
        intent.putExtra(Constants.NOTIFICATION_EVENT_DATE_INTENT_EXTRA, birthdayEvent.getDate());
        getApplicationContext().sendBroadcast(intent);
    }

    @Override
    public void onErrorOccured() {
    }
}
