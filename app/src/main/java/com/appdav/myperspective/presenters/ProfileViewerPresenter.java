package com.appdav.myperspective.presenters;

import com.appdav.myperspective.common.daggerproviders.components.DaggerDataModelComponent;
import com.appdav.myperspective.common.daggerproviders.modules.DataModelModule;
import com.appdav.myperspective.common.data.UserData;
import com.appdav.myperspective.common.mvp.PresenterBase;
import com.appdav.myperspective.contracts.ProfileViewerContract;
import com.appdav.myperspective.datamodels.UserDataModel;

public class ProfileViewerPresenter extends PresenterBase<ProfileViewerContract.View> implements ProfileViewerContract.Presenter, UserDataModel.UserEventListenter {

    private UserDataModel userDataModel;

    public ProfileViewerPresenter() {
        DataModelModule modelModule = new DataModelModule();
        modelModule.setUserEventListenter(this);
        userDataModel = DaggerDataModelComponent.builder().dataModelModule(modelModule).build().getUserDataModelInstance();
    }

    @Override
    public void viewIsReady() {
        userDataModel.findUserEntryByUid(getView().getUserId());
    }


    private UserData currentUser;

    /**
     * Method called when UserDataModel's method queried UserData from Firebase and found it
     *
     * @param user is UserData found and returned by Firebase
     */

    @Override
    public void onUserFound(UserData user) {
        currentUser = user;
        getView().fillViewsWithData(user);
    }

    /**
     * Called when query method has been interrupted by something or when some error has been occured
     */
    @Override
    public void onErrorOccured() {

    }

    /**
     * Method called when query method didn't find UserData by requested id
     */
    @Override
    public void onUserNotExist() {

    }

    @Override
    public void onActionEdit() {
        getView().proceedToEditor(currentUser);
    }

    @Override
    public void onMenuCreated() {
        String userId = getView().getUserId();
        String currentUserId = getView().getCurrentUserId();
        if (userId.equals(currentUserId)) getView().showEditMenu();
    }

    @Override
    public void destroy() {
        super.destroy();
        userDataModel = null;
    }

    @Override
    public void onUserChanged(UserData user) {
        this.currentUser = user;
        getView().fillViewsWithData(user);
        getView().showUpdateSuccessfulMessage();
    }
}
