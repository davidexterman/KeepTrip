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
import com.keeptrip.keeptrip.utils.LocationUtilsActivity;

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
        //    Location currentLocation = new LocationUtils().getCurrentLocation(this);
            Location currentLocation = new Location("");
            Intent getLocationIntent = new Intent(this, LocationUtilsActivity.class);
            startActivityForResult(getLocationIntent, LocationUtilsActivity.REQUEST_LOCATION_PERMISSION_ACTION);
         //   startActivity(new Intent(this, LocationUtilsActivity.class));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case LocationUtilsActivity.REQUEST_LOCATION_PERMISSION_ACTION:
                if (resultCode == RESULT_OK && data != null) {
                    Trip lastTrip = DbUtils.getLastTrip(this);
                    Location currentLocation = data.getParcelableExtra(LocationUtilsActivity.CURRENT_LOCATION_RESULT);
                    String currentLocationName = LocationUtils.updateLmLocationString(this, currentLocation);
                    String title = (currentLocationName == null || currentLocationName.trim().isEmpty()) ? getResources().getString(R.string.location_landmark_default_title) : currentLocationName;
                    Landmark newLandmark = new Landmark(lastTrip.getId(), title,
                            "", DateUtils.getDateOfToday(), currentLocationName, currentLocation, "", 0);
                    Landmark newLandmark = new Landmark(lastTrip.getId(), currentLocationName,
                            "", DateUtils.getDateOfToday(), currentLocationName, currentLocation, "", "", 0);


                    // Insert data to DataBase
                    getContentResolver().insert(
                            KeepTripContentProvider.CONTENT_LANDMARKS_URI,
                            newLandmark.landmarkToContentValues());

                    Toast.makeText(this, getResources().getString(R.string.toast_location_landmark_added_message_success, title, lastTrip.getTitle()), Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this, getResources().getString(R.string.toast_landmark_added_message_fail), Toast.LENGTH_SHORT).show();
                }
                finishAffinity();
//                finish();
                break;
        }
    }
}
