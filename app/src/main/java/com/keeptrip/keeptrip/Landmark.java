package com.keeptrip.keeptrip;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Landmark implements Parcelable {

    private static final int DEFAULT_ID = -1;

    private int id;
    private int tripId;
    private String title;
    private String photoPath; // TODO: check what is the image type
    private Date date;
    private String location;
    private Location GPSLocation;
    private String description;
    private int typePosition; //TODO: change it to enum? where to define?

    public Landmark(int tripId, String title, String photoPath, Date date, String location, Location GPSLocation, String description, int typePosition){
        this(DEFAULT_ID, tripId, title, photoPath, date, location, GPSLocation, description, typePosition);
    }

    public Landmark(int id, int tripId, String title, String photoPath, Date date, String location, Location GPSLocation, String description, int typePosition){
        this.id = id;
        this.tripId = tripId;
        this.title = title;
        this.photoPath = photoPath;
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
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
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

    public int getTypePosition() {
        return typePosition;
    }

    public void setTypePosition(int typePosition) {
        this.typePosition = typePosition;
    }


    protected Landmark(Parcel in) {
        id = in.readInt();
        tripId = in.readInt();
        title = in.readString();
        photoPath = in.readString();
        long tmpDate = in.readLong();
        date = tmpDate != -1 ? new Date(tmpDate) : null;
        location = in.readString();
        GPSLocation = (Location) in.readValue(Location.class.getClassLoader());
        description = in.readString();
        typePosition = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(tripId);
        dest.writeString(title);
        dest.writeString(photoPath);
        dest.writeLong(date != null ? date.getTime() : -1L);
        dest.writeString(location);
        dest.writeValue(GPSLocation);
        dest.writeString(description);
        dest.writeInt(typePosition);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Landmark> CREATOR = new Parcelable.Creator<Landmark>() {
        @Override
        public Landmark createFromParcel(Parcel in) {
            return new Landmark(in);
        }

        @Override
        public Landmark[] newArray(int size) {
            return new Landmark[size];
        }
    };
}