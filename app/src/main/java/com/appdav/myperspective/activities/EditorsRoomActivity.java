package com.appdav.myperspective.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.appdav.myperspective.R;
import com.appdav.myperspective.common.adapters.UserListAdapter;
import com.appdav.myperspective.common.daggerproviders.components.DaggerPresenterComponent;
import com.appdav.myperspective.common.views.MyDialogs;
import com.appdav.myperspective.contracts.EditorsRoomContract;
import com.appdav.myperspective.presenters.EditorsRoomPresenter;
import com.google.android.material.tabs.TabLayout;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditorsRoomActivity extends AppCompatActivity implements EditorsRoomContract.View {

    private final static String GIVE_EDITORS_RIGHTS_DIALOG_TAG = "giveeditorsrightsdialog";
    private final static String CREATING_PERMISSIONS_DIALOG_TAG = "creatingdialog";
    private final static String PERMISSIONS_DIALOG_TAG = "editorspermissions";

    @BindView(R.id.tab_layout_editors_room)
    TabLayout tabLayout;
    @BindView(R.id.recycler_view_editors_room)
    RecyclerView recyclerView;
    @Inject
    EditorsRoomPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editors_room);
        ButterKnife.bind(this);
        DaggerPresenterComponent.builder().build().injectEditorsRoomActivity(this);
        presenter.attachView(this);
        tabLayout.addOnTabSelectedListener(presenter);
        presenter.viewIsReady();
    }

    @Override
    public void attachAdapteToRecyclerView(UserListAdapter adapter) {
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void setupGiveCreatingPermissionDialog(MyDialogs.EditUsersCreatingEntriesPermissionsDialog.CreatingEntriesRightsDialogType type, MyDialogs.EditUsersCreatingEntriesPermissionsDialog.UsersCreatingEntriesDialogCallback callback) {
        new MyDialogs.EditUsersCreatingEntriesPermissionsDialog(this, type, callback)
                .show(getSupportFragmentManager(), CREATING_PERMISSIONS_DIALOG_TAG);
    }

    @Override
    public void setupEditPermissionsDialog(MyDialogs.EditUsersEditorsPermissionsDialog.EditorsRightsDialogType editingType, MyDialogs.EditUsersCreatingEntriesPermissionsDialog.CreatingEntriesRightsDialogType creatingType, MyDialogs.EditUsersPermissionDialog.EditUsersPermissionsDialogCallback callback) {
        new MyDialogs.EditUsersPermissionDialog(this, creatingType, editingType, callback)
                .show(getSupportFragmentManager(), PERMISSIONS_DIALOG_TAG);
    }

    @Override
    public void setupGiveEditorsRightsDialog(MyDialogs.EditUsersEditorsPermissionsDialog.EditorsRightsDialogType type, MyDialogs.EditUsersEditorsPermissionsDialog.EditUsersEditorsPermissionsDialogCallback callback) {
        new MyDialogs.EditUsersEditorsPermissionsDialog(this, type, callback).show(getSupportFragmentManager(), GIVE_EDITORS_RIGHTS_DIALOG_TAG);
    }
}
