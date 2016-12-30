package com.keeptrip.keeptrip.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.keeptrip.keeptrip.R;

public class NoTripsDialogFragment extends DialogFragment {

    // tag
    public static final String TAG = NoTripsDialogFragment.class.getSimpleName();

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
                       // onClickHandle(DialogOptions.DONE.ordinal());
                    }
                })
                .setNegativeButton(R.string.description_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        onClickHandle(DialogOptions.CANCEL.ordinal());
                    }
                });
        final AlertDialog noTripsDialog = noTripsDialogBuilder.create();
        setCancelable(false);
        noTripsDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                //TODO: CHECK IF NEED TO ADD CANCEL LISTENER
                Button doneButton = noTripsDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                doneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(onClickHandle(DialogOptions.DONE.ordinal())){
                            noTripsDialog.dismiss();
                        }
                    }
                });
            }
        });

        return noTripsDialog;
    }



    private boolean onClickHandle(int whichButton){
        boolean res = false;
        DialogOptions whichOptionEnum = DialogOptions.values()[whichButton];
        if(whichOptionEnum == DialogOptions.DONE && dialogEditText.getText().toString().trim().isEmpty()){
            dialogEditText.requestFocus();
            dialogEditText.setError(getResources().getString(R.string.trip_no_title_error_message));
        }
        else {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(NO_TRIPS_DIALOG_OPTION, whichOptionEnum);
            resultIntent.putExtra(TITLE_FROM_NO_TRIPS_DIALOG, dialogEditText.getText().toString());
            getTargetFragment().onActivityResult(getTargetRequestCode(), getActivity().RESULT_OK, resultIntent);
            res = true;
        }
        return res;
    }
}
