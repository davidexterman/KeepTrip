package com.keeptrip.keeptrip;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.ListView;

/**
 * Created by omussel on 12/10/2016.
 */

public class TripOptionsDialogFragment extends DialogFragment {
    private String[] dialogOptionsArray;
    private AlertDialog optionsDialog;
    public static final String CUR_TRIP_PARAM = "CUR_TRIP";
    private Trip currentTrip;
    private enum DialogOptions{
        EDIT,
        DELETE
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialogOptionsArray = getResources().getStringArray(R.array.trips_settings_dialog_options);
        Bundle mArgs = getArguments();
        currentTrip = mArgs.getParcelable(CUR_TRIP_PARAM);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder optionsDialogBuilder = new AlertDialog.Builder(getActivity());
        optionsDialogBuilder.setItems(dialogOptionsArray, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                TripOptionsDialogFragment.DialogOptions whichOptionEnum = TripOptionsDialogFragment.DialogOptions.values()[which];
                switch (whichOptionEnum){
                    case EDIT:
                        TripUpdateFragment newFragment = new TripUpdateFragment();
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.replace(R.id.trip_main_fragment_container, newFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                        break;
                    case DELETE:
                        //TODO: add refreshing?
                        SingletonAppDataProvider.getInstance().deleteTrip(currentTrip.getId());
                        break;
                }
            }
        });
        optionsDialogBuilder.setTitle(currentTrip.getTitle());

        optionsDialog = optionsDialogBuilder.create();
        ListView listView = optionsDialog.getListView();

        //TODO: remove divider at the end
        listView.setDivider(new ColorDrawable(ContextCompat.getColor
                (getActivity(), R.color.toolBarLineBackground))); // set color
        listView.setDividerHeight(2); // set height
        // Create the AlertDialog object and return it
        return optionsDialog;
    }
}
