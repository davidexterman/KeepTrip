package com.keeptrip.keeptrip.landmark.activity;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
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
        mMap = googleMap;

        // Add a marker in Landmark and move the camera
        LatLng landmarkLatLng = new LatLng(
                lmCurrent.getGPSLocation().getLatitude(),
                lmCurrent.getGPSLocation().getLongitude()
        );
        mMap.animateCamera(CameraUpdateFactory
                .newLatLngZoom(landmarkLatLng,15), 2000, null);
        mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(getBitmap(R.drawable.hotel)))
                .title(lmCurrent.getTitle())
                .position(landmarkLatLng));
    }
}
