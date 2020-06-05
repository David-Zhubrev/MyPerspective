package com.appdav.myperspective.datamodels;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.HashMap;

public class InstantEventsDatabase {

    private final static String INSTANT_EVENTS_DATABASE_NAME = "myInstantEventsDB";
    private final static int DB_VERSION = 1;
    private final static String EVENT_ID_COLUMN_NAME = "eventId";
    private final static String DATE_COLUMN_NAME = "eventDate";

    private SQLiteDatabase database;

    public InstantEventsDatabase(Context context) {
        InstantEventsDatabaseHelper helper = new InstantEventsDatabaseHelper(context, INSTANT_EVENTS_DATABASE_NAME, null, DB_VERSION);
        database = helper.getWritableDatabase();
    }

    public HashMap<String, Long> getCreatedInstantEventList() {
        Cursor c = database.query(INSTANT_EVENTS_DATABASE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);
        if (c.moveToFirst()) {
            HashMap<String, Long> result = new HashMap<>();
            int eventIdIndex = c.getColumnIndex(EVENT_ID_COLUMN_NAME);
            int dateIndex = c.getColumnIndex(DATE_COLUMN_NAME);
            do {
                result.put(c.getString(eventIdIndex), c.getLong(dateIndex));
            }
            while (c.moveToNext());
            c.close();
            return result;
        }
        c.close();
        return null;
    }

    public void addInstantEventToLocalDatabase(String eventId, long date) {
        ContentValues cv = new ContentValues();
        cv.put(EVENT_ID_COLUMN_NAME, eventId);
        cv.put(DATE_COLUMN_NAME, date);
        database.insert(INSTANT_EVENTS_DATABASE_NAME, null, cv);
    }

    public void deleteInstantEventFromLocalDatabase(String eventId) {
        database.delete(INSTANT_EVENTS_DATABASE_NAME, EVENT_ID_COLUMN_NAME + " = '" + eventId + "'", null);
    }


    private class InstantEventsDatabaseHelper extends SQLiteOpenHelper {


        private String name;
        private int version;

        private InstantEventsDatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
            this.name = name;
            this.version = version;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table " + name + " (" +
                    "id integer primary key autoincrement," +
                    EVENT_ID_COLUMN_NAME + " text," +
                    DATE_COLUMN_NAME + " long);");
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

}
