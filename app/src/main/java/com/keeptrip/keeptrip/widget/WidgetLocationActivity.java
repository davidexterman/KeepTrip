package com.keeptrip.keeptrip.widget;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.AsyncTask;
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

    private AsyncTask<Void, Void, String> updateLocationTask;
    private ProgressDialog progressDialog;
    private Location currentLocation;
    private static String SAVE_CURRENT_LOCATION = "SAVE_CURRENT_LOCATION";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_location);

        if(savedInstanceState != null){
            currentLocation = savedInstanceState.getParcelable(SAVE_CURRENT_LOCATION);
        }
        initProgressDialog();

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
    private void initProgressDialog(){
        progressDialog = new ProgressDialog(this);

        // Set progress dialog style spinner
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // Set the progress dialog title and message
        progressDialog.setTitle(getResources().getString(R.string.toast_widget_location_utils_open_title));
        progressDialog.setMessage(getResources().getString(R.string.toast_widget_location_open_massage));

        // Set the progress dialog background color
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        progressDialog.setIndeterminate(false);

        progressDialog.setCancelable(false);

        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.toast_widget_location_cancel_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancelTask();
                completeAdding(null, currentLocation);
            }
        });
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
                    currentLocation = data.getParcelableExtra(LocationUtilsActivity.CURRENT_LOCATION_RESULT);
                    progressDialog.show();
                    createUpdateLocationTask(currentLocation);
                    break;
                }
                else {
                    Toast.makeText(this, getResources().getString(R.string.toast_landmark_added_message_fail), Toast.LENGTH_SHORT).show();
                    finishAffinity();
                }
        }
    }

    private void completeAdding(String currentLocationName, Location currentLocation){
//        String currentLocationName = LocationUtils.updateLmLocationString(this, currentLocation);
        Trip lastTrip = DbUtils.getLastTrip(this);
        String title = (currentLocationName == null || currentLocationName.trim().isEmpty()) ? getResources().getString(R.string.location_landmark_default_title) : currentLocationName;
        Landmark newLandmark = new Landmark(lastTrip.getId(), title,
                "", DateUtils.getDateOfToday(), currentLocationName, currentLocation, "", "", 0);

        // Insert data to DataBase
        getContentResolver().insert(
                KeepTripContentProvider.CONTENT_LANDMARKS_URI,
                newLandmark.landmarkToContentValues());

        Toast.makeText(this, getResources().getString(R.string.toast_location_landmark_added_message_success, title, lastTrip.getTitle()), Toast.LENGTH_SHORT).show();
        finishAffinity();
    }



    private void createUpdateLocationTask(final Location currentLocation){
        if(updateLocationTask != null && updateLocationTask.getStatus() == AsyncTask.Status.RUNNING){
            updateLocationTask.cancel(true);
        }
        updateLocationTask = new AsyncTask<Void, Void, String>(){
            @Override
            protected void onPostExecute(String stringResult) {
                super.onPostExecute(stringResult);
                progressDialog.dismiss();
                completeAdding(stringResult, currentLocation);
            }

            @Override
            protected String doInBackground(Void... params) {
                return LocationUtils.updateLmLocationString(WidgetLocationActivity.this, currentLocation);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void cancelTask(){
        if(updateLocationTask != null) {
            if(!updateLocationTask.isCancelled()) {
                updateLocationTask.cancel(true);
            }
            updateLocationTask = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTask();
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_CURRENT_LOCATION, currentLocation);
    }
}
