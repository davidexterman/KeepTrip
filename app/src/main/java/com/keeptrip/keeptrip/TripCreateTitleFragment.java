package com.keeptrip.keeptrip;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.net.Uri;
import android.widget.Button;
import android.widget.DatePicker;
import android.app.DatePickerDialog;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.SimpleFormatter;


public class TripCreateTitleFragment extends Fragment {

    View tripCreateActivityView;
    private EditText dateTxt;
    private DatePicker datePickerResult;
    private DatePickerDialog tripDatePicker;
    private int year;
    private int month;
    private int day;

    SimpleDateFormat dateFormatter;

    //TODO: check about the poping key board. to allow user write?

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        tripCreateActivityView = inflater.inflate(R.layout.fragment_trip_create_title, container, false);
        //  setCurrentDateOnView();

        dateFormatter = new SimpleDateFormat ("dd/MM/yyyy", Locale.US); //TODO: change local



        dateTxt = (EditText) tripCreateActivityView.findViewById(R.id.date_txt);
        dateTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tripDatePicker.show();
            }

        });

        setDateTimeField();
//        Button btn = (Button) tripCreateActivityView.findViewById(R.id.date_btn);
//        btn.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                showDatePickerDialog(v);
//            }
//        });



        return tripCreateActivityView;
    }


    private void setDateTimeField() {

        Calendar newCalendar = Calendar.getInstance();
        year = newCalendar.get(Calendar.YEAR);
        month = newCalendar.get(Calendar.MONTH);
        day = newCalendar.get(Calendar.DAY_OF_MONTH);
        tripDatePicker = new DatePickerDialog(getActivity(), R.style.datePickerTheme, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                dateTxt.setText(dateFormatter.format(newDate.getTime()));
            }

        },year, month, day);

        dateTxt.setText(dateFormatter.format(newCalendar.getTime()));
    }

    // display current date
    public void setCurrentDateOnView() {

       // datePickerResult = (DatePicker) tripCreateActivityView.findViewById(R.id.trip_create_date_picker);

        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);

        // set current date into datepicker
        datePickerResult.init(year, month, day, null);

    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }
}
