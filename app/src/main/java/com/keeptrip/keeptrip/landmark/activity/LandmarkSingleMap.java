package com.keeptrip.keeptrip.landmark.activity;

import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

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
    private static final String LANDMARK_LOCATION = "landmarkLocation";

    private Landmark lmCurrent;
    private int lmSpinnerPosition;
    private Location landmarkLocation;
    private Intent resultIntent;
    private Geocoder gcd;
    private AsyncTask<Void, Void, String> updateLocationTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lmCurrent = lmArrayList.get(0);
        lmSpinnerPosition = lmCurrent.getTypePosition();
        resultIntent = new Intent();
        landmarkLocation = lmCurrent.getGPSLocation();
        gcd = new Geocoder(LandmarkSingleMap.this, Locale.getDefault());

        if(savedInstanceState!= null){
            landmarkLocation = savedInstanceState.getParcelable(LANDMARK_LOCATION);
        }

        resultIntent.putExtra(LandmarkMainActivity.LandmarkNewGPSLocation, lmCurrent.getGPSLocation());
        resultIntent.putExtra(LandmarkMainActivity.LandmarkNewLocation, lmCurrent.getAutomaticLocation());
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
                        .newLatLngZoom(landmarkLatLng, 15), 2000, null);
            }
        }else{
            Toast.makeText(this, R.string.gps_disabled_mark_map, Toast.LENGTH_LONG).show();
        }

        isFirstLoad = false;
    }

    private void setListeners(){
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng point) {
                setMarker(point);
            }
        });
    }

    private void setMarker(LatLng point) {
        mMap.clear();

        // add marker and update the marker/index dictionary
        addMarkerAndUpdateDict(point);

        // save the new landmark GPS location and string location
        updateAddressLocation(point);
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
        createUpdateLocationTask();
        setResult(RESULT_OK, resultIntent);
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putParcelable(LANDMARK_LOCATION, landmarkLocation);
    }

    private void createUpdateLocationTask(){
        if(updateLocationTask != null && updateLocationTask.getStatus() == AsyncTask.Status.RUNNING){
            updateLocationTask.cancel(true);
        }
        updateLocationTask = new AsyncTask<Void, Void, String>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                String nullStr = null;
                resultIntent.putExtra(LandmarkMainActivity.LandmarkNewLocation, nullStr);
            }

            @Override
            protected void onPostExecute(String stringResult) {
                super.onPostExecute(stringResult);
                resultIntent.putExtra(LandmarkMainActivity.LandmarkNewLocation, stringResult);
            }

            @Override
            protected String doInBackground(Void... params) {
                return LocationUtils.updateLmLocationString(LandmarkSingleMap.this, landmarkLocation);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void onStop() {
        if(updateLocationTask != null && updateLocationTask.getStatus() == AsyncTask.Status.RUNNING){
            updateLocationTask.cancel(true);
        }
        super.onStop();
    }
}
