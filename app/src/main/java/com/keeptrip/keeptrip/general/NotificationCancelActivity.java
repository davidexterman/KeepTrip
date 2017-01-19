package com.keeptrip.keeptrip.general;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.utils.NotificationUtils;
import com.keeptrip.keeptrip.utils.SharedPreferencesUtils;

public class NotificationCancelActivity extends Activity {

    public CheckBox dontShowAgainCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_cancel);


        //don't skip the dialog
        if(!SharedPreferencesUtils.getCancelNotificationsWarningDialogState(this)){
            createAndShowDialog();
        }
        //skip the dialog
        else {
            NotificationUtils.cancelNotification(this);
            finishAffinity();
        }
    }
    private void createAndShowDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater alertDialogBuilderInflater = LayoutInflater.from(this);
        View checkboxView = alertDialogBuilderInflater.inflate(R.layout.cancel_notification_dialog, null);

        dontShowAgainCheckBox = (CheckBox) checkboxView.findViewById(R.id.notification_cancel_checkbox);
        alertDialogBuilder.setView(checkboxView);
        alertDialogBuilder.setTitle(getResources().getString(R.string.notification_hide_dialog_title));
        alertDialogBuilder.setMessage(getResources().getString(R.string.notification_hide_dialog_message));

        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.notification_hide_dialog_yes_button), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                SharedPreferencesUtils.saveCancelNotificationsWarningDialogState(NotificationCancelActivity.this, dontShowAgainCheckBox.isChecked());
                NotificationUtils.cancelNotification(NotificationCancelActivity.this);
                SharedPreferencesUtils.saveCancelNotificationsWarningDialogState(NotificationCancelActivity.this, true);
                finishAffinity();
            }
        });

        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.notification_hide_dialog_no_button), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferencesUtils.saveCancelNotificationsWarningDialogState(NotificationCancelActivity.this, dontShowAgainCheckBox.isChecked());
                finishAffinity();
            }
        });

        alertDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finishAffinity();
            }
        });

        alertDialogBuilder.show();

    }
}

//