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
import com.keeptrip.keeptrip.model.Trip;
import com.keeptrip.keeptrip.utils.DateUtils;
import com.keeptrip.keeptrip.utils.DbUtils;

public class WidgetLocationProvider extends AppWidgetProvider {

    public static String ADD_LOCATION_LANDMARK = "ADD_LOCATION_LANDMARK";
    private static PendingIntent pendingIntent;
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        final int count = appWidgetIds.length;
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.widget_location);

        // Create an Intent to launch WidgetLocationActivity
        Intent intent = new Intent(context, WidgetLocationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        for (int i = 0; i < count; i++) {
            int widgetId = appWidgetIds[i];

            remoteViews.setOnClickPendingIntent(R.id.widget_image_button, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }
}
