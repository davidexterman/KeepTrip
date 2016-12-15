package com.keeptrip.keeptrip.landmark.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.keeptrip.keeptrip.landmark.fragment.LandmarkDetailsFragment;
import com.keeptrip.keeptrip.landmark.fragment.LandmarksListFragment;
import com.keeptrip.keeptrip.landmark.interfaces.OnGetCurrentTripId;
import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.model.Landmark;

public class LandmarkMainActivity extends AppCompatActivity implements OnGetCurrentTripId,
        LandmarkDetailsFragment.GetCurrentLandmark, LandmarksListFragment.OnSetCurrentLandmark {
    public static final String TRIP_ID_PARAM = "TRIP_ID_PARAM";
    public static final String TRIP_TITLE_PARAM = "TRIP_TITLE_PARAM";
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
        String currentTripTitle = intent.getStringExtra(TRIP_TITLE_PARAM);

        setTitle(currentTripTitle);

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

    @Override
    public int onGetCurrentTripId() {
        return currentTripId;
    }

    @Override
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
