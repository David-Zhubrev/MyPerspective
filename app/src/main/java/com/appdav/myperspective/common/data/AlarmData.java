package com.appdav.myperspective.common.data;

import android.os.Parcel;
import android.os.Parcelable;

public class AlarmData implements Parcelable {

    public static final int ALARM_TYPE_ONE_HOUR = 1;
    public static final int ALARM_TYPE_SIX_HOURS = 2;
    public static final int ALARM_TYPE_TWENTY_FOUR_HOURS = 3;

    private final String eventId;
    private final String eventName;
    private final long eventTime;
    private final int type;

    public AlarmData(String eventId, String eventName, long eventTime, int type) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventTime = eventTime;
        this.type = type;
    }

    private AlarmData(Parcel in) {
        eventId = in.readString();
        eventName = in.readString();
        eventTime = in.readLong();
        type = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(eventId);
        dest.writeString(eventName);
        dest.writeLong(eventTime);
        dest.writeInt(type);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AlarmData> CREATOR = new Creator<AlarmData>() {
        @Override
        public AlarmData createFromParcel(Parcel in) {
            return new AlarmData(in);
        }

        @Override
        public AlarmData[] newArray(int size) {
            return new AlarmData[size];
        }
    };

    public String getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public long getEventTime() {
        return eventTime;
    }

    public int getType() {
        return type;
    }
}
