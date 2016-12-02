package com.keeptrip.keeptrip;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class TripMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.MainToolBar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        if (savedInstanceState != null) {
            return;
        }

        TripsListFragment tripsListFragment = new TripsListFragment();
        tripsListFragment.setArguments(getIntent().getExtras());

        // Add the fragment to the 'fragment_container' FrameLayout
        getFragmentManager().beginTransaction().add(R.id.trip_main_fragment_container, tripsListFragment).commit();
    }
}
