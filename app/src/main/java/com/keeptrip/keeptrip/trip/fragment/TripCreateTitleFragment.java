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

import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.model.Trip;
import com.keeptrip.keeptrip.trip.activity.TripCreateActivity;
import com.keeptrip.keeptrip.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class TripCreateTitleFragment extends Fragment {

    // tag
    public static final String TAG = TripCreateTitleFragment.class.getSimpleName();

    View tripCreateTitleView;
    private EditText tripStartDateEditText;
    private EditText tripTitleEditText;
    private DatePickerDialog tripDatePickerDialog;
    private FloatingActionButton tripContinueFloatingActionButton;
    SimpleDateFormat dateFormatter;
    private Activity tripCreateParentActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        tripCreateTitleView = inflater.inflate(R.layout.fragment_trip_create_title, container, false);

        dateFormatter = DateUtils.getFormDateFormat();
        tripCreateParentActivity = getActivity();

        findViewsById();
        setListeners();

        //restore already written details, that saved in activity
        Trip currentTrip = ((TripCreateActivity)tripCreateParentActivity).currentCreatedTrip;
        tripTitleEditText.setText(currentTrip.getTitle());
        tripStartDateEditText.setText(dateFormatter.format(currentTrip.getStartDate()));

        setDatePickerSettings(currentTrip.getStartDate());

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
                    tripTitleEditText.requestFocus();
                    tripTitleEditText.setError(getResources().getString(R.string.trip_no_title_error_message));
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
                DateUtils.updateDatePicker(tripDatePickerDialog, DateUtils.stringToDate(tripStartDateEditText.getText().toString(), dateFormatter));
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
            }
        });
    }


    //---------------- Button function ---------------//
    private void onContinueButtonSelect() {
        if (tripCreateParentActivity.findViewById(R.id.trip_create_fragment_container) != null) {

          //move to details fragment
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.trip_create_fragment_container, new TripCreateDetailsFragment(), TripCreateDetailsFragment.TAG);
            transaction.addToBackStack(null);
            transaction.commit();

        }
    }

    //---------------- Date functions ---------------//
    private void setDatePickerSettings(Date currentDate) {
        tripDatePickerDialog = DateUtils.getDatePicker(getActivity(), currentDate, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);

                tripStartDateEditText.setText(dateFormatter.format(newDate.getTime()));
                ((TripCreateActivity)tripCreateParentActivity).currentCreatedTrip.setStartDate(DateUtils.stringToDate(tripStartDateEditText.getText().toString(), dateFormatter));
            }
        });
    }



    //-----------------Save and Restore handle-------------------//
    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
    }

}
