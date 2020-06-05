package com.appdav.myperspective.common.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.appdav.myperspective.common.Constants;
import com.appdav.myperspective.common.receivers.NotificationBroadcastReceiver;
import com.appdav.myperspective.common.data.AlarmData;

public class RebootService extends JobIntentService {

    private static final String ONE_HOUR = "_oneHour";
    private static final String SIX_HOURS = "_sixHours";
    private static final String TWENTY_FOUR_HOURS = "twentyFourHours";

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        AlarmDatabaseHandler databaseHandler = new AlarmDatabaseHandler(getApplicationContext());
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        assert manager != null;
        SparseArray<AlarmData> alarms = databaseHandler.queryAllAlarmEntries();
        for (int i = 0; i < alarms.size(); i++) {
            AlarmData alarmData = alarms.get(i);
            long timeBeforeEvent = 0;
            switch (alarmData.getType()) {
                case AlarmData.ALARM_TYPE_ONE_HOUR:
                    timeBeforeEvent = Constants.ONE_HOURS_IN_MILLIS;
                    intent.setAction(alarmData.getEventId() + ONE_HOUR);
                    break;
                case AlarmData.ALARM_TYPE_SIX_HOURS:
                    timeBeforeEvent = Constants.SIX_HOURS_IN_MILLIS;
                    intent.setAction(alarmData.getEventId() + SIX_HOURS);
                    break;
                case AlarmData.ALARM_TYPE_TWENTY_FOUR_HOURS:
                    timeBeforeEvent = Constants.TWENTY_FOUR_HOURS_IN_MILLIS;
                    intent.setAction(alarmData.getEventId() + TWENTY_FOUR_HOURS);
                    break;
            }
            Intent broadcastIntent = new Intent(getApplicationContext(), NotificationBroadcastReceiver.class);
            intent.putExtra(Constants.NOTIFICATION_EVENT_TYPE_EXTRA, Constants.NOTIFICATION_TYPE_EVENT);
            intent.putExtra(Constants.ALARM_DATA_EXTRA, alarmData);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            manager.set(AlarmManager.RTC_WAKEUP, alarmData.getEventTime() - timeBeforeEvent, pendingIntent);
        }
    }
}
