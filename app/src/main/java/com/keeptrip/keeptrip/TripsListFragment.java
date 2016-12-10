package com.keeptrip.keeptrip;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
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

public class TripsListFragment extends Fragment implements TripsListRowAdapter.OnTripLongPress {
    private ArrayList<Trip> trips = new ArrayList<>();
    private RecyclerView tripsRecyclerView;
    private TripsListRowAdapter tripsListRowAdapter;
    private View currentView;
    private OnSetCurrentTrip mSetCurrentTripCallback;
    private Trip currentTrip;
    AlertDialog deleteTripDialogConfirm;

    static final int NEW_TRIP_CREATED = 1;
    static final String NEW_TRIP = "NEW_TRIP";
    static final int TRIP_DIALOG = 0;
    static final String TRIP_DIALOG_OPTION = "TRIP_DIALOG_OPTION";


    public interface OnSetCurrentTrip {
        void onSetCurrentTrip(Trip trip);
    }


    @Override
    public void onResume() {
        onResumeHelper();
    }

    //TODO: check if we can minimize it
    private void onResumeHelper(){
        tripsRecyclerView = (RecyclerView) getActivity().findViewById(R.id.trips_recycler_view);
        trips = new ArrayList<>(Arrays.asList(SingletonAppDataProvider.getInstance().getTrips()));

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
                startActivityForResult(intent, NEW_TRIP_CREATED);
            }
        });
        initDialogs();
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
        } else if (requestCode == TRIP_DIALOG) {
            if (resultCode == getActivity().RESULT_OK) {
                TripOptionsDialogFragment.DialogOptions whichOptionEnum = (TripOptionsDialogFragment.DialogOptions) data.getSerializableExtra(TRIP_DIALOG_OPTION);
                switch (whichOptionEnum) {
                    case EDIT:
                        onUpdateTripDialog();
                        break;
                    case DELETE:
                        deleteTripDialogConfirm.setMessage(getResources().getString(R.string.trip_delete_warning_dialog_massage) + " \"" + currentTrip.getTitle() + "\"?");
                        deleteTripDialogConfirm.show();
                        break;
                }
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

    //------------implement interfaces------------//
    @Override
    public void onTripLongPress(Trip trip){
        currentTrip = trip;
        mSetCurrentTripCallback.onSetCurrentTrip(trip);
        Bundle args = new Bundle();

        args.putParcelable(TripOptionsDialogFragment.CUR_TRIP_PARAM, trip);
        DialogFragment optionsDialog = new TripOptionsDialogFragment();
        optionsDialog.setArguments(args);

        optionsDialog.setTargetFragment(this, TRIP_DIALOG);
        optionsDialog.show(getFragmentManager(), "tripOptions");
    }

//    @Override
    public void onUpdateTripDialog(){
        TripUpdateFragment updateFragment = new TripUpdateFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.trip_main_fragment_container, updateFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
//
//    @Override
    public void onDeleteTripDialog(){
        SingletonAppDataProvider.getInstance().deleteTrip(currentTrip.getId());
        onResumeHelper();

    }


    private void initDialogs(){
        // Use the Builder class for convenient dialog construction
        deleteTripDialogConfirm = new AlertDialog.Builder(getActivity())
                //set message, title, and icon
                .setTitle(getResources().getString(R.string.trip_delete_warning_dialog_title))
                .setPositiveButton(getResources().getString(R.string.trip_delete_warning_dialog_delete_label), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        onDeleteTripDialog();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.trip_delete_warning_dialog_cancel_label), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
    }
}

