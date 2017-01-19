package com.keeptrip.keeptrip.landmark.activity;

import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.model.Landmark;
import com.keeptrip.keeptrip.utils.DateUtils;
import com.keeptrip.keeptrip.utils.PicassoMarker;
import com.keeptrip.keeptrip.utils.ImageUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class LandmarkMap extends AppCompatActivity implements OnMapReadyCallback {

    // tag
    public static final String TAG = LandmarkMap.class.getSimpleName();

    //privates
    private SimpleDateFormat dateFormatter;
    private PicassoMarker picassoMarker;

    //protected
    protected GoogleMap mMap;
    protected TypedArray iconTypeArray;
    protected ArrayList<Landmark> lmArrayList;
    protected Map<Marker, Integer> markerToLmIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landmarks_map);
        iconTypeArray = getResources().obtainTypedArray(R.array.landmark_map_marker_icon_type_array);
        markerToLmIndex = new HashMap<>();

        dateFormatter = DateUtils.getFormDateFormat();
        lmArrayList = getIntent().getExtras().getParcelableArrayList(LandmarkMainActivity.LandmarkArrayList);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap){
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        // Setting a custom info window adapter for the google map
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(Marker marker) {

                // Getting view from the layout file info_window_layout
                View v = getLayoutInflater().inflate(R.layout.landmark_card_map_view_layout, null);
                TextView lmTitleTextView = (TextView) v.findViewById(R.id.landmark_map_card_title_text_view);
                ImageView lmPhotoImageView = (ImageView) v.findViewById(R.id.landmark_map_card_cover_photo_view);
                TextView lmDateTextView = (TextView) v.findViewById(R.id.landmark_map_card_date_text_view);
                TextView lmAutomaticLocationTextView = (TextView) v.findViewById(R.id.landmark_map_card_automatic_location_text_view);
                TextView lmLocationDescriptionTextView = (TextView) v.findViewById(R.id.landmark_map_card_location_description_text_view);

                int lmIndex = markerToLmIndex.get(marker);
                Landmark currentLm = lmArrayList.get(lmIndex);

                setViewOrGone(lmTitleTextView, currentLm.getTitle());
                setViewOrGone(lmDateTextView, dateFormatter.format(currentLm.getDate()));
                setViewOrGone(lmAutomaticLocationTextView, currentLm.getAutomaticLocation());
                setViewOrGone(lmLocationDescriptionTextView, currentLm.getLocationDescription());

                if (currentLm.getPhotoPath() == null || currentLm.getPhotoPath().trim().isEmpty()) {
                    lmPhotoImageView.setVisibility(View.GONE);
                    ImageView textViewsImageViews = (ImageView) v.findViewById(R.id.landmark_map_card_text_views);
                    textViewsImageViews.setBackgroundColor(Color.WHITE);
                    textViewsImageViews.setAlpha((1f));
                } else {
                    picassoMarker = new PicassoMarker(marker, lmPhotoImageView);
                    ImageUtils.updatePhotoImageViewByPath(LandmarkMap.this, currentLm.getPhotoPath(), picassoMarker);
                }

                // Returning the view containing InfoWindow contents
                return v;
            }
        });
    }

    private void setViewOrGone(TextView view, String text){
        if(text == null || text.trim().isEmpty()){
            view.setVisibility(View.GONE);
        }
        else{
            view.setText(text);
        }
    }
}
