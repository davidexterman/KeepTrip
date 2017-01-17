package com.keeptrip.keeptrip.utils;

import android.app.Activity;
import android.content.Intent;

import com.keeptrip.keeptrip.landmark.activity.LandmarkMainActivity;
import com.keeptrip.keeptrip.model.Trip;

public class StartActivitiesUtils {
    public static final int  NOT_JUMP_TO_LANDMARK_ID = -1;

    public static void startLandmarkMainActivity(Activity activity, Trip currentTrip, int landmarkId) {
        Intent intent = new Intent(activity, LandmarkMainActivity.class);
        intent.putExtra(LandmarkMainActivity.CURRENT_TRIP_PARAM, currentTrip);
        intent.putExtra(LandmarkMainActivity.CURRENT_LANDMARK_ID_PARAM, landmarkId);

        activity.startActivity(intent);
    }

    public static void startLandmarkMainActivity(Activity activity, Trip currentTrip) {
        startLandmarkMainActivity(activity, currentTrip, NOT_JUMP_TO_LANDMARK_ID);
    }

    public static <Interface> Interface onAttachCheckInterface (Object objToAttach, Class<Interface> clazz) {
        Interface mCallback;

        try {
            mCallback = clazz.cast(objToAttach);
        } catch (ClassCastException e) {
            throw new ClassCastException(objToAttach.toString()
                    + " must implement " + clazz.getSimpleName() + " Interface");
        }

        return mCallback;
    }
}
