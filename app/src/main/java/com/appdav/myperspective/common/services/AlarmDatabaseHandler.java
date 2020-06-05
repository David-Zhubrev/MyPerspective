package com.appdav.myperspective.common.services;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.SparseArray;

import androidx.annotation.Nullable;

import com.appdav.myperspective.common.data.AlarmData;


public class AlarmDatabaseHandler {

    private final String ALARM_DATABASE_NAME = "alarmdatabase";
    private final String EVENT_ID_COLUMN_NAME = "eventID";
    private final String EVENT_NAME_COLUMN_NAME = "eventName";
    private final String TIME_COLUMN_NAME = "time";
    private final String ALARM_TYPE_COLUMN_NAME = "alarmType";


    private static final int VERSION = 1;

    private SQLiteDatabase database;

    public AlarmDatabaseHandler(Context context) {
        DBHelper helper = new DBHelper(context, ALARM_DATABASE_NAME, null, VERSION);
        this.database = helper.getWritableDatabase();
    }

    public void addAlarmEntry(AlarmData alarmData) {
        ContentValues cv = new ContentValues();
        cv.put(EVENT_ID_COLUMN_NAME, alarmData.getEventId());
        cv.put(EVENT_NAME_COLUMN_NAME, alarmData.getEventName());
        cv.put(TIME_COLUMN_NAME, alarmData.getEventTime());
        cv.put(ALARM_TYPE_COLUMN_NAME, alarmData.getType());
        database.insert(ALARM_DATABASE_NAME, null, cv);
    }

    void removeAlarmEntry(String eventId) {
        database.delete(ALARM_DATABASE_NAME, EVENT_ID_COLUMN_NAME + " = " + eventId, null);
    }

    SparseArray<AlarmData> queryAllAlarmEntries() {
        Cursor c = database.query(ALARM_DATABASE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);
        if (c.moveToFirst()) {
            int eventIdIndex = c.getColumnIndex(EVENT_ID_COLUMN_NAME);
            int eventNameIndex = c.getColumnIndex(EVENT_NAME_COLUMN_NAME);
            int timeIndex = c.getColumnIndex(TIME_COLUMN_NAME);
            int typeIndex = c.getColumnIndex(ALARM_TYPE_COLUMN_NAME);
            int counter = 0;
            SparseArray<AlarmData> result = new SparseArray<>();
            do {
                result.put(counter, new AlarmData(c.getString(eventIdIndex), c.getString(eventNameIndex), c.getLong(timeIndex), c.getInt(typeIndex)));
                counter++;
            }
            while (c.moveToNext());
            c.close();
            return result;
        }
        c.close();
        return null;
    }


    private class DBHelper extends SQLiteOpenHelper {

        private DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table " + ALARM_DATABASE_NAME + "(" +
                    "id integer primary key autoincrement," +
                    EVENT_ID_COLUMN_NAME + " text," +
                    TIME_COLUMN_NAME + " long," +
                    EVENT_NAME_COLUMN_NAME + " text," +
                    ALARM_TYPE_COLUMN_NAME + " integer);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }


}
