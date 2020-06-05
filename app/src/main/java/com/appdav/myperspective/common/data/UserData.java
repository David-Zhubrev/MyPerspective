package com.appdav.myperspective.common.data;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.appdav.myperspective.R;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

@IgnoreExtraProperties
public class UserData implements Parcelable {

    @Exclude
    private static final String DATE_OF_BIRTH_FORMAT_NO_YEAR = "d MMM";
    @Exclude
    private static final String DATE_OF_BIRTH_FORMAT_WITH_YEAR = "d MMM yyyy";
    @Exclude
    private static final String DATE_OF_BIRTH_FORMAT_NUMBERS = "dd.MM.yyyy";

    private String uId;
    private String name;
    private String surname;

    private String phoneNumber;

    private long dateOfBirth;
    private int joiningYear;
    private int rank;

    private ArrayList<String> favEvents;

    private String photoUri;

    private String about;

    public boolean hasEditorRights = false;
    public boolean bannedFromCreatingEntries = false;


    protected UserData(Parcel in) {
        uId = in.readString();
        name = in.readString();
        surname = in.readString();
        phoneNumber = in.readString();
        dateOfBirth = in.readLong();
        joiningYear = in.readInt();
        rank = in.readInt();
        favEvents = in.createStringArrayList();
        photoUri = in.readString();
        about = in.readString();
        hasEditorRights = in.readByte() != 0;
        bannedFromCreatingEntries = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uId);
        dest.writeString(name);
        dest.writeString(surname);
        dest.writeString(phoneNumber);
        dest.writeLong(dateOfBirth);
        dest.writeInt(joiningYear);
        dest.writeInt(rank);
        dest.writeStringList(favEvents);
        dest.writeString(photoUri);
        dest.writeString(about);
        dest.writeByte((byte) (hasEditorRights ? 1 : 0));
        dest.writeByte((byte) (bannedFromCreatingEntries ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UserData> CREATOR = new Creator<UserData>() {
        @Override
        public UserData createFromParcel(Parcel in) {
            return new UserData(in);
        }

        @Override
        public UserData[] newArray(int size) {
            return new UserData[size];
        }
    };

    @Exclude
    public String getDateOfBirthTextWithYear() {
        return new SimpleDateFormat(DATE_OF_BIRTH_FORMAT_WITH_YEAR, Locale.getDefault()).format(new Date(dateOfBirth));
    }

    public String getDateOfBirthTextFormatNumbers() {
        return new SimpleDateFormat(DATE_OF_BIRTH_FORMAT_NUMBERS, Locale.getDefault()).format(new Date(dateOfBirth));
    }

    @Exclude
    public String getDateOfBirthTextFormatNoYear() {
        return new SimpleDateFormat(DATE_OF_BIRTH_FORMAT_NO_YEAR, Locale.getDefault()).format(new Date(dateOfBirth));
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }


    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }


    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof UserData)) return false;
        return this.getuId().equals(((UserData) obj).getuId());
    }

    /**
     * Public empty constructor required by Firebase Database
     * Should NOT be used for creating UserData instances on the client side
     * Use Builder class instead
     */


    public UserData() {
    }

    /**
     * Used to add eventId to UserData's favEvents array
     * Should be used instead of favEvents.add() method
     *
     * @param eventId is String to be added to array list
     */
    public void addEventToFavs(String eventId) {
        if (favEvents == null) favEvents = new ArrayList<>();
        favEvents.add(eventId);
    }

    /**
     * Used to remove eventId to UserData's favEvents array
     * Should be used instead of favEvents.remove() method
     *
     * @param eventId is String to be removed to array list
     */
    public void removeEventFromFavs(String eventId) {
        if (favEvents != null)
            favEvents.remove(eventId);
    }

    /**
     * Used to easily retrieve user's name and surname pair
     *
     * @return String name and surname pair
     */
    @Exclude
    public String getUserNameAndSurname() {
        return this.getName() + " " + getSurname();
    }


    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public long getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(long dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public int getJoiningYear() {
        return joiningYear;
    }


    public void setJoiningYear(int joiningYear) {
        this.joiningYear = joiningYear;
    }

    public int getRank() {
        return rank;
    }

    public String getRankText(Context context) {
        return context.getResources().getStringArray(R.array.ranks)[rank];
    }

    public String getJoiningYearText(Context context) {
        return context.getResources().getStringArray(R.array.joining_year)[joiningYear];
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public ArrayList<String> getFavEvents() {
        return favEvents;
    }

    public void setFavEvents(ArrayList<String> favEvents) {
        this.favEvents = favEvents;
    }

    public String getLifehackAuthorText(Context context) {
        String result;
        result = getUserNameAndSurname() + "\n";
        result += getRankText(context) + " ";
        result += context.getString(R.string.spo_name);
        return result;
    }


    /**
     * Builder class should be used whenever new UserData object should be created on the client side
     */
    public static class Builder {

        public Builder() {

        }

        String uid;
        String name = "";
        String surname = "";
        String photoUri = "";

        String phoneNumber = "";
        String about = "";

        long dateOfBirth = 0;
        int joiningYear = 0;
        int rank = 0;

        public UserData build() {
            UserData result = new UserData();
            result.uId = this.uid;
            result.name = this.name;
            result.surname = this.surname;
            result.phoneNumber = this.phoneNumber;
            result.dateOfBirth = this.dateOfBirth;
            result.joiningYear = this.joiningYear;
            result.rank = this.rank;
            result.photoUri = photoUri;
            result.about = about;
            return result;
        }

        public Builder about(String about) {
            this.about = about;
            return this;
        }

        public Builder uId(String uid) {
            this.uid = uid;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder surname(String surname) {
            this.surname = surname;
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder dateOfBirth(long dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder joiningYear(int joiningYear) {
            this.joiningYear = joiningYear;
            return this;
        }

        public Builder rank(int rank) {
            this.rank = rank;
            return this;
        }

        public Builder photoUri(String photoUri) {
            this.photoUri = photoUri;
            return this;
        }
    }
}
