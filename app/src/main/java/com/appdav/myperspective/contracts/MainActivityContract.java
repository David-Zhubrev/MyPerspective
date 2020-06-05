package com.appdav.myperspective.contracts;

import android.content.Context;

import androidx.work.WorkInfo;

import com.appdav.myperspective.common.mvp.PresenterInterface;
import com.appdav.myperspective.common.mvp.ViewInterface;

public interface MainActivityContract {
    interface View extends ViewInterface {
        void setMenuItemsForEventFragment();

        void setMenuItemsForLifehackFragment();

        void setMenuForSpoStuffFragment();

        void proceedToProfileViewer(String uId);

        void proceedToCalendarView();

        void proceedToInfoActivity();

        void proceedToEditorsRoom();

        Context getContext();

        void checkForUpdates();

        void startDailyWorker();

        WorkInfo.State checkIfWorkerIsRunning();
    }

    interface Presenter extends PresenterInterface<View> {
        void onActionCalendar();

        void onActionProfile();

        void onActionAbout();

        void onActionEditorsRoom();

        void onMenuCreated();

    }


}
