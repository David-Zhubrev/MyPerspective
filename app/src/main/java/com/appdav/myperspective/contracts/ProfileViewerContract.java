package com.appdav.myperspective.contracts;

import androidx.annotation.Nullable;

import com.appdav.myperspective.common.data.UserData;
import com.appdav.myperspective.common.mvp.PresenterInterface;
import com.appdav.myperspective.common.mvp.ViewInterface;

public interface ProfileViewerContract {

    interface View extends ViewInterface {
        String getUserId();

        String getCurrentUserId();

        void fillViewsWithData(@Nullable UserData userData);

        void setErrorView();

        void proceedToEditor(UserData currentUser);

        void showUpdateSuccessfulMessage();

        void showEditMenu();


    }

    interface Presenter extends PresenterInterface<View> {
        void onActionEdit();

        void onMenuCreated();

        void onUserChanged(UserData user);
    }

}
