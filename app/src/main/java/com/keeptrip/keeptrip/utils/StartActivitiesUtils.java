package com.keeptrip.keeptrip.utils;

import android.app.Activity;
import android.content.Intent;

import com.keeptrip.keeptrip.landmark.activity.LandmarkMainActivity;
import com.keeptrip.keeptrip.model.Trip;

/**
 * Created by omussel on 12/23/2016.
 */

public class StartActivitiesUtils {

    public static void startLandmarkMainActivity(Activity activity, Trip currentTrip){
        Intent intent = new Intent(activity, LandmarkMainActivity.class);
        intent.putExtra(LandmarkMainActivity.CURRENT_TRIP_PARAM, currentTrip);
        activity.startActivity(intent);
    }
}
