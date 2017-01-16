package com.keeptrip.keeptrip.trip.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;

import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.contentProvider.KeepTripContentProvider;
import com.keeptrip.keeptrip.model.Landmark;
import com.keeptrip.keeptrip.model.Trip;
import com.keeptrip.keeptrip.trip.adapter.SearchResultCursorTreeAdapter;
import com.keeptrip.keeptrip.trip.interfaces.OnSetCurrentTrip;
import com.keeptrip.keeptrip.utils.StartActivitiesUtils;

public class TripSearchResultFragment extends Fragment {

    // TAG
    public static final String TAG = TripSearchResultFragment.class.getSimpleName();

    private SearchResultCursorTreeAdapter mAdapter;
    private ProgressBar loadingSpinner;
    private SearchView searchView;
    public LoaderManager.LoaderCallbacks<Cursor> cursorLoaderCallbacks; // todo: change this!!!

    private OnGetSearchQueryListener mCallBackGetSearchQuery;
    private OnSetCurrentTrip mSetCurrentTripCallback;

    private String currentSearchQuery;

    public interface OnGetSearchQueryListener {
        String onGetSearchQuery();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View currentView = inflater.inflate(R.layout.fragment_trip_search_result, container, false);

        final AppCompatActivity activity = (AppCompatActivity) getActivity();
        final ExpandableListView expandableSearchListView = (ExpandableListView) currentView.findViewById(R.id.trips_search_results_list_view);
        currentSearchQuery = mCallBackGetSearchQuery.onGetSearchQuery();
        activity.supportInvalidateOptionsMenu();

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle(getResources().getString(R.string.app_name));
        actionBar.setHomeButtonEnabled(false); // disable the button
        actionBar.setDisplayHomeAsUpEnabled(false); // remove the left caret
        actionBar.setDisplayShowHomeEnabled(true);
        setHasOptionsMenu(true);

//        if(savedInstanceState != null){
//            currentTrip = savedInstanceState.getParcelable(saveTrip);
//        }

        mAdapter = new SearchResultCursorTreeAdapter(null, getActivity(), false, this);
        expandableSearchListView.setAdapter(mAdapter);
        expandableSearchListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                int type = mAdapter.getChildType(groupPosition, childPosition);

                Trip selectedTrip = null;
                Landmark selectedLandmark = null;

                switch (type) {
                    case 0:
                        selectedTrip = (Trip) v.getTag();
                        break;
                    case 1:
                        selectedLandmark = (Landmark) v.getTag();
                        Cursor tripCursor = activity.getContentResolver().query(
                                ContentUris.withAppendedId(KeepTripContentProvider.CONTENT_TRIP_ID_URI_BASE, selectedLandmark.getTripId()),
                                null,
                                null,
                                null,
                                null);
                        if (tripCursor != null) {
                            tripCursor.moveToFirst();
                            selectedTrip = new Trip(tripCursor);
                            tripCursor.close();
                        }

                        break;
                }

                if (selectedTrip != null) {
                    mSetCurrentTripCallback.onSetCurrentTrip(selectedTrip);
                    if (selectedLandmark != null) {
                        StartActivitiesUtils.startLandmarkMainActivity(getActivity(), selectedTrip, selectedLandmark.getId());
                    } else {
                        StartActivitiesUtils.startLandmarkMainActivity(getActivity(), selectedTrip);
                    }
                }

                return true;
            }
        });

        loadingSpinner = (ProgressBar) currentView.findViewById(R.id.trips_search_results_progress_bar_loading_spinner);
        loadingSpinner.setVisibility(View.VISIBLE);

        cursorLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                CursorLoader loader;

                switch (id) {
                    case -1:
                        loader = new CursorLoader(activity,
                                KeepTripContentProvider.CONTENT_SEARCH_GROUPS_URI,
                                null,
                                null,
                                null,
                                null);
                        break;
                    case 0:
                        loader = new CursorLoader(activity,
                                KeepTripContentProvider.CONTENT_TRIPS_URI,
                                null,
                                KeepTripContentProvider.Trips.TITLE_COLUMN + " like ? ",
                                new String[] { "%" + currentSearchQuery + "%" },
                                null);

                        break;

                    case 1:
                        loader = new CursorLoader(activity,
                                KeepTripContentProvider.CONTENT_LANDMARKS_URI,
                                null,
                                KeepTripContentProvider.Landmarks.TITLE_COLUMN + " like ? ",
                                new String[] { "%" + currentSearchQuery + "%" },
                                null);

                        break;
                    default:
                        loader = new CursorLoader(activity);
                }

                return loader;
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                int id = loader.getId();
                if (id != -1) {
                    mAdapter.setChildrenCursor(id, cursor);
                } else {
                    mAdapter.setGroupCursor(cursor);
                    for(int i=0; i < mAdapter.getGroupCount(); i++)
                        expandableSearchListView.expandGroup(i);
                }

                loadingSpinner.setVisibility(View.GONE);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                int id = loader.getId();
                if (id != -1) {
                    try {
                        mAdapter.setChildrenCursor(id, null);
                    } catch (NullPointerException e) {
                        Log.w(TAG, "Adapter expired, try again on the next query: "
                                + e.getMessage());
                    }
                } else {
                    try {
                        mAdapter.setGroupCursor(null);
                    } catch (NullPointerException e) {
                        Log.w(TAG, "Adapter expired, try again on the next query: "
                                + e.getMessage());
                    }
                }
            }
        };

        Loader<Cursor> loader = getLoaderManager().getLoader(-1);
        if (loader != null && !loader.isReset()) {
            getLoaderManager().restartLoader(-1, null, cursorLoaderCallbacks);
        } else {
            getLoaderManager().initLoader(-1, null, cursorLoaderCallbacks);
        }

//        getLoaderManager().initLoader(SEARCH_LOADER_ID, null, cursorLoaderCallbacks);

        return currentView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        mSetCurrentTripCallback = StartActivitiesUtils.onAttachCheckInterface(activity, OnSetCurrentTrip.class);
        mCallBackGetSearchQuery = StartActivitiesUtils.onAttachCheckInterface(activity, OnGetSearchQueryListener.class);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_trip_list_menusitem, menu);

        // Associate searchable configuration with the SearchView
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
        searchView.setMaxWidth(Integer.MAX_VALUE);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (!TextUtils.isEmpty(currentSearchQuery)) {
            String searchQuery = currentSearchQuery;
            MenuItemCompat.expandActionView(menu.findItem(R.id.search));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    updateSearchQuery(query);
                    searchView.clearFocus();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    updateSearchQuery(newText);
                    return true;
                }
            });
            searchView.setQuery(searchQuery, false);
        }

        super.onPrepareOptionsMenu(menu);
    }

    private void updateSearchQuery(String newSearchQuery) {
        if (TextUtils.isEmpty(newSearchQuery)) {

            getFragmentManager().popBackStackImmediate();
        } else {
            this.currentSearchQuery = newSearchQuery;
            if (mAdapter != null) {
                mAdapter.restartAllLoaders();
            }
            getLoaderManager().restartLoader(-1, null, cursorLoaderCallbacks);
        }
    }
}

//       listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
//                Cursor cursor = ((CursorAdapter) adapterView.getAdapter()).getCursor();
//                cursor.moveToPosition(position);
//                Trip currentTrip = new Trip(cursor);
//                mSetCurrentTripCallback.onSetCurrentTrip(currentTrip);
//
//                Activity curActivity = (Activity) view.getContext();
//                StartActivitiesUtils.startLandmarkMainActivity(curActivity, currentTrip);
//            }
//        });