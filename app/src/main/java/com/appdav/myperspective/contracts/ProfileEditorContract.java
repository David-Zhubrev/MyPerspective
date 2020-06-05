package com.appdav.myperspective.contracts;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.IdRes;

import com.appdav.myperspective.common.data.UserData;
import com.appdav.myperspective.common.mvp.PresenterInterface;
import com.appdav.myperspective.common.mvp.ViewInterface;

public interface ProfileEditorContract {
    interface View extends ViewInterface {

        UserData getCurrentUser();

        boolean isNewUser();

        void setModeNewUser();

        void setModeOldUser();

        void setupViews(UserData user);

        void updatePhoto(Uri uri);

        void setupWaitingView();

        void createPhotoDialog();

        void createCameraActivity();

        void createGalleryActivity();

        String getNameText();

        String getSurnameText();

        String getBirthdayText();

        String getPhoneNumberText();

        int getJoiningYear();

        int getRank();

        String getAboutText();

        void setError(@IdRes int editTextId);

        void setFormatError(@IdRes int editTextId);

        void requestFocus(@IdRes int editTextId);

        void showQuitToast();

        void showQuitDialog();

        void finishActivityNoResult();

        void finishActivityWithResult(UserData userData);

        void proceedToMainActivity(UserData userData);

        Context getContext();

    }

    interface Presenter extends PresenterInterface<View> {
        void onImageClicked();

        void onTextViewAddClicked();

        void onPhotoUploaded(Uri uri);

        void onActionAccept();

        void onBackPressed();

        void onCreationCanceled();
    }
}
