package com.keeptrip.keeptrip.general;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.utils.SharedPreferencesUtils;

public class SettingsActivity extends AppCompatActivity {

    Switch switchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.MainToolBar);
        setSupportActionBar(myToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //toolbar
        setTitle(getResources().getString(R.string.app_settings_toolbar_title));

        switchButton = (Switch) findViewById(R.id.notifications_switch);

        switchButton.setChecked(SharedPreferencesUtils.getNotificationsState(getApplicationContext()));
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {
                SharedPreferencesUtils.saveNotificationsState(getApplicationContext(), bChecked);
                if (bChecked) {

                } else {

                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
