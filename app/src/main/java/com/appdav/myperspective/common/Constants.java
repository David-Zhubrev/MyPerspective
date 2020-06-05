package com.appdav.myperspective.common;

import android.Manifest;

public class Constants {

    public static final String UNIQUE_PERIODIC_WORK_NAME = "uniqueperiodicwork";

    public static final String DEFAULT_USER_PIC_URL = "https://firebasestorage.googleapis.com/v0/b/perspective-1f565.appspot.com/o/profile_photos%2Fdefault_user_pic.png?alt=media&token=e62f2b93-b0d3-4311-8396-3da98cc310e8";
    public static final String DEFAULT_EVENT_PIC_URL = "https://firebasestorage.googleapis.com/v0/b/perspective-1f565.appspot.com/o/event_pics%2Fdefault_event_pic.jpg?alt=media&token=d05b8048-f3d5-45b3-bb22-3b4adf091213";

    public static final String ACTION_WITH_CURRENT_USER_INTENT = "currentuser";
    public static final String ACTION_UPDATE_IS_READY = "com.appdav.myperspective.ACTION_UPDATE_READY";
    public static final String UPDATE_LINK_INTENT_EXTRA = "updatelink";

    public static final String EVENT_EXTRA = "eventidextra";

    public static final String NOTIFICATION_EVENT_NAME_INTENT_EXTRA = "eventname";
    public static final String NOTIFICATION_EVENT_DATE_INTENT_EXTRA = "eventdate";
    public static final String NOTIFICATION_EVENT_TYPE_EXTRA = "eventtype";

    public static final String ALARM_DATA_EXTRA = "alarmdataextra";

    public static final int NOTIFICATION_TYPE_EVENT = 145;
    public static final int NOTIFICATION_TYPE_BIRTHDAY = 150;
    public static final int NOTIFICATION_TYPE_UPDATE = 155;

    public static final String ACTION_WITH_ANY_USER = "anyuser";
    public static final String ACTION_NEW_USER = "newuser";

    public static final String[] CAMERA_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    public static final String[] GALLERY_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    //NOTE: actually this values are 2 minutes less than actual value;
    public static final long SIX_HOURS_IN_MILLIS = 21360000;
    public static final long ONE_HOURS_IN_MILLIS = 3360000;
    public static final long TWENTY_FOUR_HOURS_IN_MILLIS = 86160000;

    public static final String NOTIFICATION_CHANNEL_ID = "com.appdav.myperspective event notifications channel";

    public static final String USER_HAS_EDITOR_RIGHTS = "editorRights";


}
