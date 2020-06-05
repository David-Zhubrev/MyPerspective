package com.appdav.myperspective.datamodels;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.appdav.myperspective.common.data.EventData;
import com.appdav.myperspective.common.data.UserData;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class EventDataModel {

    private static final String LOG_TAG = "myLogs";

    private DatabaseReference dbRootRef, dbBirthdayRef;
    private StorageReference storageReference;
    private ArrayList<EventData> events;
    private UserDataModel userDataModel;
    private EventDataModelCallback callback;

    private String currentUserId;

    private EventDataChangesListener listener;

    private boolean arrayAlreadyExists = false;

    public boolean eventsArrayAlreadyExists() {
        return arrayAlreadyExists && events != null;
    }


    public EventDataModel(FirebaseDatabase database, FirebaseStorage storage, UserDataModel dataModel, EventDataModelCallback callback) {
        this.dbRootRef = database.getReference().getRoot().child("events");
        this.dbBirthdayRef = database.getReference().getRoot().child("birthdays");
        this.storageReference = storage.getReference().child("event_images");
        this.userDataModel = dataModel;
        this.callback = callback;
    }

    /**
     * This interface's methods are invoked by EventDataModel and by
     *
     * @see EventDataChangesListener classes
     * <p>
     * This methods should be called whenever some database changes are occured because
     * Firebase services are working asynchroniously and result of any method can't be returned directly
     */

    public interface EventDataModelCallback {

        /**
         * This method is invoked after calling
         *
         * @param events is the resulting ArrayList<EventData>
         * @see EventDataModel#queryEventsArray() method
         */
        default void onEventsArrayFirstGet(ArrayList<EventData> events) {

        }

        /**
         * This method should be called whenever database onCanceled() callback is called,
         * object type is wrong or when some EventDataModel class method that supposed to call any other interface methods
         * returned void and never called corresponding method
         */
        void onErrorOccured();

        /**
         * This method is called by
         *
         * @see EventDataChangesListener class
         * and signals that new Event has been uploaded by any user
         * <p>
         * This method supposed to tell Presenter that it should make UI changes corresponding to that,
         * like refreshing RecyclerView or activating Refresh Button
         */
        default void onNewEventGot() {

        }

        default void onEventRemoved(int arrayIndex) {
        }

        ;

        /**
         * This method is called when new Event has been uploaded to database by the current user
         * <p>
         * Used by NewEventActivity, informing that creating process is successful
         */

        default void onEventSuccessfullyUploaded() {

        }

        /**
         * This method is called whenever EventData#peopleGoingArray is changed by any user
         *
         * @param index indicates which EventDataModel#events item is changed
         */
        default void onPeopleGoingArrayChanged(int index) {

        }

        default void onUsersFavEventsGot(@Nullable ArrayList<EventData> favEvents) {

        }
    }

    /**
     * Setting currentUserId to EventDataModel
     *
     * @param uId is UserData#uId field and can be used to get UserData whenever it is necessary
     */
    public void setCurrentUser(String uId) {
        this.currentUserId = uId;
    }

    /**
     * Private method used to set boolean EventData#currentUserIsGoing so that it could be used by adapter.
     * Method should be called for EventData BEFORE UserDataModel#getArrayOfEventsForAdapter or UserDataModel#getEventForAdapter is called,
     * because after that EventData#peopleGoing and EventData#peopleNotGoing will contain users' names, not uIds
     *
     * @param eventData is EventData object to process
     */
    private void setCurrentUserIsGoing(EventData eventData) {
        if (eventData.getPeopleGoing() != null) {
            if (eventData.getPeopleGoing().contains(currentUserId)) {
                eventData.setCurrentUserIsGoing(EventData.GOING);
                return;
            }
        }
        if (eventData.getPeopleNotGoing() != null) {
            if (eventData.getPeopleNotGoing().contains(currentUserId)) {
                eventData.setCurrentUserIsGoing(EventData.NOT_GOING);
                return;
            }
        }
        eventData.setCurrentUserIsGoing(EventData.INDETERMINATE);
    }

    //returns ArrayList of events
    public ArrayList<EventData> getEventsArray() {
        if (newEvents != null && !newEvents.isEmpty()) {
            events.addAll(0, newEvents);
            newEvents = null;
        }
        return events;
    }

    /**
     * This method should be called when presenter, using this Model class is firstly created
     * This method calls
     *
     * @see EventDataModelCallback#onEventsArrayFirstGet(ArrayList)
     * After that, getEventsArray() should be called to get events array, or
     * @see #nullifyDataSet() to nullify events array in model
     */
    public void queryEventsArray() {
        dbRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (events == null) events = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    EventData eventData = snapshot.getValue(EventData.class);
                    if (eventData != null) {
                        setCurrentUserIsGoing(eventData);
                        events.add(0, eventData);
                    }
                }
                userDataModel.getArrayOfEventsForAdapter(currentUserId, events, new UserDataModel.ConvertEventsForAdapterCallback() {

                    @Override
                    public void onEventsArrayGot(ArrayList<EventData> arrayOfEventsWithNames) {
                        events = arrayOfEventsWithNames;
                        arrayAlreadyExists = true;
                        callback.onEventsArrayFirstGet(events);
                    }

                    @Override
                    public void onUserEntryNotFound(String uId) {
                        dbRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    EventData eventData = snapshot.getValue(EventData.class);
                                    if (eventData != null) {
                                        eventData.deletePeopleGoing(uId);
                                        eventData.deletePeopleNotGoing(uId);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onError() {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(LOG_TAG, "model onErrorOccurred");
                callback.onErrorOccured();
            }
        });
    }

    public interface QueryPeopleGoingCallback {
        void queryPeopleGoing(ArrayList<UserData> users);

        void queryPeopleNotGoing(ArrayList<UserData> users);
    }

    public void queryPeopleGoing(String eventId, QueryPeopleGoingCallback callback) {
        dbRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                EventData event = dataSnapshot.child(eventId).getValue(EventData.class);
                if (event == null) EventDataModel.this.callback.onErrorOccured();
                else {
                    userDataModel.queryUsersFromArrayId(event.getPeopleGoing(), callback::queryPeopleGoing);
                    userDataModel.queryUsersFromArrayId(event.getPeopleNotGoing(), callback::queryPeopleNotGoing);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void queryFavEventsArray(String currentUserId) {
        userDataModel.queryUsersFavEventsList(currentUserId, new UserDataModel.GetUsersFavEventsListCallback() {
            @Override
            public void onFavEventsListGot(@Nullable ArrayList<String> favEventIds) {
                if (favEventIds == null || favEventIds.isEmpty()) {
                    callback.onUsersFavEventsGot(null);
                } else {
                    ArrayList<EventData> favEvents = new ArrayList<>();
                    dbRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String eventId = snapshot.child("eventId").getValue(String.class);
                                if (favEventIds.contains(eventId)) {
                                    favEvents.add(snapshot.getValue(EventData.class));
                                }
                            }
                            callback.onUsersFavEventsGot(favEvents);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            callback.onErrorOccured();
                        }
                    });
                }
            }

            @Override
            public void onError() {
                callback.onErrorOccured();
            }
        });
    }

    /**
     * This method is used to put current user to EventData#peopleGoing list
     *
     * @param eventId should be passed to get corresponding EventData from database
     */
    public void addCurrentUserToGoingList(String eventId) {
        dbRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                EventData currentEvent = dataSnapshot.child(eventId).getValue(EventData.class);
                if (currentEvent != null) {
                    currentEvent.addPeopleGoing(currentUserId);
                    dbRootRef.child(eventId).setValue(currentEvent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onErrorOccured();
            }
        });
    }


    /**
     * Adds user to EventData#peopleNotGoing
     *
     * @see #addCurrentUserToGoingList(String) for more
     */
    public void addCurrentUserToNotGoingList(String eventId) {
        dbRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                EventData currentEvent = dataSnapshot.child(eventId).getValue(EventData.class);
                if (currentEvent != null) {
                    currentEvent.addPeopleNotGoing(currentUserId);
                    dbRootRef.child(eventId).setValue(currentEvent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onErrorOccured();
            }
        });
    }

    /**
     * Deletes user with eventId from both EventData#peopleGoing and #peopleNotGoing lists
     *
     * @see #addCurrentUserToGoingList(String) for more
     */
    public void deleteCurrentUserFromGoingLists(String eventId) {
        dbRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                EventData currentEvent = dataSnapshot.child(eventId).getValue(EventData.class);
                if (currentEvent != null) {
                    currentEvent.deletePeopleGoing(currentUserId);
                    currentEvent.deletePeopleNotGoing(currentUserId);
                    dbRootRef.child(eventId).setValue(currentEvent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onErrorOccured();
            }
        });
    }

    /**
     * This method adds new entry to database root
     * Method calls
     *
     * @param eventData filled with data but without eventId should be passed
     * @see EventDataModelCallback#onEventSuccessfullyUploaded()
     * if data successfuly uploaded
     */
    public void createNewEventEntry(EventData eventData) {
        String eventId = dbRootRef.push().getKey();
        if (eventId == null) {
            callback.onErrorOccured();
            return;
        }
        eventData.setEventId(eventId);
        if (!eventData.getImageUrl().isEmpty()) {
            storageReference.child(eventId).putFile(Uri.parse(eventData.getImageUrl())).addOnSuccessListener(taskSnapshot ->
                    storageReference.child(eventId).getDownloadUrl().addOnSuccessListener(uri -> {
                        eventData.setImageUrl(uri.toString());
                        dbRootRef.child(eventId).setValue(eventData).addOnSuccessListener(aVoid -> {
                            Log.d(LOG_TAG, "onEventWithPhotoUploaded");
                            callback.onEventSuccessfullyUploaded();
                        });
                    }));
        } else dbRootRef.child(eventId).setValue(eventData).addOnSuccessListener(aVoid -> {
            Log.d(LOG_TAG, "onEventWithoutPhotoUploaded");
            callback.onEventSuccessfullyUploaded();
        });
    }

    public void createNewEventEntry(EventData eventData, OnSuccessListener callback) {
        String eventId = dbRootRef.push().getKey();
        if (eventId == null) {
            EventDataModel.this.callback.onErrorOccured();
            return;
        }
        eventData.setEventId(eventId);
        if (!eventData.getImageUrl().isEmpty()) {
            storageReference.child(eventId).putFile(Uri.parse(eventData.getImageUrl())).addOnSuccessListener(taskSnapshot ->
                    storageReference.child(eventId).getDownloadUrl().addOnSuccessListener(uri -> {
                        eventData.setImageUrl(uri.toString());
                        dbRootRef.child(eventId).setValue(eventData).addOnSuccessListener(aVoid -> {
                            Log.d(LOG_TAG, "onEventWithPhotoUploaded");
                            callback.onSuccess();
                        });
                    }));
        } else dbRootRef.child(eventId).setValue(eventData).addOnSuccessListener(aVoid -> {
            Log.d(LOG_TAG, "onEventWithoutPhotoUploaded");
            callback.onSuccess();
        });
    }

    public interface OnSuccessListener {
        void onSuccess();
    }

    public void removeEventEntry(String eventId) {
        dbRootRef.child(eventId).removeValue();
    }

    public void removeEventEntry(String eventId, OnSuccessListener listener) {
        dbRootRef.child(eventId).removeValue().addOnSuccessListener(aVoid -> listener.onSuccess());
    }

    /**
     * Use this to add listener to event database root, so any changes to database made by any user
     * will invoke callback methods and let presenter know, that some to database changes has been occurred
     */
    public void subscribeForEventChanges() {
        Log.d(LOG_TAG, "subscribed for event changes");
        if (listener == null) listener = new EventDataChangesListener();
        dbRootRef.addChildEventListener(listener);
    }


    /**
     * Use this method to delete listener from database root so that changes callback methods
     * wouldn't be invoked
     */
    public void unsubscribeFromEventChanges() {
        if (listener != null) {
            dbRootRef.removeEventListener(listener);
            listener = null;
        }
    }


    /**
     * This interface is specific callback interface for
     *
     * @see #addEventToFavs(String, OnEventToFavsChangeListener) method
     */
    public interface OnEventToFavsChangeListener {
        /**
         * Called when event has been successfully added to user's fav list
         *
         * @param arrayIndex is index of event from EventDataModel events array
         */
        void onEventAddedToFavs(int arrayIndex);


        /*
         * Called when event has been successfully deleted from user's fav list
         * *@see #onEventAddedToFavs(int)
         */
        void onEventRemovedFromFavs(int arrayIndex);

        /**
         * Called when action has been interrupted or when some error has been occured
         *
         * @see #onEventAddedToFavs(int)
         */
        void onError(int arrayIndex);
    }

    public interface BirthdayQueryCallback {
        void onBirthdayArrayGot(ArrayList<EventData> birthdayDataArrayList);
    }

    public void queryBirthdayEvents(BirthdayQueryCallback callback) {
        dbBirthdayRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<EventData> birthdayDataArrayList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    EventData bd = snapshot.getValue(EventData.class);
                    if (bd != null) birthdayDataArrayList.add(bd);
                }
                callback.onBirthdayArrayGot(birthdayDataArrayList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * This method is used to add event to current user's favourite events list
     *
     * @param eventId  will be written to list
     * @param listener instance is needed
     */
    public void addEventToFavs(String eventId, OnEventToFavsChangeListener listener) {
        EventData temp = new EventData();
        temp.setEventId(eventId);
        userDataModel.addEventToFavs(currentUserId, eventId, new UserDataModel.EventToFavsCallback() {
            @Override
            public void onSuccess(int actionCode) {
                if (actionCode == ACTION_ADDED) {
                    events.get(events.indexOf(new EventData.Builder().eventId(eventId).build())).setIsInCurrentUserFavs(true);
                    listener.onEventAddedToFavs(events.indexOf(temp));
                }
            }

            @Override
            public void onError() {
                listener.onError(events.indexOf(temp));
            }
        });
    }

    /**
     * Used to nullify events array within EventDataModel
     * Should be called whenever presenter is destroyed to release resources
     */
    public void nullifyDataSet() {
        events = null;
        arrayAlreadyExists = false;
    }

    /**
     * @see #addEventToFavs(String, OnEventToFavsChangeListener)
     */
    public void removeEventFromFavs(String eventId, OnEventToFavsChangeListener listener) {
        EventData temp = new EventData();
        temp.setEventId(eventId);
        userDataModel.removeEventFromFavs(currentUserId, eventId, new UserDataModel.EventToFavsCallback() {
            @Override
            public void onSuccess(int actionCode) {
                if (actionCode == UserDataModel.EventToFavsCallback.ACTION_REMOVED) {
                    events.get(events.indexOf(new EventData.Builder().eventId(eventId).build())).setIsInCurrentUserFavs(true);
                    listener.onEventRemovedFromFavs(events.indexOf(temp));
                }
            }

            @Override
            public void onError() {
                listener.onError(events.indexOf(temp));
            }
        });
    }

    /**
     * This class manages events changes made by any users
     * Should be attached to root database reference with
     *
     * @see #subscribeForEventChanges()
     */
    private ArrayList<EventData> newEvents;

    public class EventDataChangesListener implements ChildEventListener {

        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            if (events == null) return;
            EventData eventData = dataSnapshot.getValue(EventData.class);
            if (eventData == null) return;
            if (events.contains(eventData) || ((newEvents != null) && newEvents.contains(eventData)))
                return;
            setCurrentUserIsGoing(eventData);
            userDataModel.getEventForAdapter(currentUserId, eventData, new UserDataModel.ConvertEventsForAdapterCallback() {

                @Override
                public void onEventGot(EventData eventWithNames) {
                    if (newEvents == null) newEvents = new ArrayList<>();
                    newEvents.add(0, eventWithNames);
                    callback.onNewEventGot();
                }

                @Override
                public void onUserEntryNotFound(String uId) {
                    eventData.deletePeopleGoing(uId);
                    eventData.deletePeopleGoing(uId);
                    dbRootRef.child(eventData.getEventId()).setValue(eventData);
                }

                @Override
                public void onError() {
                    callback.onErrorOccured();
                }
            });
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            if (events == null) return;
            EventData eventData = dataSnapshot.getValue(EventData.class);
            if (eventData == null) return;
            setCurrentUserIsGoing(eventData);
            userDataModel.getEventForAdapter(currentUserId, eventData, new UserDataModel.ConvertEventsForAdapterCallback() {

                @Override
                public void onEventGot(EventData eventWithNames) {
                    if (events.contains(eventWithNames)) {
                        int index = events.indexOf(eventWithNames);
                        events.set(index, eventWithNames);
                        callback.onPeopleGoingArrayChanged(index);
                    } else if (newEvents != null && newEvents.contains(eventWithNames)) {
                        int index = newEvents.indexOf(eventWithNames);
                        newEvents.set(index, eventWithNames);
                    }
                }

                @Override
                public void onUserEntryNotFound(String uId) {
                    eventData.deletePeopleGoing(uId);
                    eventData.deletePeopleNotGoing(uId);
                    dbRootRef.child(eventData.getEventId()).setValue(eventData);
                }

                @Override
                public void onError() {
                    callback.onErrorOccured();
                }
            });
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            if (events == null) return;
            EventData eventData = dataSnapshot.getValue(EventData.class);
            if (eventData == null) return;
            if (events.contains(eventData)) {
                int index = events.indexOf(eventData);
                events.remove(eventData);
                callback.onEventRemoved(index);
            } else if (newEvents != null) {
                newEvents.remove(eventData);
            }
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            callback.onErrorOccured();
        }
    }
}
