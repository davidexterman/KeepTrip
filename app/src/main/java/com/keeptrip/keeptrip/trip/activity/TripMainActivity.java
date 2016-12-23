package com.keeptrip.keeptrip.trip.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.trip.fragment.TripUpdateFragment;
import com.keeptrip.keeptrip.trip.fragment.TripViewDetailsFragment;
import com.keeptrip.keeptrip.trip.fragment.TripsListFragment;
import com.keeptrip.keeptrip.model.Trip;
import com.keeptrip.keeptrip.utils.DbUtils;
import com.keeptrip.keeptrip.utils.StartActivitiesUtils;

public class TripMainActivity extends AppCompatActivity implements
        TripUpdateFragment.OnGetCurrentTrip, TripsListFragment.OnSetCurrentTrip, TripViewDetailsFragment.OnGetCurrentTrip {

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
        if(lastTrip != null){
            currentTrip = lastTrip;
            StartActivitiesUtils.startLandmarkMainActivity(this, currentTrip);
        }
//        Trip lastTripUsed = SharedPreferencesUtils.getLastUsedTrip(this.getApplicationContext());
//        if (lastTripUsed != null){
//            currentTrip = lastTripUsed;
//            Intent intent = new Intent(this, LandmarkMainActivity.class);
//            intent.putExtra(LandmarkMainActivity.CURRENT_TRIP_PARAM, currentTrip);
//            startActivity(intent);
//        }


        TripsListFragment tripsListFragment = new TripsListFragment();
        tripsListFragment.setArguments(getIntent().getExtras());

        // Add the fragment to the 'fragment_container' FrameLayout
        if (getFragmentManager().findFragmentById(R.id.trip_main_fragment_container) == null) {
            getFragmentManager().beginTransaction().add(R.id.trip_main_fragment_container, tripsListFragment).commit();
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
}
