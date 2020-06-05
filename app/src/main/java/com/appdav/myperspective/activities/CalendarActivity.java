package com.appdav.myperspective.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.appdav.myperspective.common.adapters.CalendarEventAdapter;
import com.appdav.myperspective.common.Constants;
import com.appdav.myperspective.common.daggerproviders.components.DaggerPresenterComponent;
import com.appdav.myperspective.common.decorators.BirthdayDecorator;
import com.appdav.myperspective.common.decorators.EventDecorator;
import com.appdav.myperspective.common.views.CalendarTitleFormatter;
import com.appdav.myperspective.common.views.MyDialogs;
import com.appdav.myperspective.contracts.CalendarContract;
import com.appdav.myperspective.presenters.CalendarPresenter;
import com.appdav.myperspective.R;
import com.google.android.material.snackbar.Snackbar;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CalendarActivity extends AppCompatActivity implements CalendarContract.View {


    @BindView(R.id.recycler_view_calendar)
    RecyclerView recyclerView;
    @BindView(R.id.calendar_view)
    MaterialCalendarView calendarView;
    @BindView(R.id.tv_date_calendar)
    TextView tvDate;

    @Inject
    CalendarPresenter presenter;

    private AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        ButterKnife.bind(this);
        calendarView.setTitleFormatter(CalendarTitleFormatter.getInstance());
        DaggerPresenterComponent.builder().build().injectCalendarActivity(this);
        presenter.attachView(this);
        presenter.viewIsReady();
        calendarView.setOnDateChangedListener(presenter);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) this.onBackPressed();
        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
        presenter.destroy();
        presenter = null;
    }

    @Override
    public void createNotification(PendingIntent pendingIntent, long time) {
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        showReminderAddedSuccessfullyMessage();
    }

    @Override
    public void showEventAlreadyEndedMessage() {
        Snackbar.make(findViewById(android.R.id.content), R.string.message_event_time_already_passed, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showEventInOneHourMessage() {
        Snackbar.make(findViewById(android.R.id.content), R.string.message_event_time_beginning_in_one_hour, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showReminderAddedSuccessfullyMessage() {
        Snackbar.make(findViewById(android.R.id.content), R.string.message_reminder_added_successfully, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public String getCurrentUserId() {
        return getIntent().getStringExtra(Constants.ACTION_WITH_CURRENT_USER_INTENT);
    }

    @Override
    public void addEventDecorator(EventDecorator decorator) {
        calendarView.addDecorator(decorator);
    }

    @Override
    public void addBirthdayDecorator(BirthdayDecorator decorator) {
        calendarView.addDecorator(decorator);
    }

    @Override
    public void setNoEventsView() {
        recyclerView.setVisibility(View.INVISIBLE);
        tvDate.setText(R.string.tv_calendar_empty_list);
    }

    private final static String LOG_TAG = "myLogs";

    @Override
    public void fillViewWithEvents(String text, CalendarEventAdapter adapter) {
        Log.d(LOG_TAG, "activity fillViewWithEvents invoked");
        recyclerView.setVisibility(View.VISIBLE);
        tvDate.setText(text);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public Context getContext() {
        return this;
    }

    private final static String SET_NOTIFICATIONS_DIALOG_TAG = "setnotificationsdialog";

    @Override
    public void showSetNotificationsDialog(long eventTime, MyDialogs.SetNotificationsDialog.SetNotificationsDialogCallback callback) {
        new MyDialogs.SetNotificationsDialog(eventTime, callback).show(getSupportFragmentManager(), SET_NOTIFICATIONS_DIALOG_TAG);
    }
}
