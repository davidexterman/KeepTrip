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
            final Trip trip2 = new Trip(2, "another awesome trip!", date, "", "", "");
            final Landmark landmark1 = createLandmark(1, 1, "Haifa!", true);
            final Landmark landmark2 = createLandmark(2, 1, "Netanya!", true);
            final Landmark landmark3 = createLandmark(3, 1, "Herzliya!", false);
            final Landmark landmark4 = createLandmark(4, 1, "Tel-Aviv!", false);
            final Landmark landmark5 = createLandmark(5, 1, "Yafo!", true);
            final Landmark landmark6 = createLandmark(6, 1, "", true);

            this.Landmarks = new ArrayList<>();
            Landmarks.add(landmark1);
            Landmarks.add(landmark2);
            Landmarks.add(landmark3);
            Landmarks.add(landmark4);
            Landmarks.add(landmark5);
            Landmarks.add(landmark6);
            this.Trips = new ArrayList<>();
            Trips.add(trip1);
            addNewTrip(trip2);

            for (int i = 0 ; i < 2 ; i++){
                addNewTrip(trip2);
            }
        }
        catch (Exception e) {
            // ignore
        }
    }

    private Landmark createLandmark(int id, int tripId, String title, boolean withPhoto) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date date = null;// sdf.parse("01/11/2016");
        Uri path = Uri.parse("android.resource://com.keeptrip.keeptrip/landscape.jpg");
        String imagePath = withPhoto ? path.getPath() : null;
        return new Landmark(id, title, imagePath, date, "", new Location(""), "" , 0);
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
        trip.setId(Collections.max(Trips, new Comparator<Trip>() {
            @Override
            public int compare(Trip t1, Trip t2) {
                return (t1.getId() - t2.getId());
            }
        }).getId() + 1);
        Trips.add(trip);
    }

    @Override
    public Landmark[] getLandmarks(int tripId) {
        return Landmarks.toArray(new Landmark[Landmarks.size()]);
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
        landmark.setId(Collections.max(Landmarks, new Comparator<Landmark>() {
            @Override
            public int compare(Landmark l1, Landmark l2) {
                return (l1.getId() - l2.getId());
            }
        }).getId() + 1);
        Landmarks.add(landmark);
    }
}