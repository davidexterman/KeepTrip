package com.keeptrip.keeptrip.widget;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.keeptrip.keeptrip.R;

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
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK );
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        for (int i = 0; i < count; i++) {
            int widgetId = appWidgetIds[i];

            remoteViews.setOnClickPendingIntent(R.id.location_widget_image_button, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }
}
