package com.appdav.myperspective.contracts;

import com.appdav.myperspective.common.adapters.UserListAdapter;
import com.appdav.myperspective.common.mvp.PresenterInterface;
import com.appdav.myperspective.common.mvp.ViewInterface;
import com.appdav.myperspective.common.views.MyDialogs.EditUsersCreatingEntriesPermissionsDialog.CreatingEntriesRightsDialogType;
import com.appdav.myperspective.common.views.MyDialogs.EditUsersCreatingEntriesPermissionsDialog.UsersCreatingEntriesDialogCallback;
import com.appdav.myperspective.common.views.MyDialogs.EditUsersEditorsPermissionsDialog.EditUsersEditorsPermissionsDialogCallback;
import com.appdav.myperspective.common.views.MyDialogs.EditUsersEditorsPermissionsDialog.EditorsRightsDialogType;
import com.appdav.myperspective.common.views.MyDialogs.EditUsersPermissionDialog.EditUsersPermissionsDialogCallback;

public interface EditorsRoomContract {

    interface View extends ViewInterface {
        void attachAdapteToRecyclerView(UserListAdapter adapter);

        void setupGiveEditorsRightsDialog(EditorsRightsDialogType type, EditUsersEditorsPermissionsDialogCallback callback);

        void setupGiveCreatingPermissionDialog(CreatingEntriesRightsDialogType type, UsersCreatingEntriesDialogCallback callback);

        void setupEditPermissionsDialog(EditorsRightsDialogType editingType,
                                        CreatingEntriesRightsDialogType creatingType,
                                        EditUsersPermissionsDialogCallback callback);
    }

    interface Presenter extends PresenterInterface<View> {

    }

}
