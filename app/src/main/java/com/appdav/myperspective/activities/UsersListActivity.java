package com.appdav.myperspective.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.appdav.myperspective.R;
import com.appdav.myperspective.common.Constants;
import com.appdav.myperspective.common.adapters.UserListAdapter;
import com.appdav.myperspective.common.daggerproviders.components.DaggerDataModelComponent;
import com.appdav.myperspective.common.daggerproviders.modules.DataModelModule;
import com.appdav.myperspective.common.data.UserData;
import com.appdav.myperspective.datamodels.UserDataModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UsersListActivity extends AppCompatActivity implements UserDataModel.UserEventListenter, UserListAdapter.UserListClickListener {

    @BindView(R.id.recycler_view_users_list)
    RecyclerView recyclerView;

    private UserListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        DataModelModule modelModule = new DataModelModule();
        modelModule.setUserEventListenter(this);
        UserDataModel model = DaggerDataModelComponent.builder().dataModelModule(modelModule).build().getUserDataModelInstance();
        model.queryAllUsersList();
    }

    @Override
    public void onUsersArrayGot(ArrayList<UserData> users) {
        if (adapter == null) adapter = new UserListAdapter(users, this);
        else adapter.updateDataSet(users);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onErrorOccured() {

    }

    private void proceedToProfileViewer(String userId) {
        Intent intent = new Intent(this, ProfileViewerActivity.class);
        intent.putExtra(Constants.ACTION_WITH_CURRENT_USER_INTENT, MainActivity.getCurrentUserId());
        intent.putExtra(Constants.ACTION_WITH_ANY_USER, userId);
        startActivity(intent);
    }

    @Override
    public void onUserItemClicked(String userId, boolean hasEditorsRights, boolean isBannedFromCreatingEntries) {
        proceedToProfileViewer(userId);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) onBackPressed();
        return false;
    }
}
