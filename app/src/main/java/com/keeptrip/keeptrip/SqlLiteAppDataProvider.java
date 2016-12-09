package com.keeptrip.keeptrip;

import android.location.Location;
import android.net.Uri;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import static java.lang.System.out;

public class SqlLiteAppDataProvider implements AppDataProvider {
    private ArrayList<Trip> Trips;
    private ArrayList<Landmark> Landmarks;

    @Override
    public void initialize() {
        try {
            out.println("initialize success");
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            Date date = sdf.parse("01/11/2016");

            final Trip trip1 = new Trip(1, "The best trip ever!", date, "kvish hahof", "No picture", "roh basear shotef et hanof");

            this.Landmarks = new ArrayList<>();
            Landmarks.add(createLandmark(1, 1, "Haifa!", true));
            Landmarks.add(createLandmark(2, 1, "Netanya!", true));
            Landmarks.add(createLandmark(3, 1, "Herzliya!", false));
            Landmarks.add(createLandmark(4, 1, "Tel-Aviv!", false));
            Landmarks.add(createLandmark(5, 1, "Yafo!", true));
            Landmarks.add(createLandmark(6, 1, "", true));
            Landmarks.add(createLandmark(7, 2, "", true));
            this.Trips = new ArrayList<>();
            Trips.add(trip1);
            addNewTrip(new Trip("another awesome trip!", date, "", "", ""));

            for (int i = 0 ; i < 2 ; i++){
                addNewTrip(new Trip("another awesome trip!", date, "", "", ""));
            }
        }
        catch (Exception e) {
            // ignore
        }
    }

    private Landmark createLandmark(int id, int tripId, String title, boolean withPhoto) {
        Date date = new Date();
        String imagePath = null;
        Landmark land = new Landmark(id, tripId, title, imagePath, date, "", new Location(""), "" , 0);
        return land;
    }

    @Override
    public Trip[] getTrips() {
        return Trips.toArray(new Trip[Trips.size()]);
    }

    @Override
    public void updateTripDetails(Trip trip) {
//        if (trip.getId() < 0) {
//            throw new IllegalArgumentException("Invalid trip Id");
//        }

        for (int i = 0; i < Trips.size(); i++) {
            if (Trips.get(i).getId() == trip.getId()) {
                Trips.set(i, trip);
            }
        }
    }

    @Override
    public Trip addNewTrip(Trip trip) {
        trip.setId(Collections.max(Trips, new Comparator<Trip>() {
            @Override
            public int compare(Trip t1, Trip t2) {
                return (t1.getId() - t2.getId());
            }
        }).getId() + 1);
        Trips.add(trip);

        return trip;
    }

    @Override
    public Landmark[] getLandmarks(int tripId) {
        ArrayList<Landmark> filterLandmarks = new ArrayList<>();

        for (int i = 0; i < Landmarks.size(); i++) {
            if (Landmarks.get(i).getTripId() == tripId) {
                filterLandmarks.add(Landmarks.get(i));
            }
        }

        return filterLandmarks.toArray(new Landmark[filterLandmarks.size()]);
    }

    @Override
    public void updateLandmarkDetails(Landmark landmark) {
        if (landmark.getId() < 0) {
            throw new IllegalArgumentException("Invalid landmark Id");
        }

        for (int i = 0; i < Landmarks.size(); i++) {
            if (Landmarks.get(i).getId() == landmark.getId()) {
                Landmarks.set(i, landmark);
                break;
            }
        }
    }

    @Override
    public void addNewLandmark(Landmark landmark) {
        landmark.setId(Collections.max(Landmarks, new Comparator<Landmark>() {
            @Override
            public int compare(Landmark l1, Landmark l2) {
                return (l1.getId() - l2.getId());
            }
        }).getId() + 1);
        Landmarks.add(landmark);
    }
}
