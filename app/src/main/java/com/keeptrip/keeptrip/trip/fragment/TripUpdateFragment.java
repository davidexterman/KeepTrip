package com.keeptrip.keeptrip.trip.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v13.app.FragmentCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.contentProvider.KeepTripContentProvider;
import com.keeptrip.keeptrip.dialogs.DescriptionDialogFragment;
import com.keeptrip.keeptrip.model.Trip;
import com.keeptrip.keeptrip.trip.interfaces.OnGetCurrentTrip;
import com.keeptrip.keeptrip.utils.DateUtils;
import com.keeptrip.keeptrip.utils.DbUtils;
import com.keeptrip.keeptrip.utils.ImageUtils;
import com.keeptrip.keeptrip.utils.NotificationUtils;
import com.keeptrip.keeptrip.utils.SharedPreferencesUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class TripUpdateFragment extends Fragment{

    // tag
    public static final String TAG = TripUpdateFragment.class.getSimpleName();

    //photo defines
    private static final int PICK_GALLERY_PHOTO_ACTION = 0;
    private static final int REQUEST_READ_STORAGE_PERMISSION_ACTION = 4;
    static final int DESCRIPTION_DIALOG = 1;
    private static final int TAKE_PHOTO_FROM_CAMERA_ACTION = 2;
    private static final int REQUEST_CAMERA_PERMISSION_ACTION = 3;

    private View tripUpdateView;

    private EditText tripStartDateEditText;
    private EditText tripEndDateEditText;
  //  private Date tripStartDate;
  //  private Date tripEndDate;
    private EditText tripTitleEditText;

    private DatePickerDialog tripStartDatePickerDialog;
    private DatePickerDialog tripEndDatePickerDialog;
    SimpleDateFormat dateFormatter;
    private Activity tripUpdateParentActivity;
    private ImageView tripPhotoImageView;
    private FloatingActionButton tripDoneFloatingActionButton;
    private EditText tripPlaceEditText;
    public EditText tripDescriptionEditText;
    private String tripPhotoPath;
   //  private int currentTripId;
    private Trip currentTrip;
    OnGetCurrentTrip mGetCurrentTripCallback;

    private String saveCurrentTrip = "saveCurrentTrip";
    private String saveTripPhotoPath = "saveTripPhotoPath";
    private String SAVE_NEW_TAKE_PHOTO_PATH = "SAVE_NEW_TAKE_PHOTO_PATH";

    private String newTakePhotoPath;
    public static final String initDescription = "initDescription";

    //photo handle
    private Uri photoURI;
    private boolean isRequestedPermissionFromCamera;
    private String saveIsRequestedPermissionFromCamera = "saveIsRequestedPermissionFromCamera";
    private AlertDialog.Builder photoOptionsDialogBuilder;

    // Trip Photo Dialog Options
    public enum PhotoDialogOptions{
        CHANGE_PICTURE,
        TAKE_PHOTO
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null){
            currentTrip = savedInstanceState.getParcelable(saveCurrentTrip);
            tripPhotoPath = savedInstanceState.getString(saveTripPhotoPath);
            isRequestedPermissionFromCamera = savedInstanceState.getBoolean(saveIsRequestedPermissionFromCamera);
            newTakePhotoPath = savedInstanceState.getString(SAVE_NEW_TAKE_PHOTO_PATH);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        tripUpdateView = inflater.inflate(R.layout.fragment_trip_update, container, false);

        dateFormatter = DateUtils.getFormDateFormat();
        tripUpdateParentActivity = getActivity();

        findViewsById();
        setListeners();

        // init the details fragment dialogs
        initDialogs();

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle(getResources().getString(R.string.trip_update_trip_toolbar_title));
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);

        if (savedInstanceState != null){
            ImageUtils.updatePhotoImageViewByPath(getActivity(),tripPhotoPath, tripPhotoImageView);
        }
        else {
            initCurrentTripDetails();
        }

        // for start date
        setStartDatePickerSettings(currentTrip.getStartDate());

        // for end date
        setEndDatePickerSettings(currentTrip.getEndDate());

        return tripUpdateView;
    }

//
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//
//        // This makes sure that the container activity has implemented
//        // the callback interface. If not, it throws an exception
//        try {
//            mGetCurrentTripCallback = (GetCurrentTrip) context;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(context.toString()
//                    + " must implement GetCurrentTrip");
//        }
//    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mGetCurrentTripCallback = (OnGetCurrentTrip) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement GetCurrentTrip");
        }

    }

    //---------------- Init views ---------------//

    // find all needed views by id's
    private void findViewsById() {
        tripStartDateEditText = (EditText) tripUpdateView.findViewById(R.id.trip_update_start_date_edit_text);
        tripEndDateEditText = (EditText) tripUpdateView.findViewById(R.id.trip_update_end_date_edit_text);
        tripTitleEditText = (EditText) tripUpdateView.findViewById(R.id.trip_update_title_edit_text);
        tripPlaceEditText = (EditText) tripUpdateView.findViewById(R.id.trip_update_place_edit_text);
        tripDescriptionEditText = (EditText) tripUpdateView.findViewById(R.id.trip_update_description_edit_text);

        tripDoneFloatingActionButton = (FloatingActionButton) tripUpdateView.findViewById(R.id.trip_update_done_floating_action_button);
        tripPhotoImageView = (ImageView) tripUpdateView.findViewById(R.id.trip_update_photo_image_view);
        tripPlaceEditText = (EditText) tripUpdateView.findViewById(R.id.trip_update_place_edit_text);
        tripDescriptionEditText = (EditText) tripUpdateView.findViewById(R.id.trip_update_description_edit_text);
    }

    // define all needed listeners
    private void setListeners() {

        // Start Date Edit Text Listener
        tripStartDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateUtils.updateDatePicker(tripStartDatePickerDialog, DateUtils.stringToDate(tripStartDateEditText.getText().toString(), dateFormatter));
                tripStartDatePickerDialog.show();
            }
        });

        // Start Date Edit Text Listener
        tripEndDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateUtils.updateDatePicker(tripEndDatePickerDialog, DateUtils.stringToDate(tripEndDateEditText.getText().toString(), dateFormatter));
                tripEndDatePickerDialog.show();
            }
        });

        // Done Button Listener
        tripDoneFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (tripTitleEditText.getText().toString().trim().isEmpty()) {
                    tripTitleEditText.requestFocus();
                    tripTitleEditText.setError(getResources().getString(R.string.trip_no_title_error_message));
                } else {

                    currentTrip.setTitle(tripTitleEditText.getText().toString().trim());
                    currentTrip.setStartDate(DateUtils.stringToDate(tripStartDateEditText.getText().toString(), dateFormatter));
                    currentTrip.setEndDate(DateUtils.stringToDate(tripEndDateEditText.getText().toString(), dateFormatter));
                    currentTrip.setPlace(tripPlaceEditText.getText().toString().trim());
                    currentTrip.setPicture(tripPhotoPath);
                    currentTrip.setDescription(tripDescriptionEditText.getText().toString().trim());

                    ContentValues contentValues = currentTrip.tripToContentValues();
                    getActivity().getContentResolver().update
                            (ContentUris.withAppendedId(KeepTripContentProvider.CONTENT_TRIP_ID_URI_BASE, currentTrip.getId()), contentValues, null, null);

                    // update the notification with new title only if its the last trip
                    Trip latestTrip = DbUtils.getLastTrip(getActivity());
                    if(latestTrip != null && (latestTrip.getId() == currentTrip.getId())) {
                        //a new trip is created, so reopen the quick landmark option
                        SharedPreferencesUtils.saveCloseNotificationsState(getActivity(), false);

                        if (NotificationUtils.areNotificationsEnabled(getActivity())) {
                            NotificationUtils.initNotification(getActivity(), currentTrip.getTitle());
                        }
                    }
                    getFragmentManager().popBackStackImmediate();
                }
            }
        });

        // Trip Photo Listener
        tripPhotoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photoOptionsDialogBuilder.show();
            }
        });



        tripDescriptionEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             //   popUpDescriptionTextEditor();
                DialogFragment descriptionDialog = new DescriptionDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putString(initDescription, tripDescriptionEditText.getText().toString());
                descriptionDialog.setArguments(bundle);
                descriptionDialog.setTargetFragment(TripUpdateFragment.this, DESCRIPTION_DIALOG);
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
                            FragmentCompat.requestPermissions(TripUpdateFragment.this,
                                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE_PERMISSION_ACTION);
                        } else {
                            Intent takePictureIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(takePictureIntent, PICK_GALLERY_PHOTO_ACTION);
                        }
                        break;
                    case TAKE_PHOTO:
                        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            isRequestedPermissionFromCamera = true;
                            FragmentCompat.requestPermissions(TripUpdateFragment.this,
                                    new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_ACTION);
                        } else {
                            if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                                    == PackageManager.PERMISSION_GRANTED) {

                            } else {
                                FragmentCompat.requestPermissions(TripUpdateFragment.this,
                                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE_PERMISSION_ACTION);
                            }
                        }
                        break;
                }
            }
        });

    }

    private void initCurrentTripDetails() {
        currentTrip = mGetCurrentTripCallback.onGetCurrentTrip();

        tripTitleEditText.setText(currentTrip.getTitle());
        tripStartDateEditText.setText(dateFormatter.format(currentTrip.getStartDate()));
        tripEndDateEditText.setText(dateFormatter.format(currentTrip.getEndDate()));
        tripPlaceEditText.setText(currentTrip.getPlace());
        tripDescriptionEditText.setText(currentTrip.getDescription());

        tripPhotoPath = currentTrip.getPicture();
        ImageUtils.updatePhotoImageViewByPath(getActivity(), tripPhotoPath, tripPhotoImageView);
    }

    //---------------- Date functions ---------------//
    private void setStartDatePickerSettings(Date currentStartDate) {
        tripStartDatePickerDialog = DateUtils.getDatePicker(getActivity(), currentStartDate, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                tripStartDateEditText.setText(dateFormatter.format(newDate.getTime()));
            }
        });
    }

    private void setEndDatePickerSettings(Date currentEndDate) {
        tripEndDatePickerDialog = DateUtils.getDatePicker(getActivity(), currentEndDate, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                tripEndDateEditText.setText(dateFormatter.format(newDate.getTime()));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION_ACTION: {
                if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                        handleTakePhotoIntent();
                    } else {
                        FragmentCompat.requestPermissions(TripUpdateFragment.this,
                                new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE_PERMISSION_ACTION);
                    }
                }
                else {
                    Toast.makeText(getActivity().getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }

            case REQUEST_READ_STORAGE_PERMISSION_ACTION: {
                if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    if (isRequestedPermissionFromCamera) {
                        handleTakePhotoIntent();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
//            case PICK_GALLERY_PHOTO_ACTION:
//                if (resultCode == tripUpdateParentActivity.RESULT_OK && data != null) {
//                    Uri imageUri = data.getData();
//                    String[] filePath = {MediaStore.Images.Media.DATA};
//
//                    Cursor cursor = tripUpdateParentActivity.getContentResolver().query(imageUri, filePath, null, null, null);
//                    cursor.moveToFirst();
//
//                    tripPhotoPath = cursor.getString(cursor.getColumnIndex(filePath[0]));
//                    ImageUtils.updatePhotoImageViewByPath(getActivity(), tripPhotoPath, tripPhotoImageView);
//
//                    cursor.close();
//                }
//                break;

            case PICK_GALLERY_PHOTO_ACTION:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    Uri imageUri = data.getData();
                    String[] filePath = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getActivity().getContentResolver().query(imageUri, filePath, null, null, null);
                    cursor.moveToFirst();

                    tripPhotoPath = cursor.getString(cursor.getColumnIndex(filePath[0]));
                    ImageUtils.updatePhotoImageViewByPath(getActivity(), tripPhotoPath, tripPhotoImageView);
// TODO: check problems from finding gallery photo
                    cursor.close();
                }
                break;
            case TAKE_PHOTO_FROM_CAMERA_ACTION:
                if (resultCode == Activity.RESULT_OK) {
                    tripPhotoPath = newTakePhotoPath;
                    try {
                        ImageUtils.insertImageToGallery(getActivity(),tripPhotoPath, null);

                        ImageUtils.updatePhotoImageViewByPath(getActivity(), tripPhotoPath, tripPhotoImageView);
                    } catch (Exception ex) {
                        Toast.makeText(getActivity(), "Problem adding the taken photo", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
//                    tripPhotoPath = null;
                    newTakePhotoPath = null;
//                    ImageUtils.updatePhotoImageViewByPath(tripUpdateParentActivity, tripPhotoPath, tripPhotoImageView);
                    Toast.makeText(getActivity(), "Problem adding the taken photo", Toast.LENGTH_SHORT).show();
                }
                break;

            case DESCRIPTION_DIALOG:
                if (resultCode == Activity.RESULT_OK) {
                    DescriptionDialogFragment.DialogOptions whichOptionEnum = (DescriptionDialogFragment.DialogOptions) data.getSerializableExtra(DescriptionDialogFragment.DESCRIPTION_DIALOG_OPTION);
                    switch (whichOptionEnum) {
                        case DONE:
                            tripDescriptionEditText.setText(data.getStringExtra(DescriptionDialogFragment.DESCRIPTION_FROM_DIALOG));
                            break;
                        case CANCEL:
                            break;
                    }
                }
        }
    }

    public void handleTakePhotoIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = ImageUtils.createImageFile();
                newTakePhotoPath = photoFile.toString();
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
    }

    //---------------Save and Restore--------------//
    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putString(saveTripPhotoPath, tripPhotoPath);
        currentTrip.setStartDate(DateUtils.stringToDate(tripStartDateEditText.getText().toString(), dateFormatter));
        currentTrip.setEndDate(DateUtils.stringToDate(tripEndDateEditText.getText().toString(), dateFormatter));
        state.putParcelable(saveCurrentTrip, currentTrip);
        state.putBoolean(saveIsRequestedPermissionFromCamera, isRequestedPermissionFromCamera);
        state.putString(SAVE_NEW_TAKE_PHOTO_PATH, newTakePhotoPath);
    }

}