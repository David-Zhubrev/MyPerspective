package com.appdav.myperspective.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.appdav.myperspective.activities.MainActivity;
import com.appdav.myperspective.activities.NewEventActivity;
import com.appdav.myperspective.activities.PeopleGoingActivity;
import com.appdav.myperspective.common.Constants;
import com.appdav.myperspective.common.adapters.EventFeedAdapter;
import com.appdav.myperspective.common.daggerproviders.components.DaggerPresenterComponent;
import com.appdav.myperspective.common.data.EventData;
import com.appdav.myperspective.common.views.MyDialogs;
import com.appdav.myperspective.common.views.RefreshButton;
import com.appdav.myperspective.contracts.EventFeedContract;
import com.appdav.myperspective.presenters.EventFeedPresenter;
import com.appdav.myperspective.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class EventFeedFragment extends Fragment implements EventFeedContract.View {

    private final static String LOG_TAG = "myLogs";
    private final static int RC_NEW_EVENT = 142;

    @Inject
    EventFeedPresenter presenter;
    @BindView(R.id.recycler_view_event_feed)
    RecyclerView recyclerView;
    @BindView(R.id.fab_event_feed)
    FloatingActionButton fab;
    @BindView(R.id.button_refresh_event_feed)
    RefreshButton refreshButton;
    @BindView(R.id.progress_bar_event_feed)
    ProgressBar progressBar;
    @BindView(R.id.event_feed_root)
    CoordinatorLayout coordinatorLayout;

    private Unbinder unbinder;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_event_feed, container, false);
        unbinder = ButterKnife.bind(this, root);
        DaggerPresenterComponent.builder().build().injectEventFeedFragment(this);
        return root;
    }

    @OnClick(R.id.fab_event_feed)
    void onFabClick() {
        presenter.onFabClicked();
    }

    @OnClick(R.id.button_refresh_event_feed)
    void onClick() {
        presenter.onRefreshButtonClicked();
    }

    @Override
    public void onStart() {
        super.onStart();
        setScreenRefreshing();
        presenter.attachView(this);
        presenter.viewIsReady();
        if (MainActivity.isUserBanned()) {
            fab.setEnabled(false);
            fab.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.detachView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.destroy();
        presenter = null;
        unbinder.unbind();
    }

    @Override
    public String getCurrentUserId() {
        return MainActivity.getCurrentUserId();
    }

    @Override
    public void attachAdapterToRecyclerView(EventFeedAdapter adapter) {
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void setRefreshButtonActive(boolean isActive) {
        refreshButton.setDeactivated(!isActive);
    }

    @Override
    public void setScreenRefreshing() {
        if (!refreshButton.isHidden()) refreshButton.hide(false);
        refreshButton.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void setScreenReady() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }


    @Override
    public void setupNewEventActivity() {
        Intent intent = new Intent(getContext(), NewEventActivity.class);
        startActivityForResult(intent, RC_NEW_EVENT);
    }

    @Override
    public void setupPeopleGoingActivity(EventData eventData) {
        Intent intent = new Intent(getContext(), PeopleGoingActivity.class);
        intent.putExtra(Constants.EVENT_EXTRA, eventData);
        startActivity(intent);
    }

    private final static String CONFIRM_DIALOG_TAG = "confirmdialogtag";

    @Override
    public void setupConfirmationDialog(MyDialogs.ConfirmRemovalDialog.ConfirmRemovalDialogCallback callback) {
        new MyDialogs.ConfirmRemovalDialog(getContext(), callback).show(getParentFragmentManager(), CONFIRM_DIALOG_TAG);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_NEW_EVENT && resultCode == Activity.RESULT_OK) {
            MainActivity.snackbarEventUploaded.show();
        }
    }

    @Override
    public void showAddedToFavsMessage() {
        Log.d(LOG_TAG, "showAddedMessage");
        Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.event_added_to_favs, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    @Override
    public void showRemovedFromFavsMessage() {
        Log.d(LOG_TAG, "showRemovedMessage");
        Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.event_removed_from_favs, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }


}