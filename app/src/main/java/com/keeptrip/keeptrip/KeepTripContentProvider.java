package com.keeptrip.keeptrip;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by david on 12/10/2016.
 */

public class KeepTripContentProvider extends ContentProvider{

    private KeepTripSQLiteHelper handler = null;

    private class Trips{
        //trips data
        private final static String TABLE_NAME = "trips_table";
        private final static String ID_COLUMN = "ID_COLUMN";
        private final static String TITLE_COLUMN = "TITLE_COLUMN";
        private final static String START_DATE_COLUMN = "START_DATE";
        private final static String END_DATE_COLUMN = "END_DATE";
        private final static String PLACE_COLUMN = "PLACE";
        private final static String PICTURE_COLUMN = "PICTURE";
        private final static String DESCRIPTION_COLUMN = "DESCRIPTION_COLUMN";

        // trip table create statement
        private final static String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +" (" +
                ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TITLE_COLUMN + " TEXT, " +
                START_DATE_COLUMN + " TEXT, " +
                END_DATE_COLUMN + " TEXT, " +
                PLACE_COLUMN + " TEXT, " +
                PICTURE_COLUMN + " TEXT, " +
                DESCRIPTION_COLUMN + " TEXT)";
    }

    private class Landmarks{
        //landmarks data
        private final static String TABLE_NAME = "landmarks_table";
        private final static String ID_COLUMN = "ID";
        private final static String TITLE_COLUMN = "TITLE";
        private final static String PHOTO_PATH_COLUMN = "PHOTO_PATH";
        private final static String DATE_COLUMN = "DATE";
        private final static String LOCATION_COLUMN = "LOCATION";
        private final static String GPS_LOCATION_COLUMN = "GPS_LOCATION";
        private final static String DESCRIPTION_COLUMN = "DESCRIPTION";
        private final static String TYPE_POSITION_COLUMN = "TYPE_POSITION";

        // landmark table create statement
        private final static String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +" (" +
                ID_COLUMN + "INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TITLE_COLUMN + " TEXT, " +
                PHOTO_PATH_COLUMN + " TEXT, " +
                DATE_COLUMN + " TEXT, " +
                LOCATION_COLUMN + " TEXT, " +
                GPS_LOCATION_COLUMN + " TEXT, " +
                DESCRIPTION_COLUMN + " TEXT, " +
                TYPE_POSITION_COLUMN + " TEXT)";
    }

    public final static String AUTHORITY = "com.keeptrip.keeptrip";

    /*
     * The scheme part for this provider's URI
     */
    private static final String SCHEME = "content://";

    /**
     * Path parts for the URIs
     */

    private static final String PATH_TRIPS = "trips";

    private static final String PATH_TRIP_ID = "trip/";

    private static final String PATH_LANDMARKS = "landmarks";

    private static final String PATH_LANDMARK_ID = "landmark/";

    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_TRIPS_URI = Uri.parse(SCHEME + AUTHORITY
            + '/' +  PATH_TRIPS);

    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_LANDMARKS_URI = Uri.parse(SCHEME + AUTHORITY
            + '/' +  PATH_LANDMARKS);

    /**
     * The content URI base for a single note. Callers must append a numeric
     * note id to this Uri to retrieve a note
     */
    public static final Uri CONTENT_TRIP_ID_URI_BASE = Uri.parse(SCHEME
            + AUTHORITY + '/' + PATH_TRIP_ID);

    public static final Uri CONTENT_LANDMARK_ID_URI_BASE = Uri.parse(SCHEME
            + AUTHORITY + '/' + PATH_LANDMARK_ID);

    private static final int TRIPS_ID_PATH_POSITION = 1;
    private static final int LANDMARKS_ID_PATH_POSITION = 1;


    /*
     * Constants used by the Uri matcher to choose an action based on the
     * pattern of the incoming URI
     */
    // The incoming URI matches the Links URI pattern
    private static final int TRIPS = 1;

    // The incoming URI matches the Link ID URI pattern
    private static final int TRIP_ID = 2;

    // The incoming URI matches the Links URI pattern
    private static final int LANDMARKS = 3;

    // The incoming URI matches the Link ID URI pattern
    private static final int LANDMARK_ID = 4;



    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, PATH_TRIPS, TRIPS);
        uriMatcher.addURI(AUTHORITY, PATH_TRIP_ID + "#", TRIP_ID);
        uriMatcher.addURI(AUTHORITY, PATH_LANDMARKS, LANDMARKS);
        uriMatcher.addURI(AUTHORITY, PATH_LANDMARK_ID + "#", LANDMARK_ID);
    }


    @Override
    public boolean onCreate() {
        handler = new KeepTripSQLiteHelper(getContext());
        return true;
    }

    final static private KeepTripContentProvider instance = new KeepTripContentProvider();

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] columns, String selection, String[] selectionArgs, String sortOrder) {
        /**
         * Choose the projection and adjust the "where" clause based on URI
         * pattern-matching.
         */
        String id = null;
        String finalWhere = "";
        String tableName = null;

        if (selection != null && selection.trim().length() > 0)
        {
            finalWhere = selection;
        }

        switch (uriMatcher.match(uri)) {
            // If the incoming URI is for notes, chooses the Notes projection
            case LANDMARKS:
                tableName = Landmarks.TABLE_NAME;
                break;
            case TRIPS:
                tableName = Trips.TABLE_NAME;
                break;

            case LANDMARK_ID:
                tableName = Landmarks.TABLE_NAME;
                id = uri.getPathSegments().get(LANDMARKS_ID_PATH_POSITION);

                finalWhere = Landmarks.ID_COLUMN + "=" + id;

                if (selection != null && selection.trim().length() > 0)
                {
                    finalWhere += selection + " AND " + finalWhere;
                }
                break;
            case TRIP_ID:
                tableName = Trips.TABLE_NAME;
                id = uri.getPathSegments().get(TRIPS_ID_PATH_POSITION);

                finalWhere += Trips.ID_COLUMN + "=" + id;

                if (selection != null && selection.trim().length() > 0)
                {
                    finalWhere = selection + " AND " + finalWhere;
                }
                break;

            default:
                // If the URI doesn't match any of the known patterns, throw an
                // exception.
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase database = handler.getReadableDatabase();

        /*
		 * Performs the query. If no problems occur trying to read the database,
		 * then a Cursor object is returned; otherwise, the cursor variable
		 * contains null. If no records were selected, then the Cursor object is
		 * empty, and Cursor.getCount() returns 0.
		 */
        Cursor cursor = database.query(tableName, columns, // The columns to return from the query
                finalWhere, // The columns for the where clause
                selectionArgs, // The values for the where clause
                null, // don't group the rows
                null, // don't filter by row groups
                null // The sort order
        );

        // Tells the Cursor what URI to watch, so it knows when its source data
        // changes
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        /**
         * Chooses the MIME type based on the incoming URI pattern
         */
        switch (uriMatcher.match(uri)) {

            case LANDMARKS:
            case TRIPS:
            case LANDMARK_ID:
            case TRIP_ID:

                // If the URI pattern doesn't match any permitted patterns, throws
                // an exception.
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        Uri baseUrl = null;
        String tableName;
        switch (uriMatcher.match(uri))
        {
            case LANDMARKS:
                baseUrl = CONTENT_LANDMARK_ID_URI_BASE;
                tableName = Landmarks.TABLE_NAME;
                break;
            case TRIPS:
                baseUrl = CONTENT_TRIP_ID_URI_BASE;
                tableName = Trips.TABLE_NAME;
                break;

            case LANDMARK_ID:
                baseUrl = CONTENT_LANDMARK_ID_URI_BASE;
                tableName = Landmarks.TABLE_NAME;
                break;
            case TRIP_ID:
                baseUrl = CONTENT_TRIP_ID_URI_BASE;
                tableName = Trips.TABLE_NAME;
                break;

            // If the URI pattern doesn't match any permitted patterns, throws an
            // exception.
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;

        if (contentValues != null)
        {
            values = new ContentValues(contentValues);
        }
        else
        {
            values = new ContentValues();
        }
        SQLiteDatabase db = handler.getWritableDatabase();

        long rowId = db.insert(tableName, null, values);

        if (rowId > 0)
        {
            Uri noteUri = ContentUris.withAppendedId(baseUrl, rowId);

            getContext().getContentResolver().notifyChange(noteUri, null);

            return noteUri;
        }

        throw new SQLException("Failed to insert row into " + uri);

    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

    private class KeepTripSQLiteHelper extends SQLiteOpenHelper{

        private final static String DATABASE_NAME = "KeepTrip.db";

        public KeepTripSQLiteHelper(Context context) {
            super(context, DATABASE_NAME, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // Create Trips Table
            db.execSQL(Trips.CREATE_TABLE);

            // Create Landmarks Table
            db.execSQL(Landmarks.CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + Trips.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + Landmarks.TABLE_NAME);
            onCreate(db);
        }

//        //@Override
//        public Trip addNewTrip(Trip trip) {
//            SQLiteDatabase db = this.getWritableDatabase();
//            ContentValues contentValues = new ContentValues();
//            contentValues.put(Trips.TITLE_COLUMN, trip.getTitle());
//            contentValues.put(Trips.START_DATE_COLUMN, trip.getStartDate().toString());
//            contentValues.put(Trips.END_DATE_COLUMN, trip.getEndDate().toString());
//            contentValues.put(Trips.PLACE_COLUMN, trip.getPlace());
//            contentValues.put(Trips.PICTURE_COLUMN, trip.getPicture());
//            contentValues.put(Trips.DESCRIPTION_COLUMN, trip.getDescription());
//            long result = db.insert(Trips.TABLE_NAME, null, contentValues);
//            if (result == -1) {
//                throw new SQLiteException();
//            }
//
//            // todo get the current trip's id from the db: int id = and use the trip's id setter
//            return trip;
//        }
//
//        @Override
//        public void addNewLandmark(Landmark landmark) {
//            SQLiteDatabase db = this.getWritableDatabase();
//            ContentValues contentValues = new ContentValues();
//            contentValues.put(LANDMARKS_COL2, landmark.getTitle());
//            contentValues.put(LANDMARKS_COL3, landmark.getPhotoPath());
//            contentValues.put(LANDMARKS_COL4, landmark.getDate().toString());
//            contentValues.put(LANDMARKS_COL5, landmark.getLocation());
//            contentValues.put(LANDMARKS_COL6, landmark.getGPSLocation().toString()); //todo parse to latitude longitude
//            contentValues.put(LANDMARKS_COL7, landmark.getDescription());
//            contentValues.put(LANDMARKS_COL8, landmark.getTypePosition());
//            long result = db.insert(LANDMARKS_TABLE_NAME, null, contentValues);
//            if (result == -1) {
//                throw new SQLiteException();
//            }
//        }
    }

}
