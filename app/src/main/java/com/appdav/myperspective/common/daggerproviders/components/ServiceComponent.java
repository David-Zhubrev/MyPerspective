package com.appdav.myperspective.common.daggerproviders.components;

import com.appdav.myperspective.common.daggerproviders.modules.ServiceModule;
import com.appdav.myperspective.common.services.Preferences;
import com.squareup.picasso.Picasso;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = ServiceModule.class)
public interface ServiceComponent {
    Picasso getPicasso();

    Preferences getPreferences();
}


