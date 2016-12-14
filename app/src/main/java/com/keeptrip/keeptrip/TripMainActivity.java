package com.keeptrip.keeptrip;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

public class TripMainActivity extends AppCompatActivity implements TripsListRowAdapter.OnTripLongPress,
        OnGetCurrentTripId { //todo:fix! delete onSetCurrentTrip interface
    //todo:fix!
//    private Trip curTrip;
//    Trip Dialog Options Handling
        private String[] dialogOptionsArray;
        private AlertDialog optionsDialog;
        private int currentTripId;
//
//
//    private enum DialogOptions{
//        EDIT,
//        DELETE
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.MainToolBar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setIcon(R.mipmap.logo);

        TripsListFragment tripsListFragment = new TripsListFragment();
        tripsListFragment.setArguments(getIntent().getExtras());

        // Add the fragment to the 'fragment_container' FrameLayout
        if (getFragmentManager().findFragmentById(R.id.trip_main_fragment_container) == null) {
            getFragmentManager().beginTransaction().add(R.id.trip_main_fragment_container, tripsListFragment).commit();
        }

        dialogOptionsArray = getResources().getStringArray(R.array.trips_settings_dialog_options);
        initDialog();
    }


    //todo:fix!
//    //TODO: add interface
//    @Override
//    public void onSetCurrentTrip(Trip trip) {
//        curTrip = trip;
//    }


    @Override
    public int onGetCurrentTripId() {
        return currentTripId;
        //return curTrip;
    }

    @Override
    public void onTripLongPress(Trip trip){
        //todo:fix!
        // curTrip = trip;
        optionsDialog.setTitle(trip.getTitle());
        optionsDialog.show();
    }

    private void initDialog() {

        AlertDialog.Builder optionsDialogBuilder = new AlertDialog.Builder(this);
        optionsDialogBuilder.setItems(dialogOptionsArray, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                TripOptionsDialogFragment.DialogOptions whichOptionEnum = TripOptionsDialogFragment.DialogOptions.values()[which];
                switch (whichOptionEnum){
                    case EDIT:
                        TripUpdateFragment newFragment = new TripUpdateFragment();
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.replace(R.id.trip_main_fragment_container, newFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                        break;
                    case DELETE:
                        //TODO: add refreshing
//                        SingletonAppDataProvider.getInstance(TripMainActivity.this).deleteTrip(curTrip.getId());
                        break;
                }
            }
        });
        optionsDialog = optionsDialogBuilder.create();
        ListView listView = optionsDialog.getListView();

        //TODO: remove divider at the end
        listView.setDivider(new ColorDrawable(ContextCompat.getColor
                (getApplicationContext(), R.color.toolBarLineBackground))); // set color
        listView.setDividerHeight(2); // set height
    }
}
