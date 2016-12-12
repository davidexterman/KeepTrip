 package com.keeptrip.keeptrip;

import android.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.Date;


 public class TripCreateActivity extends AppCompatActivity {

//     public String tripTitle = "";
//     public String tripStartDateTxt = "";
//     public Date tripStartDate;
//     public TripCreateTitleFragment tripTitleFragment = null;
//     public TripCreateDetailsFragment tripDetailsFragment = null;

     public Trip currentCreatedTrip = null;
     private String saveTrip = "saveTrip";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_create);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.MainToolBar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setIcon(R.mipmap.logo);

        if (findViewById(R.id.trip_create_fragment_container) != null) {

            if (savedInstanceState != null) {
                currentCreatedTrip = savedInstanceState.getParcelable(saveTrip);
                return;

            }

            getFragmentManager().beginTransaction().add(R.id.trip_create_fragment_container, new TripCreateTitleFragment()).commit();
        }

        //TODO: add static fragments handling
        else {
        //    main_fragment = (FragmentMainActivity) getFragmentManager().findFragmentById(R.id.main_fragment);

        }

    }
     //-----------------Save and Restore handle-------------------//
     @Override
     public void onSaveInstanceState(Bundle state) {
         super.onSaveInstanceState(state);
         state.putParcelable(saveTrip, currentCreatedTrip);
     }
}
