package com.keeptrip.keeptrip.dialogs;

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
import com.keeptrip.keeptrip.trip.fragment.TripsListFragment;

public class ChangesNotSavedDialogFragment extends DialogFragment {

    public enum DialogOptions{
        BACK,
        CANCEL,
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog changesNotSavedDialogConfirm = new AlertDialog.Builder(getActivity())
                .setMessage(getResources().getString(R.string.unsaved_details_warning_dialog_message))
                .setTitle(getResources().getString(R.string.unsaved_details_warning_dialog_title))
                .setPositiveButton(getResources().getString(R.string.unsaved_details_warning_dialog_back_label), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.unsaved_details_warning_dialog_cancel_label), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        return changesNotSavedDialogConfirm;
    }
}
