package com.appdav.myperspective.presenters;

import android.net.Uri;

import com.appdav.myperspective.common.Constants;
import com.appdav.myperspective.common.daggerproviders.components.DaggerDataModelComponent;
import com.appdav.myperspective.common.daggerproviders.modules.DataModelModule;
import com.appdav.myperspective.common.daggerproviders.modules.ServiceModule;
import com.appdav.myperspective.common.data.EventData;
import com.appdav.myperspective.common.mvp.PresenterBase;
import com.appdav.myperspective.common.views.MyDialogs;
import com.appdav.myperspective.contracts.NewEventContract;
import com.appdav.myperspective.datamodels.EventDataModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

public class NewEventPresenter extends PresenterBase<NewEventContract.View> implements NewEventContract.Presenter,
        MyDialogs.AddPhotoDialog.AddPhotoDialogCallback,
        EventDataModel.EventDataModelCallback, MyDialogs.EditorExitDialog.ProfileEditorExitDialogCallback {

    private EventDataModel model;


    private Uri photoUri;


    @Inject
    public NewEventPresenter() {
        DataModelModule modelModule = new DataModelModule();
        modelModule.setEventDataModelCallback(this);
        model = DaggerDataModelComponent.builder().dataModelModule(modelModule).build().getEventDataModelInstance();
    }

    @Override
    public void viewIsReady() {
        getView().setInfoFieldFocus();
        getView().setNoPhotoView();
        ServiceModule serviceModule = new ServiceModule();
        serviceModule.setContext(getView().getContext());
    }

    @Override
    public void detachView() {
        super.detachView();
    }

    @Override
    public void destroy() {
        model = null;
        photoUri = null;
    }

    @Override
    public void onActionNewPhoto() {
        getView().startCameraActivity();
    }

    @Override
    public void onActionGallery() {
        getView().startGalleryActivity();
    }


    @Override
    public void onPhotoUploaded(Uri uri) {
        getView().setPhotoView(uri);
        this.photoUri = uri;
    }

    @Override
    public void onBackPressed() {
        getView().createExitDialog();
    }

    @Override
    public void onButtonAddPhotoClicked() {
        getView().createAddPhotoDialog();
    }

    @Override
    public void onButtonRemovePhotoClicked() {
        getView().setNoPhotoView();
        this.photoUri = null;
    }

    @Override
    public void onActionAccept() {
        this.createEvent();
    }

    @Override
    public void onErrorOccured() {
        getView().setActive();
        getView().setDatabaseErrorMessage();
    }

    @Override
    public void onEventRemoved(int arrayIndex) {
        //TODO
    }

    @Override
    public void onEventSuccessfullyUploaded() {
        getView().showUploadSuccessfulMessage();
        getView().finishActivity(true);
    }

    private long checkDate(String dateText) {
        Date date;
        if (dateText.length() > 0 && dateText.length() < EventData.EVENT_DATE_FORMAT.length())
            return -1;
        int day = Integer.valueOf(dateText.substring(0, 2));
        if (day < 1 || day > 31) return -1;
        int month = Integer.valueOf(dateText.substring(3, 5));
        if (month < 1 || month > 12) return -1;
        int year = Integer.valueOf(dateText.substring(6, 10));
        if (year < 2019) return -1;
        int hours = Integer.valueOf(dateText.substring(12, 14));
        if (hours > 24 || hours < 0) return -1;
        int minutes = Integer.valueOf(dateText.substring(15, 17));
        if (minutes < 0 || minutes > 60) return -1;
        try {
            date = new SimpleDateFormat(EventData.EVENT_DATE_FORMAT, Locale.getDefault()).parse(dateText);
            if (date != null) return date.getTime();
        } catch (ParseException e) {
            return -1;
        }
        return -1;


    }

    private void createEvent() {
        String eventName = getView().getNameText();
        if (eventName.isEmpty()) {
            getView().setNameFieldError();
            getView().setNameFieldFocus();
            return;
        }
        String stringDate = getView().getDateText();
        long date;
        if (stringDate.isEmpty()) {
            getView().setDateFieldEmptyError();
            getView().setDateFieldFocus();
            return;
        }
        date = checkDate(getView().getDateText());
        if (date == -1) {
            getView().setDateFieldFormatError();
            getView().setDateFieldFocus();
            return;
        }
        String info = getView().getInfoText();
        if (info.isEmpty()) {
            getView().setInfoFieldError();
            return;
        }
        Calendar currentDate = Calendar.getInstance(Locale.getDefault());
        if (getView().isEventTypeSwitchChecked()) {
            if ((currentDate.getTimeInMillis() + 3 * Constants.TWENTY_FOUR_HOURS_IN_MILLIS) < date) {
                getView().setDateFieldFocus();
                getView().setDateFieldInstantEventError();
                return;
            }
        }
        if (currentDate.getTimeInMillis() > date) {
            getView().setDateFieldFocus();
            getView().setPastTimeError();
            return;
        }
        EventData eventData = new EventData.Builder()
                .name(eventName)
                .date(date)
                .info(info)
                .imageUrl(photoUri == null ? "" : photoUri.toString())
                .build();
        getView().setInactive();
        if (getView().isEventTypeSwitchChecked()) {
            eventData.setEventType(EventData.EVENT_TYPE_INSTANT);
            uploadInstantEvent(eventData);
        } else uploadBasicEvent(eventData);
    }

    private void uploadBasicEvent(EventData eventData) {
        model.createNewEventEntry(eventData);
    }

    private void uploadInstantEvent(EventData eventData) {
        eventData.setEventType(EventData.EVENT_TYPE_INSTANT);
        model.createNewEventEntry(eventData, () -> {
            getView().showUploadSuccessfulMessage();
            getView().finishActivity(true);
        });

    }

    @Override
    public void onExitButtonClicked() {
        getView().finishActivity(false);
    }
}
