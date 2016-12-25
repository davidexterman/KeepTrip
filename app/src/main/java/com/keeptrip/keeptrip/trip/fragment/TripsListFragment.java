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
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.contentProvider.KeepTripContentProvider;
import com.keeptrip.keeptrip.model.Trip;
import com.keeptrip.keeptrip.trip.activity.TripCreateActivity;
import com.keeptrip.keeptrip.utils.AnimationUtils;
import com.keeptrip.keeptrip.utils.DateFormatUtils;
import com.keeptrip.keeptrip.utils.ImageUtils;
import com.keeptrip.keeptrip.utils.StartActivitiesUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TripsListFragment extends Fragment {

    // Static final const
    static final int NEW_TRIP_CREATED = 1;
    static final String NEW_TRIP_ID = "NEW_TRIP_ID";
    static final int TRIP_DIALOG = 0;
    static final String TRIP_DIALOG_OPTION = "TRIP_DIALOG_OPTION";
    static final int TRIP_LOADER_ID = 1;
    static final String NEW_TRIP_TITLE = "NEW_TRIP_TITLE";
    static final String NEW_CREATED_TRIP = "NEW_CREATED_TRIP";


    private AlertDialog deleteTripDialogConfirm;
    private CursorAdapter cursorAdapter;
    private ProgressBar loadingSpinner;
    private ImageView arrowWhenNoTripsImageView;
    private TextView messageWhenNoTripsTextView;

    private OnSetCurrentTrip mSetCurrentTripCallback;
    private Trip currentTrip;

    private String saveTrip = "saveTrip";

    public interface OnSetCurrentTrip {
        void onSetCurrentTrip(Trip trip);
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View currentView = inflater.inflate(R.layout.fragment_trips_list, container, false);
        final Activity activity = getActivity();
        final ListView listView = (ListView) currentView.findViewById(R.id.trips_list_view);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle(getResources().getString(R.string.app_name));
        actionBar.setHomeButtonEnabled(false); // disable the button
        actionBar.setDisplayHomeAsUpEnabled(false); // remove the left caret
//        actionBar.setIcon(R.mipmap.logo);
        actionBar.setIcon(R.mipmap.logo);
        actionBar.setDisplayShowHomeEnabled(true);

        if(savedInstanceState != null){
            currentTrip = savedInstanceState.getParcelable(saveTrip);
        }

        cursorAdapter = new CursorAdapter(activity, null, true) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
                return LayoutInflater.from(context).inflate(R.layout.trip_list_view_row_layout, viewGroup, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                TextView title = (TextView) view.findViewById(R.id.trip_card_title_text_view);
                TextView location = (TextView) view.findViewById(R.id.trip_card_location_text_view);
                TextView date = (TextView) view.findViewById(R.id.trip_card_date_text_view);
                ImageView coverPhoto = (ImageView) view.findViewById(R.id.trip_card_cover_photo_view);

                Trip currentTrip = new Trip(cursor);

                title.setText(currentTrip.getTitle());
                location.setText(currentTrip.getPlace());

                String imagePath = currentTrip.getPicture();
                ImageUtils.updatePhotoImageViewByPath(context, imagePath, coverPhoto);

                //SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
                SimpleDateFormat sdf = DateFormatUtils.getTripListDateFormat();
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

        loadingSpinner = (ProgressBar) currentView.findViewById(R.id.trips_main_progress_bar_loading_spinner);
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
        return currentView;
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

                   // SharedPreferencesUtils.saveLastUsedTrip(getActivity().getApplicationContext(), currentTrip);

                    StartActivitiesUtils.startLandmarkMainActivity(getActivity(), currentTrip);
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
                            bundle.putBoolean(tripViewFragment.FROM_TRIPS_LIST, true);
                            tripViewFragment.setArguments(bundle);
                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                            transaction.replace(R.id.trip_main_fragment_container, tripViewFragment);
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
        try {
            mSetCurrentTripCallback = (OnSetCurrentTrip) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SetCurrentTrip");
        }
    }


    //    @Override
    public void onUpdateTripDialog() {
        TripUpdateFragment updateFragment = new TripUpdateFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.trip_main_fragment_container, updateFragment, "TRIP_UPDATE_FRAGMENT");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void onDeleteTripDialog() {
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
                //     .setTitle(getResources().getString(R.string.trip_delete_warning_dialog_title))
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
    }

}

