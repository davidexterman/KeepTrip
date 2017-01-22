package com.keeptrip.keeptrip.utils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
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

    public static Trip getLastTrip(Context context){
        Trip lastTrip = null;
        Cursor cursor = context.getContentResolver().query(KeepTripContentProvider.CONTENT_TRIPS_URI, null, null,
                null, " LIMIT 1");
        if(cursor.moveToFirst()) {
            lastTrip = new Trip(cursor);
        }
        return lastTrip;
    }

    public static String getWhereClause(String[] columns) {
        String whereClause = "";

        for (String col : columns) {
            if (!whereClause.isEmpty()) {
                whereClause += " OR ";
            }

            whereClause += col + " like ? ";
        }

        return whereClause;
    }
}
