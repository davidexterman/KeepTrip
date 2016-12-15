package com.keeptrip.keeptrip.trip.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.net.Uri;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.keeptrip.keeptrip.contentProvider.KeepTripContentProvider;
import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.model.Trip;
import com.keeptrip.keeptrip.trip.activity.TripCreateActivity;

import static android.app.Activity.RESULT_OK;


//TODO: change default picture?


public class TripCreateDetailsFragment extends Fragment {

   //photo defines
    private static final int PICK_GALLERY_PHOTO_ACTION = 0;
    private static final int REQUEST_READ_STORAGE_PERMISSION_ACTION = 4;

    private View tripCreateDetailsView;
    private Activity tripCreateParentActivity;
    private ImageView tripPhotoImageView;
    private FloatingActionButton tripDoneFloatingActionButton;
    private FloatingActionButton tripReturnFloatingActionButton;
    private EditText tripPlaceEditText;
    private EditText tripDescriptionEditText;
    private String tripPhotoPath;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        tripCreateDetailsView = inflater.inflate(R.layout.fragment_trip_create_details, container, false);
        tripCreateParentActivity = getActivity();

        findViewsById();

        Trip currentTrip = ((TripCreateActivity)tripCreateParentActivity).currentCreatedTrip;
        if(currentTrip != null){
            tripPlaceEditText.setText(currentTrip.getPlace());
            tripPhotoPath = currentTrip.getPicture();
            if (tripPhotoPath != null && !tripPhotoPath.isEmpty()) {
                updatePhotoImageViewByPath(tripPhotoPath);
            }
            tripDescriptionEditText.setText(currentTrip.getDescription());
        }

        setListeners();
        return tripCreateDetailsView;
    }


    //---------------- Init views ---------------//

    // find all needed views by id's
    private void findViewsById(){
        tripDoneFloatingActionButton = (FloatingActionButton) tripCreateDetailsView.findViewById(R.id.trip_create_details_done_floating_action_button);
        tripPhotoImageView = (ImageView) tripCreateDetailsView.findViewById(R.id.trip_create_details_photo_image_view);
        tripReturnFloatingActionButton = (FloatingActionButton) tripCreateDetailsView.findViewById(R.id.trip_create_details_return_floating_action_button);
        tripPlaceEditText = (EditText) tripCreateDetailsView.findViewById(R.id.trip_create_details_place_edit_text);
        tripDescriptionEditText = (EditText) tripCreateDetailsView.findViewById(R.id.trip_create_details_description_edit_text);

    }

    // define all needed listeners
    private void setListeners(){
        // Done Button Listener
            tripDoneFloatingActionButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                Trip currentTrip = ((TripCreateActivity)tripCreateParentActivity).currentCreatedTrip;
                //todo:fix need to save this one!!!
                Trip newTrip = new Trip(currentTrip.getTitle(), currentTrip.getStartDate(), tripPlaceEditText.getText().toString(), tripPhotoPath, tripDescriptionEditText.getText().toString());

                ContentValues contentValues = newTrip.tripToContentValues();

                Uri uri = getActivity().getContentResolver().insert(KeepTripContentProvider.CONTENT_TRIPS_URI, contentValues);
                int tripId = Integer.parseInt(uri.getPathSegments().get(KeepTripContentProvider.TRIPS_ID_PATH_POSITION));

                Intent resultIntent = new Intent();
                resultIntent.putExtra(TripsListFragment.NEW_TRIP_ID, tripId);
                tripCreateParentActivity.setResult(RESULT_OK, resultIntent);
                tripCreateParentActivity.finish();

            }
        });

        // Trip Photo Listener
        tripPhotoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE_PERMISSION_ACTION );
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

        // return Button Listener
        tripReturnFloatingActionButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                onReturnButtonSelect();
            }
        });

        // trip place Listener
        tripPlaceEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                ((TripCreateActivity)tripCreateParentActivity).currentCreatedTrip.setPlace(s.toString());
            }
        });

        // trip description Listener
        tripDescriptionEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                ((TripCreateActivity)tripCreateParentActivity).currentCreatedTrip.setDescription(s.toString());
            }
        });

    }

    //---------------- Button functions ---------------//
    private void onReturnButtonSelect() {
        if (tripCreateParentActivity.findViewById(R.id.trip_create_fragment_container) != null) {
            tripCreateParentActivity.getFragmentManager().popBackStack();
        }
    }

    //-----------------Photo handle----------------//
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case PICK_GALLERY_PHOTO_ACTION:
                if (resultCode == RESULT_OK && data != null){
                    Uri imageUri = data.getData();
                    String[] filePath = {MediaStore.Images.Media.DATA};

                    Cursor cursor = tripCreateParentActivity.getContentResolver().query(imageUri, filePath, null, null, null);
                    cursor.moveToFirst();

                    tripPhotoPath = cursor.getString(cursor.getColumnIndex(filePath[0]));

                    Bitmap d = BitmapFactory.decodeFile(tripPhotoPath);
                    int nh = (int) ( d.getHeight() * (512.0 / d.getWidth()) );
                    Bitmap scaled = Bitmap.createScaledBitmap(d, 512, nh, true);
                    tripPhotoImageView.setImageBitmap(scaled);

                    cursor.close();

                    ((TripCreateActivity)tripCreateParentActivity).currentCreatedTrip.setPicture(tripPhotoPath);
                }
                break;
        }
    }


    private void updatePhotoImageViewByPath(String imagePath){
        Bitmap d = BitmapFactory.decodeFile(tripPhotoPath);
        int nh = (int) ( d.getHeight() * (512.0 / d.getWidth()) );
        Bitmap scaled = Bitmap.createScaledBitmap(d, 512, nh, true);
        tripPhotoImageView.setImageBitmap(scaled);
    }


    //-----------------Save and Restore handle-------------------//
    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
    }
}