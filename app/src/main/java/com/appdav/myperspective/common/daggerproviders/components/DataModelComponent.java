package com.appdav.myperspective.common.daggerproviders.components;

import com.appdav.myperspective.common.daggerproviders.modules.DataModelModule;

import com.appdav.myperspective.datamodels.EventDataModel;
import com.appdav.myperspective.datamodels.LifehackDataModel;
import com.appdav.myperspective.datamodels.UserDataModel;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {DataModelModule.class})
public interface DataModelComponent {
    UserDataModel getUserDataModelInstance();

    EventDataModel getEventDataModelInstance();

    LifehackDataModel getLifehackDataModelInstance();
}
