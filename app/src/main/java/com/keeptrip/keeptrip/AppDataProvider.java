package com.keeptrip.keeptrip;

public interface AppDataProvider {
    public void initialize();
    public Trip[] getTrips();
    public void updateTripDetails(Trip trip);
    public Trip addNewTrip(Trip trip);
    public Landmark[] getLandmarks(int tripId);
    public void updateLandmarkDetails(Landmark landmark);
    public void addNewLandmark(Landmark landmark);
}
