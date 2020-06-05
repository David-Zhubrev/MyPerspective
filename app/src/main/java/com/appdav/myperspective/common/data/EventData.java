package com.appdav.myperspective.common.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.appdav.myperspective.common.services.DateFormatter;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

@IgnoreExtraProperties
public class EventData implements Parcelable {

    public final static int GOING = 115;
    public final static int NOT_GOING = 116;
    public final static int INDETERMINATE = 117;

    public final static int EVENT_TYPE_BIRTHDAY = 14;
    public final static int EVENT_TYPE_COMMON = 13;
    public final static int EVENT_TYPE_INSTANT = 15;

    private String eventId;

    private String name;
    private long date;
    private String info;
    private String imageUrl;
    private ArrayList<String> peopleGoing;
    private ArrayList<String> peopleNotGoing;
    private int eventType = EVENT_TYPE_COMMON;

    @Exclude
    private int isCurrentUserGoing;

    @Exclude
    private boolean isInCurrentUserFavs;


    public final static String EVENT_DATE_FORMAT = "dd.MM.yyyy, HH:mm";


    protected EventData(Parcel in) {
        eventId = in.readString();
        name = in.readString();
        date = in.readLong();
        info = in.readString();
        imageUrl = in.readString();
        peopleGoing = in.createStringArrayList();
        peopleNotGoing = in.createStringArrayList();
        eventType = in.readInt();
        isCurrentUserGoing = in.readInt();
        isInCurrentUserFavs = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(eventId);
        dest.writeString(name);
        dest.writeLong(date);
        dest.writeString(info);
        dest.writeString(imageUrl);
        dest.writeStringList(peopleGoing);
        dest.writeStringList(peopleNotGoing);
        dest.writeInt(eventType);
        dest.writeInt(isCurrentUserGoing);
        dest.writeByte((byte) (isInCurrentUserFavs ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<EventData> CREATOR = new Creator<EventData>() {
        @Override
        public EventData createFromParcel(Parcel in) {
            return new EventData(in);
        }

        @Override
        public EventData[] newArray(int size) {
            return new EventData[size];
        }
    };

    @Exclude
    public DateFormatter.Formatter getDateFormatter() {
        return DateFormatter.getInstance().getFormatter(this.getDate());
    }

    public boolean dateEqualsTo(CalendarDay calendarDay) {
        DateFormatter.Formatter date = getDateFormatter();
        return (calendarDay.getYear() == date.getYear() &&
                calendarDay.getMonth() == date.getMonth() &&
                calendarDay.getDay() == date.getDay());
    }

    /**
     * Empty constructor required by Firebase Database
     * Should NOT be used to create new EventData on the client side,
     * use Builder static class instead
     */
    public EventData() {
    }

    public int isCurrentUserGoing() {
        return isCurrentUserGoing;
    }

    public void setCurrentUserIsGoing(int isCurrentUserGoing) {
        this.isCurrentUserGoing = isCurrentUserGoing;
    }

    @Exclude
    public boolean isInCurrentUserFavs() {
        return isInCurrentUserFavs;
    }

    @Exclude
    public void setIsInCurrentUserFavs(boolean isInCurrentUserFavs) {
        this.isInCurrentUserFavs = isInCurrentUserFavs;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof EventData)) return false;
        else return ((EventData) obj).eventId.equals(this.eventId);
    }


    /**
     * Method is used by EventDataModel to check if peopleGoing arrays of two equal(with same event ID)
     * EventData objects differs from each other
     * Used to check if new user has been added to peopleGoing array list
     *
     * @param newEventData is EventData to be compared to
     * @return true if two arrays differ from each other, false otherwise
     */
    public boolean peopleGoingArrayDiffersFrom(EventData newEventData) {
        if (this.peopleGoing == null) {
            return newEventData.peopleGoing != null;
        } else {
            if (newEventData.peopleGoing == null) return true;
            else if (this.peopleGoing.size() != newEventData.peopleGoing.size()) return true;
            else {
                for (String s : peopleGoing) {
                    if (!newEventData.peopleGoing.contains(s)) return true;
                }
                return false;
            }
        }
    }

    public boolean peopleNotGoingArrayDiffersFrom(EventData newEventData) {
        if (this.peopleNotGoing == null) {
            return newEventData.peopleNotGoing != null;
        } else {
            if (newEventData.peopleNotGoing == null) return true;
            else if (this.peopleNotGoing.size() != newEventData.peopleNotGoing.size()) return true;
            else {
                for (String s : peopleNotGoing) {
                    if (!newEventData.peopleNotGoing.contains(s)) return true;
                }
                return false;
            }
        }
    }


    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public ArrayList<String> getPeopleGoing() {
        return peopleGoing;
    }

    public void setPeopleGoing(ArrayList<String> peopleGoing) {
        this.peopleGoing = peopleGoing;
    }

    public ArrayList<String> getPeopleNotGoing() {
        return peopleNotGoing;
    }

    public void setPeopleNotGoing(ArrayList<String> peopleNotGoing) {
        this.peopleNotGoing = peopleNotGoing;
    }


    /**
     * used to add user (userID) to peopleGoing array
     *
     * @param uId is String to be added
     */
    public void addPeopleGoing(String uId) {
        if (peopleGoing == null) peopleGoing = new ArrayList<>();
        if (peopleGoing.contains(uId)) return;
        peopleGoing.add(uId);
    }

    /**
     * @see #addPeopleGoing(String) for more
     */
    public void deletePeopleGoing(String uId) {
        if (peopleGoing == null) return;
        peopleGoing.remove(uId);
    }

    /**
     * @see #addPeopleGoing(String) for more
     */
    public void addPeopleNotGoing(String uId) {
        if (peopleNotGoing == null) peopleNotGoing = new ArrayList<>();
        if (peopleNotGoing.contains(uId)) return;
        peopleNotGoing.add(uId);
    }

    /**
     * @see #addPeopleGoing(String) for more
     */
    public void deletePeopleNotGoing(String uId) {
        if (peopleNotGoing == null) return;
        peopleNotGoing.remove(uId);
    }

    /**
     * Used to get String date from EventData long date field
     */
    @Exclude
    public String getEventDateAndTimeText() {
        return new SimpleDateFormat(EVENT_DATE_FORMAT, Locale.getDefault()).format(new Date(date));
    }

    private final static String EVENT_DATE_FORMAT_NO_TIME = "dd MMMM";

    @Exclude
    public String getEventDateText() {
        return new SimpleDateFormat(EVENT_DATE_FORMAT_NO_TIME, Locale.getDefault()).format(new Date(date));
    }


    /**
     * Builder class should be used instead of constructor whenever new EventData should be created,
     * as it contains no null fields
     */
    public static class Builder {
        String eventId;
        String name = "";
        long date = 0;
        String info = "";
        String imageUrl = "";
        int eventType = EVENT_TYPE_COMMON;

        public Builder() {

        }

        public Builder eventType(int eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder eventId(String eventId) {
            this.eventId = eventId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder date(long date) {
            this.date = date;
            return this;
        }

        public Builder info(String info) {
            this.info = info;
            return this;
        }

        public EventData build() {
            EventData result = new EventData();
            result.eventId = this.eventId;
            result.imageUrl = this.imageUrl;
            result.name = this.name;
            result.date = this.date;
            result.info = this.info;
            result.eventType = eventType;
            return result;
        }
    }
}
