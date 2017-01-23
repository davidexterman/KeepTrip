package com.keeptrip.keeptrip.landmark.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.contentProvider.KeepTripContentProvider;
import com.keeptrip.keeptrip.dialogs.NoTripsDialogFragment;
import com.keeptrip.keeptrip.model.Landmark;
import com.keeptrip.keeptrip.model.Trip;
import com.keeptrip.keeptrip.utils.DateUtils;
import com.keeptrip.keeptrip.utils.DbUtils;
import com.keeptrip.keeptrip.utils.ImageUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class LandmarkAddMultipleFromGalleryActivity extends Activity implements NoTripsDialogFragment.NoTripDialogClickListener{

    private static final int REQUEST_READ_STORAGE_PERMISSION_ACTION = 0;
    private static final int NO_TRIPS_DIALOG = 1;
    private static Intent multiplePhotosIntent;
    private ProgressDialog progressDialog;private AsyncTask<Void, Integer, Void> addLandmarksTask;
    private ArrayList<Uri> imageUris;
    private Trip lastTrip;
    private int currentImageIndex;
    private String saveCurrentImage = "saveCurrentImage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landmark_add_multiple_from_gallery);

        if(savedInstanceState != null) {
            currentImageIndex = savedInstanceState.getInt(saveCurrentImage);
        }
        else {
            currentImageIndex = 0;
        }
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(saveCurrentImage, currentImageIndex);

    }

    private void createAddMultipleLandmarksTask(){
        if(addLandmarksTask != null ){
            if(addLandmarksTask.isCancelled()){
                return;
            }
            if(addLandmarksTask.getStatus() != AsyncTask.Status.FINISHED){
                addLandmarksTask.cancel(true);
            }
            addLandmarksTask = null;
        }

        addLandmarksTask = new AsyncTask<Void, Integer, Void>(){

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                imageUris = multiplePhotosIntent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                if (imageUris != null) {
                    lastTrip = DbUtils.getLastTrip(LandmarkAddMultipleFromGalleryActivity.this);
                    initProgressDialog(imageUris.size());
                    progressDialog.show();
                }

            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                progressDialog.setProgress(values[0]);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                progressDialog.dismiss();
                Toast.makeText(LandmarkAddMultipleFromGalleryActivity.this, getResources().getString(R.string.toast_landmarks_added_message_success, lastTrip.getTitle()), Toast.LENGTH_SHORT).show();
                finishAffinity();
            }

            @Override
            protected Void doInBackground(Void... params) {

                imageUris = multiplePhotosIntent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                if (imageUris != null) {
                    for (; currentImageIndex < imageUris.size(); currentImageIndex++) {
                        if(addLandmarksTask == null || isCancelled()){
                            return null;
                        }
                        publishProgress(currentImageIndex);
                        String currentImagePath = ImageUtils.getRealPathFromURI(LandmarkAddMultipleFromGalleryActivity.this, imageUris.get(currentImageIndex));

                        Landmark newLandmark = new Landmark(lastTrip.getId(),
                                "", currentImagePath, DateUtils.getDateOfToday(), "", null, "", "", 0);

                        getDataFromPhotoAndUpdateLandmark(newLandmark);


                        // Insert data to DataBase
                        getContentResolver().insert(
                                KeepTripContentProvider.CONTENT_LANDMARKS_URI,
                                newLandmark.landmarkToContentValues());

                    }

                }
                return null;
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void initProgressDialog(int numberOfPhotos){
        progressDialog = new ProgressDialog(this);

        // Set progress dialog style spinner
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(numberOfPhotos);
        progressDialog.setProgress(currentImageIndex);

        // Set the progress dialog title and message
        progressDialog.setTitle(getResources().getString(R.string.toast_add_landmark_progress_dialog_title));
        progressDialog.setMessage(getResources().getString(R.string.toast_add_landmark_progress_dialog_message));

        // Set the progress dialog background color
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        progressDialog.setIndeterminate(false);

        progressDialog.setCancelable(false);
    }

    private void handleSendMultipleImages() {

        Trip currentTrip = DbUtils.getLastTrip(this);
        if(currentTrip == null){
            NoTripsDialogFragment dialogFragment = new NoTripsDialogFragment();

            Bundle args = new Bundle();
            args.putInt(dialogFragment.CALLED_FROM_WHERE_ARGUMENT, dialogFragment.CALLED_FROM_ACTIVITY);
            dialogFragment.setArguments(args);

            dialogFragment.show(getFragmentManager(), "noTrips");
        }
        else {
//            handleLandmarksFromGalleryWhenThereAreTrips();
            createAddMultipleLandmarksTask();
        }
    }

//    private void handleLandmarksFromGalleryWhenThereAreTrips() {
//        ArrayList<Uri> imageUris = multiplePhotosIntent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
//        if (imageUris != null) {
//            Trip lastTrip = DbUtils.getLastTrip(this);
//            progressDialog.show();
//            for (int i = 0; i < imageUris.size(); i++) {
//                String currentImagePath = ImageUtils.getRealPathFromURI(this, imageUris.get(i));
//
//                Landmark newLandmark = new Landmark(lastTrip.getId(),
//                        "", currentImagePath, DateUtils.getDateOfToday(), "", null, "" , "", 0);
//
//                getDataFromPhotoAndUpdateLandmark(newLandmark);
//
//                // Insert data to DataBase
//                getContentResolver().insert(
//                        KeepTripContentProvider.CONTENT_LANDMARKS_URI,
//                        newLandmark.landmarkToContentValues());
//            }
//            progressDialog.dismiss();
//            Toast.makeText(this, getResources().getString(R.string.toast_landmarks_added_message_success, lastTrip.getTitle()), Toast.LENGTH_SHORT).show();
//            finishAffinity();
//        }
//    }

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
    public void onClickHandleParent(int whichButton, String newTripTitle) {
        NoTripsDialogFragment.DialogOptions whichOptionEnum = NoTripsDialogFragment.DialogOptions.values()[whichButton];
        switch (whichOptionEnum){
            case DONE:
                Trip newTrip = new Trip(newTripTitle, Calendar.getInstance().getTime(), "", "", "");
                DbUtils.addNewTrip(this, newTrip);

//                handleLandmarksFromGalleryWhenThereAreTrips();
                createAddMultipleLandmarksTask();
                break;
            case CANCEL:
                Toast.makeText(this, getResources().getString(R.string.toast_no_trips_dialog_canceled_message), Toast.LENGTH_LONG).show();
                finishAffinity();
        }
    }

    private void getDataFromPhotoAndUpdateLandmark(Landmark landmark) {
        ExifInterface exifInterface = ImageUtils.getImageExif(landmark.getPhotoPath());
        Date imageDate = ImageUtils.getImageDateFromExif(exifInterface);
        if(imageDate != null) {
            landmark.setDate(imageDate);
        }
        Location imageLocation = ImageUtils.getImageLocationFromExif(exifInterface);
        if(imageLocation != null) {
            landmark.setGPSLocation(imageLocation);
        }
    }

    @Override
    protected void onDestroy() {
        clearTask();
        progressDialog.dismiss();
        super.onDestroy();
    }

    private void clearTask(){
        if(addLandmarksTask != null){
            if(!addLandmarksTask.isCancelled()) {
                addLandmarksTask.cancel(true);
            }
            addLandmarksTask = null;
        }
    }
}
