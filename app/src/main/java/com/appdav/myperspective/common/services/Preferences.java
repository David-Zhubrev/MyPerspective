package com.appdav.myperspective.common.services;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;

public class Preferences {

    private static final String SHARED_PREFERENCES_TAG = "sharedpreferences";

    private SharedPreferences sharedPreferences;

    public static final String DIALOG_UPDATE_NEVER_SHOW_AGAIN = "dialogupdatenevershow";

    @Inject
    public Preferences(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
    }

    public void writeBoolean(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getDontShowUpdateMessageAgain() {
        return sharedPreferences.getBoolean(DIALOG_UPDATE_NEVER_SHOW_AGAIN, false);
    }

    public String getString(String key) {
        return sharedPreferences.getString(key, null);
    }

}