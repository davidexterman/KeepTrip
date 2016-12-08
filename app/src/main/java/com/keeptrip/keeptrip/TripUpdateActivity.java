package com.keeptrip.keeptrip;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TripUpdateActivity extends AppCompatActivity implements TripUpdateFragment.GetCurrentTrip{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_update);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.MainToolBar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        if (findViewById(R.id.trip_update_fragment_container) != null) {

            if (savedInstanceState != null) {
                return;
            }
            getFragmentManager().beginTransaction().add(R.id.trip_update_fragment_container, new TripUpdateFragment()).commit();
        }
    }


    //--------------Interface-------------//
    public Trip getCurrentTrip(){
        return new Trip("trial", new Date(), "argentina", null, "big trip! yay!");
    }
}
