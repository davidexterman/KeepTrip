package com.keeptrip.keeptrip;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.net.sip.SipAudioCall;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;


public class LandmarksListFragment extends Fragment {
    private OnGetCurrentTrip mCallbackGetCurTrip;
    private Toolbar myToolbar;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallbackGetCurTrip = (OnGetCurrentTrip) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnGetCurTrip");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_landmarks_list, container, false);
        Trip trip = mCallbackGetCurTrip.onGetCurrentTrip();

        // get landmarks from database
        ArrayList<Landmark> landmarks = new ArrayList<>(Arrays.asList(SingletonAppDataProvider.getInstance(getActivity()).getLandmarks(trip.getId())));

        // init the the RecyclerView
        RecyclerView landmarksRecyclerView = (RecyclerView) view.findViewById(R.id.landmarks_recycler_view);
        LandmarksListRowAdapter landmarksListRowAdapter = new LandmarksListRowAdapter(getActivity(), landmarks);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        landmarksRecyclerView.setLayoutManager(mLayoutManager);
        landmarksRecyclerView.setItemAnimator(new DefaultItemAnimator());
        landmarksRecyclerView.setAdapter(landmarksListRowAdapter);

        // init the the FloatingActionButton
        FloatingActionButton AddFab = (FloatingActionButton) view.findViewById(R.id.landmarks_main_floating_action_button);
        AddFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((LandmarkMainActivity)getActivity()).curLandmark = null;
                LandmarkDetailsFragment newFragment = new LandmarkDetailsFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.landmark_main_fragment, newFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        return view;
    }


}
