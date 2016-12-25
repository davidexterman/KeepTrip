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
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.keeptrip.keeptrip.trip.activity.TripCreateActivity;
import com.keeptrip.keeptrip.utils.DateFormatUtils;
import com.keeptrip.keeptrip.utils.ImageUtils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class TripUpdateFragment extends Fragment{

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

    public interface OnGetCurrentTrip {
        Trip onGetCurrentTrip();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        tripUpdateView = inflater.inflate(R.layout.fragment_trip_update, container, false);

        //  dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.US); //TODO: change local according to where i am??
//        dateFormatter = new SimpleDateFormat("E, MMM dd, yyyy", Locale.US);
        dateFormatter = DateFormatUtils.getFormDateFormat();
        tripUpdateParentActivity = getActivity();

        findViewsById();
        setListeners();
        setDatePickerSettings();

        // init the details fragment dialogs
        initDialogs();

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle(getResources().getString(R.string.trip_update_trip_toolbar_title));
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);

        if (savedInstanceState != null){
            currentTrip = savedInstanceState.getParcelable(saveCurrentTrip);
            tripPhotoPath = savedInstanceState.getString(saveTripPhotoPath);
            ImageUtils.updatePhotoImageViewByPath(getActivity(),tripPhotoPath, tripPhotoImageView);
            isRequestedPermissionFromCamera = savedInstanceState.getBoolean(saveIsRequestedPermissionFromCamera);
        }
        else{
            initCurrentTripDetails();
        }

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
                tripStartDatePickerDialog.show();
            }
        });

        // Start Date Edit Text Listener
        tripEndDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tripEndDatePickerDialog.show();
            }
        });


        // Title Edit Text Listener
//        tripTitleEditText.addTextChangedListener(new TextWatcher() {
//            public void afterTextChanged(Editable s) {
//            }
//
//            public void beforeTextChanged(CharSequence s, int start,
//                                          int count, int after) {
//            }
//
//            public void onTextChanged(CharSequence s, int start,
//                                      int before, int count) {
//
//                String strTxt = s.toString();
//                if (!strTxt.isEmpty()) {
//                    tripDoneFloatingActionButton.setEnabled(true);
//                } else {
//                    tripDoneFloatingActionButton.setEnabled(false);
//
//                }
//            }
//
//        });

        // Done Button Listener
        //   doneButton.setOnClickListener(new View.OnClickListener(){
        tripDoneFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (tripTitleEditText.getText().toString().trim().isEmpty()) {
                    tripTitleEditText.requestFocus();
                    tripTitleEditText.setError(getResources().getString(R.string.trip_no_title_error_message));
                } else {

                    currentTrip.setTitle(tripTitleEditText.getText().toString().trim());
                    currentTrip.setStartDate(DateFormatUtils.stringToDate(tripStartDateEditText.getText().toString(), dateFormatter));
                    currentTrip.setEndDate(DateFormatUtils.stringToDate(tripEndDateEditText.getText().toString(), dateFormatter));
                    currentTrip.setPlace(tripPlaceEditText.getText().toString().trim());
                    currentTrip.setPicture(tripPhotoPath);
                    currentTrip.setDescription(tripDescriptionEditText.getText().toString().trim());

                    ContentValues contentValues = currentTrip.tripToContentValues();
                    getActivity().getContentResolver().update
                            (ContentUris.withAppendedId(KeepTripContentProvider.CONTENT_TRIP_ID_URI_BASE, currentTrip.getId()), contentValues, null, null);

                    getFragmentManager().popBackStackImmediate();
                }
            }
        });

        // Trip Photo Listener
        tripPhotoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
//                        != PackageManager.PERMISSION_GRANTED){
//                    ActivityCompat.requestPermissions(getActivity(),
//                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE_PERMISSION_ACTION );
//                }
//
//                if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
//                        == PackageManager.PERMISSION_GRANTED) {
//                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                    startActivityForResult(intent, PICK_GALLERY_PHOTO_ACTION);
//                }
//                else{
//                    Toast.makeText(getActivity().getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
//                }

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
                                FragmentCompat.requestPermissions(TripUpdateFragment.this,
                                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE_PERMISSION_ACTION);
                            }
                        }
                        break;
                }
            }
        });

    }

    //TODO: make sure that i didn't forgot
    private void initCurrentTripDetails() {
        currentTrip = mGetCurrentTripCallback.onGetCurrentTrip();

        tripTitleEditText.setText(currentTrip.getTitle());
//        tripStartDate = currentTrip.getStartDate();
//        tripEndDate = currentTrip.getEndDate();
        tripStartDateEditText.setText(dateFormatter.format(currentTrip.getStartDate()));
        tripEndDateEditText.setText(dateFormatter.format(currentTrip.getEndDate()));
        tripPlaceEditText.setText(currentTrip.getPlace());
        tripDescriptionEditText.setText(currentTrip.getDescription());

        tripPhotoPath = currentTrip.getPicture();
        ImageUtils.updatePhotoImageViewByPath(getActivity(), tripPhotoPath, tripPhotoImageView);
    }
    //---------------- Date functions ---------------//
    private void setDatePickerSettings() {

        Calendar newCalendar = Calendar.getInstance();
        int currentYear = newCalendar.get(Calendar.YEAR);
        int currentMonth = newCalendar.get(Calendar.MONTH);
        int currentDay = newCalendar.get(Calendar.DAY_OF_MONTH);

        //-----------Start Date-------------//
        tripStartDatePickerDialog = new DatePickerDialog(tripUpdateParentActivity, R.style.datePickerTheme, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                tripStartDateEditText.setText(dateFormatter.format(newDate.getTime()));

               // tripStartDate = newDate.getTime();

//                tripEndDatePickerDialog.getDatePicker().setMinDate(newDate.getTimeInMillis());
//
//                try{
//                    Date endDate = dateFormatter.parse(tripEndDateEditText.getText().toString());
//                    if(tripStartDate.getTime() >= endDate.getTime()){
//                        tripEndDateEditText.setText(tripStartDateEditText.getText());
//                    }
//                }catch (ParseException e){
//                    e.getCause();
//                }catch (Exception e) {
//
//                }
            }

        }, currentYear, currentMonth, currentDay);

        //initial init
        //TODO:MAKE SURE
      //  tripStartDateEditText.setText(dateFormatter.format(newCalendar.getTime()));
       // tripStartDate = newCalendar.getTime();


        //-----------End Date-------------//
        tripEndDatePickerDialog = new DatePickerDialog(tripUpdateParentActivity, R.style.datePickerTheme, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                tripEndDateEditText.setText(dateFormatter.format(newDate.getTime()));

//                tripEndDate = newDate.getTime();
            }

        }, currentYear, currentMonth, currentDay);
//        try {
//            Date startDate = dateFormatter.parse(tripStartDateEditText.getText().toString());
//            tripEndDatePickerDialog.getDatePicker().setMinDate(startDate.getTime());
//        }
//        catch (ParseException e){
//            e.getCause();
//        }catch (Exception e) {
//
//        }
    }


    //-----------------Photo handle----------------//
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = DateFormatUtils.getImageTimeStampDateFormat().format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStorageDirectory();
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file path
        tripPhotoPath = image.getAbsolutePath();
        return image;
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
                    FragmentCompat.requestPermissions(TripUpdateFragment.this,
                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE_PERMISSION_ACTION);
                }
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
                    try {
                        ImageUtils.addImageToGallery(getActivity(), tripPhotoPath, "keepTrip", "Photo from keepTrip");
//                        MediaStore.Images.Media.insertImage(
//                                getActivity().getContentResolver(),
//                                tripPhotoPath,
//                                "keepTrip",
//                                "Photo from keepTrip");

                        ImageUtils.updatePhotoImageViewByPath(getActivity(), tripPhotoPath, tripPhotoImageView);
                    } catch (Exception ex) {
                        Toast.makeText(getActivity(), "Problem adding the taken photo", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    tripPhotoPath = null;
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

    //---------------Save and Restore--------------//
    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putString(saveTripPhotoPath, tripPhotoPath);
        state.putParcelable(saveCurrentTrip, currentTrip);
        state.putBoolean(saveIsRequestedPermissionFromCamera, isRequestedPermissionFromCamera);
    }

}