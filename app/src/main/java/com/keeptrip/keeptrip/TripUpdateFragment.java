package com.keeptrip.keeptrip;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.net.Uri;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class TripUpdateFragment extends Fragment {

    private static final int PICK_GALLERY_PHOTO_ACTION_NUM = 0;

    private View tripUpdateView;
    private EditText tripStartDateTxt;
    private EditText tripEndDateTxt;
    private Date tripStartDate;
    private Date tripEndDate;
    private EditText tripTitle;
    private DatePickerDialog tripStartDatePicker;
    private DatePickerDialog tripEndDatePicker;
    SimpleDateFormat dateFormatter;
    private Activity tripUpdateParentActivity;
    private ImageView tripPhotoImageView;
    private FloatingActionButton doneFloatingActionButton;
    private EditText tripPlace;
    private EditText tripDescription;
    private String tripPhotoPath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        tripUpdateView = inflater.inflate(R.layout.fragment_trip_update, container, false);

        //  dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.US); //TODO: change local according to where i am??
        dateFormatter = new SimpleDateFormat("E, MMM dd, yyyy", Locale.US);
        tripUpdateParentActivity = getActivity();

        //TODO: reload trip existing details

        findViewsById();

        if (savedInstanceState != null){
            tripPhotoImageView.setImageBitmap((Bitmap)savedInstanceState.getParcelable("savedImagePath"));
        }

        setListeners();

        setDatePickerSettings();

        //TODO: delete
        tripTitle.setText("init");

        return tripUpdateView;
    }


    //---------------- Init views ---------------//

    // find all needed views by id's
    private void findViewsById() {
        tripStartDateTxt = (EditText) tripUpdateView.findViewById(R.id.trip_update_start_date_edit_text);
        tripEndDateTxt = (EditText) tripUpdateView.findViewById(R.id.trip_update_end_date_edit_text);
        tripTitle = (EditText) tripUpdateView.findViewById(R.id.trip_update_title_edit_text);

        doneFloatingActionButton = (FloatingActionButton) tripUpdateView.findViewById(R.id.trip_update_done_floating_action_button);
        tripPhotoImageView = (ImageView) tripUpdateView.findViewById(R.id.trip_update_photo_image_view);
        tripPlace = (EditText) tripUpdateView.findViewById(R.id.trip_update_place_edit_text);
        tripDescription = (EditText) tripUpdateView.findViewById(R.id.trip_update_description_edit_text);
    }

    // find all needed listeners
    private void setListeners() {

        // Start Date Edit Text Listener
        tripStartDateTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tripStartDatePicker.show();
            }
        });

        // Start Date Edit Text Listener
        tripEndDateTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tripEndDatePicker.show();
            }
        });


        // Title Edit Text Listener
        tripTitle.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

                String strTxt = s.toString();
                if (!strTxt.isEmpty()) {
                    doneFloatingActionButton.setEnabled(true);
                } else {
                    doneFloatingActionButton.setEnabled(false);

                }
            }

        });

        // Done Button Listener
        //   doneButton.setOnClickListener(new View.OnClickListener(){
        doneFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: save all the details to database

                Trip updatedTrip = new Trip(tripTitle.getText().toString(), tripStartDate, tripPlace.getText().toString(), tripPhotoPath, tripDescription.getText().toString());
                updatedTrip.setEndDate(tripEndDate);

                //TODO: how to call this method
                SingletonAppDataProvider.getInstance().updateTripDetails(updatedTrip);
                //Toast.makeText(tripUpdateParentActivity, "Trip \"" + tripTitle.getText().toString() + "\" was updated successfully", Toast.LENGTH_SHORT).show();
            }
        });

        // Trip Photo Listener
        tripPhotoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_GALLERY_PHOTO_ACTION_NUM);
            }
        });


    }


    //---------------- Date functions ---------------//
    private void setDatePickerSettings() {

        Calendar newCalendar = Calendar.getInstance();
        int currentYear = newCalendar.get(Calendar.YEAR);
        int currentMonth = newCalendar.get(Calendar.MONTH);
        int currentDay = newCalendar.get(Calendar.DAY_OF_MONTH);

        //-----------Start Date-------------//
        tripStartDatePicker = new DatePickerDialog(tripUpdateParentActivity, R.style.datePickerTheme, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                tripStartDateTxt.setText(dateFormatter.format(newDate.getTime()));

                tripStartDate = newDate.getTime();
            }

        }, currentYear, currentMonth, currentDay);
        tripStartDateTxt.setText(dateFormatter.format(newCalendar.getTime()));
        tripStartDate = newCalendar.getTime();

        //-----------End Date-------------//
        tripEndDatePicker = new DatePickerDialog(tripUpdateParentActivity, R.style.datePickerTheme, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                tripEndDateTxt.setText(dateFormatter.format(newDate.getTime()));

                tripEndDate = newDate.getTime();
            }

        }, currentYear, currentMonth, currentDay);
        tripEndDateTxt.setText(dateFormatter.format(newCalendar.getTime()));
        tripEndDate = newCalendar.getTime();
    }


    //-----------------Photo handle----------------//
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK_GALLERY_PHOTO_ACTION_NUM:
                if (resultCode == tripUpdateParentActivity.RESULT_OK && data != null) {
                    Uri imageUri = data.getData();
                    String[] filePath = {MediaStore.Images.Media.DATA};

                    Cursor cursor = tripUpdateParentActivity.getContentResolver().query(imageUri, filePath, null, null, null);
                    cursor.moveToFirst();

                    tripPhotoPath = cursor.getString(cursor.getColumnIndex(filePath[0]));

                    Bitmap d = BitmapFactory.decodeFile(tripPhotoPath);
                    int nh = (int) (d.getHeight() * (512.0 / d.getWidth()));
                    Bitmap scaled = Bitmap.createScaledBitmap(d, 512, nh, true);
                    tripPhotoImageView.setImageBitmap(scaled);

                    cursor.close();
                }
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putParcelable("savedImagePath", ((BitmapDrawable)tripPhotoImageView.getDrawable()).getBitmap());
    }
}