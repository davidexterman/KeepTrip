package com.keeptrip.keeptrip.utils;

import android.content.Context;
import android.content.SharedPreferences;


public class SharedPreferencesUtils {

    public static final String SAVE_NOTIFICATIONS_STATE = "SAVE_NOTIFICATIONS_STATE";
    private static final String SHARED_PREFERENCES__NAME = "SHARED_PREFERENCES__NAME";

    public static void saveNotificationsState(Context appContext, boolean notificationState){
        SharedPreferences sharedPref = appContext.getSharedPreferences(SHARED_PREFERENCES__NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = sharedPref.edit();
        prefsEditor.putBoolean(SAVE_NOTIFICATIONS_STATE, notificationState);
        prefsEditor.commit();
    }

    public static boolean getNotificationsState(Context appContext){
        SharedPreferences sharedPref = appContext.getSharedPreferences(SHARED_PREFERENCES__NAME, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(SAVE_NOTIFICATIONS_STATE, true);
    }
}
