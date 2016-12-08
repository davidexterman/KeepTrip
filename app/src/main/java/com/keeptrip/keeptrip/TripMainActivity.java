package com.keeptrip.keeptrip;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class TripMainActivity extends AppCompatActivity implements TripsListFragment.OnSetCurTripListener {
    private Trip curTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.MainToolBar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        TripsListFragment tripsListFragment = new TripsListFragment();
        tripsListFragment.setArguments(getIntent().getExtras());

        // Add the fragment to the 'fragment_container' FrameLayout
        if (getFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            getFragmentManager().beginTransaction().add(R.id.trip_main_fragment_container, tripsListFragment).commit();
        }
    }

    @Override
    public void onSetCurTrip(Trip trip) {
        curTrip = trip;
    }
}
