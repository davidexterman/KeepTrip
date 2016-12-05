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
        final Trip trip1 = new Trip(1, "The best trip ever!", date, "kvish hahof", "No picture", "roh basear shotef et hanof");
        final Trip trip2 = new Trip(2, "another awesome trip!", date, "", "No picture", "");
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
        Trips.add(trip1);
        Trips.add(trip2);

        for (int i = 0 ; i < 10 ; i++){
            Trips.add(trip2);
        }

    }

    private Landmark createLandmark(int id, int tripId, String title) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date date = null;// sdf.parse("01/11/2016");
        return new Landmark(id, "Natanya!", "", date, "", 0.0, 0.0, "" , 0);
    }

    @Override
    public Trip[] getTrips() {
        return Trips.toArray(new Trip[Trips.size()]);
    }

    @Override
    public void updateTripDetails(Trip trip) {
        for (int i = 0; i < Trips.size(); i++) {
            if (Trips.get(i).getId() == trip.getId()) {
                Trips.set(i, trip);
            }
        }
    }

    @Override
    public void addNewTrip(Trip trip) {
        Trips.add(trip);
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

    @Override
    public void addNewLandmark(Landmark landmark) {
        Landmarks.add(landmark);
    }
}
