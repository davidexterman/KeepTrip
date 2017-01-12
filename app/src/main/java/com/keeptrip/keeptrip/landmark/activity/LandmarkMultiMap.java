package com.keeptrip.keeptrip.landmark.activity;

import android.location.Location;
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
        LatLng landmarkLatLng = null;

        if (lmArrayList.isEmpty()){
            Toast.makeText(this, R.string.no_landmarks_to_view, Toast.LENGTH_LONG).show();
            Toast.makeText(this, R.string.make_sure_gps_enabled_or_map, Toast.LENGTH_LONG).show();
            return;
        }

        Location currentLocation;
        do{
            currentLocation = lmArrayList.get(index).getGPSLocation();
            if(currentLocation == null){
                index ++;
                continue;
            }

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

        if(landmarkLatLng == null){
            Toast.makeText(this, R.string.no_landmarks_with_location_found, Toast.LENGTH_LONG).show();
            Toast.makeText(this, R.string.make_sure_gps_enabled_or_map, Toast.LENGTH_LONG).show();
        }else {

            CameraUpdate cu;
            if (markers.size() == 1) {
                cu = CameraUpdateFactory.newLatLngZoom(landmarkLatLng, 15);
            } else {
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
    }

    LatLngBounds getMarkersBound(ArrayList<Marker> markers){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }
        return builder.build();
    }
}