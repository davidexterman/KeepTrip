package com.keeptrip.keeptrip;

public class Trip {

}

public class Landmark {

}

public interface AppDataProvider {
    public void initialize();
    public Trip[] getTrips();
    public void updateTripDetails(Trip trip);
    public Landmark[] getLandmarks(int tripId);
    public void updateLandmarkDetails(Landmark landmark);
}
