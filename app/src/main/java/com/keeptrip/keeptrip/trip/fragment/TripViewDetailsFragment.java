package com.keeptrip.keeptrip.trip.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.model.Trip;
import com.keeptrip.keeptrip.utils.DateFormatUtils;
import com.keeptrip.keeptrip.utils.ImageUtils;

import java.text.SimpleDateFormat;


public class TripViewDetailsFragment extends Fragment {

    // Landmark View Details Views
    private TextView tripTitleTextView;
    private ImageView tripPhotoImageView;
    private TextView tripDatesTextView;
    private TextView tripPlaceTextView;
    private TextView tripDescriptionTextView;


    // Private parameters
    private View parentView;
    private OnGetCurrentTrip mCallbackGetCurrentTrip;
    private Trip currentTrip;

    private SimpleDateFormat dateFormatter;


    public interface OnGetCurrentTrip {
        Trip onGetCurrentTrip();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        parentView = inflater.inflate(R.layout.fragment_trip_view_details, container, false);

        // initialize trip date parameters
        dateFormatter = DateFormatUtils.getFormDateFormat();

        findViewsById(parentView);

        currentTrip = mCallbackGetCurrentTrip.onGetCurrentTrip();

        updateTripParameters();

        setHasOptionsMenu(true);
        return parentView;
    }

//    private void initCurrentTripDetails() {
//        currentTrip = mCallbackGetCurrentTrip.onGetCurrentTrip();
//
//        tripTitleTextView.setText(currentTrip.getTitle());
//        tripDatesTextView.setText(dateFormatter.format(currentTrip.getStartDate())
//                + "-" + dateFormatter.format(currentTrip.getEndDate()));
//        dateFormatter.format(currentTrip.getStartDate());
//
//        tripPlaceTextView.setText(currentTrip.getPlace());
//        tripDescriptionTextView.setText(currentTrip.getDescription());
//
//        String tripPhotoPath = currentTrip.getPicture();
//        ImageUtils.updatePhotoImageViewByPath(getActivity(), tripPhotoPath, tripPhotoImageView);
//    }
//

    // find all needed views by id's
    private void findViewsById(View parentView) {
        tripTitleTextView = (TextView) parentView.findViewById(R.id.trip_view_details_title);
        tripPhotoImageView = (ImageView) parentView.findViewById(R.id.trip_view_details_photo);
        tripPlaceTextView = (TextView) parentView.findViewById(R.id.trip_view_details_place);
        tripDatesTextView = (TextView) parentView.findViewById(R.id.trip_view_details_dates);
        tripDescriptionTextView = (TextView) parentView.findViewById(R.id.trip_view_details_description);
    }


    private void updateTripParameters() {

        // for each view, if it's not empty set the text, otherwise, set the view as gone.
        setViewStringOrGone(tripTitleTextView, currentTrip.getTitle());
        setViewStringOrGone(tripDatesTextView, dateFormatter.format(currentTrip.getStartDate())
                + " - " + dateFormatter.format(currentTrip.getEndDate()));
        setViewStringOrGone(tripPlaceTextView, currentTrip.getPlace());
        setViewStringOrGone(tripDescriptionTextView, currentTrip.getDescription());

        if(currentTrip.getPicture() == null || currentTrip.getPicture().trim().equals("")){
            tripPhotoImageView.setVisibility(View.GONE);
        }
        else{
            ImageUtils.updatePhotoImageViewByPath(getActivity(), currentTrip.getPicture(), tripPhotoImageView);
        }
    }

    private void setViewStringOrGone(TextView currentView, String string){
        if(string == null || string.trim().equals("")){
            currentView.setVisibility(View.GONE);
        }
        else{
            currentView.setText(string);
        }
    }

    ////////////////////////////////
    //Toolbar functions
    ////////////////////////////////
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_trip_details_menusitem, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.edit_item:
                //move to trip update details fragment
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.trip_main_fragment_container, new TripUpdateFragment());
                transaction.addToBackStack(null);
                transaction.commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallbackGetCurrentTrip = (OnGetCurrentTrip) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement GetCurrentTrip");
        }

    }
}
