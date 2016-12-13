package com.keeptrip.keeptrip;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.ListView;

public class LandmarkOptionsDialogFragment extends DialogFragment {
    private String[] dialogOptionsArray;
    private AlertDialog optionsDialog;
    private Landmark currentLandmark;
    public static final String CUR_LANDMARK_PARAM = "CUR_LANDMARK";
  //  private Trip currentTrip;

    public enum DialogOptions{
        EDIT,
        DELETE
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialogOptionsArray = getResources().getStringArray(R.array.landmarks_settings_dialog_options);
        Bundle mArgs = getArguments();
        currentLandmark = mArgs.getParcelable(CUR_LANDMARK_PARAM);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder optionsDialogBuilder = new AlertDialog.Builder(getActivity());
        optionsDialogBuilder.setItems(dialogOptionsArray, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                LandmarkOptionsDialogFragment.DialogOptions whichOptionEnum = LandmarkOptionsDialogFragment.DialogOptions.values()[which];
                Intent resultIntent = new Intent();
                resultIntent.putExtra(LandmarksListFragment.LANDMARK_DIALOG_OPTION, whichOptionEnum);
                getTargetFragment().onActivityResult(getTargetRequestCode(), getActivity().RESULT_OK, resultIntent);
            }
        });
        optionsDialogBuilder.setTitle(currentLandmark.getTitle());

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