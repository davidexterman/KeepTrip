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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

import com.keeptrip.keeptrip.contentProvider.KeepTripContentProvider;
import com.keeptrip.keeptrip.landmark.activity.LandmarkMainActivity;
import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.model.Trip;
import com.keeptrip.keeptrip.trip.activity.TripCreateActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TripsListFragment extends Fragment{ //implements TripsListRowAdapter.OnTripLongPress{
    static final int NEW_TRIP_CREATED = 1;
    static final String NEW_TRIP_ID = "NEW_TRIP_ID";
    static final int TRIP_DIALOG = 0;
    static final String TRIP_DIALOG_OPTION = "TRIP_DIALOG_OPTION";
    static int loaderId = 0;
    static final String NEW_TRIP_TITLE = "NEW_TRIP_TITLE";

    private AlertDialog deleteTripDialogConfirm;
    private int currentTripId;
    private CursorAdapter adapter;
    private LoaderManager.LoaderCallbacks<Cursor> cursorLoaderCallbacks;
    private ProgressBar loadingSpinner;

    private OnSetCurrentTrip mSetCurrentTripCallback;

    private Trip currentTrip;

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

        loadingSpinner = (ProgressBar) currentView.findViewById(R.id.trips_main_progress_bar_loading_spinner);
        loadingSpinner.setVisibility(View.GONE);

        cursorLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>()
        {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args)
            {
                CursorLoader loader =
                        new CursorLoader(getActivity(),
                                KeepTripContentProvider.CONTENT_TRIPS_URI,
                                null,
                                null,
                                null,
                                null);

                return loader;
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                // Here you implement the cursor adapter
                loadingSpinner.setVisibility(View.GONE);
                adapter = new CursorAdapter(activity, cursor, true) {
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
                        if (imagePath != null && !imagePath.isEmpty()){
                            Bitmap image = null;
                            try {
                                image = BitmapFactory.decodeFile(imagePath);
                            } catch (Exception e) {
                                // ignore
                            }

                            if (image != null) { // todo: change this!
                                coverPhoto.setImageBitmap(image);
                            } else {
                                coverPhoto.setImageResource(R.drawable.default_no_image);
                            }
                        }

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
                        Date startDate = currentTrip.getStartDate();
                        String stringStartDate = startDate == null ? "" : sdf.format(startDate);
                        Date endDate = currentTrip.getEndDate();
                        String stringEndDate = endDate == null ?"" : sdf.format(endDate);
                        date.setText(stringStartDate + " - " + stringEndDate);
                    }
                };

                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        Cursor cursor = ((CursorAdapter)adapterView.getAdapter()).getCursor();
                        cursor.moveToPosition(position);
                        currentTrip = new Trip(cursor);
                        int tripId = currentTrip.getId();

                        Activity curActivity = (Activity) view.getContext();

                                Intent intent = new Intent(curActivity, LandmarkMainActivity.class);
                                intent.putExtra(LandmarkMainActivity.TRIP_ID_PARAM, tripId);
                                intent.putExtra(LandmarkMainActivity.TRIP_TITLE_PARAM, currentTrip.getTitle());

                                curActivity.startActivity(intent);
                            }
                        });
                        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                            @Override
                            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                                Cursor cursor = ((CursorAdapter)adapterView.getAdapter()).getCursor();
                                cursor.moveToPosition(position);
                                currentTrip = new Trip(cursor);
                                mSetCurrentTripCallback.onSetCurrentTrip(currentTrip);

                         //       String tripTitle = cursor.getString(cursor.getColumnIndexOrThrow(KeepTripContentProvider.Trips.TITLE_COLUMN)); // <-- //todo: change this
                              //  Bundle args = new Bundle();
                                currentTripId = currentTrip.getId();
                            //    args.putString(TripOptionsDialogFragment.CUR_TRIP_PARAM, currentTrip.getTitle());
                                DialogFragment optionsDialog = new TripOptionsDialogFragment();
                            //    optionsDialog.setArguments(args);
                                optionsDialog.setTargetFragment(TripsListFragment.this, TRIP_DIALOG);
                                optionsDialog.show(getFragmentManager(), "tripOptions");

                        return true;
                    }
                });
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader)
            {
            }
        };

        getLoaderManager().initLoader(loaderId++, null, cursorLoaderCallbacks);
        loadingSpinner.setVisibility(View.VISIBLE);

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
                int newTripId = data.getIntExtra(NEW_TRIP_ID, -1);
                String newTripTitle = data.getStringExtra(NEW_TRIP_TITLE);

                getLoaderManager().restartLoader(loaderId, null, cursorLoaderCallbacks);

                Intent intent = new Intent(getActivity(), LandmarkMainActivity.class);
                intent.putExtra(LandmarkMainActivity.TRIP_ID_PARAM, newTripId);
                intent.putExtra(LandmarkMainActivity.TRIP_TITLE_PARAM, newTripTitle);

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
//                        deleteTripDialogConfirm.setMessage(getResources().getString(R.string.trip_delete_warning_dialog_massage) +  " \"" + currentTrip.getTitle() + "\"?"); //todo:fix!  getResources().getString(R.string.trip_delete_warning_dialog_massage) + " \"" + currentTrip.getTitle() + "\"?"
                        String title = getResources().getString(R.string.trip_delete_warning_dialog_title) + " " + "<b>" + currentTrip.getTitle() + "</b>";
                        deleteTripDialogConfirm.setTitle(Html.fromHtml(title));
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


    //    @Override
    public void onUpdateTripDialog(){
        TripUpdateFragment updateFragment = new TripUpdateFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.trip_main_fragment_container, updateFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void onDeleteTripDialog(){
        getActivity().getContentResolver().delete(
                ContentUris.withAppendedId(KeepTripContentProvider.CONTENT_TRIP_ID_URI_BASE, currentTripId),
                null,
                null);
        getLoaderManager().restartLoader(loaderId, null, cursorLoaderCallbacks);
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
}
