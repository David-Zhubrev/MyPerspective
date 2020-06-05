package com.appdav.myperspective.contracts;

import com.appdav.myperspective.common.adapters.EventFeedAdapter;
import com.appdav.myperspective.common.data.EventData;
import com.appdav.myperspective.common.mvp.PresenterInterface;
import com.appdav.myperspective.common.mvp.ViewInterface;
import com.appdav.myperspective.common.views.MyDialogs;

public interface EventFeedContract {

    interface View extends ViewInterface {

        String getCurrentUserId();

        void attachAdapterToRecyclerView(EventFeedAdapter adapter);

        void setRefreshButtonActive(boolean isActive);

        void setScreenRefreshing();

        void setScreenReady();

        void setupNewEventActivity();

        void setupPeopleGoingActivity(EventData eventData);

        void setupConfirmationDialog(MyDialogs.ConfirmRemovalDialog.ConfirmRemovalDialogCallback callback);

        void showAddedToFavsMessage();

        void showRemovedFromFavsMessage();
    }

    interface Presenter extends PresenterInterface<View> {
        void onFabClicked();

        void onRefreshButtonClicked();
    }

}
