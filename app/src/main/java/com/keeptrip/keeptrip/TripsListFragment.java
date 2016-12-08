package com.keeptrip.keeptrip;

import android.app.Fragment;
import android.content.Context;
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

public class TripsListFragment extends Fragment {
    private ArrayList<Trip> trips = new ArrayList<>();
    private RecyclerView tripsRecyclerView;
    private TripsListRowAdapter tripsListRowAdapter;
    private View currentView;
    private OnSetCurTripListener mCallbackSetCurTrip;

    public interface OnSetCurTripListener {
        public void onSetCurTrip(Trip trip);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallbackSetCurTrip = (OnSetCurTripListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }


    @Override
    public void onResume(){
        tripsRecyclerView = (RecyclerView) getActivity().findViewById(R.id.trips_recycler_view);
        trips = new ArrayList<>(Arrays.asList(SingletonAppDataProvider.getInstance().getTrips()));

        tripsListRowAdapter = new TripsListRowAdapter(trips);
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

        FloatingActionButton myFab = (FloatingActionButton) currentView.findViewById(R.id.trips_main_floating_action_button);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TripCreateActivity.class);
                startActivity(intent);
            }
        });

        return currentView;
    }
}
