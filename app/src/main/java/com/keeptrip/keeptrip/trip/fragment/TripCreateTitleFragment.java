package com.keeptrip.keeptrip.trip.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.app.DatePickerDialog;
import android.widget.EditText;
import android.widget.TextView;

import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.model.Trip;
import com.keeptrip.keeptrip.trip.activity.TripCreateActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class TripCreateTitleFragment extends Fragment {

    View tripCreateTitleView;
    private EditText tripStartDateEditText;
    private EditText tripTitleEditText;
    private DatePickerDialog tripDatePickerDialog;
    private FloatingActionButton tripContinueFloatingActionButton;
    SimpleDateFormat dateFormatter;
    private Activity tripCreateParentActivity;
    private Date tripStartDate;

    //TODO: add states to the floating button (enabled\disabled\pressed)
    //TODO: restrict number of characters on title? input type?
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        tripCreateTitleView = inflater.inflate(R.layout.fragment_trip_create_title, container, false);

      //  dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.US); //TODO: change local according to where i am??
        dateFormatter = new SimpleDateFormat("E, MMM dd, yyyy", Locale.US);
        tripCreateParentActivity = getActivity();

        findViewsById();
        setListeners();
        setDatePickerSettings();

//        if(savedInstanceState == null){
//            tripContinueFloatingActionButton.setEnabled(false);
//        }

        //restore already written details, that saved in activity
        Trip currentTrip = ((TripCreateActivity)tripCreateParentActivity).currentCreatedTrip;
        if(currentTrip == null){
            ((TripCreateActivity)getActivity()).currentCreatedTrip = new Trip(tripTitleEditText.getText().toString(), tripStartDate, "", "", "");

        }
        else {
            tripTitleEditText.setText(currentTrip.getTitle());
            tripStartDate = currentTrip.getStartDate();
            tripStartDateEditText.setText(dateFormatter.format(tripStartDate));
        }


        return tripCreateTitleView;
    }


    //---------------- Init views ---------------//

    // define all needed views by id's
    private void findViewsById(){
        tripContinueFloatingActionButton = (FloatingActionButton) tripCreateTitleView.findViewById(R.id.trip_create_title_continue_floating_action_button);
        tripStartDateEditText = (EditText) tripCreateTitleView.findViewById(R.id.date_txt);
        tripTitleEditText = (EditText) tripCreateTitleView.findViewById(R.id.trip_create_title_edittext);
    }

    // find all needed listeners
    private void setListeners(){

        // Continue Button Listener

        tripContinueFloatingActionButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (tripTitleEditText.getText().toString().trim().isEmpty()){
                    tripTitleEditText.setError(getResources().getString(R.string.trip_create_no_title_error_message));
                }
                else {
                    onContinueButtonSelect();
                }
            }
        });

        // Date Edit Text Listener
        tripStartDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tripDatePickerDialog.show();
            }
        });

        // Title Edit Text Listener
        tripTitleEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                String strTxt = s.toString();
                ((TripCreateActivity)tripCreateParentActivity).currentCreatedTrip.setTitle(strTxt);
//                if (!strTxt.isEmpty()) {
//                    tripContinueFloatingActionButton.setEnabled(true);
//                }
//                else {
//                    tripContinueFloatingActionButton.setEnabled(false);
//                }
            }
        });
    }


    //---------------- Button function ---------------//
    private void onContinueButtonSelect() {
        if (tripCreateParentActivity.findViewById(R.id.trip_create_fragment_container) != null) {

          //move to details fragment
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.trip_create_fragment_container, new TripCreateDetailsFragment());
            transaction.addToBackStack(null);
            transaction.commit();

        }
    }

    //---------------- Date functions ---------------//
    private void setDatePickerSettings() {

        final Calendar newCalendar = Calendar.getInstance();
        int currentYear = newCalendar.get(Calendar.YEAR);
        int currentMonth = newCalendar.get(Calendar.MONTH);
        int currentDay = newCalendar.get(Calendar.DAY_OF_MONTH);
        
        tripDatePickerDialog = new DatePickerDialog(tripCreateParentActivity, R.style.datePickerTheme, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);

                tripStartDateEditText.setText(dateFormatter.format(newDate.getTime()));
                tripStartDate = newDate.getTime();
                ((TripCreateActivity)tripCreateParentActivity).currentCreatedTrip.setStartDate(tripStartDate);

            }

        },currentYear, currentMonth, currentDay);

        tripStartDateEditText.setText(dateFormatter.format(newCalendar.getTime()));
        tripStartDate = newCalendar.getTime();
    }



    //-----------------Save and Restore handle-------------------//
    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
    }

}