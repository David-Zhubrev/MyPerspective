package com.appdav.myperspective.common.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationManagerCompat;

import com.appdav.myperspective.R;
import com.appdav.myperspective.common.Constants;
import com.appdav.myperspective.common.data.AlarmData;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NotificationService extends JobIntentService {

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        int eventType = intent.getIntExtra(Constants.NOTIFICATION_EVENT_TYPE_EXTRA, 0);
        switch (eventType) {
            case Constants.NOTIFICATION_TYPE_EVENT:
                createEventNotification(intent);
                break;
            case Constants.NOTIFICATION_TYPE_BIRTHDAY:
                createBirthdayNotification(intent);
                break;
            /*case Constants.NOTIFICATION_TYPE_UPDATE:
                break;*/
        }
    }

    private void createBirthdayNotification(Intent intent) {
        long eventLongDate = intent.getLongExtra(Constants.NOTIFICATION_EVENT_DATE_INTENT_EXTRA, 0);
        String text = intent.getStringExtra(Constants.NOTIFICATION_EVENT_NAME_INTENT_EXTRA);
        text += " " + getString(R.string.notification_birthday_message_postfix);
        NotificationHelper helper = new NotificationHelper(getApplicationContext());
        helper.setTitle(getString(R.string.notification_birthday_title))
                .setText(text)
                .setSmallIcon(R.drawable.ic_birthday_cake)
                .setInboxStyle(getString(R.string.notification_remind_title))
                .sendNotification(getString(R.string.notification_channel_name), (int) (eventLongDate / 1000L));
    }

    private void createEventNotification(Intent intent) {
        AlarmData alarmData = intent.getParcelableExtra(Constants.ALARM_DATA_EXTRA);
        assert alarmData != null;
        String text = "";
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.set(Calendar.HOUR_OF_DAY, 1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Calendar eventCalendar = Calendar.getInstance(Locale.getDefault());
        Date eventDate = new Date(alarmData.getEventTime());
        eventCalendar.setTime(eventDate);
        eventCalendar.set(Calendar.HOUR_OF_DAY, 1);
        eventCalendar.set(Calendar.MINUTE, 0);
        eventCalendar.set(Calendar.SECOND, 0);
        eventCalendar.set(Calendar.MILLISECOND, 0);

        if (eventCalendar.before(calendar)) {
            text += getString(R.string.notification_remind_tomorrow) + " ";
        } else if (eventCalendar.before(calendar)) return;
        else text += getString(R.string.notification_remind_today) + " ";
        text += alarmData.getEventName() + "\n";
        text += new SimpleDateFormat("HH:mm", Locale.getDefault()).format(eventDate);

        NotificationHelper helper = new NotificationHelper(getApplicationContext());
        helper.setText(text).setTitle(getString(R.string.notification_remind_title))
                .setSmallIcon(R.drawable.ic_event_feed).setInboxStyle(getString(R.string.notification_remind_title))
                .sendNotification(getString(R.string.notification_channel_name), (int) (alarmData.getEventTime() / 1000L));
        AlarmDatabaseHandler handler = new AlarmDatabaseHandler(getApplicationContext());
        handler.removeAlarmEntry(alarmData.getEventId());
    }
}
