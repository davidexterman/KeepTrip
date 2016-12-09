package com.keeptrip.keeptrip;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.net.Uri;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Date;

import static android.app.Activity.RESULT_OK;


//TODO: change default picture?


public class TripCreateDetailsFragment extends Fragment {

   //photo defines
    private static final int PICK_GALLERY_PHOTO_ACTION = 0;
    private static final int REQUEST_READ_STORAGE_PERMISSION_ACTION = 4;

    private View tripCreateDetailsView;
    private Activity tripCreateParentActivity;
    private ImageView tripPhotoImageView;
    private FloatingActionButton doneFloatingActionButton;
    private FloatingActionButton returnFloatingActionButton;
    private EditText tripPlace;
    private EditText tripDescription;
    private String tripPhotoPath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        tripCreateDetailsView = inflater.inflate(R.layout.fragment_trip_create_details, container, false);
        tripCreateParentActivity = getActivity();
        ((TripCreateActivity)tripCreateParentActivity).tripDetailsFragment = (TripCreateDetailsFragment) getFragmentManager().findFragmentById(R.id.trip_create_fragment_container);

        findViewsById();

        if (savedInstanceState != null){
            tripPhotoPath = savedInstanceState.getString("savedImagePath");
            if (tripPhotoPath != null) {
                updatePhotoImageViewByPath(tripPhotoPath);
            }
        }

        setListeners();
        return tripCreateDetailsView;
    }


    //---------------- Init views ---------------//

    // find all needed views by id's
    private void findViewsById(){
        doneFloatingActionButton = (FloatingActionButton) tripCreateDetailsView.findViewById(R.id.trip_create_details_done_floating_action_button);
        tripPhotoImageView = (ImageView) tripCreateDetailsView.findViewById(R.id.trip_create_details_photo_image_view);
        returnFloatingActionButton = (FloatingActionButton) tripCreateDetailsView.findViewById(R.id.trip_create_details_return_floating_action_button);
        tripPlace = (EditText) tripCreateDetailsView.findViewById(R.id.trip_create_details_place_edit_text);
        tripDescription = (EditText) tripCreateDetailsView.findViewById(R.id.trip_create_details_description_edit_text);

    }

    // define all needed listeners
    private void setListeners(){
        // Done Button Listener
     //   doneButton.setOnClickListener(new View.OnClickListener(){
            doneFloatingActionButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //TODO: save all the details to database
                String tripTitle = ((TripCreateActivity)tripCreateParentActivity).tripTitle;
                Date tripStartDate = ((TripCreateActivity)tripCreateParentActivity).tripStartDate;

                Trip newTrip = new Trip(tripTitle, tripStartDate, tripPlace.getText().toString(), tripPhotoPath, tripDescription.getText().toString());

                //TODO: how to call this method
                newTrip = SingletonAppDataProvider.getInstance().addNewTrip(newTrip);
                //Toast.makeText(tripCreateParentActivity,"Trip \"" + tripTitle + "\" was created successfully",Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(getActivity(), LandmarkMainActivity.class);
//                startActivity(intent);

                Intent resultIntent = new Intent();
                resultIntent.putExtra(TripsListFragment.NEW_TRIP, newTrip);
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
        returnFloatingActionButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                onReturnButtonSelect();
            }
        });
    }

    //---------------- Button functions ---------------//
    private void onReturnButtonSelect() {
        //TODO: save already written data?
        //TODO: return to the current fragment without deleting the fields (like the back button)
        if (tripCreateParentActivity.findViewById(R.id.trip_create_fragment_container) != null) {
            TripCreateTitleFragment titleFragment = ((TripCreateActivity)tripCreateParentActivity).tripTitleFragment;
            if(titleFragment == null) {
                titleFragment = new TripCreateTitleFragment();
            }
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            tripCreateParentActivity.getFragmentManager().popBackStack();
            transaction.replace(R.id.trip_create_fragment_container, titleFragment);
            transaction.commit();
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
        state.putString("savedImagePath", tripPhotoPath);
    }
}