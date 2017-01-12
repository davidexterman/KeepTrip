package com.keeptrip.keeptrip.trip.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.dialogs.ChangesNotSavedDialogFragment;
import com.keeptrip.keeptrip.trip.fragment.TripUpdateFragment;
import com.keeptrip.keeptrip.trip.fragment.TripViewDetailsFragment;
import com.keeptrip.keeptrip.trip.fragment.TripsListFragment;
import com.keeptrip.keeptrip.model.Trip;
import com.keeptrip.keeptrip.trip.interfaces.OnGetCurrentTrip;
import com.keeptrip.keeptrip.utils.DbUtils;
import com.keeptrip.keeptrip.utils.LocationUtils;
import com.keeptrip.keeptrip.utils.StartActivitiesUtils;
import com.keeptrip.keeptrip.widget.WidgetLocationActivity;

public class TripMainActivity extends AppCompatActivity implements TripsListFragment.OnSetCurrentTrip, OnGetCurrentTrip,
        ChangesNotSavedDialogFragment.OnHandleDialogResult{

    // tag
    public static final String TAG = TripMainActivity.class.getSimpleName();

    private Trip currentTrip;
    private String saveTrip = "saveTrip";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_main);

        if(savedInstanceState != null){
            currentTrip = savedInstanceState.getParcelable(saveTrip);
        }

        Toolbar myToolbar = (Toolbar) findViewById(R.id.MainToolBar);
        setSupportActionBar(myToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Trip lastTrip = DbUtils.getLastTrip(this);
        if(lastTrip != null && savedInstanceState == null){
            currentTrip = lastTrip;
            StartActivitiesUtils.startLandmarkMainActivity(this, lastTrip);
        }

        TripsListFragment tripsListFragment = new TripsListFragment();
        tripsListFragment.setArguments(getIntent().getExtras());

        // Add the fragment to the 'fragment_container' FrameLayout
        if (getFragmentManager().findFragmentById(R.id.trip_main_fragment_container) == null) {
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.trip_main_fragment_container, tripsListFragment, TripsListFragment.TAG)
                    .commit();
        }


    }

    @Override
    public void onSetCurrentTrip(Trip trip) {
        currentTrip = trip;
    }

    @Override
    public Trip onGetCurrentTrip() {
        return currentTrip;
    }

    //-------------Toolbar---------------//
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

    //-------------save----------------//
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(saveTrip, currentTrip);
    }

    @Override
    public void onBackPressed() {
        TripUpdateFragment myFragment = (TripUpdateFragment)getFragmentManager().findFragmentByTag(TripUpdateFragment.TAG);
        if (myFragment != null && myFragment.isVisible()) {
            ChangesNotSavedDialogFragment notSavedDialog = new ChangesNotSavedDialogFragment();
            notSavedDialog.setTargetFragment(myFragment, ChangesNotSavedDialogFragment.NOT_SAVED_DIALOG);
            notSavedDialog.show(getFragmentManager(), "Not_saved_dialog");
        }
        else{
            super.onBackPressed();
        }
    }

    @Override
    public void onHandleDialogResult(int whichButton) {
        ChangesNotSavedDialogFragment.DialogOptions whichOptionEnum = ChangesNotSavedDialogFragment.DialogOptions.values()[whichButton];
        switch (whichOptionEnum){
            case YES:
                super.onBackPressed();
                break;
            case NO:
                break;
        }
    }
}
