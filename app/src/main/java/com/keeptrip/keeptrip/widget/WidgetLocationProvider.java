package com.keeptrip.keeptrip.widget;


import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.location.Location;

import com.keeptrip.keeptrip.contentProvider.KeepTripContentProvider;
import com.keeptrip.keeptrip.model.Landmark;
import com.keeptrip.keeptrip.utils.DateUtils;

public class WidgetLocationProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
//
//        Landmark newLandmark = new Landmark(22, "test1", "", DateUtils.getDateOfToday(),
//                "", new Location(""), "", 0);
//
//        // Insert data to DataBase
//        context.getContentResolver().insert(
//                KeepTripContentProvider.CONTENT_LANDMARKS_URI,
//                newLandmark.landmarkToContentValues());
    }
}
