package com.keeptrip.keeptrip.landmark.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.contentProvider.KeepTripContentProvider;
import com.keeptrip.keeptrip.dialogs.NoTripsDialogFragment;
import com.keeptrip.keeptrip.landmark.fragment.LandmarkDetailsFragment;
import com.keeptrip.keeptrip.model.Landmark;
import com.keeptrip.keeptrip.model.Trip;
import com.keeptrip.keeptrip.utils.DateUtils;
import com.keeptrip.keeptrip.utils.DbUtils;
import com.keeptrip.keeptrip.utils.ImageUtils;

import java.util.ArrayList;
import java.util.Calendar;

public class LandmarkAddMultipleFromGalleryActivity extends Activity {

    private static final int REQUEST_READ_STORAGE_PERMISSION_ACTION = 0;
    private static final int NO_TRIPS_DIALOG = 1;
    private static Intent multiplePhotosIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landmark_add_multiple_from_gallery);

        multiplePhotosIntent = getIntent();
        // Get action and MIME type
        String action = multiplePhotosIntent.getAction();
        String type = multiplePhotosIntent.getType();

        //add multiple landmarks from gallery
        if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE_PERMISSION_ACTION);
                } else {
                    handleSendMultipleImages(); // Handle multiple images being sent
                }
            }
        }
    }

    private void handleSendMultipleImages() {

        Trip currentTrip = DbUtils.getLastTrip(this);
        if(currentTrip == null){
            NoTripsDialogFragment dialogFragment = new NoTripsDialogFragment();
//            dialogFragment.setTargetFragment(LandmarkDetailsFragment.this, NO_TRIPS_DIALOG);
            dialogFragment.show(getFragmentManager(), "noTrips");
        }
        else {
            handleLandmarksFromGalleryWhenThereAreTrips();
        }
    }

    private void handleLandmarksFromGalleryWhenThereAreTrips() {
        ArrayList<Uri> imageUris = multiplePhotosIntent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            for (int i = 0; i < imageUris.size(); i++) {
                String currentImagePath = ImageUtils.getRealPathFromURI(this, imageUris.get(i));

                Landmark newLandmark = new Landmark(DbUtils.getLastTrip(this).getId(),
                        "", currentImagePath, DateUtils.getDateOfToday(), "", new Location(""), "", 0);

                // Insert data to DataBase
                getContentResolver().insert(
                        KeepTripContentProvider.CONTENT_LANDMARKS_URI,
                        newLandmark.landmarkToContentValues());
            }
            Toast.makeText(this, getResources().getString(R.string.toast_landmarks_added_message_success), Toast.LENGTH_SHORT).show();
            finishAffinity();
        }
    }

    @Override

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_READ_STORAGE_PERMISSION_ACTION: {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    handleSendMultipleImages();
                } else {
                    Toast.makeText(this, getResources().getString(R.string.toast_landmark_added_from_gallery_no_permission), Toast.LENGTH_SHORT).show();
                    finishAffinity();
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case NO_TRIPS_DIALOG:
                if (resultCode == Activity.RESULT_OK) {
                    NoTripsDialogFragment.DialogOptions whichOptionEnum = (NoTripsDialogFragment.DialogOptions) data.getSerializableExtra(NoTripsDialogFragment.NO_TRIPS_DIALOG_OPTION);
                    switch (whichOptionEnum) {
                        case DONE:
                            String title = data.getStringExtra(NoTripsDialogFragment.TITLE_FROM_NO_TRIPS_DIALOG);
                            Trip newTrip = new Trip(title, Calendar.getInstance().getTime(), "", "", "");

                            int tripId = DbUtils.addNewTrip(this, newTrip);
                            newTrip.setId(tripId);

                            handleLandmarksFromGalleryWhenThereAreTrips();

                            break;
                        case CANCEL:
                            Toast.makeText(this, getResources().getString(R.string.toast_no_trips_dialog_canceled_message), Toast.LENGTH_LONG).show();
                            finishAffinity();
                    }
                }
        }
    }
}
