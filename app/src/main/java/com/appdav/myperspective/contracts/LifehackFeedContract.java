package com.appdav.myperspective.contracts;

import android.content.Context;

import com.appdav.myperspective.common.adapters.LifehackFeedAdapter;
import com.appdav.myperspective.common.mvp.PresenterInterface;
import com.appdav.myperspective.common.mvp.ViewInterface;
import com.appdav.myperspective.common.views.MyDialogs;

public interface LifehackFeedContract {
    interface View extends ViewInterface {
        void setScreenRefreshing();

        void setScreenReady();

        void setRefreshButtonActivated(boolean isActivated);

        void attachAdapterToRecyclerView(LifehackFeedAdapter adapter);

        Context getContext();

        void createNewLifehackDialog();

        void setupUploadSuccessfulSnackBar();

        void setupUploadFailedSnackbar();

        void showProfileViewer(String uId);

        void setupRemovalConfirmDialog(MyDialogs.ConfirmRemovalDialog.ConfirmRemovalDialogCallback callback);

        String getCurrentUserId();
    }

    interface Presenter extends PresenterInterface<View> {
        void onFabClicked();

        void onButtonRefreshClicked();

        void onSnackbarRetryButtonClicked();
    }
}
