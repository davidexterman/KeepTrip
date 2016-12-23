package com.keeptrip.keeptrip.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.keeptrip.keeptrip.model.Trip;

public class SharedPreferencesUtils {

    public static final String SAVE_LAST_USED_TRIP = "SAVE_LAST_USED_TRIP";
    private static final String SHARED_PREFERENCES__NAME = "SHARED_PREFERENCES__NAME";

    public static void saveLastUsedTrip(Context appContext, Trip trip){
        SharedPreferences sharedPref = appContext.getSharedPreferences(SHARED_PREFERENCES__NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = sharedPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(trip);
        prefsEditor.putString(SAVE_LAST_USED_TRIP, json);
        prefsEditor.commit();
    }

    public static Trip getLastUsedTrip(Context appContext){
        SharedPreferences sharedPref = appContext.getSharedPreferences(SHARED_PREFERENCES__NAME, Context.MODE_PRIVATE);

        Gson gson = new Gson();
        String json = sharedPref.getString(SAVE_LAST_USED_TRIP, "");
        Trip trip = (Trip) gson.fromJson(json, Trip.class);

        return trip;
    }
}
