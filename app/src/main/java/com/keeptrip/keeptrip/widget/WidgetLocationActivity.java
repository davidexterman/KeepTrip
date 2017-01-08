package com.keeptrip.keeptrip.widget;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.contentProvider.KeepTripContentProvider;
import com.keeptrip.keeptrip.dialogs.NoTripsDialogFragment;
import com.keeptrip.keeptrip.model.Landmark;
import com.keeptrip.keeptrip.model.Trip;
import com.keeptrip.keeptrip.utils.DateUtils;
import com.keeptrip.keeptrip.utils.DbUtils;
import com.keeptrip.keeptrip.utils.LocationUtils;

import java.util.Calendar;

public class WidgetLocationActivity extends Activity implements NoTripsDialogFragment.NoTripDialogClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_location);

        if(savedInstanceState == null) {

                Trip lastTrip = DbUtils.getLastTrip(this);
                if(lastTrip == null){
                    NoTripsDialogFragment dialogFragment = new NoTripsDialogFragment();

                    Bundle args = new Bundle();
                    args.putInt(dialogFragment.CALLED_FROM_WHERE_ARGUMENT, dialogFragment.CALLED_FROM_ACTIVITY);
                    dialogFragment.setArguments(args);
                    dialogFragment.show(getFragmentManager(), "noTrips");
                }

                else {
                    addLocationLandmark();
                }
        }
    }

    private void addLocationLandmark() {
        Trip lastTrip = DbUtils.getLastTrip(this);
        if(lastTrip != null) {
            Location currentLocation = LocationUtils.getCurrentLocation(this);
            Landmark newLandmark = new Landmark(lastTrip.getId(),
                    "My Landmark", "", DateUtils.getDateOfToday(), "", currentLocation, "", 0);

            // Insert data to DataBase
            getContentResolver().insert(
                    KeepTripContentProvider.CONTENT_LANDMARKS_URI,
                    newLandmark.landmarkToContentValues());

            Toast.makeText(this, getResources().getString(R.string.toast_landmark_added_message_success), Toast.LENGTH_SHORT).show();
            finishAffinity();
        }
    }

    @Override
    public void onClickHandleParent(int whichButton, String newTripTitle) {
        NoTripsDialogFragment.DialogOptions whichOptionEnum = NoTripsDialogFragment.DialogOptions.values()[whichButton];
        switch (whichOptionEnum){
            case DONE:
                Trip newTrip = new Trip(newTripTitle, Calendar.getInstance().getTime(), "", "", "");
                DbUtils.addNewTrip(this, newTrip);
                addLocationLandmark();
                break;
            case CANCEL:
                Toast.makeText(this, getResources().getString(R.string.toast_no_trips_dialog_canceled_message), Toast.LENGTH_LONG).show();
                finishAffinity();
        }
    }
}
