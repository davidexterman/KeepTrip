package com.keeptrip.keeptrip.widget;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.contentProvider.KeepTripContentProvider;
import com.keeptrip.keeptrip.model.Landmark;
import com.keeptrip.keeptrip.utils.DateUtils;
import com.keeptrip.keeptrip.utils.DbUtils;

public class WidgetLocationProvider extends AppWidgetProvider {

    public static String ADD_LOCATION_LANDMARK = "ADD_LOCATION_LANDMARK";
    private static PendingIntent pendingIntent;
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

        final int count = appWidgetIds.length;

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.widget_location);

        //TODO: MAKE SURE IT'S O.K TO INIT HERE
        Intent intent = new Intent(context, WidgetLocationProvider.class);
        intent.setAction(ADD_LOCATION_LANDMARK);
        pendingIntent = PendingIntent.getBroadcast(context,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        for (int i = 0; i < count; i++) {
            int widgetId = appWidgetIds[i];

     //       intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            remoteViews.setOnClickPendingIntent(R.id.widget_image_button, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

   //     AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        if (intent.getAction().equals(ADD_LOCATION_LANDMARK)) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
//            int viewIndex = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
//
          //  if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                Landmark newLandmark = new Landmark(DbUtils.getLastTrip(context).getId(), "test4", "", DateUtils.getDateOfToday(),
                        "", new Location(""), "", 0);

                // Insert data to DataBase
                context.getContentResolver().insert(
                        KeepTripContentProvider.CONTENT_LANDMARKS_URI,
                        newLandmark.landmarkToContentValues());

                Toast.makeText(context, context.getResources().getString(R.string.toast_landmark_added_message_success), Toast.LENGTH_SHORT).show();
            }

   //     }
        super.onReceive(context, intent);
    }
}
