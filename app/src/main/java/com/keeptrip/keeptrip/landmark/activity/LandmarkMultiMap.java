package com.keeptrip.keeptrip.landmark.activity;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.keeptrip.keeptrip.R;

import java.util.ArrayList;

public class LandmarkMultiMap extends LandmarkMap {

    private ArrayList<Marker> markers;
    private ArrayList<LatLng> points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        markers = new ArrayList<>();
        points = new ArrayList<>();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        int index = 0;
        LatLng landmarkLatLng;

        if (lmArrayList.isEmpty()){
            Toast.makeText(this, R.string.no_landmarks_to_view, Toast.LENGTH_SHORT).show();
            return;
        }

        do{
            landmarkLatLng = new LatLng(
                    lmArrayList.get(index).getGPSLocation().getLatitude(),
                    lmArrayList.get(index).getGPSLocation().getLongitude()
            );

            // Add a marker in Landmark
            int spinnerPosition = lmArrayList.get(index).getTypePosition();
            Marker marker = mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromBitmap(getBitmap(iconTypeArray.getResourceId(spinnerPosition, -1))))
                            .title(lmArrayList.get(index).getTitle())
                            .position(landmarkLatLng));

            markerToLmIndex.put(marker, index);

            markers.add(marker);
            points.add(landmarkLatLng);

            index ++;
        } while (index < lmArrayList.size());

//        points.add(new LatLng(32,35)); mMap.addMarker(new MarkerOptions().position(new LatLng(32,35)));
//        points.add(new LatLng(32.2,35.1)); mMap.addMarker(new MarkerOptions().position(new LatLng(32.2,35.1)));
//        points.add(new LatLng(32.6,35.15)); mMap.addMarker(new MarkerOptions().position(new LatLng(32.6,35.15)));
//        points.add(new LatLng(32.45,35.25));mMap.addMarker(new MarkerOptions().position(new LatLng(32.45,35.25)));
//        points.add(new LatLng(31.80,34.9));mMap.addMarker(new MarkerOptions().position(new LatLng(31.80,34.9)));
//        points.add(new LatLng(31.10,35.8));mMap.addMarker(new MarkerOptions().position(new LatLng(31.10,35.8)));
//        points.add(new LatLng(30,32));mMap.addMarker(new MarkerOptions().position(new LatLng(30,32)));
//        points.add(new LatLng(32,35));mMap.addMarker(new MarkerOptions().position(new LatLng(32,35)));
//        points.add(new LatLng(34,38));mMap.addMarker(new MarkerOptions().position(new LatLng(34,38)));
//        points.add(new LatLng(35,39));mMap.addMarker(new MarkerOptions().position(new LatLng(35,39)));
//        points.add(new LatLng(35,40));mMap.addMarker(new MarkerOptions().position(new LatLng(35,40)));



        CameraUpdate cu;
        if(markers.size() == 1){
            cu = CameraUpdateFactory.newLatLngZoom(landmarkLatLng,15);
        }
        else{
            mMap.addPolyline(new PolylineOptions()
                    .addAll(points)
                    .width(5)
                    .color(R.color.accent));

            LatLngBounds bounds = getMarkersBound(markers);
            cu = CameraUpdateFactory.newLatLngBounds(bounds, 100);
        }

        // move the camera
        mMap.animateCamera(cu, 2000, null);

    }

    LatLngBounds getMarkersBound(ArrayList<Marker> markers){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }
        return builder.build();
    }
}
