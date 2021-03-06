package com.keeptrip.keeptrip.trip.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.ListView;

import com.keeptrip.keeptrip.R;

public class TripOptionsDialogFragment extends DialogFragment {

    // tag
    public static final String TAG = TripOptionsDialogFragment.class.getSimpleName();

    public static final String CUR_TRIP_PARAM = "CUR_TRIP";

    public enum DialogOptions{
        VIEW,
        EDIT,
        DELETE
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String[] dialogOptionsArray = getResources().getStringArray(R.array.trips_settings_dialog_options);
//        Bundle mArgs = getArguments();
//        String currentTripTitle = mArgs.getString(CUR_TRIP_PARAM);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder optionsDialogBuilder = new AlertDialog.Builder(getActivity());
        optionsDialogBuilder.setItems(dialogOptionsArray, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                TripOptionsDialogFragment.DialogOptions whichOptionEnum = TripOptionsDialogFragment.DialogOptions.values()[which];
                Intent resultIntent = new Intent();
                resultIntent.putExtra(TripsListFragment.TRIP_DIALOG_OPTION, whichOptionEnum);
                getTargetFragment().onActivityResult(getTargetRequestCode(), getActivity().RESULT_OK, resultIntent);
            }
        });
     //   optionsDialogBuilder.setTitle(currentTripTitle);
        optionsDialogBuilder.setTitle(R.string.trip_options_dialog_title);
        AlertDialog optionsDialog = optionsDialogBuilder.create();
        ListView listView = optionsDialog.getListView();

        //TODO: remove divider at the end

        listView.setDivider(new ColorDrawable(ContextCompat.getColor
                (getActivity(), R.color.toolBarLineBackground))); // set color
        listView.setDividerHeight(2); // set height
        // Create the AlertDialog object and return it
        listView.setFooterDividersEnabled(false);
        return optionsDialog;
    }
}
