package com.appdav.myperspective.common.views;

import androidx.appcompat.app.AlertDialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.appdav.myperspective.R;
import com.appdav.myperspective.common.Constants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class MyDialogs {

    public static class ConfirmRemovalDialog extends DialogFragment {
        private Context context;
        private ConfirmRemovalDialogCallback callback;

        public interface ConfirmRemovalDialogCallback {
            void onRemovalConfirmationPositiveButtonClicked();
        }

        public ConfirmRemovalDialog(Context context, ConfirmRemovalDialogCallback callback) {
            this.context = context;
            this.callback = callback;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            return new AlertDialog.Builder(context)
                    .setTitle(R.string.confirm_dialog_title)
                    .setMessage(R.string.confirm_dialog_message)
                    .setPositiveButton(R.string.confirm_dialog_button_positive, (dialog, which) -> {
                        callback.onRemovalConfirmationPositiveButtonClicked();
                        dismiss();
                    })
                    .setNegativeButton(R.string.confirm_dialog_button_negative, ((dialog, which) -> dismiss()))
                    .create();
        }
    }

    public static class SetNotificationsDialog extends DialogFragment {

        private ArrayList<Long> results;
        private SetNotificationsDialogCallback callback;
        private long eventTime;
        private long currentTime;

        @BindView(R.id.checkbox_one_hour_dialog_alarm)
        CheckBox chbOneHour;
        @BindView(R.id.checkbox_six_hours_dialog_alarm)
        CheckBox chbSixHours;
        @BindView(R.id.checkbox_twenty_four_hours_dialog_alarm)
        CheckBox chbTwentyFourHours;


        public interface SetNotificationsDialogCallback {
            void onNotificationsSet(ArrayList<Long> alarms);
        }


        public SetNotificationsDialog(long eventTime, SetNotificationsDialogCallback callback) {
            this.callback = callback;
            this.eventTime = eventTime;
            currentTime = Calendar.getInstance(Locale.getDefault()).getTimeInMillis();
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.dialog_alarm, container, false);
            ButterKnife.bind(this, v);
            if (eventTime - currentTime < Constants.TWENTY_FOUR_HOURS_IN_MILLIS)
                chbTwentyFourHours.setEnabled(false);
            if (eventTime - currentTime < Constants.SIX_HOURS_IN_MILLIS)
                chbSixHours.setEnabled(false);
            return v;
        }

        @OnClick(R.id.button_accept_dialog_alarm)
        void onClick() {
            if (chbOneHour.isChecked()) putToResultArray(Constants.ONE_HOURS_IN_MILLIS);
            if (chbSixHours.isChecked()) putToResultArray(Constants.SIX_HOURS_IN_MILLIS);
            if (chbTwentyFourHours.isChecked())
                putToResultArray(Constants.TWENTY_FOUR_HOURS_IN_MILLIS);
            callback.onNotificationsSet(results);
            dismiss();
        }

        private void putToResultArray(long alarmType) {
            if (this.results == null) results = new ArrayList<>();
            results.add(alarmType);
        }

        @Override
        public void onStart() {
            super.onStart();
            Dialog dialog = getDialog();
            if (dialog != null && dialog.getWindow() != null) {
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }

    }

    public static class EditUsersPermissionDialog extends DialogFragment {
        private Context context;
        private EditUsersPermissionsDialogCallback callback;
        private EditUsersCreatingEntriesPermissionsDialog.CreatingEntriesRightsDialogType creatingType;
        private EditUsersEditorsPermissionsDialog.EditorsRightsDialogType editingType;


        public EditUsersPermissionDialog(Context context,
                                         EditUsersCreatingEntriesPermissionsDialog.CreatingEntriesRightsDialogType creatingType,
                                         EditUsersEditorsPermissionsDialog.EditorsRightsDialogType editingType,
                                         EditUsersPermissionsDialogCallback callback) {
            this.context = context;
            this.callback = callback;
            this.editingType = editingType;
            this.creatingType = creatingType;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            CharSequence creatingTypeText = "", editingTypeText = "";
            switch (creatingType) {
                case DENY_RIGHTS:
                    creatingTypeText = context.getString(R.string.editors_dialog_item_deny_creating_permission);
                    break;
                case GIVE_RIGHTS:
                    creatingTypeText = context.getString(R.string.editors_dialog_item_give_creating_permission);
                    break;
            }
            switch (editingType) {
                case DENY_RIGHTS:
                    editingTypeText = context.getString(R.string.editors_dialog_item_deny_editors_permission);
                    break;
                case GIVE_RIGHTS:
                    editingTypeText = context.getString(R.string.editors_dialog_item_give_editors_permission);
                    break;
            }
            CharSequence[] items = {editingTypeText, creatingTypeText};
            return builder.setItems(items, (dialog, which) -> {
                switch (which) {
                    case 0:
                        callback.onActionEditorsPermission();
                        dismiss();
                        break;
                    case 1:
                        callback.onActionCreatingPermission();
                        dismiss();
                        break;
                }
            }).create();
        }

        public interface EditUsersPermissionsDialogCallback {
            void onActionEditorsPermission();

            void onActionCreatingPermission();
        }
    }

    public static class EditUsersCreatingEntriesPermissionsDialog extends DialogFragment {
        public enum CreatingEntriesRightsDialogType {DENY_RIGHTS, GIVE_RIGHTS}

        private Context context;
        private UsersCreatingEntriesDialogCallback callback;
        private CreatingEntriesRightsDialogType type;

        public interface UsersCreatingEntriesDialogCallback {
            void onActionDenyCreatingRights();

            void onActionGiveCreatingRights();
        }

        public EditUsersCreatingEntriesPermissionsDialog(Context context, CreatingEntriesRightsDialogType type, UsersCreatingEntriesDialogCallback callback) {
            this.context = context;
            this.type = type;
            this.callback = callback;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            switch (type) {
                case DENY_RIGHTS:
                    return new AlertDialog.Builder(context)
                            .setTitle(R.string.editors_dialog_deny_creating_entries_title)
                            .setMessage(R.string.editors_dialog_deny_creating_entries_message)
                            .setPositiveButton(R.string.editors_dialog_deny_creating_entries_button_positive, (dialog, which) -> {
                                callback.onActionDenyCreatingRights();
                                dismiss();
                            })
                            .setNegativeButton(R.string.editors_dialog_deny_creating_entries_button_negative, (dialog, which) -> dismiss())
                            .create();
                case GIVE_RIGHTS:
                    return new AlertDialog.Builder(context)
                            .setTitle(R.string.editors_dialog_deny_creating_entries_title)
                            .setMessage(R.string.editors_dialog_give_creating_entries_rights_message)
                            .setPositiveButton(R.string.editors_dialog_give_creating_entries_rights_button_positive, (dialog, which) -> {
                                callback.onActionGiveCreatingRights();
                                dismiss();
                            })
                            .setNegativeButton(R.string.editors_dialog_give_creating_entries_rights_button_negative, (dialog, which) ->
                                    dismiss())
                            .create();
            }
            return null;
        }
    }

    public static class EditUsersEditorsPermissionsDialog extends DialogFragment {

        public enum EditorsRightsDialogType {DENY_RIGHTS, GIVE_RIGHTS}

        private Context context;
        private EditUsersEditorsPermissionsDialogCallback callback;

        private EditorsRightsDialogType type;

        public EditUsersEditorsPermissionsDialog(Context context, EditorsRightsDialogType type, EditUsersEditorsPermissionsDialogCallback callback) {
            this.context = context;
            this.type = type;
            this.callback = callback;
        }

        public interface EditUsersEditorsPermissionsDialogCallback {
            void onActionDenyEditorsRights();

            void onActionGiveEditorsRights();
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            switch (type) {
                case GIVE_RIGHTS:
                    return new AlertDialog.Builder(context)
                            .setTitle(R.string.editors_dialog_give_rights_title)
                            .setMessage(R.string.editors_dialog_give_rights_message)
                            .setPositiveButton(R.string.editors_dialog_give_rights_button_positive, (dialog, which) -> {
                                callback.onActionGiveEditorsRights();
                                dismiss();
                            })
                            .setNegativeButton(R.string.editors_dialog_give_rights_button_negative, (dialog, which) -> dismiss())
                            .create();
                case DENY_RIGHTS:
                    return new AlertDialog.Builder(context)
                            .setTitle(R.string.editors_dialog_deny_rights_title)
                            .setMessage(R.string.editors_dialog_deny_rights_message)
                            .setPositiveButton(R.string.editors_dialog_deny_rights_button_positive, (dialog, which) -> {
                                callback.onActionDenyEditorsRights();
                                dismiss();
                            })
                            .setNegativeButton(R.string.editors_dialog_deny_rights_button_negative, (dialog, which) -> dismiss())
                            .create();
            }
            return super.onCreateDialog(savedInstanceState);
        }
    }

    public static class AddPhotoDialog extends DialogFragment {

        private Context context;

        private AddPhotoDialogCallback callback;

        public AddPhotoDialog(Context context, AddPhotoDialogCallback callback) {
            this.context = context;
            this.callback = callback;
        }

        public interface AddPhotoDialogCallback {
            void onActionNewPhoto();

            void onActionGallery();

            default void onAddPhotoDialogCanceled() {
            }
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            CharSequence[] items = {context.getString(R.string.photo_dialog_action_new), context.getString(R.string.photo_dialog_action_gallery)};
            return new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.photo_dialog_title))
                    .setItems(items, (dialog1, which) -> {
                        switch (which) {
                            case 0:
                                callback.onActionNewPhoto();
                                dismiss();
                                break;
                            case 1:
                                callback.onActionGallery();
                                dismiss();
                                break;
                        }
                    })
                    .create();
        }

        @Override
        public void onCancel(@NonNull DialogInterface dialog) {
            super.onCancel(dialog);
            callback.onAddPhotoDialogCanceled();
        }
    }

    public static class EditorExitDialog extends DialogFragment {

        public interface ProfileEditorExitDialogCallback {
            void onExitButtonClicked();

            default void onCancel() {
            }
        }

        private Context context;
        private ProfileEditorExitDialogCallback callback;

        public EditorExitDialog(Context context, ProfileEditorExitDialogCallback callback) {
            this.callback = callback;
            this.context = context;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            return new AlertDialog.Builder(context)
                    .setTitle(R.string.profile_editor_quit_dialog_title)
                    .setMessage(R.string.profile_editor_quit_dialog_message)
                    .setPositiveButton(R.string.profile_editor_quit_dialog_button_positive, (dialog, which) ->
                            callback.onExitButtonClicked())
                    .setNegativeButton(R.string.profile_editor_quit_dialog_button_negative, ((dialog, which) ->
                            callback.onCancel()))
                    .create();
        }
    }


    public static class UpdateReleasedDialog extends DialogFragment {

        @BindView(R.id.button_dialog_update)
        Button button;
        @BindView(R.id.checkbox_dialog_update)
        CheckBox checkBox;
        @BindView(R.id.tv_info_dialog_update)
        TextView tvInfo;

        private UpdateDialogCallback callback;
        private String updateLink;

        public interface UpdateDialogCallback {
            /**
             * This method is invoked when Okay button of UpdateDialog is clicked
             *
             * @param dontShowAgain shows UpdateDialog -Do not show again- checkbox state
             */
            void onButtonClicked(boolean dontShowAgain);
        }


        public UpdateReleasedDialog(String updateLink, UpdateDialogCallback callback) {
            this.callback = callback;
            this.updateLink = updateLink;
        }

        @Override
        public void onStart() {
            super.onStart();
            Dialog dialog = getDialog();
            if (dialog != null && dialog.getWindow() != null) {
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }

        @OnClick(R.id.button_dialog_update)
        void onClick() {
            callback.onButtonClicked(checkBox.isChecked());
            this.dismiss();
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.dialog_update, container, false);
            ButterKnife.bind(this, v);
            String text = inflater.getContext().getString(R.string.update_dialog_message)+" "+updateLink;
            tvInfo.setText(text);
            return v;
        }
    }

    public static class NewLifehackDialog extends DialogFragment {

        @BindView(R.id.et_dialog_new_lifehack)
        EditText et;
        @BindView(R.id.button_accept_dialog_new_lifehack)
        Button button;
        private Unbinder unbinder;
        private NewLifehackDialogCallback callback;

        public interface NewLifehackDialogCallback {
            void onButtonAcceptClicked(String content);

            default void onDialogDismissed() {
            }
        }

        public NewLifehackDialog(NewLifehackDialogCallback callback) {
            this.callback = callback;
        }


        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.dialog_new_lifehack, container, false);
            unbinder = ButterKnife.bind(this, v);
            return v;
        }

        @Override
        public void onStart() {
            super.onStart();
            Dialog dialog = getDialog();
            if (dialog != null && dialog.getWindow() != null) {
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }

        @Override
        public void onDestroyView() {
            unbinder.unbind();
            super.onDestroyView();
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            button.setOnClickListener(v -> {
                callback.onButtonAcceptClicked(et.getText().toString());
                dismiss();
            });
        }

        @Override
        public void onDismiss(@NonNull DialogInterface dialog) {
            super.onDismiss(dialog);
            callback.onDialogDismissed();
        }
    }


}
