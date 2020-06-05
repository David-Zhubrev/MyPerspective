package com.appdav.myperspective.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appdav.myperspective.common.Constants;
import com.appdav.myperspective.common.daggerproviders.components.DaggerPresenterComponent;
import com.appdav.myperspective.common.daggerproviders.components.DaggerServiceComponent;
import com.appdav.myperspective.common.data.UserData;
import com.appdav.myperspective.contracts.ProfileViewerContract;
import com.appdav.myperspective.presenters.ProfileViewerPresenter;
import com.appdav.myperspective.R;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileViewerActivity extends AppCompatActivity implements ProfileViewerContract.View {

    private final static int RC_EDITOR = 35003;

    private final int[] viewsInActiveGroup = {R.id.iv_profile_viewer,
            R.id.tv_name_profile_viewer,
            R.id.tv_rank_profile_viewer,
            R.id.tv_joined_profile_viewer,
            R.id.tv_birthday_profile_viewer,
            R.id.tv_phone_profile_viewer,
            R.id.tv_info_profile_viewer,
            R.id.photo_loader_profile_viewer};

    @BindView(R.id.iv_profile_viewer)
    CircleImageView ivPhoto;
    @BindView(R.id.tv_name_profile_viewer)
    TextView tvName;
    @BindView(R.id.tv_rank_profile_viewer)
    TextView tvRank;
    @BindView(R.id.tv_joined_profile_viewer)
    TextView tvJoiningYear;
    @BindView(R.id.tv_birthday_profile_viewer)
    TextView tvBirthday;
    @BindView(R.id.tv_phone_profile_viewer)
    TextView tvPhone;
    @BindView(R.id.tv_info_profile_viewer)
    TextView tvInfo;
    @BindView(R.id.loader_profile_viewer)
    ProgressBar progressBar;
    @BindView(R.id.photo_loader_profile_viewer)
    ImageView loader;


    Group group;

    Picasso picasso;
    @Inject
    ProfileViewerPresenter presenter;
    MenuItem menuItemEdit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_viewer);
        DaggerPresenterComponent.builder().build().injectProfileViewerActivity(this);
        picasso = DaggerServiceComponent.builder().build().getPicasso();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);
        group = new Group(this);
        group.setReferencedIds(viewsInActiveGroup);
        presenter.attachView(this);
        presenter.viewIsReady();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_viewer_menu, menu);
        menuItemEdit = menu.findItem(R.id.action_edit);
        presenter.onMenuCreated();
        return true;
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        if (item.getItemId() == R.id.action_edit) {
            presenter.onActionEdit();
        }
        return false;
    }

    @Override
    public String getUserId() {
        return getIntent().getStringExtra(Constants.ACTION_WITH_ANY_USER);
    }

    @Override
    public String getCurrentUserId() {
        return getIntent().getStringExtra(Constants.ACTION_WITH_CURRENT_USER_INTENT);
    }


    @Override
    public void fillViewsWithData(@Nullable UserData userData) {
        if (userData == null) {
            setErrorView();
            return;
        }
        if (userData.getPhotoUri() != null) {
            String photoUrl;
            if (userData.getPhotoUri().isEmpty()) photoUrl = Constants.DEFAULT_USER_PIC_URL;
            else photoUrl = userData.getPhotoUri();
            picasso.load(photoUrl).resize(50, 50).centerCrop().into(ivPhoto, new Callback() {
                @Override
                public void onSuccess() {
                    loader.setVisibility(View.GONE);
                    picasso.load(photoUrl).fit().centerCrop().placeholder(ivPhoto.getDrawable()).into(ivPhoto);
                }

                @Override
                public void onError(Exception e) {
                    setErrorView();
                }
            });
        }
        progressBar.setVisibility(View.INVISIBLE);
        group.setVisibility(View.VISIBLE);
        tvName.setText(userData.getUserNameAndSurname());
        String rankText = userData.getRankText(this) + " " + getString(R.string.spo_name);
        tvRank.setText(rankText);

        String joinDate = getString(R.string.profile_joined_prefix) + " " + userData.getJoiningYearText(this) + " " +
                getString(R.string.profile_joined_postfix);
        tvJoiningYear.setText(joinDate);
        if (userData.getDateOfBirth() == 0) {
            tvBirthday.setText(R.string.profile_empty_field);
        } else tvBirthday.setText(userData.getDateOfBirthTextWithYear());
        if (userData.getPhoneNumber().isEmpty()) {
            tvPhone.setText(R.string.profile_empty_field);
        } else tvPhone.setText(userData.getPhoneNumber());
        if (userData.getAbout() == null || userData.getAbout().isEmpty())
            tvInfo.setText(R.string.profile_empty_field);
        else tvInfo.setText(userData.getAbout());
    }

    @Override
    public void setErrorView() {
        //TODO: create error view
    }

    @Override
    public void proceedToEditor(UserData currentUser) {
        Intent intent = new Intent(this, ProfileEditorActivity.class);
        intent.putExtra(Constants.ACTION_WITH_CURRENT_USER_INTENT, currentUser);
        startActivityForResult(intent, RC_EDITOR);
    }

    @Override
    public void showEditMenu() {
        menuItemEdit.setVisible(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
        presenter.destroy();
        presenter = null;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_EDITOR && resultCode == RESULT_OK && data != null) {
            presenter.onUserChanged(data.getParcelableExtra(Constants.ACTION_WITH_CURRENT_USER_INTENT));
        }
    }

    @Override
    public void showUpdateSuccessfulMessage() {
        Snackbar.make(findViewById(android.R.id.content), R.string.profile_updated_successfully, Snackbar.LENGTH_SHORT).show();
    }
}
