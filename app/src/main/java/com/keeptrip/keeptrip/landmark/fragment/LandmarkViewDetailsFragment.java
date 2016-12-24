package com.keeptrip.keeptrip.landmark.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.landmark.interfaces.OnGetCurrentLandmark;
import com.keeptrip.keeptrip.model.Landmark;
import com.keeptrip.keeptrip.utils.DateFormatUtils;
import com.keeptrip.keeptrip.utils.ImageUtils;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by david on 12/19/2016.
 */

public class LandmarkViewDetailsFragment extends Fragment {

    // Landmark View Details Views
    private TextView lmTitleTextView;
    private ImageView lmPhotoImageView;
    private TextView lmDateTextView;
    private TextView lmLocationTextView;
    private LinearLayout lmTypeLayout;
    private TextView lmTypeTextView;
    private ImageView lmIconTypeImageView;
    private TextView lmDescriptionTextView;


    // Private parameters
    private Landmark currentLandmark;
    private View parentView;
    private OnGetCurrentLandmark mCallback;
    private SimpleDateFormat dateFormatter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        parentView = inflater.inflate(R.layout.fragment_landmark_view_details, container, false);

        // initialize landmark date parameters
        //  dateFormatter = new SimpleDateFormat("E, MMM dd, yyyy", Locale.US);
        dateFormatter = DateFormatUtils.getFormDateFormat();

        findViewsById(parentView);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle(getResources().getString(R.string.landmark_view_details_toolbar_title));
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);

        currentLandmark = mCallback.onGetCurrentLandmark();
        if (currentLandmark != null) {
            // We were called from Update Landmark need to update parameters
            updateLmParameters();
        }

        setHasOptionsMenu(true);
        return parentView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnGetCurrentLandmark) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnGetCurrentLandmark");
        }
    }


    // find all needed views by id's
    private void findViewsById(View parentView) {
        lmTitleTextView = (TextView) parentView.findViewById(R.id.landmark_view_details_title);
        lmPhotoImageView = (ImageView) parentView.findViewById(R.id.landmark_view_details_photo);
        lmLocationTextView = (TextView) parentView.findViewById(R.id.landmark_view_details_location);
        lmDateTextView = (TextView) parentView.findViewById(R.id.landmark_view_details_date);
        lmTypeLayout = (LinearLayout) parentView.findViewById(R.id.landmark_view_type_layout);
        lmTypeTextView = (TextView) parentView.findViewById(R.id.landmark_view_details_type);
        lmIconTypeImageView = (ImageView) parentView.findViewById(R.id.landmark_view_details_icon_type);
        lmDescriptionTextView = (TextView) parentView.findViewById(R.id.landmark_view_details_description);
    }

    // Update Landmark , need to update landmark Parameters
    private void updateLmParameters() {

        String[] type = getResources().getStringArray(R.array.landmark_details_type_spinner_array);
        TypedArray iconType = getResources().obtainTypedArray(R.array.landmark_view_details_icon_type_array);

        // for each view, if it's not empty set the text, otherwise, set the view as gone.
        setViewStringOrGone(lmTitleTextView,
                parentView.findViewById(R.id.landmark_view_underline_title),
                currentLandmark.getTitle());
        setViewStringOrGone(lmDateTextView,
                null,
                dateFormatter.format(currentLandmark.getDate()));
        setViewStringOrGone(lmLocationTextView,
                parentView.findViewById(R.id.landmark_view_uperline_location),
                currentLandmark.getLocation());
        setViewStringOrGone(lmTypeTextView,
                parentView.findViewById(R.id.landmark_view_uperline_type),
                type[currentLandmark.getTypePosition()]);
        setViewStringOrGone(lmDescriptionTextView,
                parentView.findViewById(R.id.landmark_view_uperline_description),
                currentLandmark.getDescription());

        if(currentLandmark.getTypePosition() > 0){
            lmTypeLayout.setVisibility(View.VISIBLE);
            lmIconTypeImageView.setVisibility(View.VISIBLE);
            lmIconTypeImageView.setImageResource(iconType.getResourceId(currentLandmark.getTypePosition(), -1));
        }
        else{
            lmTypeLayout.setVisibility(View.GONE);
            lmIconTypeImageView.setVisibility(View.GONE);
        }

        if (currentLandmark.getPhotoPath() == null || currentLandmark.getPhotoPath().trim().equals("")) {
            lmPhotoImageView.setVisibility(View.GONE);
        } else {
            ImageUtils.updatePhotoImageViewByPath(getActivity(), currentLandmark.getPhotoPath(), lmPhotoImageView);
        }
    }

    private void setViewStringOrGone(TextView currentView, View view, String string) {
        if (string == null || string.trim().isEmpty()) {
            currentView.setVisibility(View.GONE);
            if(view != null){
                view.setVisibility(View.GONE);
            }
        } else {
            currentView.setText(string);
        }
    }

    ////////////////////////////////
    //Toolbar functions
    ////////////////////////////////
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_landmark_details_menusitem, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.edit_item:
                //move to landmark update details fragment
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.landmark_main_fragment_container, new LandmarkDetailsFragment(), "LANDMARK_DETAILS_FRAGMENT");
                transaction.addToBackStack(null);
                transaction.commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}