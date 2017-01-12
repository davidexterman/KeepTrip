package com.keeptrip.keeptrip.landmark.activity;

import android.content.CursorLoader;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.keeptrip.keeptrip.contentProvider.KeepTripContentProvider;
import com.keeptrip.keeptrip.dialogs.ChangesNotSavedDialogFragment;
import com.keeptrip.keeptrip.landmark.fragment.LandmarkDetailsFragment;
import com.keeptrip.keeptrip.landmark.fragment.LandmarksListFragment;
import com.keeptrip.keeptrip.landmark.interfaces.OnGetCurrentLandmark;
import com.keeptrip.keeptrip.trip.interfaces.OnGetCurrentTrip;
import com.keeptrip.keeptrip.landmark.interfaces.OnGetCurrentTripId;
import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.model.Landmark;
import com.keeptrip.keeptrip.model.Trip;
import com.keeptrip.keeptrip.trip.fragment.TripUpdateFragment;
import com.keeptrip.keeptrip.utils.ImageUtils;
import com.keeptrip.keeptrip.utils.NotificationUtils;

public class LandmarkMainActivity extends AppCompatActivity implements OnGetCurrentTripId,
        OnGetCurrentLandmark, OnGetCurrentTrip, LandmarksListFragment.OnSetCurrentLandmark, LandmarksListFragment.GetCurrentTripTitle,
        LandmarksListFragment.OnGetIsLandmarkAdded, LandmarkDetailsFragment.OnLandmarkAddedListener,
        ChangesNotSavedDialogFragment.OnHandleDialogResult {

    // tag
    public static final String TAG = LandmarkMainActivity.class.getSimpleName();

    public static final String CURRENT_TRIP_PARAM = "CURRENT_TRIP_PARAM";

    private static final String SAVE_TRIP = "SAVE_TRIP";
    private static final String SAVE_LANDMARK = "SAVE_LANDMARK";
    private static final String SAVE_IS_LANDMARK_ADDED = "SAVE_IS_LANDMARK_ADDED";
    public Landmark currentLandmark;
    private Trip currentTrip;
    private boolean isLandmarkAdded;
    private String searchQuery;


    private String imageFromGalleryPath;
    public static final String IMAGE_FROM_GALLERY_PATH = "IMAGE_FROM_GALLERY_PATH";
    public static final String LandmarkNewLocation = "LandmarkNewLocation";
    public static final String LandmarkNewGPSLocation = "LandmarkNewGPSLocation";
    public static final String LandmarkArrayList ="LandmarkArrayList";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landmark_main);

        if (savedInstanceState != null){
            currentLandmark = savedInstanceState.getParcelable(SAVE_LANDMARK);
            currentTrip = savedInstanceState.getParcelable(SAVE_TRIP);
            isLandmarkAdded = savedInstanceState.getBoolean(SAVE_IS_LANDMARK_ADDED);
        }

        Toolbar myToolbar = (Toolbar) findViewById(R.id.MainToolBar);
        setSupportActionBar(myToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        // Get action and MIME type
        String action = intent.getAction();
        String type = intent.getType();

        if(action != null && action.equals(NotificationUtils.NOTIFICATION_ACTION_STR)){
            handleNotificationAction();
        }
        else {
            if (Intent.ACTION_SEND.equals(action) && type != null) {
                if (type.startsWith("image/")) {
                    handleSentImage(intent); // Handle single image being sent
                }
            } else {
                currentTrip = intent.getParcelableExtra(CURRENT_TRIP_PARAM);

                if (findViewById(R.id.landmark_main_fragment_container) != null) {
                    if (getFragmentManager().findFragmentById(R.id.landmark_main_fragment_container) == null) {
                        LandmarksListFragment fragment = new LandmarksListFragment();
                        getFragmentManager()
                                .beginTransaction()
                                .add(R.id.landmark_main_fragment_container, fragment, LandmarksListFragment.TAG)
                                .commit();
                    }
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_LANDMARK, currentLandmark);
        outState.putParcelable(SAVE_TRIP, currentTrip);
        outState.putBoolean(SAVE_IS_LANDMARK_ADDED, isLandmarkAdded);
    }



    //----------add landmark from gallery------------//
    private void handleSentImage(Intent intent) {
        Uri imageUri = (Uri)intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            imageFromGalleryPath = ImageUtils.getRealPathFromURI(this, imageUri);
            if (findViewById(R.id.landmark_main_fragment_container) != null) {
                if (getFragmentManager().findFragmentById(R.id.landmark_main_fragment_container) == null)
                {
                    LandmarkDetailsFragment fragment = new LandmarkDetailsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(IMAGE_FROM_GALLERY_PATH, imageFromGalleryPath);
                    fragment.setArguments(bundle);
                    getFragmentManager()
                            .beginTransaction()
                            .add(R.id.landmark_main_fragment_container, fragment, LandmarkDetailsFragment.TAG)
                            .commit();
                }
            }
        }
    }

    //----------add landmark from gallery------------//
    private void handleNotificationAction() {
        if (findViewById(R.id.landmark_main_fragment_container) != null) {
            if (getFragmentManager().findFragmentById(R.id.landmark_main_fragment_container) == null) {
                LandmarkDetailsFragment fragment = new LandmarkDetailsFragment();
                getFragmentManager()
                        .beginTransaction()
                        .add(R.id.landmark_main_fragment_container, fragment, LandmarkDetailsFragment.TAG)
                        .commit();
            }
        }
    }

    @Override
    public void onSetCurrentLandmark(Landmark landmark) {
        currentLandmark = landmark;
    }

    @Override
    public int onGetCurrentTripId() {
        return currentTrip.getId();
    }

    @Override
    public Landmark onGetCurrentLandmark() {
        return currentLandmark;
    }

    @Override
    public String getCurrentTripTitle() {
        return currentTrip.getTitle();
    }

    @Override
    public Trip onGetCurrentTrip() {
        return currentTrip;
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
    public boolean getIsLandmarkAdded() {
        boolean res = isLandmarkAdded;
        isLandmarkAdded = false;
        return res;
    }

    @Override
    public void onLandmarkAdded() {
        isLandmarkAdded = true;
    }

    @Override
    public void onBackPressed() {
        LandmarkDetailsFragment landmarkDetailsFragment = (LandmarkDetailsFragment)getFragmentManager().findFragmentByTag(LandmarkDetailsFragment.TAG);
        TripUpdateFragment tripUpdateFragment = (TripUpdateFragment)getFragmentManager().findFragmentByTag(TripUpdateFragment.TAG);
        if (landmarkDetailsFragment != null && landmarkDetailsFragment.isVisible()) {
            ChangesNotSavedDialogFragment notSavedDialog = new ChangesNotSavedDialogFragment();
            notSavedDialog.setTargetFragment(landmarkDetailsFragment, ChangesNotSavedDialogFragment.NOT_SAVED_DIALOG);
            notSavedDialog.show(getFragmentManager(), "Not_saved_dialog");
        }
        else{
            if(tripUpdateFragment != null && tripUpdateFragment.isVisible()){
                ChangesNotSavedDialogFragment notSavedDialog = new ChangesNotSavedDialogFragment();
                notSavedDialog.setTargetFragment(tripUpdateFragment, ChangesNotSavedDialogFragment.NOT_SAVED_DIALOG);
                notSavedDialog.show(getFragmentManager(), "Not_saved_dialog");
            }
            else{
                super.onBackPressed();
            }
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