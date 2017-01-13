package com.keeptrip.keeptrip.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.app.TaskStackBuilder;

import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.landmark.activity.LandmarkMainActivity;

/**
 * Created by david on 1/10/2017.
 */

public class NotificationUtils {

    public static final String NOTIFICATION_ACTION_STR = "NOTIFICATION_ACTION";
    public static final int NOTIFICATION_ACTION = 1500;
    public static final int NOTIFICATION_ID = 2000;

    public static void initNotification(Context context, String textTitle){

        // Creates an explicit intent for an Activity in your app

        Intent resultIntent = new Intent(context, LandmarkMainActivity.class);
        resultIntent.setAction(NOTIFICATION_ACTION_STR);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(LandmarkMainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        NOTIFICATION_ACTION,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(
                        R.drawable.ic_add_black_24dp,
                        context.getString(R.string.notification_add_landmark),
                        resultPendingIntent)
                        .build();

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_tree_icon)
                        .setLargeIcon(ImageUtils.getBitmap(context, R.drawable.ic_logo))
                        .setColor(context.getResources().getColor(R.color.notificationBackground))
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(context.getString(R.string.notification_added_to_trip_message, textTitle))
                        .addAction(action)
                        .setOngoing(true);

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

// mId allows you to update the notification later on.
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
