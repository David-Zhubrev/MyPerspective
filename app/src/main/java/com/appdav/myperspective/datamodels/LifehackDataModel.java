package com.appdav.myperspective.datamodels;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.appdav.myperspective.common.data.LifehackData;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class LifehackDataModel {

    private DatabaseReference dbRootRef;
    private ArrayList<LifehackData> lifehacks;
    private UserDataModel userDataModel;
    private LifehackDataModelCallback callback;
    private LifehackChangeListener changeListener;
    private ArrayList<LifehackData> newLifehacks;

    private String currentUserId;

    public LifehackDataModel(FirebaseDatabase database, UserDataModel userDataModel, LifehackDataModelCallback callback) {
        dbRootRef = database.getReference().getRoot().child("lifehacks");
        this.userDataModel = userDataModel;
        this.callback = callback;
    }

    public boolean arrayAlreadyExists() {
        return lifehacks != null;
    }

    public ArrayList<LifehackData> getLifehacksArray() {
        if (newLifehacks != null && !newLifehacks.isEmpty()) {
            lifehacks.addAll(0, newLifehacks);
            newLifehacks = null;
        }
        return lifehacks;
    }

    public void addLike(String lifehackId) {
        dbRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LifehackData lifehack = dataSnapshot.child(lifehackId).getValue(LifehackData.class);
                if (lifehack != null) {
                    lifehack.addLike(currentUserId);
                    dbRootRef.child(lifehackId).setValue(lifehack);
                } else callback.onErrorOccurred();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onErrorOccurred();
            }
        });
    }

    public void removeLike(String lifehackId) {
        dbRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LifehackData lifehackData = dataSnapshot.child(lifehackId).getValue(LifehackData.class);
                if (lifehackData != null) {
                    lifehackData.removeLike(currentUserId);
                    dbRootRef.child(lifehackId).setValue(lifehackData);
                } else callback.onErrorOccurred();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onErrorOccurred();
            }
        });
    }

    /**
     * This method should be used to get Array of Lifehacks at the first place
     * Result is returned by LifehackDataModel callback
     *
     * @param context       used to get string resources
     * @param currentUserId used to set LifehackDataModel's private field.
     *                      This is used to not overwhelm constructor with parameters, as soon as that method
     *                      will be called before any other actions could be done with the model
     */
    public void queryLifehacksArray(Context context, String currentUserId) {
        this.currentUserId = currentUserId;
        dbRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (lifehacks == null) lifehacks = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    LifehackData lifehackData = snapshot.getValue(LifehackData.class);
                    if (lifehackData != null) {
                        if (lifehackData.getLikes() != null && lifehackData.getLikes().contains(currentUserId))
                            lifehackData.setLikedByCurrentUser(true);
                        else lifehackData.setLikedByCurrentUser(false);
                        lifehacks.add(0, lifehackData);
                    }
                }
                userDataModel.convertArrayOfLifehacksForAdapter(context, lifehacks, new UserDataModel.LifehackDataConversionCallback() {
                    @Override
                    public void onLifehackArrayConverted(ArrayList<LifehackData> lifehacks) {
                        callback.onLifehackArrayFirstGet(lifehacks);
                    }

                    @Override
                    public void onAuthorNotExist(LifehackData lifehackData) {
                        //TODO: test if deleting user id crashing the app
                    }

                    @Override
                    public void onError() {
                        callback.onErrorOccurred();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onErrorOccurred();
            }
        });
    }

    /**
     * This method creates instance of
     *
     * @param context used to get String resources
     * @see LifehackChangeListener
     * and provides feedback on any changes made by all the users to Lifehack database
     */
    public void subscribeForLifehackChanges(Context context) {
        if (changeListener == null) changeListener = new LifehackChangeListener(context);
        dbRootRef.addChildEventListener(changeListener);
    }

    /**
     * This method should be called whenever presenter class should stop get any updates on lifehacks
     * Otherwise, callbacks from this method will still be invoked
     */
    public void unsubscribeFromLifehackChanges() {
        if (changeListener != null) {
            dbRootRef.removeEventListener(changeListener);
            changeListener = null;
        }
    }

    /**
     * This interface is passed to LifehackDataModel constructor
     * as Firebase works asynchroniously and can't return results directly
     */
    public interface LifehackDataModelCallback {
        /**
         * This method is called after
         *
         * @param lifehacks - resulting array of LifehackData
         * @see #queryLifehacksArray(Context, String) is called
         */
        void onLifehackArrayFirstGet(ArrayList<LifehackData> lifehacks);

        /**
         * This method is called by ChangeListener when LifehackData not contained by private lifehacks field
         * has been uploaded to the database
         */
        void onNewLifehackGot();

        /**
         * This method is called when LifehackData's likes array is changed
         *
         * @param arrayPosition - position of changed LifehackData in lifehacks
         */
        void onLifehackChanged(int arrayPosition);

        void onLifehackRemoved(int arrayPosition);

        void onNewLifehackSuccessfullyUploaded();

        void onNewLifehackUploadFailed();

        void onErrorOccurred();
    }

    public void nullifyLifehackArray() {
        this.lifehacks = null;
    }

    /**
     * Instance of this class listens to changes made by any user to the lifehack database root
     */

    public void deleteLifehackEntry(String lifehackId) {
        dbRootRef.child(lifehackId).removeValue();
    }

    public void createNewLifehackEntry(LifehackData lifehackData) {
        String lifehackId = dbRootRef.push().getKey();
        if (lifehackId == null || lifehackId.isEmpty()) {
            callback.onErrorOccurred();
            return;
        }
        lifehackData.setLifehackId(lifehackId);
        lifehackData.setAuthorUid(currentUserId);
        dbRootRef.child(lifehackId).setValue(lifehackData).addOnSuccessListener(aVoid ->
                callback.onNewLifehackSuccessfullyUploaded())
                .addOnFailureListener(e ->
                        callback.onNewLifehackUploadFailed());
    }


    private class LifehackChangeListener implements ChildEventListener {

        private Context context;

        LifehackChangeListener(Context context) {
            this.context = context;
        }

        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            if (lifehacks == null) return;
            LifehackData lifehackData = dataSnapshot.getValue(LifehackData.class);
            if (lifehackData == null || lifehacks.contains(lifehackData)) return;
            if (lifehackData.getLikes() != null && lifehackData.getLikes().contains(currentUserId))
                lifehackData.setLikedByCurrentUser(true);
            else lifehackData.setLikedByCurrentUser(false);
            userDataModel.convertLifehackForAdapter(context, lifehackData, new UserDataModel.LifehackDataConversionCallback() {

                @Override
                public void onLifehackConverted(LifehackData lifehackData) {
                    if (newLifehacks == null) newLifehacks = new ArrayList<>();
                    if (!newLifehacks.contains(lifehackData)) {
                        newLifehacks.add(0, lifehackData);
                        callback.onNewLifehackGot();
                    }
                }

                @Override
                public void onAuthorNotExist(LifehackData lifehackData) {
                    if (newLifehacks != null) newLifehacks.remove(lifehackData);
                    if (s != null) dbRootRef.child(s).removeValue();
                }

                @Override
                public void onError() {

                }
            });
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            LifehackData lifehackData = dataSnapshot.getValue(LifehackData.class);
            if (lifehackData == null) return;
            if (lifehackData.getLikes() != null && lifehackData.getLikes().contains(currentUserId))
                lifehackData.setLikedByCurrentUser(true);
            else lifehackData.setLikedByCurrentUser(false);
            userDataModel.convertLifehackForAdapter(context, lifehackData, new UserDataModel.LifehackDataConversionCallback() {

                @Override
                public void onLifehackConverted(LifehackData lifehackData) {
                    if (lifehacks.contains(lifehackData)) {
                        int index = lifehacks.indexOf(lifehackData);
                        lifehacks.set(index, lifehackData);
                        callback.onLifehackChanged(index);
                    } else if (newLifehacks != null && newLifehacks.contains(lifehackData)) {
                        int index = newLifehacks.indexOf(lifehackData);
                        newLifehacks.set(index, lifehackData);
                    }
                }

                @Override
                public void onAuthorNotExist(LifehackData lifehackData) {
                    if (s != null) dbRootRef.child(s).removeValue();
                }

                @Override
                public void onError() {

                }
            });
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            LifehackData lifehackData = dataSnapshot.getValue(LifehackData.class);
            if (lifehacks != null && lifehacks.contains(lifehackData)) {
                int index = lifehacks.indexOf(lifehackData);
                lifehacks.remove(lifehackData);
                callback.onLifehackRemoved(index);
            }
            if (newLifehacks != null) newLifehacks.remove(lifehackData);
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            callback.onErrorOccurred();
        }
    }


}
