package com.keeptrip.keeptrip.landmark.activity;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.model.Landmark;

public class LandmarkSingleMap extends LandmarkMap {

    private Landmark lmCurrent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lmCurrent = lmArrayList.get(0);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);

        int spinnerPosition = lmCurrent.getTypePosition();

        // Create new LatLng
        LatLng landmarkLatLng = new LatLng(
                lmCurrent.getGPSLocation().getLatitude(),
                lmCurrent.getGPSLocation().getLongitude()
        );

        // Create and Add a marker
        Marker marker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(getBitmap(iconTypeArray.getResourceId(spinnerPosition, -1))))
                .title(lmCurrent.getTitle())
                .position(landmarkLatLng));

        // Add 0 index to receive is from array
        markerToLmIndex.put(marker, 0);

        // Move Camera
        mMap.animateCamera(CameraUpdateFactory
                .newLatLngZoom(landmarkLatLng,15), 2000, null);
    }
}
