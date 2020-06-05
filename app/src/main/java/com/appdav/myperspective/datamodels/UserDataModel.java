package com.appdav.myperspective.datamodels;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.appdav.myperspective.common.data.EventData;
import com.appdav.myperspective.common.data.LifehackData;
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

import javax.inject.Inject;

public class UserDataModel {

    private DatabaseReference dbRoot;
    private StorageReference storage;
    private UserEventListenter callback;

    private ArrayList<UserData> users;

    @Inject
    public UserDataModel(FirebaseDatabase database, FirebaseStorage storage, UserEventListenter callback) {
        this.dbRoot = database.getReference().child("users");
        this.storage = storage.getReference().child("profile_pics");
        this.callback = callback;
    }

    /**
     * This interface should be implemented or passed by any class, using UserDataModel
     * As Firebase works asynchroniously, this interface's methods called when Firebase completes actions of
     * this class methods also returning the action's result
     */
    public interface UserEventListenter {

        default void onUsersArrayGot(ArrayList<UserData> users) {

        }

        /**
         * Method called when UserDataModel's method queried UserData from Firebase and found it
         *
         * @param user is UserData found and returned by Firebase
         */
        default void onUserFound(UserData user) {
        }

        default void onNewUserCreated(UserData user) {
        }

        default void onUserEntryDeleted() {
        }

        /**
         * Method called when query method didn't find UserData by requested id
         */
        default void onUserNotExist() {
        }

        default void onUserUpdatedSuccessfully(UserData userData) {
        }

        /**
         * Called when query method has been interrupted by something or when some error has been occured
         */
        void onErrorOccured();
    }

    public interface GetUsersFavEventsListCallback {
        void onFavEventsListGot(@Nullable ArrayList<String> favEventIds);

        void onError();
    }

    public interface ConvertEventsForAdapterCallback {

        default void onEventGot(EventData eventWithNames) {
        }

        default void onEventsArrayGot(ArrayList<EventData> arrayOfEventsWithNames) {
        }

        void onUserEntryNotFound(String uId);

        void onError();
    }

    /**
     * This method is called by EventDataModel to convert existing ArrayList of EventData into format
     * expected by EventFeedAdapter
     *
     * @param currentUserId is used to check whether user is going, not going to this event, or he didn't made a decision yet
     * @param events        is ArrayList of EventData to be converted
     * @param callback      used to get resulting data
     */
    void getArrayOfEventsForAdapter(String currentUserId, ArrayList<EventData> events, ConvertEventsForAdapterCallback callback) {
        if (events == null) return;
        dbRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (EventData event : events) {
                    if (event.getPeopleGoing() != null) {
                        ArrayList<String> resultPeopleGoing = new ArrayList<>();
                        for (String uId : event.getPeopleGoing()) {
                            UserData user = dataSnapshot.child(uId).getValue(UserData.class);
                            if (user != null)
                                resultPeopleGoing.add(user.getUserNameAndSurname());
                            else callback.onUserEntryNotFound(uId);
                        }
                        event.setPeopleGoing(resultPeopleGoing);
                    }
                    if (event.getPeopleNotGoing() != null) {
                        ArrayList<String> resultPeopleNotGoing = new ArrayList<>();
                        for (String uId : event.getPeopleNotGoing()) {
                            UserData user = dataSnapshot.child(uId).getValue(UserData.class);
                            if (user != null)
                                resultPeopleNotGoing.add(user.getUserNameAndSurname());
                            else callback.onUserEntryNotFound(uId);
                        }
                        event.setPeopleNotGoing(resultPeopleNotGoing);
                    }
                    UserData user = dataSnapshot.child(currentUserId).getValue(UserData.class);
                    if (user != null) {
                        if (user.getFavEvents() != null && user.getFavEvents().contains(event.getEventId())) {
                            event.setIsInCurrentUserFavs(true);
                        } else event.setIsInCurrentUserFavs(false);
                    } else callback.onError();
                }
                callback.onEventsArrayGot(events);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError();
            }
        });
    }


    public void queryAllUsersList() {
        dbRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (users == null) users = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserData userData = snapshot.getValue(UserData.class);
                    if (userData != null) {
                        users.add(userData);
                    }
                }
                callback.onUsersArrayGot(users);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void addOnChangeListener(ChildEventListener listener) {
        dbRoot.addChildEventListener(listener);
    }

    public void removeChangeListener(ChildEventListener listener) {
        dbRoot.removeEventListener(listener);
    }

    void getEventForAdapter(String currentUserId, EventData eventData, ConvertEventsForAdapterCallback callback) {
        dbRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (eventData.getPeopleGoing() != null) {
                    ArrayList<String> resultPeopleGoing = new ArrayList<>();
                    for (String uId : eventData.getPeopleGoing()) {
                        UserData userData = dataSnapshot.child(uId).getValue(UserData.class);
                        if (userData != null) {
                            resultPeopleGoing.add(userData.getUserNameAndSurname());
                        } else callback.onUserEntryNotFound(uId);
                    }
                    eventData.setPeopleGoing(resultPeopleGoing);
                }
                if (eventData.getPeopleNotGoing() != null) {
                    ArrayList<String> resultPeopleNotGoing = new ArrayList<>();
                    for (String uId : eventData.getPeopleNotGoing()) {
                        UserData userData = dataSnapshot.child(uId).getValue(UserData.class);
                        if (userData != null) {
                            resultPeopleNotGoing.add(userData.getUserNameAndSurname());
                        } else callback.onUserEntryNotFound(uId);
                    }
                    eventData.setPeopleNotGoing(resultPeopleNotGoing);
                }
                UserData currentUser = dataSnapshot.child(currentUserId).getValue(UserData.class);
                if (currentUser != null) {
                    if (currentUser.getFavEvents() != null && currentUser.getFavEvents().contains(eventData.getEventId())) {
                        eventData.setIsInCurrentUserFavs(true);
                    } else eventData.setIsInCurrentUserFavs(false);
                } else callback.onError();
                callback.onEventGot(eventData);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError();
            }
        });

    }

    /**
     * Used to get UserData Firebase entry by passing user ID
     *
     * @param uid is passed to find UserData entry
     */
    public void findUserEntryByUid(String uid) {
        dbRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(uid)) {
                    callback.onUserFound(dataSnapshot.child(uid).getValue(UserData.class));
                } else callback.onUserNotExist();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onErrorOccured();
            }
        });
    }


    public interface onSuccessListener {
        void onSuccess(UserData userData);
    }

    public void giveUserEditorsRights(String uId, onSuccessListener listener) {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listener.onSuccess(dataSnapshot.getValue(UserData.class));
                dbRoot.child(uId).removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        dbRoot.child(uId).addValueEventListener(valueEventListener);
        dbRoot.child(uId).child("hasEditorRights").setValue(true);
    }

    public void denyUserEditorsRights(String uId, onSuccessListener listener) {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listener.onSuccess(dataSnapshot.getValue(UserData.class));
                dbRoot.child(uId).removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        dbRoot.child(uId).addValueEventListener(valueEventListener);
        dbRoot.child(uId).child("hasEditorRights").setValue(false);
    }

    public void banFromCreatingEntries(String uId, onSuccessListener listener) {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listener.onSuccess(dataSnapshot.getValue(UserData.class));
                dbRoot.child(uId).removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        dbRoot.child(uId).addValueEventListener(valueEventListener);
        dbRoot.child(uId).child("bannedFromCreatingEntries").setValue(true);
    }

    public void giveCreatingEntriesRights(String uId, onSuccessListener listener) {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listener.onSuccess(dataSnapshot.getValue(UserData.class));
                dbRoot.child(uId).removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        dbRoot.child(uId).addValueEventListener(valueEventListener);
        dbRoot.child(uId).child("bannedFromCreatingEntries").setValue(false);
    }

    interface UsersGoingQueryCallback {
        void onUsersGot(ArrayList<UserData> users);
    }

    void queryUsersFromArrayId(ArrayList<String> users, UsersGoingQueryCallback callback) {
        if (users == null) return;
        dbRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<UserData> result = new ArrayList<>();
                for (String uId : users) {
                    UserData temp = dataSnapshot.child(uId).getValue(UserData.class);
                    if (temp != null) result.add(temp);
                }
                callback.onUsersGot(result);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    void queryUsersFavEventsList(String uId, GetUsersFavEventsListCallback callback) {
        dbRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(uId)) {
                    UserData userData = dataSnapshot.child(uId).getValue(UserData.class);
                    if (userData != null) {
                        callback.onFavEventsListGot(userData.getFavEvents());
                    } else callback.onError();
                } else callback.onError();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError();
            }
        });
    }

    /**
     * This method is used to create new Firebase UserData entry
     *
     * @param uId is Firebase Auth uID, should be passed so that Firebase User and Firebase Database UserData entry have equal IDs
     */
    public void createNewUserEntry(String uId) {
        UserData newUser = new UserData.Builder().uId(uId).build();
        dbRoot.child(uId).setValue(newUser).addOnSuccessListener(aVoid -> callback.onNewUserCreated(newUser));
    }

    public void deleteCurrentUserEntry(String uId) {
        dbRoot.child(uId).removeValue().addOnSuccessListener(aVoid -> callback.onUserEntryDeleted());
    }


    /**
     * This method is used to update existing database entry
     *
     * @param userData is data which will override existing entry
     */
    public void updateUserEntry(UserData userData) {
        if (!userData.getPhotoUri().isEmpty()) {
            dbRoot.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UserData existingData = dataSnapshot.child(userData.getuId()).getValue(UserData.class);
                    if (existingData != null) {
                        if (!existingData.getPhotoUri().equals(userData.getPhotoUri())) {
                            storage.child(userData.getuId()).putFile(Uri.parse(userData.getPhotoUri()))
                                    .addOnSuccessListener(taskSnapshot ->
                                            storage.child(userData.getuId()).getDownloadUrl().addOnSuccessListener(uri -> {
                                                userData.setPhotoUri(uri.toString());
                                                dbRoot.child(userData.getuId()).setValue(userData)
                                                        .addOnSuccessListener(aVoid -> {
                                                            callback.onUserUpdatedSuccessfully(userData);
                                                        })
                                                        .addOnFailureListener(e ->
                                                                callback.onErrorOccured());
                                            }));

                        } else
                            dbRoot.child(userData.getuId()).setValue(userData).addOnSuccessListener(aVoid -> {
                                callback.onUserUpdatedSuccessfully(userData);
                            }).addOnFailureListener(e -> callback.onErrorOccured());
                    }
                }


                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else
            dbRoot.child(userData.getuId()).setValue(userData).addOnSuccessListener(aVoid ->
                    callback.onUserUpdatedSuccessfully(userData))
                    .addOnFailureListener(e -> callback.onErrorOccured());
    }


    /**
     * This callback interface is used by methods managing actions with user's fav events list
     */
    public interface EventToFavsCallback {
        int ACTION_REMOVED = 5;
        int ACTION_ADDED = 6;

        /**
         * Called when User's favEvents list has been changed successfully
         *
         * @param actionCode tells what kind of action has been made: whether event has been added to list, or it was removed
         */
        void onSuccess(int actionCode);

        /**
         * Called when something gone wrong
         */
        void onError();
    }

    /**
     * Used to remove event from User's favEvents list
     *
     * @param currentUserId is id of UserData to be changed
     * @param eventId       - String data to be removed from favEventsList
     * @param callback      - used to check if result has been successfully written to database
     */
    void removeEventFromFavs(String currentUserId, String eventId, EventToFavsCallback callback) {
        dbRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserData currentUser = dataSnapshot.child(currentUserId).getValue(UserData.class);
                if (currentUser != null) {
                    currentUser.removeEventFromFavs(eventId);
                    dbRoot.child(currentUserId).setValue(currentUser).addOnSuccessListener(aVoid ->
                            callback.onSuccess(EventToFavsCallback.ACTION_REMOVED));
                } else callback.onError();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError();
            }
        });
    }

    /**
     * Used to add event to user's favEventsList
     *
     * @see #removeEventFromFavs(String, String, EventToFavsCallback) for more
     */
    void addEventToFavs(String currentUserId, String eventId, EventToFavsCallback callback) {
        dbRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserData currentUser = dataSnapshot.child(currentUserId).getValue(UserData.class);
                if (currentUser != null) {
                    currentUser.addEventToFavs(eventId);
                    dbRoot.child(currentUserId).setValue(currentUser).addOnSuccessListener(aVoid ->
                            callback.onSuccess(EventToFavsCallback.ACTION_ADDED));
                } else callback.onError();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError();
            }
        });
    }

    interface LifehackDataConversionCallback {
        default void onLifehackConverted(LifehackData lifehackData) {

        }

        default void onLifehackArrayConverted(ArrayList<LifehackData> lifehacks) {

        }

        void onAuthorNotExist(LifehackData lifehackData);

        void onError();
    }


    void convertArrayOfLifehacksForAdapter(Context context, ArrayList<LifehackData> lifehacks, LifehackDataConversionCallback callback) {
        dbRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (LifehackData lifehackData : lifehacks) {
                    UserData author = dataSnapshot.child(lifehackData.getAuthorUid()).getValue(UserData.class);
                    if (author != null) {
                        lifehackData.setPhotoUri(author.getPhotoUri());
                        lifehackData.setAuthorText(author.getLifehackAuthorText(context));
                    } else callback.onAuthorNotExist(lifehackData);
                }
                callback.onLifehackArrayConverted(lifehacks);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError();
            }
        });
    }

    void convertLifehackForAdapter(Context context, LifehackData lifehack, LifehackDataConversionCallback callback) {
        dbRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserData author = dataSnapshot.child(lifehack.getAuthorUid()).getValue(UserData.class);
                if (author != null) {
                    lifehack.setAuthorText(author.getLifehackAuthorText(context));
                    lifehack.setPhotoUri(author.getPhotoUri());
                    callback.onLifehackConverted(lifehack);
                } else callback.onAuthorNotExist(lifehack);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError();
            }
        });
    }


}
