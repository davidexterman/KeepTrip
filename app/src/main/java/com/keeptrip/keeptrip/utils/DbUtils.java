package com.keeptrip.keeptrip.utils;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.keeptrip.keeptrip.contentProvider.KeepTripContentProvider;
import com.keeptrip.keeptrip.model.Trip;

public class DbUtils {

    public static int addNewTrip(Activity activity, Trip newTrip){
        ContentValues contentValues = newTrip.tripToContentValues();
        Uri uri = activity.getContentResolver().insert(KeepTripContentProvider.CONTENT_TRIPS_URI, contentValues);
        return Integer.parseInt(uri.getPathSegments().get(KeepTripContentProvider.TRIPS_ID_PATH_POSITION));
    }

    public static Trip getLastTrip(Activity activity){
        Trip lastTrip = null;
        Cursor cursor = activity.getContentResolver().query(KeepTripContentProvider.CONTENT_TRIPS_URI, null, null,
                null, " LIMIT 1");
        if(cursor.moveToFirst()) {
            lastTrip = new Trip(cursor);
        }
        return lastTrip;
    }
}