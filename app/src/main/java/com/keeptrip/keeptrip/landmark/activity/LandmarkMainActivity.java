package com.keeptrip.keeptrip.landmark.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.keeptrip.keeptrip.landmark.fragment.LandmarkDetailsFragment;
import com.keeptrip.keeptrip.landmark.fragment.LandmarksListFragment;
import com.keeptrip.keeptrip.landmark.interfaces.OnGetCurrentLandmark;
import com.keeptrip.keeptrip.landmark.interfaces.OnGetCurrentTripId;
import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.model.Landmark;
import com.keeptrip.keeptrip.model.Trip;
import com.keeptrip.keeptrip.trip.fragment.TripUpdateFragment;
import com.keeptrip.keeptrip.trip.fragment.TripViewDetailsFragment;

public class LandmarkMainActivity extends AppCompatActivity implements OnGetCurrentTripId,
        OnGetCurrentLandmark, LandmarksListFragment.OnSetCurrentLandmark, LandmarksListFragment.GetCurrentTripTitle,
        TripViewDetailsFragment.OnGetCurrentTrip, TripUpdateFragment.OnGetCurrentTrip{

    public static final String TRIP_ID_PARAM = "TRIP_ID_PARAM";
    public static final String TRIP_TITLE_PARAM = "TRIP_TITLE_PARAM";
    public static final String CURRENT_TRIP_PARAM = "CURRENT_TRIP_PARAM";

    private static final String SAVE_TRIP = "SAVE_TRIP";
    private static final String SAVE_LANDMARK = "SAVE_LANDMARK";
    public Landmark currentLandmark;
    private int currentTripId;
    private String currentTripTitle;
    private Trip currentTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landmark_main);

        if (savedInstanceState != null){
            currentLandmark = savedInstanceState.getParcelable(SAVE_LANDMARK);
            currentTrip = savedInstanceState.getParcelable(SAVE_TRIP);
        }

        Toolbar myToolbar = (Toolbar) findViewById(R.id.MainToolBar);
        setSupportActionBar(myToolbar);
       // getSupportActionBar().setIcon(R.mipmap.logo);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        currentTrip = intent.getParcelableExtra(CURRENT_TRIP_PARAM);
        currentTripId = currentTrip.getId();
        currentTripTitle = currentTrip.getTitle();
//        currentTripId = intent.getIntExtra(TRIP_ID_PARAM, -1);
//        currentTripTitle = intent.getStringExtra(TRIP_TITLE_PARAM);


        if (findViewById(R.id.landmark_main_fragment_container) != null) {
            if (getFragmentManager().findFragmentById(R.id.landmark_main_fragment_container) == null)
            {
                LandmarksListFragment fragment = new LandmarksListFragment();
                getFragmentManager().beginTransaction().add(R.id.landmark_main_fragment_container, fragment).commit();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_LANDMARK, currentLandmark);
        outState.putParcelable(SAVE_TRIP, currentTrip);
    }

    @Override
    public void onSetCurrentLandmark(Landmark landmark) {
        currentLandmark = landmark;
    }

    @Override
    public int onGetCurrentTripId() {
        return currentTripId;
    }

    @Override
    public Landmark onGetCurrentLandmark() {
        return currentLandmark;
    }

    @Override
    public String getCurrentTripTitle() {
        return currentTripTitle;
    }

    @Override
    public Trip onGetCurrentTrip() {
        return currentTrip;
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