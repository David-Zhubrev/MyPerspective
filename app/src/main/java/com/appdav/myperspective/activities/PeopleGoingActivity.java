package com.appdav.myperspective.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.appdav.myperspective.R;
import com.appdav.myperspective.common.Constants;
import com.appdav.myperspective.common.adapters.UserListAdapter;
import com.appdav.myperspective.common.daggerproviders.components.DaggerDataModelComponent;
import com.appdav.myperspective.common.daggerproviders.modules.DataModelModule;
import com.appdav.myperspective.common.data.EventData;
import com.appdav.myperspective.common.data.UserData;
import com.appdav.myperspective.datamodels.EventDataModel;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PeopleGoingActivity extends AppCompatActivity implements EventDataModel.EventDataModelCallback, UserListAdapter.UserListClickListener {

    @BindView(R.id.recycler_view_people_going)
    RecyclerView recyclerView;
    @BindView(R.id.tab_layout_people_going)
    TabLayout tabLayout;
    @BindView(R.id.tv_empty_list_people_going)
    TextView tv;

    private UserListAdapter adapter;

    private int selectedTab = 0;

    private ArrayList<UserData> peopleGoing, peopleNotGoing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people_going);
        ButterKnife.bind(this);
        EventData event = getIntent().getParcelableExtra(Constants.EVENT_EXTRA);
        if (event != null && getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(event.getName());
        }
        DataModelModule modelModule = new DataModelModule();
        modelModule.setEventDataModelCallback(this);
        EventDataModel model = DaggerDataModelComponent.builder().dataModelModule(modelModule).build().getEventDataModelInstance();

        if (event == null) {
            setErrorView();
            return;
        }
        model.queryPeopleGoing(event.getEventId(), new EventDataModel.QueryPeopleGoingCallback() {
            @Override
            public void queryPeopleGoing(ArrayList<UserData> users) {
                peopleGoing = users;
                updateRecyclerView();
            }

            @Override
            public void queryPeopleNotGoing(ArrayList<UserData> users) {
                peopleNotGoing = users;
                updateRecyclerView();
            }
        });
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTab = tab.getPosition();
                updateRecyclerView();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void updateRecyclerView() {
        switch (selectedTab) {
            case 0:
                setupRecyclerView(peopleGoing);
                break;
            case 1:
                setupRecyclerView(peopleNotGoing);
                break;
        }
    }

    private void setupRecyclerView(ArrayList<UserData> users) {
        if (users == null || users.isEmpty()) {
            tv.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
        } else {
            tv.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            if (adapter == null) adapter = new UserListAdapter(users, this);
            else adapter.updateDataSet(users);
            if (!adapter.isAttachedToRecyclerView) recyclerView.setAdapter(adapter);
        }
    }

    private void setErrorView() {
        //TODO
    }

    @Override
    public void onErrorOccured() {
        setErrorView();
    }


    @Override
    public void onUserItemClicked(String userId, boolean hasEditorsRights, boolean isBannedFromCreatingEntries) {
        proceedToProfileViewer(userId);
    }

    private void proceedToProfileViewer(String userId) {
        Intent intent = new Intent(this, ProfileViewerActivity.class);
        intent.putExtra(Constants.ACTION_WITH_ANY_USER, userId);
        intent.putExtra(Constants.ACTION_WITH_CURRENT_USER_INTENT, MainActivity.getCurrentUserId());
        startActivity(intent);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) onBackPressed();
        return false;
    }
}
