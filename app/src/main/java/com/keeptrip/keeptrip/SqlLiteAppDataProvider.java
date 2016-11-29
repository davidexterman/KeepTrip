package com.keeptrip.keeptrip;

import static java.lang.System.out;

public class SqlLiteAppDataProvider implements AppDataProvider {
    @Override
    public void initialize() {
        out.println("initialize success");
    }

    @Override
    public Trip[] getTrips() {
        return new Trip[0];
    }

    @Override
    public void updateTripDetails(Trip trip) {

    }

    @Override
    public Landmark[] getLandmarks(int tripId) {
        return new Landmark[0];
    }

    @Override
    public void updateLandmarkDetails(Landmark landmark) {

    }
}
