package com.keeptrip.keeptrip;

import android.content.Intent;
import android.os.Bundle;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class LandmarkMainActivity extends AppCompatActivity implements OnGetCurrentTrip, LandmarkDetailsFragment.GetCurrentLandmark,
        LandmarksListRowAdapter.OnOpenLandmarkDetailsForUpdate {
    public static final String TRIP_ID_PARAM = "TRIP_ID_PARAM";
    public Landmark curLandmark;
    private Trip curTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landmark_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.MainToolBar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        Intent intent = getIntent();
        curTrip = intent.getParcelableExtra(TRIP_ID_PARAM);

        if (findViewById(R.id.landmark_main_fragment) != null) {
            if (getFragmentManager().findFragmentById(R.id.landmark_main_fragment) == null)
            {
                LandmarksListFragment fragment = new LandmarksListFragment();
                getFragmentManager().beginTransaction().add(R.id.landmark_main_fragment, fragment).commit();
            }
        }
    }

    @Override
    public Trip onGetCurrentTrip() {
        return curTrip;
    }

    public Landmark onGetCurLandmark() {
        return curLandmark;
    }

    @Override
    public void onOpenLandmarkDetailsForUpdate(Landmark landmark) {
        curLandmark = landmark;
        LandmarkDetailsFragment newFragment = new LandmarkDetailsFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.landmark_main_fragment, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
