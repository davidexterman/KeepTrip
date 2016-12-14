package com.keeptrip.keeptrip;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


public class LandmarksListFragment extends Fragment implements LandmarksListRowAdapter.OnLandmarkLongPress,
        LandmarksListRowAdapter.OnOpenLandmarkDetailsForUpdate{
    private OnGetCurrentTrip mCallbackGetCurTrip;
    private OnSetCurrentLandmark mSetCurrentLandmarkCallback;

    static final int LANDMARK_DIALOG = 0;
    static final String LANDMARK_DIALOG_OPTION = "LANDMARK_DIALOG_OPTION";

    private Landmark currentLandmark;
    AlertDialog deleteLandmarkDialogConfirm;

    public interface OnSetCurrentLandmark {
        void onSetCurrentLandmark(Landmark landmark);
    }
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

        try {
            mSetCurrentLandmarkCallback = (OnSetCurrentLandmark) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SetCurrentLandmark");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_landmarks_list, container, false);
        int currentTripId = mCallbackGetCurTrip.onGetCurrentTrip().getId();
      //  Uri uri = getActivity().getContentResolver().insert(KeepTripContentProvider.CONTENT_TRIPS_URI, contentValues);
        ArrayList<Landmark> landmarks = new ArrayList<>();
        //TODO: get landmarks from database
        //todo:fix!
        //ArrayList<Landmark> landmarks = new ArrayList<>(Arrays.asList(SingletonAppDataProvider.getInstance(getActivity()).getLandmarks(trip.getId())));


        // init the the RecyclerView
//        RecyclerView landmarksRecyclerView = (RecyclerView) view.findViewById(R.id.landmarks_recycler_view);
        RecyclerView landmarksRecyclerView = (RecyclerView) getActivity().findViewById(R.id.landmarks_recycler_view);
        LandmarksListRowAdapter landmarksListRowAdapter = new LandmarksListRowAdapter(this, landmarks);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        landmarksRecyclerView.setLayoutManager(mLayoutManager);
        landmarksRecyclerView.setItemAnimator(new DefaultItemAnimator());
        landmarksRecyclerView.setAdapter(landmarksListRowAdapter);

        // init the the FloatingActionButton
        FloatingActionButton AddFab = (FloatingActionButton) view.findViewById(R.id.landmarks_main_floating_action_button);
        AddFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((LandmarkMainActivity)getActivity()).currentLandmark = null;
                LandmarkDetailsFragment newFragment = new LandmarkDetailsFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.landmark_main_fragment_container, newFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        initDialogs();
        return view;
    }

    //------------implement interfaces------------//
    @Override
    public void onLandmarkLongPress(Landmark landmark){
        currentLandmark = landmark;
        mSetCurrentLandmarkCallback.onSetCurrentLandmark(landmark);
        Bundle args = new Bundle();

        args.putParcelable(LandmarkOptionsDialogFragment.CUR_LANDMARK_PARAM, landmark);
        DialogFragment optionsDialog = new LandmarkOptionsDialogFragment();
        optionsDialog.setArguments(args);

        optionsDialog.setTargetFragment(this, LANDMARK_DIALOG);
        optionsDialog.show(getFragmentManager(), "landmarkOptions");
    }

    @Override
    public void onOpenLandmarkDetailsForUpdate(Landmark landmark) {
        currentLandmark = landmark;
        mSetCurrentLandmarkCallback.onSetCurrentLandmark(landmark);
        LandmarkDetailsFragment newFragment = new LandmarkDetailsFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.landmark_main_fragment_container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    //------------On Activity Result--------------//
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request it is that we're responding to
         if (requestCode == LANDMARK_DIALOG) {
            if (resultCode == getActivity().RESULT_OK) {
                LandmarkOptionsDialogFragment.DialogOptions whichOptionEnum = (LandmarkOptionsDialogFragment.DialogOptions) data.getSerializableExtra(LANDMARK_DIALOG_OPTION);
                switch (whichOptionEnum) {
                    case EDIT:
                        onUpdateLandmarkDialog();
                        break;
                    case DELETE:
                        deleteLandmarkDialogConfirm.setMessage(getResources().getString(R.string.landmark_delete_warning_dialog_massage) + " \"" + currentLandmark.getTitle() + "\"?");
                        deleteLandmarkDialogConfirm.show();
                        break;
                }
            }
        }
    }


    public void onUpdateLandmarkDialog(){
        LandmarkDetailsFragment updateFragment = new LandmarkDetailsFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.landmark_main_fragment_container, updateFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void onDeleteLandmarkDialog(){
        //TODO: BRING IT BACK
      //  SingletonAppDataProvider.getInstance().deleteLandmark(currentLandmark.getId());
      //  onResumeHelper();
    }

    private void initDialogs(){
        // Use the Builder class for convenient dialog construction
        deleteLandmarkDialogConfirm = new AlertDialog.Builder(getActivity())
                //set message, title, and icon
                .setTitle(getResources().getString(R.string.landmark_delete_warning_dialog_title))
                .setPositiveButton(getResources().getString(R.string.landmark_delete_warning_dialog_delete_label), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        onDeleteLandmarkDialog();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.landmark_delete_warning_dialog_cancel_label), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
    }

}

