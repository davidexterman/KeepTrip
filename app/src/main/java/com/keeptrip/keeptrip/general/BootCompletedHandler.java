package com.keeptrip.keeptrip.general;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.keeptrip.keeptrip.model.Trip;
import com.keeptrip.keeptrip.utils.DbUtils;
import com.keeptrip.keeptrip.utils.NotificationUtils;
import com.keeptrip.keeptrip.utils.SharedPreferencesUtils;

/**
 * Created by david on 1/13/2017.
 */

public class BootCompletedHandler extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Trip latestTrip = DbUtils.getLastTrip(context);
//        if(NotificationUtils.areNotificationsEnabled(context) && latestTrip != null &&
//                !SharedPreferencesUtils.getCloseNotificationsState(context)) {
        if(latestTrip != null && SharedPreferencesUtils.getIsNotificationsWindowOpen(context)){
            NotificationUtils.initNotification(context, latestTrip.getTitle());
        }
    }
}
