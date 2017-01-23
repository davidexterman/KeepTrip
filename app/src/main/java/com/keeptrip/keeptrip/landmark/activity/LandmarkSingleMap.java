package com.keeptrip.keeptrip.landmark.activity;

import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.model.Landmark;
import com.keeptrip.keeptrip.utils.ImageUtils;
import com.keeptrip.keeptrip.utils.LocationUtils;

import java.util.Locale;

public class LandmarkSingleMap extends LandmarkMap {

    public static final String TAG = LandmarkSingleMap.class.getSimpleName();
    private static final String SAVE_LANDMARK_LOCATION = "SAVE_LANDMARK_LOCATION";
    private static final String SAVE_LANDMARK_AUTOMATIC_LOCATION = "SAVE_LANDMARK_AUTOMATIC_LOCATION";

    private Landmark lmCurrent;
    private int lmSpinnerPosition;
    private Location landmarkLocation;
    private String landmarkAutomaticLocation;
    private Intent resultIntent;
    private Geocoder gcd;
    private AsyncTask<Void, Void, String> updateLocationTask;
    private Button doneButton;
    private Button cancelButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lmCurrent = lmArrayList.get(0);
        lmSpinnerPosition = lmCurrent.getTypePosition();
        resultIntent = new Intent();
        landmarkLocation = lmCurrent.getGPSLocation();
        gcd = new Geocoder(LandmarkSingleMap.this, Locale.getDefault());

        if(savedInstanceState!= null){
            landmarkLocation = savedInstanceState.getParcelable(SAVE_LANDMARK_LOCATION);
            landmarkAutomaticLocation = savedInstanceState.getString(SAVE_LANDMARK_AUTOMATIC_LOCATION);
            resultIntent.putExtra(LandmarkMainActivity.LandmarkNewGPSLocation, landmarkLocation);
            resultIntent.putExtra(LandmarkMainActivity.LandmarkNewLocation, landmarkAutomaticLocation);
        }
        else {
        resultIntent.putExtra(LandmarkMainActivity.LandmarkNewGPSLocation, lmCurrent.getGPSLocation());
        resultIntent.putExtra(LandmarkMainActivity.LandmarkNewLocation, lmCurrent.getAutomaticLocation());
        }

       findViewsByIdAndSetListeners();

        setResult(RESULT_CANCELED, resultIntent);

//        resultIntent.putExtra(LandmarkMainActivity.LandmarkNewGPSLocation, lmCurrent.getGPSLocation());
//        resultIntent.putExtra(LandmarkMainActivity.LandmarkNewLocation, lmCurrent.getAutomaticLocation());
    }

    private void findViewsByIdAndSetListeners(){
        doneButton = (Button) findViewById(R.id.map_done_button);
        doneButton.setVisibility(View.VISIBLE);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
        cancelButton = (Button) findViewById(R.id.map_cancel_button);
        cancelButton.setVisibility(View.VISIBLE);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED, resultIntent);
                finish();
            }
        });
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);

        setListeners();

        if (landmarkLocation != null){
            // Create new LatLng
            LatLng landmarkLatLng = new LatLng(
                    landmarkLocation.getLatitude(),
                    landmarkLocation.getLongitude()
            );

            // add marker and update the marker/index dictionary
            addMarkerAndUpdateDict(landmarkLatLng);

            // Move Camera
            if (isFirstLoad) {
                mMap.animateCamera(CameraUpdateFactory
                        .newLatLngZoom(landmarkLatLng, 15), 2000, new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        isFirstLoad = false;
                    }

                    @Override
                    public void onCancel() {
                        isFirstLoad = false;
                    }
                });
            }
        }else{
            Toast.makeText(this, R.string.no_previous_location_mark_map, Toast.LENGTH_LONG).show();
        }


    }

    private void setListeners(){
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng point) {
                setMarker(point, true);
            }
        });
    }

    private void setMarker(LatLng point, boolean isUpdateAutomaticLocation) {
        mMap.clear();

        // add marker and update the marker/index dictionary
        addMarkerAndUpdateDict(point);

        updateAddressLocation(point);

        if (isUpdateAutomaticLocation) {
            // save the new landmark GPS location and string location
            updateAutomaticLocation();
        }
    }

    private void updateAutomaticLocation() {
        createUpdateLocationTask();
    }


    public void setLandmarkAutomaticLocation(String landmarkAutomaticLocation) {
        resultIntent.putExtra(LandmarkMainActivity.LandmarkNewLocation, landmarkAutomaticLocation);
        this.landmarkAutomaticLocation = landmarkAutomaticLocation;
    }

    private Marker addMarkerAndUpdateDict(LatLng point){
        Marker marker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(ImageUtils.getBitmap(this, iconTypeArray.getResourceId(lmSpinnerPosition, -1))))
                .title(lmCurrent.getTitle())
                .position(point));

        // Add 0 index to receive is from array
        markerToLmIndex.put(marker, 0);
        return marker;
    }

    private void updateAddressLocation(LatLng point){
        if(point != null){
            if(landmarkLocation == null){
                landmarkLocation = new Location("");
            }
            landmarkLocation.setLatitude(point.latitude);
            landmarkLocation.setLongitude(point.longitude);
        }
        resultIntent.putExtra(LandmarkMainActivity.LandmarkNewGPSLocation, landmarkLocation);
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putParcelable(SAVE_LANDMARK_LOCATION, landmarkLocation);
        state.putString(SAVE_LANDMARK_AUTOMATIC_LOCATION, landmarkAutomaticLocation);
    }

    private void createUpdateLocationTask(){
        if(updateLocationTask != null && updateLocationTask.getStatus() == AsyncTask.Status.RUNNING){
            updateLocationTask.cancel(true);
        }
        updateLocationTask = new AsyncTask<Void, Void, String>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                setLandmarkAutomaticLocation(null);
            }

            @Override
            protected void onPostExecute(String stringResult) {
                super.onPostExecute(stringResult);
                setLandmarkAutomaticLocation(stringResult);
            }

            @Override
            protected String doInBackground(Void... params) {
                return LocationUtils.updateLmLocationString(LandmarkSingleMap.this, landmarkLocation);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void cancelTask(){
        if(updateLocationTask != null && updateLocationTask.getStatus() == AsyncTask.Status.RUNNING){
            updateLocationTask.cancel(true);
        }
    }

    public void onStop() {
        cancelTask();
        super.onStop();
    }

    @Override
    public void onPlaceSelected(Place place) {
        super.onPlaceSelected(place);

        setMarker(place.getLatLng(), false);
        setLandmarkAutomaticLocation(place.getName().toString());
    }
}
