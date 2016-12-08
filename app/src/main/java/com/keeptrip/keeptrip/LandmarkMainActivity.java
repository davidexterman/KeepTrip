package com.keeptrip.keeptrip;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class LandmarkMainActivity extends AppCompatActivity implements LandmarksListFragment.OnGetCurTrip {
    public static final String TRIP_ID_PARAM = "TRIP_ID_PARAM";
    private Landmark curLandmark;
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

        if (findViewById(R.id.fragment_container) != null) {
            if (getFragmentManager().findFragmentById(R.id.fragment_container) == null)
            {
                LandmarksListFragment fragment = new LandmarksListFragment();
                getFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();
            }
        }
    }

    @Override
    public Trip onGetCurTrip() {
        return curTrip;
    }
}
