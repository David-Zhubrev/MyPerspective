package com.appdav.myperspective.common.services;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.appdav.myperspective.App;
import com.appdav.myperspective.common.Constants;
import com.appdav.myperspective.common.daggerproviders.components.DaggerFirebaseComponent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;


public class UpdateNotificationService extends IntentService {

    private DatabaseReference dbRef;

    public UpdateNotificationService(String name) {
        super(name);
        this.dbRef = DaggerFirebaseComponent.builder().build().getFirebaseDatabaseInstance().getReference().child("version");
    }

    public UpdateNotificationService() {
        super("UpdateNotificationService");
        this.dbRef = DaggerFirebaseComponent.builder().build().getFirebaseDatabaseInstance().getReference().child("version");
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Double newVersion = dataSnapshot.child("android_app_version").getValue(Double.class);
                if (newVersion != null && newVersion > App.APP_VERSION) {
                    Intent intent = new Intent(Constants.ACTION_UPDATE_IS_READY);
                    intent.putExtra(Constants.UPDATE_LINK_INTENT_EXTRA, dataSnapshot.child("android_app_link").getValue(String.class));
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
