package com.keeptrip.keeptrip;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class LandmarkMainActivity extends AppCompatActivity implements
        LandmarkDetailsFragment.GetCurrentLandmark, LandmarksListFragment.OnSetCurrentLandmark{
    public static final String TRIP_ID_PARAM = "TRIP_ID_PARAM";
    public Landmark currentLandmark;
    private int currentTripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landmark_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.MainToolBar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setIcon(R.mipmap.logo);
        Intent intent = getIntent();
        currentTripId = intent.getIntExtra(TRIP_ID_PARAM, -1);

        if (findViewById(R.id.landmark_main_fragment_container) != null) {
            if (getFragmentManager().findFragmentById(R.id.landmark_main_fragment_container) == null)
            {
                LandmarksListFragment fragment = new LandmarksListFragment();
                getFragmentManager().beginTransaction().add(R.id.landmark_main_fragment_container, fragment).commit();
            }
        }
    }


    @Override
    public void onSetCurrentLandmark(Landmark landmark) {
        currentLandmark = landmark;
    }

//    @Override
//    public int onGetCurrentTripId() {
//        return currentTripId;
//    }

    public Landmark onGetCurLandmark() {
        return currentLandmark;
    }


//    @Override
//    public void onOpenLandmarkDetailsForUpdate(Landmark landmark) {
//        currentLandmark = landmark;
//        LandmarkDetailsFragment newFragment = new LandmarkDetailsFragment();
//        FragmentTransaction transaction = getFragmentManager().beginTransaction();
//        transaction.replace(R.id.landmark_main_fragment_container, newFragment);
//        transaction.addToBackStack(null);
//        transaction.commit();
//    }
}
