package com.keeptrip.keeptrip;

import java.util.Date;

public class Landmark {

    private static final int DEFAULT_ID = -1;

    private int id;
    private int TripId;
    private String title;
    private String photo; // TODO: check what is the image type
    private Date date;
    private String location;
    private Double latitude;
    private Double longitude;
    private String description;
    private int typePosition; //TODO: change it to enum? where to define?

    public Landmark(String title, String photo, Date date, String location, Double latitude, Double longitude, String description, int typePosition){
        this(DEFAULT_ID, title, photo, date, location, latitude, longitude, description, typePosition);
    }

    public Landmark(int id, String title, String photo, Date date, String location, Double latitude, Double longitude, String description, int typePosition){
        this.id = id;
        this.title = title;
        this.photo = photo;
        this.date = date;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
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

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
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
