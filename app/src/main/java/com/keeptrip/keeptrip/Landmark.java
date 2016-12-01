package com.keeptrip.keeptrip;

import java.util.Date;

public class Landmark {

    private static final int DEFAULT_ID = -1;

    private int id;
    private int TripId;
    private String title;
    private String photo; // TODO: check what is the image type
    private Date date;
    private String gpsLocation;  //TODO: verify the type
    private String location;
    private String description;
    private String type; //TODO: change it to enum? where to define?

    public Landmark(String title, String photo, Date date, String gpsLocation, String location, String description, String type){
        this(DEFAULT_ID, title, photo, date, gpsLocation, location, description, type);
    }

    public Landmark(int id, String title, String photo, Date date, String gpsLocation, String location, String description, String type){
        this.id = id;
        this.title = title;
        this.photo = photo;
        this.date = date;
        this.gpsLocation = gpsLocation;
        this.location = location;
        this.description = description;
        this.type = type;
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

    public String getGpsLocation() {
        return gpsLocation;
    }

    public void setGpsLocation(String gpsLocation) {
        this.gpsLocation = gpsLocation;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
