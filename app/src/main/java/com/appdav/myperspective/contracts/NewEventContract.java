package com.appdav.myperspective.contracts;

import android.content.Context;
import android.net.Uri;

import com.appdav.myperspective.common.mvp.PresenterInterface;
import com.appdav.myperspective.common.mvp.ViewInterface;

public interface NewEventContract {

    interface View extends ViewInterface {
        String getNameText();

        String getDateText();

        String getInfoText();

        boolean isEventTypeSwitchChecked();

        void setNameFieldFocus();

        void setDateFieldFocus();

        void setInfoFieldFocus();

        void setNameFieldError();

        void setDateFieldEmptyError();

        void setDateFieldFormatError();

        void setInfoFieldError();

        void setPhotoError();

        void setDateFieldInstantEventError();

        void setPastTimeError();

        void setNoPhotoView();

        void setPhotoView(Uri uri);

        void createExitDialog();

        void createAddPhotoDialog();

        void startCameraActivity();

        void startGalleryActivity();

        void setInactive();

        void setActive();

        void finishActivity(boolean isSuccessful);

        void setDatabaseErrorMessage();

        void showUploadSuccessfulMessage();

        Context getContext();
    }

    interface Presenter extends PresenterInterface<View> {
        void onPhotoUploaded(Uri uri);

        void onBackPressed();

        void onButtonAddPhotoClicked();

        void onButtonRemovePhotoClicked();

        void onActionAccept();
    }
}
