package com.keeptrip.keeptrip;

import java.util.Date;

public class Trip {

    private static final int DEFAULT_ID_VALUE = -1;

    private int id;
    private String title;
    private Date startDate;
    private Date endDate;
    private String place;
    private String picture; // TODO: check the true type needed.
    private String description;

    public Trip(String title, Date startDate, String place, String picture, String description) {
        this(DEFAULT_ID_VALUE, title, startDate, place, picture, description);
    }

    public Trip(int id, String title, Date startDate, String place, String picture, String description) {
        this.id = id;
        this.title = title;
        this.startDate = startDate;
        this.endDate = startDate;
        this.place = place;
        this.picture = picture; // TODO: check the default picture value needed if empty
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
