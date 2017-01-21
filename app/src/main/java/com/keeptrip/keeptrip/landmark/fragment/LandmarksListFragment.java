package com.keeptrip.keeptrip.landmark.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.contentProvider.KeepTripContentProvider;
import com.keeptrip.keeptrip.landmark.activity.LandmarkMainActivity;
import com.keeptrip.keeptrip.landmark.activity.LandmarkMultiMap;
import com.keeptrip.keeptrip.landmark.adapter.LandmarksListRowAdapter;
import com.keeptrip.keeptrip.landmark.interfaces.OnGetCurrentTripId;
import com.keeptrip.keeptrip.model.Landmark;
import com.keeptrip.keeptrip.model.Trip;
import com.keeptrip.keeptrip.trip.fragment.TripViewDetailsFragment;
import com.keeptrip.keeptrip.utils.AnimationUtils;
import com.keeptrip.keeptrip.utils.DbUtils;
import com.keeptrip.keeptrip.utils.NotificationUtils;
import com.keeptrip.keeptrip.utils.SharedPreferencesUtils;
import com.keeptrip.keeptrip.utils.StartActivitiesUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class LandmarksListFragment extends Fragment implements LandmarksListRowAdapter.OnFilterPublishResults,
        LandmarksListRowAdapter.OnOpenLandmarkDetailsForUpdate, LandmarksListRowAdapter.OnActionItemPress,
        LandmarksListRowAdapter.OnGetSelectedLandmarkMap{

    // tag
    public static final String TAG = LandmarksListFragment.class.getSimpleName();

    private OnGetCurrentTripId mCallbackGetCurrentTripId;
    private OnSetCurrentLandmark mSetCurrentLandmarkCallback;
    private OnGetIsLandmarkAdded mCallbackGetIsLandmarkAdded;
    private GetCurrentTripTitle mCallbackGetCurrentTripTitle;
    private OnGetMoveToLandmarkId mCallbackGetMoveToLandmarkId;

    static final int LANDMARK_DIALOG = 0;
    static final String LANDMARK_DIALOG_OPTION = "LANDMARK_DIALOG_OPTION";
    static final int LANDMARK_LOADER_ID = 0;

    private Landmark currentLandmark;
    AlertDialog deleteLandmarkDialogConfirm;
    AlertDialog deleteMultipleLandmarkDialogConfirm;
    LoaderManager.LoaderCallbacks<Cursor> cursorLoaderCallbacks;
    LandmarksListRowAdapter landmarksListRowAdapter;
    private String currentSearchQuery;
    private LandmarkOnQueryTextListener landmarkOnQueryTextListener;

    private ProgressBar loadingSpinner;
    private ImageView arrowWhenNoLandmarksImageView;
    private TextView messageWhenNoLandmarksTextView;
    private SearchView searchView;
    private TextView searchViewNoResultsMessage;

    private String saveCurrentLandmark = "saveCurrentLandmark";
    private String saveCurrentSearchQuery = "saveCurrentSearchQuery";
    private String saveSelectedLandmarks = "saveSelectedLandmarks";
    private String saveIsMultipleSelected = "saveIsMultipleSelected";

    private int currentTripId;

    private HashMap<Integer, Landmark> multiSelectedLandmarksMap = new HashMap<Integer, Landmark>();
    private boolean isMultipleSelect = false;

    public interface OnSetCurrentLandmark {
        void onSetCurrentLandmark(Landmark landmark);
    }

    public interface GetCurrentTripTitle {
        String getCurrentTripTitle();
    }

    public interface OnGetIsLandmarkAdded {
        boolean getIsLandmarkAdded();
    }

    public interface OnGetMoveToLandmarkId {
        int onGetMoveToLandmarkId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View parentView = inflater.inflate(R.layout.fragment_landmarks_list, container, false);
        currentTripId = mCallbackGetCurrentTripId.onGetCurrentTripId();

        loadingSpinner = (ProgressBar) parentView.findViewById(R.id.landmarks_main_progress_bar_loading_spinner);
        loadingSpinner.setVisibility(View.VISIBLE);
        arrowWhenNoLandmarksImageView = (ImageView) parentView.findViewById(R.id.landmarks_add_trips_when_empty_arrow_image_view);
        messageWhenNoLandmarksTextView = (TextView) parentView.findViewById(R.id.landmarks_add_trips_when_empty_text_view);
        searchViewNoResultsMessage = (TextView) parentView.findViewById(R.id.landmarks_no_results_found_text_view);

        //toolbar
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(mCallbackGetCurrentTripTitle.getCurrentTripTitle());
        setHasOptionsMenu(true);

        if(savedInstanceState != null){
            currentLandmark = savedInstanceState.getParcelable(saveCurrentLandmark);
            currentSearchQuery = savedInstanceState.getString(saveCurrentSearchQuery);
            multiSelectedLandmarksMap = ((HashMap<Integer, Landmark>)savedInstanceState.getSerializable(saveSelectedLandmarks));
            isMultipleSelect = savedInstanceState.getBoolean(saveIsMultipleSelected);
            ((AppCompatActivity) getActivity()).supportInvalidateOptionsMenu();
        }

        // init the the RecyclerView
        final RecyclerView landmarksRecyclerView = (RecyclerView) parentView.findViewById(R.id.landmarks_recycler_view);
        landmarksRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        landmarksRecyclerView.setItemAnimator(new DefaultItemAnimator());
        landmarksListRowAdapter = new LandmarksListRowAdapter(getActivity(), LandmarksListFragment.this, null, currentSearchQuery);
        // set map if needed

        landmarksRecyclerView.setAdapter(landmarksListRowAdapter);

        // init the cursorLoader
        cursorLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
                return new CursorLoader(getActivity(),
                        KeepTripContentProvider.CONTENT_LANDMARKS_URI,
                        null,
                        KeepTripContentProvider.Landmarks.TRIP_ID_COLUMN + " =? ",
                        new String[] { Integer.toString(currentTripId) },
                        null);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                loadingSpinner.setVisibility(View.GONE);
                onCursorChange(cursor);

                // Swap the new cursor in. (The framework will take care of closing the
                // old cursor once we return.)
                landmarksListRowAdapter.swapCursor(cursor);

                int gotoLandmarkId = mCallbackGetMoveToLandmarkId.onGetMoveToLandmarkId();
                while (cursor.moveToNext()) {
                    int landmarkId = cursor.getInt(cursor.getColumnIndexOrThrow(KeepTripContentProvider.Landmarks.ID_COLUMN));
                    if (gotoLandmarkId == landmarkId) {
                        landmarksRecyclerView.getLayoutManager().scrollToPosition(cursor.getPosition()); // make it smooth
                    }
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                // This is called when the last Cursor provided to onLoadFinished()
                // above is about to be closed.  We need to make sure we are no
                // longer using it.
                landmarksListRowAdapter.swapCursor(null);
            }
        };

        if (getLoaderManager().getLoader(LANDMARK_LOADER_ID) == null) {
            getLoaderManager().initLoader(LANDMARK_LOADER_ID, null, cursorLoaderCallbacks);
        }
        else {
            getLoaderManager().restartLoader(LANDMARK_LOADER_ID, null, cursorLoaderCallbacks);
        }

        // init the FloatingActionButton
        FloatingActionButton AddFab = (FloatingActionButton) parentView.findViewById(R.id.landmarks_main_floating_action_button);
        AddFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((LandmarkMainActivity) getActivity()).currentLandmark = null;
                LandmarkDetailsFragment newFragment = new LandmarkDetailsFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.landmark_main_fragment_container, newFragment, LandmarkDetailsFragment.TAG);
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
        mCallbackGetCurrentTripId = StartActivitiesUtils.onAttachCheckInterface(activity, OnGetCurrentTripId.class);
        mSetCurrentLandmarkCallback = StartActivitiesUtils.onAttachCheckInterface(activity, OnSetCurrentLandmark.class);
        mCallbackGetCurrentTripTitle = StartActivitiesUtils.onAttachCheckInterface(activity, GetCurrentTripTitle.class);
        mCallbackGetIsLandmarkAdded = StartActivitiesUtils.onAttachCheckInterface(activity, OnGetIsLandmarkAdded.class);
        mCallbackGetMoveToLandmarkId = StartActivitiesUtils.onAttachCheckInterface(activity, OnGetMoveToLandmarkId.class);
    }

    @Override
    public void onOpenLandmarkDetailsForView(Landmark landmark) {
        currentLandmark = landmark;
        mSetCurrentLandmarkCallback.onSetCurrentLandmark(landmark);
        LandmarkViewDetailsFragment newFragment = new LandmarkViewDetailsFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.landmark_main_fragment_container, newFragment, LandmarkViewDetailsFragment.TAG);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onPause() {
        super.onPause();

        // in order to save the current search query, we need to deactivate the callbacks.
        if (searchView != null) {
            searchView.setOnQueryTextListener(null);
        }


//        ((AppCompatActivity) getActivity()).getSupportActionBar().co
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).supportInvalidateOptionsMenu();

        if (searchView != null) {
            searchView.setOnQueryTextListener(getLandmarkOnQueryTextListener());
        }
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
                        onOpenLandmarkDetailsForUpdate();
                        break;
                    case DELETE:
                        deleteLandmarkDialogConfirm.setMessage(getResources().getString(R.string.landmark_delete_warning_dialog_message));
                        deleteLandmarkDialogConfirm.show();
                        break;
                    case VIEW:
                        onOpenLandmarkDetailsForView(currentLandmark);
                        break;
                }
            }
        }
    }

    public void onOpenLandmarkDetailsForUpdate() {
        LandmarkDetailsFragment updateFragment = new LandmarkDetailsFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.landmark_main_fragment_container, updateFragment, LandmarkDetailsFragment.TAG);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void onDeleteLandmarkDialog() {
        // delete current landmark
        getActivity().getContentResolver().delete(
                ContentUris.withAppendedId(KeepTripContentProvider.CONTENT_LANDMARK_ID_URI_BASE, currentLandmark.getId()),
                null,
                null);
    }

    public void onDeleteMultipleLandmarks() {
        for (Landmark landmark : multiSelectedLandmarksMap.values()) {
            getActivity().getContentResolver().delete(
                    ContentUris.withAppendedId(KeepTripContentProvider.CONTENT_LANDMARK_ID_URI_BASE, landmark.getId()),
                    null,
                    null);
        }
    }

    private void initDialogs() {
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

        deleteMultipleLandmarkDialogConfirm = new AlertDialog.Builder(getActivity())
                //set message, title, and icon
                .setTitle(getResources().getString(R.string.landmark_multiple_delete_warning_dialog_title))
                .setPositiveButton(getResources().getString(R.string.landmark_delete_warning_dialog_delete_label), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        onDeleteMultipleLandmarks();
                        dialog.dismiss();
                        landmarksListRowAdapter.handleFinishActionMode();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.landmark_delete_warning_dialog_cancel_label), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
    }


    //---------------------save-------------------//

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(saveCurrentLandmark, currentLandmark);
        outState.putString(saveCurrentSearchQuery, currentSearchQuery);
        outState.putSerializable(saveSelectedLandmarks, multiSelectedLandmarksMap);
        outState.putBoolean(saveIsMultipleSelected, isMultipleSelect);

    }

    ////////////////////////////////
    //Toolbar functions
    ////////////////////////////////
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_landmarks_timeline_menusitem, menu);

        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(getLandmarkOnQueryTextListener());

        EditText searchEditText = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        if(searchEditText != null) {
            searchEditText.setTextColor(Color.WHITE);
        }
    }

    private class LandmarkOnQueryTextListener implements SearchView.OnQueryTextListener {
        @Override
        public boolean onQueryTextSubmit(String query) {
            searchView.clearFocus();
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            updateSearchQuery(newText);
            return true;
        }
    }

    public LandmarkOnQueryTextListener getLandmarkOnQueryTextListener() {
        if (landmarkOnQueryTextListener == null) {
            landmarkOnQueryTextListener = new LandmarkOnQueryTextListener();
        }
        return landmarkOnQueryTextListener;
    }

    @Override
        public void onPrepareOptionsMenu(Menu menu) {
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        if (!TextUtils.isEmpty(currentSearchQuery)) {
            String searchQuery = currentSearchQuery;

            // set to null in order to avoid text change when expandActionView
            searchView.setOnQueryTextListener(null);
            MenuItemCompat.expandActionView(menu.findItem(R.id.search));

            searchView.setOnQueryTextListener(getLandmarkOnQueryTextListener());
            searchView.setQuery(searchQuery, true);
        }

        //check if i'm the last trip and the quick landmark window is closed
        MenuItem showQuickLandmarks =  menu.findItem(R.id.show_quick_landmarks_option_item);
        MenuItem hideQuickLandmarks =  menu.findItem(R.id.hide_quick_landmarks_option_item);
        Trip lastTrip = DbUtils.getLastTrip(getActivity());
        if(lastTrip != null && lastTrip.getId() == currentTripId){
           if(!SharedPreferencesUtils.getIsNotificationsWindowOpen(getActivity())){
               showQuickLandmarks.setVisible(true);
               hideQuickLandmarks.setVisible(false);
           }
           else {
               showQuickLandmarks.setVisible(false);
               hideQuickLandmarks.setVisible(true);
           }
        }
        super.onPrepareOptionsMenu(menu);
    }

    private void updateSearchQuery(String query) {
        if (TextUtils.equals(query, currentSearchQuery)) {
            return;
        }

        currentSearchQuery = query;
        if (landmarksListRowAdapter != null) {
            landmarksListRowAdapter.getFilter().filter(query);
        }
    }


    @Override
    public void onFilterPublishResults(int resultsCount) {
        if (resultsCount == 0 && (messageWhenNoLandmarksTextView.getVisibility() == View.GONE)) {
            searchViewNoResultsMessage.setVisibility(View.VISIBLE);
        } else {
            searchViewNoResultsMessage.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.view_trip_details_item:
                //move to trip view details fragment
                TripViewDetailsFragment tripViewFragment = new TripViewDetailsFragment();
                Bundle bundle = new Bundle();
                bundle.putBoolean(TripViewDetailsFragment.FROM_TRIPS_LIST, false);
                tripViewFragment.setArguments(bundle);
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.landmark_main_fragment_container, tripViewFragment, TripViewDetailsFragment.TAG);
                transaction.addToBackStack(null);
                transaction.commit();
                return true;

            case R.id.view_map_item:
                Intent mapIntent = new Intent(getActivity(), LandmarkMultiMap.class);
                Bundle gpsLocationBundle = new Bundle();
                ArrayList<Landmark> landmarkArray = new ArrayList();

                Cursor cursor = landmarksListRowAdapter.getOrigCursor();
                if(cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            Landmark currentLandmark = new Landmark(cursor);
                            landmarkArray.add(currentLandmark);
                        } while (cursor.moveToNext());
                    }

                    gpsLocationBundle.putParcelableArrayList(LandmarkMainActivity.LandmarkArrayList, landmarkArray);
                    mapIntent.putExtras(gpsLocationBundle);
                    startActivity(mapIntent);
                }
                break;
            case R.id.show_quick_landmarks_option_item:
                if(NotificationUtils.areNotificationsEnabled(getActivity())) {
                    NotificationUtils.initNotification(getActivity(), DbUtils.getLastTrip(getActivity()).getTitle());
//                    ((AppCompatActivity) getActivity()).supportInvalidateOptionsMenu();
                }
                else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.notification_disabled_message), Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.hide_quick_landmarks_option_item:
                NotificationUtils.cancelNotification(getActivity());
//                ((AppCompatActivity) getActivity()).supportInvalidateOptionsMenu();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void onCursorChange(Cursor cursor) {
        if (mCallbackGetIsLandmarkAdded.getIsLandmarkAdded()) {
            return;
        }

        if (cursor.getCount() == 0) {
                arrowWhenNoLandmarksImageView.setVisibility(View.VISIBLE);
                arrowWhenNoLandmarksImageView.setAnimation(AnimationUtils.getArrowListEmptyAnimation());
                messageWhenNoLandmarksTextView.setVisibility(View.VISIBLE);
        } else {
            arrowWhenNoLandmarksImageView.setAnimation(null);
            arrowWhenNoLandmarksImageView.setVisibility(View.GONE);
            messageWhenNoLandmarksTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void OnActionItemPress(MenuItem item) {
        int id = item.getItemId();
        if(multiSelectedLandmarksMap.values().size() == 1){
            currentLandmark = (Landmark) multiSelectedLandmarksMap.values().iterator().next();
            mSetCurrentLandmarkCallback.onSetCurrentLandmark(currentLandmark);
        }
        switch (id) {
            case R.id.multiple_select_action_delete:
                deleteMultipleLandmarkDialogConfirm.setMessage(getResources().getString(R.string.landmark_multiple_delete_warning_dialog_message));
                deleteMultipleLandmarkDialogConfirm.show();
                break;
            case R.id.multiple_select_action_edit:
                onOpenLandmarkDetailsForUpdate();
                landmarksListRowAdapter.handleFinishActionMode();
                break;
            case R.id.multiple_select_action_view:
                onOpenLandmarkDetailsForView(currentLandmark);
                landmarksListRowAdapter.handleFinishActionMode();
                break;

        }
    }

    @Override
    public HashMap<Integer, Landmark> onGetSelectedLandmarkMap() {
        return multiSelectedLandmarksMap;
    }

    @Override
    public boolean getIsMultipleSelect() {
        return isMultipleSelect;
    }

    @Override
    public void setIsMultipleSelect(boolean isMultipleSelect) {
        this.isMultipleSelect = isMultipleSelect;
    }

    @Override
    public void onClearSelectedLandmarkMap() {
        multiSelectedLandmarksMap.clear();
    }




}