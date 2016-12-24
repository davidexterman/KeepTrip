package com.keeptrip.keeptrip.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.trip.fragment.TripUpdateFragment;

public class DescriptionDialogFragment extends DialogFragment {
    private String initialDescription;
    private EditText dialogEditText;

    public static final String DESCRIPTION_DIALOG_OPTION = "DESCRIPTION_DIALOG_OPTION";
    public static final String DESCRIPTION_FROM_DIALOG = "DESCRIPTION_FROM_DIALOG";

    public enum DialogOptions{
        DONE,
        CANCEL
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.description_dialog, null);

        Bundle bundle = getArguments();
        initialDescription = bundle.getString(TripUpdateFragment.initDescription);
        dialogEditText = (EditText) dialogView.findViewById(R.id.description_dialog_edit_text);
        dialogEditText.setText(initialDescription);
        dialogEditText.setSelection(dialogEditText.getText().length());

        AlertDialog.Builder descriptionDialogBuilder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.description_dialog_title)
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
        AlertDialog descriptionDialog = descriptionDialogBuilder.create();

//        descriptionDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(final DialogInterface dialog){
//                Button doneButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                Button cancelButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                doneButton.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.transparent));
//                cancelButton.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.transparent));
//            }
//        });
        return descriptionDialog;
    }

  private void onClickHandle(int whichButton){
      DialogOptions whichOptionEnum = DialogOptions.values()[whichButton];
      Intent resultIntent = new Intent();
      resultIntent.putExtra(DESCRIPTION_DIALOG_OPTION, whichOptionEnum);
      resultIntent.putExtra(DESCRIPTION_FROM_DIALOG, dialogEditText.getText().toString());
      getTargetFragment().onActivityResult(getTargetRequestCode(), getActivity().RESULT_OK, resultIntent);
  }
}
