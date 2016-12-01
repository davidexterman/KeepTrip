package com.keeptrip.keeptrip;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class LandmarkMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landmark_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.MainToolBar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        if (findViewById(R.id.fragment_container) != null) {
            getFragmentManager().beginTransaction().add(R.id.fragment_container, new LandmarkDetailsFragment()).commit();
        }
    }
}
