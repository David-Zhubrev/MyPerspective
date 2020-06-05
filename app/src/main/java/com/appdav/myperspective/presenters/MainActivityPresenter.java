package com.appdav.myperspective.presenters;

import android.util.Log;

import androidx.work.WorkInfo;

import com.appdav.myperspective.activities.MainActivity;
import com.appdav.myperspective.common.services.Preferences;
import com.appdav.myperspective.common.daggerproviders.components.DaggerServiceComponent;
import com.appdav.myperspective.common.daggerproviders.modules.ServiceModule;
import com.appdav.myperspective.common.mvp.PresenterBase;
import com.appdav.myperspective.common.views.MyDialogs;
import com.appdav.myperspective.contracts.MainActivityContract;


import javax.inject.Inject;

public class MainActivityPresenter extends PresenterBase<MainActivityContract.View> implements MainActivityContract.Presenter, MyDialogs.UpdateReleasedDialog.UpdateDialogCallback {

    private Preferences preferences;

    @Inject
    public MainActivityPresenter() {
    }

    @Override
    public void viewIsReady() {
        ServiceModule module = new ServiceModule();
        module.setContext(getView().getContext());
        preferences = DaggerServiceComponent.builder().serviceModule(module).build().getPreferences();
        if (!preferences.getDontShowUpdateMessageAgain()) {
            getView().checkForUpdates();
        }
        Log.d("myLogs", "Worker state: " + getView().checkIfWorkerIsRunning().toString());
        if (getView().checkIfWorkerIsRunning() != WorkInfo.State.ENQUEUED) {
            getView().startDailyWorker();
        }
    }

    @Override
    public void onActionCalendar() {
        getView().proceedToCalendarView();
    }

    @Override
    public void onActionProfile() {
        getView().proceedToProfileViewer(MainActivity.getCurrentUserId());
    }

    @Override
    public void onActionEditorsRoom() {
        getView().proceedToEditorsRoom();
    }

    @Override
    public void onActionAbout() {
        getView().proceedToInfoActivity();
    }

    @Override
    public void onMenuCreated() {
        getView().setMenuItemsForEventFragment();
    }

    /**
     * This method is invoked when Okay button of UpdateDialog is clicked
     *
     * @param dontShowAgain shows UpdateDialog -Do not show again- checkbox state
     */
    @Override
    public void onButtonClicked(boolean dontShowAgain) {
        if (dontShowAgain) {
            preferences.writeBoolean(Preferences.DIALOG_UPDATE_NEVER_SHOW_AGAIN, dontShowAgain);
        }
    }

}
