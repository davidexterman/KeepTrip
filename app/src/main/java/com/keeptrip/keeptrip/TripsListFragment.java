package com.keeptrip.keeptrip;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TripsListFragment extends Fragment {
    static final int NEW_TRIP_CREATED = 1;
    static final String NEW_TRIP = "NEW_TRIP";
    static final int TRIP_DIALOG = 0;
    static final String TRIP_DIALOG_OPTION = "TRIP_DIALOG_OPTION";

    private AlertDialog deleteTripDialogConfirm;
    private int tripId;

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View currentView = inflater.inflate(R.layout.fragment_trips_list, container, false);

        final Cursor cursor = new Cursor() {
            @Override
            public int getCount() {
                return 0;
            }

            @Override
            public int getPosition() {
                return 0;
            }

            @Override
            public boolean move(int i) {
                return false;
            }

            @Override
            public boolean moveToPosition(int i) {
                return false;
            }

            @Override
            public boolean moveToFirst() {
                return false;
            }

            @Override
            public boolean moveToLast() {
                return false;
            }

            @Override
            public boolean moveToNext() {
                return false;
            }

            @Override
            public boolean moveToPrevious() {
                return false;
            }

            @Override
            public boolean isFirst() {
                return false;
            }

            @Override
            public boolean isLast() {
                return false;
            }

            @Override
            public boolean isBeforeFirst() {
                return false;
            }

            @Override
            public boolean isAfterLast() {
                return false;
            }

            @Override
            public int getColumnIndex(String s) {
                return 0;
            }

            @Override
            public int getColumnIndexOrThrow(String s) throws IllegalArgumentException {
                return 0;
            }

            @Override
            public String getColumnName(int i) {
                return null;
            }

            @Override
            public String[] getColumnNames() {
                return new String[0];
            }

            @Override
            public int getColumnCount() {
                return 0;
            }

            @Override
            public byte[] getBlob(int i) {
                return new byte[0];
            }

            @Override
            public String getString(int i) {
                return null;
            }

            @Override
            public void copyStringToBuffer(int i, CharArrayBuffer charArrayBuffer) {

            }

            @Override
            public short getShort(int i) {
                return 0;
            }

            @Override
            public int getInt(int i) {
                return 0;
            }

            @Override
            public long getLong(int i) {
                return 0;
            }

            @Override
            public float getFloat(int i) {
                return 0;
            }

            @Override
            public double getDouble(int i) {
                return 0;
            }

            @Override
            public int getType(int i) {
                return 0;
            }

            @Override
            public boolean isNull(int i) {
                return false;
            }

            @Override
            public void deactivate() {

            }

            @Override
            public boolean requery() {
                return false;
            }

            @Override
            public void close() {

            }

            @Override
            public boolean isClosed() {
                return false;
            }

            @Override
            public void registerContentObserver(ContentObserver contentObserver) {

            }

            @Override
            public void unregisterContentObserver(ContentObserver contentObserver) {

            }

            @Override
            public void registerDataSetObserver(DataSetObserver dataSetObserver) {

            }

            @Override
            public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

            }

            @Override
            public void setNotificationUri(ContentResolver contentResolver, Uri uri) {

            }

            @Override
            public Uri getNotificationUri() {
                return null;
            }

            @Override
            public boolean getWantsAllOnMoveCalls() {
                return false;
            }

            @Override
            public void setExtras(Bundle bundle) {

            }

            @Override
            public Bundle getExtras() {
                return null;
            }

            @Override
            public Bundle respond(Bundle bundle) {
                return null;
            }
        };

        ListView listView = (ListView) currentView.findViewById(R.id.trips_list_view);
        listView.setAdapter(new android.widget.CursorAdapter(this.getActivity(), cursor, true) {
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

                title.setText(cursor.getString(1)); // <-- change to title!
                location.setText(cursor.getString(2)); // <-- change to location!

                String imagePath = cursor.getString(3); // <-- change to location!
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
                String startDate = cursor.getString(5); // <-- change to startDate!
                String endDate = cursor.getString(6); // <-- change to endDate!
                date.setText(startDate + " - " + endDate);
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                cursor.move(position);
                int tripId = cursor.getInt(1); // <-- need to be tripId

                Activity curActivity = (Activity) view.getContext();

                Intent intent = new Intent(curActivity, LandmarkMainActivity.class);
                intent.putExtra(LandmarkMainActivity.TRIP_PARAM, tripId);
                curActivity.startActivity(intent);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                cursor.move(position);
                tripId = cursor.getInt(1); // <-- need to be tripId
                Bundle args = new Bundle();

                args.putInt(TripOptionsDialogFragment.CUR_TRIP_PARAM, tripId);
                DialogFragment optionsDialog = new TripOptionsDialogFragment();
                optionsDialog.setArguments(args);
                optionsDialog.setTargetFragment(TripsListFragment.this, TRIP_DIALOG);
                optionsDialog.show(getFragmentManager(), "tripOptions");

                return true;
            }
        });

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

    public void onUpdateTripDialog(){
        TripUpdateFragment updateFragment = new TripUpdateFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.trip_main_fragment_container, updateFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void onDeleteTripDialog(){
        // todo swap cursor
        SingletonAppDataProvider.getInstance().deleteTrip(currentTrip.getId());
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

