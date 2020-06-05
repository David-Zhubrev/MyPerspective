package com.appdav.myperspective.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.appdav.myperspective.common.Constants;
import com.appdav.myperspective.common.daggerproviders.components.DaggerEditActivitiesComponent;
import com.appdav.myperspective.common.daggerproviders.components.DaggerServiceComponent;
import com.appdav.myperspective.common.daggerproviders.components.ServiceComponent;
import com.appdav.myperspective.common.daggerproviders.modules.ServiceModule;
import com.appdav.myperspective.common.views.MaskKeyListener;
import com.appdav.myperspective.common.views.MyDialogs;
import com.appdav.myperspective.contracts.NewEventContract;
import com.appdav.myperspective.presenters.NewEventPresenter;
import com.appdav.myperspective.R;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import ru.tinkoff.decoro.MaskImpl;
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser;
import ru.tinkoff.decoro.slots.Slot;
import ru.tinkoff.decoro.watchers.FormatWatcher;
import ru.tinkoff.decoro.watchers.MaskFormatWatcher;

import static android.widget.Toast.LENGTH_SHORT;

public class NewEventActivity extends AppCompatActivity implements NewEventContract.View {

    private final static String PHOTO_DIALOG_TAG = "dialogphoto";
    private final static String EXIT_DIALOG_TAG = "dialogexit";

    private final static int RC_CAMERA = 1;
    private final static int RC_GALLERY = 2;

    @Inject
    NewEventPresenter presenter;
    @Inject
    Picasso picasso;

    @BindView(R.id.et_name_new_event)
    EditText etName;
    @BindView(R.id.et_date_new_event)
    EditText etDate;
    @BindView(R.id.et_info_new_event)
    EditText etInfo;
    @BindView(R.id.iv_new_event)
    ImageView ivPhoto;
    @BindView(R.id.loader_new_event)
    ImageView loader;
    @BindView(R.id.button_remove_new_event)
    ImageView buttonRemove;

    @BindView(R.id.tv_no_photo_new_event)
    TextView tvNoPhoto;
    @BindView(R.id.tv_no_photo_hint_new_event)
    TextView tvNoPhotoHint;
    @BindView(R.id.button_add_photo_new_event)
    Button buttonAddPhoto;

    @BindView(R.id.iv_hint_new_event)
    ImageView ivHint;
    @BindView(R.id.switch_new_event)
    Switch eventTypeSwitch;

    private Uri photoUri;

    private Snackbar snackbarState, snackbarError, snackbarNoEditorsRights;
    private PopupWindow popupWindow;
    private InputMethodManager inputMethodManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);
        ButterKnife.bind(this);
        init();
        ServiceModule serviceModule = new ServiceModule();
        serviceModule.setContext(this);
        ServiceComponent serviceComponent = DaggerServiceComponent.builder().serviceModule(serviceModule).build();
        DaggerEditActivitiesComponent.builder().serviceComponent(serviceComponent).build().injectNewEventActivity(this);
        presenter.attachView(this);
        presenter.viewIsReady();
    }

    private void init() {
        Slot[] slots = new UnderscoreDigitSlotsParser().parseSlots("__.__.____, __:__");
        MaskImpl mask = MaskImpl.createTerminated(slots);
        FormatWatcher formatWatcher = new MaskFormatWatcher(mask);
        etDate.setKeyListener(MaskKeyListener.getInstance());
        formatWatcher.installOn(etDate);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        View v = LayoutInflater.from(this).inflate(R.layout.event_type_hint, null);
        popupWindow = new PopupWindow(v, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        snackbarNoEditorsRights = Snackbar.make(findViewById(android.R.id.content), R.string.new_event_snackbar_no_editors_rights, BaseTransientBottomBar.LENGTH_SHORT);
        if (!MainActivity.hasEditorsRights()) {
            eventTypeSwitch.setChecked(true);
            eventTypeSwitch.setOnClickListener(v1 -> {
                if (!snackbarNoEditorsRights.isShown()) snackbarNoEditorsRights.show();
                eventTypeSwitch.setChecked(true);
            });
        }
    }


    @Override
    public boolean isEventTypeSwitchChecked() {
        return eventTypeSwitch.isChecked();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                presenter.onBackPressed();
                break;
            case R.id.action_accept:
                presenter.onActionAccept();
                break;
        }
        return false;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @OnClick(R.id.iv_hint_new_event)
    void onHintButtonClicked() {
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus() == null ? null : getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        popupWindow.showAsDropDown(ivHint, 0, -100);
    }

    @Override
    public void onBackPressed() {
        presenter.onBackPressed();
    }

    @OnClick(R.id.button_add_photo_new_event)
    void onButtonAddClick() {
        presenter.onButtonAddPhotoClicked();
    }

    @OnClick(R.id.button_remove_new_event)
    void onButtonRemoveClick() {
        presenter.onButtonRemovePhotoClicked();
    }

    @Override
    public String getNameText() {
        return etName.getText().toString();
    }

    @Override
    public String getDateText() {
        return etDate.getText().toString();
    }

    @Override
    public String getInfoText() {
        return etInfo.getText().toString();
    }

    @Override
    public void setNameFieldFocus() {
        etName.requestFocus();
    }

    @Override
    public void setDateFieldFocus() {
        etDate.requestFocus();
    }

    @Override
    public void setInfoFieldFocus() {
        etInfo.requestFocus();
    }

    @Override
    public void setNameFieldError() {
        etName.setError(getString(R.string.required_field_error));
    }

    @Override
    public void setDateFieldEmptyError() {
        etDate.setError(getString(R.string.required_field_error));
    }

    @Override
    public void setDateFieldFormatError() {
        etDate.setError(getString(R.string.format_error));
    }


    @Override
    public void setDateFieldInstantEventError() {
        etDate.setError(getString(R.string.instant_event_duration_error));
    }

    @Override
    public void setPastTimeError() {
        etDate.setError(getString(R.string.past_time_error));
    }

    @Override
    public void setInfoFieldError() {
        etInfo.setError(getString(R.string.required_field_error));
    }

    @Override
    public void setPhotoError() {
        Toast.makeText(this, R.string.photo_error, LENGTH_SHORT).show();
    }

    @Override
    public void setNoPhotoView() {
        ivPhoto.setVisibility(View.INVISIBLE);
        buttonRemove.setVisibility(View.INVISIBLE);
        loader.setVisibility(View.INVISIBLE);

        tvNoPhoto.setVisibility(View.VISIBLE);
        tvNoPhotoHint.setVisibility(View.VISIBLE);
        buttonAddPhoto.setVisibility(View.VISIBLE);
    }

    @Override
    public void setPhotoView(Uri uri) {
        tvNoPhotoHint.setVisibility(View.INVISIBLE);
        tvNoPhoto.setVisibility(View.INVISIBLE);
        buttonAddPhoto.setVisibility(View.INVISIBLE);
        loader.setVisibility(View.VISIBLE);
        buttonRemove.setVisibility(View.VISIBLE);
        ivPhoto.setVisibility(View.VISIBLE);
        ivPhoto.bringToFront();
        loader.setVisibility(View.VISIBLE);
        picasso.cancelRequest(ivPhoto);
        picasso.invalidate(uri);
        picasso.load(uri).memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE).fit().centerCrop().into(ivPhoto, new Callback() {

            @Override
            public void onSuccess() {
                loader.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    @Override
    public void createExitDialog() {
        new MyDialogs.EditorExitDialog(this, presenter).show(getSupportFragmentManager(), EXIT_DIALOG_TAG);
    }

    @Override
    public void showUploadSuccessfulMessage() {
        snackbarState = Snackbar.make(findViewById(android.R.id.content), R.string.new_event_successful, Snackbar.LENGTH_LONG);
        snackbarState.show();
    }

    MenuItem itemActionAccept, itemWait;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        itemActionAccept = menu.findItem(R.id.action_accept);
        itemWait = menu.findItem(R.id.action_wait);
        itemWait.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void createAddPhotoDialog() {
        new MyDialogs.AddPhotoDialog(this, presenter).show(getSupportFragmentManager(), PHOTO_DIALOG_TAG);
    }

    @AfterPermissionGranted(RC_CAMERA)
    @Override
    public void startCameraActivity() {
        String[] permissions = Constants.CAMERA_PERMISSIONS;
        if (EasyPermissions.hasPermissions(this, permissions)) {
            photoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(intent, RC_CAMERA);
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_ask), RC_CAMERA, permissions);
        }
    }

    @AfterPermissionGranted(RC_GALLERY)
    @Override
    public void startGalleryActivity() {
        String[] permissions = Constants.GALLERY_PERMISSIONS;
        if (EasyPermissions.hasPermissions(this, permissions)) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(Intent.createChooser(intent, getString(R.string.new_event_choose_photo)), RC_GALLERY);
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_ask), RC_GALLERY, permissions);
        }
    }

    @Override
    public void setInactive() {
        etName.setActivated(false);
        etInfo.setActivated(false);
        etDate.setActivated(false);
        ivPhoto.setActivated(false);
        buttonRemove.setActivated(false);
        buttonAddPhoto.setActivated(false);

        itemActionAccept.setVisible(false);
        itemWait.setVisible(true);
        if (snackbarState == null)
            snackbarState = Snackbar.make(findViewById(android.R.id.content), R.string.wait, Snackbar.LENGTH_INDEFINITE);
        if (!snackbarState.isShown()) snackbarState.show();
    }

    @Override
    public void setActive() {
        etName.setActivated(true);
        etDate.setActivated(true);
        etInfo.setActivated(true);

        itemActionAccept.setVisible(true);
        itemWait.setVisible(false);
        ivPhoto.setActivated(true);
        buttonRemove.setActivated(true);
        buttonAddPhoto.setActivated(true);

        if (snackbarState != null && snackbarState.isShown()) snackbarState.dismiss();
    }

    @Override
    public void finishActivity(boolean isSuccessful) {
        presenter.detachView();
        presenter.destroy();
        presenter = null;
        if (isSuccessful) setResult(RESULT_OK);
        else setResult(RESULT_CANCELED);
        this.finish();
    }

    @Override
    public void setDatabaseErrorMessage() {
        if (snackbarError == null)
            snackbarError = Snackbar.make(findViewById(android.R.id.content), R.string.database_error, Snackbar.LENGTH_LONG).setAction(R.string.retry, v -> {
                presenter.onActionAccept();
                snackbarError.dismiss();
            });
        if (!snackbarError.isShown()) snackbarError.show();
    }


    @Override
    protected void onDestroy() {
        if (presenter != null) {
            presenter.detachView();
            presenter.destroy();
            presenter = null;
        }
        super.onDestroy();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_CAMERA) {
            if (resultCode == RESULT_OK) {
                presenter.onPhotoUploaded(photoUri);
            }
        }
        if (requestCode == RC_GALLERY && resultCode == RESULT_OK) {
            if (data != null) {
                presenter.onPhotoUploaded(data.getData());
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


}
