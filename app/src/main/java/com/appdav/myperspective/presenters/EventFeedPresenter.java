package com.appdav.myperspective.presenters;

import com.appdav.myperspective.activities.MainActivity;
import com.appdav.myperspective.common.adapters.EventFeedAdapter;
import com.appdav.myperspective.common.daggerproviders.components.DaggerDataModelComponent;
import com.appdav.myperspective.common.daggerproviders.modules.DataModelModule;
import com.appdav.myperspective.common.data.EventData;
import com.appdav.myperspective.common.mvp.PresenterBase;
import com.appdav.myperspective.common.views.MyDialogs;
import com.appdav.myperspective.contracts.EventFeedContract;
import com.appdav.myperspective.datamodels.EventDataModel;

import java.util.ArrayList;

public class EventFeedPresenter extends PresenterBase<EventFeedContract.View> implements EventFeedContract.Presenter,
        EventDataModel.EventDataModelCallback,
        EventFeedAdapter.EventFeedCallback {

    private EventDataModel eventDataModel;
    private EventFeedAdapter adapter;

    //inject dependencies inside the constructor
    public EventFeedPresenter() {
        DataModelModule modelModule = new DataModelModule();
        modelModule.setEventDataModelCallback(this);
        eventDataModel = DaggerDataModelComponent.builder().dataModelModule(modelModule).build().getEventDataModelInstance();
    }

    @Override
    public void viewIsReady() {
        getView().setRefreshButtonActive(false);
        if (!eventDataModel.eventsArrayAlreadyExists()) {
            eventDataModel.setCurrentUser(getView().getCurrentUserId());
            eventDataModel.queryEventsArray();
        } else {
            setupAdapterWithDataset(eventDataModel.getEventsArray());
        }

    }

    //method used to setup adapter with data set, push it to the recycler view and subscribe for EventDataModel change events
    private void setupAdapterWithDataset(ArrayList<EventData> events) {
        if (events == null) {
            onErrorOccured();
            return;
        }
        if (adapter == null) {
            if (MainActivity.hasEditorsRights())
                adapter = new EventFeedAdapter(events, this, true);
            else adapter = new EventFeedAdapter(events, this);
        } else adapter.setDataSet(events);
        if (isViewAttached()) {
            getView().attachAdapterToRecyclerView(adapter);
            getView().setScreenReady();
            eventDataModel.subscribeForEventChanges();
        }

    }

    @Override
    public void onEventsArrayFirstGet(ArrayList<EventData> events) {
        setupAdapterWithDataset(events);
    }

    @Override
    public void detachView() {
        super.detachView();
        eventDataModel.unsubscribeFromEventChanges();
    }

    /**
     * @see PresenterBase#destroy()
     */
    @Override
    public void destroy() {
        super.destroy();
        eventDataModel.nullifyDataSet();
    }


    /**
     * @see EventDataModel.EventDataModelCallback#onErrorOccured()
     */
    @Override
    public void onErrorOccured() {
        //TODO
    }

    /**
     * @see EventDataModel.EventDataModelCallback#onNewEventGot()
     */
    @Override
    public void onNewEventGot() {
        getView().setRefreshButtonActive(true);
    }

    @Override
    public void onEventRemoved(int arrayIndex) {
        adapter.removeEventFromAdapter(arrayIndex);
    }


    /**
     * @param index shows which adapter item is changed
     * @see com.appdav.myperspective.datamodels.EventDataModel.EventDataModelCallback#onPeopleGoingArrayChanged(int)
     */
    @Override
    public void onPeopleGoingArrayChanged(int index) {
        adapter.updatePeopleGoingList(index);
    }

    @Override
    public void onButtonGoClicked(String eventId) {
        eventDataModel.addCurrentUserToGoingList(eventId);
    }

    @Override
    public void onButtonWontGoClicked(String eventId) {
        eventDataModel.addCurrentUserToNotGoingList(eventId);
    }

    @Override
    public void onButtonChangeClicked(String eventId) {
        eventDataModel.deleteCurrentUserFromGoingLists(eventId);
    }

    private EventDataModel.OnEventToFavsChangeListener onEventToFavsChangeListener = new EventDataModel.OnEventToFavsChangeListener() {
        @Override
        public void onEventAddedToFavs(int arrayIndex) {
            getView().showAddedToFavsMessage();
            adapter.setEventReadyAfterAddingToFavs(arrayIndex, true);
        }

        @Override
        public void onEventRemovedFromFavs(int arrayIndex) {
            getView().showRemovedFromFavsMessage();
            adapter.setEventReadyAfterAddingToFavs(arrayIndex, true);
        }

        @Override
        public void onError(int arrayIndex) {
            adapter.setEventReadyAfterAddingToFavs(arrayIndex, false);
        }
    };

    @Override
    public void onButtonFavouriteClicked(String eventId, boolean toBeAdd) {
        if (toBeAdd) eventDataModel.addEventToFavs(eventId, onEventToFavsChangeListener);
        else eventDataModel.removeEventFromFavs(eventId, onEventToFavsChangeListener);
    }

    @Override
    public void onButtonRemoveEventClicked(String eventId) {
        getView().setupConfirmationDialog(() ->
                eventDataModel.removeEventEntry(eventId));
    }

    @Override
    public void onButtonShowPeopleList(EventData eventData) {
        getView().setupPeopleGoingActivity(eventData);
    }

    @Override
    public void onFabClicked() {
        getView().setupNewEventActivity();
    }

    @Override
    public void onRefreshButtonClicked() {
        getView().setRefreshButtonActive(false);
        getView().setScreenRefreshing();
        this.refresh();
    }


    private void refresh() {
        eventDataModel.unsubscribeFromEventChanges();
        this.viewIsReady();
    }
}
