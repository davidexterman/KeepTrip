package com.keeptrip.keeptrip.trip.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v13.app.FragmentCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.net.Uri;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.dialogs.DescriptionDialogFragment;
import com.keeptrip.keeptrip.model.Trip;
import com.keeptrip.keeptrip.trip.activity.TripCreateActivity;
import com.keeptrip.keeptrip.utils.DateUtils;
import com.keeptrip.keeptrip.utils.DbUtils;
import com.keeptrip.keeptrip.utils.ImageUtils;
import com.keeptrip.keeptrip.utils.NotificationUtils;
import com.keeptrip.keeptrip.utils.SharedPreferencesUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;


//TODO: change default picture?


public class TripCreateDetailsFragment extends Fragment {

    // tag
    public static final String TAG = TripCreateDetailsFragment.class.getSimpleName();

   //photo defines
    private static final int PICK_GALLERY_PHOTO_ACTION = 0;
    private static final int TAKE_PHOTO_FROM_CAMERA_ACTION = 2;
    private static final int REQUEST_READ_STORAGE_PERMISSION_ACTION = 4;
    private static final int REQUEST_CAMERA_PERMISSION_ACTION = 3;
    static final int DESCRIPTION_DIALOG = 1;

    //photo handle
    private Uri photoURI;
    private boolean isRequestedPermissionFromCamera;
    private String saveIsRequestedPermissionFromCamera = "saveIsRequestedPermissionFromCamera";

    private View tripCreateDetailsView;
    private Activity tripCreateParentActivity;
    private ImageView tripPhotoImageView;
    private FloatingActionButton tripDoneFloatingActionButton;
    private EditText tripPlaceEditText;
    private EditText tripDescriptionEditText;
    private String tripPhotoPath;

    private AlertDialog.Builder photoOptionsDialogBuilder;
    public static final String initDescription = "initDescription";


    // Trip Photo Dialog Options
    public enum PhotoDialogOptions{
        CHANGE_PICTURE,
        TAKE_PHOTO
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        tripCreateDetailsView = inflater.inflate(R.layout.fragment_trip_create_details, container, false);
        tripCreateParentActivity = getActivity();

        findViewsById();

        // init the details fragment dialogs
        initDialogs();

        Trip currentTrip = ((TripCreateActivity)tripCreateParentActivity).currentCreatedTrip;
        if(currentTrip != null){
            tripPlaceEditText.setText(currentTrip.getPlace());
            tripPhotoPath = currentTrip.getPicture();
            ImageUtils.updatePhotoImageViewByPath(tripCreateParentActivity, tripPhotoPath, tripPhotoImageView);

            tripDescriptionEditText.setText(currentTrip.getDescription());
        }

        if (savedInstanceState != null) {
            isRequestedPermissionFromCamera = savedInstanceState.getBoolean(saveIsRequestedPermissionFromCamera);
        }
        setListeners();
        return tripCreateDetailsView;
    }


    //---------------- Init views ---------------//

    // find all needed views by id's
    private void findViewsById(){
        tripDoneFloatingActionButton = (FloatingActionButton) tripCreateDetailsView.findViewById(R.id.trip_create_details_done_floating_action_button);
        tripPhotoImageView = (ImageView) tripCreateDetailsView.findViewById(R.id.trip_create_details_photo_image_view);
        //tripReturnFloatingActionButton = (FloatingActionButton) tripCreateDetailsView.findViewById(R.id.trip_create_details_return_floating_action_button);
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
            Trip newTrip = new Trip(currentTrip.getTitle().trim(), currentTrip.getStartDate(), tripPlaceEditText.getText().toString().trim(), tripPhotoPath, tripDescriptionEditText.getText().toString().trim());

            int tripId = DbUtils.addNewTrip(getActivity(), newTrip);

            //TODO: MAKE SURE IT'S O.K
            newTrip.setId(tripId);
            Intent resultIntent = new Intent();
            resultIntent.putExtra(TripsListFragment.NEW_CREATED_TRIP, newTrip);

            tripCreateParentActivity.setResult(RESULT_OK, resultIntent);
            Toast.makeText(getActivity(), getResources().getString(R.string.toast_trip_added_message), Toast.LENGTH_LONG).show();

            // update the notification with new title only if its the last trip
            Trip latestTrip = DbUtils.getLastTrip(getActivity());
            if( (latestTrip != null && latestTrip.getId() == tripId)) {
                //a new trip is created, so reopen the quick landmark option
                SharedPreferencesUtils.saveCloseNotificationsState(getActivity(), false);

                if (NotificationUtils.areNotificationsEnabled(getActivity())) {
                    NotificationUtils.initNotification(getActivity(), newTrip.getTitle());
                }
            }

            tripCreateParentActivity.finish();

        }
    });

        // Trip Photo Listener
//        tripPhotoImageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
//                        != PackageManager.PERMISSION_GRANTED){
//                    FragmentCompat.requestPermissions(TripCreateDetailsFragment.this,
//                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE_PERMISSION_ACTION );
//                }
//                else{
//                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                    startActivityForResult(intent, PICK_GALLERY_PHOTO_ACTION);
//                }
//            }
//        });
        tripPhotoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photoOptionsDialogBuilder.show();
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
//        tripDescriptionEditText.addTextChangedListener(new TextWatcher() {
//            public void afterTextChanged(Editable s) {
//            }
//
//            public void beforeTextChanged(CharSequence s, int start,
//                                          int count, int after) {
//            }
//
//            public void onTextChanged(CharSequence s, int start,
//                                      int before, int count) {
//                ((TripCreateActivity)tripCreateParentActivity).currentCreatedTrip.setDescription(s.toString());
//            }
//        });

        tripDescriptionEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //   popUpDescriptionTextEditor();
                DialogFragment descriptionDialog = new DescriptionDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putString(initDescription, tripDescriptionEditText.getText().toString());
                descriptionDialog.setArguments(bundle);
                descriptionDialog.setTargetFragment(TripCreateDetailsFragment.this, DESCRIPTION_DIALOG);
                descriptionDialog.show(getFragmentManager(), "Description");

            }
        });
    }

    //------------dialogs-----------//
    private void initDialogs() {
        String[] dialogOptionsArray = getResources().getStringArray(R.array.trip_photo_dialog_options);

        // Use the Builder class for convenient dialog construction
        photoOptionsDialogBuilder = new AlertDialog.Builder(getActivity());
        photoOptionsDialogBuilder.setTitle(R.string.trip_photo_dialog);
        photoOptionsDialogBuilder.setItems(dialogOptionsArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position) {
                PhotoDialogOptions photoDialogOptions = PhotoDialogOptions.values()[position];
                switch (photoDialogOptions){
                    case CHANGE_PICTURE:
                        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            isRequestedPermissionFromCamera = false;
                            FragmentCompat.requestPermissions(TripCreateDetailsFragment.this,
                                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE_PERMISSION_ACTION);
                        } else {
                            Intent takePictureIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(takePictureIntent, PICK_GALLERY_PHOTO_ACTION);
                        }
                        break;
                    case TAKE_PHOTO:
                        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            isRequestedPermissionFromCamera = true;
                            FragmentCompat.requestPermissions(TripCreateDetailsFragment.this,
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
                                FragmentCompat.requestPermissions(TripCreateDetailsFragment.this,
                                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE_PERMISSION_ACTION);
                            }
                        }
                        break;
                }
            }
        });

    }

    //-----------------Photo handle----------------//
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = DateUtils.getImageTimeStampDateFormat().format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStorageDirectory();
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file path
        updateTripPhotoPath(image.getAbsolutePath());
        return image;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case PICK_GALLERY_PHOTO_ACTION:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    Uri imageUri = data.getData();
                    String[] filePath = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getActivity().getContentResolver().query(imageUri, filePath, null, null, null);
                    cursor.moveToFirst();

                    updateTripPhotoPath(cursor.getString(cursor.getColumnIndex(filePath[0])));
                    ImageUtils.updatePhotoImageViewByPath(getActivity(), tripPhotoPath, tripPhotoImageView);
// TODO: check problems from finding gallery photo
                    cursor.close();
                  // ((TripCreateActivity)tripCreateParentActivity).currentCreatedTrip.setPicture(tripPhotoPath);

                }
                break;
            case TAKE_PHOTO_FROM_CAMERA_ACTION:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        MediaStore.Images.Media.insertImage(
                                getActivity().getContentResolver(),
                                tripPhotoPath,
                                "keepTrip",
                                "Photo from keepTrip");

                        ImageUtils.updatePhotoImageViewByPath(getActivity(), tripPhotoPath, tripPhotoImageView);
                    } catch (Exception ex) {
                        Toast.makeText(getActivity(), "Problem adding the taken photo", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    updateTripPhotoPath(null);
                    ImageUtils.updatePhotoImageViewByPath(tripCreateParentActivity, tripPhotoPath, tripPhotoImageView);
                    Toast.makeText(getActivity(), "Problem adding the taken photo", Toast.LENGTH_SHORT).show();
                }
                break;
            case DESCRIPTION_DIALOG:
                if (resultCode == Activity.RESULT_OK) {
                    DescriptionDialogFragment.DialogOptions whichOptionEnum = (DescriptionDialogFragment.DialogOptions) data.getSerializableExtra(DescriptionDialogFragment.DESCRIPTION_DIALOG_OPTION);
                    switch (whichOptionEnum) {
                        case DONE:
                            String currentDescription =  data.getStringExtra(DescriptionDialogFragment.DESCRIPTION_FROM_DIALOG);
                            tripDescriptionEditText.setText(currentDescription);
                            ((TripCreateActivity)tripCreateParentActivity).currentCreatedTrip.setDescription(currentDescription);
                            break;
                        case CANCEL:
                            break;
                    }
                }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION_ACTION: {
                if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, TAKE_PHOTO_FROM_CAMERA_ACTION);
                    } else {
                        FragmentCompat.requestPermissions(TripCreateDetailsFragment.this,
                                new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE_PERMISSION_ACTION);
                    }
                }
                break;
            }

            case REQUEST_READ_STORAGE_PERMISSION_ACTION: {
                if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    if (isRequestedPermissionFromCamera) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, TAKE_PHOTO_FROM_CAMERA_ACTION);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, PICK_GALLERY_PHOTO_ACTION);
                    }
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    private void updateTripPhotoPath(String photoPath){
        tripPhotoPath = photoPath;
        ((TripCreateActivity)tripCreateParentActivity).currentCreatedTrip.setPicture(tripPhotoPath);
    }


    //-----------------Save and Restore handle-------------------//
    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putBoolean(saveIsRequestedPermissionFromCamera, isRequestedPermissionFromCamera);
    }



}