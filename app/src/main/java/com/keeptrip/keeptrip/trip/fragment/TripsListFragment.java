package com.keeptrip.keeptrip.trip.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.contentProvider.KeepTripContentProvider;
import com.keeptrip.keeptrip.general.SettingsActivity;
import com.keeptrip.keeptrip.model.Landmark;
import com.keeptrip.keeptrip.model.Trip;
import com.keeptrip.keeptrip.trip.activity.TripCreateActivity;
import com.keeptrip.keeptrip.trip.adapter.SearchResultCursorTreeAdapter;
import com.keeptrip.keeptrip.trip.interfaces.OnSetCurrentTrip;
import com.keeptrip.keeptrip.utils.AnimationUtils;
import com.keeptrip.keeptrip.utils.DateUtils;
import com.keeptrip.keeptrip.utils.DbUtils;
import com.keeptrip.keeptrip.utils.ImageUtils;
import com.keeptrip.keeptrip.utils.NotificationUtils;
import com.keeptrip.keeptrip.utils.StartActivitiesUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TripsListFragment extends Fragment implements  SearchResultCursorTreeAdapter.OnGetChildrenCursorListener {

    // tag
    public static final String TAG = TripsListFragment.class.getSimpleName();

    // Static final const
    static final int NEW_TRIP_CREATED = 1;
    static final int TRIP_DIALOG = 0;
    static final String TRIP_DIALOG_OPTION = "TRIP_DIALOG_OPTION";
    static final String NEW_CREATED_TRIP = "NEW_CREATED_TRIP";

    // Loaders Id
    static final int TRIP_LOADER_ID = 2;
    static final int SEARCH_MAIN_LOADER_ID = -1;
    static final int SEARCH_TRIP_LOADER_ID = 0;
    static final int SEARCH_LANDMARK_LOADER_ID = 1;

    private AlertDialog deleteTripDialogConfirm;
    private CursorAdapter cursorAdapter;
    private ProgressBar loadingSpinner;
    private ProgressBar searchLoadingSpinner;
    private ImageView arrowWhenNoTripsImageView;
    private TextView messageWhenNoTripsTextView;
    private ViewSwitcher fragmentViewSwitcher;
    private SearchView searchView;

    private OnSetCurrentTrip mSetCurrentTripCallback;

    private Trip currentTrip;

    private String saveTrip = "saveTrip";
    private String saveCurrentSearchQuery = "saveCurrentSearchQuery";
    private String currentSearchQuery;
    private boolean isFirstSearch;
    private LoaderManager.LoaderCallbacks<Cursor> cursorSearchLoaderCallbacks;
    private SearchResultCursorTreeAdapter searchAdapter;

    // searchLoader
    private boolean isLandmarkLoadFinished;
    private boolean isTripLoadFinished;

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null){
            currentTrip = savedInstanceState.getParcelable(saveTrip);
            currentSearchQuery = savedInstanceState.getString(saveCurrentSearchQuery);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View currentView = inflater.inflate(R.layout.fragment_trips_list, container, false);
        final AppCompatActivity activity = (AppCompatActivity) getActivity();
        loadingSpinner = (ProgressBar) currentView.findViewById(R.id.trips_main_progress_bar_loading_spinner);
        searchLoadingSpinner = (ProgressBar) currentView.findViewById(R.id.trips_search_progress_bar_loading_spinner);
        fragmentViewSwitcher = (ViewSwitcher) currentView.findViewById(R.id.trips_list_view_switcher);

        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setTitle(getResources().getString(R.string.app_name));
        actionBar.setHomeButtonEnabled(false); // disable the button
        actionBar.setDisplayHomeAsUpEnabled(false); // remove the left caret
        actionBar.setIcon(R.drawable.toolbar_logo_xml);
        actionBar.setDisplayShowHomeEnabled(true);
        setHasOptionsMenu(true);

        if (savedInstanceState != null){
            ((AppCompatActivity) getActivity()).supportInvalidateOptionsMenu();
        }

        onCreateViewTripList(activity, currentView);
        onCreateViewSearch(activity, currentView);

        return currentView;
    }

    private void onCreateViewTripList(final AppCompatActivity activity, View currentView) {
        final ListView listView = (ListView) currentView.findViewById(R.id.trips_list_view);

        cursorAdapter = new CursorAdapter(activity, null, true) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
                return LayoutInflater.from(context).inflate(R.layout.trip_list_view_row_layout, viewGroup, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                TextView title = (TextView) view.findViewById(R.id.landmark_map_card_title_text_view);
                TextView location = (TextView) view.findViewById(R.id.landmark_map_card_location_text_view);
                TextView date = (TextView) view.findViewById(R.id.landmark_map_card_date_text_view);
                ImageView coverPhoto = (ImageView) view.findViewById(R.id.landmark_map_card_cover_photo_view);

                Trip currentTrip = new Trip(cursor);

                title.setText(currentTrip.getTitle());
                location.setText(currentTrip.getPlace());

                String imagePath = currentTrip.getPicture();
                ImageUtils.updatePhotoImageViewByPath(context, imagePath, coverPhoto);

                SimpleDateFormat sdf = DateUtils.getTripListDateFormat();
                Date startDate = currentTrip.getStartDate();
                String stringStartDate = startDate == null ? "" : sdf.format(startDate);
                Date endDate = currentTrip.getEndDate();
                String stringEndDate = endDate == null ? "" : sdf.format(endDate);
                date.setText(stringStartDate + " - " + stringEndDate);
            }
        };

        listView.setAdapter(cursorAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor cursor = ((CursorAdapter) adapterView.getAdapter()).getCursor();
                cursor.moveToPosition(position);
                currentTrip = new Trip(cursor);
                mSetCurrentTripCallback.onSetCurrentTrip(currentTrip);

                Activity curActivity = (Activity) view.getContext();
                StartActivitiesUtils.startLandmarkMainActivity(curActivity, currentTrip);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor cursor = ((CursorAdapter) adapterView.getAdapter()).getCursor();
                cursor.moveToPosition(position);
                currentTrip = new Trip(cursor);
                mSetCurrentTripCallback.onSetCurrentTrip(currentTrip);

                DialogFragment optionsDialog = new TripOptionsDialogFragment();
                optionsDialog.setTargetFragment(TripsListFragment.this, TRIP_DIALOG);
                optionsDialog.show(getFragmentManager(), "tripOptions");

                return true;
            }
        });

        loadingSpinner.setVisibility(View.VISIBLE);

        arrowWhenNoTripsImageView = (ImageView) currentView.findViewById(R.id.trips_add_trips_when_empty_arrow_image_view);
        messageWhenNoTripsTextView = (TextView) currentView.findViewById(R.id.trips_add_trips_when_empty_text_view);

        LoaderManager.LoaderCallbacks<Cursor> cursorLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(getActivity(),
                        KeepTripContentProvider.CONTENT_TRIPS_URI,
                        null,
                        null,
                        null,
                        null);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                loadingSpinner.setVisibility(View.GONE);
                onCursorChange(cursor);

                // Swap the new cursor in. (The framework will take care of closing the
                // old cursor once we return.)
                cursorAdapter.swapCursor(cursor);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                // This is called when the last Cursor provided to onLoadFinished()
                // above is about to be closed.  We need to make sure we are no
                // longer using it.
                cursorAdapter.swapCursor(null);
            }
        };

        getLoaderManager().initLoader(TRIP_LOADER_ID, null, cursorLoaderCallbacks);

        FloatingActionButton addTripFab = (FloatingActionButton) currentView.findViewById(R.id.trips_main_floating_action_button);
        addTripFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TripCreateActivity.class);
                startActivityForResult(intent, NEW_TRIP_CREATED);
            }
        });

        initDialogs();
    }

    private void onCreateViewSearch(final AppCompatActivity activity, View currentView) {
        final ExpandableListView expandableSearchListView = (ExpandableListView) currentView.findViewById(R.id.trips_search_results_list_view);

        searchAdapter = new SearchResultCursorTreeAdapter(null, getActivity(), false, this, currentSearchQuery);
        expandableSearchListView.setAdapter(searchAdapter);
        expandableSearchListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                int type = searchAdapter.getChildType(groupPosition, childPosition);

                Trip selectedTrip = null;
                Landmark selectedLandmark = null;

                switch (type) {
                    case SEARCH_TRIP_LOADER_ID:
                        selectedTrip = (Trip) v.getTag();
                        break;
                    case SEARCH_LANDMARK_LOADER_ID:
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

        cursorSearchLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                CursorLoader loader;

                switch (id) {
                    case SEARCH_MAIN_LOADER_ID:
                        loader = new CursorLoader(activity,
                                KeepTripContentProvider.CONTENT_SEARCH_GROUPS_URI,
                                null,
                                null,
                                null,
                                null);
                        break;
                    case SEARCH_TRIP_LOADER_ID:
                        loader = new CursorLoader(activity,
                                KeepTripContentProvider.CONTENT_TRIPS_URI,
                                null,
                                KeepTripContentProvider.Trips.TITLE_COLUMN + " like ? ",
                                new String[] { "%" + currentSearchQuery + "%" },
                                null);
                        break;
                    case SEARCH_LANDMARK_LOADER_ID:
                        loader = new CursorLoader(activity,
                                KeepTripContentProvider.CONTENT_SEARCH_LANDMARK_RESULTS_URI,
                                null,
                                KeepTripContentProvider.SearchLandmarkResults.LANDMARK_TITLE_COLUMN + " like ? ",
                                new String[] { "%" + currentSearchQuery + "%" },
                                null);
                        break;
                    default:
                        loader = new CursorLoader(activity);
                }

                updateSearchLoadersStatus(id, false);

                return loader;
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                int id = loader.getId();
                if (id != SEARCH_MAIN_LOADER_ID) {
                    searchAdapter.setChildrenCursor(id, cursor);

                    updateSearchLoadersStatus(id, true);
                } else {
                    searchAdapter.setGroupCursor(cursor);
                    if (isFirstSearch) {
                        for(int i=0; i < searchAdapter.getGroupCount(); i++) {
                            expandableSearchListView.expandGroup(i);
                        }

                        isFirstSearch = false;
                    }
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                int id = loader.getId();
                if (id != SEARCH_MAIN_LOADER_ID) {
                    try {
                        searchAdapter.setChildrenCursor(id, null);
                    } catch (NullPointerException e) {
                        Log.w(TAG, "Adapter expired, try again on the next query: "
                                + e.getMessage());
                    }
                } else {
                    try {
                        searchAdapter.setGroupCursor(null);
                    } catch (NullPointerException e) {
                        Log.w(TAG, "Adapter expired, try again on the next query: "
                                + e.getMessage());
                    }
                }
            }
        };
    }


    //------------On Activity Result--------------//
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request it is that we're responding to
        switch (requestCode) {
            case NEW_TRIP_CREATED:

            // Make sure the request was successful
                if (resultCode == Activity.RESULT_OK) {
                    currentTrip = data.getParcelableExtra(NEW_CREATED_TRIP);
                }
                break;
            case TRIP_DIALOG:
                if (resultCode == Activity.RESULT_OK) {
                    TripOptionsDialogFragment.DialogOptions whichOptionEnum = (TripOptionsDialogFragment.DialogOptions) data.getSerializableExtra(TRIP_DIALOG_OPTION);
                    switch (whichOptionEnum) {
                        case EDIT:
                            onUpdateTripDialog();
                            break;
                        case DELETE:
                            String title = getResources().getString(R.string.trip_delete_warning_dialog_title) + " " + "<b>" + currentTrip.getTitle() + "</b>";
                            deleteTripDialogConfirm.setTitle(Html.fromHtml(title));
                            deleteTripDialogConfirm.show();
                            break;
                        case VIEW:
                            TripViewDetailsFragment tripViewFragment = new TripViewDetailsFragment();
                            Bundle bundle = new Bundle();
                            bundle.putBoolean(TripViewDetailsFragment.FROM_TRIPS_LIST, true);
                            tripViewFragment.setArguments(bundle);
                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                            transaction.replace(R.id.trip_main_fragment_container, tripViewFragment, TripViewDetailsFragment.TAG);
                            transaction.addToBackStack(null);
                            transaction.commit();
                            break;
                    }
                    break;
                }
        }
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        mSetCurrentTripCallback = StartActivitiesUtils.onAttachCheckInterface(activity, OnSetCurrentTrip.class);
    }


    //    @Override
    public void onUpdateTripDialog() {
        TripUpdateFragment updateFragment = new TripUpdateFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.trip_main_fragment_container, updateFragment, TripUpdateFragment.TAG);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void onDeleteTripDialog() {

        // erase the notification if last trip
        Trip latestTrip = DbUtils.getLastTrip(getActivity());
        if(latestTrip != null && (latestTrip.getId() == currentTrip.getId())){
//            NotificationManager mNotificationManager =
//                    (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
//            mNotificationManager.cancel(NotificationUtils.NOTIFICATION_ID);
            NotificationUtils.cancelNotification(getActivity());
        }

        // delete the trip
        getActivity().getContentResolver().delete(
                ContentUris.withAppendedId(KeepTripContentProvider.CONTENT_TRIP_ID_URI_BASE, currentTrip.getId()),
                null,
                null);

        // delete all the landmarks of the trip
        getActivity().getContentResolver().delete(
                KeepTripContentProvider.CONTENT_LANDMARKS_URI,
                KeepTripContentProvider.Landmarks.TRIP_ID_COLUMN + " =? ",
                new String[]{Integer.toString(currentTrip.getId())});
    }

    private void initDialogs() {
        // Use the Builder class for convenient dialog construction
        deleteTripDialogConfirm = new AlertDialog.Builder(getActivity())
                //set message, title, and icon
                .setMessage(getResources().getString(R.string.trip_delete_warning_dialog_message))
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

    private void onCursorChange(Cursor cursor) {
        if (cursor.getCount() == 0) {
            arrowWhenNoTripsImageView.setVisibility(View.VISIBLE);
            arrowWhenNoTripsImageView.setAnimation(AnimationUtils.getArrowListEmptyAnimation());
            messageWhenNoTripsTextView.setVisibility(View.VISIBLE);
        } else {
            arrowWhenNoTripsImageView.setAnimation(null);
            arrowWhenNoTripsImageView.setVisibility(View.GONE);
            messageWhenNoTripsTextView.setVisibility(View.GONE);
        }
    }

    //---------------------save-------------------//
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(saveTrip, currentTrip);
        outState.putString(saveCurrentSearchQuery, currentSearchQuery);
    }

    ////////////////////////////////
    //Toolbar functions
    ////////////////////////////////
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_trip_list_menusitem, menu);

        // Associate searchable configuration with the SearchView
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(new TripOnQueryTextListener());

        EditText searchEditText = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        if(searchEditText != null) {
            searchEditText.setTextColor(Color.WHITE);
        }
    }

    private class TripOnQueryTextListener implements SearchView.OnQueryTextListener {
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

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        if (!TextUtils.isEmpty(currentSearchQuery)) {
            String searchQuery = currentSearchQuery;

            // set to null in order to avoid text change when expandActionView
            searchView.setOnQueryTextListener(null);
            MenuItemCompat.expandActionView(menu.findItem(R.id.search));

            searchView.setOnQueryTextListener(new TripOnQueryTextListener());
            searchView.setQuery(searchQuery, true);
        }

        super.onPrepareOptionsMenu(menu);
    }

    private void updateSearchQuery(String newText) {

        // use inorder to expand all view for the first search
        if (TextUtils.isEmpty(currentSearchQuery) && !TextUtils.isEmpty(newText)) {
            isFirstSearch = true;
        }

        currentSearchQuery = newText;
        searchAdapter.setFilter(currentSearchQuery);
        final int TRIP_LIST_VIEW_NUMBER = 0;
        final int SEARCH_VIEW_NUMBER = 1;

        if (TextUtils.isEmpty(newText)) {
            fragmentViewSwitcher.setDisplayedChild(TRIP_LIST_VIEW_NUMBER);
            getLoaderManager().destroyLoader(SEARCH_MAIN_LOADER_ID);
            getLoaderManager().destroyLoader(SEARCH_LANDMARK_LOADER_ID);
            getLoaderManager().destroyLoader(SEARCH_TRIP_LOADER_ID);
            searchLoadingSpinner.setVisibility(View.GONE);
        } else {
            searchLoadingSpinner.setVisibility(View.VISIBLE);
            fragmentViewSwitcher.setDisplayedChild(SEARCH_VIEW_NUMBER);
            Loader<Cursor> loader = getLoaderManager().getLoader(SEARCH_MAIN_LOADER_ID);
            if (loader != null) {
                getLoaderManager().restartLoader(SEARCH_MAIN_LOADER_ID, null, cursorSearchLoaderCallbacks);
                onGetChildrenCursorListener(SEARCH_LANDMARK_LOADER_ID);
                onGetChildrenCursorListener(SEARCH_TRIP_LOADER_ID);
            } else {
                getLoaderManager().initLoader(SEARCH_MAIN_LOADER_ID, null, cursorSearchLoaderCallbacks);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.settings_item:
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onGetChildrenCursorListener(int groupId) {
        Loader<Cursor> loader = getLoaderManager().getLoader(groupId);
        if (loader != null) {
            getLoaderManager().restartLoader(groupId, null, cursorSearchLoaderCallbacks);
        } else {
            getLoaderManager().initLoader(groupId, null, cursorSearchLoaderCallbacks);
        }
    }

    private void updateSearchLoadersStatus(int loaderId, boolean isFinish) {
        switch (loaderId) {
            case SEARCH_TRIP_LOADER_ID:
                isTripLoadFinished = isFinish;
                break;

            case SEARCH_LANDMARK_LOADER_ID:
                isLandmarkLoadFinished = isFinish;
                break;

            default:
                return;
        }

        if (!isTripLoadFinished || !isLandmarkLoadFinished) {
            searchLoadingSpinner.setVisibility(View.VISIBLE);
        } else {
            searchLoadingSpinner.setVisibility(View.GONE);
        }
    }
}

