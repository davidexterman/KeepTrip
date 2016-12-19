package com.keeptrip.keeptrip.landmark.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.contentProvider.KeepTripContentProvider;
import com.keeptrip.keeptrip.landmark.activity.LandmarkMainActivity;
import com.keeptrip.keeptrip.landmark.adapter.LandmarksListRowAdapter;
import com.keeptrip.keeptrip.landmark.interfaces.OnGetCurrentTripId;
import com.keeptrip.keeptrip.model.Landmark;


public class LandmarksListFragment extends Fragment implements LandmarksListRowAdapter.OnLandmarkLongPress,
        LandmarksListRowAdapter.OnOpenLandmarkDetailsForUpdate{
    private OnGetCurrentTripId mCallbackGetCurrentTripId;
    private OnSetCurrentLandmark mSetCurrentLandmarkCallback;

    static final int LANDMARK_DIALOG = 0;
    static final String LANDMARK_DIALOG_OPTION = "LANDMARK_DIALOG_OPTION";
    static final int LANDMARK_LOADER_ID = 0;

    private Landmark currentLandmark;
    AlertDialog deleteLandmarkDialogConfirm;
    LoaderManager.LoaderCallbacks<Cursor> cursorLoaderCallbacks;
    LandmarksListRowAdapter landmarksListRowAdapter;

    private ProgressBar loadingSpinner;
    private View parentView;

    private AlertDialog deleteTripDialogConfirm;
    private int currentTripId;

    public interface OnSetCurrentLandmark {
        void onSetCurrentLandmark(Landmark landmark);
    }

    public interface GetCurrentTripTitle {
        String getCurrentTripTitle();
    }

    private GetCurrentTripTitle mCallbackGetCurrentTripTitle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        parentView = inflater.inflate(R.layout.fragment_landmarks_list, container, false);
        currentTripId = mCallbackGetCurrentTripId.onGetCurrentTripId();
        //addLandmark(currentTripId);

        loadingSpinner = (ProgressBar) parentView.findViewById(R.id.landmarks_main_progress_bar_loading_spinner);
        loadingSpinner.setVisibility(View.VISIBLE);
   //     setHasOptionsMenu(true);


        //toolbar
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(mCallbackGetCurrentTripTitle.getCurrentTripTitle());

        // init the the RecyclerView
        RecyclerView landmarksRecyclerView = (RecyclerView) parentView.findViewById(R.id.landmarks_recycler_view);
        landmarksRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        landmarksRecyclerView.setItemAnimator(new DefaultItemAnimator());
        landmarksListRowAdapter = new LandmarksListRowAdapter(getActivity(), LandmarksListFragment.this, null);
        landmarksRecyclerView.setAdapter(landmarksListRowAdapter);

        // init the cursorLoader
        cursorLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
                return new CursorLoader(getActivity(),
                        KeepTripContentProvider.CONTENT_LANDMARKS_URI,
                        null,
                        KeepTripContentProvider.Landmarks.TRIP_ID_COLUMN + " =? ",
                        new String[]{Integer.toString(currentTripId)},
                        null);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                loadingSpinner.setVisibility(View.GONE);

                // Swap the new cursor in. (The framework will take care of closing the
                // old cursor once we return.)
                landmarksListRowAdapter.swapCursor(cursor);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                // This is called when the last Cursor provided to onLoadFinished()
                // above is about to be closed.  We need to make sure we are no
                // longer using it.
                landmarksListRowAdapter.swapCursor(null);
            }
        };

        getLoaderManager().initLoader(LANDMARK_LOADER_ID, null, cursorLoaderCallbacks);

        // init the FloatingActionButton
        FloatingActionButton AddFab = (FloatingActionButton) parentView.findViewById(R.id.landmarks_main_floating_action_button);
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
        return parentView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallbackGetCurrentTripId = (OnGetCurrentTripId) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnGetCurrentTripId");
        }

        try {
            mSetCurrentLandmarkCallback = (OnSetCurrentLandmark) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SetCurrentLandmark");
        }

        try {
            mCallbackGetCurrentTripTitle = (GetCurrentTripTitle) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement GetCurrentTripTitle");
        }
    }

    public void onLandmarkLongPress(Landmark landmark){
        currentLandmark = landmark;
        mSetCurrentLandmarkCallback.onSetCurrentLandmark(landmark);
//        Bundle args = new Bundle();
//
//        args.putParcelable(LandmarkOptionsDialogFragment.CUR_LANDMARK_PARAM, landmark);
        DialogFragment optionsDialog = new LandmarkOptionsDialogFragment();
//        optionsDialog.setArguments(args);

        optionsDialog.setTargetFragment(this, LANDMARK_DIALOG);
        optionsDialog.show(getFragmentManager(), "landmarkOptions");
    }

    @Override
    public void onOpenLandmarkDetailsForUpdate(Landmark landmark) {
        currentLandmark = landmark;
        mSetCurrentLandmarkCallback.onSetCurrentLandmark(landmark);
        LandmarkViewDetailsFragment newFragment = new LandmarkViewDetailsFragment();
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
            if (resultCode == Activity.RESULT_OK) {
                LandmarkOptionsDialogFragment.DialogOptions whichOptionEnum = (LandmarkOptionsDialogFragment.DialogOptions) data.getSerializableExtra(LANDMARK_DIALOG_OPTION);
                switch (whichOptionEnum) {
                    case EDIT:
                        onUpdateLandmarkDialog();
                        break;
                    case DELETE:
                        deleteLandmarkDialogConfirm.setMessage(getResources().getString(R.string.landmark_delete_warning_dialog_message));
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
        // delete current landmark
        getActivity().getContentResolver().delete(
        ContentUris.withAppendedId(KeepTripContentProvider.CONTENT_LANDMARK_ID_URI_BASE, currentLandmark.getId()),
                null,
                null);

        //getLoaderManager().restartLoader(LANDMARK_LOADER_ID, null, cursorLoaderCallbacks);
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


//        deleteTripDialogConfirm = new AlertDialog.Builder(getActivity())
//                //set message, title, and icon
//                //     .setTitle(getResources().getString(R.string.trip_delete_warning_dialog_title))
//                .setMessage(getResources().getString(R.string.trip_delete_warning_dialog_message))
//                .setPositiveButton(getResources().getString(R.string.trip_delete_warning_dialog_delete_label), new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        onDeleteTripDialog();
//                        dialog.dismiss();
//                        //TODO:RETURN BACK
//                    }
//                })
//                .setNegativeButton(getResources().getString(R.string.trip_delete_warning_dialog_cancel_label), new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                })
//                .create();
    }

    private void addLandmark(int currentTripId) {
        //getActivity().getContentResolver().delete(KeepTripContentProvider.CONTENT_LANDMARKS_URI, null ,null);
        String date = "2016-11-10 10:12:13:222";
        ContentValues contentValues = new ContentValues();
        contentValues.put(KeepTripContentProvider.Landmarks.TRIP_ID_COLUMN, currentTripId);
        contentValues.put(KeepTripContentProvider.Landmarks.DESCRIPTION_COLUMN, "desc");
        contentValues.put(KeepTripContentProvider.Landmarks.LOCATION_COLUMN, "Loc");
        contentValues.put(KeepTripContentProvider.Landmarks.TITLE_COLUMN, "New Title" + date);
        contentValues.put(KeepTripContentProvider.Landmarks.DATE_COLUMN, date);
        getActivity().getContentResolver().insert(KeepTripContentProvider.CONTENT_LANDMARKS_URI, contentValues);
    }

    ////////////////////////////////
    //Toolbar functions
    ////////////////////////////////


//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.fragment_trip_options_menusitem, menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // handle item selection
//        switch (item.getItemId()) {
//            case R.id.edit_item:
//                //TODO: CALL UPDATE FRAGMENT
//                //   Intent editIntent = new Intent(this, TripUpdateFragment.class);
//                return true;
//            case R.id.delete_item:
//                //TODO: CHANGE TO TITLE
//                String title = getResources().getString(R.string.trip_delete_warning_dialog_title) + " " + "<b>" + currentTripId + "</b>";
//                deleteTripDialogConfirm.setTitle(Html.fromHtml(title));
//                deleteTripDialogConfirm.show();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

//
//
//    public void onDeleteTripDialog() {
//        // delete the trip
//        getActivity().getContentResolver().delete(
//                ContentUris.withAppendedId(KeepTripContentProvider.CONTENT_TRIP_ID_URI_BASE, currentTripId),
//                null,
//                null);
//
//        // delete all the landmarks of the trip
//        getActivity().getContentResolver().delete(
//                KeepTripContentProvider.CONTENT_LANDMARKS_URI,
//                KeepTripContentProvider.Landmarks.TRIP_ID_COLUMN + " =? ",
//                new String[]{Integer.toString(currentTripId)});
//    }
}

