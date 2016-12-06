package com.keeptrip.keeptrip;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;


public class LandmarksListFragment extends Fragment {
    public static final String TRIP_ID_PARAM = "tripId";

    private AppDataProvider dataProvider = new SqlLiteAppDataProvider();
    private ArrayList<Landmark> trips = new ArrayList<>();
    private RecyclerView landmarksRecyclerView;
    private LandmarksListRowAdapter landmarksListRowAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_landmarks_list, container, false);

        landmarksRecyclerView = (RecyclerView) view.findViewById(R.id.landmarks_recycler_view);
        dataProvider.initialize();

        int tripId = 1; // savedInstanceState.getInt(TRIP_ID_PARAM);

        ArrayList<Landmark> landmarks = new ArrayList<>(Arrays.asList(dataProvider.getLandmarks(tripId)));

        landmarksListRowAdapter = new LandmarksListRowAdapter(landmarks);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        landmarksRecyclerView.setLayoutManager(mLayoutManager);
        landmarksRecyclerView.setItemAnimator(new DefaultItemAnimator());
        landmarksRecyclerView.setAdapter(landmarksListRowAdapter);

        FloatingActionButton myFab = (FloatingActionButton) view.findViewById(R.id.landmarks_main_floating_action_button);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(v.getContext().getApplicationContext()," Add new landmark! ", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
