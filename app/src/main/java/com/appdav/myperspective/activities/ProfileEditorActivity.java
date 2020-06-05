package com.appdav.myperspective.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.appdav.myperspective.common.Constants;
import com.appdav.myperspective.common.daggerproviders.components.DaggerPresenterComponent;
import com.appdav.myperspective.common.daggerproviders.components.DaggerServiceComponent;
import com.appdav.myperspective.common.data.UserData;
import com.appdav.myperspective.common.views.MaskKeyListener;
import com.appdav.myperspective.common.views.MyDialogs;
import com.appdav.myperspective.contracts.ProfileEditorContract;
import com.appdav.myperspective.presenters.ProfileEditorPresenter;
import com.appdav.myperspective.R;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import ru.tinkoff.decoro.MaskImpl;
import ru.tinkoff.decoro.parser.UnderscoreDigitSlotsParser;
import ru.tinkoff.decoro.slots.Slot;
import ru.tinkoff.decoro.watchers.FormatWatcher;
import ru.tinkoff.decoro.watchers.MaskFormatWatcher;

public class ProfileEditorActivity extends AppCompatActivity implements ProfileEditorContract.View {

    private final static String PHOTO_DIALOG_TAG = "photodialog";
    private final static String QUIT_DIALOG_TAG = "quitdialog";
    private final static int RC_CAMERA = 131;
    private final static int RC_GALLERY = 132;

    @BindView(R.id.cv_profile_editor)
    CircleImageView cvPhoto;
    @BindView(R.id.tv_edit_photo_profile_editor)
    TextView tvAddPhoto;
    @BindView(R.id.et_name_profile_editor)
    EditText etName;
    @BindView(R.id.et_surname_profile_editor)
    EditText etSurname;
    @BindView(R.id.et_birthday_profile_editor)
    EditText etBirthday;
    @BindView(R.id.et_phone_profile_editor)
    EditText etPhone;
    @BindView(R.id.spinner_rank_profile_editor)
    Spinner spinnerRank;
    @BindView(R.id.spinner_year_profile_editor)
    Spinner spinnerYear;
    @BindView(R.id.et_about_profile_editor)
    EditText etAbout;
    @BindView(R.id.iv_loader_profile_editor)
    ImageView loader;

    private MenuItem menuAccept, menuWait;

    Uri photoUri;

    private boolean isBackAlreadyPressed = false;
    private Toast toast;

    private Picasso picasso;
    @Inject
    ProfileEditorPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_editor);
        ButterKnife.bind(this);
        DaggerPresenterComponent.builder().build().injectProfileEditorActivity(this);
        picasso = DaggerServiceComponent.builder().build().getPicasso();
        presenter.attachView(this);
        presenter.viewIsReady();
    }

    @Override
    public void setModeNewUser() {
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public void setModeOldUser() {
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public UserData getCurrentUser() {
        return getIntent().getParcelableExtra(Constants.ACTION_WITH_CURRENT_USER_INTENT);
    }

    @Override
    public boolean isNewUser() {
        return getIntent().getBooleanExtra(Constants.ACTION_NEW_USER, false);
    }


    @OnClick(R.id.cv_profile_editor)
    void onCvClicked() {
        presenter.onImageClicked();
    }

    @OnClick(R.id.tv_edit_photo_profile_editor)
    void onTextViewAddPhotoClicked() {
        presenter.onTextViewAddClicked();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        menuAccept = menu.findItem(R.id.action_accept);
        menuWait = menu.findItem(R.id.action_wait);
        menuWait.setVisible(false);
        return true;
    }

    @Override
    protected void onDestroy() {
        presenter.detachView();
        presenter.destroy();
        presenter = null;
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            presenter.onBackPressed();
        }
        if (item == menuAccept) {
            presenter.onActionAccept();
        }
        return false;
    }

    @Override
    public void setupViews(UserData user) {
        if (user.getPhotoUri() != null) {
            String photoUrl;
            if (user.getPhotoUri().isEmpty()) photoUrl = Constants.DEFAULT_USER_PIC_URL;
            else photoUrl = user.getPhotoUri();
            picasso.load(photoUrl).fit().centerCrop().into(cvPhoto, new Callback() {
                @Override
                public void onSuccess() {
                    loader.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {
                    cvPhoto.setImageDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimaryLight)));
                }
            });
        }
        etName.setText(user.getName());
        etSurname.setText(user.getSurname());

        addDateOfBirthMask(etBirthday, user.getDateOfBirthTextWithYear().isEmpty() ? null : user.getDateOfBirthTextFormatNumbers());
        addPhoneMask(etPhone, user.getPhoneNumber().isEmpty() ? null : user.getPhoneNumber().substring(3));
        spinnerYear.setSelection(user.getJoiningYear());
        spinnerRank.setSelection(user.getRank());
        etAbout.setText(user.getAbout());


    }

    @Override
    public void updatePhoto(Uri uri) {
        if (loader.getVisibility() != View.VISIBLE) loader.setVisibility(View.VISIBLE);
        picasso.load(uri).fit().centerCrop().into(cvPhoto, new Callback() {
            @Override
            public void onSuccess() {
                if (loader.getVisibility() == View.VISIBLE) loader.setVisibility(View.GONE);
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    private Snackbar snackbar;

    @Override
    public void setupWaitingView() {
        menuAccept.setVisible(false);
        menuWait.setVisible(true);
        if (snackbar == null) {
            snackbar = Snackbar.make(findViewById(android.R.id.content), R.string.wait, Snackbar.LENGTH_INDEFINITE);
        }
        if (!snackbar.isShown()) snackbar.show();
    }


    @Override
    public void createPhotoDialog() {
        new MyDialogs.AddPhotoDialog(this, presenter).show(getSupportFragmentManager(), PHOTO_DIALOG_TAG);
    }

    @AfterPermissionGranted(RC_CAMERA)
    @Override
    public void createCameraActivity() {
        String[] permissions = Constants.CAMERA_PERMISSIONS;
        if (EasyPermissions.hasPermissions(this, permissions)) {
            photoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(intent, RC_CAMERA);
        } else
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_ask), RC_CAMERA, permissions);

    }

    @AfterPermissionGranted(RC_GALLERY)
    @Override
    public void createGalleryActivity() {
        String[] permissions = Constants.GALLERY_PERMISSIONS;
        if (EasyPermissions.hasPermissions(this, permissions)) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, RC_GALLERY);
        } else
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_ask), RC_GALLERY, permissions);
    }

    @Override
    public String getNameText() {
        return etName.getText().toString();
    }

    @Override
    public String getSurnameText() {
        return etSurname.getText().toString();
    }

    @Override
    public String getBirthdayText() {
        return etBirthday.getText().toString();
    }

    @Override
    public String getPhoneNumberText() {
        return etPhone.getText().toString();
    }

    private final static String LOG_TAG = "myLogs";

    @Override
    public int getJoiningYear() {
        return spinnerYear.getSelectedItemPosition();
    }

    @Override
    public void onBackPressed() {
        presenter.onBackPressed();
    }

    @Override
    public int getRank() {
        Log.d(LOG_TAG, "activity: spinnerRank position: " + spinnerRank.getSelectedItemPosition());
        return spinnerRank.getSelectedItemPosition();
    }

    @Override
    public String getAboutText() {
        return etAbout.getText().toString();
    }

    @Override
    public void setError(int editTextId) {
        View v = findViewById(editTextId);
        if (v instanceof EditText) {
            ((EditText) v).setError(getString(R.string.required_field_error));
        }
    }

    @Override
    public void setFormatError(int editTextId) {
        View v = findViewById(editTextId);
        if (v instanceof EditText) {
            ((EditText) v).setError(getString(R.string.format_error));
        }
    }

    @Override
    public void requestFocus(int editTextId) {
        findViewById(editTextId).requestFocus();
    }

    @Override
    public void finishActivityNoResult() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void finishActivityWithResult(UserData userData) {
        Intent intent = new Intent();
        intent.putExtra(Constants.ACTION_WITH_CURRENT_USER_INTENT, userData);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public Context getContext() {
        return this;
    }

    private void addPhoneMask(EditText et, @Nullable String defaultText) {
        et.setKeyListener(MaskKeyListener.getInstance());
        Slot[] slots = new UnderscoreDigitSlotsParser().parseSlots("+7 (___) ___-__-__");
        MaskImpl phoneMask = MaskImpl.createTerminated(slots);
        FormatWatcher watcher = new MaskFormatWatcher(phoneMask);
        watcher.installOn(et);
        if (defaultText != null && !defaultText.isEmpty()) et.setText(defaultText);
    }

    private void addDateOfBirthMask(EditText et, @Nullable String defaultText) {
        et.setKeyListener(MaskKeyListener.getInstance());
        Slot[] slots = new UnderscoreDigitSlotsParser().parseSlots("__.__.____");
        MaskImpl dateMask = MaskImpl.createTerminated(slots);
        FormatWatcher watcher = new MaskFormatWatcher(dateMask);
        watcher.installOn(et);
        if ((defaultText != null) && !defaultText.isEmpty()) et.setText(defaultText);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    @Override
    protected void onPause() {
        if (toast != null) toast.cancel();
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_CAMERA) {
            if (resultCode == RESULT_OK) {
                presenter.onPhotoUploaded(photoUri);
            }
        }
        if (requestCode == RC_GALLERY && resultCode == RESULT_OK && data != null) {
            presenter.onPhotoUploaded(data.getData());
        }
        //TODO: test on different devices, if any one of them doesn't work - add error screen or something
    }


    @Override
    public void showQuitToast() {
        if (isBackAlreadyPressed) {
            presenter.onCreationCanceled();
        } else {
            if (toast == null)
                toast = Toast.makeText(this, R.string.press_back_twice_to_quit, Toast.LENGTH_SHORT);
            else if (toast.getView() == null)
                toast = Toast.makeText(this, R.string.press_back_twice_to_quit, Toast.LENGTH_SHORT);
            else toast.setText(R.string.press_back_twice_to_quit);
            toast.show();
            isBackAlreadyPressed = true;
            new Handler().postDelayed(() ->
                    isBackAlreadyPressed = false, 2000);
        }
    }

    @Override
    public void showQuitDialog() {
        new MyDialogs.EditorExitDialog(this, presenter).show(getSupportFragmentManager(), QUIT_DIALOG_TAG);
    }

    @Override
    public void proceedToMainActivity(UserData userData) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Constants.ACTION_WITH_CURRENT_USER_INTENT, userData);
        startActivity(intent);
        finish();
    }
}
