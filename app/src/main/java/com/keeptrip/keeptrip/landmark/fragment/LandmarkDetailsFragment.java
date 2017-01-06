package com.keeptrip.keeptrip.landmark.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.database.SQLException;
import android.media.ExifInterface;
import android.os.Environment;
import android.support.v13.app.FragmentCompat;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.support.design.widget.FloatingActionButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

import com.keeptrip.keeptrip.contentProvider.KeepTripContentProvider;
import com.keeptrip.keeptrip.dialogs.DescriptionDialogFragment;
import com.keeptrip.keeptrip.dialogs.NoTripsDialogFragment;
import com.keeptrip.keeptrip.landmark.activity.LandmarkSingleMap;
import com.keeptrip.keeptrip.landmark.interfaces.OnGetCurrentLandmark;
import com.keeptrip.keeptrip.trip.interfaces.OnGetCurrentTrip;
import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.landmark.activity.LandmarkMainActivity;
import com.keeptrip.keeptrip.model.Landmark;
import com.keeptrip.keeptrip.model.Trip;
import com.keeptrip.keeptrip.utils.DateUtils;
import com.keeptrip.keeptrip.utils.DbUtils;
import com.keeptrip.keeptrip.utils.ImageUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class LandmarkDetailsFragment extends Fragment implements
        OnConnectionFailedListener, ConnectionCallbacks {

    // tag
    public static final String TAG = LandmarkDetailsFragment.class.getSimpleName();

    // Landmark Details form on result actions
    private static final int PICK_GALLERY_PHOTO_ACTION = 0;
    private static final int TAKE_PHOTO_FROM_CAMERA_ACTION = 1;
    private static final int REQUEST_LOCATION_PERMISSION_ACTION = 2;
    private static final int REQUEST_CAMERA_PERMISSION_ACTION = 3;
    private static final int REQUEST_READ_STORAGE_PERMISSION_ACTION = 4;
    private static final int DESCRIPTION_DIALOG = 5;
    private static final int NO_TRIPS_DIALOG = 6;

    // Landmark Location Defines
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    // Landmark Photo Dialog Options
    public enum PhotoDialogOptions{
        CHANGE_PICTURE,
        TAKE_PHOTO
    }

    // Landmark Details Views
    private EditText lmTitleEditText;
    private ImageView lmPhotoImageView;
    private EditText lmDateEditText;
    private EditText lmLocationEditText;
    private ImageButton lmGpsLocationImageButton;
    private Spinner lmTypeSpinner;
    private EditText lmDescriptionEditText;
    private FloatingActionButton lmDoneButton;

    // Private parameters
    private Uri photoURI;
    private View parentView;
    private ImageView lmIconTypeSpinner;
    private boolean isCalledFromUpdateLandmark;
    private boolean isCalledFromGallery = false;
    private AlertDialog.Builder optionsDialogBuilder;
    private boolean isRequestedPermissionFromCamera;
    private OnGetCurrentLandmark mCallback;
    //    private OnGetCurrentTripId mCallbackGetCurTripId;
    private OnGetCurrentTrip mCallbackGetCurTrip;
    private OnLandmarkAddedListener mCallbackOnLandmarkAddedListener;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private String currentLmPhotoPath;
    private Date lmCurrentDate;
    private DatePickerDialog lmDatePicker;
    private SimpleDateFormat dateFormatter;

    // add landmark from gallery
    private TextView parentTripMessage;

    // Landmark Details Final Parameters
    private Landmark finalLandmark;

    //Description Dialog
    public static final String initDescription = "initDescription";

    //Save State
    private String saveFinalLandmark = "saveLandmark";
    private String saveCurrentTrip = "saveCurrentTrip";
    private String saveLmCurrentDate= "saveLmCurrentDate";
    private String saveIsCalledFromGallery = "saveIsCalledFromGallery";
    private String saveIsRequestedPermissionFromCamera = "saveIsRequestedPermissionFromCamera";
    private String savemLastLocation = "savemLastLocation";

    private Trip currentTrip;

    public interface OnLandmarkAddedListener {
        void onLandmarkAdded();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        parentView = inflater.inflate(R.layout.fragment_landmark_details, container, false);

        // get all private views by id's
        findViewsById(parentView);

        // initialize the landmark spinner
        initLmSpinner(parentView, savedInstanceState);

        // init the details fragment dialogs
        initDialogs();

        // Building the GoogleApi client
        buildGoogleApiClient();

        // set all listeners
        setListeners();

        // initialize landmark date parameters
        dateFormatter = DateUtils.getFormDateFormat();
        setDatePickerSettings();

        // initialize the create/update boolean so we can check where we were called from
        isCalledFromUpdateLandmark = false;

        parentTripMessage.setVisibility(View.GONE);

        if (savedInstanceState != null) {
            isCalledFromUpdateLandmark = savedInstanceState.getBoolean("isCalledFromUpdateLandmark");
            isRequestedPermissionFromCamera = savedInstanceState.getBoolean(saveIsRequestedPermissionFromCamera);
            isCalledFromGallery = savedInstanceState.getBoolean(saveIsCalledFromGallery);
            mLastLocation = savedInstanceState.getParcelable(savemLastLocation);
            finalLandmark = savedInstanceState.getParcelable(saveFinalLandmark);
            currentTrip = savedInstanceState.getParcelable(saveCurrentTrip);
            lmCurrentDate = new Date(savedInstanceState.getLong(saveLmCurrentDate));
            updateLmPhotoImageView(savedInstanceState.getString("savedImagePath"));

            if(isCalledFromUpdateLandmark){
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.landmark_update_landmark_toolbar_title));
            }
            else {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.landmark_create_new_landmark_toolbar_title));
                if(isCalledFromGallery){
                    updateParentTripMessage();
                }
            }

        } else {
            currentTrip = mCallbackGetCurTrip.onGetCurrentTrip();
            finalLandmark = mCallback.onGetCurrentLandmark();
            if (finalLandmark != null) {
                // We were called from Update Landmark need to update parameters
                updateLmParameters();
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.landmark_update_landmark_toolbar_title));
            }
            else{
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.landmark_create_new_landmark_toolbar_title));

                Bundle args = getArguments();
                if(args != null) {
                    currentLmPhotoPath = args.getString(LandmarkMainActivity.IMAGE_FROM_GALLERY_PATH);
                    isCalledFromGallery = true;

                    if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED){
                        FragmentCompat.requestPermissions(LandmarkDetailsFragment.this,
                                new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE_PERMISSION_ACTION );
                    }

                    else {
                        handleLandmarkFromGallery();
                    }

                }
            }
        }

        return parentView;
    }

    private void handleLandmarkFromGallery(){
        currentTrip = DbUtils.getLastTrip(getActivity());
        if(currentTrip == null){
            NoTripsDialogFragment dialogFragment = new NoTripsDialogFragment();
            dialogFragment.setTargetFragment(LandmarkDetailsFragment.this, NO_TRIPS_DIALOG);
            dialogFragment.show(getFragmentManager(), "noTrips");
        }
        else {
            handleLandmarkFromGalleryWhenThereAreTrips();
        }
    }

    private void handleLandmarkFromGalleryWhenThereAreTrips(){
        ImageUtils.updatePhotoImageViewByPath(getActivity(), currentLmPhotoPath, lmPhotoImageView);
        getDataFromPhotoAndUpdateLandmark(currentLmPhotoPath);
        updateParentTripMessage();
    }

    // find all needed views by id's
    private void findViewsById(View parentView) {
        lmTitleEditText = (EditText) parentView.findViewById(R.id.landmark_details_title_edit_text);
        lmPhotoImageView = (ImageView) parentView.findViewById(R.id.landmark_details_photo_image_view);
        lmLocationEditText = (EditText) parentView.findViewById(R.id.landmark_details_location_edit_text);
        lmGpsLocationImageButton = (ImageButton) parentView.findViewById(R.id.landmark_details_gps_location_image_button);
        lmDateEditText = (EditText) parentView.findViewById(R.id.landmark_details_date_edit_text);
        lmTypeSpinner = (Spinner) parentView.findViewById(R.id.landmark_details_type_spinner);
        lmIconTypeSpinner = (ImageView) parentView.findViewById(R.id.landmark_details_icon_type_spinner_item);
        lmDescriptionEditText = (EditText) parentView.findViewById(R.id.landmark_details_description_edit_text);
        lmDoneButton = (FloatingActionButton) parentView.findViewById(R.id.landmark_details_floating_action_button);
        parentTripMessage = (TextView) parentView.findViewById(R.id.parent_trip_message);
    }

    private void setListeners() {
        // Landmark Photo Listener
        lmPhotoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                optionsDialogBuilder.show();
            }
        });

        // Date Edit Text Listener
        lmDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lmDatePicker.show();
            }
        });

        lmGpsLocationImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!checkPlayServices()){
                    // not supporting google api at the moment
                    return;
                }
                else{
                    // Building the GoogleApi client
                    buildGoogleApiClient();
                }
                if (mGoogleApiClient != null) {
                    if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        displayLocation();
                    } else {
                        // TODO: check if prompt dialog to ask for permissions for location is working
                        checkLocationPermission();
                    }
                }
            }
        });

        lmTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                TypedArray iconType = getResources().obtainTypedArray(R.array.landmark_view_details_icon_type_array);
                if(position > 0){
                    lmIconTypeSpinner.setVisibility(View.VISIBLE);
                    lmIconTypeSpinner.setImageResource(iconType.getResourceId(position, -1));
                }
                else{
                    lmIconTypeSpinner.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Landmark Description TextView Got Clicked (Pop Up Editor)
        lmDescriptionEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //popUpDescriptionTextEditor();
                DialogFragment descriptionDialog = new DescriptionDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putString(initDescription, lmDescriptionEditText.getText().toString());
                descriptionDialog.setArguments(bundle);
                descriptionDialog.setTargetFragment(LandmarkDetailsFragment.this, DESCRIPTION_DIALOG);
                descriptionDialog.show(getFragmentManager(), "Description");
            }
        });


        // Landmark Done button Listener (Available only if title or picture was insert)
        lmDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lmTitleEditText.getText().toString().trim().isEmpty() && (currentLmPhotoPath == null || currentLmPhotoPath.isEmpty())) {
                    lmTitleEditText.requestFocus();
                    lmTitleEditText.setError(getResources().getString(R.string.landmark_no_title_or_photo_error_message));
                }
                else {
                    if (isCalledFromGallery) {
                        if(createAndInsertNewLandmark()) {
                            Toast.makeText(getActivity(), getResources().getString(R.string.toast_landmark_added_message_success), Toast.LENGTH_LONG).show();
                            //getActivity().finish();
                            getActivity().finishAffinity();
                        }
                        else {
                            Toast.makeText(getActivity(), getResources().getString(R.string.toast_landmark_added_message_fail), Toast.LENGTH_LONG).show();
                        }
                    } else if(!isCalledFromUpdateLandmark) {
                        if(!createAndInsertNewLandmark()){
                            Toast.makeText(getActivity(), getResources().getString(R.string.toast_landmark_added_message_fail), Toast.LENGTH_LONG).show();
                        }
                        else {
                            getFragmentManager().popBackStackImmediate();
                        }

                    } else {
                        // Update the final landmark
                        setLandmarkParameters(finalLandmark);

                        // Update the DataBase with the edited landmark
                        getActivity().getContentResolver().update(
                                ContentUris.withAppendedId(KeepTripContentProvider.CONTENT_LANDMARK_ID_URI_BASE, finalLandmark.getId()),
                                finalLandmark.landmarkToContentValues(),
                                null,
                                null);

                        if(DateUtils.isFirstLaterThanSecond(lmCurrentDate, currentTrip.getEndDate())){
                            //update trip end date
                            updateTripEndDate(currentTrip.getId(), lmCurrentDate);
                        }

                        getFragmentManager().popBackStackImmediate();
                    }

                }
            }
        });
    }

    private void setLandmarkParameters(Landmark landmark){
        landmark.setTitle(lmTitleEditText.getText().toString().trim());
        landmark.setPhotoPath(currentLmPhotoPath);
        landmark.setDate(lmCurrentDate);
        landmark.setLocation(lmLocationEditText.getText().toString().trim());
        landmark.setGPSLocation(mLastLocation);
        landmark.setDescription(lmDescriptionEditText.getText().toString().trim());
        landmark.setTypePosition(lmTypeSpinner.getSelectedItemPosition());
    }

    private boolean createAndInsertNewLandmark(){
        Boolean result = true;
        // Create the new final landmark
        finalLandmark = new Landmark(currentTrip.getId(), lmTitleEditText.getText().toString().trim(), currentLmPhotoPath, lmCurrentDate,
                lmLocationEditText.getText().toString().trim(), mLastLocation, lmDescriptionEditText.getText().toString().trim(),
                lmTypeSpinner.getSelectedItemPosition());

        try {
            // Insert data to DataBase
            getActivity().getContentResolver().insert(
                    KeepTripContentProvider.CONTENT_LANDMARKS_URI,
                    finalLandmark.landmarkToContentValues());

            if(DateUtils.isFirstLaterThanSecond(lmCurrentDate, currentTrip.getEndDate())){
                //update trip end date
                updateTripEndDate(currentTrip.getId(), lmCurrentDate);
            }
        }
        catch (SQLException e){
            result = false;
        }

        if (result) {
            mCallbackOnLandmarkAddedListener.onLandmarkAdded();
        }
        return result;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
     //   String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String timeStamp = DateUtils.getImageTimeStampDateFormat().format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStorageDirectory();
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file path
        currentLmPhotoPath = image.getAbsolutePath();
        return image;
    }

    // Update Landmark , need to update landmark Parameters
    private void updateLmParameters() {

        // We were called from update landmark (not create)
        isCalledFromUpdateLandmark = true;

        lmTitleEditText.setText(finalLandmark.getTitle());

        updateLmPhotoImageView(finalLandmark.getPhotoPath());

        updateLandmarkDate(finalLandmark.getDate());

        lmLocationEditText.setText(finalLandmark.getLocation());
        mLastLocation = finalLandmark.getGPSLocation();

        lmTypeSpinner.setSelection(finalLandmark.getTypePosition());

        TypedArray iconType = getResources().obtainTypedArray(R.array.landmark_view_details_icon_type_array);
        if(finalLandmark.getTypePosition() > 0){
            lmIconTypeSpinner.setVisibility(View.VISIBLE);
            lmIconTypeSpinner.setImageResource(iconType.getResourceId(finalLandmark.getTypePosition(), -1));
        }
        else{
            lmIconTypeSpinner.setVisibility(View.INVISIBLE);
        }

        lmDescriptionEditText.setText(finalLandmark.getDescription());

    }

    private void updateLmPhotoImageView(String imagePath){
        currentLmPhotoPath = imagePath;
        if (!ImageUtils.isPhotoExist(currentLmPhotoPath)) {
            // check if photo not exist in order to force to user to enter new photo.
            currentLmPhotoPath = null;
        }
        ImageUtils.updatePhotoImageViewByPath(getActivity(), currentLmPhotoPath, lmPhotoImageView);
    }

    private void initLmSpinner(View parentView, Bundle savedInstanceState) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(parentView.getContext(),
                R.array.landmark_details_type_spinner_array, R.layout.landmark_details_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lmTypeSpinner.setAdapter(adapter);
        if (savedInstanceState == null){
            lmIconTypeSpinner.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK_GALLERY_PHOTO_ACTION:
                if (resultCode == LandmarkMainActivity.RESULT_OK && data != null) {
                    Uri imageUri = data.getData();
                    String[] filePath = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getActivity().getContentResolver().query(imageUri, filePath, null, null, null);
                    cursor.moveToFirst();

                    String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
                    ImageUtils.updatePhotoImageViewByPath(getActivity(), imagePath, lmPhotoImageView);
                    getDataFromPhotoAndUpdateLandmark(imagePath);

                    lmTitleEditText.setError(null);
// TODO: check problems from finding gallery photo
                    cursor.close();

                    // save the current photo path
                    currentLmPhotoPath = imagePath;
                }
                break;
            case TAKE_PHOTO_FROM_CAMERA_ACTION:
                if (resultCode == LandmarkMainActivity.RESULT_OK) {
                    try {
                        MediaStore.Images.Media.insertImage(
                                getActivity().getContentResolver(),
                                currentLmPhotoPath,
                                "keepTrip",
                                "Photo from keepTrip");

                        ImageUtils.updatePhotoImageViewByPath(getActivity(), currentLmPhotoPath, lmPhotoImageView);
                        lmTitleEditText.setError(null);
                    } catch (Exception ex) {
                        Toast.makeText(getActivity(), "Problem adding the taken photo", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    currentLmPhotoPath = null;
                    ImageUtils.updatePhotoImageViewByPath(getActivity(), currentLmPhotoPath, lmPhotoImageView);
                    Toast.makeText(getActivity(), "Problem adding the taken photo", Toast.LENGTH_SHORT).show();
                }
                break;
            case DESCRIPTION_DIALOG:
                if (resultCode == Activity.RESULT_OK) {
                    DescriptionDialogFragment.DialogOptions whichOptionEnum = (DescriptionDialogFragment.DialogOptions) data.getSerializableExtra(DescriptionDialogFragment.DESCRIPTION_DIALOG_OPTION);
                    switch (whichOptionEnum) {
                        case DONE:
                            lmDescriptionEditText.setText(data.getStringExtra(DescriptionDialogFragment.DESCRIPTION_FROM_DIALOG));
                            break;
                        case CANCEL:
                            break;
                    }
                }
                break;
            case NO_TRIPS_DIALOG:
                if (resultCode == Activity.RESULT_OK) {
                    NoTripsDialogFragment.DialogOptions whichOptionEnum = (NoTripsDialogFragment.DialogOptions) data.getSerializableExtra(NoTripsDialogFragment.NO_TRIPS_DIALOG_OPTION);
                    switch (whichOptionEnum) {
                        case DONE:
                            String title = data.getStringExtra(NoTripsDialogFragment.TITLE_FROM_NO_TRIPS_DIALOG);
                            Trip newTrip = new Trip(title, Calendar.getInstance().getTime(), "", "", "");

                            int tripId = DbUtils.addNewTrip(getActivity(), newTrip);
                            newTrip.setId(tripId);
                            currentTrip = newTrip;

                            handleLandmarkFromGalleryWhenThereAreTrips();

//                            Toast.makeText(getActivity(), getResources().getString(R.string.toast_trip_added_message), Toast.LENGTH_LONG).show();
                            break;
                        case CANCEL:
                            Toast.makeText(getActivity(), getResources().getString(R.string.toast_no_trips_dialog_canceled_message), Toast.LENGTH_LONG).show();
                            getActivity().finish();
                    }
                }
        }
    }

    private void getDataFromPhotoAndUpdateLandmark(String imagePath) {
        try {
            ExifInterface exifInterface = new ExifInterface(imagePath);

            // update the landmark date.
            exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US);
            Date imageDate = sdf.parse(exifInterface.getAttribute(ExifInterface.TAG_DATETIME));
            updateLandmarkDate(imageDate);

            // update the landmark Longitude and Latitude.
            float[] latLong = new float[2];
            boolean hasLatLong = exifInterface.getLatLong(latLong);
            if (hasLatLong) {
                Location location = new Location("");
                location.setLongitude(latLong[0]);
                location.setLatitude(latLong[1]);
                mLastLocation = location;
            }

        } catch (Exception e) {
            // Ignore
        }
    }

    //---------------- Date functions ---------------//
    private void setDatePickerSettings() {

        Calendar newCalendar = Calendar.getInstance();
        int currentYear = newCalendar.get(Calendar.YEAR);
        int currentMonth = newCalendar.get(Calendar.MONTH);
        int currentDay = newCalendar.get(Calendar.DAY_OF_MONTH);
        int currentHour = newCalendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = newCalendar.get(Calendar.MINUTE);

        lmDatePicker = new DatePickerDialog(getActivity(), R.style.datePickerTheme, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                updateLandmarkDate(newDate.getTime());
            }
        }, currentYear, currentMonth, currentDay);

        updateLandmarkDate(newCalendar.getTime());
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (FragmentCompat.shouldShowRequestPermissionRationale(LandmarkDetailsFragment.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.location_permission_title))
                        .setMessage(getString(R.string.location_permission_message))
                        .setPositiveButton(getString(R.string.location_permission_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                FragmentCompat.requestPermissions(LandmarkDetailsFragment.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_LOCATION_PERMISSION_ACTION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                FragmentCompat.requestPermissions(LandmarkDetailsFragment.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSION_ACTION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION_ACTION: {
                if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, TAKE_PHOTO_FROM_CAMERA_ACTION);
                    }
                    else {
                        FragmentCompat.requestPermissions(LandmarkDetailsFragment.this,
                                new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE_PERMISSION_ACTION);
                    }
                }
                break;
            }

            case REQUEST_READ_STORAGE_PERMISSION_ACTION: {
                if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    if(isCalledFromGallery){
                        handleLandmarkFromGallery();
                        return;
                    }
                    else if (isRequestedPermissionFromCamera) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, TAKE_PHOTO_FROM_CAMERA_ACTION);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, PICK_GALLERY_PHOTO_ACTION);
                    }
                } else {
                    if(isCalledFromGallery){
                        Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.toast_landmark_added_from_gallery_no_permission), Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                    }
                    else {
                        Toast.makeText(getActivity().getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
            case REQUEST_LOCATION_PERMISSION_ACTION: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient != null) {
                            displayLocation();
                        }
                    }
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getActivity(), "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void initDialogs() {
        String[] dialogOptionsArray = getResources().getStringArray(R.array.landmark_details_photo_dialog_options);

        // Use the Builder class for convenient dialog construction
        optionsDialogBuilder = new AlertDialog.Builder(getActivity());
        optionsDialogBuilder.setTitle(R.string.landmark_details_photo_dialog);
        optionsDialogBuilder.setItems(dialogOptionsArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position) {
                PhotoDialogOptions photoDialogOptions = PhotoDialogOptions.values()[position];
                switch (photoDialogOptions){
                    case CHANGE_PICTURE:
                        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            isRequestedPermissionFromCamera = false;
                            FragmentCompat.requestPermissions(LandmarkDetailsFragment.this,
                                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE_PERMISSION_ACTION);
                        } else {
                            Intent takePictureIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(takePictureIntent, PICK_GALLERY_PHOTO_ACTION);
                        }
                        break;
                    case TAKE_PHOTO:
                        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            isRequestedPermissionFromCamera = true;
                            FragmentCompat.requestPermissions(LandmarkDetailsFragment.this,
                                    new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_ACTION);
                        } else {
                            if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                                    == PackageManager.PERMISSION_GRANTED) {
                                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                    // Create the File where the photo should go
                                    File photoFile = null;
                                    try {
                                        photoFile = createImageFile();
                                    } catch (IOException ex) {
                                        // Error occurred while creating the File
                                    }
                                    // Continue only if the File was successfully created
                                    if (photoFile != null) {
                                        photoURI = FileProvider.getUriForFile(getActivity(),
                                                "com.keeptrip.keeptrip.fileprovider",
                                                photoFile);
                                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                                        // grant permission to the camera to use the photoURI
                                        List<ResolveInfo> resInfoList = getActivity().getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                                        for (ResolveInfo resolveInfo : resInfoList) {
                                            String packageName = resolveInfo.activityInfo.packageName;
                                            getActivity().grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        }

                                        // open the camera
                                        startActivityForResult(takePictureIntent, TAKE_PHOTO_FROM_CAMERA_ACTION);
                                    }
                                }
                            } else {
                                FragmentCompat.requestPermissions(LandmarkDetailsFragment.this,
                                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE_PERMISSION_ACTION);
                            }
                        }
                        break;
                }
            }
        });

    }


    @Override
    public void onConnected(Bundle connectionHint) {
        // Once connected with google api, get the location
//        checkLocationPermission();
//        displayLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    @Override
    public void onStart() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * Method to display the location on UI
     * */
    private void displayLocation() {

        try{
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }catch (SecurityException e){
            e.printStackTrace();
        }

        if (mLastLocation != null) {
            String latitudeStr = String.valueOf(mLastLocation.getLatitude());
            String longitudeStr = String.valueOf(mLastLocation.getLongitude());

            String strLocationToast = String.format(getActivity().getString(R.string.toast_landmark_location_sample),
                    latitudeStr,
                    longitudeStr);

            Toast.makeText(
                    getActivity().getApplicationContext(),
                    strLocationToast,
                    Toast.LENGTH_SHORT)
                    .show();

            Intent mapIntent = new Intent(getActivity(), LandmarkSingleMap.class);
            Bundle gpsLocationBundle = new Bundle();
            Landmark newLandmark = new Landmark(currentTrip.getId(), lmTitleEditText.getText().toString().trim(), currentLmPhotoPath, lmCurrentDate,
                    lmLocationEditText.getText().toString().trim(), mLastLocation, lmDescriptionEditText.getText().toString().trim(),
                    lmTypeSpinner.getSelectedItemPosition());

            setLandmarkParameters(newLandmark);
            ArrayList<Landmark> landmarkArray = new ArrayList(1);
            landmarkArray.add(newLandmark);
            gpsLocationBundle.putParcelableArrayList(LandmarkMainActivity.LandmarkArrayList, landmarkArray);
            mapIntent.putExtras(gpsLocationBundle);
            startActivity(mapIntent);
        }
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(getActivity());
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(getActivity(), result,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }

            return false;
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putString("savedImagePath", currentLmPhotoPath);
        state.putBoolean("isCalledFromUpdateLandmark", isCalledFromUpdateLandmark);
        state.putBoolean(saveIsRequestedPermissionFromCamera, isRequestedPermissionFromCamera);
        state.putBoolean(saveIsCalledFromGallery, isCalledFromGallery);
        state.putParcelable(saveFinalLandmark, finalLandmark);
        state.putParcelable(savemLastLocation, mLastLocation);
        state.putParcelable(saveCurrentTrip, currentTrip);
        state.putLong(saveLmCurrentDate, lmCurrentDate.getTime());
        //state.putBoolean("isEditLandmarkPressed", isEditLandmarkPressed);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnGetCurrentLandmark) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement GetCurrentLandmark");
        }
        try {
            mCallbackGetCurTrip = (OnGetCurrentTrip) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnGetCurrentTrip");
        }
        try {
            mCallbackOnLandmarkAddedListener = (OnLandmarkAddedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnLandmarkAddedListener");
        }
    }

    //--------------helper methods--------//
    private void updateLandmarkDate(Date newDate) {
        lmCurrentDate = newDate;
        lmDateEditText.setText(dateFormatter.format(lmCurrentDate));
    }

    private void updateTripEndDate(int tripId, Date newEndDate){
        ContentValues contentValues = new ContentValues();
        contentValues.put(KeepTripContentProvider.Trips.END_DATE_COLUMN, DateUtils.databaseDateToString(newEndDate));
        getActivity().getContentResolver().update
                (ContentUris.withAppendedId(KeepTripContentProvider.CONTENT_TRIP_ID_URI_BASE, tripId), contentValues, null, null);
        currentTrip.setEndDate(newEndDate);
    }

    private void updateParentTripMessage(){
        String message = getResources().getString(R.string.parent_trip_message) + " " + "<b>" + currentTrip.getTitle() + "</b>" + " trip";
        parentTripMessage.setText(Html.fromHtml(message));
        parentTripMessage.setVisibility(View.VISIBLE);
    }
}
