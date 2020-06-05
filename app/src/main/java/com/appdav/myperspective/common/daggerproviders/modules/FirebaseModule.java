package com.appdav.myperspective.common.daggerproviders.modules;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Arrays;
import java.util.List;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class FirebaseModule {

    @Provides
    @Singleton
    FirebaseAuth firebaseAuth() {
        return FirebaseAuth.getInstance();
    }

    @Provides
    @Singleton
    List<AuthUI.IdpConfig> authProviders() {
        return Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build());

    }

    @Provides
    @Singleton
    AuthUI authUI() {
        return AuthUI.getInstance();
    }

    @Provides
    @Singleton
    FirebaseDatabase firebaseDatabase() {
        return FirebaseDatabase.getInstance();
    }

    @Provides
    @Singleton
    FirebaseStorage firebaseStorage() {
        return FirebaseStorage.getInstance();
    }

}
