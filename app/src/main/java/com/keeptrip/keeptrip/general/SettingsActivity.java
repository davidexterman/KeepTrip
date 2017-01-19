package com.keeptrip.keeptrip.general;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.model.Trip;
import com.keeptrip.keeptrip.utils.DbUtils;
import com.keeptrip.keeptrip.utils.NotificationUtils;
import com.keeptrip.keeptrip.utils.SharedPreferencesUtils;

public class SettingsActivity extends AppCompatActivity {

    Switch enableQuickLandmarkSwitchButton;
    Switch enableCancelWarningSwitchButton;

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

        enableQuickLandmarkSwitchButton = (Switch) findViewById(R.id.notifications_switch);
        enableQuickLandmarkSwitchButton.setChecked(SharedPreferencesUtils.getEnableNotificationsState(getApplicationContext()));
        enableQuickLandmarkSwitchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {
                SharedPreferencesUtils.saveEnableNotificationsState(getApplicationContext(), bChecked);
                if (bChecked) {
                    // update the notification with the last trip if there is
                    SharedPreferencesUtils.saveCloseNotificationsState(SettingsActivity.this, false);
                    Trip latestTrip = DbUtils.getLastTrip(SettingsActivity.this);
                    if(latestTrip != null){
                        NotificationUtils.initNotification(SettingsActivity.this, latestTrip.getTitle());
                    }
                    else {
                        Toast.makeText(SettingsActivity.this, getResources().getString(R.string.app_settings_switch_enable_notifications_no_trips_message), Toast.LENGTH_SHORT).show();

                    }
                } else {
                    NotificationUtils.cancelNotification(SettingsActivity.this);
                }
            }
        });

        enableCancelWarningSwitchButton = (Switch) findViewById(R.id.show_warning_switch);
        enableCancelWarningSwitchButton.setChecked(!SharedPreferencesUtils.getCancelNotificationsWarningDialogState(this));
        enableCancelWarningSwitchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {
                SharedPreferencesUtils.saveCancelNotificationsWarningDialogState(getApplicationContext(), !bChecked);
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
