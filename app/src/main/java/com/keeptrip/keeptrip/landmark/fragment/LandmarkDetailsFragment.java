package com.keeptrip.keeptrip.landmark.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.media.ExifInterface;
import android.os.Environment;
import android.support.v13.app.FragmentCompat;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.keeptrip.keeptrip.contentProvider.KeepTripContentProvider;
import com.keeptrip.keeptrip.landmark.interfaces.OnGetCurrentLandmark;
import com.keeptrip.keeptrip.landmark.interfaces.OnGetCurrentTripId;
import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.landmark.activity.LandmarkMainActivity;
import com.keeptrip.keeptrip.model.Landmark;
import com.keeptrip.keeptrip.utils.DateFormatUtils;
import com.keeptrip.keeptrip.utils.ImageUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class LandmarkDetailsFragment extends Fragment implements
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    // Landmark Details form on result actions
    private static final int PICK_GALLERY_PHOTO_ACTION = 0;
    private static final int TAKE_PHOTO_FROM_CAMERA_ACTION = 1;
    private static final int REQUEST_LOCATION_PERMISSION_ACTION = 2;
    private static final int REQUEST_CAMERA_PERMISSION_ACTION = 3;
    private static final int REQUEST_READ_STORAGE_PERMISSION_ACTION = 4;

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
    private AlertDialog.Builder optionsDialogBuilder;
    private boolean isRequestedPermissionFromCamera;
    private OnGetCurrentLandmark mCallback;
    private OnGetCurrentTripId mCallbackGetCurTripId;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private boolean isTitleOrPictureInserted;
    private String currentLmPhotoPath;
    private Date lmCurrentDate;
    private DatePickerDialog lmDatePicker;
    private SimpleDateFormat dateFormatter;

    // Landmark Details Final Parameters
    private Landmark finalLandmark;


    //Save State
    private String saveFinalLandmark = "saveLandmark";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        parentView = inflater.inflate(R.layout.fragment_landmark_details, container, false);

        // get all private views by id's
        findViewsById(parentView);

        // initialize the landmark spinner
        initLmSpinner(parentView);

        // initialize GPS data
        initLmGPSData();

        // init the details fragment dialogs
        initDialogs();

        // set all listeners
        setListeners();

        // initialize landmark date parameters
        //dateFormatter = new SimpleDateFormat("E, MMM dd, yyyy", Locale.US);
        dateFormatter = DateFormatUtils.getFormDateFormat();
        setDatePickerSettings();

        // initialize done button as false at start
        //lmDoneButton.setEnabled(false);

        // initialize the create/update boolean so we can check where we were called from
        isCalledFromUpdateLandmark = false;
        //isEditLandmarkPressed = false;

        if (savedInstanceState != null) {
            isCalledFromUpdateLandmark = savedInstanceState.getBoolean("isCalledFromUpdateLandmark");
            //isEditLandmarkPressed = savedInstanceState.getBoolean("isEditLandmarkPressed");
            finalLandmark = savedInstanceState.getParcelable(saveFinalLandmark);
            currentLmPhotoPath = savedInstanceState.getString("savedImagePath");
            if (currentLmPhotoPath != null) {
                ImageUtils.updatePhotoImageViewByPath(getActivity(), currentLmPhotoPath, lmPhotoImageView);

                // enable the "done" button because picture was selected
                isTitleOrPictureInserted = true;
                lmDoneButton.setEnabled(true);
            }
        } else {
            finalLandmark = mCallback.onGetCurrentLandmark();
            if (finalLandmark != null) {
                // We were called from Update Landmark need to update parameters
                updateLmParameters();
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.landmark_update_landmark_toolbar_title));
            }
            else{
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.landmark_create_new_landmark_toolbar_title));
            }
        }

//        if(isCalledFromUpdateLandmark && !isEditLandmarkPressed && (getArguments() == null || !getArguments().getBoolean("isFromDialog"))){
//            disableEnableControls(false, (ViewGroup) parentView);
//            setHasOptionsMenu(true);
//        }

        return parentView;
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
    }

    private void setListeners() {

        // Landmark Title EditText Listener
        lmTitleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String currentTitle = lmTitleEditText.getText().toString();
                if (currentTitle.trim().isEmpty() && !isTitleOrPictureInserted) {
                    lmDoneButton.setEnabled(false);
                } else {
                    lmDoneButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Landmark Photo Listener
        //TODO: take
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
                if (mGoogleApiClient != null) {
                    if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        handleGpsLocationButton();
                    } else {
                        // TODO: check if prompt dialog to ask for permissions for location is working
                        checkLocationPermission();
                    }
                }
            }
        });

        lmTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                TypedArray iconType = getResources().obtainTypedArray(R.array.landmark_view_details_icon_type_array);
                lmIconTypeSpinner.setImageResource(iconType.getResourceId(i, -1));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Landmark Description TextView Got Clicked (Pop Up Editor)
        lmDescriptionEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popUpDescriptionTextEditor();
            }
        });

//        // Landmark Description TextView Got Focus (Pop Up Editor)
//        lmDescriptionEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean hasFocus) {
//                if (!hasFocus) {
//                    return;
//                }
//                popUpDescriptionTextEditor();
//            }
//        });

        // Landmark Done button Listener (Available only if title or picture was insert)
        lmDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lmTitleEditText.getText().toString().trim().isEmpty() && (currentLmPhotoPath == null || currentLmPhotoPath.isEmpty())) {
                    lmTitleEditText.requestFocus();
                    lmTitleEditText.setError(getResources().getString(R.string.landmark_no_title_or_photo_error_message));
                }
                else {
                    int tripId = mCallbackGetCurTripId.onGetCurrentTripId();
                    if (!isCalledFromUpdateLandmark) {
                        // Create the new final landmark
                        finalLandmark = new Landmark(tripId, lmTitleEditText.getText().toString(), currentLmPhotoPath, lmCurrentDate,
                                lmLocationEditText.getText().toString(), mLastLocation, lmDescriptionEditText.getText().toString(),
                                lmTypeSpinner.getSelectedItemPosition());

                        // Insert data to DataBase
                        getActivity().getContentResolver().insert(
                                KeepTripContentProvider.CONTENT_LANDMARKS_URI,
                                finalLandmark.landmarkToContentValues());
                    } else {
                        // Update the final landmark
                        finalLandmark.setTitle(lmTitleEditText.getText().toString());
                        finalLandmark.setPhotoPath(currentLmPhotoPath);
                        finalLandmark.setDate(lmCurrentDate);
                        finalLandmark.setLocation(lmLocationEditText.getText().toString());
                        finalLandmark.setGPSLocation(mLastLocation);
                        finalLandmark.setDescription(lmDescriptionEditText.getText().toString());
                        finalLandmark.setTypePosition(lmTypeSpinner.getSelectedItemPosition());

                        // Update the DataBase with the edited landmark
                        getActivity().getContentResolver().update(
                                ContentUris.withAppendedId(KeepTripContentProvider.CONTENT_LANDMARK_ID_URI_BASE, finalLandmark.getId()),
                                finalLandmark.landmarkToContentValues(),
                                null,
                                null);
                    }
                    getFragmentManager().popBackStackImmediate();
                }
            }
        });
    }

    private File createImageFile() throws IOException {
        // Create an image file name
     //   String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String timeStamp = DateFormatUtils.getImageTimeStampDateFormat().format(new Date());
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

    private void handleGpsLocationButton(){
        try{
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }catch (SecurityException e){
            e.printStackTrace();
        }

        if (mLastLocation != null) {
            String latitudeStr = String.valueOf(mLastLocation.getLatitude());
            String longitudeStr = String.valueOf(mLastLocation.getLongitude());
            Toast.makeText(
                    getActivity().getApplicationContext(),
                    "Latitude: " + latitudeStr + " longitudeStr: " + longitudeStr,
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

//    private void galleryAddPic(String path) {
//        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//        File f = new File(path);
//        Uri contentUri = Uri.fromFile(f);
//        mediaScanIntent.setData(contentUri);
//        getActivity().sendBroadcast(mediaScanIntent);
//    }

    private void popUpDescriptionTextEditor(){
        final View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.landmark_description_dialog, null);
        final EditText dialogEditText = (EditText) dialogView.findViewById(R.id.landmark_details_dialog_description_edit_text);
        dialogEditText.setText(lmDescriptionEditText.getText().toString());
        dialogEditText.setSelection(dialogEditText.getText().length());
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.landmark_details_description_dialog_title)
                .setView(dialogView)
                .setPositiveButton(R.string.landmark_details_description_dialog_done, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String text = dialogEditText.getText().toString();
                        lmDescriptionEditText.setText(text);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
    }

    // Update Landmark , need to update landmark Parameters
    private void updateLmParameters() {

        // We were called from update landmark (not create)
        isCalledFromUpdateLandmark = true;

        lmTitleEditText.setText(finalLandmark.getTitle());

        currentLmPhotoPath = finalLandmark.getPhotoPath();
        ImageUtils.updatePhotoImageViewByPath(getActivity(), currentLmPhotoPath, lmPhotoImageView);

        lmDateEditText.setText(dateFormatter.format(finalLandmark.getDate()));
        lmCurrentDate = finalLandmark.getDate();

        lmLocationEditText.setText(finalLandmark.getLocation());
        mLastLocation = finalLandmark.getGPSLocation();

        lmTypeSpinner.setSelection(finalLandmark.getTypePosition());

        TypedArray iconType = getResources().obtainTypedArray(R.array.landmark_view_details_icon_type_array);
        lmIconTypeSpinner.setImageResource(iconType.getResourceId(finalLandmark.getTypePosition(), -1));

        lmDescriptionEditText.setText(finalLandmark.getDescription());

        // we have a title or a picture when we are updating so can enable
        lmDoneButton.setEnabled(true);
    }

    private void initLmSpinner(View parentView) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(parentView.getContext(),
                R.array.landmark_details_type_spinner_array, R.layout.landmark_details_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lmTypeSpinner.setAdapter(adapter);
    }

    private void initLmGPSData() {
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity().getApplicationContext())
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

// TODO: check problems from finding gallery photo
                    cursor.close();

                    // enable the "done" button because picture was selected
                    isTitleOrPictureInserted = true;
                    lmDoneButton.setEnabled(true);

                    // save the current photo path
                    currentLmPhotoPath = imagePath;
                }
                break;
            case TAKE_PHOTO_FROM_CAMERA_ACTION:
                if (resultCode == LandmarkMainActivity.RESULT_OK) {
                    Bitmap imageBitmap = BitmapFactory.decodeFile(currentLmPhotoPath);
//                    currentLmPhotoPath = photoURI.getPath();

                    lmPhotoImageView.setImageBitmap(imageBitmap);

//                    galleryAddPic(currentLmPhotoPath);

                    MediaStore.Images.Media.insertImage(
                            getActivity().getContentResolver(),
                            imageBitmap,
                            "test title" ,
                            "test description");
                    isTitleOrPictureInserted = true;
                    lmDoneButton.setEnabled(true);
                }
                else{
                    Toast.makeText(getActivity(), "Problem adding the taken photo", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void getDataFromPhotoAndUpdateLandmark(String imagePath) {
        try {
            ExifInterface exifInterface = new ExifInterface(imagePath);

            // update the landmark date.
            exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US);
            lmCurrentDate = sdf.parse(exifInterface.getAttribute(ExifInterface.TAG_DATETIME));
            lmDateEditText.setText(dateFormatter.format(lmCurrentDate));

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

        lmDatePicker = new DatePickerDialog(getActivity(), R.style.datePickerTheme, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                lmDateEditText.setText(dateFormatter.format(newDate.getTime()));
                lmCurrentDate = newDate.getTime();
            }
        }, currentYear, currentMonth, currentDay);

        lmDateEditText.setText(dateFormatter.format(newCalendar.getTime()));
        lmCurrentDate = newCalendar.getTime();
    }

    @Override
    public void onConnected(Bundle connectionHint) {

        // TODO: maybe use requestLocationUpdates to check if we received a new location
//        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//
//            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//        } else {
//            // TODO: check if prompt dialog to ask for permissions for location is working
//            checkLocationPermission();
//        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
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
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putString("savedImagePath", currentLmPhotoPath);
        state.putBoolean("isCalledFromUpdateLandmark", isCalledFromUpdateLandmark);
        state.putParcelable(saveFinalLandmark, finalLandmark);
        //state.putBoolean("isEditLandmarkPressed", isEditLandmarkPressed);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (FragmentCompat.shouldShowRequestPermissionRationale(LandmarkDetailsFragment.this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getActivity())
                        .setTitle("Location Permission Needed")
                        .setMessage("KeepTrip needs Location permission to get location from map, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                FragmentCompat.requestPermissions(LandmarkDetailsFragment.this,
                                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                        REQUEST_LOCATION_PERMISSION_ACTION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                FragmentCompat.requestPermissions(LandmarkDetailsFragment.this,
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_LOCATION_PERMISSION_ACTION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION_ACTION: {
                if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, TAKE_PHOTO_FROM_CAMERA_ACTION);
                } else {
                    FragmentCompat.requestPermissions(LandmarkDetailsFragment.this,
                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE_PERMISSION_ACTION);
                }
            }
            //TODO: take
            case REQUEST_READ_STORAGE_PERMISSION_ACTION: {
                if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    if (isRequestedPermissionFromCamera) {           //TODO: not take
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, TAKE_PHOTO_FROM_CAMERA_ACTION);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, PICK_GALLERY_PHOTO_ACTION);
                    }
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
            case REQUEST_LOCATION_PERMISSION_ACTION: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            android.Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient != null) {
                            handleGpsLocationButton();
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

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//
//        // This makes sure that the container activity has implemented
//        // the callback interface. If not, it throws an exception
//        try {
//            mCallback = (GetCurrentLandmark) context;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(context.toString()
//                    + " must implement GetCurrentLandmark");
//        }
//        try {
//            mCallbackGetCurTripId = (OnGetCurrentTripId) context;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(context.toString()
//                    + " must implement OnGetCurTrip");
//        }
//    }

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
                            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                            // grant permission to the camera to use the photoURI
                            List<ResolveInfo> resInfoList = getActivity().getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                            for (ResolveInfo resolveInfo : resInfoList) {
                                String packageName = resolveInfo.activityInfo.packageName;
                                getActivity().grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            }

                            // open the camera
                            startActivityForResult(intent, PICK_GALLERY_PHOTO_ACTION);
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
            mCallbackGetCurTripId = (OnGetCurrentTripId) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnGetCurrentTripId");
        }
    }
}
