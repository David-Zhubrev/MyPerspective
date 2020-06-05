package com.appdav.myperspective.contracts;

import android.app.PendingIntent;
import android.content.Context;

import com.appdav.myperspective.common.adapters.CalendarEventAdapter;
import com.appdav.myperspective.common.decorators.BirthdayDecorator;
import com.appdav.myperspective.common.decorators.EventDecorator;
import com.appdav.myperspective.common.mvp.PresenterInterface;
import com.appdav.myperspective.common.mvp.ViewInterface;
import com.appdav.myperspective.common.views.MyDialogs;

public interface CalendarContract {

    interface View extends ViewInterface {
        String getCurrentUserId();

        Context getContext();

        void addEventDecorator(EventDecorator decorator);

        void addBirthdayDecorator(BirthdayDecorator decorator);

        void setNoEventsView();

        void fillViewWithEvents(String text, CalendarEventAdapter adapter);

        void createNotification(PendingIntent pendingIntent, long time);

        void showEventAlreadyEndedMessage();

        void showEventInOneHourMessage();

        void showReminderAddedSuccessfullyMessage();

        void showSetNotificationsDialog(long eventTime, MyDialogs.SetNotificationsDialog.SetNotificationsDialogCallback callback);

    }

    interface Presenter extends PresenterInterface<View> {

    }

}
