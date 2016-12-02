package com.keeptrip.keeptrip;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.app.DatePickerDialog;
import android.widget.EditText;
import android.widget.ImageButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class TripCreateTitleFragment extends Fragment {

    View tripCreateTitleView;
    private EditText dateTxt;
    private EditText titleTxt;
    private DatePickerDialog tripDatePicker;
    private int chosenYear;
    private int chosenMonth;
    private int chosenDay;
    private ImageButton continueButton;

    SimpleDateFormat dateFormatter;

    //TODO: decide if to allow user to write the date?
    //TODO: add images to continue and done buttons (and gray color when disabled)

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        tripCreateTitleView = inflater.inflate(R.layout.fragment_trip_create_title, container, false);

      //  dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.US); //TODO: change local according to where i am??
        dateFormatter = new SimpleDateFormat("E, MMM dd, yyyy", Locale.US);

        findViewsById();
        setListeners();

        if(savedInstanceState == null){
            continueButton.setEnabled(false);
        }

        setDatePickerSettings();

        return tripCreateTitleView;
    }


    //---------------- Button function ---------------//
    private void onContinueButtonSelect() {
        ((TripCreateActivity) getActivity()).tripTitle = titleTxt.getText().toString();
        if (getActivity().findViewById(R.id.trip_create_fragment_container) != null) {
            TripCreateDetailsFragment detailsFragment = new TripCreateDetailsFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();

            transaction.replace(R.id.trip_create_fragment_container, detailsFragment);
            transaction.addToBackStack(null);

            transaction.commit();
        }
    }

    //---------------- Date functions ---------------//
    private void setDatePickerSettings() {

        Calendar newCalendar = Calendar.getInstance();
        int currentYear = newCalendar.get(Calendar.YEAR);
        int currentMonth = newCalendar.get(Calendar.MONTH);
        int currentDay = newCalendar.get(Calendar.DAY_OF_MONTH);
        
        tripDatePicker = new DatePickerDialog(getActivity(), R.style.datePickerTheme, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                chosenDay = dayOfMonth;
                chosenMonth = monthOfYear;
                chosenYear = year;

                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                dateTxt.setText(dateFormatter.format(newDate.getTime()));
            }

        },currentYear, currentMonth, currentDay);

        dateTxt.setText(dateFormatter.format(newCalendar.getTime()));
    }



    //---------------- Init views ---------------//

    // find all needed views by id's
    private void findViewsById(){
        continueButton = (ImageButton) tripCreateTitleView.findViewById(R.id.trip_create_continue_button);
        dateTxt = (EditText) tripCreateTitleView.findViewById(R.id.date_txt);
        titleTxt = (EditText) tripCreateTitleView.findViewById(R.id.trip_create_title_edittext);
    }

    // find all needed listeners
    private void setListeners(){
        continueButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                onContinueButtonSelect();
            }
        });
        dateTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tripDatePicker.show();
            }
        });
        titleTxt.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

                String strTxt = s.toString();
                if (!strTxt.isEmpty()) {
                    continueButton.setEnabled(true);
                }
                else {
                    continueButton.setEnabled(false);
                }
            }

        });
    }





//    public void showDatePickerDialog(View v) {
//        DialogFragment newFragment = new DatePickerFragment();
//        newFragment.show(getFragmentManager(), "datePicker");
//    }
}
