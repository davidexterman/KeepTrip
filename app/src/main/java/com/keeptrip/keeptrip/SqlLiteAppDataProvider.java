package com.keeptrip.keeptrip;

import android.location.Location;

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
            SimpleDateFormat sdfLong = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US);
            SimpleDateFormat sdfShort = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            Date date = sdfShort.parse("03/11/2016");

            final Trip trip1 = new Trip(1, "The best trip ever!", date, "kvish hahof", "No picture", "roh basear shotef et hanof");

            this.Landmarks = new ArrayList<>();
            Landmarks.add(createLandmark(1, 1, "Haifa!", sdfLong.parse("01/11/2016 17:12:23")));
            Landmarks.add(createLandmark(2, 1, "Netanya!", sdfLong.parse("01/11/2016 17:22:23")));
            Landmarks.add(createLandmark(3, 1, "Herzliya!", sdfLong.parse("01/11/2016 17:32:23")));
            Landmarks.add(createLandmark(4, 1, "Tel-Aviv!", sdfLong.parse("01/11/2016 17:42:23")));
            Landmarks.add(createLandmark(5, 1, "Yafo!", sdfLong.parse("02/11/2016 17:52:23")));
            Landmarks.add(createLandmark(6, 3, "wow!", sdfLong.parse("01/11/2016 17:42:23")));
            Landmarks.add(createLandmark(7, 2, "no way", sdfLong.parse("01/11/2016 17:42:23")));
            this.Trips = new ArrayList<>();
            Trips.add(trip1);
            addNewTrip(new Trip("another awesome trip!", sdfShort.parse("02/11/2016"), "", "", ""));

            for (int i = 0 ; i < 2 ; i++){
                addNewTrip(new Trip("another awesome trip!", sdfShort.parse("01/11/2016"), "", "", ""));
            }
        }
        catch (Exception e) {
            // ignore
        }
    }

    private Landmark createLandmark(int id, int tripId, String title, Date fromDate) {
        Date date = fromDate == null ?  new Date() : fromDate;
        return new Landmark(id, tripId, title, null, date, "", new Location(""), "" , 0);
    }

    @Override
    public Trip[] getTrips() {
        Collections.sort(Trips, new Comparator<Trip>() {
            @Override
            public int compare(Trip trip1, Trip trip2) {
                return trip2.getStartDate().compareTo(trip1.getStartDate());
            }
        });

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
    public void deleteTrip(int tripId){
        for (int i = 0; i < Trips.size(); i++) {
            if (Trips.get(i).getId() == tripId) {
                Trips.remove(i);
            }
        }
    }

    @Override
    public void deleteLandmark(int landmarkId){
        for (int i = 0; i < Landmarks.size(); i++) {
            if (Landmarks.get(i).getId() == landmarkId) {
                Landmarks.remove(i);
            }
        }
    }


    @Override
    public Landmark[] getLandmarks(int tripId) {
        ArrayList<Landmark> filterLandmarks = new ArrayList<>();

        for (int i = 0; i < Landmarks.size(); i++) {
            if (Landmarks.get(i).getTripId() == tripId) {
                filterLandmarks.add(Landmarks.get(i));
            }
        }
        Collections.sort(filterLandmarks, new Comparator<Landmark>() {
            @Override
            public int compare(Landmark landmark1, Landmark landmark2) {
                return landmark2.getDate().compareTo(landmark1.getDate());
            }
        });

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
    public Landmark addNewLandmark(Landmark landmark) {
        landmark.setId(Collections.max(Landmarks, new Comparator<Landmark>() {
            @Override
            public int compare(Landmark l1, Landmark l2) {
                return (l1.getId() - l2.getId());
            }
        }).getId() + 1);
        Landmarks.add(landmark);
        return landmark;
    }
}
