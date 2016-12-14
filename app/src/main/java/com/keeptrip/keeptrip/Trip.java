package com.keeptrip.keeptrip;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Trip implements Parcelable {

    private String dateFormatString = "YYYY-MM-DDTHH:MM:SS.SSS";

    private static final int DEFAULT_ID_VALUE = -1;

    private int id;
    private String title;
    private Date startDate;
    private Date endDate;
    private String place;
    private String picture; // TODO: check the true type needed.
    private String description;

    public Trip(Cursor cursor){
        final int COLUMN_ID = cursor.getColumnIndexOrThrow(KeepTripContentProvider.Trips.ID_COLUMN);
        final int COLUMN_TITLE = cursor.getColumnIndexOrThrow(KeepTripContentProvider.Trips.TITLE_COLUMN);
        final int COLUMN_START_DATE = cursor.getColumnIndexOrThrow(KeepTripContentProvider.Trips.START_DATE_COLUMN);
        final int COLUMN_END_DATE = cursor.getColumnIndexOrThrow(KeepTripContentProvider.Trips.END_DATE_COLUMN);
        final int COLUMN_PLACE = cursor.getColumnIndexOrThrow(KeepTripContentProvider.Trips.PLACE_COLUMN);
        final int COLUMN_PICTURE = cursor.getColumnIndexOrThrow(KeepTripContentProvider.Trips.PICTURE_COLUMN);
        final int COLUMN_DESCRIPTION = cursor.getColumnIndexOrThrow(KeepTripContentProvider.Trips.DESCRIPTION_COLUMN);

        id = cursor.getInt(COLUMN_ID);
        title = cursor.getString(COLUMN_TITLE);

        startDate = DbUtils.stringToDate(cursor.getString(COLUMN_START_DATE));
        endDate = DbUtils.stringToDate(cursor.getString(COLUMN_START_DATE));

        place = cursor.getString(COLUMN_PLACE);
        picture = cursor.getString(COLUMN_PICTURE);
        description = cursor.getString(COLUMN_DESCRIPTION);
    }

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

    public ContentValues tripToContentValues(){

        ContentValues contentValues = new ContentValues();
        contentValues.put(KeepTripContentProvider.Trips.TITLE_COLUMN, title);
        contentValues.put(KeepTripContentProvider.Trips.START_DATE_COLUMN, DbUtils.dateToString(startDate));
        contentValues.put(KeepTripContentProvider.Trips.END_DATE_COLUMN, DbUtils.dateToString(endDate));
        contentValues.put(KeepTripContentProvider.Trips.TITLE_COLUMN, title);
        contentValues.put(KeepTripContentProvider.Trips.PLACE_COLUMN, place);
        contentValues.put(KeepTripContentProvider.Trips.PICTURE_COLUMN, picture);
        contentValues.put(KeepTripContentProvider.Trips.DESCRIPTION_COLUMN, description);

        return contentValues;
    }

    protected Trip(Parcel in) {
        id = in.readInt();
        title = in.readString();
        long tmpStartDate = in.readLong();
        startDate = tmpStartDate != -1 ? new Date(tmpStartDate) : null;
        long tmpEndDate = in.readLong();
        endDate = tmpEndDate != -1 ? new Date(tmpEndDate) : null;
        place = in.readString();
        picture = in.readString();
        description = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeLong(startDate != null ? startDate.getTime() : -1L);
        dest.writeLong(endDate != null ? endDate.getTime() : -1L);
        dest.writeString(place);
        dest.writeString(picture);
        dest.writeString(description);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Trip> CREATOR = new Parcelable.Creator<Trip>() {
        @Override
        public Trip createFromParcel(Parcel in) {
            return new Trip(in);
        }

        @Override
        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };
}
