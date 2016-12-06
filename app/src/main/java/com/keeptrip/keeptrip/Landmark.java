package com.keeptrip.keeptrip;

import android.location.Location;

import java.util.Date;

public class Landmark {

    private static final int DEFAULT_ID = -1;

    private int id;
    private int TripId;
    private String title;
    private String photo; // TODO: check what is the image type
    private Date date;
    private String location;
    private Location GPSLocation;
    private String description;
    private int typePosition; //TODO: change it to enum? where to define?

    public Landmark(String title, String photo, Date date, String location, Location GPSLocation, String description, int typePosition){
        this(DEFAULT_ID, title, photo, date, location, GPSLocation, description, typePosition);
    }

    public Landmark(int id, String title, String photo, Date date, String location, Location GPSLocation, String description, int typePosition){
        this.id = id;
        this.title = title;
        this.photo = photo;
        this.date = date;
        this.location = location;
        this.GPSLocation = GPSLocation;
        this.description = description;
        this.typePosition = typePosition;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTripId() {
        return TripId;
    }

    public void setTripId(int tripId) {
        TripId = tripId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Location getGPSLocation() {
        return GPSLocation;
    }

    public void setGPSLocation(Location GPSLocation) {
        this.GPSLocation = GPSLocation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getType() {
        return typePosition;
    }

    public void setType(int typePosition) {
        this.typePosition = typePosition;
    }

}
