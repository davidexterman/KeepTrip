package com.keeptrip.keeptrip.trip.activity;

import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.trip.fragment.TripUpdateFragment;
import com.keeptrip.keeptrip.trip.fragment.TripViewDetailsFragment;
import com.keeptrip.keeptrip.trip.fragment.TripsListFragment;
import com.keeptrip.keeptrip.model.Trip;

public class TripMainActivity extends AppCompatActivity implements
        TripUpdateFragment.OnGetCurrentTrip, TripsListFragment.OnSetCurrentTrip, TripViewDetailsFragment.OnGetCurrentTrip {

    private Trip currentTrip;
    private String saveTrip = "saveTrip";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.MainToolBar);
        setSupportActionBar(myToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        TripsListFragment tripsListFragment = new TripsListFragment();
        tripsListFragment.setArguments(getIntent().getExtras());

        // Add the fragment to the 'fragment_container' FrameLayout
        if (getFragmentManager().findFragmentById(R.id.trip_main_fragment_container) == null) {
            getFragmentManager().beginTransaction().add(R.id.trip_main_fragment_container, tripsListFragment).commit();
        }

        if(savedInstanceState != null){
            currentTrip = savedInstanceState.getParcelable(saveTrip);
        }
     //   dialogOptionsArray = getResources().getStringArray(R.array.trips_settings_dialog_options);

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
}
