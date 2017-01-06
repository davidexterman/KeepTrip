 package com.keeptrip.keeptrip.trip.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.dialogs.ChangesNotSavedDialogFragment;
import com.keeptrip.keeptrip.trip.fragment.TripCreateTitleFragment;
import com.keeptrip.keeptrip.model.Trip;

import java.util.Date;


 public class TripCreateActivity extends AppCompatActivity implements ChangesNotSavedDialogFragment.OnHandleDialogResult {

//     public String tripTitle = "";
//     public String tripStartDateTxt = "";
//     public Date tripStartDate;
//     public TripCreateTitleFragment tripTitleFragment = null;
//     public TripCreateDetailsFragment tripDetailsFragment = null;

     // tag
     public static final String TAG = TripCreateActivity.class.getSimpleName();

     public Trip currentCreatedTrip = null;
     private String saveTrip = "saveTrip";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_create);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.MainToolBar);
        setSupportActionBar(myToolbar);
      //  getSupportActionBar().setIcon(R.mipmap.logo);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle(getResources().getString(R.string.trip_create_new_trip_toolbar_title));

        if (findViewById(R.id.trip_create_fragment_container) != null) {

            if (savedInstanceState != null) {
                currentCreatedTrip = savedInstanceState.getParcelable(saveTrip);
                return;
            } else {
                currentCreatedTrip = new Trip("", new Date(), "", "", "");
            }

            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.trip_create_fragment_container, new TripCreateTitleFragment(), TripCreateTitleFragment.TAG)
                    .commit();
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


     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 onBackPressed();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }

     @Override
     public void onBackPressed() {
         TripCreateTitleFragment myFragment = (TripCreateTitleFragment)getFragmentManager().findFragmentByTag(TripCreateTitleFragment.TAG);
         if (myFragment != null && myFragment.isVisible()) {
             ChangesNotSavedDialogFragment notSavedDialog = new ChangesNotSavedDialogFragment();
             notSavedDialog.setTargetFragment(myFragment, ChangesNotSavedDialogFragment.NOT_SAVED_DIALOG);
             notSavedDialog.show(getFragmentManager(), "Not_saved_dialog");
         }
         else{
             super.onBackPressed();
         }
     }

     @Override
     public void onHandleDialogResult(int whichButton) {
         ChangesNotSavedDialogFragment.DialogOptions whichOptionEnum = ChangesNotSavedDialogFragment.DialogOptions.values()[whichButton];
         switch (whichOptionEnum){
             case YES:
                 super.onBackPressed();
                 break;
             case NO:
                 break;
         }
     }
}
