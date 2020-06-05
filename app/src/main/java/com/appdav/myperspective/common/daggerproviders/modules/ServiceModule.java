package com.appdav.myperspective.common.daggerproviders.modules;

import android.content.Context;

import com.appdav.myperspective.common.services.Preferences;
import com.squareup.picasso.Picasso;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ServiceModule {

    private Context context;

    public void setContext(Context context) {
        this.context = context;
    }

    @Provides
    @Singleton
    Picasso picasso() {
        return Picasso.get();
    }


    @Provides
    @Singleton
    Preferences preferences() {
        return new Preferences(context);
    }


}
