package com.appdav.myperspective.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.appdav.myperspective.App;
import com.appdav.myperspective.R;
import com.appdav.myperspective.common.services.Preferences;
import com.appdav.myperspective.common.daggerproviders.components.DaggerServiceComponent;
import com.appdav.myperspective.common.daggerproviders.modules.ServiceModule;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InfoActivity extends AppCompatActivity {

    @BindView(R.id.tv_info_version)
    TextView tvVersion;
    Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_screen);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ServiceModule module = new ServiceModule();
        module.setContext(this);
        preferences = DaggerServiceComponent.builder().serviceModule(module).build().getPreferences();
        String text = getString(R.string.info_version) + " ";
        text += App.APP_VERSION;
        tvVersion.setText(text);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) this.onBackPressed();
        return false;
    }

}
