package com.keeptrip.keeptrip.trip.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.net.Uri;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.keeptrip.keeptrip.contentProvider.KeepTripContentProvider;
import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.model.Trip;
import com.keeptrip.keeptrip.utils.ImageUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class TripUpdateFragment extends Fragment {

    //photo defines
    private static final int PICK_GALLERY_PHOTO_ACTION = 0;
    private static final int REQUEST_READ_STORAGE_PERMISSION_ACTION = 4;

    private View tripUpdateView;

    private EditText tripStartDateEditText;
    private EditText tripEndDateEditText;
    private Date tripStartDate;
    private Date tripEndDate;
    private EditText tripTitleEditText;

    private DatePickerDialog tripStartDatePickerDialog;
    private DatePickerDialog tripEndDatePickerDialog;
    SimpleDateFormat dateFormatter;
    private Activity tripUpdateParentActivity;
    private ImageView tripPhotoImageView;
    private FloatingActionButton tripDoneFloatingActionButton;
    private EditText tripPlaceEditText;
    private EditText tripDescriptionEditText;
    private String tripPhotoPath;
   //  private int currentTripId;
    private Trip currentTrip;
    OnGetCurrentTrip mGetCurrentTripCallback;

    private String saveCurrentTrip = "saveCurrentTrip";
    private String saveTripPhotoPath = "saveTripPhotoPath";

    public interface OnGetCurrentTrip {
        Trip onGetCurrentTrip();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        tripUpdateView = inflater.inflate(R.layout.fragment_trip_update, container, false);

        //  dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.US); //TODO: change local according to where i am??
        dateFormatter = new SimpleDateFormat("E, MMM dd, yyyy", Locale.US);
        tripUpdateParentActivity = getActivity();

        findViewsById();
        setListeners();
        setDatePickerSettings();

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle(getResources().getString(R.string.trip_update_trip_toolbar_title));
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);

        if (savedInstanceState != null){
            currentTrip = savedInstanceState.getParcelable(saveCurrentTrip);
            tripPhotoPath = savedInstanceState.getString(saveTripPhotoPath);
            ImageUtils.updatePhotoImageViewByPath(getActivity(), tripPhotoPath, tripPhotoImageView);
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

                    currentTrip.setTitle(tripTitleEditText.getText().toString());
                    currentTrip.setStartDate(tripStartDate);
                    currentTrip.setEndDate(tripEndDate);
                    currentTrip.setPlace(tripPlaceEditText.getText().toString());
                    currentTrip.setPicture(tripPhotoPath);
                    currentTrip.setDescription(tripDescriptionEditText.getText().toString());

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

        tripDescriptionEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popUpDescriptionTextEditor();
            }
        });


    }

    //TODO: make sure that i didn't forgot
    private void initCurrentTripDetails() {
        currentTrip = mGetCurrentTripCallback.onGetCurrentTrip();

        tripTitleEditText.setText(currentTrip.getTitle());
        tripStartDate = currentTrip.getStartDate();
        tripEndDate = currentTrip.getEndDate();
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

                tripStartDate = newDate.getTime();
            }

        }, currentYear, currentMonth, currentDay);

        //initial init
        tripStartDateEditText.setText(dateFormatter.format(newCalendar.getTime()));
        tripStartDate = newCalendar.getTime();


        //-----------End Date-------------//
        tripEndDatePickerDialog = new DatePickerDialog(tripUpdateParentActivity, R.style.datePickerTheme, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                tripEndDateEditText.setText(dateFormatter.format(newDate.getTime()));

                tripEndDate = newDate.getTime();
            }

        }, currentYear, currentMonth, currentDay);

    }


    //-----------------Photo handle----------------//
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK_GALLERY_PHOTO_ACTION:
                if (resultCode == tripUpdateParentActivity.RESULT_OK && data != null) {
                    Uri imageUri = data.getData();
                    String[] filePath = {MediaStore.Images.Media.DATA};

                    Cursor cursor = tripUpdateParentActivity.getContentResolver().query(imageUri, filePath, null, null, null);
                    cursor.moveToFirst();

                    tripPhotoPath = cursor.getString(cursor.getColumnIndex(filePath[0]));
                    ImageUtils.updatePhotoImageViewByPath(getActivity(), tripPhotoPath, tripPhotoImageView);

                    cursor.close();
                }
                break;
        }
    }

    //---------------Save and Restore--------------//
    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putString(saveTripPhotoPath, tripPhotoPath);
        state.putParcelable(saveCurrentTrip, currentTrip);
    }


    private void popUpDescriptionTextEditor(){
        final View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.trip_description_dialog, null);
        final EditText dialogEditText = (EditText) dialogView.findViewById(R.id.trip_update_dialog_description_edit_text);
        dialogEditText.setText(tripDescriptionEditText.getText().toString());
        dialogEditText.setSelection(dialogEditText.getText().length());
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.trip_update_description_dialog_title)
                .setView(dialogView)
                .setPositiveButton(R.string.trip_update_description_dialog_done, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String text = dialogEditText.getText().toString();
                        tripDescriptionEditText.setText(text);
                    }
                })
                .setNegativeButton(R.string.landmark_details_description_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
    }

}