package com.appdav.myperspective.presenters;

import android.app.PendingIntent;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.appdav.myperspective.common.services.AlarmDatabaseHandler;
import com.appdav.myperspective.common.Constants;
import com.appdav.myperspective.common.receivers.NotificationBroadcastReceiver;
import com.appdav.myperspective.common.adapters.CalendarEventAdapter;
import com.appdav.myperspective.common.daggerproviders.components.DaggerDataModelComponent;
import com.appdav.myperspective.common.daggerproviders.modules.DataModelModule;
import com.appdav.myperspective.common.data.AlarmData;
import com.appdav.myperspective.common.decorators.BirthdayDecorator;
import com.appdav.myperspective.common.data.EventData;
import com.appdav.myperspective.common.decorators.EventDecorator;
import com.appdav.myperspective.common.services.DateFormatter;
import com.appdav.myperspective.common.mvp.PresenterBase;
import com.appdav.myperspective.contracts.CalendarContract;
import com.appdav.myperspective.datamodels.EventDataModel;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class CalendarPresenter extends PresenterBase<CalendarContract.View> implements CalendarContract.Presenter, EventDataModel.EventDataModelCallback, OnDateSelectedListener, CalendarEventAdapter.CalendarAdapterOnClickListener {

    private EventDataModel model;
    private ArrayList<EventData> events;
    private CalendarEventAdapter adapter;

    public CalendarPresenter() {
        DataModelModule modelModule = new DataModelModule();
        modelModule.setEventDataModelCallback(this);
        model = DaggerDataModelComponent.builder().dataModelModule(modelModule).build().getEventDataModelInstance();
    }

    @Override
    public void viewIsReady() {
        model.queryFavEventsArray(getView().getCurrentUserId());
    }


    @Override
    public void onErrorOccured() {

    }

    @Override
    public void onUsersFavEventsGot(@Nullable ArrayList<EventData> favEvents) {
        if (favEvents != null && !favEvents.isEmpty()) {
            getView().addEventDecorator(new EventDecorator(getView().getContext(), favEvents));
            if (events == null) events = new ArrayList<>();
            events.addAll(favEvents);
        }
        model.queryBirthdayEvents(birthdayDataArrayList -> {
            if (birthdayDataArrayList != null && !birthdayDataArrayList.isEmpty()) {
                getView().addBirthdayDecorator(new BirthdayDecorator(getView().getContext(), birthdayDataArrayList));
                if (events == null) events = new ArrayList<>();
                events.addAll(birthdayDataArrayList);
            }
        });
    }

    private final int currentYear = Calendar.getInstance(Locale.getDefault()).get(Calendar.YEAR);

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        ArrayList<EventData> recyclerViewEvents = new ArrayList<>();
        for (EventData event : events) {
            if (event.getEventType() == EventData.EVENT_TYPE_BIRTHDAY) {
                DateFormatter.Formatter formatter = event.getDateFormatter();
                if (CalendarDay.from(currentYear, formatter.getMonth(), formatter.getDay()).equals(date)) {
                    recyclerViewEvents.add(0, event);
                }
            } else if (event.getEventType() == 0 || event.getEventType() == EventData.EVENT_TYPE_COMMON) {
                DateFormatter.Formatter formatter = event.getDateFormatter();
                if (CalendarDay.from(formatter.getYear(), formatter.getMonth(), formatter.getDay()).equals(date))
                    recyclerViewEvents.add(event);
            }
        }
        if (recyclerViewEvents.isEmpty()) {
            getView().setNoEventsView();
            return;
        }
        if (adapter == null) adapter = new CalendarEventAdapter(recyclerViewEvents, this);
        else adapter.updateAdapter(recyclerViewEvents);
        getView().fillViewWithEvents(recyclerViewEvents.get(0).getEventDateText(), adapter);
    }

    @Override
    public void onClick(EventData event) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        long eventTime = event.getDate();
        if (calendar.getTimeInMillis() >= eventTime) {
            getView().showEventAlreadyEndedMessage();
            return;
        } else if (calendar.getTimeInMillis() + Constants.ONE_HOURS_IN_MILLIS >= eventTime) {
            getView().showEventInOneHourMessage();
            return;
        }
        getView().showSetNotificationsDialog(eventTime, alarms -> {
            int counter = 0;
            if (alarms.contains(Constants.ONE_HOURS_IN_MILLIS)) {
                createAlarm(event, Constants.ONE_HOURS_IN_MILLIS, ONE_HOUR);
                counter++;
            }
            if (alarms.contains(Constants.SIX_HOURS_IN_MILLIS)) {
                createAlarm(event, Constants.SIX_HOURS_IN_MILLIS, SIX_HOURS);
                counter++;
            }
            if (alarms.contains(Constants.TWENTY_FOUR_HOURS_IN_MILLIS)) {
                createAlarm(event, Constants.TWENTY_FOUR_HOURS_IN_MILLIS, TWENTY_FOUR_HOURS);
                counter++;
            }
            if (counter > 0)
                getView().showReminderAddedSuccessfullyMessage();
        });
    }

    private static final String ONE_HOUR = "_oneHour";
    private static final String SIX_HOURS = "_sixHours";
    private static final String TWENTY_FOUR_HOURS = "twentyFourHours";


    private void createAlarm(EventData event, long timeBeforeAlarm, String actionSuffix) {
        Intent intent = new Intent(getView().getContext(), NotificationBroadcastReceiver.class);
        intent.setAction(event.getEventId() + actionSuffix);
        intent.putExtra(Constants.NOTIFICATION_EVENT_TYPE_EXTRA, Constants.NOTIFICATION_TYPE_EVENT);

        int alarmType = -1;
        if (actionSuffix.equals(ONE_HOUR)) alarmType = AlarmData.ALARM_TYPE_ONE_HOUR;
        if (actionSuffix.equals(SIX_HOURS)) alarmType = AlarmData.ALARM_TYPE_SIX_HOURS;
        if (actionSuffix.equals(TWENTY_FOUR_HOURS))
            alarmType = AlarmData.ALARM_TYPE_TWENTY_FOUR_HOURS;
        AlarmData alarmData = new AlarmData(event.getEventId(), event.getName(), event.getDate(), alarmType);

        intent.putExtra(Constants.ALARM_DATA_EXTRA, alarmData);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getView().getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        getView().createNotification(pendingIntent, event.getDate() - timeBeforeAlarm);


        AlarmDatabaseHandler alarmDatabaseHandler = new AlarmDatabaseHandler(getView().getContext());
        alarmDatabaseHandler.addAlarmEntry(alarmData);
    }

    /**
     * This method is used to nullify all subscriptions
     * and should be called from lifecycle methods
     */
    @Override
    public void destroy() {
        super.destroy();
        model = null;
        events = null;
        adapter = null;
    }
}
