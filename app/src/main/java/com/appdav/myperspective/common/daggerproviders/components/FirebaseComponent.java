package com.appdav.myperspective.common.daggerproviders.components;

import com.appdav.myperspective.common.daggerproviders.modules.FirebaseModule;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = FirebaseModule.class)
@Singleton
public interface FirebaseComponent {
    FirebaseAuth getAuthInstance();

    AuthUI getAuthUI();

    List<AuthUI.IdpConfig> getAuthProviders();

    FirebaseDatabase getFirebaseDatabaseInstance();
}
