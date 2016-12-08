package com.keeptrip.keeptrip;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.content.Context;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class LandmarkDetailsFragment extends Fragment implements
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {


    // Landmark Details form on result actions
    private static final int PICK_GALLERY_PHOTO_ACTION = 0;
    private static final int TAKE_PHOHO_FROM_CAMERA_ACTION = 1;
    private static final int REQUEST_LOCATION_PERMISSON_ACTION = 2;
    private static final int REQUEST_CAMERA_PERMISSON_ACTION = 3;
    private static final int REQUEST_READ_STORAGE_PERMISSON_ACTION = 4;


    // Landmark Details Views
    private EditText lmTitleEditText;
    private ImageView lmPhotoImageView;
    private ImageButton lmCameraImageButton;
    private EditText lmDateEditText;
    private EditText lmLocationEditText;
    private Spinner lmTypeSpinner;
    private EditText lmDescriptionEditText;
    private FloatingActionButton lmDoneButton;

    // Private parameters
    private GetCurrentLandmark mCallback;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private boolean isTitleOrPictureInserted;
    private String currentLmPhotoPath;
    private Date lmCurrentDate;
    private DatePickerDialog lmDatePicker;
    private SimpleDateFormat dateFormatter;

    // Landmark Details Final Parameters
    private Landmark finalLandmark;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View parentView = inflater.inflate(R.layout.fragment_landmark_details, container, false);

        // get all private views by id's
        findViewsById(parentView);

        // initialize the landmark spinner
        initLmSpinner(parentView);

        // initialize GPS data
        initLmGPSData();

        // set all listeners
        setListeners();

        // initialize landmark date parameters
        dateFormatter = new SimpleDateFormat("E, MMM dd, yyyy", Locale.US);
        setDatePickerSettings();

        // initialize done button as false at start
        lmDoneButton.setEnabled(false);

        if (savedInstanceState != null){
            currentLmPhotoPath = savedInstanceState.getString("savedImagePath");
            if (currentLmPhotoPath != null){
                updatePhotoImageViewByPath(currentLmPhotoPath);

                // enable the "done" button because picture was selected
                isTitleOrPictureInserted = true;
                lmDoneButton.setEnabled(true);
            }
        }
        else{
            finalLandmark = mCallback.getCurrentLandmark();
            if(finalLandmark != null){
                // Update Final Landmark Parameters
                lmTitleEditText.setText(finalLandmark.getTitle());

                //make sure the picture wasn't deleted and the path really exists
                try {
                    if (finalLandmark.getPhotoPath() != null) {
                        lmPhotoImageView.setImageBitmap(BitmapFactory.decodeFile(finalLandmark.getPhotoPath()));
                        isTitleOrPictureInserted = true;
                    }
                    currentLmPhotoPath = finalLandmark.getPhotoPath();
                }
                    catch(Exception e){
                    Toast.makeText(getActivity().getApplicationContext(), "Photo Wasn't found", Toast.LENGTH_SHORT).show();
                }
                lmDateEditText.setText(dateFormatter.format(finalLandmark.getDate()));
                lmCurrentDate = finalLandmark.getDate();

                lmLocationEditText.setText(finalLandmark.getLocation());
                mLastLocation.set(finalLandmark.getGPSLocation());
                lmTypeSpinner.setSelection(finalLandmark.getTypePosition());
                lmDescriptionEditText.setText(finalLandmark.getDescription());

                // we have a title or a picture when we are updating so can enable
                lmDoneButton.setEnabled(true);
            }
        }

        return parentView;
    }


    // find all needed views by id's
    private void findViewsById(View parentView) {
        lmTitleEditText = (EditText) parentView.findViewById(R.id.landmark_details_title_edit_text);
        lmPhotoImageView = (ImageView) parentView.findViewById(R.id.landmark_details_photo_image_view);
        lmLocationEditText = (EditText) parentView.findViewById(R.id.landmark_details_location_edit_text);
        lmDateEditText = (EditText) parentView.findViewById(R.id.landmark_details_date_edit_text);
        lmTypeSpinner = (Spinner) parentView.findViewById(R.id.landmark_details_type_spinner);
        lmDescriptionEditText = (EditText) parentView.findViewById(R.id.landmark_details_description_edit_text);
        lmCameraImageButton = (ImageButton) parentView.findViewById(R.id.landmark_details_camera_image_button);
        lmDoneButton = (FloatingActionButton) parentView.findViewById(R.id.landmark_details_floating_action_button);
    }

    private void setListeners(){

        // Landmark Title EditText Listener
        lmTitleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String currentTitle = lmTitleEditText.getText().toString();
                if (currentTitle.trim().isEmpty() && !isTitleOrPictureInserted){
                    lmDoneButton.setEnabled(false);
                }
                else{
                    lmDoneButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Landmark Photo Listener
        lmPhotoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE_PERMISSON_ACTION );
                }

                if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, PICK_GALLERY_PHOTO_ACTION);
                }
                else{
                    Toast.makeText(getActivity().getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Date Edit Text Listener
        lmDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lmDatePicker.show();
            }
        });

        // Landmark Camera ImageButton Listener
        lmCameraImageButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //if (takePictureIntent.resolveActivity(getActivity().getApplicationContext().getPackageManager()) != null) {
                if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSON_ACTION );
                }
                if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE_PERMISSON_ACTION );
                }

                if((ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                    && (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)){
                    startActivityForResult(takePictureIntent, TAKE_PHOHO_FROM_CAMERA_ACTION);
                }
                else {
                    Toast.makeText(getActivity().getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Landmark Done button Listener (Available only if title or picture was insert)
        lmDoneButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                // Create the new final landmark
                finalLandmark = new Landmark(lmTitleEditText.getText().toString(), currentLmPhotoPath, lmCurrentDate,
                        lmLocationEditText.getText().toString(), mLastLocation, lmDescriptionEditText.getText().toString(),
                        lmTypeSpinner.getSelectedItemPosition());

                Toast.makeText(getActivity().getApplicationContext(), "Created a Landmark!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initLmSpinner(View parentView){
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(parentView.getContext(),
                R.array.landmark_details_type_spinner_array, R.layout.landmark_details_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lmTypeSpinner.setAdapter(adapter);
    }

    private void initLmGPSData(){
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
        switch (requestCode){
            case PICK_GALLERY_PHOTO_ACTION:
                if (resultCode == LandmarkMainActivity.RESULT_OK && data != null){
                    Uri imageUri = data.getData();
                    String[] filePath = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getActivity().getContentResolver().query(imageUri, filePath, null, null, null);
                    cursor.moveToFirst();

                    String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

                    updatePhotoImageViewByPath(imagePath);
// TODO: check problems from finding gallery photo
                    cursor.close();

                    // enable the "done" button because picture was selected
                    isTitleOrPictureInserted = true;
                    lmDoneButton.setEnabled(true);

                    // save the current photo path
                    currentLmPhotoPath = imagePath;
                }
                break;
            case TAKE_PHOHO_FROM_CAMERA_ACTION:
                if (resultCode == LandmarkMainActivity.RESULT_OK && data != null) {
                    Bundle extras = data.getExtras();

                    String[] projection = { MediaStore.Images.Media.DATA };
                    Cursor cursor = getActivity().getContentResolver().query(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
                    int column_index_data = cursor
                            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToLast();

                    // save the current photo path
                    // TODO: check rotate picture
                    currentLmPhotoPath = cursor.getString(column_index_data);

                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    lmPhotoImageView.setImageBitmap(imageBitmap);

                    isTitleOrPictureInserted = true;
                    lmDoneButton.setEnabled(true);
                }

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
        },currentYear, currentMonth, currentDay);

        lmDateEditText.setText(dateFormatter.format(newCalendar.getTime()));
        lmCurrentDate = newCalendar.getTime();
    }

    @Override
    public void onConnected(Bundle connectionHint) {

        // TODO: maybe use requestLocationUpdates to check if we received a new location
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED ) {

            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                Toast.makeText(getActivity().getApplicationContext(), String.valueOf(mLastLocation.getLatitude()), Toast.LENGTH_SHORT).show();
            }
        }
        else {
                // TODO: check if prompt dialog to ask for permissions for location is working
                checkLocationPermission();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    @Override
    public void onStart() {
        if (mGoogleApiClient != null){
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
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getActivity().getApplicationContext())
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                        REQUEST_LOCATION_PERMISSON_ACTION );
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_LOCATION_PERMISSON_ACTION );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSON_ACTION: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            android.Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                            if (mLastLocation != null) {
                                Toast.makeText(getActivity().getApplicationContext(), String.valueOf(mLastLocation.getLatitude()), Toast.LENGTH_SHORT).show();
                            }
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

    private void updatePhotoImageViewByPath(String imagePath){
        Bitmap d = BitmapFactory.decodeFile(imagePath);
        int nh = (int) ( d.getHeight() * (512.0 / d.getWidth()) );
        Bitmap scaled = Bitmap.createScaledBitmap(d, 512, nh, true);
        lmPhotoImageView.setImageBitmap(scaled);
    }

    public interface GetCurrentLandmark{
        Landmark getCurrentLandmark();
    }

    @Override
	public void onAttach(Context context) {
		super.onAttach(context);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (GetCurrentLandmark) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
					+ " must implement GetCurrentLandmark");
			}
		}
}
