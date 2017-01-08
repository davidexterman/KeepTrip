package com.keeptrip.keeptrip.landmark.activity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.keeptrip.keeptrip.model.Landmark;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LandmarkSingleMap extends LandmarkMap {

    public static final String TAG = LandmarkSingleMap.class.getSimpleName();

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

        updateAddressLocation(null);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);

        setListeners();

        // Create new LatLng
        landmarkLatLng = new LatLng(
                lmCurrent.getGPSLocation().getLatitude(),
                lmCurrent.getGPSLocation().getLongitude()
        );

        // Create and Add a marker
        Marker marker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(getBitmap(iconTypeArray.getResourceId(lmSpinnerPosition, -1))))
                .title(lmCurrent.getTitle())
                .position(landmarkLatLng));

        // Add 0 index to receive is from array
        markerToLmIndex.put(marker, 0);

        // Move Camera
        mMap.animateCamera(CameraUpdateFactory
                .newLatLngZoom(landmarkLatLng,15), 2000, null);
    }

    private void setListeners(){
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng point) {
                mMap.clear();
                mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromBitmap(getBitmap(iconTypeArray.getResourceId(lmSpinnerPosition, -1))))
                        .title(lmCurrent.getTitle())
                        .position(point));

                // Animating to the touched position
                mMap.animateCamera(CameraUpdateFactory.newLatLng(point));

                // save the new landmark GPS location and string location
                updateAddressLocation(point);
            }
        });
    }

    private void updateAddressLocation(LatLng point){
        if(point != null){
            landmarkLocation.setLatitude(point.latitude);
            landmarkLocation.setLongitude(point.longitude);
        }
        try {
            List<Address> addresses = gcd.getFromLocation(landmarkLocation.getLatitude(), landmarkLocation.getLongitude(), 1);
            if (addresses.size() > 0) {
                resultIntent.putExtra(LandmarkMainActivity.LandmarkNewLocation, addresses.get(0).getLocality());
            }
        }catch (IOException e){
            Log.i(TAG, "IOException = " + e.getCause());
        }

        resultIntent.putExtra(LandmarkMainActivity.LandmarkNewGPSLocation, landmarkLocation);
        setResult(RESULT_OK, resultIntent);
    }

}
