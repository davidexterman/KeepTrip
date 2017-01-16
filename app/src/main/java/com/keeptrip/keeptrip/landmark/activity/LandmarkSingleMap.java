package com.keeptrip.keeptrip.landmark.activity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
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

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LandmarkSingleMap extends LandmarkMap {

    public static final String TAG = LandmarkSingleMap.class.getSimpleName();
    private static final String LANDMARK_LOCATION = "landmarkLocation";

    private Landmark lmCurrent;
    private int lmSpinnerPosition;
    private LatLng landmarkLatLng;
    private Location landmarkLocation;
    private Intent resultIntent;
    private Geocoder gcd;

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

        updateAddressLocation(null);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);

        setListeners();

        if (landmarkLocation != null){
            // Create new LatLng
            landmarkLatLng = new LatLng(
                    landmarkLocation.getLatitude(),
                    landmarkLocation.getLongitude()
            );

            // add marker and update the marker/index dictionary
            addMarkerAndUpdateDict(landmarkLatLng);

            // Move Camera
            mMap.animateCamera(CameraUpdateFactory
                    .newLatLngZoom(landmarkLatLng,15), 2000, null);
        }else{
            Toast.makeText(this, R.string.gps_disabled_mark_map, Toast.LENGTH_LONG).show();
        }
    }

    private void setListeners(){
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng point) {
                mMap.clear();

                // add marker and update the marker/index dictionary
                addMarkerAndUpdateDict(point);

                // save the new landmark GPS location and string location
                updateAddressLocation(point);
            }
        });
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
        if(landmarkLocation == null){
            setResult(RESULT_CANCELED, resultIntent);
        }
        else {
            try {
                List<Address> addresses = gcd.getFromLocation(landmarkLocation.getLatitude(), landmarkLocation.getLongitude(), 1);
                if (addresses.size() > 0) {
                    Address ad = addresses.get(0);
                    String locationName = ad.getAddressLine(0) != null ? ad.getAddressLine(0) :
                            (ad.getLocality() != null ? ad.getLocality() : ad.getCountryName());
                    resultIntent.putExtra(LandmarkMainActivity.LandmarkNewLocation, locationName);
                }
            } catch (IOException e) {
                Log.i(TAG, "IOException = " + e.getCause());
            }

            resultIntent.putExtra(LandmarkMainActivity.LandmarkNewGPSLocation, landmarkLocation);
            setResult(RESULT_OK, resultIntent);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putParcelable(LANDMARK_LOCATION, landmarkLocation);
    }
}
