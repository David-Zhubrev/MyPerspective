package com.appdav.myperspective.presenters;

import com.appdav.myperspective.common.adapters.UserListAdapter;
import com.appdav.myperspective.common.daggerproviders.components.DaggerDataModelComponent;
import com.appdav.myperspective.common.daggerproviders.modules.DataModelModule;
import com.appdav.myperspective.common.data.UserData;
import com.appdav.myperspective.common.mvp.PresenterBase;
import com.appdav.myperspective.common.views.MyDialogs;
import com.appdav.myperspective.common.views.MyDialogs.EditUsersCreatingEntriesPermissionsDialog.CreatingEntriesRightsDialogType;
import com.appdav.myperspective.common.views.MyDialogs.EditUsersEditorsPermissionsDialog;
import com.appdav.myperspective.common.views.MyDialogs.EditUsersEditorsPermissionsDialog.EditorsRightsDialogType;
import com.appdav.myperspective.contracts.EditorsRoomContract;
import com.appdav.myperspective.datamodels.UserDataModel;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class EditorsRoomPresenter extends PresenterBase<EditorsRoomContract.View> implements EditorsRoomContract.Presenter, TabLayout.OnTabSelectedListener, UserDataModel.UserEventListenter, UserListAdapter.UserListClickListener {

    private UserDataModel model;
    private int selectedTab = 0;
    private UserListAdapter adapter;
    private ArrayList<UserData> users;
    private ArrayList<UserData> editorsList;

    public EditorsRoomPresenter() {
        DataModelModule modelModule = new DataModelModule();
        modelModule.setUserEventListenter(this);
        model = DaggerDataModelComponent.builder().dataModelModule(modelModule).build().getUserDataModelInstance();
    }

    @Override
    public void viewIsReady() {
        model.queryAllUsersList();
    }

    private void onSelectedTabChanged(int selectedTab) {
        this.selectedTab = selectedTab;
        setupAdapter();
    }

    private void setupAdapter() {
        if (adapter == null) adapter = new UserListAdapter(users, this);
        if (selectedTab == 0) {
            if (editorsList == null) editorsList = new ArrayList<>();
            for (UserData user : users) {
                if (user.hasEditorRights && !editorsList.contains(user)) editorsList.add(user);
            }
            adapter.updateDataSet(editorsList);
        }
        if (selectedTab == 1) {
            adapter.updateDataSet(users);
        }
        getView().attachAdapteToRecyclerView(adapter);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        onSelectedTabChanged(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        //TODO
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        model.queryAllUsersList();
    }

    @Override
    public void onUsersArrayGot(ArrayList<UserData> users) {
        this.users = users;
        setupAdapter();
    }

    @Override
    public void onErrorOccured() {

    }

    @Override
    public void onUserItemClicked(String userId, boolean hasEditorsRights, boolean isBannedFromCreatingEntries) {
        EditorsRightsDialogType editingType;
        CreatingEntriesRightsDialogType creatingType;
        if (hasEditorsRights) editingType = EditorsRightsDialogType.DENY_RIGHTS;
        else editingType = EditorsRightsDialogType.GIVE_RIGHTS;
        if (isBannedFromCreatingEntries) creatingType = CreatingEntriesRightsDialogType.GIVE_RIGHTS;
        else creatingType = CreatingEntriesRightsDialogType.DENY_RIGHTS;
        getView().setupEditPermissionsDialog(editingType, creatingType, new MyDialogs.EditUsersPermissionDialog.EditUsersPermissionsDialogCallback() {
            @Override
            public void onActionEditorsPermission() {
                getView().setupGiveEditorsRightsDialog(editingType, new EditUsersEditorsPermissionsDialog.EditUsersEditorsPermissionsDialogCallback() {
                    @Override
                    public void onActionDenyEditorsRights() {
                        model.denyUserEditorsRights(userId, userData -> {
                            int editorsIndex = editorsList.indexOf(userData);
                            editorsList.remove(userData);
                            if (selectedTab == 0) adapter.notifyItemRemoved(editorsIndex);
                            int usersIndex = users.indexOf(userData);
                            users.set(usersIndex, userData);
                            if (selectedTab == 1) adapter.updateItem(usersIndex);
                        });
                    }

                    @Override
                    public void onActionGiveEditorsRights() {
                        model.giveUserEditorsRights(userId, userData -> {
                            editorsList.add(userData);
                            if (selectedTab == 0)
                                adapter.notifyItemInserted(adapter.getItemCount());
                            int usersIndex = users.indexOf(userData);
                            users.set(usersIndex, userData);
                            if (selectedTab == 1) adapter.updateItem(usersIndex);
                        });
                    }
                });
            }


            @Override
            public void onActionCreatingPermission() {
                getView().setupGiveCreatingPermissionDialog(creatingType, new MyDialogs.EditUsersCreatingEntriesPermissionsDialog.UsersCreatingEntriesDialogCallback() {
                    @Override
                    public void onActionDenyCreatingRights() {
                        model.banFromCreatingEntries(userId, userData -> {
                            int editorsIndex = -1, usersIndex = -1;
                            editorsIndex = editorsList.indexOf(userData);
                            usersIndex = users.indexOf(userData);
                            if (editorsIndex != -1) editorsList.set(editorsIndex, userData);
                            if (usersIndex != -1) users.set(usersIndex, userData);
                            if (selectedTab == 0 && editorsIndex != -1)
                                adapter.updateItem(editorsIndex);
                            if (selectedTab == 1 && usersIndex != -1)
                                adapter.updateItem(usersIndex);
                        });
                    }

                    @Override
                    public void onActionGiveCreatingRights() {
                        model.giveCreatingEntriesRights(userId, userData -> {
                            int editorsIndex = -1, usersIndex = -1;
                            editorsIndex = editorsList.indexOf(userData);
                            usersIndex = users.indexOf(userData);
                            if (editorsIndex != -1) editorsList.set(editorsIndex, userData);
                            if (usersIndex != -1) users.set(usersIndex, userData);
                            if (selectedTab == 0 && editorsIndex != -1)
                                adapter.updateItem(editorsIndex);
                            if (selectedTab == 1 && usersIndex != -1)
                                adapter.updateItem(usersIndex);
                        });
                    }
                });
            }
        });
    }
}


