package com.appdav.myperspective.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.appdav.myperspective.common.Constants;
import com.appdav.myperspective.common.services.UpdateNotificationService;
import com.appdav.myperspective.common.receivers.WorkerBroadcastReceiver;
import com.appdav.myperspective.common.daggerproviders.components.DaggerPresenterComponent;
import com.appdav.myperspective.common.data.UserData;
import com.appdav.myperspective.common.views.MyDialogs;
import com.appdav.myperspective.contracts.MainActivityContract;
import com.appdav.myperspective.datamodels.InstantEventsDatabase;
import com.appdav.myperspective.presenters.MainActivityPresenter;
import com.appdav.myperspective.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity implements MainActivityContract.View {

    private MenuItem menuItemCalendar;

    private static UserData currentUser;

    private boolean isBackAlreadyPressed = false;

    private BroadcastReceiver updateBroadcastReceiver;
    private LocalBroadcastManager localBroadcastManager;

    private final static String DIALOG_UPDATE_RELEASED_TAG = "dialogupdate";

    private static boolean updateDialogAlreadyShown = false;

    @Inject
    MainActivityPresenter presenter;

    public static Snackbar snackbarEventUploaded;
    private Snackbar exitOnBackPressedSnackbar;

    View snackbarRootView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        currentUser = getIntent().getParcelableExtra(Constants.ACTION_WITH_CURRENT_USER_INTENT);
        /*if (currentUser == null){
            //TODO: add no internet screen view
        }*/
        assert currentUser != null;
        DaggerPresenterComponent.builder().build().injectMainActivity(this);
        presenter.attachView(this);
        presenter.viewIsReady();
        snackbarRootView = findViewById(android.R.id.content);
        snackbarEventUploaded = Snackbar.make(snackbarRootView, R.string.new_event_successful, Snackbar.LENGTH_SHORT);
        exitOnBackPressedSnackbar = Snackbar.make(snackbarRootView, getString(R.string.press_back_twice_to_quit), Snackbar.LENGTH_SHORT);
    }

    @Override
    public void checkForUpdates() {
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        if (!updateDialogAlreadyShown) createUpdateBroadcastReceiver();
        Intent intent = new Intent(getApplicationContext(), UpdateNotificationService.class);
        startService(intent);
    }

    @Override
    public WorkInfo.State checkIfWorkerIsRunning() {
        try {
            if (WorkManager.getInstance(this).getWorkInfosForUniqueWork(Constants.UNIQUE_PERIODIC_WORK_NAME).get().size() > 0) {
                return WorkManager.getInstance(this).getWorkInfosForUniqueWork(Constants.UNIQUE_PERIODIC_WORK_NAME).get().get(0).getState();
            } else return WorkInfo.State.CANCELLED;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return WorkInfo.State.CANCELLED;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return WorkInfo.State.CANCELLED;
        }
    }

    @Override
    public void startDailyWorker() {
        long delay;
        final int hourToLaunch = 9;
        final int minutesToLaunch = 0;
        Calendar currentDate = Calendar.getInstance(Locale.getDefault());
        Calendar timeToLaunch = Calendar.getInstance(Locale.getDefault());
        timeToLaunch.set(Calendar.HOUR_OF_DAY, hourToLaunch);
        timeToLaunch.set(Calendar.MINUTE, minutesToLaunch);
        timeToLaunch.set(Calendar.SECOND, 0);
        if (currentDate.before(timeToLaunch))
            delay = timeToLaunch.getTimeInMillis() - currentDate.getTimeInMillis();
        else
            delay = timeToLaunch.getTimeInMillis() - currentDate.getTimeInMillis() + Constants.TWENTY_FOUR_HOURS_IN_MILLIS;
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        assert alarmManager != null;
        Intent intent = new Intent(this, WorkerBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, currentDate.getTimeInMillis() + delay, pendingIntent);
    }


    @Override
    protected void onStart() {
        super.onStart();

        if (updateBroadcastReceiver != null) {
            localBroadcastManager.registerReceiver(updateBroadcastReceiver, new IntentFilter(Constants.ACTION_UPDATE_IS_READY));
        }
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (menuItemCalendar != null) menuItemCalendar.setEnabled(true);

    }

    @Override
    public void onBackPressed() {
        if (!isBackAlreadyPressed) {
            isBackAlreadyPressed = true;
            Handler h = new Handler();
            h.postDelayed(() -> isBackAlreadyPressed = false, 2000);
            if (!exitOnBackPressedSnackbar.isShown()) exitOnBackPressedSnackbar.show();
        } else {
            finish();
        }
    }

    @Override
    protected void onStop() {
        if (localBroadcastManager != null && updateBroadcastReceiver != null)
            localBroadcastManager.unregisterReceiver(updateBroadcastReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (localBroadcastManager != null && updateBroadcastReceiver != null)
            localBroadcastManager.unregisterReceiver(updateBroadcastReceiver);
        super.onDestroy();
    }

    private void createUpdateBroadcastReceiver() {
        updateBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() != null && intent.getAction().equals(Constants.ACTION_UPDATE_IS_READY)) {
                    new MyDialogs.UpdateReleasedDialog(intent.getStringExtra(Constants.UPDATE_LINK_INTENT_EXTRA), presenter).show(getSupportFragmentManager(), DIALOG_UPDATE_RELEASED_TAG);
                    updateDialogAlreadyShown = true;
                    localBroadcastManager.unregisterReceiver(this);
                    localBroadcastManager = null;
                    updateBroadcastReceiver = null;
                }
            }
        };
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        menuItemCalendar = menu.findItem(R.id.action_calendar);
        if (!currentUser.hasEditorRights) menu.findItem(R.id.action_editors_room).setVisible(false);
        presenter.onMenuCreated();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_calendar:
                menuItemCalendar.setEnabled(false);
                presenter.onActionCalendar();
                break;
            case R.id.action_profile:
                presenter.onActionProfile();
                break;
            case R.id.action_about:
                presenter.onActionAbout();
                break;
            case R.id.action_editors_room:
                presenter.onActionEditorsRoom();
                break;
            case R.id.action_users_list:
                proceedToUsersListActivity();
                break;
        }
        return true;
    }

    private void init() {
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_event_feed, R.id.navigation_lifehack_feed, R.id.navigation_spo_stuff)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        //Overriding ItemSelectedListener set by setupWithNavController()
        //to invoke presenters methods to manage views
        navView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_event_feed:
                    navController.navigate(R.id.navigation_event_feed);
                    setMenuItemsForEventFragment();
                    break;
                case R.id.navigation_lifehack_feed:
                    navController.navigate(R.id.navigation_lifehack_feed);
                    setMenuItemsForLifehackFragment();
                    break;
                case R.id.navigation_spo_stuff:
                    navController.navigate(R.id.navigation_spo_stuff);
                    setMenuForSpoStuffFragment();
                    break;
            }
            return false;
        });
    }

    public void proceedToProfileViewer(String uId) {
        Intent intent = new Intent(this, ProfileViewerActivity.class);
        intent.putExtra(Constants.ACTION_WITH_CURRENT_USER_INTENT, currentUser.getuId());
        intent.putExtra(Constants.ACTION_WITH_ANY_USER, uId);
        startActivity(intent);
    }

    @Override
    public void proceedToEditorsRoom() {
        Intent intent = new Intent(this, EditorsRoomActivity.class);
        intent.putExtra(Constants.ACTION_WITH_CURRENT_USER_INTENT, currentUser.getuId());
        startActivity(intent);
    }

    @Override
    public void setMenuItemsForEventFragment() {
        if (menuItemCalendar != null && !menuItemCalendar.isVisible()) {
            menuItemCalendar.setVisible(true);
            menuItemCalendar.setEnabled(true);
        }
    }

    @Override
    public void setMenuForSpoStuffFragment() {
        if (menuItemCalendar != null && menuItemCalendar.isVisible()) {
            menuItemCalendar.setVisible(false);
            menuItemCalendar.setEnabled(false);
        }
    }

    @Override
    public void setMenuItemsForLifehackFragment() {
        if (menuItemCalendar != null && menuItemCalendar.isVisible()) {
            menuItemCalendar.setVisible(false);
            menuItemCalendar.setEnabled(false);
        }
    }

    @Override
    public void proceedToCalendarView() {
        Intent intent = new Intent(this, CalendarActivity.class);
        intent.putExtra(Constants.ACTION_WITH_CURRENT_USER_INTENT, currentUser.getuId());
        startActivity(intent);
    }

    private void proceedToUsersListActivity() {
        Intent intent = new Intent(this, UsersListActivity.class);
        startActivity(intent);
    }

    public static String getCurrentUserId() {
        return currentUser.getuId();
    }

    public static boolean hasEditorsRights() {
        return currentUser.hasEditorRights;
    }

    public static boolean isUserBanned() {
        return currentUser.bannedFromCreatingEntries;
    }


    @Override
    public void proceedToInfoActivity() {
        Intent intent = new Intent(this, InfoActivity.class);
        startActivity(intent);
        Log.d("myLogs", "Database size: " + new InstantEventsDatabase(this).getCreatedInstantEventList().size());
    }
}
