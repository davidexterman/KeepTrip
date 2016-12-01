 package com.keeptrip.keeptrip;

import android.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;


 public class TripCreateActivity extends AppCompatActivity {

     public String tripTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_create);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.MainToolBar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        if (findViewById(R.id.trip_create_fragment_container) != null) {

            if (savedInstanceState != null) {
                return;
            }
            getFragmentManager().beginTransaction().add(R.id.trip_create_fragment_container, new TripCreateTitleFragment()).commit();
        }

        else {
        //    main_fragment = (FragmentMainActivity) getFragmentManager().findFragmentById(R.id.main_fragment);

        }

    }

}
