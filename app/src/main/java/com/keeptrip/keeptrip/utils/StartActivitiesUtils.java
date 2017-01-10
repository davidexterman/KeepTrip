package com.keeptrip.keeptrip.utils;

import android.app.Activity;
import android.content.Intent;

import com.keeptrip.keeptrip.landmark.activity.LandmarkMainActivity;
import com.keeptrip.keeptrip.model.Trip;

public class StartActivitiesUtils {

    public static void startLandmarkMainActivity(Activity activity, Trip currentTrip){
        Intent intent = new Intent(activity, LandmarkMainActivity.class);
        intent.putExtra(LandmarkMainActivity.CURRENT_TRIP_PARAM, currentTrip);
        activity.startActivity(intent);
    }

    public static <Interface> Interface onAttachCheckInterface (Activity activity, Class<Interface> clazz) {
        Interface mCallback;

        try {
            mCallback = clazz.cast(activity);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement " + clazz.getSimpleName() + " Interface");
        }

        return mCallback;
    }
}
