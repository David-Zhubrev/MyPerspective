package com.appdav.myperspective.common.daggerproviders.modules;

import com.appdav.myperspective.datamodels.EventDataModel;
import com.appdav.myperspective.datamodels.LifehackDataModel;
import com.appdav.myperspective.datamodels.UserDataModel;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(includes = FirebaseModule.class)
public class DataModelModule {

    private UserDataModel.UserEventListenter userEventListenter;
    private EventDataModel.EventDataModelCallback eventDataModelCallback;
    private LifehackDataModel.LifehackDataModelCallback lifehackDataModelCallback;

    public void setUserEventListenter(UserDataModel.UserEventListenter listenter) {
        this.userEventListenter = listenter;
    }

    public void setEventDataModelCallback(EventDataModel.EventDataModelCallback callback) {
        this.eventDataModelCallback = callback;
    }

    public void setLifehackDataModelCallback(LifehackDataModel.LifehackDataModelCallback callback) {
        this.lifehackDataModelCallback = callback;
    }

    @Provides
    @Singleton
    UserDataModel userDataModel(FirebaseDatabase database, FirebaseStorage storage) {
        return new UserDataModel(database, storage, userEventListenter);
    }

    @Provides
    @Singleton
    EventDataModel eventDataModel(FirebaseDatabase database, FirebaseStorage storage, UserDataModel userDataModel) {
        return new EventDataModel(database, storage, userDataModel, eventDataModelCallback);
    }

    @Provides
    @Singleton
    LifehackDataModel lifehackDataModel(FirebaseDatabase database, UserDataModel userDataModel) {
        return new LifehackDataModel(database, userDataModel, lifehackDataModelCallback);
    }


}
