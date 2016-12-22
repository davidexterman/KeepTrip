package com.keeptrip.keeptrip.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.keeptrip.keeptrip.R;

public class NoTripsDialogFragment extends DialogFragment {
    public static final String NO_TRIPS_DIALOG_OPTION = "NO_TRIPS_DIALOG_OPTION";
    public static final String TITLE_FROM_NO_TRIPS_DIALOG = "TITLE_FROM_NO_TRIPS_DIALOG";

    private EditText dialogEditText;

    public enum DialogOptions{
        DONE,
        CANCEL
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.no_trips_dialog, null);

        dialogEditText = (EditText) dialogView.findViewById(R.id.no_trips_edit_text);

        AlertDialog.Builder noTripsDialogBuilder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.no_trips_dialog_title)
                .setView(dialogView)
                .setPositiveButton(R.string.description_dialog_done, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        onClickHandle(DialogOptions.DONE.ordinal());
                    }
                })
                .setNegativeButton(R.string.description_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        onClickHandle(DialogOptions.CANCEL.ordinal());
                    }
                });
        AlertDialog noTripsDialog = noTripsDialogBuilder.create();
        return noTripsDialog;
    }

    private void onClickHandle(int whichButton){
        DialogOptions whichOptionEnum = DialogOptions.values()[whichButton];
        Intent resultIntent = new Intent();
        resultIntent.putExtra(NO_TRIPS_DIALOG_OPTION, whichOptionEnum);
        resultIntent.putExtra(TITLE_FROM_NO_TRIPS_DIALOG, dialogEditText.getText().toString());
        getTargetFragment().onActivityResult(getTargetRequestCode(), getActivity().RESULT_OK, resultIntent);
    }
}
