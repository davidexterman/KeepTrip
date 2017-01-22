package com.keeptrip.keeptrip.contentProvider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.keeptrip.keeptrip.model.Landmark;

public class KeepTripContentProvider extends ContentProvider{

    private final static String TAG = KeepTripContentProvider.class.getName();
    private KeepTripSQLiteHelper handler = null;

    public class Trips{
        //trips data
        public final static String TABLE_NAME = "trips_table";
        public final static String ID_COLUMN = "_id";
        public final static String TITLE_COLUMN = "TITLE";
        public final static String START_DATE_COLUMN = "START_DATE";
        public final static String END_DATE_COLUMN = "END_DATE";
        public final static String PLACE_COLUMN = "PLACE";
        public final static String PICTURE_COLUMN = "PICTURE";
        public final static String DESCRIPTION_COLUMN = "DESCRIPTION";

        // trip table create statement
        private final static String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +" (" +
                ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                TITLE_COLUMN + " TEXT, " +
                START_DATE_COLUMN + " TEXT, " +
                END_DATE_COLUMN + " TEXT, " +
                PLACE_COLUMN + " TEXT, " +
                PICTURE_COLUMN + " TEXT, " +
                DESCRIPTION_COLUMN + " TEXT)";
    }

    public class Landmarks{
        //landmarks data
        public final static String TABLE_NAME = "landmarks_table";
        public final static String ID_COLUMN = "_id";
        public final static String TRIP_ID_COLUMN = "TRIP_ID";
        public final static String TITLE_COLUMN = "TITLE";
        public final static String PHOTO_PATH_COLUMN = "PHOTO_PATH";
        public final static String DATE_COLUMN = "DATE";
        public final static String AUTOMATIC_LOCATION_COLUMN = "LOCATION";
        public final static String LOCATION_LATITUDE_COLUMN = "LOCATION_LATITUDE";
        public final static String LOCATION_LONGITUDE_COLUMN = "LOCATION_LONGITUDE";
        public final static String LOCATION_DESCRIPTION_COLUMN = "LOCATION_DESCRIPTION";
        public final static String DESCRIPTION_COLUMN = "DESCRIPTION";
        public final static String TYPE_POSITION_COLUMN = "TYPE_POSITION";

        // landmark table create statement
        private final static String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +" (" +
                ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                TRIP_ID_COLUMN + " INTEGER, " +
                TITLE_COLUMN + " TEXT, " +
                PHOTO_PATH_COLUMN + " TEXT, " +
                DATE_COLUMN + " TEXT, " +
                AUTOMATIC_LOCATION_COLUMN + " TEXT, " +
                LOCATION_LATITUDE_COLUMN + " DOUBLE, " +
                LOCATION_LONGITUDE_COLUMN + " DOUBLE, " +
                LOCATION_DESCRIPTION_COLUMN + " STRING, " +
                DESCRIPTION_COLUMN + " TEXT, " +
                TYPE_POSITION_COLUMN + " INTEGER)";
    }

    public class SearchGroups {
        //SearchGroups data
        public final static String ID_COLUMN = "_id";
        public final static String TITLE_COLUMN = "TITLE";
    }

    public class SearchLandmarkResults {
        //SearchLandmarkResults data
        public final static String TABLE_NAME = Landmarks.TABLE_NAME + " INNER JOIN " + Trips.TABLE_NAME +
                                                " ON " + Landmarks.TABLE_NAME + "." + Landmarks.TRIP_ID_COLUMN +
                                                " = " + Trips.TABLE_NAME + "." + Trips.ID_COLUMN;

        public final static String LANDMARK_TITLE_COLUMN = Landmarks.TABLE_NAME + "." + Landmarks.TITLE_COLUMN;
        public final static String AUTOMATIC_LOCATION_COLUMN = Landmarks.TABLE_NAME + "." + Landmarks.AUTOMATIC_LOCATION_COLUMN;
        public final static String LOCATION_DESCRIPTION_COLUMN = Landmarks.TABLE_NAME + "." + Landmarks.LOCATION_DESCRIPTION_COLUMN;
        public final static String DESCRIPTION_COLUMN = Landmarks.TABLE_NAME + "." + Landmarks.DESCRIPTION_COLUMN;

        // Trip
        public final static String TRIP_TITLE_COLUMN = "TRIP_TITLE";
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

    private static final String PATH_TRIP_ID = "trips/";

    private static final String PATH_LANDMARKS = "landmarks";

    private static final String PATH_LANDMARK_ID = "landmarks/";

    private static final String PATH_SEARCH_GROUPS = "search_groups/";

    private static final String PATH_SEARCH_LANDMARK_RESULTS = "search_landmark_results/";

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
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_SEARCH_GROUPS_URI = Uri.parse(SCHEME + AUTHORITY
            + '/' +  PATH_SEARCH_GROUPS);

    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_SEARCH_LANDMARK_RESULTS_URI = Uri.parse(SCHEME + AUTHORITY
            + '/' +  PATH_SEARCH_LANDMARK_RESULTS);

    /**
     * The content URI base for a single note. Callers must append a numeric
     * note id to this Uri to retrieve a note
     */
    public static final Uri CONTENT_TRIP_ID_URI_BASE = Uri.parse(SCHEME
            + AUTHORITY + '/' + PATH_TRIP_ID);

    public static final Uri CONTENT_LANDMARK_ID_URI_BASE = Uri.parse(SCHEME
            + AUTHORITY + '/' + PATH_LANDMARK_ID);

    public static final int TRIPS_ID_PATH_POSITION = 1;
    public static final int LANDMARKS_ID_PATH_POSITION = 1;

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

    // The incoming URI matches the Link URI pattern
    private static final int SEARCH_GROUPS = 5;

    // The incoming URI matches the Link URI pattern
    private static final int SEARCH_LANDMARK_RESULTS = 6;


    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, PATH_TRIPS, TRIPS);
        uriMatcher.addURI(AUTHORITY, PATH_TRIP_ID + "#", TRIP_ID);
        uriMatcher.addURI(AUTHORITY, PATH_LANDMARKS, LANDMARKS);
        uriMatcher.addURI(AUTHORITY, PATH_LANDMARK_ID + "#", LANDMARK_ID);
        uriMatcher.addURI(AUTHORITY, PATH_SEARCH_GROUPS, SEARCH_GROUPS);
        uriMatcher.addURI(AUTHORITY, PATH_SEARCH_LANDMARK_RESULTS, SEARCH_LANDMARK_RESULTS);
    }


    @Override
    public boolean onCreate() {
        handler = new KeepTripSQLiteHelper(getContext());
        return true;
    }

    final static private KeepTripContentProvider instance = new KeepTripContentProvider();

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] columns, String selection, String[] selectionArgs, String sortOrder) {
        /**
         * Choose the projection and adjust the "where" clause based on URI
         * pattern-matching.
         */
        String id;
        String finalWhere = "";
        String tableName = "";
        String orderBy = "";
        String rawQuery = null;

        if (selection != null && selection.trim().length() > 0)
        {
            finalWhere = selection;
        }

        switch (uriMatcher.match(uri)) {
            // If the incoming URI is for notes, chooses the Notes projection
            case LANDMARKS:
                tableName = Landmarks.TABLE_NAME;
                orderBy = Landmarks.DATE_COLUMN + " DESC ";
                break;
            case TRIPS:
                tableName = Trips.TABLE_NAME;
                orderBy = Trips.START_DATE_COLUMN + " DESC ";
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

            case SEARCH_GROUPS:
                String[] searchColumns = new String[] { SearchGroups.ID_COLUMN, SearchGroups.TITLE_COLUMN };

                MatrixCursor matrixCursor= new MatrixCursor(searchColumns);

                matrixCursor.addRow(new Object[] { 0, "Trips" });
                matrixCursor.addRow(new Object[] { 1, "Landmarks" });

                return matrixCursor;

            case SEARCH_LANDMARK_RESULTS:
                rawQuery = "SELECT " + Landmarks.TABLE_NAME +  ".*, " + Trips.TABLE_NAME + "." + Trips.TITLE_COLUMN + " AS " + SearchLandmarkResults.TRIP_TITLE_COLUMN + " "
                            + "FROM " + SearchLandmarkResults.TABLE_NAME + " "
                            + "WHERE " + finalWhere + " "
                            + "ORDER BY " + Landmarks.TABLE_NAME + "." + Landmarks.DATE_COLUMN + " DESC ";

                break;


            default:
                // If the URI doesn't match any of the known patterns, throw an
                // exception.
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if(sortOrder != null){
            orderBy += sortOrder;
        }

        SQLiteDatabase database = handler.getReadableDatabase();

        /*
		 * Performs the query. If no problems occur trying to read the database,
		 * then a Cursor object is returned; otherwise, the cursor variable
		 * contains null. If no records were selected, then the Cursor object is
		 * empty, and Cursor.getCount() returns 0.
		 */
        Cursor cursor;
        if (!TextUtils.isEmpty(rawQuery)) {
            cursor = database.rawQuery(rawQuery, selectionArgs);
        } else {
            cursor = database.query(tableName, columns, // The columns to return from the query
                    finalWhere, // The columns for the where clause
                    selectionArgs, // The values for the where clause
                    null, // don't group the rows
                    null, // don't filter by row groups
                    orderBy // The sort order
            );
        }


        // Tells the Cursor what URI to watch, so it knows when its source data
        // changes
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
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
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        Uri baseUrl;
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
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        String id;
        String finalWhere = "";
        String tableName;
        switch (uriMatcher.match(uri)) {
            case LANDMARKS:
                tableName = Landmarks.TABLE_NAME;
                if (selection != null && selection.trim().length() > 0) {
                    finalWhere = selection;
                }
                break;

            case TRIPS:
                tableName = Trips.TABLE_NAME;
                if (selection != null && selection.trim().length() > 0) {
                    finalWhere = selection;
                }
                break;

            case LANDMARK_ID:
                tableName = Landmarks.TABLE_NAME;
                id = uri.getPathSegments().get(LANDMARKS_ID_PATH_POSITION);
                finalWhere = Landmarks.ID_COLUMN + "=" + id;

                if (selection != null && selection.trim().length() > 0)
                {
                    finalWhere = selection + " AND " + finalWhere;
                }
                break;
            case TRIP_ID:
                tableName = Trips.TABLE_NAME;
                id = uri.getPathSegments().get(TRIPS_ID_PATH_POSITION);

                finalWhere = Trips.ID_COLUMN + "=" + id;

                if (selection != null && selection.trim().length() > 0) {
                    finalWhere = selection + " AND " + finalWhere;
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase database = handler.getWritableDatabase();

        try
        {
            int rowDeleted = database.delete(tableName, finalWhere, selectionArgs);
            if (rowDeleted > 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return rowDeleted;
        }
        catch(Throwable e)
        {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

        return -1;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        String id;
        String finalWhere = "";
        String tableName;

        switch (uriMatcher.match(uri))
        {
            case LANDMARKS:
                tableName = Landmarks.TABLE_NAME;
                if (selection != null && selection.trim().length() > 0) {
                    finalWhere = selection;
                }

                break;
            case TRIPS:
                tableName = Trips.TABLE_NAME;
                if (selection != null && selection.trim().length() > 0) {
                    finalWhere = selection;
                }

                break;

            case LANDMARK_ID:
                tableName = Landmarks.TABLE_NAME;
                id = uri.getPathSegments().get(LANDMARKS_ID_PATH_POSITION);

                finalWhere = Landmarks.ID_COLUMN + "=" + id;

                if (selection != null && selection.trim().length() > 0)
                {
                    finalWhere = selection + " AND " + finalWhere;
                }
                break;

            case TRIP_ID:
                tableName = Trips.TABLE_NAME;
                id = uri.getPathSegments().get(TRIPS_ID_PATH_POSITION);

                finalWhere = Trips.ID_COLUMN + "=" + id;

                if (selection != null && selection.trim().length() > 0)
                {
                    finalWhere = selection + " AND " + finalWhere;
                }
                break;

            default:

                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase database = handler.getWritableDatabase();
        try {
            int rowEffected = database.update(tableName, contentValues, finalWhere, selectionArgs);

            if (rowEffected > 0)
            {
                getContext().getContentResolver().notifyChange(uri, null);
            }

            return rowEffected;
        }
        catch(Throwable e)
        {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    private class KeepTripSQLiteHelper extends SQLiteOpenHelper{

        private final static String DATABASE_NAME = "KeepTrip.db";
        private static final int DATABASE_VERSION = 2;

        private KeepTripSQLiteHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            getWritableDatabase();
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            try {
                // Create Trips Table
                db.execSQL(Trips.CREATE_TABLE);
            }
            catch(SQLiteException e)
            {
                e.printStackTrace();
                Log.e(TAG,"Failed to create the Trips table");
                throw new RuntimeException(e.getMessage());
            }
            try{
                // Create Landmarks Table
                db.execSQL(Landmarks.CREATE_TABLE);
            }
            catch(SQLiteException e)
            {
                e.printStackTrace();
                Log.e(TAG,"Failed to create the Landmarks table");
                throw new RuntimeException(e.getMessage());
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (newVersion > oldVersion) {
                db.execSQL("ALTER TABLE " + Landmarks.TABLE_NAME + " ADD COLUMN " + Landmarks.LOCATION_DESCRIPTION_COLUMN + " STRING");
            }
//            db.execSQL("DROP TABLE IF EXISTS " + Trips.TABLE_NAME);
//            db.execSQL("DROP TABLE IF EXISTS " + Landmarks.TABLE_NAME);
//            onCreate(db);
        }
    }
}
