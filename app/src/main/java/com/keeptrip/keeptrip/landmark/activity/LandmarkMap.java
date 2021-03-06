package com.keeptrip.keeptrip.landmark.activity;

import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.keeptrip.keeptrip.R;
import com.keeptrip.keeptrip.model.Landmark;
import com.keeptrip.keeptrip.utils.DateUtils;
import com.keeptrip.keeptrip.utils.ImageUtils;
import com.keeptrip.keeptrip.utils.PicassoMarker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class LandmarkMap extends AppCompatActivity implements OnMapReadyCallback, PlaceSelectionListener {

    // tag
    public static final String TAG = LandmarkMap.class.getSimpleName();

    //privates
    private SimpleDateFormat dateFormatter;
    private PicassoMarker picassoMarker;
    private PlaceAutocompleteFragment autocompleteFragment;

    //protected
    protected GoogleMap mMap;
    protected TypedArray iconTypeArray;
    protected ArrayList<Landmark> lmArrayList;
    protected Map<Marker, Integer> markerToLmIndex;
    protected boolean isFirstLoad = true;

    private static final String SAVE_IS_FIRST_LOAD = "SAVE_IS_FIRST_LOAD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landmarks_map);

        if (savedInstanceState != null) {
            isFirstLoad = savedInstanceState.getBoolean(SAVE_IS_FIRST_LOAD, false);
        }

        iconTypeArray = getResources().obtainTypedArray(R.array.landmark_map_marker_icon_type_array);
        markerToLmIndex = new HashMap<>();

        dateFormatter = DateUtils.getFormDateFormat();
        lmArrayList = getIntent().getExtras().getParcelableArrayList(LandmarkMainActivity.LandmarkArrayList);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        initAutoCompleteFilter();
        setListeners();
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
            mMap.getUiSettings().setCompassEnabled(true);
        }

        int upperPadding = getResources().getDimensionPixelSize(R.dimen.map_upper_padding);
        mMap.setPadding(0, upperPadding, 0, 0);
        mMap.getUiSettings().setZoomControlsEnabled(true);

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

                    int widthInPixel = getResources().getDimensionPixelSize(R.dimen.map_marker_width);
                    int heightInPixel = getResources().getDimensionPixelSize(R.dimen.map_marker_height);

                    ImageUtils.updatePhotoImageViewByPath(LandmarkMap.this, currentLm.getPhotoPath(), picassoMarker, widthInPixel, heightInPixel);
                }

                // Returning the view containing InfoWindow contents
                return v;
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(SAVE_IS_FIRST_LOAD, isFirstLoad);
    }

    private void setViewOrGone(TextView view, String text){
        if(text == null || text.trim().isEmpty()){
            view.setVisibility(View.GONE);
        }
        else{
            view.setText(text);
        }
    }

    private void initAutoCompleteFilter() {
        /*
        * The following code example shows setting an AutocompleteFilter on a PlaceAutocompleteFragment to
        * set a filter returning only results with a precise address.
        */
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_NONE)
                .build();
        autocompleteFragment.setFilter(typeFilter);

        String hintSearchText = getResources().getString(R.string.search_hint);
        autocompleteFragment.setHint(hintSearchText);
    }

    @Override
    public void onPlaceSelected(Place place) {
        int zoomScale = 18;
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(place.getLatLng(), zoomScale);

        mMap.animateCamera(cu, 2000, null);

//        LatLngBounds bounds = place.getViewport();
//
//        if (bounds != null) {
//            cu = CameraUpdateFactory.newLatLngBounds(place.getViewport(), 100);
//        } else {
//            int zoomScale = 18;
//            cu = CameraUpdateFactory.newLatLngZoom(place.getLatLng(), zoomScale);
//        }
    }

    @Override
    public void onError(Status status) {
        // TODO: Handle the error.
        Log.i(TAG, "An error occurred: " + status);
    }

    private void setListeners(){
        autocompleteFragment.setOnPlaceSelectedListener(this);
    }
}
