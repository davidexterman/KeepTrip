package com.keeptrip.keeptrip;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;

public class TripsListFragment extends Fragment implements TripsListRowAdapter.OnTripLongPress{
    private ArrayList<Trip> trips = new ArrayList<>();
    private RecyclerView tripsRecyclerView;
    private TripsListRowAdapter tripsListRowAdapter;
    private View currentView;
    OnSetCurrentTrip mSetCurrentTripCallback;

    static final int NEW_TRIP_CREATED = 1;
    static final String NEW_TRIP = "NEW_TRIP";

    public interface OnSetCurrentTrip {
        void onSetCurrentTrip(Trip trip);
    }


    @Override
    public void onResume() {
        tripsRecyclerView = (RecyclerView) getActivity().findViewById(R.id.trips_recycler_view);
        trips = new ArrayList<>(Arrays.asList(SingletonAppDataProvider.getInstance().getTrips()));

//        tripsListRowAdapter = new TripsListRowAdapter(getActivity(), trips);
//        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        tripsListRowAdapter = new TripsListRowAdapter(this, trips);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        tripsRecyclerView.setLayoutManager(mLayoutManager);
        tripsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        tripsRecyclerView.setAdapter(tripsListRowAdapter);

        super.onResume(); // todo: check where need to call, in the end or start of activity
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.fragment_trips_list, container, false);

        FloatingActionButton addTripFab = (FloatingActionButton) currentView.findViewById(R.id.trips_main_floating_action_button);
        addTripFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TripCreateActivity.class);
                //     startActivity(intent);
                startActivityForResult(intent, NEW_TRIP_CREATED);
            }
        });

        return currentView;
    }


    //------------On Activity Result--------------//
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request it is that we're responding to
        if (requestCode == NEW_TRIP_CREATED) {
            // Make sure the request was successful
            if (resultCode == getActivity().RESULT_OK) {
                Trip newTrip = data.getExtras().getParcelable(NEW_TRIP);

                Intent intent = new Intent(getActivity(), LandmarkMainActivity.class);
                intent.putExtra(LandmarkMainActivity.TRIP_PARAM, newTrip);
                getActivity().startActivity(intent);


            }
        }
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mSetCurrentTripCallback = (OnSetCurrentTrip) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SetCurrentTrip");
        }
    }

    @Override
    public void onTripLongPress(Trip trip){
        ((TripMainActivity)getActivity()).onSetCurrentTrip(trip);
        Bundle args = new Bundle();

        args.putParcelable(TripOptionsDialogFragment.CUR_TRIP_PARAM, trip);
        DialogFragment optionsDialog = new TripOptionsDialogFragment();
        optionsDialog.setArguments(args);

       // optionsDialog.setTitle(trip.getTitle());
        optionsDialog.show(getFragmentManager(), "tripOptions");
    }
}

