package com.keeptrip.keeptrip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static java.lang.System.out;

public class SqlLiteAppDataProvider implements AppDataProvider {
    private ArrayList<Trip> Trips;
    private ArrayList<Landmark> Landmarks;

    @Override
    public void initialize() {
        out.println("initialize success");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date date = null;// sdf.parse("01/11/2016");
        final Trip trip = new Trip(1, "The best trip ever!", date, "kvish hahof", "No picture", "roh basear shotef et hanof");
        final Landmark landmark1 = createLandmark(1, 1, "Haifa!");
        final Landmark landmark2 = createLandmark(2, 1, "Netanya!");
        final Landmark landmark3 = createLandmark(3, 1, "Herzliya!");
        final Landmark landmark4 = createLandmark(4, 1, "Tel-Aviv!");

        this.Landmarks = new ArrayList<>();
        Landmarks.add(landmark1);
        Landmarks.add(landmark2);
        Landmarks.add(landmark3);
        Landmarks.add(landmark4);
        this.Trips = new ArrayList<>();
        Trips.add(trip);
    }

    private Landmark createLandmark(int id, int tripId, String title) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date date = null;// sdf.parse("01/11/2016");
        return new Landmark(id, "Natanya!", "", date, "", "", "" , "");
    }

    @Override
    public Trip[] getTrips() {
        return (Trip[])Trips.toArray();
    }

    @Override
    public void updateTripDetails(Trip trip) {
        if (trip.getId() == 0) {
            Trips.add(trip);
            return;
        }

        for (int i = 0; i < Trips.size(); i++) {
            if (Trips.get(i).getId() == trip.getId()) {
                Trips.set(i, trip);
            }
        }
    }

    @Override
    public Landmark[] getLandmarks(int tripId) {
        return (Landmark[])Landmarks.toArray();
    }

    @Override
    public void updateLandmarkDetails(Landmark landmark) {
        if (landmark.getId() == 0) {
            Landmarks.add(landmark);
            return;
        }

        for (int i = 0; i < Landmarks.size(); i++) {
            if (Landmarks.get(i).getId() == landmark.getId()) {
                Landmarks.set(i, landmark);
            }
        }
    }
}
