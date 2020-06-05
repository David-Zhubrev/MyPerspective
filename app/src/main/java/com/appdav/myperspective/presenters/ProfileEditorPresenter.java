package com.appdav.myperspective.presenters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.appdav.myperspective.common.daggerproviders.components.DaggerDataModelComponent;
import com.appdav.myperspective.common.daggerproviders.modules.DataModelModule;
import com.appdav.myperspective.common.data.UserData;
import com.appdav.myperspective.common.mvp.PresenterBase;
import com.appdav.myperspective.common.views.MyDialogs;
import com.appdav.myperspective.contracts.ProfileEditorContract;
import com.appdav.myperspective.datamodels.UserDataModel;
import com.appdav.myperspective.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileEditorPresenter extends PresenterBase<ProfileEditorContract.View> implements ProfileEditorContract.Presenter, UserDataModel.UserEventListenter, MyDialogs.AddPhotoDialog.AddPhotoDialogCallback, MyDialogs.EditorExitDialog.ProfileEditorExitDialogCallback {

    private final static String DATE_OF_BIRTH_FORMAT = "dd.MM.yyyy";


    private UserDataModel model;
    private UserData currentUser;
    private Uri photoUri;
    private boolean isNewUser;

    public ProfileEditorPresenter() {
        DataModelModule module = new DataModelModule();
        module.setUserEventListenter(this);
        model = DaggerDataModelComponent.builder().dataModelModule(module).build().getUserDataModelInstance();
    }


    @Override
    public void viewIsReady() {
        currentUser = getView().getCurrentUser();
        if (!currentUser.getPhotoUri().isEmpty()) {
            photoUri = Uri.parse(currentUser.getPhotoUri());
        }
        isNewUser = getView().isNewUser();
        if (isNewUser) getView().setModeNewUser();
        else getView().setModeOldUser();
        getView().setupViews(currentUser);
    }

    @Override
    public void onUserUpdatedSuccessfully(UserData userData) {
        if (!isNewUser) {
            getView().finishActivityWithResult(userData);
        } else getView().proceedToMainActivity(userData);
    }

    /**
     * Called when query method has been interrupted by something or when some error has been occured
     */
    @Override
    public void onErrorOccured() {
        //TODO: add error message
    }

    @Override
    public void onImageClicked() {
        getView().createPhotoDialog();
    }

    @Override
    public void onTextViewAddClicked() {
        getView().createPhotoDialog();
    }

    @Override
    public void onPhotoUploaded(Uri uri) {
        this.photoUri = uri;
        getView().updatePhoto(uri);
    }

    @Override
    public void onActionAccept() {
        UserData updatedUserData = createUserData();
        if (updatedUserData != null) {
            model.updateUserEntry(updatedUserData);
            getView().setupWaitingView();
        }
    }

    private UserData createUserData() {
        String name = getView().getNameText();
        if (name.isEmpty()) {
            getView().setError(R.id.et_name_profile_editor);
            getView().requestFocus(R.id.et_name_profile_editor);
            return null;
        }
        String surname = getView().getSurnameText();
        if (surname.isEmpty()) {
            getView().setError(R.id.et_surname_profile_editor);
            getView().requestFocus(R.id.et_surname_profile_editor);
            return null;
        }
        long dateOfBirth = 0;
        if (!getView().getBirthdayText().isEmpty()) {
            dateOfBirth = parseDateOfBirth(getView().getBirthdayText(), getView().getContext());
            if (dateOfBirth == -1) {
                return null;
            }
        }
        String phoneNumber = checkPhoneNumber(getView().getPhoneNumberText());
        if (phoneNumber.equals(FORMAT_ERROR)) return null;
        int rank = getView().getRank();
        int joiningYear = getView().getJoiningYear();

        String about = getView().getAboutText();
        String uri = "";
        if (photoUri != null) uri = photoUri.toString();

        UserData result = new UserData.Builder()
                .uId(currentUser.getuId())
                .name(name)
                .surname(surname)
                .dateOfBirth(dateOfBirth)
                .phoneNumber(phoneNumber)
                .rank(rank)
                .joiningYear(joiningYear)
                .about(about)
                .photoUri(uri)
                .build();
        result.setFavEvents(currentUser.getFavEvents());
        return result;
    }

    private final static String FORMAT_ERROR = "error";

    private String checkPhoneNumber(@NonNull String phoneNumber) {
        if (phoneNumber.length() > 4 && phoneNumber.length() < 18) {
            getView().setFormatError(R.id.et_phone_profile_editor);
            getView().requestFocus(R.id.et_phone_profile_editor);
            return FORMAT_ERROR;
        } else if (phoneNumber.equals("+7 (")) {
            return "";
        } else if (phoneNumber.length() >= 18)
            return phoneNumber;
        return phoneNumber;
    }

    private long parseDateOfBirth(@NonNull String dateOfBirthText, Context context) {
        if (dateOfBirthText.length() > 0 && dateOfBirthText.length() < 10) {
            getView().setFormatError(R.id.et_birthday_profile_editor);
            getView().requestFocus(R.id.et_birthday_profile_editor);
            return -1;
        }
        int day = Integer.valueOf(dateOfBirthText.substring(0, 2));
        if (day > 31 || day < 1) {
            getView().setFormatError(R.id.et_birthday_profile_editor);
            getView().requestFocus(R.id.et_birthday_profile_editor);
            return -1;
        }
        int month = Integer.valueOf(dateOfBirthText.substring(3, 5));
        if (month < 1 || month > 12) {
            getView().setFormatError(R.id.et_birthday_profile_editor);
            getView().requestFocus(R.id.et_birthday_profile_editor);
            return -1;
        }
        int year = Integer.valueOf(dateOfBirthText.substring(6, 10));
        if (year < context.getResources().getInteger(R.integer.min_year_of_birth) ||
                year > context.getResources().getInteger(R.integer.max_year_of_birth)) {
            getView().setFormatError(R.id.et_birthday_profile_editor);
            getView().requestFocus(R.id.et_birthday_profile_editor);
            return -1;
        }

        SimpleDateFormat spdf = new SimpleDateFormat(DATE_OF_BIRTH_FORMAT, Locale.getDefault());
        try {
            Date date = spdf.parse(dateOfBirthText);
            if (date != null) return date.getTime();
            else return 0;
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public void onActionNewPhoto() {
        getView().createCameraActivity();
    }

    @Override
    public void onActionGallery() {
        getView().createGalleryActivity();
    }

    @Override
    public void onAddPhotoDialogCanceled() {
        //TODO add something or make default (or delete this)
    }

    @Override
    public void onBackPressed() {
        if (isNewUser) getView().showQuitToast();
        else getView().showQuitDialog();
    }

    @Override
    public void onExitButtonClicked() {
        getView().finishActivityNoResult();
    }

    /**
     * This method is used to nullify all subscriptions
     * and should be called from lifecycle methods
     */
    @Override
    public void onCreationCanceled() {
        model.deleteCurrentUserEntry(currentUser.getuId());
        getView().finishActivityNoResult();
    }
}
