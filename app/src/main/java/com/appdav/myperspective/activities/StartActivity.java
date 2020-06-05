package com.appdav.myperspective.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.appdav.myperspective.common.Constants;
import com.appdav.myperspective.common.daggerproviders.components.DaggerDataModelComponent;
import com.appdav.myperspective.common.daggerproviders.components.DaggerFirebaseComponent;
import com.appdav.myperspective.common.daggerproviders.components.FirebaseComponent;
import com.appdav.myperspective.common.daggerproviders.modules.DataModelModule;
import com.appdav.myperspective.common.data.UserData;
import com.appdav.myperspective.datamodels.UserDataModel;
import com.appdav.myperspective.R;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class StartActivity extends AppCompatActivity implements UserDataModel.UserEventListenter {

    private static final int RC_SIGN_IN = 1;

    FirebaseComponent component;
    FirebaseAuth auth;
    AuthUI ui;
    List<AuthUI.IdpConfig> authProviders;
    Unbinder unbinder;

    UserDataModel userDataModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        component = DaggerFirebaseComponent.builder().build();
        auth = component.getAuthInstance();
        DataModelModule dataModelModule = new DataModelModule();
        dataModelModule.setUserEventListenter(this);
        userDataModel = DaggerDataModelComponent.builder().dataModelModule(dataModelModule).build().getUserDataModelInstance();
        login();
    }

    //Method invoked whenever we want to try to login, it gets FirebaseUser instance and manage actions accordingly
    private void login() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            createNewUser();
        } else {
            String uid = currentUser.getUid();
            findUserDatabaseEntry(uid);
        }
    }

    //This method is invoked when no Firebase User is found or when it's found but no according user database entry is found
    private void createNewUser() {
        ui = component.getAuthUI();
        authProviders = component.getAuthProviders();
        startActivityForResult(ui.createSignInIntentBuilder()
                .setAvailableProviders(authProviders)
                .build(), RC_SIGN_IN);
    }

    //This method is invoked when Firebase User is found to search for database record with same uId
    //Result is managed through UserEventListener interface implemented by this class
    private void findUserDatabaseEntry(String uId) {
        userDataModel.findUserEntryByUid(uId);

    }


    private void setupLoginContent() {
        setContentView(R.layout.activity_start);
        unbinder = ButterKnife.bind(this);
    }

    @OnClick(R.id.button_login)
    void onClick() {
        unbinder.unbind();
        setContentView(R.layout.splash_screen);
        login();
    }

    @Override
    public void onNewUserCreated(UserData user) {
        Intent intent = new Intent(this, ProfileEditorActivity.class);
        intent.putExtra(Constants.ACTION_WITH_CURRENT_USER_INTENT, user);
        intent.putExtra(Constants.ACTION_NEW_USER, true);
        startActivity(intent);
        this.finish();
    }

    @Override
    public void onUserFound(UserData user) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Constants.ACTION_WITH_CURRENT_USER_INTENT, user);
        startActivity(intent);
        this.finish();
    }

    @Override
    public void onUserNotExist() {
        if (isAfterRegistration) {
            if (auth.getCurrentUser() != null) {
                userDataModel.createNewUserEntry(auth.getCurrentUser().getUid());
            }
        } else createNewUser();
    }

    @Override
    public void onErrorOccured() {
        Toast.makeText(this, R.string.login_error, Toast.LENGTH_SHORT).show();
        setupLoginContent();
    }

    private boolean isAfterRegistration = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                //This method creates new user entry in database with new uId got by Firebase Auth and waits for callback from UserDataModel
                if (auth.getCurrentUser() != null) {
                    isAfterRegistration = true;
                    login();
                } else setupLoginContent();
            } else {
                //When registration is cancelled or when error is occurred
                setupLoginContent();
            }
        }
    }
}
