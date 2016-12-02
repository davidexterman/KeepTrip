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

public class TripsListFragment extends Fragment {
    private AppDataProvider dataProvider = new SqlLiteAppDataProvider();
    private ArrayList<Trip> trips = new ArrayList<>();
    private RecyclerView tripsRecyclerView;
    private TripsCardsAdapter tripsCardsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trips_list, container, false);


        tripsRecyclerView = (RecyclerView) view.findViewById(R.id.trips_recycler_view);
        dataProvider.initialize();

        trips = new ArrayList<>(Arrays.asList(dataProvider.getTrips()));

        tripsCardsAdapter = new TripsCardsAdapter(trips);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        tripsRecyclerView.setLayoutManager(mLayoutManager);
        tripsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        tripsRecyclerView.setAdapter(tripsCardsAdapter);

        FloatingActionButton myFab = (FloatingActionButton) view.findViewById(R.id.trips_main_floating_action_button);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(v.getContext().getApplicationContext()," Add new trips! ", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
